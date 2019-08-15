/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2019 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.stats.data;

import java.util.HashMap;
import java.util.List;

public class DataSequence extends HashMap<Object, List<Object>> {
	
	
	public static class ActionOnPattern {
		protected void doAction(DataPattern pattern, Object... parms) {}
	}
	
	
	protected static enum Mode {Total, Partial}
	
	private final boolean isEmptyPatternAccepted;
	private final Mode mode;
	private final ActionOnPattern action;
	
	public DataSequence(boolean isEmptyPatternAccepted, Mode mode, ActionOnPattern action) {
		this.isEmptyPatternAccepted = isEmptyPatternAccepted;
		this.mode = mode;
		this.action = action;
	}

	
	public boolean testPattern(DataPattern pattern, List<Object> exclusions) {
		switch (mode) {
		case Total:
			boolean fit = doesFitInThisSequence(pattern, exclusions);
			if (fit) {
				action.doAction(pattern);
			}
			return fit;
		case Partial:
			Integer index = doesPartlyFitInThisSequence(pattern, exclusions);
			if (index != null) {
				action.doAction(pattern, index);
			}
			return  index != null;
		}
		return false;
	}
	
	
	private boolean doesFitInThisSequence(DataPattern pattern, List<Object> exclusions) {
		DataPattern cleanPattern = pattern.getTrimmedPattern(exclusions);
		if (cleanPattern == null || cleanPattern.isEmpty()) {
			return isEmptyPatternAccepted;
		} else  if (cleanPattern.size() == 1) {
			Object singleObj = cleanPattern.get(0);
			return containsKey(singleObj);
		} else {
			for (int i = 1; i < cleanPattern.size(); i++) {
				Object obj0 = cleanPattern.get(i - 1);
				Object obj1 = cleanPattern.get(i);
				if (!containsKey(obj0) || !get(obj0).contains(obj1)) {
					return false;
				} 
			}
			return true;
		}
	}

	private Integer doesPartlyFitInThisSequence(DataPattern pattern, List<Object> exclusions) {
		if (pattern.size() > 2) {
			for (int i = 1; i < pattern.size(); i++) {
				Object obj0 = pattern.get(i - 1);
				Object obj1 = pattern.get(i);
				if (containsKey(obj0) && get(obj0).contains(obj1)) {
					return i;
				} 
			}
		} 
		return null;
	}

	
	
}
