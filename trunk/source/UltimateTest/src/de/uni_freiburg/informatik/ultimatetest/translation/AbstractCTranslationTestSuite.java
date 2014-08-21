package de.uni_freiburg.informatik.ultimatetest.translation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import de.uni_freiburg.informatik.ultimatetest.UltimateRunDefinition;
import de.uni_freiburg.informatik.ultimatetest.UltimateStarter;
import de.uni_freiburg.informatik.ultimatetest.UltimateTestCase;
import de.uni_freiburg.informatik.ultimatetest.UltimateTestSuite;
import de.uni_freiburg.informatik.ultimatetest.decider.TranslationTestResultDecider;
import de.uni_freiburg.informatik.ultimatetest.summary.ITestSummary;
import de.uni_freiburg.informatik.ultimatetest.util.Util;

public abstract class AbstractCTranslationTestSuite extends UltimateTestSuite {

	@Override
	public Collection<UltimateTestCase> createTestCases() {
		ArrayList<UltimateTestCase> rtr = new ArrayList<UltimateTestCase>();

		// get a set of input files

		Collection<File> inputFiles = getInputFiles();
		ITestSummary summary = getSummary();
		getSummaries().add(summary);

		File toolchainFile = new File(Util.getPathFromTrunk("examples/toolchains/CTranslationTest.xml"));
		long deadline = 10000;

		for (File inputFile : inputFiles) {
			UltimateRunDefinition urd = new UltimateRunDefinition(inputFile, null, toolchainFile);
			UltimateStarter starter = new UltimateStarter(urd, deadline, null, null);
			rtr.add(new UltimateTestCase(
					starter, 
					new TranslationTestResultDecider(inputFile.getAbsolutePath()), 
					summary,
					inputFile.getAbsolutePath(), 
					urd));
		}

		return rtr;
	}

	public abstract Collection<File> getInputFiles();
	
	public abstract ITestSummary getSummary();

}
