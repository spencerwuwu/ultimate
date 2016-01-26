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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.sign;

import java.math.BigDecimal;

import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.EvaluatorUtils.EvaluatorType;

/**
 * Represents a single decimal expression in the {@link SignDomain}.
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
public class SignSingletonDecimalExpressionEvaluator extends SignSingletonValueExpressionEvaluator<BigDecimal> {

	public SignSingletonDecimalExpressionEvaluator(String value, EvaluatorType type) {
		super(value, type);
	}

	@Override
	protected BigDecimal instantiate(String value) {
		BigDecimal number;
		try {
			number = new BigDecimal(value);
		} catch (NumberFormatException e) {
			throw new UnsupportedOperationException("The value \"" + value
			        + "\" cannot be transformed to a decimal number.");
		}

		return number;
	}

	@Override
	protected int getSignum() {
		return mValue.signum();
	}

	@Override
	public boolean containsBool() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EvaluatorType getEvaluatorType() {
		// TODO Auto-generated method stub
		return null;
	}

}
