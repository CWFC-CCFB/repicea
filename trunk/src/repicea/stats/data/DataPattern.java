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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.stats.data.DataSet.ActionType;

class DataPattern extends ArrayList<Object> implements Cloneable {

	protected final static String JavaComments = "JavaComments";
	
	
	protected final int fieldIndex;
	protected final DataPatternMap dataPatternMap;
	
	protected DataPattern(int fieldIndex, DataPatternMap dataPatternMap) {
		this.fieldIndex = fieldIndex;
		this.dataPatternMap = dataPatternMap;
	}
	
	/*
	 * For test purpose and cloning only
	 */
	protected DataPattern() {
		fieldIndex = -1;
		dataPatternMap = null;
	}
	
	/**
	 * Returns a DataPattern instance without exclusions.
	 * @param exclusions
	 * @return
	 */
	private DataPattern getCleanClone(List<Object> exclusions) {
		DataPattern clone = new DataPattern();
		for (Object obj : this) {
			if (exclusions == null || !exclusions.contains(obj)) {
				clone.add(obj);
			}
		}
		return clone;
	}
	
	/**
	 * Returns a DataPattern trimmed for exclusions
	 * @param exclusions
	 * @return
	 */
	protected DataPattern getTrimmedPattern(List<Object> exclusions) {
		if (exclusions != null && !exclusions.isEmpty()) {
			DataPattern cleanPattern = new DataPattern();
			int i = 0;
			while (i < size() && exclusions.contains(get(i))) {
				i++;
			}
			int j = size() - 1;
			while (j >= 0 && exclusions.contains(get(j))) {
				j--;
			}
			for (int k = i; k <= j; k++) {
				cleanPattern.add(get(k));
			}
			return cleanPattern;
		} else {
			return this;
		}
	}

	/**
	 * Returns the object that is prevalent in terms of frequency. A greater
	 * weight is given to last measurement under the assumption that the ranking
	 * has been done according to the date.
	 * @param exclusions
	 * @return
	 */
	protected Object getEmergingObject(List<Object> exclusions) {
		DataPattern clone = getCleanClone(exclusions);
		Map<Object, Double> rankingMap = new HashMap<Object, Double>();
		for (int i = 0; i < clone.size(); i++) {
			Object obj = clone.get(i);
			double previousValue = 0d;
			if (rankingMap.containsKey(obj)) {
				previousValue = rankingMap.get(obj);
			} 
			rankingMap.put(obj, previousValue + i * .5 + 1);
		}
		double maxValue = 1d;
		Object winningObject = null;
		for (Object obj : rankingMap.keySet()) {
			double rank = rankingMap.get(obj);
			if (rank > maxValue + 1) {
				maxValue = rank;
				winningObject = obj;
			}
		}
		return winningObject;
	}
	
	protected DataPattern getSubDataPattern(int start, int end) {
		DataPattern subPattern = new DataPattern();
		for (Object obj : this) {
			subPattern.add(obj.toString().substring(start, end));
		}
		return subPattern;
	}
	
	/**
	 * Uses a sub pattern typically the first two characters of a String to see
	 * if there is a homogeneous pattern.
	 * @param exclusions
	 * @param start
	 * @param end
	 * @return
	 */
	protected Object getLastButSimilar(List<Object> exclusions, int start, int end) {
		DataPattern clone = getCleanClone(exclusions);
		DataPattern subPattern = clone.getSubDataPattern(start, end);
		if (subPattern.getHomogeneousObject(null) != null) {
			return clone.get(clone.size() - 1);
		} else {
			return null;
		}
	}
	
	protected Object getLastObject(List<Object> exclusions) {
		DataPattern clone = getCleanClone(exclusions);
		if (clone.size() > 0) {
			return clone.get(clone.size() - 1);
		} else {
			return null;
		}
	}
	
	protected Object getHomogeneousObject(List<Object> exclusions) {
		List<Object> clone = getCleanClone(exclusions);
		if (clone.isEmpty()) {
			return null;
		} else if (clone.size() == 1) {
			return clone.get(0);
		} else {
			for (int i = 1; i < clone.size(); i++) {
				if (!clone.get(i).equals(clone.get(i - 1))) {
					return null;
				}
			}
			return clone.get(0);
		}
	}
	
	protected static Object[] getHomogeneousField(int numberOfObservations, Object value) {
		Object[] field = new Object[numberOfObservations];
		for (int i = 0; i < field.length; i++) {
			field[i] = value;
		}
		return field;
	}

	protected void comment(String str) {
		for (int i = 0; i < size(); i++) {
			comment(i, str);
		}
	}

	protected void comment(int i, String str) {
		DataSet originalDataSet = dataPatternMap.dataSetGroupMap.originalDataSet; 
		if (!originalDataSet.getFieldNames().contains(JavaComments)) {
			int size = originalDataSet.getNumberOfObservations();
			Object[] values = new Object[size];
			for (int j = 0; j < values.length; j++) {
				values[j] = "";
			}
			originalDataSet.addField(JavaComments, values);
		}
		updateField(i, JavaComments, str, ActionType.Add);
	}
	
	protected void updateField(int i, String fieldName, Object newValue, ActionType action) {
		List<DataGroup> groups = dataPatternMap.get(this);
		for (DataGroup group : groups) {
			DataSet ds = dataPatternMap.dataSetGroupMap.get(group);
			ds.setValueAt(i, fieldName, newValue, action);
		}
	}
	
	@Override
	public DataPattern clone() {
		DataPattern clone = new DataPattern(fieldIndex, dataPatternMap);
		clone.addAll(this);
		return clone;
	}
	
	
	

}
