/*
 * Copyright (C) 2012-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE TraceAbstraction plug-in.
 * 
 * The ULTIMATE TraceAbstraction plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE TraceAbstraction plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE TraceAbstraction plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE TraceAbstraction plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE TraceAbstraction plug-in grant you additional permission
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.logic.Util;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SMT;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.ModifiableGlobalVariableManager;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramNonOldVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.SimplificationTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.XnfConversionTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.Substitution;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.PredicateUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.TermVarsProc;

public class SmtManager {


	private final Boogie2SMT mBoogie2Smt;
	private final Script mScript;
	private final SimplificationTechnique mSimplificationTechnique;
	private final ManagedScript mManagedScript;
	private final ModifiableGlobalVariableManager mModifiableGlobals;


	private final IUltimateServiceProvider mServices;

	private final PredicateFactory mPredicateFactory;

	public SmtManager(final Script script, final Boogie2SMT boogie2smt, final ModifiableGlobalVariableManager modifiableGlobals,
			final IUltimateServiceProvider services, final boolean interpolationModeSwitchNeeded, final ManagedScript managedScript,
			final SimplificationTechnique simplificationTechnique, final XnfConversionTechnique xnfConversionTechnique) {
		mServices = services;
		mSimplificationTechnique = simplificationTechnique;
		mBoogie2Smt = boogie2smt;
		mScript = script;
		mManagedScript = managedScript;
		mModifiableGlobals = modifiableGlobals;
		mPredicateFactory = new PredicateFactory(services, boogie2smt.getManagedScript(), boogie2smt.getBoogie2SmtSymbolTable(), simplificationTechnique, xnfConversionTechnique);
	}


	public PredicateFactory getPredicateFactory() {
		return mPredicateFactory;
	}



	public Boogie2SMT getBoogie2Smt() {
		return mBoogie2Smt;
	}

	public Script getScript() {
		return mScript;
	}

	



	/**
	 * Returns a predicate which states that old(g)=g for all global variables g
	 * that are modifiable by procedure proc according to
	 * ModifiableGlobalVariableManager modGlobVarManager.
	 */
	public static TermVarsProc getOldVarsEquality(final String proc, final ModifiableGlobalVariableManager modGlobVarManager, final Script script) {
		final Set<IProgramVar> vars = new HashSet<IProgramVar>();
		Term term = script.term("true");
		for (final IProgramVar bv : modGlobVarManager.getGlobalVarsAssignment(proc).getAssignedVars()) {
			vars.add(bv);
			final IProgramVar bvOld = ((IProgramNonOldVar) bv).getOldVar();
			vars.add(bvOld);
			final TermVariable tv = bv.getTermVariable();
			final TermVariable tvOld = bvOld.getTermVariable();
			final Term equality = script.term("=", tv, tvOld);
			term = Util.and(script, term, equality);
		}
		final String[] procs = new String[0];
		final TermVarsProc result = new TermVarsProc(term, vars, procs, PredicateUtils.computeClosedFormula(term, vars,
				script));
		return result;

	}



	/**
	 * Construct Predicate which represents the same Predicate as ps, but where
	 * all globalVars are renamed to oldGlobalVars.
	 */
	public IPredicate renameGlobalsToOldGlobals(final IPredicate ps) {
		if (getPredicateFactory().isDontCare(ps)) {
			throw new UnsupportedOperationException("don't cat not expected");
		}
		final Map<Term, Term> substitutionMapping = new HashMap<Term, Term>();
		for (final IProgramVar pv : ps.getVars()) {
			if (pv instanceof IProgramNonOldVar) {
				final IProgramVar oldVar = ((IProgramNonOldVar) pv).getOldVar();
				substitutionMapping.put(pv.getTermVariable(), oldVar.getTermVariable());
			}
		}
		Term renamedFormula = (new Substitution(getManagedScript(), substitutionMapping)).transform(ps.getFormula());
		renamedFormula = SmtUtils.simplify(getManagedScript(), renamedFormula, mServices, mSimplificationTechnique);
		final IPredicate result = getPredicateFactory().newPredicate(renamedFormula);
		return result;
	}




	public ManagedScript getManagedScript() {
		return mManagedScript;
	}

	public ModifiableGlobalVariableManager getModifiableGlobals() {
		return mModifiableGlobals;
	}
	
	


}
