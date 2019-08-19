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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.gui.genericwindows.REpiceaProgressBarDialog;
import repicea.stats.data.DataSequence.ActionOnPattern;
import repicea.stats.data.DataSequence.Mode;
import repicea.stats.data.DataSet.ActionType;
import repicea.stats.data.DataSetGroupMap.PatternMode;
import repicea.util.ObjectUtility;

public class DataPattern extends ArrayList<Object> implements Cloneable {

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
	
	public static void main(String[] args) throws IOException {
		String filename = ObjectUtility.getPackagePath(DataSet.class) + "trees.csv";
		DataSet dataSet = new DataSet(filename, false);
		new REpiceaProgressBarDialog("Reading inventory", "...", dataSet, false);

		// for species
		List<Object> exclusions = new ArrayList<Object>();
		exclusions.add("NA");
		
		List<Integer> fieldsForSplitting = new ArrayList<Integer>();
		fieldsForSplitting.add(1);	// field i
		fieldsForSplitting.add(3);	// field NO_ARBRE
		List<Integer> fieldsForSorting = new ArrayList<Integer>();
		fieldsForSorting.add(2);	// field year
		DataSetGroupMap dataSetGroupMap = dataSet.splitAndOrder(fieldsForSplitting, fieldsForSorting);
		
		
		List<Object> terminalStatuses = new ArrayList<Object>();
		terminalStatuses.add(23.0);
		terminalStatuses.add(24.0);
		terminalStatuses.add(25.0);
		terminalStatuses.add(26.0);
		terminalStatuses.add(29.0);
		List<Object> deadStatuses = new ArrayList<Object>();
		deadStatuses.add(14.0);
		deadStatuses.add(16.0);
		List<Object> forgottenDeadStatuses = new ArrayList<Object>();
		deadStatuses.add(34.0);
		deadStatuses.add(36.0);
		List<Object> recruitDeadStatuses = new ArrayList<Object>();
		deadStatuses.add(44.0);
		deadStatuses.add(46.0);
		List<Object> renumberedDeadStatuses = new ArrayList<Object>();
		deadStatuses.add(54.0);
		deadStatuses.add(56.0);
		List<Object> aliveStatuses = new ArrayList<Object>();
		aliveStatuses.add(10.0);
		aliveStatuses.add(12.0);
		List<Object> forgottenStatuses = new ArrayList<Object>();
		forgottenStatuses.add(30.0);
		forgottenStatuses.add(32.0);
		List<Object> recruitStatuses = new ArrayList<Object>();
		recruitStatuses.add(40.0);
		recruitStatuses.add(42.0);
		List<Object> renumberedStatuses = new ArrayList<Object>();
		renumberedStatuses.add(50.0);
		renumberedStatuses.add(52.0);
		
		ActionOnPattern action = new ActionOnPattern() {
			@Override
			protected void doAction(DataPattern pattern, Object...parms) {
				pattern.comment("status = C");
			}
		};
		
		DataSequence acceptableDataSequence = new DataSequence("normal sequence", true, Mode.Total, action);

		List<Object> alives = new ArrayList<Object>();
		alives.addAll(aliveStatuses);
		alives.addAll(forgottenStatuses);
		alives.addAll(recruitStatuses);
		alives.addAll(renumberedStatuses);
		
		
		List<Object> possibleOutcomes;
		for (Object obj : alives) {
			possibleOutcomes = new ArrayList<Object>();
			possibleOutcomes.addAll(aliveStatuses);
			possibleOutcomes.addAll(deadStatuses);
			possibleOutcomes.addAll(terminalStatuses);
			acceptableDataSequence.put(obj, DataSequence.convertListToMap(possibleOutcomes));
		}

//		possibleOutcomes = new ArrayList<Object>();
//		possibleOutcomes.addAll(forgottenStatuses);
//		possibleOutcomes.addAll(recruitStatuses);
//		possibleOutcomes.addAll(renumberedStatuses);
//		possibleOutcomes.add("NA");
////		possibleOutcomes.addAll(deadStatuses);
////		possibleOutcomes.addAll(terminalStatuses);
//		dataSequence.put("NA", possibleOutcomes);
		
		List<Object> allDead = new ArrayList<Object>();
		allDead.addAll(deadStatuses);
		allDead.addAll(forgottenDeadStatuses);
		allDead.addAll(recruitDeadStatuses);
		allDead.addAll(renumberedDeadStatuses);
		for (Object obj : allDead) {
			possibleOutcomes = new ArrayList<Object>();
			possibleOutcomes.addAll(deadStatuses);
			possibleOutcomes.addAll(terminalStatuses);
			acceptableDataSequence.put(obj, DataSequence.convertListToMap(possibleOutcomes));
		}

		possibleOutcomes = new ArrayList<Object>();
		possibleOutcomes.add("GM");
		acceptableDataSequence.put("GM", DataSequence.convertListToMap(possibleOutcomes));
		
		for (Object obj : terminalStatuses) {
			possibleOutcomes = new ArrayList<Object>();
			possibleOutcomes.addAll(terminalStatuses);
			possibleOutcomes.addAll(deadStatuses);
			acceptableDataSequence.put(obj, DataSequence.convertListToMap(possibleOutcomes));
		}
		
		
		action = new ActionOnPattern() {
			@Override
			protected void doAction(DataPattern pattern, Object... parms) {
				int observationIndex = (Integer) parms[0];
				for (int i = 0; i < pattern.size(); i++) {
					if (i >= observationIndex) {
						pattern.updateField(i, "NO_ARBRE", 1000, ActionType.Add);
						pattern.comment(i, "renumbered");
					} else {
						pattern.comment(i, "status = C");
					}
				}
			}
		};
		
		DataSequence twoDifferentTreesSequence = new DataSequence("two trees confounded", false, Mode.Partial, action);
		List<Object> deadOrMissingStatuses = new ArrayList<Object>();
		deadOrMissingStatuses.addAll(terminalStatuses);
		deadOrMissingStatuses.add("NA");
		for (Object obj : deadOrMissingStatuses) {
			possibleOutcomes = new ArrayList<Object>();
			possibleOutcomes.addAll(recruitStatuses);
			possibleOutcomes.addAll(recruitDeadStatuses);
			twoDifferentTreesSequence.put(obj, DataSequence.convertListToMap(possibleOutcomes));
		}

		
		action = new ActionOnPattern() {
			@Override
			protected void doAction(DataPattern pattern, Object... parms) {
				int observationIndex = (Integer) parms[0];
				for (int i = 0; i < pattern.size(); i++) {
					if (i == observationIndex) {
						pattern.updateField(i, "ETAT", 10.0, ActionType.Replace);
						pattern.comment(i, "status dead changed for alive");
					} else {
						pattern.comment(i, "status = C");
					}
				}
			}
		};
		DataSequence measurementErrorSequence1 = new DataSequence("measurement error", false, Mode.Partial, action);
		for (Object obj : deadStatuses) {
			possibleOutcomes = new ArrayList<Object>();
			possibleOutcomes.addAll(aliveStatuses);
			Map<Object, Map> oMap = new HashMap<Object, Map>();
			oMap.put(10.0, DataSequence.convertListToMap(possibleOutcomes));
			measurementErrorSequence1.put(obj, oMap);
		}

		action = new ActionOnPattern() {
			@Override
			protected void doAction(DataPattern pattern, Object... parms) {
				int observationIndex = (Integer) parms[0];
				for (int i = 0; i < pattern.size(); i++) {
					if (i == observationIndex + 1) {
						pattern.updateField(i, "ETAT", 10.0, ActionType.Replace);
						pattern.comment(i, "status dead changed for alive");
					} else {
						pattern.comment(i, "status = C");
					}
				}
			}
		};
		DataSequence measurementErrorSequence2 = new DataSequence("measurement error", false, Mode.Partial, action);
		List<Object> aliveAndRecruits = new ArrayList<Object>();
		aliveAndRecruits.addAll(aliveStatuses);
		aliveAndRecruits.addAll(recruitStatuses);
		for (Object obj : aliveAndRecruits) {
			possibleOutcomes = new ArrayList<Object>();
			possibleOutcomes.add(10.0);
			Map<Object, Map> oMap = new HashMap<Object, Map>();
			oMap.put(14.0, DataSequence.convertListToMap(possibleOutcomes));
			measurementErrorSequence1.put(obj, oMap);
		}

		List<DataSequence> sequences = new ArrayList<DataSequence>();
		sequences.add(acceptableDataSequence);
		sequences.add(twoDifferentTreesSequence);
		sequences.add(measurementErrorSequence1);
		sequences.add(measurementErrorSequence2);
		
		dataSetGroupMap.patternize(PatternMode.Sequence, 
				"ETAT", 
				exclusions, 
				sequences);

		DataSet ds = dataSetGroupMap.get(new DataGroup(312.0, 4.0));	// 3 obs
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(443.0, 42.0));			// 4 obs - 7
		ds.correctValue(0, "ETAT", 10.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(446.0, 23.0));			// 4 obs - 11
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(746.0, 64.0));			// 2 obs - 13
		ds.correctValue(0, "ETAT", 30.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(985.0, 2.0));			// 4 obs - 17	
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(1085.0, 57.0));			// 4 obs - 21
		ds.correctValue(0, "ETAT", 10.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(1099.0, 14.0));			// 3 obs - 24
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(1325.0, 67.0));			// 2 obs - 26
		ds.correctValue(0, "ETAT", 30.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(1605.0, 127.0));			// 2 obs - 28
		ds.correctValue(0, "ETAT", 40.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(1614.0, 2.0));			// 4 obs - 32
		ds.correctValue(2, "ETAT", 10.0, "harvested status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(1616.0, 25.0));			// 4 obs - 36
		ds.correctValue(2, "ETAT", 10.0, "harvested status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(1938.0, 16.0));			// 3 obs - 39
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2086.0, 42.0));			// 2 obs - 41	 
		ds.correctValue(0, "ETAT", 40.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2126.0, 62.0));			// 2 obs - 43
		ds.correctValue(0, "ETAT", 40.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2294.0, 44.0));			// 3 obs - 46
		ds.correctValue(1, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2422.0, 18.0));			// 4 obs - 50
		ds.correctValue(2, "ETAT", 10.0, "harvested status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2427.0, 5.0));			// 4 obs - 54
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2500.0, 45.0));			// 2 obs - 56
		ds.correctValue(0, "ETAT", 40.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2508.0, 36.0));			// 3 obs - 59
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3086.0, 24.0));			// 3 obs - 62
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3099.0, 6.0));			// 5 obs - 67
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3320.0, 9.0));			// 3 obs - 70
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3468.0, 13.0));			// 4 obs - 74
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3490.0, 17.0));			// 2 obs - 76
		ds.correctValue(0, "ETAT", 40.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3490.0, 20.0));			// 2 obs - 78
		ds.correctValue(0, "ETAT", 40.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3647.0, 68.0));			// 3 obs - 81
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3655.0, 7.0));			// 3 obs - 84
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3718.0, 66.0));			// 3 obs - 87
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3754.0, 10.0));			// 2 obs - 89
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3762.0, 86.0));			// 2 obs - 91
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3762.0, 87.0));			// 2 obs - 93
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3784.0, 28.0));			// 4 obs - 97
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3810.0, 51.0));			// 3 obs - 100
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3811.0, 26.0));			// 2 obs - 102
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3811.0, 27.0));			// 2 obs - 104
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3811.0, 28.0));			// 2 obs - 106
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3840.0, 11.0));			// 3 obs - 109
		ds.correctValue(2, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3840.0, 10.0));			// 3 obs - 112
		ds.correctValue(2, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3840.0, 12.0));			// 2 obs - 114
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3840.0, 13.0));			// 2 obs - 116
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3840.0, 14.0));			// 2 obs - 118
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3840.0, 15.0));			// 2 obs - 120
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3840.0, 16.0));			// 2 obs - 122
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3847.0, 1.0));			// 3 obs - 125
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3855.0, 7.0));			// 3 obs - 128
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3941.0, 49.0));			// 4 obs - 132
		ds.correctValue(1, "ETAT", 10.0, "intruder status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3941.0, 5.0));			// 4 obs - 136
		ds.correctValue(1, "ETAT", 10.0, "intruder status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3957.0, 55.0));			// 3 obs - 139
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3987.0, 9.0));			// 3 obs - 142
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4010.0, 13.0));			// 4 obs - 146
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4078.0, 107.0));			// 3 obs - 149
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4079.0, 3.0));			// 3 obs - 152
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4139.0, 42.0));			// 4 obs - 156
		ds.correctValue(2, "ETAT", 10.0, "missing status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4203.0, 28.0));			// 5 obs - 161
		ds.correctValue(1, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds.correctValue(2, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4327.0, 25.0));			// 3 obs - 164
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4538.0, 20.0));			// 4 obs - 168
		ds.correctValue(1, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds.correctValue(2, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(1864.0, 47.0));			// 3 obs - 171
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4679.0, 5.0));			// 5 obs - 176
		ds.correctValue(3, "ETAT", 10.0, "forgotten status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4679.0, 7.0));			// 5 obs - 181
		ds.correctValue(3, "ETAT", 10.0, "forgotten status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4679.0, 9.0));			// 3 obs - 184
		ds.correctValue(1, "ETAT", 10.0, "forgotten status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4679.0, 10.0));			// 3 obs - 187
		ds.correctValue(1, "ETAT", 10.0, "forgotten status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4700.0, 17.0));			// 5 obs - 192
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4786.0, 32.0));			// 3 obs - 195
		ds.correctValue(1, "ETAT", 10.0, "missing status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4791.0, 20.0));			// 3 obs - 198
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(4810.0, 26.0));			// 2 obs - 200
		ds.correctValue(0, "ETAT", 40.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(387.0, 29.0));			// 4 obs - 204
		ds.correctValue(3, "NO_ARBRE", 1029.0, "manually renumbered", true, "status = C");
		ds.correctValue(2, "NO_ARBRE", 1029.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(808.0, 2.0));			// 4 obs - 208
		ds.correctValue(3, "NO_ARBRE", 1002.0, "manually renumbered", true, "status = C");
		ds.correctValue(2, "NO_ARBRE", 1002.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(1086.0, 99.0));			// 4 obs - 212
		ds.correctValue(3, "NO_ARBRE", 1099.0, "manually renumbered", true, "status = C");
		ds.correctValue(2, "NO_ARBRE", 1099.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(1660.0, 63.0));			// 3 obs - 215
		ds.correctValue(0, "ETAT", 40.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(1925.0, 30.0));			// 4 obs - 219
		ds.correctValue(0, "ETAT", 10.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2181.0, 37.0));			// 4 obs - 223
		ds.correctValue(3, "NO_ARBRE", 1037.0, "manually renumbered", true, "status = C");
		ds.correctValue(2, "NO_ARBRE", 1037.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2295.0, 9.0));			// 4 obs - 227
		ds.correctValue(0, "ETAT", 10.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2755.0, 73.0));			// 3 obs - 230
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2872.0, 11.0));			// 3 obs - 233
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(2874.0, 1.0));			// 3 obs - 236
		ds.correctValue(2, "NO_ARBRE", 1001.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3970.0, 41.0));			// 4 obs - 240
		ds.correctValue(3, "NO_ARBRE", 1041.0, "manually renumbered", true, "status = C");
		ds.correctValue(2, "NO_ARBRE", 1041.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3970.0, 42.0));			// 4 obs - 244
		ds.correctValue(3, "NO_ARBRE", 1042.0, "manually renumbered", true, "status = C");
		ds.correctValue(2, "NO_ARBRE", 1042.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3970.0, 43.0));			// 4 obs - 248
		ds.correctValue(3, "NO_ARBRE", 1043.0, "manually renumbered", true, "status = C");
		ds.correctValue(2, "NO_ARBRE", 1043.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3971.0, 87.0));			// 4 obs - 252
		ds.correctValue(3, "NO_ARBRE", 1087.0, "manually renumbered", true, "status = C");
		ds.correctValue(2, "NO_ARBRE", 1087.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3971.0, 88.0));			// 4 obs - 256
		ds.correctValue(3, "NO_ARBRE", 1088.0, "manually renumbered", true, "status = C");
		ds.correctValue(2, "NO_ARBRE", 1088.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(3971.0, 89.0));			// 4 obs - 260
		ds.correctValue(3, "NO_ARBRE", 1089.0, "manually renumbered", true, "status = C");
		ds.correctValue(2, "NO_ARBRE", 1089.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5143.0, 55.0));			// 2 obs - 262
		ds.correctValue(0, "ETAT", 30.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5189.0, 24.0));			// 5 obs - 267
		ds.correctValue(4, "NO_ARBRE", 1024.0, "manually renumbered", true, "status = C");
		ds.correctValue(3, "NO_ARBRE", 1024.0, "manually renumbered", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5218.0, 25.0));			// 5 obs - 272
		ds.correctValue(1, "ETAT", 10.0, "intruder status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5252.0, 43.0));			// 2 obs - 274
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5257.0, 3.0));			// 4 obs - 278
		ds.correctValue(0, "ETAT", 14.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5257.0, 5.0));			// 4 obs - 282
		ds.correctValue(0, "ETAT", 14.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5257.0, 9.0));			// 4 obs - 286
		ds.correctValue(0, "ETAT", 10.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5257.0, 16.0));			// 4 obs - 290
		ds.correctValue(0, "ETAT", 10.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5258.0, 8.0));			// 4 obs - 294
		ds.correctValue(0, "ETAT", 10.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5258.0, 15.0));			// 4 obs - 298
		ds.correctValue(0, "ETAT", 14.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5258.0, 19.0));			// 4 obs - 302
		ds.correctValue(0, "ETAT", 10.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5258.0, 31.0));			// 4 obs - 306
		ds.correctValue(0, "ETAT", 14.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5258.0, 37.0));			// 4 obs - 310
		ds.correctValue(0, "ETAT", 10.0, "accepted as is", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5258.0, 41.0));			// 3 obs - 313
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5258.0, 42.0));			// 3 obs - 316
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5258.0, 43.0));			// 3 obs - 319
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5258.0, 44.0));			// 3 obs - 322
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5258.0, 45.0));			// 3 obs - 325
		ds.correctValue(1, "ETAT", 10.0, "recruit status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5303.0, 4.0));			// 3 obs - 328
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds = dataSetGroupMap.get(new DataGroup(5444.0, 21.0));			// 2 obs - 330
		ds.correctValue(0, "ETAT", 40.0, "dead status manually changed for alive", true, "status = C");
		
//		dataSetGroupMap.patternize(PatternMode.Homogenize, 3, exclusions);

		
		
//		DataSet correctedDataSet = dataSetGroupMap.recomposeDataSet();

		String exportCorrectedFilename = ObjectUtility.getPackagePath(DataSet.class).replace("bin", "test") + "corrected.csv";
		dataSet.save(exportCorrectedFilename);
//		new FakeDialog(dataSet.getUI());
		System.exit(0);
	}


}
