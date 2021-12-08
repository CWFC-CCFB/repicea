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
import java.util.List;

import repicea.stats.data.DataSet.ActionType;

class DataPattern extends ArrayList<Object> implements Cloneable {

	protected final static String JavaComments = "JavaComments";
	
	
//	protected final int fieldIndex;
	protected final DataPatternMap dataPatternMap;
	
	protected DataPattern(DataPatternMap dataPatternMap) {
//		this.fieldIndex = fieldIndex;
		this.dataPatternMap = dataPatternMap;
	}
	
	protected DataPattern(DataPatternMap dataPatternMap, Object...objects) {
		this(dataPatternMap);
		if (objects != null && objects.length > 0) {
			for (Object obj : objects) {
				add(obj);
			}
		}
	}
	
	/**
	 * Returns a DataPattern instance without exclusions.
	 * @param exclusions
	 * @return
	 */
	protected DataPattern getCleanPattern(List<Object> exclusions) {
		DataPattern clone = new DataPattern(null);
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
			DataPattern cleanPattern = new DataPattern(null);
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

	protected DataPattern getSubDataPattern(int start, int end) {
		DataPattern subPattern = new DataPattern(null);
		for (Object obj : this) {
			subPattern.add(obj.toString().substring(start, end));
		}
		return subPattern;
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
		DataPattern clone = new DataPattern(dataPatternMap);
		clone.addAll(this);
		return clone;
	}
	
	

}
