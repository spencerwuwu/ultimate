/*
 * Copyright (C) 2016 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2016 University of Freiburg
 * 
 * This file is part of the ULTIMATE SMTSolverBridge.
 * 
 * The ULTIMATE SMTSolverBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE SMTSolverBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE SMTSolverBridge. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE SMTSolverBridge, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE SMTSolverBridge grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.smtsolver.external;

import de.uni_freiburg.informatik.ultimate.logic.PrintTerm;
import de.uni_freiburg.informatik.ultimate.logic.QuotedObject;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;

public class SmtCommandUtils {
	
	public interface ISmtCommand {
		
		public abstract void execute(Script script);
		/**
		 * 
		 * @return the representation of the command that can be passed to
		 * an SMT solver 
		 */
		@Override
		public abstract String toString();
	}
	
	
	public static class SetLogicCommand implements ISmtCommand {
		private final String mLogic;
		public SetLogicCommand(final String logic) {
			mLogic = logic;
		}
		
		public static String buildString(final String logic) {
			return "(set-logic " + logic + ")";
		}
		@Override
		public void execute(final Script script) {
			script.setLogic(mLogic);
		}
		@Override
		public String toString() {
			return buildString(mLogic);
		}
	}
	
	public static class SetOptionCommand implements ISmtCommand {
		private final String mOpt;
		private final Object mValue;
		
		public SetOptionCommand(final String opt, final Object value) {
			super();
			mOpt = opt;
			mValue = value;
		}
		
		public static String buildString(final String opt, final Object value) {
			final StringBuilder sb = new StringBuilder();
			sb.append("(set-option ").append(opt);
			if (value != null) {
				sb.append(" ");
				if (value instanceof String) {
					// symbol
					sb.append(PrintTerm.quoteIdentifier((String) value));
				} else if (value instanceof Object[]) {
					// s-expr
					new PrintTerm().append(sb, (Object[]) value);
				} else {
					sb.append(value.toString());
				}
			}
			sb.append(")");
			return sb.toString();
		}

		@Override
		public void execute(final Script script) {
			script.setOption(mOpt, mValue);
		}
		
		@Override
		public String toString() {
			return buildString(mOpt, mValue);
		}
	}

	public static class SetInfoCommand implements ISmtCommand {
		private final String mInfo;
		private final Object mValue;
		
		public SetInfoCommand(final String info, final Object value) {
			super();
			mInfo = info;
			mValue = value;
		}
		
		public static String buildString(final String info, final Object value) {
			final StringBuilder sb = new StringBuilder();
			sb.append("(set-info ");
			sb.append(info);
			sb.append(' ');
			sb.append(PrintTerm.quoteObjectIfString(value));
			sb.append(")");
			sb.append(System.lineSeparator());
			return sb.toString();
		}

		@Override
		public void execute(final Script script) {
			script.setInfo(mInfo, mValue);
		}
		
		@Override
		public String toString() {
			return buildString(mInfo, mValue);
		}
	}

	public static class DeclareSortCommand implements ISmtCommand {
		private final String mSort;
		private final int mArity;
		
		
		public DeclareSortCommand(final String sort, final int arity) {
			super();
			mSort = sort;
			mArity = arity;
		}
		
		public static String buildString(final String sort, final int arity) {
			final StringBuilder sb = new StringBuilder("(declare-sort ").append(PrintTerm.quoteIdentifier(sort));
			sb.append(" ").append(arity).append(")");
			return sb.toString();
		}

		@Override
		public void execute(final Script script) {
			script.declareSort(mSort, mArity);
		}
		
		@Override
		public String toString() {
			return buildString(mSort, mArity);
		}
	}

	public static class DefineSortCommand implements ISmtCommand {
		private final String mSort;
		private final Sort[] mSortParams; 
		private final Sort mDefinition;
		
		public DefineSortCommand(final String sort, final Sort[] sortParams, final Sort definition) {
			super();
			mSort = sort;
			mSortParams = sortParams;
			mDefinition = definition;
		}
		
		public static String buildString(final String sort, final Sort[] sortParams, final Sort definition) {
			final PrintTerm pt = new PrintTerm();
			final StringBuilder sb = new StringBuilder();
			sb.append("(define-sort ");
			sb.append(PrintTerm.quoteIdentifier(sort));
			sb.append(" (");
			String delim = "";
			for (final Sort s : sortParams) {
				sb.append(delim);
				pt.append(sb, s);
				delim = " ";
			}
			sb.append(") ");
			pt.append(sb, definition);
			sb.append(")");
			return sb.toString();
		}
		
		@Override
		public void execute(final Script script) {
			script.defineSort(mSort, mSortParams, mDefinition);
		}
		@Override
		public String toString() {
			return buildString(mSort, mSortParams, mDefinition);
		}
		
	}

	public static class DeclareFunCommand implements ISmtCommand {
		final String mFun;
		final Sort[] mParamSorts; 
		final Sort mResultSort;
		public DeclareFunCommand(final String fun, final Sort[] paramSorts, final Sort resultSort) {
			super();
			mFun = fun;
			mParamSorts = paramSorts;
			mResultSort = resultSort;
		}
		
		public static String buildString(final String fun, final Sort[] paramSorts, final Sort resultSort) {
			final PrintTerm pt = new PrintTerm();
			final StringBuilder sb = new StringBuilder();
			sb.append("(declare-fun ");
			sb.append(PrintTerm.quoteIdentifier(fun));
			sb.append(" (");
			String delim = "";
			for (final Sort s : paramSorts) {
				sb.append(delim);
				pt.append(sb, s);
				delim = " ";
			}
			sb.append(") ");
			pt.append(sb, resultSort);
			sb.append(")");
			return sb.toString();
		}

		@Override
		public void execute(final Script script) {
			script.declareFun(mFun, mParamSorts, mResultSort);
		}

		@Override
		public String toString() {
			return buildString(mFun, mParamSorts, mResultSort);
		}


	}

	public static class DefineFunCommand implements ISmtCommand {
		final String mFun; 
		final TermVariable[] mParams; 
		final Sort mResultSort; 
		final Term mDefinition;
		public DefineFunCommand(final String fun, final TermVariable[] params, final Sort resultSort, final Term definition) {
			super();
			mFun = fun;
			mParams = params;
			mResultSort = resultSort;
			mDefinition = definition;
		}

		public static String buildString(final String fun, final TermVariable[] params, final Sort resultSort, final Term definition) {
			final PrintTerm pt = new PrintTerm();
			final StringBuilder sb = new StringBuilder();
			sb.append("(define-fun ");
			sb.append(PrintTerm.quoteIdentifier(fun));
			sb.append(" (");
			String delim = "";
			for (final TermVariable t : params) {
				sb.append(delim);
				sb.append("(").append(t).append(" ");
				pt.append(sb, t.getSort());
				sb.append(")");
				delim = " ";
			}
			sb.append(") ");
			pt.append(sb, resultSort);
			pt.append(sb, definition);
			sb.append(")");
			return sb.toString();
		}
		@Override
		public void execute(final Script script) {
			script.defineFun(mFun, mParams, mResultSort, mDefinition);
		}

		@Override
		public String toString() {
			return buildString(mFun, mParams, mResultSort, mDefinition);
		}

	}

	public static class AssertCommand implements ISmtCommand {
		private final Term mTerm;

		public AssertCommand(final Term term) {
			super();
			mTerm = term;
		}

		public static String buildString(final Term term) {
			return "(assert " + term.toStringDirect() + ")";
		}

		@Override
		public void execute(final Script script) {
			script.assertTerm(mTerm);
		}

		@Override
		public String toString() {
			return buildString(mTerm);
		}
	}

	public static class ResetCommand implements ISmtCommand {

		public ResetCommand() {
			super();
		}

		public static String buildString() {
			return "(reset)";
		}

		@Override
		public void execute(final Script script) {
			script.reset();;
		}
		@Override
		public String toString() {
			return buildString();
		}
	}

	public static class EchoCommand implements ISmtCommand {
		final QuotedObject mMsg;
		public EchoCommand(final QuotedObject msg) {
			super();
			mMsg = msg;
		}
		@Override
		public void execute(final Script script) {
			script.echo(mMsg);
		}
		public static String buildString(final QuotedObject msg) {
			return "(echo " + msg + ")";
		}

		@Override
		public String toString() {
			return buildString(mMsg);
		}

	}
	
}
