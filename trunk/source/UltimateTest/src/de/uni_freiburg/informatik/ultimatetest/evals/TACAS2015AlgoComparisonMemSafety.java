package de.uni_freiburg.informatik.ultimatetest.evals;

import java.util.List;

import de.uni_freiburg.informatik.ultimatetest.UltimateTestCase;

public class TACAS2015AlgoComparisonMemSafety extends TACAS2015 {

	@Override
	protected void createTestCasesForReal(List<UltimateTestCase> testcases) {
		addTestCasesFixed("AutomizerCWithBlockEncoding.xml", "TACASInterpolation2015/Automizer/Z3_Interpolation-mem.epf", testcases);
		addTestCasesFixed("AutomizerCWithBlockEncoding.xml", "TACASInterpolation2015/Automizer/Princess_Interpolation-mem.epf", testcases);
		addTestCasesFixed("AutomizerCWithBlockEncoding.xml", "TACASInterpolation2015/Automizer/SMTInterpol_Interpolation-mem.epf", testcases);
		addTestCasesFixed("AutomizerCWithBlockEncoding.xml", "TACASInterpolation2015/Automizer/SMTInterpol_SP-IC-LV-mem.epf", testcases);
		addTestCasesFixed("AutomizerCWithBlockEncoding.xml", "TACASInterpolation2015/Automizer/Z3_SP-IC-LV-mem.epf", testcases);
		addTestCasesFixed("AutomizerCWithBlockEncoding.xml", "TACASInterpolation2015/Automizer/CVC4_SP-IC-LV-mem.epf", testcases);
		addTestCasesFixed("AutomizerCWithBlockEncoding.xml", "TACASInterpolation2015/Automizer/SMTInterpol_SP-IC-mem.epf", testcases);
		addTestCasesFixed("AutomizerCWithBlockEncoding.xml", "TACASInterpolation2015/Automizer/Z3_SP-IC-mem.epf", testcases);
		addTestCasesFixed("AutomizerCWithBlockEncoding.xml", "TACASInterpolation2015/Automizer/CVC4_SP-IC-mem.epf", testcases);
		addTestCasesFixed("AutomizerCWithBlockEncoding.xml", "TACASInterpolation2015/Automizer/Z3_SP-LV-mem.epf", testcases);
		addTestCasesFixed("AutomizerCWithBlockEncoding.xml", "TACASInterpolation2015/Automizer/Z3_SP-mem.epf", testcases);

//		addTestCasesFixed("CodeCheckNoBE-C.xml", "TACASInterpolation2015/Kojak-SP-mem.epf", testcases);
//		addTestCasesFixed("CodeCheckNoBE-C.xml", "TACASInterpolation2015/Kojak-SP-IC-mem.epf", testcases);
//		addTestCasesFixed("CodeCheckNoBE-C.xml", "TACASInterpolation2015/Kojak-SP-LV-mem.epf", testcases);
//		addTestCasesFixed("CodeCheckNoBE-C.xml", "TACASInterpolation2015/Kojak-SP-IC-LV-mem.epf", testcases);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long getTimeout() {
		return 60 * 1000;
	}

	@Override
	protected String[] getDirectories() {
		// @formatter:off
		String[] directories = {
			"examples/svcomp/memsafety/",
			"examples/svcomp/memsafety-ext/",
			"examples/svcomp/list-ext-properties/",
			"examples/svcomp/memory-alloca/",
			"examples/svcomp/memory-unsafe/",
		};
		return directories;
		// @formatter:on
	}
	
	@Override
	protected String[] getFileEndings() {
		return new String[] { ".i" };
	}
	
	@Override
	protected int getFilesPerCategory() {
		return -1;
	}
}
