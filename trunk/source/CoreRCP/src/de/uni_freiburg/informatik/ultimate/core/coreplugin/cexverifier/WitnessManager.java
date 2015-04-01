package de.uni_freiburg.informatik.ultimate.core.coreplugin.cexverifier;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.core.coreplugin.Activator;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.preferences.CorePreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceStore;
import de.uni_freiburg.informatik.ultimate.core.services.IBacktranslationService;
import de.uni_freiburg.informatik.ultimate.core.services.IToolchainStorage;
import de.uni_freiburg.informatik.ultimate.core.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.core.util.CoreUtil;
import de.uni_freiburg.informatik.ultimate.core.util.MonitoredProcess;
import de.uni_freiburg.informatik.ultimate.core.util.MonitoredProcess.MonitoredProcessState;
import de.uni_freiburg.informatik.ultimate.result.CounterExampleResult;
import de.uni_freiburg.informatik.ultimate.result.IProgramExecution;
import de.uni_freiburg.informatik.ultimate.result.WitnessResult;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 * 
 */
public class WitnessManager {

	private final Logger mLogger;
	private final IUltimateServiceProvider mServices;
	private final IToolchainStorage mStorage;

	public WitnessManager(Logger logger, IUltimateServiceProvider services, IToolchainStorage storage) {
		mLogger = logger;
		mServices = services;
		mStorage = storage;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run() throws IOException, InterruptedException {
		UltimatePreferenceStore ups = new UltimatePreferenceStore(Activator.s_PLUGIN_ID);
		Collection<CounterExampleResult> cexResults = CoreUtil.filterResults(mServices.getResultService().getResults(),
				CounterExampleResult.class);

		IBacktranslationService backtrans = mServices.getBacktranslationService();

		for (CounterExampleResult cex : cexResults) {
			IProgramExecution pe = backtrans.translateProgramExecution(cex.getProgramExecution());
			String svcompWitness = pe.getSVCOMPWitnessString();

			boolean writeInWorkingDir = ups.getBoolean(CorePreferenceInitializer.LABEL_WITNESS_WRITE_WORKINGDIR);
			boolean writeBesideInputFile = ups.getBoolean(CorePreferenceInitializer.LABEL_WITNESS_WRITE);
			List<String> filenamesToDelete = new ArrayList<>();

			if (!writeBesideInputFile && !writeInWorkingDir) {
				continue;
			}
			String filename = null;
			if (writeInWorkingDir && svcompWitness != null) {
				filename = writeWitness(svcompWitness, null);
				filenamesToDelete.add(filename);
			}

			if (writeBesideInputFile && svcompWitness != null) {
				filename = writeWitness(svcompWitness, cex.getLocation().getFileName());
				filenamesToDelete.add(filename);
			}

			if (ups.getBoolean(CorePreferenceInitializer.LABEL_WITNESS_VERIFY)) {
				if (svcompWitness == null) {
					reportWitnessResult(null, cex, false);
				} else {
					checkWitness(filename, cex, svcompWitness);
				}
			} else if (ups.getBoolean(CorePreferenceInitializer.LABEL_WITNESS_LOG)) {
				reportWitnessResult(svcompWitness, cex, false);
			}

			if (ups.getBoolean(CorePreferenceInitializer.LABEL_WITNESS_DELETE_GRAPHML)) {
				for (String fi : filenamesToDelete) {
					deleteFile(fi);
				}
			}
		}
	}

	private void deleteFile(String filename) {
		if (filename == null) {
			return;
		}
		File f = new File(filename);
		f.delete();
	}

	private String writeWitness(String svcompWitness, String origInputFile) {
		String filename;

		if (origInputFile == null) {
			filename = "witness.graphml";
		} else {
			filename = origInputFile + "-witness.graphml";
		}

		try {
			File f = CoreUtil.writeFile(filename, svcompWitness);
			if (f != null) {
				mLogger.info("Wrote witness to " + f.getAbsolutePath());
			} else {
				mLogger.warn("Something wrent wrong trying to write to " + filename);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return filename;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void reportWitnessResult(String svcompWitness, CounterExampleResult cex, boolean isVerified) {
		mServices.getResultService().reportResult(cex.getPlugin(), new WitnessResult<>(cex, svcompWitness, isVerified));
	}

	@SuppressWarnings("rawtypes")
	private boolean checkWitness(String svcompWitnessFile, CounterExampleResult cex, String svcompWitness)
			throws IOException, InterruptedException {
		mLogger.info("Verifying witness for CEX: " + cex.getShortDescription());
		UltimatePreferenceStore ups = new UltimatePreferenceStore(Activator.s_PLUGIN_ID);
		CorePreferenceInitializer.WitnessVerifierType type = ups.getEnum(
				CorePreferenceInitializer.LABEL_WITNESS_VERIFIER, CorePreferenceInitializer.WitnessVerifierType.class);
		String command = ups.getString(CorePreferenceInitializer.LABEL_WITNESS_VERIFIER_COMMAND);

		switch (type) {
		case CPACHECKER:
			return checkWitnessWithCPAChecker(svcompWitnessFile, cex, command, svcompWitness);
		default:
			throw new UnsupportedOperationException("Witness verifier " + type + " unknown");
		}
	}

	@SuppressWarnings("rawtypes")
	private boolean checkWitnessWithCPAChecker(String svcompWitnessFile, CounterExampleResult cex, String command,
			String svcompWitness) throws IOException, InterruptedException {

		String originalFile = cex.getLocation().getFileName();
		UltimatePreferenceStore ups = new UltimatePreferenceStore(Activator.s_PLUGIN_ID);
		String cpaCheckerHome = System.getenv().get("CPACHECKER_HOME");
		if (cpaCheckerHome == null) {
			mLogger.error("CPACHECKER_HOME not set, cannot use CPACHECKER as witness verifier");
			reportWitnessResult(svcompWitness, cex, false);
			return false;
		}
		int timeoutInS = ups.getInt(CorePreferenceInitializer.LABEL_WITNESS_VERIFIER_TIMEOUT);

		String[] cmdArray = makeCPACheckerCommand(command, svcompWitnessFile,
				ups.getString(CorePreferenceInitializer.LABEL_WITNESS_CPACHECKER_PROPERTY), originalFile,
				cpaCheckerHome, timeoutInS);
		timeoutInS++;
		command = StringUtils.join(cmdArray, " ");
		mLogger.info(command);
		MonitoredProcess mp = MonitoredProcess.exec(cmdArray, cpaCheckerHome, null, mServices, mStorage);
		BufferedInputStream errorStream = new BufferedInputStream(mp.getErrorStream());
		BufferedInputStream outputStream = new BufferedInputStream(mp.getInputStream());

		String error = convertStreamToString(errorStream);
		String output = convertStreamToString(outputStream);

		// wait for timeoutInS seconds for the witness checker, then kill it
		// forcefully
		mLogger.info("Waiting for " + timeoutInS + "s for CPA Checker...");
		MonitoredProcessState mps = mp.impatientWaitUntilTime(timeoutInS * 1000);
		mLogger.info("Return code was " + mps.getReturnCode());

		// TODO: interpret error and output

		if (checkOutputForSuccess(output)) {
			mLogger.info("Witness for CEX was verified successfully");
			reportWitnessResult(svcompWitness, cex, true);
			return true;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Witness for CEX did not verify");
			if (mps.isKilled()) {
				sb.append(" due to timeout of ");
				sb.append(timeoutInS);
				sb.append("s");
			}
			sb.append("! CPAChecker said:");
			sb.append(CoreUtil.getPlatformLineSeparator());
			sb.append("STDERR:");
			sb.append(CoreUtil.getPlatformLineSeparator());
			sb.append(error);
			sb.append(CoreUtil.getPlatformLineSeparator());
			sb.append("STDOUT:");
			sb.append(CoreUtil.getPlatformLineSeparator());
			sb.append(output);
			mLogger.error(sb.toString());
			reportWitnessResult(svcompWitness, cex, false);
			return false;
		}
	}

	private boolean checkOutputForSuccess(String output) {
		for (String line : output.split(CoreUtil.getPlatformLineSeparator())) {
			if (line.startsWith("Verification result: FALSE.")) {
				return true;
			}
		}
		return false;
	}

	private String[] makeCPACheckerCommand(String command, String svcompWitnessFile, String cpaCheckerProp,
			String originalFile, String workingDir, int timeoutInS) {
		List<String> cmdArgs = new ArrayList<>();
		String[] args = command.split(" ");
		File f = new File(workingDir + File.separator + args[0]);
		cmdArgs.add(f.getAbsolutePath());
		for (int i = 1; i < args.length; ++i) {
			cmdArgs.add(args[i]);
		}
		cmdArgs.add("-timelimit");
		cmdArgs.add(String.valueOf(timeoutInS));
		cmdArgs.add("-spec");
		cmdArgs.add(escape(svcompWitnessFile));
		cmdArgs.add("-spec");
		cmdArgs.add(escape(cpaCheckerProp));
		cmdArgs.add(escape(originalFile));
		return cmdArgs.toArray(new String[0]);
	}

	private String escape(String str) {
		return str;
		// return "\"" + str + "\"";
	}

	// private static String convertStreamToString(InputStream is) {
	// Scanner s = null;
	// try {
	// s = new Scanner(is).useDelimiter("\\A");
	// return s.hasNext() ? s.next() : "";
	// } finally {
	// if (s != null) {
	// s.close();
	// }
	// }
	// }

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder out = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				out.append(line).append(CoreUtil.getPlatformLineSeparator());
			}
			reader.close();
		} catch (IOException e) {
		}
		return out.toString();
	}
}
