/*
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Test Library.
 * 
 * The ULTIMATE Test Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Test Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Test Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Test Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Test Library grant you additional permission 
 * to convey the resulting work.
 */
/**
 * 
 */
package de.uni_freiburg.informatik.ultimatetest.suites.traceabstraction;

import java.util.Collection;

import de.uni_freiburg.informatik.ultimatetest.DirectoryFileEndingsPair;
import de.uni_freiburg.informatik.ultimatetest.UltimateTestCase;

/**
 * @author heizmann@informatik.uni-freiburg.de
 * 
 */
public class InterpolationTest_Memsafety extends AbstractTraceAbstractionTestSuite {

	/** Limit the number of files per directory. */
	private static int m_FilesPerDirectoryLimit = Integer.MAX_VALUE;
//	private static int m_FilesPerDirectoryLimit = 1;
	
	private static final DirectoryFileEndingsPair[] m_DirectoryFileEndingsPairs_Deref = {
		/*** Category 1. Arrays ***/
		new DirectoryFileEndingsPair("examples/svcomp/array-memsafety/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
	};
	
	private static final DirectoryFileEndingsPair[] m_DirectoryFileEndingsPairs_DerefFreeMemtrack = {
		/*** Category 3. Heap Data Structures ***/
		new DirectoryFileEndingsPair("examples/svcomp/memsafety/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/list-ext-properties/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/memory-alloca/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-memsafety/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
	};

	
	
	private static final String[] m_CurrentBugs_Deref = {
		};
	
	private static final String[] m_CurrentBugs_DerefFreeMemtrack = {
//			"examples/programs/regression",
//			"examples/programs/quantifier/regression",
		};


	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTimeout() {
		return 60 * 1000;
	}

	private static final String[] m_Settings = {
		"automizer/interpolation/memsafety/DerefFreeMemtrack-32bit-Princess-TreeInterpolation-Integer.epf",
		"automizer/interpolation/memsafety/DerefFreeMemtrack-32bit-SMTInterpol-TreeInterpolation-Integer.epf",
		"automizer/interpolation/memsafety/DerefFreeMemtrack-32bit-Z3-NestedInterpolation-Integer.epf",
		"automizer/interpolation/memsafety/DerefFreeMemtrack-32bit-Z3-IcSpLv-Integer.epf",
		"automizer/interpolation/memsafety/DerefFreeMemtrack-32bit-Z3-IcSp-Integer.epf",
		"automizer/interpolation/memsafety/DerefFreeMemtrack-32bit-Z3-SpLv-Integer.epf",
		"automizer/interpolation/memsafety/DerefFreeMemtrack-32bit-Z3-Sp-Integer.epf",
		"automizer/interpolation/memsafety/DerefFreeMemtrack-32bit-Z3-IcWpLv-Integer.epf",
		"automizer/interpolation/memsafety/DerefFreeMemtrack-32bit-Z3-IcWp-Integer.epf",
		"automizer/interpolation/memsafety/DerefFreeMemtrack-32bit-Z3-WpLv-Integer.epf",
		"automizer/interpolation/memsafety/DerefFreeMemtrack-32bit-Z3-Wp-Integer.epf",
	};
//	"automizer/interpolation/DerefFreeMemtrack-32bit-SMTInterpol-FPandBP-Integer.epf",
//	"automizer/interpolation/DerefFreeMemtrack-32bit-Z3-FPandBP-Integer.epf",
	
	
	
	private static final String[] m_CToolchains = {
//		"AutomizerC.xml",
		"AutomizerCInline.xml",
	};

	
	
	

	@Override
	public Collection<UltimateTestCase> createTestCases() {
		for (String setting : m_Settings) {
			for (String toolchain : m_CToolchains) {
				addTestCase(toolchain, setting, m_DirectoryFileEndingsPairs_Deref);
				addTestCase(toolchain, setting, m_CurrentBugs_Deref, new String[] {".c", ".i"});
			}
		}
		for (String setting : m_Settings) {
			for (String toolchain : m_CToolchains) {
				addTestCase(toolchain, setting, m_DirectoryFileEndingsPairs_DerefFreeMemtrack);
				addTestCase(toolchain, setting, m_CurrentBugs_DerefFreeMemtrack, new String[] {".c", ".i"});
			}
		}
		return super.createTestCases();
	}

	
}
