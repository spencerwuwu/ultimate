/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE AbstractInterpretationV2 plug-in.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE AbstractInterpretationV2 plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE AbstractInterpretationV2 plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE AbstractInterpretationV2 plug-in grant you additional permission 
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.rcfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;

import de.uni_freiburg.informatik.ultimate.boogie.DeclarationInformation;
import de.uni_freiburg.informatik.ultimate.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.boogie.DeclarationInformation.StorageClass;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Declaration;
import de.uni_freiburg.informatik.ultimate.boogie.symboltable.BoogieSymbolTable;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.IVariableProvider;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.preferences.AbsIntPrefInitializer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class RcfgVariableProvider<STATE extends IAbstractState<STATE, CodeBlock, IBoogieVar>, LOCATION>
		implements IVariableProvider<STATE, CodeBlock, IBoogieVar, LOCATION> {

	private static final StorageClass[] LOCAL_STORAGE_CLASSES = new StorageClass[] { StorageClass.LOCAL,
			StorageClass.IMPLEMENTATION_INPARAM, StorageClass.IMPLEMENTATION_OUTPARAM };
	private final BoogieSymbolTable mSymbolTable;
	private final Boogie2SmtSymbolTable mBoogieVarTable;
	private final ILogger mLogger;

	public RcfgVariableProvider(final BoogieSymbolTable table, final Boogie2SmtSymbolTable boogieVarTable,
			final IUltimateServiceProvider services) {
		assert table != null;
		assert boogieVarTable != null;
		assert services != null;
		mSymbolTable = table;
		mBoogieVarTable = boogieVarTable;
		mLogger = services.getLoggingService().getLogger(Activator.PLUGIN_ID);
	}

	@Override
	public STATE defineVariablesBefore(final CodeBlock current, final STATE state) {
		assert current != null;
		assert state != null;
		assert state.isEmpty();

		final Map<String, IBoogieVar> vars = new TreeMap<String, IBoogieVar>();

		vars.putAll(mBoogieVarTable.getGlobals());
		vars.putAll(mBoogieVarTable.getConsts());

		// add locals if applicable, thereby overriding globals
		final String procedure = current.getPreceedingProcedure();
		if (procedure != null) {
			vars.putAll(getLocalVariables(procedure));
		}
		if (vars.isEmpty()) {
			return state;
		}
		return state.addVariables(vars);
	}

	@Override
	public STATE defineVariablesAfter(final CodeBlock action, final STATE localPreState, final STATE hierachicalPreState) {
		assert action != null;
		assert localPreState != null;

		// we assume that state has all variables except the ones that would be
		// introduced or removed by this edge
		// so, only call or return can do this

		if (action instanceof Call) {
			// if we call we just need to update all local variables, i.e., remove all the ones from the current scope
			// and add all the ones from the new scope (thus also automatically masking globals)
			final String remove = action.getPreceedingProcedure();
			final String add = action.getSucceedingProcedure();
			STATE rtr = localPreState;
			// remove current locals
			rtr = removeLocals(rtr, remove);
			// remove globals that will be masked by the new scope
			final Map<String, IBoogieVar> masked = getMaskedGlobalsVariables(add);
			if (!masked.isEmpty()) {
				rtr = rtr.removeVariables(masked);
			}
			// add locals of new scope
			rtr = applyLocals(rtr, add, rtr::addVariables);
			return rtr;
		} else if (action instanceof Return) {
			// if the action is a return, we have to:
			// - remove all currently local variables
			// - keep all unmasked globals
			// - add old locals from the scope we are returning to
			// - add globals that were masked by this scope from the scope we are returning to

			final String source = action.getPreceedingProcedure();
			final String target = action.getSucceedingProcedure();
			final Map<String, IBoogieVar> varsNeededFromOldScope = new HashMap<String, IBoogieVar>();

			if (source != null) {
				// we need masked globals from the old scope, so we have to determine which globals are masked
				varsNeededFromOldScope.putAll(getMaskedGlobalsVariables(source));
			}
			if (target != null) {
				// we also need oldlocals from the old scope; if the old scope also masks global variables, this will
				// mask them again
				varsNeededFromOldScope.putAll(getLocalVariables(target));
			}

			STATE rtr = localPreState;
			// in any case, we have to remove all local variables from the state
			rtr = removeLocals(rtr, source);

			if (varsNeededFromOldScope.isEmpty()) {
				// we do not need information from the old scope, so we are finished
				if (mLogger.isDebugEnabled()) {
					mLogger.debug(new StringBuilder().append(AbsIntPrefInitializer.INDENT)
							.append(" No vars needed from old scope"));
				}
				return rtr;
			}

			// the program state that has to be used to obtain the values of the old scope
			// (old locals, unmasked globals) is the pre state of the call
			STATE preCallState = hierachicalPreState;
			assert preCallState != null : "There is no abstract state before the call that corresponds to this return";
			// we determine which variables are not needed ...
			final Map<String, IBoogieVar> toberemoved = new TreeMap<String, IBoogieVar>();
			for (final Entry<String, IBoogieVar> entry : preCallState.getVariables().entrySet()) {
				if (!varsNeededFromOldScope.containsKey(entry.getKey())) {
					toberemoved.put(entry.getKey(), entry.getValue());
				}
			}

			if (!toberemoved.isEmpty()) {
				// ... and remove them if there are any
				if (mLogger.isDebugEnabled()) {
					mLogger.debug(getLogMessageRemoveLocalsPreCall(preCallState, toberemoved));
				}
				preCallState = preCallState.removeVariables(toberemoved);
			} else if (mLogger.isDebugEnabled()) {
				if (mLogger.isDebugEnabled()) {
					mLogger.debug(getLogMessageNoRemoveLocalsPreCall(preCallState));
				}
			}
			// now we combine the state after returning from this method with the one from before we entered the method.
			rtr = rtr.patch(preCallState);
			return rtr;

		} else {
			// nothing changes
			return localPreState;
		}
	}

	private STATE removeLocals(final STATE state, final String procedure) {
		return applyLocals(state, procedure, state::removeVariables);
	}

	private STATE applyLocals(final STATE state, final String procedure,
			final Function<Map<String, IBoogieVar>, STATE> fun) {
		if (procedure == null) {
			return state;
		}

		final Map<String, IBoogieVar> locals = getLocalVariables(procedure);
		if (locals.isEmpty()) {
			return state;
		}

		return fun.apply(locals);
	}

	/**
	 * Get all global variables that are masked by the specified procedure.
	 * 
	 * @param procedure
	 * @return
	 */
	private Map<String, IBoogieVar> getMaskedGlobalsVariables(final String procedure) {
		assert procedure != null;
		final Map<String, IBoogieVar> globals = new HashMap<String, IBoogieVar>();
		globals.putAll(mBoogieVarTable.getGlobals());
		globals.putAll(mBoogieVarTable.getConsts());

		final Map<String, IBoogieVar> locals = new HashMap<String, IBoogieVar>();
		locals.putAll(getLocalVariables(procedure));

		final Map<String, IBoogieVar> rtr = new TreeMap<String, IBoogieVar>();

		for (final Entry<String, IBoogieVar> local : locals.entrySet()) {
			if (globals.containsKey(local.getKey())) {
				rtr.put(local.getKey(), local.getValue());
			}
		}

		return rtr;
	}

	private Map<String, IBoogieVar> getLocalVariables(final String procedure) {
		assert procedure != null;
		final Map<String, IBoogieVar> localVars = new TreeMap<>();
		final Map<String, Declaration> locals = mSymbolTable.getLocalVariables(procedure);
		for (final Entry<String, Declaration> local : locals.entrySet()) {
			final IBoogieVar bvar = getLocalVariable(local.getKey(), procedure);
			if (bvar == null) {
				continue;
			}
			localVars.put(local.getKey(), bvar);
		}
		return localVars;
	}

	private IBoogieVar getLocalVariable(final String key, final String procedure) {
		for (final StorageClass storageClass : LOCAL_STORAGE_CLASSES) {
			final IBoogieVar var = getLocalVariable(key, procedure, storageClass);
			if (var != null) {
				return var;
			}
		}
		return null;
	}

	private IBoogieVar getLocalVariable(String key, String procedure, StorageClass sclass) {
		return mBoogieVarTable.getBoogieVar(key, new DeclarationInformation(sclass, procedure), false);
	}

	private StringBuilder getLogMessageRemoveLocalsPreCall(STATE state, final Map<String, IBoogieVar> toberemoved) {
		return new StringBuilder().append(AbsIntPrefInitializer.INDENT).append(" removing vars from pre-call state [")
				.append(state.hashCode()).append("] ").append(state.toLogString()).append(": ").append(toberemoved);
	}

	private StringBuilder getLogMessageNoRemoveLocalsPreCall(STATE state) {
		return new StringBuilder().append(AbsIntPrefInitializer.INDENT).append(" using unchanged pre-call state [")
				.append(state.hashCode()).append("] ").append(state.toLogString());
	}
}
