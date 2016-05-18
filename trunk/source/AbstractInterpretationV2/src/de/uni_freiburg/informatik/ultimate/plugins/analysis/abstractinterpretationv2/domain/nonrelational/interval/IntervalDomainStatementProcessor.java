/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.interval;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.boogie.BoogieVisitor;
import de.uni_freiburg.informatik.ultimate.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayAccessExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayStoreExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BinaryExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BooleanLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Declaration;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.FunctionApplication;
import de.uni_freiburg.informatik.ultimate.boogie.ast.FunctionDeclaration;
import de.uni_freiburg.informatik.ultimate.boogie.ast.HavocStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IfThenElseExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IntegerLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.LeftHandSide;
import de.uni_freiburg.informatik.ultimate.boogie.ast.RealLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.UnaryExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BinaryExpression.Operator;
import de.uni_freiburg.informatik.ultimate.boogie.output.BoogiePrettyPrinter;
import de.uni_freiburg.informatik.ultimate.boogie.symboltable.BoogieSymbolTable;
import de.uni_freiburg.informatik.ultimate.boogie.type.ArrayType;
import de.uni_freiburg.informatik.ultimate.boogie.type.PrimitiveType;
import de.uni_freiburg.informatik.ultimate.core.model.models.IType;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.BooleanValue;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.BooleanValue.Value;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.EvaluatorUtils;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.ExpressionEvaluator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.IEvaluationResult;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.IEvaluator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.IEvaluatorFactory;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.INAryEvaluator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.preferences.AbsIntPrefInitializer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;

/**
 * Processes Boogie {@link Statement}s and returns a new {@link IntervalDomainState} for the given statement.
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
public class IntervalDomainStatementProcessor extends BoogieVisitor {

	private final BoogieSymbolTable mSymbolTable;

	private IntervalDomainState mOldState;
	private List<IntervalDomainState> mReturnState;

	private IEvaluatorFactory<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar> mEvaluatorFactory;
	private ExpressionEvaluator<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar> mExpressionEvaluator;

	private String mLhsVariable;

	private final ILogger mLogger;

	protected IntervalDomainStatementProcessor(final ILogger logger, final BoogieSymbolTable symbolTable) {
		mSymbolTable = symbolTable;
		mLogger = logger;
		mLhsVariable = null;
	}

	public List<IntervalDomainState> process(final IntervalDomainState oldState, final Statement statement) {

		mReturnState = new ArrayList<>();

		assert oldState != null;
		assert statement != null;

		mOldState = oldState;

		mLhsVariable = null;

		processStatement(statement);

		assert (oldState.getVariables().isEmpty()) || (!mReturnState.isEmpty());

		return mReturnState;
	}

	@Override
	protected Statement processStatement(final Statement statement) {
		if (statement instanceof AssignmentStatement) {
			handleAssignment((AssignmentStatement) statement);
			return statement;
		} else if (statement instanceof AssumeStatement) {
			handleAssumeStatement((AssumeStatement) statement);
			return statement;
		} else if (statement instanceof HavocStatement) {
			handleHavocStatement((HavocStatement) statement);
			return statement;
		}

		return super.processStatement(statement);
	}

	@Override
	protected Expression processExpression(final Expression expr) {

		assert mEvaluatorFactory != null;

		Expression newExpr = null;

		if (expr instanceof BinaryExpression) {
			newExpr = handleBinaryExpression((BinaryExpression) expr);
		} else if (expr instanceof UnaryExpression) {
			newExpr = handleUnaryExpression((UnaryExpression) expr);
		} else if (expr instanceof ArrayStoreExpression) {
			mExpressionEvaluator.addEvaluator(new IntervalSingletonValueExpressionEvaluator(new IntervalDomainValue()));
			return expr;
		} else if (expr instanceof ArrayAccessExpression) {
			mExpressionEvaluator.addEvaluator(new IntervalSingletonValueExpressionEvaluator(new IntervalDomainValue()));
			return expr;
		}

		if (newExpr == null || expr == newExpr) {
			return super.processExpression(expr);
		} else {
			if (mLogger.isDebugEnabled()) {
				mLogger.debug(new StringBuilder().append(AbsIntPrefInitializer.INDENT).append(" Expression ")
				        .append(BoogiePrettyPrinter.print(expr)).append(" rewritten to: ")
				        .append(BoogiePrettyPrinter.print(newExpr)).toString());
			}
			return processExpression(newExpr);
		}
	}

	private void handleAssignment(final AssignmentStatement statement) {
		mEvaluatorFactory = new IntervalEvaluatorFactory(mLogger);

		final LeftHandSide[] lhs = statement.getLhs();
		final Expression[] rhs = statement.getRhs();

		List<IntervalDomainState> currentStateList = new ArrayList<>();
		currentStateList.add(mOldState);

		for (int i = 0; i < lhs.length; i++) {
			assert mLhsVariable == null;
			processLeftHandSide(lhs[i]);
			assert mLhsVariable != null;
			final String varname = mLhsVariable;
			mLhsVariable = null;

			mExpressionEvaluator = new ExpressionEvaluator<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar>();

			processExpression(rhs[i]);

			assert mExpressionEvaluator.isFinished() : "Expression evaluator is not finished";
			assert mExpressionEvaluator.getRootEvaluator() != null;

			final List<IntervalDomainState> newStates = new ArrayList<>();

			for (final IntervalDomainState currentState : currentStateList) {
				final List<IEvaluationResult<IntervalDomainValue>> result = mExpressionEvaluator.getRootEvaluator()
				        .evaluate(currentState);

				if (result.isEmpty()) {
					throw new UnsupportedOperationException(
					        "There is supposed to be at least on evaluation result for the assingment expression.");
				}

				for (final IEvaluationResult<IntervalDomainValue> res : result) {
					IntervalDomainState newState = currentState.copy();

					final IBoogieVar type = newState.getVariableDeclarationType(varname);
					if (type.getIType() instanceof PrimitiveType) {
						final PrimitiveType primitiveType = (PrimitiveType) type.getIType();

						if (primitiveType.getTypeCode() == PrimitiveType.BOOL) {
							newState = currentState.setBooleanValue(varname, res.getBooleanValue());
						} else {
							newState = newState.setValue(varname, res.getValue());
						}
					} else if (type.getIType() instanceof ArrayType) {
						// TODO:
						// We treat Arrays as normal variables for the time being.
						newState = newState.setValue(varname, res.getValue());
					} else {
						newState = newState.setValue(varname, res.getValue());
					}

					newStates.add(newState);
				}
			}

			currentStateList = newStates;
		}

		mReturnState.addAll(currentStateList);
	}

	@Override
	protected void visit(final VariableLHS lhs) {
		mLhsVariable = lhs.getIdentifier();
	}

	@Override
	protected void visit(final IntegerLiteral expr) {
		assert mEvaluatorFactory != null;

		final IEvaluator<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar> evaluator = mEvaluatorFactory
		        .createSingletonValueExpressionEvaluator(expr.getValue(), BigDecimal.class);

		mExpressionEvaluator.addEvaluator(evaluator);
	}

	@Override
	protected void visit(final RealLiteral expr) {
		assert mEvaluatorFactory != null;

		final IEvaluator<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar> evaluator = mEvaluatorFactory
		        .createSingletonValueExpressionEvaluator(expr.getValue(), BigDecimal.class);

		mExpressionEvaluator.addEvaluator(evaluator);
	}

	private Expression handleBinaryExpression(final BinaryExpression expr) {
		if (expr.getOperator() == Operator.COMPNEQ) {
			if (expr.getType() instanceof PrimitiveType && expr.getLeft().getType() instanceof PrimitiveType
			        && expr.getRight().getType() instanceof PrimitiveType) {
				final PrimitiveType prim = (PrimitiveType) expr.getType();
				final PrimitiveType leftPrim = (PrimitiveType) expr.getLeft().getType();
				final PrimitiveType rightPrim = (PrimitiveType) expr.getRight().getType();
				if (prim.getTypeCode() == PrimitiveType.BOOL && leftPrim.getTypeCode() == PrimitiveType.BOOL
				        && rightPrim.getTypeCode() == PrimitiveType.BOOL) {
					final UnaryExpression negatedRight = new UnaryExpression(expr.getLocation(),
					        expr.getRight().getType(), UnaryExpression.Operator.LOGICNEG, expr.getRight());
					final BinaryExpression newExp = new BinaryExpression(expr.getLocation(), expr.getType(),
					        Operator.COMPEQ, expr.getLeft(), negatedRight);

					return newExp;
				}
			}

			final BinaryExpression negativeCase = new BinaryExpression(expr.getLocation(), expr.getType(),
			        Operator.COMPLT, expr.getLeft(), expr.getRight());
			final BinaryExpression positiveCase = new BinaryExpression(expr.getLocation(), expr.getType(),
			        Operator.COMPGT, expr.getLeft(), expr.getRight());

			final Expression newExp = new BinaryExpression(expr.getLocation(), expr.getType(), Operator.LOGICOR,
			        negativeCase, positiveCase);

			return newExp;
		} else if (expr.getOperator() == Operator.COMPGT || expr.getOperator() == Operator.COMPLT) {
			if (expr.getLeft().getType() instanceof PrimitiveType
			        && expr.getRight().getType() instanceof PrimitiveType) {
				final PrimitiveType primLeft = (PrimitiveType) expr.getLeft().getType();
				final PrimitiveType primRight = (PrimitiveType) expr.getRight().getType();

				if (primLeft.getTypeCode() == PrimitiveType.INT && primRight.getTypeCode() == PrimitiveType.INT) {
					BinaryExpression newExp;

					switch (expr.getOperator()) {
					case COMPGT:
						final BinaryExpression newRightGt = new BinaryExpression(expr.getRight().getLocation(),
						        expr.getRight().getType(), Operator.ARITHPLUS, expr.getRight(),
						        new IntegerLiteral(expr.getRight().getLocation(), "1"));

						newExp = new BinaryExpression(expr.getLocation(), expr.getType(), Operator.COMPGEQ,
						        expr.getLeft(), newRightGt);
						break;
					case COMPLT:
						final BinaryExpression newRightLt = new BinaryExpression(expr.getRight().getLocation(),
						        expr.getRight().getType(), Operator.ARITHMINUS, expr.getRight(),
						        new IntegerLiteral(expr.getRight().getLocation(), "1"));

						newExp = new BinaryExpression(expr.getLocation(), expr.getType(), Operator.COMPLEQ,
						        expr.getLeft(), newRightLt);
						break;
					default:
						throw new UnsupportedOperationException("Unexpected operator: " + expr.getOperator());
					}

					return newExp;
				}
			}
		} else if (expr.getOperator() == Operator.LOGICIMPLIES) {
			final UnaryExpression newLeft = new UnaryExpression(expr.getLocation(), expr.getLeft().getType(),
			        UnaryExpression.Operator.LOGICNEG, expr.getLeft());

			final BinaryExpression newExp = new BinaryExpression(expr.getLocation(), expr.getType(), Operator.LOGICOR,
			        newLeft, expr.getRight());
			return newExp;
		} else if (expr.getOperator() == Operator.LOGICIFF) {
			final BinaryExpression newTrueExpression = new BinaryExpression(expr.getLocation(), expr.getType(),
			        Operator.LOGICAND, expr.getLeft(), expr.getRight());

			final UnaryExpression negatedLeft = new UnaryExpression(expr.getLocation(), expr.getLeft().getType(),
			        UnaryExpression.Operator.LOGICNEG, expr.getLeft());
			final UnaryExpression negatedRight = new UnaryExpression(expr.getLocation(), expr.getRight().getType(),
			        UnaryExpression.Operator.LOGICNEG, expr.getRight());
			final BinaryExpression newFalseExpression = new BinaryExpression(expr.getLocation(), expr.getType(),
			        Operator.LOGICAND, negatedLeft, negatedRight);

			final BinaryExpression newExp = new BinaryExpression(expr.getLocation(), expr.getType(), Operator.LOGICOR,
			        newTrueExpression, newFalseExpression);
			return newExp;
		}

		return expr;
	}

	private Expression handleUnaryExpression(final UnaryExpression expr) {
		if (expr.getOperator() == UnaryExpression.Operator.LOGICNEG) {
			if (expr.getExpr() instanceof BinaryExpression) {
				final BinaryExpression binexp = (BinaryExpression) expr.getExpr();

				Operator newOp;

				Expression newLeft = binexp.getLeft();
				final IType leftType = binexp.getLeft().getType();
				Expression newRight = binexp.getRight();
				final IType rightType = binexp.getRight().getType();

				switch (binexp.getOperator()) {
				case COMPEQ:
					newOp = Operator.COMPNEQ;
					break;
				case COMPNEQ:
					newOp = Operator.COMPEQ;
					break;
				case COMPGEQ:
					newOp = Operator.COMPLT;
					break;
				case COMPGT:
					newOp = Operator.COMPLEQ;
					break;
				case COMPLEQ:
					newOp = Operator.COMPGT;
					break;
				case COMPLT:
					newOp = Operator.COMPGEQ;
					break;
				case LOGICAND:
					newOp = Operator.LOGICOR;
					newLeft = new UnaryExpression(binexp.getLocation(), leftType, UnaryExpression.Operator.LOGICNEG,
					        newLeft);
					newRight = new UnaryExpression(binexp.getLocation(), rightType, UnaryExpression.Operator.LOGICNEG,
					        newRight);
					break;
				case LOGICOR:
					newOp = Operator.LOGICAND;
					newLeft = new UnaryExpression(binexp.getLocation(), leftType, UnaryExpression.Operator.LOGICNEG,
					        newLeft);
					newRight = new UnaryExpression(binexp.getLocation(), rightType, UnaryExpression.Operator.LOGICNEG,
					        newRight);
					break;
				case COMPPO:
					mLogger.warn("The comparison operator " + binexp.getOperator() + " is not yet supported.");
				default:
					newOp = binexp.getOperator();
					throw new UnsupportedOperationException("Fix me: " + binexp.getOperator());
				}

				final BinaryExpression newExp = new BinaryExpression(binexp.getLocation(), expr.getType(), newOp,
				        newLeft, newRight);

				if (mLogger.isDebugEnabled()) {
					mLogger.debug(new StringBuilder().append(AbsIntPrefInitializer.INDENT).append(" Expression ")
					        .append(BoogiePrettyPrinter.print(expr)).append(" rewritten to: ")
					        .append(BoogiePrettyPrinter.print(newExp)));
				}

				return newExp;
			} else if (expr.getExpr() instanceof UnaryExpression) {
				final UnaryExpression unexp = (UnaryExpression) expr.getExpr();
				if (unexp.getOperator() == UnaryExpression.Operator.LOGICNEG) {
					return unexp.getExpr();
				}
			}
		}

		return expr;
	}

	@Override
	protected void visit(final BinaryExpression expr) {

		assert mEvaluatorFactory != null;

		final INAryEvaluator<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar> evaluator = mEvaluatorFactory
		        .createNAryExpressionEvaluator(2, EvaluatorUtils.getEvaluatorType(expr.getType()));

		evaluator.setOperator(expr.getOperator());

		mExpressionEvaluator.addEvaluator(evaluator);
	}

	private void handleAssumeStatement(final AssumeStatement statement) {
		mEvaluatorFactory = new IntervalEvaluatorFactory(mLogger);
		mExpressionEvaluator = new ExpressionEvaluator<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar>();

		final Expression formula = statement.getFormula();

		if (formula instanceof BooleanLiteral) {
			final BooleanLiteral boolform = (BooleanLiteral) formula;
			if (!boolform.getValue()) {
				mReturnState.add(mOldState.bottomState());
			} else {
				mReturnState.add(mOldState);
			}
			// We return since newState is a copy of the old state and the application of true is the old state.
			return;
		}

		processExpression(formula);

		assert mExpressionEvaluator.isFinished();

		final List<IEvaluationResult<IntervalDomainValue>> result = mExpressionEvaluator.getRootEvaluator()
		        .evaluate(mOldState);

		for (final IEvaluationResult<IntervalDomainValue> res : result) {
			if (res.getValue().isBottom() || res.getBooleanValue().getValue() == Value.BOTTOM
			        || res.getBooleanValue().getValue() == Value.FALSE) {
				if (mOldState.getVariables().size() != 0) {
					mReturnState.add(mOldState.bottomState());
				}
			} else {
				final List<IntervalDomainState> resultStates = mExpressionEvaluator.getRootEvaluator()
				        .inverseEvaluate(res, mOldState);
				mReturnState.addAll(resultStates);
			}
		}
	}

	@Override
	protected void visit(final FunctionApplication expr) {
		assert mEvaluatorFactory != null;

		IEvaluator<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar> evaluator;

		final List<Declaration> decls = mSymbolTable.getFunctionOrProcedureDeclaration(expr.getIdentifier());

		// If we don't have a specification for the function, we return top.
		if (decls == null || decls.isEmpty()) {
			evaluator = new IntervalSingletonValueExpressionEvaluator(new IntervalDomainValue());
		} else {

			assert decls.get(0) instanceof FunctionDeclaration;

			final FunctionDeclaration fun = (FunctionDeclaration) decls.get(0);

			// If the body is empty (as in undefined), we return top.
			if (fun.getBody() == null) {
				// evaluator = new IntervalSingletonValueExpressionEvaluator(new IntervalDomainValue());
				evaluator = mEvaluatorFactory.createFunctionEvaluator(fun.getIdentifier(), fun.getInParams().length);
			} else {
				// TODO Handle bitshifts, bitwise and, bitwise or, etc.

				throw new UnsupportedOperationException(
				        "The function application for not inlined functions is not yet supported.");
			}
		}

		mExpressionEvaluator.addEvaluator(evaluator);
	}

	private void handleHavocStatement(final HavocStatement statement) {
		mEvaluatorFactory = new IntervalEvaluatorFactory(mLogger);

		IntervalDomainState currentNewState = mOldState.copy();

		for (final VariableLHS var : statement.getIdentifiers()) {
			final IBoogieVar type = mOldState.getVariables().get(var.getIdentifier());

			if (type.getIType() instanceof PrimitiveType) {
				final PrimitiveType primitiveType = (PrimitiveType) type.getIType();

				if (primitiveType.getTypeCode() == PrimitiveType.BOOL) {
					currentNewState = currentNewState.setBooleanValue(var.getIdentifier(), new BooleanValue());
				} else {
					currentNewState = currentNewState.setValue(var.getIdentifier(), new IntervalDomainValue());
				}
			} else if (type.getIType() instanceof ArrayType) {
				// TODO:
				// Implement better handling of arrays.
				currentNewState = currentNewState.setValue(var.getIdentifier(), new IntervalDomainValue());
			} else {
				currentNewState = currentNewState.setValue(var.getIdentifier(), new IntervalDomainValue());
			}
		}

		mReturnState.add(currentNewState);
	}

	@Override
	protected void visit(final IdentifierExpression expr) {
		assert mEvaluatorFactory != null;

		final IEvaluator<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar> evaluator = mEvaluatorFactory
		        .createSingletonVariableExpressionEvaluator(expr.getIdentifier());

		mExpressionEvaluator.addEvaluator(evaluator);

		super.visit(expr);
	}

	@Override
	protected void visit(final UnaryExpression expr) {
		assert mEvaluatorFactory != null;

		final INAryEvaluator<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar> evaluator = mEvaluatorFactory
		        .createNAryExpressionEvaluator(1, EvaluatorUtils.getEvaluatorType(expr.getType()));

		evaluator.setOperator(expr.getOperator());

		mExpressionEvaluator.addEvaluator(evaluator);

		super.visit(expr);
	}

	@Override
	protected void visit(final BooleanLiteral expr) {
		assert mEvaluatorFactory != null;

		final IEvaluator<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar> evaluator = mEvaluatorFactory
		        .createSingletonLogicalValueExpressionEvaluator(new BooleanValue(expr.getValue()));

		mExpressionEvaluator.addEvaluator(evaluator);
	}

	@Override
	protected void visit(final ArrayStoreExpression expr) {
		throw new UnsupportedOperationException("Proper array handling is not implemented.");
	}

	@Override
	protected void visit(final ArrayAccessExpression expr) {
		throw new UnsupportedOperationException("Proper array handling is not implemented.");
	}

	@Override
	protected void visit(final IfThenElseExpression expr) {
		assert mEvaluatorFactory != null;

		final IEvaluator<IntervalDomainValue, IntervalDomainState, CodeBlock, IBoogieVar> evaluator = mEvaluatorFactory
		        .createConditionalEvaluator();

		mExpressionEvaluator.addEvaluator(evaluator);

		// Create a new expression for the negative case
		final UnaryExpression newUnary = new UnaryExpression(expr.getLocation(), UnaryExpression.Operator.LOGICNEG,
		        expr.getCondition());

		// This expression should be added first to the evaluator inside the handling of processExpression.
		processExpression(newUnary);
	}

}
