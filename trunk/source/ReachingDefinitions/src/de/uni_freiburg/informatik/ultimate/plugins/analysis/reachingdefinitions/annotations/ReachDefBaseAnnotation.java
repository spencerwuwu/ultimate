/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE ReachingDefinitions plug-in.
 * 
 * The ULTIMATE ReachingDefinitions plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE ReachingDefinitions plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE ReachingDefinitions plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE ReachingDefinitions plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE ReachingDefinitions plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.analysis.reachingdefinitions.annotations;

import java.util.HashMap;
import java.util.HashSet;

import de.uni_freiburg.informatik.ultimate.boogie.output.BoogiePrettyPrinter;
import de.uni_freiburg.informatik.ultimate.core.lib.models.annotation.AbstractAnnotations;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.reachingdefinitions.boogie.ScopedBoogieVar;

public abstract class ReachDefBaseAnnotation extends AbstractAnnotations {

	private static final long serialVersionUID = 1L;

	@Override
	protected String[] getFieldNames() {
		return new String[] { "Def", "Use" };
	}

	@Override
	protected Object getFieldValue(String field) {
		switch (field) {
		case "Def":
			return prettyPrintDefUse(getDefs());
		case "Use":
			return prettyPrintDefUse(getUse());
		default:
			return null;
		}
	}

	protected abstract HashMap<ScopedBoogieVar, HashSet<IndexedStatement>> getDefs();

	protected abstract HashMap<ScopedBoogieVar, HashSet<IndexedStatement>> getUse();

	public ReachDefBaseAnnotation() {
		super();
	}

	private String prettyPrintDefUse(HashMap<ScopedBoogieVar, HashSet<IndexedStatement>> map) {
		if (map.isEmpty()) {
			return "Empty";
		}

		StringBuilder sb = new StringBuilder();

		for (ScopedBoogieVar s : map.keySet()) {
			sb.append(s.getIdentifier()).append(": {");
			HashSet<IndexedStatement> set = map.get(s);
			if (set.isEmpty()) {
				continue;
			}
			for (IndexedStatement stmt : map.get(s)) {
				if (stmt.getKey() != null) {
					sb.append(stmt.getKey()).append(" ");
				}
				sb.append(BoogiePrettyPrinter.print(stmt.getStatement())).append(", ");
			}
			sb.delete(sb.length() - 2, sb.length());
			sb.append("}, ");
		}

		sb.delete(sb.length() - 2, sb.length());
		return sb.toString();
	}

	@Override
	public int hashCode() {
		// TODO: Does this what I think (conform to hashCode / equals contract)
		return getDefs().hashCode() + 131 * getUse().hashCode();
	}

	@Override
	public boolean equals(Object arg) {
		if (arg == null) {
			return false;
		}

		if (!(arg instanceof ReachDefBaseAnnotation)) {
			return false;
		}

		ReachDefBaseAnnotation arg0 = (ReachDefBaseAnnotation) arg;
		return compareMap(getDefs(), arg0.getDefs()) && compareMap(getUse(), arg0.getUse());
	}

	private boolean compareMap(HashMap<ScopedBoogieVar, HashSet<IndexedStatement>> mine,
			HashMap<ScopedBoogieVar, HashSet<IndexedStatement>> theirs) {
		if (mine != null && theirs != null) {
			for (ScopedBoogieVar key : mine.keySet()) {
				HashSet<IndexedStatement> myStmts = mine.get(key);
				HashSet<IndexedStatement> theirStmts = theirs.get(key);

				if (myStmts != null && theirStmts != null && myStmts.size() == theirStmts.size()) {
					for (IndexedStatement myStmt : myStmts) {
						if (!theirStmts.contains(myStmt)) {
							return false;
						}
					}
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Def: " + getDefAsString() + " Use: " + getUseAsString();
	}

	public String getDefAsString() {
		return prettyPrintDefUse(getDefs());
	}

	public String getUseAsString() {
		return prettyPrintDefUse(getUse());
	}

	protected HashMap<ScopedBoogieVar, HashSet<IndexedStatement>> copy(HashMap<ScopedBoogieVar, HashSet<IndexedStatement>> other) {
		if (other == null) {
			return null;
		}
		HashMap<ScopedBoogieVar, HashSet<IndexedStatement>> newmap = new HashMap<>();
		for (ScopedBoogieVar key : other.keySet()) {
			HashSet<IndexedStatement> otherset = other.get(key);
			if (otherset == null) {
				continue;
			}
			HashSet<IndexedStatement> newset = new HashSet<>();
			for (IndexedStatement stmt : otherset) {
				newset.add(stmt);
			}
			newmap.put(key, newset);
		}
		return newmap;
	}

}
