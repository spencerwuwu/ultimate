/*
 * Copyright (C) 2016 Yu-Wen Chen 
 * Copyright (C) 2016 Alexander Nutz (nutz@informatik.uni-freiburg.de)
 * Copyright (C) 2016 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp;

import de.uni_freiburg.informatik.ultimate.logic.ConstantTerm;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVarOrConst;

/**
 * 
 * @author Yu-Wen Chen (yuwenchen1105@gmail.com)
 *
 */
public class EqBaseNode extends EqNode {
	
	private final IProgramVarOrConst mVarOrConst;
	private final boolean mIsLiteral;

	public EqBaseNode(IProgramVarOrConst bv) {
		mVarOrConst = bv;
		mIsLiteral = bv.getTerm() instanceof ConstantTerm;
	}
	
	public String toString() {
		return mVarOrConst.toString();
	}

	@Override
	public Term getTerm(Script s) {
		return mVarOrConst.getTerm();
	}
	
	@Override
	public boolean isLiteral() {
		return mIsLiteral;
	}

	@Override
	public boolean equals(Object other) {
		return other == this;
//		if (!(other instanceof EqBaseNode)) {
//			return false;
//		}
//		EqBaseNode ebn = (EqBaseNode) other;
//		
//		return ebn.mBoogieVarOrConst.equals(this.mBoogieVarOrConst);
	}
}
