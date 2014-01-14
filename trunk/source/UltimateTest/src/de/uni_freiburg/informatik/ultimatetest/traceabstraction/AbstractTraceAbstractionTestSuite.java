package de.uni_freiburg.informatik.ultimatetest.traceabstraction;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.uni_freiburg.informatik.ultimatetest.ITestSummary;
import de.uni_freiburg.informatik.ultimatetest.UltimateStarter;
import de.uni_freiburg.informatik.ultimatetest.UltimateTestCase;
import de.uni_freiburg.informatik.ultimatetest.UltimateTestSuite;
import de.uni_freiburg.informatik.ultimatetest.Util;
import de.uni_freiburg.informatik.ultimatetest.svcomp.SVCOMP14TestSummary;

public abstract class AbstractTraceAbstractionTestSuite extends UltimateTestSuite {

	@Override
	public Collection<UltimateTestCase> createTestCases() {
		List<UltimateTestCase> rtr = new ArrayList<UltimateTestCase>();

		// get a set of input files
		Collection<File> inputFiles = getInputFiles();

		File toolchainFile = new File(
				Util.getPathFromTrunk("examples/toolchains/TraceAbstraction.xml"));
		long deadline = 10; // in seconds
		// load preferences file, with following preferences:
		// Interpolation: BackwardPredicates, Timeout: 5 s 
		File backwardsPredicatesSettings = new File(Util.getPathFromTrunk("examples/settings/traceAbstractionTestSuite/backwardsPredicateJUnitTetstSettings"));
		String interpolationType = "TraceAbstraction via Backward Predicates (WP)";
		
		String description = interpolationType;
		String summaryLogFileName = Util.generateSummaryLogFilename(Util.getPathFromSurefire(".", this.getClass().getCanonicalName()), description);
		TraceAbstractionTestSummary summary = new TraceAbstractionTestSummary(this.getClass().getCanonicalName(),
				summaryLogFileName,
				interpolationType);
		Collection<ITestSummary> summaries = getSummaries();
		summaries.add(summary);
		
		for (File inputFile : inputFiles) {

			UltimateStarter starter = new UltimateStarter(inputFile, backwardsPredicatesSettings,
					toolchainFile, deadline, null, null);
			rtr.add(new UltimateTestCase(starter,
					new TraceAbstractionTestResultDecider(inputFile, summary), 
					inputFile.getAbsolutePath()));
		}

		return rtr;

	}
	
	protected void addTestCases(File toolchainFile, File settingsFile,
			Collection<File> inputFiles,
			String interpolationType,
			long deadline) {
		
	}
	
	public abstract Collection<File> getInputFiles();
	
	private Collection<UltimateTestCase> createTestCases(File toolchainFile, File settingsFile,
			Collection<File> inputFiles,
			String interpolationType,
			long deadline) {
				return null;
		
	}
}
