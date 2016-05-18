/*
 * Copyright (C) 2014-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE ModelCheckerUtils Library.
 * 
 * The ULTIMATE ModelCheckerUtils Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE ModelCheckerUtils Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE ModelCheckerUtils Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE ModelCheckerUtils Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE ModelCheckerUtils Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.partialQuantifierElimination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.ApplicationTermFinder;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalSelect;
import de.uni_freiburg.informatik.ultimate.util.relation.HashRelation;

public class XnfUsr extends XjunctPartialQuantifierElimination {
	
	private final Set<TermVariable> affectedEliminatees = new HashSet<>();

	public XnfUsr(Script script, IUltimateServiceProvider services) {
		super(script, services);
	}

	@Override
	public String getName() {
		return "unimportant select removal";
	}

	@Override
	public String getAcronym() {
		return "USR";
	}
	
	@Override
	public boolean resultIsXjunction() {
		return true;
	};


	@Override
	public Term[] tryToEliminate(int quantifier, Term[] inputAtoms,
			Set<TermVariable> eliminatees) {
		HashRelation<TermVariable, Term> var2arrays = new HashRelation<TermVariable, Term>();
		HashRelation<TermVariable, Term> var2parameters = new HashRelation<TermVariable, Term>();
		Set<TermVariable> blacklist = new HashSet<TermVariable>();
		for (Term param : inputAtoms) {
			Set<ApplicationTerm> storeTerms = (new ApplicationTermFinder("store", true)).findMatchingSubterms(param);
			if (storeTerms.isEmpty()) {
				List<MultiDimensionalSelect> slects = MultiDimensionalSelect.extractSelectDeep(param, false);
				for (MultiDimensionalSelect mds : slects) {
					Set<TermVariable> indexFreeVars = mds.getIndex().getFreeVars();
					for (TermVariable tv : indexFreeVars) {
						if (eliminatees.contains(tv)) {
							var2arrays.addPair(tv, mds.getArray());
							var2parameters.addPair(tv, param);
						}
					}
				}
			} else {
				//if there are store terms all occurring variables become
				//blacklisted
				blacklist.addAll(Arrays.asList(param.getFreeVars()));
			}
			
		}
		Set<Term> superfluousParams = new HashSet<Term>();
		for (TermVariable eliminatee : var2arrays.getDomain()) {
			if (!blacklist.contains(eliminatee)) {
				if (var2arrays.getImage(eliminatee).size() == 1 &&
						var2parameters.getImage(eliminatee).size() == 1) {
					superfluousParams.addAll(var2parameters.getImage(eliminatee));
					affectedEliminatees.add(eliminatee);
				}
			}
		}
		ArrayList<Term> resultAtoms = new ArrayList<Term>();
		for (Term oldParam : inputAtoms) {
			if (!superfluousParams.contains(oldParam)) {
				resultAtoms.add(oldParam);
			}
		}
		return resultAtoms.toArray(new Term[resultAtoms.size()]);
	}

	public Set<TermVariable> getAffectedEliminatees() {
		return affectedEliminatees;
	}
	
	

}
