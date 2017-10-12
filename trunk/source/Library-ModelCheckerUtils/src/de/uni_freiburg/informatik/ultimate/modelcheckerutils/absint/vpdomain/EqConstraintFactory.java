/*
 * Copyright (C) 2017 Alexander Nutz (nutz@informatik.uni-freiburg.de)
 * Copyright (C) 2017 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.absint.vpdomain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalSort;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.util.datastructures.CongruenceClosureComparator;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.NestedMap2;

/**
 *
 * @author Alexander Nutz (nutz@informatik.uni-freiburg.de)
 *
 * @param <NODE>
 * @param <FUNCTION>
 */
public class EqConstraintFactory<NODE extends IEqNodeIdentifier<NODE>> {

	private final EqConstraint<NODE> mBottomConstraint;

	private final EqConstraint<NODE> mEmptyConstraint;

//	private EqStateFactory mEqStateFactory;

	private final AbstractNodeAndFunctionFactory<NODE, Term> mEqNodeAndFunctionFactory;

	private final IUltimateServiceProvider mServices;

//	private final CfgSmtToolkit mCsToolkit;

	private int mConstraintIdCounter = 1;

	private final NestedMap2<Sort, Integer, NODE> mDimensionToWeqVariableNode;

//	private final LiteralManager<NODE> mLiteralManager;

	private final CCManager<NODE> mCcManager;

	private final ManagedScript mMgdScript;

	public EqConstraintFactory(final AbstractNodeAndFunctionFactory<NODE, Term> eqNodeAndFunctionFactory,
			final IUltimateServiceProvider services, final ManagedScript mgdScript) {
		mBottomConstraint = new EqBottomConstraint<>(this);
		mBottomConstraint.freeze();

		mEmptyConstraint = new EqConstraint<>(mConstraintIdCounter++, this);
		mEmptyConstraint.freeze();

		mServices = services;
//		mCsToolkit = csToolkit;
		mMgdScript = mgdScript;
		mEqNodeAndFunctionFactory = eqNodeAndFunctionFactory;

		mDimensionToWeqVariableNode = new NestedMap2<>();

		mCcManager = new CCManager<>(new CongruenceClosureComparator<NODE>());
//		mLiteralManager = new LiteralManager<>();
	}

	public EqConstraint<NODE> getEmptyConstraint() {
		return mEmptyConstraint;
	}

	public EqConstraint<NODE> getBottomConstraint() {
		return mBottomConstraint;
	}

	public EqConstraint<NODE> unfreeze(final EqConstraint<NODE> constraint) {
		assert constraint.isFrozen();
		if (constraint.isBottom()) {
			return constraint;
		}
		return new EqConstraint<>(mConstraintIdCounter++, constraint);
	}

	public EqDisjunctiveConstraint<NODE>
			getDisjunctiveConstraint(final Collection<EqConstraint<NODE>> constraintList) {
		final Collection<EqConstraint<NODE>> bottomsFiltered = constraintList.stream()
				.filter(cons -> !(cons instanceof EqBottomConstraint)).collect(Collectors.toSet());
		return new EqDisjunctiveConstraint<NODE>(bottomsFiltered, this);
	}

	public EqConstraint<NODE> conjoinFlat(
			final EqConstraint<NODE> constraint1,
			final EqConstraint<NODE> constraint2) {
		return constraint1.meet(constraint2);
	}

	/**
	 * conjunction/intersection/join
	 *
	 * @param conjuncts
	 * @return
	 */
	public EqDisjunctiveConstraint<NODE> conjoinDisjunctiveConstraints(
			final List<EqDisjunctiveConstraint<NODE>> conjuncts) {
		final List<Set<EqConstraint<NODE>>> listOfConstraintSets = conjuncts.stream()
				.map(conjunct -> conjunct.getConstraints()).collect(Collectors.toList());

		final Set<List<EqConstraint<NODE>>> crossProduct =
				VPDomainHelpers.computeCrossProduct(listOfConstraintSets, mServices);

		if (crossProduct == null) {
			if (!mServices.getProgressMonitorService().continueProcessing()) {
				return getTopDisjunctiveConstraint();
			} else {
				throw new AssertionError("cross product should only return null if there is a timeout");
			}
		}

		// for each tuple in the crossproduct: construct the meet, and add it to the resulting constraintList
		final List<EqConstraint<NODE>> constraintList = crossProduct.stream()
			.map(tuple -> tuple.stream()
					.reduce((constraint1, constraint2) -> constraint1.meet(constraint2)).get())
			.collect(Collectors.toList());
		return getDisjunctiveConstraint(constraintList);
	}

	public EqConstraint<NODE> addWeakEquivalence(final NODE array1,
			final NODE array2, final NODE storeIndex,
			final EqConstraint<NODE> inputConstraint) {
		assert VPDomainHelpers.haveSameType(array1, array2);

		final EqConstraint<NODE> newConstraint = unfreeze(inputConstraint);
		newConstraint.reportWeakEquivalence(array1, array2, storeIndex);
		if (newConstraint.isInconsistent()) {
			return mBottomConstraint;
		}
		newConstraint.freeze();
		return newConstraint;
	}

	public EqDisjunctiveConstraint<NODE> disjoinDisjunctiveConstraints(
			final EqDisjunctiveConstraint<NODE> disjunct1,
			final EqDisjunctiveConstraint<NODE> disjunct2) {
		final Set<EqConstraint<NODE>> allConjunctiveConstraints = new HashSet<>();
		allConjunctiveConstraints.addAll(disjunct1.getConstraints());
		allConjunctiveConstraints.addAll(disjunct2.getConstraints());
		return getDisjunctiveConstraint(allConjunctiveConstraints);
	}

	/**
	 * disjunction/union/meet
	 *
	 * @param disjuncts
	 * @return
	 */
	public EqDisjunctiveConstraint<NODE> disjoinDisjunctiveConstraints(
			final List<EqDisjunctiveConstraint<NODE>> disjuncts) {

		final Set<EqConstraint<NODE>> allConjunctiveConstraints = new HashSet<>();
		for (final EqDisjunctiveConstraint<NODE> disjunct : disjuncts) {
			allConjunctiveConstraints.addAll(disjunct.getConstraints());
		}

		return getDisjunctiveConstraint(allConjunctiveConstraints);
	}

	/**
	 * Disjoin two (conjunctive) EqConstraints without creating an EqDisjunctiveConstraint -- this operation may loose
	 * information.
	 *
	 * Essentially, we only keep constraints that are present in both input constraints.
	 *
	 * @param constraint
	 * @param constraint2
	 * @return
	 */
	public EqConstraint<NODE> disjoinFlat(
			final EqConstraint<NODE> constraint1,
			final EqConstraint<NODE> constraint2) {
		final List<EqConstraint<NODE>> disjuncts = new ArrayList<>();
		disjuncts.add(constraint1);
		disjuncts.add(constraint2);
		return getDisjunctiveConstraint(disjuncts).flatten();
	}

	public EqConstraint<NODE> addEqualityFlat(final NODE node1, final NODE node2,
			final EqConstraint<NODE> originalState) {

//		factory.getBenchmark().unpause(VPSFO.addEqualityClock);
//		if (factory.isDebugMode()) {
//			factory.getLogger().debug("VPFactoryHelpers: addEquality(" + node1 + ", " + node2 + ", " + "..." + ")");
//		}
		if (originalState.isBottom()) {
//			factory.getBenchmark().stop(VPSFO.addEqualityClock);
			return originalState;
		}

		if (node1 == node2 || node1.equals(node2)) {
//			factory.getBenchmark().stop(VPSFO.addEqualityClock);
			return originalState;
		}

		if (originalState.areEqual(node1, node2)) {
			// the given identifiers are already equal in the originalState
			return originalState;
		}

		if (originalState.areUnequal(node1, node2)) {
			return getBottomConstraint();
		}

		final EqConstraint<NODE> unfrozen = unfreeze(originalState);
		unfrozen.reportEquality(node1, node2);
		if (unfrozen.isInconsistent()) {
			return mBottomConstraint;
		}
		unfrozen.freeze();
		return unfrozen;
	}


	public EqConstraint<NODE> addDisequalityFlat(final NODE node1, final NODE node2,
			final EqConstraint<NODE> originalState) {
//		if (factory.isDebugMode()) {
//			factory.getLogger().debug("VPFactoryHelpers: addDisEquality(..)");
//		}
		if (originalState.isBottom()) {
			return originalState;
		}

		if (originalState.areUnequal(node1, node2)) {
			return originalState;
		}

		/*
		 * check if the disequality introduces a contradiction, return bottom in that case
		 */
		if (originalState.areEqual(node1, node2)) {
			return getBottomConstraint();
		}

		final EqConstraint<NODE> unfrozen = unfreeze(originalState);
		unfrozen.reportDisequality(node1, node2);
		if (unfrozen.isInconsistent()) {
			return mBottomConstraint;
		}
		unfrozen.freeze();
		return unfrozen;
	}


	public EqDisjunctiveConstraint<NODE> addNode(final NODE nodeToAdd,
			final EqDisjunctiveConstraint<NODE> constraint) {

		final Set<EqConstraint<NODE>> newConstraints = new HashSet<>();

		for (final EqConstraint<NODE> cons : constraint.getConstraints()) {
			newConstraints.add(addNodeFlat(nodeToAdd, cons));
		}

		return getDisjunctiveConstraint(newConstraints);
	}

	public EqConstraint<NODE> addNodeFlat(final NODE nodeToAdd,
			final EqConstraint<NODE> constraint) {
		if (constraint.getAllNodes().contains(nodeToAdd)) {
			return constraint;
		}

		final EqConstraint<NODE> unf = unfreeze(constraint);
		unf.addNode(nodeToAdd);
		unf.freeze();

		return unf;
	}

	/**
	 *
	 * @param varsToProjectAway
	 * @return
	 */
	public EqConstraint<NODE> projectExistentially(final Collection<TermVariable> varsToProjectAway,
			final EqConstraint<NODE> original) {
		assert original.isFrozen();
		assert original.sanityCheck();
		if (original.isBottom()) {
			return original;
		}
		final EqConstraint<NODE> unfrozen = unfreeze(original);

		final Collection<TermVariable> allTvs = original.getAllTermVariables();

		for (final TermVariable var : varsToProjectAway) {
			if (!allTvs.contains(var)) {
				// nothing to do
				continue;
			}
			// havoccing an element
			final NODE nodeToHavoc = getEqNodeAndFunctionFactory().getExistingNode(var);
			unfrozen.removeElement(nodeToHavoc);
			assert unfrozen.sanityCheck();
		}

		unfrozen.freeze();
		assert VPDomainHelpers.constraintFreeOfVars(varsToProjectAway, unfrozen, getMgdScript().getScript()) :
					"resulting constraint still has at least one of the to-be-projected vars";

		return unfrozen;
	}

//	public EqStateFactory getEqStateFactory() {
//		return mEqStateFactory;
//	}
//
//	public void setEqStateFactory(final EqStateFactory eqStateFactory) {
//		mEqStateFactory = eqStateFactory;
//	}

	public AbstractNodeAndFunctionFactory<NODE, Term> getEqNodeAndFunctionFactory() {
		return mEqNodeAndFunctionFactory;
	}

	public NODE getWeqVariableNodeForDimension(final int dimensionNumber, final Sort sort) {
		NODE result = mDimensionToWeqVariableNode.get(sort, dimensionNumber);
		if (result == null) {
			final TermVariable tv = getMgdScript().constructFreshTermVariable("weq" + dimensionNumber, sort);
			result = getEqNodeAndFunctionFactory().getOrConstructNode(tv);
			mDimensionToWeqVariableNode.put(sort, dimensionNumber, result);
		}
		return result;
	}

	public TermVariable getWeqVariableForDimension(final int dimensionNumber, final Sort sort) {
		return (TermVariable) getWeqVariableNodeForDimension(dimensionNumber, sort).getTerm();
	}


	public Set<TermVariable> getAllWeqVariables() {
		final Set<TermVariable> result = new HashSet<>();
		mDimensionToWeqVariableNode.entrySet().forEach(en -> result.add((TermVariable) en.getThird().getTerm()));
		return result;
	}

	public Set<NODE> getAllWeqNodes() {
		final Set<NODE> result = new HashSet<>();
		mDimensionToWeqVariableNode.entrySet().forEach(en -> result.add(en.getThird()));
		return result;
	}

	public EqDisjunctiveConstraint<NODE> getTopDisjunctiveConstraint() {
		return getDisjunctiveConstraint(Collections.singleton(getEmptyConstraint()));
	}

	public EqConstraint<NODE> getEqConstraint(
			final WeqCongruenceClosure<NODE> newPartialArrangement) {
		if (newPartialArrangement.isInconsistent()) {
			return getBottomConstraint();
		}
		return new EqConstraint<>(mConstraintIdCounter++, newPartialArrangement, this);
	}

	public ManagedScript getMgdScript() {
		return mMgdScript;
//		return mCsToolkit.getManagedScript();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

//	public LiteralManager<NODE> getLiteralManager() {
//		return mLiteralManager;
//	}

	public CCManager<NODE> getCcManager() {
		return mCcManager;
	}

	public List<NODE> getAllWeqVarsNodeForFunction(final NODE func) {
		if (!func.getSort().isArraySort()) {
			return Collections.emptyList();
		}
		final MultiDimensionalSort mdSort = new MultiDimensionalSort(func.getSort());
		final List<Sort> indexSorts = mdSort.getIndexSorts();
		final List<NODE> result = new ArrayList<>(mdSort.getDimension());
		for (int i = 0; i < mdSort.getDimension(); i++) {
			result.add(this.getWeqVariableNodeForDimension(i, indexSorts.get(i)));
		}
		return result;
	}

//	public  IIcfgSymbolTable getSymbolTable() {
//		return mCsToolkit.getSymbolTable();
//	}
}