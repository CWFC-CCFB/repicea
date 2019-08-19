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
import java.util.Map;

public class DataSequence extends HashMap<Object, Map> {
		
	public static class ActionOnPattern {
		protected void doAction(DataPattern pattern, Object... parms) {}
	}
	
	
	protected static enum Mode {Total, Partial}
	
	private final boolean isEmptyPatternAccepted;
	private final Mode mode;
	private final ActionOnPattern action;
	protected final String name;
	
	public DataSequence(String name, boolean isEmptyPatternAccepted, Mode mode, ActionOnPattern action) {
		this.isEmptyPatternAccepted = isEmptyPatternAccepted;
		this.mode = mode;
		this.action = action;
		this.name = name;
	}

	/*
	 * For test purpose only
	 */
	DataSequence() {
		this.isEmptyPatternAccepted = false;
		this.mode = null;
		this.action = null;
		this.name = "";
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
			Integer index = doesPartOfPatternFitThisSequence(pattern, exclusions);
			if (index != null) {
				action.doAction(pattern, index);
			}
			return  index != null;
		}
		return false;
	}
	
	
	protected boolean doesFitInThisSequence(DataPattern pattern, List<Object> exclusions) {
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
				if (!containsKey(obj0) || !get(obj0).containsKey(obj1)) {
					return false;
				} 
			}
			return true;
		}
	}

	private Integer doesPartOfPatternFitThisSequence(DataPattern pattern, List<Object> exclusions) {
		Integer i = null;
		boolean sequenceCompleted = false;
		outerloop:
		for (i = 0; i < pattern.size(); i++) {
			if (containsKey(pattern.get(i))) {
				int j = i;
				Map oMap = this;
				while (j < pattern.size()) {
					Object obj = pattern.get(j);
					if (oMap.containsKey(obj)) {
						if (oMap.get(obj) == null) {
							sequenceCompleted = true;
							break outerloop;
						} else {
							oMap = (Map) oMap.get(obj);
							j++;
						}
					} else {
						break;
					}
				}  
			}
		}
		if (sequenceCompleted) {
			return i;
		} else {
			return null;
		}
	}

	protected static Map<Object, Map> convertListToMap(List<Object> list) {
		Map<Object, Map> outputMap = new HashMap<Object, Map>();
		for (Object obj : list) {
			outputMap.put(obj, null);
		}
		return outputMap;
	}
}
