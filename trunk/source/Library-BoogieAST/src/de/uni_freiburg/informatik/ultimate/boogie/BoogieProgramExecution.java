/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Core.
 * 
 * The ULTIMATE Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Core. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Core, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Core grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.boogie;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.BoogieASTNode;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.output.BoogiePrettyPrinter;
import de.uni_freiburg.informatik.ultimate.core.lib.translation.DefaultTranslator;
import de.uni_freiburg.informatik.ultimate.core.lib.translation.ProgramExecutionFormatter;
import de.uni_freiburg.informatik.ultimate.core.model.models.IType;
import de.uni_freiburg.informatik.ultimate.core.model.translation.AtomicTraceElement;
import de.uni_freiburg.informatik.ultimate.core.model.translation.IProgramExecution;
import de.uni_freiburg.informatik.ultimate.core.model.translation.ITranslator;
import de.uni_freiburg.informatik.ultimate.core.model.translation.IValuation;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 * @author heizmann@informatik.uni-freiburg.de
 * 
 */
public class BoogieProgramExecution implements IProgramExecution<BoogieASTNode, Expression> {

	private final List<AtomicTraceElement<BoogieASTNode>> m_Trace;
	private final Map<Integer, ProgramState<Expression>> m_PartialProgramStateMapping;

	public BoogieProgramExecution(Map<Integer, ProgramState<Expression>> partialProgramStateMapping,
			List<AtomicTraceElement<BoogieASTNode>> trace) {
		m_Trace = trace;
		m_PartialProgramStateMapping = partialProgramStateMapping;
	}

	@Override
	public int getLength() {
		return m_Trace.size();
	}

	@Override
	public AtomicTraceElement<BoogieASTNode> getTraceElement(int i) {
		return m_Trace.get(i);
	}

	@Override
	public ProgramState<Expression> getProgramState(int i) {
		if (i < 0 || i >= m_Trace.size()) {
			throw new IllegalArgumentException("out of range");
		}
		return m_PartialProgramStateMapping.get(i);
	}

	@Override
	public ProgramState<Expression> getInitialProgramState() {
		return m_PartialProgramStateMapping.get(-1);
	}

	@Override
	public String toString() {
		ProgramExecutionFormatter<BoogieASTNode, Expression> pef = new ProgramExecutionFormatter<>(
				new BoogieBacktranslationValueProvider());
		return pef.formatProgramExecution(this);
	}

	public IValuation getValuation(final List<ITranslator<?, ?, ?, ?>> translatorSequence) {
		return new IValuation() {
			@Override
			public Map<String, SimpleEntry<IType, List<String>>> getValuesForFailurePathIndex(int index) {
				ProgramState<Expression> ps = getProgramState(index);
				if (ps == null) {
					return getEmtpyProgramState();
				} else {
					return translateProgramState(ps);
				}
			}

			public Map<String, SimpleEntry<IType, List<String>>> getEmtpyProgramState() {
				return Collections.emptyMap();
			}

			public Map<String, SimpleEntry<IType, List<String>>> translateProgramState(ProgramState<Expression> ps) {
				Map<String, SimpleEntry<IType, List<String>>> result = new HashMap<String, SimpleEntry<IType, List<String>>>();
				for (Expression var : ps.getVariables()) {
					String varString = backtranslationWorkaround(translatorSequence, var);
					List<String> valuesString = exprSet2StringList(ps.getValues(var));
					result.put(varString, new SimpleEntry<IType, List<String>>(var.getType(), valuesString));
				}
				return result;
			}

			private List<String> exprSet2StringList(Collection<Expression> expressions) {
				List<String> result = new ArrayList<String>(expressions.size());
				for (Expression expr : expressions) {
					result.add(backtranslationWorkaround(translatorSequence, expr));
				}
				return result;
			}
		};
	}

	/**
	 * Use Ultimate's translator sequence do translate a result expression back through the toolchain.
	 * 
	 * @param expr
	 *            the resulting expression
	 * @return a string corresponding to the backtranslated expression
	 */
	private static <SE> String backtranslationWorkaround(List<ITranslator<?, ?, ?, ?>> translatorSequence, SE expr) {
		Object backExpr = DefaultTranslator.translateExpressionIteratively(expr,
				translatorSequence.toArray(new ITranslator[0]));

		// If the result is a Boogie expression, we use the Boogie pretty
		// printer
		String result;
		if (backExpr instanceof String) {
			result = (String) backExpr;
		} else if (backExpr instanceof Expression) {
			result = BoogiePrettyPrinter.print((Expression) backExpr);
		} else {
			result = backExpr.toString();
		}
		return result;
	}

	@Override
	public Class<Expression> getExpressionClass() {
		return Expression.class;
	}

	@Override
	public Class<BoogieASTNode> getTraceElementClass() {
		return BoogieASTNode.class;
	}

	@Override
	public String getSVCOMPWitnessString() {
		return null;
	}
}
