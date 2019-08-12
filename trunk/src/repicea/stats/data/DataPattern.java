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
import repicea.util.ObjectUtility;

public class DataPattern extends ArrayList<Object> {

	private DataPattern getCleanClone(List<Object> exclusions) {
		DataPattern clone = new DataPattern();
		for (Object obj : this) {
			if (exclusions == null || !exclusions.contains(obj)) {
				clone.add(obj);
			}
		}
		return clone;
	}
	
	public Object getHomogeneousObject(List<Object> exclusions) {
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

	
	
	
	public Object getEmergingObject(List<Object> exclusions) {
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
	
	protected DataPattern getDataPattern(int start, int end) {
		DataPattern subPattern = new DataPattern();
		for (Object obj : this) {
			subPattern.add(obj.toString().substring(start, end));
		}
		return subPattern;
	}
	
	
	public Object getLastButSimilar(List<Object> exclusions, int start, int end) {
		DataPattern clone = getCleanClone(exclusions);
		DataPattern subPattern = clone.getDataPattern(start, end);
		if (subPattern.getHomogeneousObject(null) != null) {
			return clone.get(clone.size() - 1);
		} else {
			return null;
		}
	}
	
	public Object getLastObject(List<Object> exclusions) {
		DataPattern clone = getCleanClone(exclusions);
		if (clone.size() > 0) {
			return clone.get(clone.size() - 1);
		} else {
			return null;
		}
	}
	
	public static Map<DataPattern, List<DataGroup>> getPatternAbundance(Map<DataGroup, DataSet> dataSetMap, int fieldIndexForPattern) {
		Map<DataPattern, List<DataGroup>> patternMap = new HashMap<DataPattern, List<DataGroup>>();
		for (DataGroup id : dataSetMap.keySet()) {
			DataSet ds = dataSetMap.get(id);
			DataPattern pattern = ds.getPatternForThisField(fieldIndexForPattern);
			if (!patternMap.containsKey(pattern)) {
				patternMap.put(pattern, new ArrayList<DataGroup>());
			}
			List<DataGroup> census = patternMap.get(pattern);
			census.add(id);
		}
		return patternMap;
	}


	protected static DataSet patternizeDataSet(DataSet dataSet, List<Integer> fieldsForSplitting, List<Integer> fieldsForSorting) {
		Map<DataGroup, DataSet> dataSetMap = dataSet.splitAndOrder(fieldsForSplitting, fieldsForSorting);
		Map<DataPattern, List<DataGroup>> patterns = DataPattern.getPatternAbundance(dataSetMap, 3);
		List<DataPattern> patternList = new ArrayList<DataPattern>();
		patternList.addAll(patterns.keySet());
		List<Object> exclusions = new ArrayList<Object>();
		exclusions.add("NA");

		List<DataSet> keptDataSets = new ArrayList<DataSet>();
		for (DataPattern pattern : patternList) {
			Object homogenenousPattern = pattern.getHomogeneousObject(exclusions);
			Object emergingWinner = null;
			Object lastButSimilar = null;
			Object last = null;
			if (homogenenousPattern != null) {	// test if they are homogeneous
				List<DataGroup> homogeneousGroups  = patterns.get(pattern);
				DataSet.addCorrectedField(dataSetMap, homogeneousGroups, homogenenousPattern, "speciesCorr", "homogeneous");
//				patterns.remove(pattern);
			} else if ((emergingWinner = pattern.getEmergingObject(exclusions)) != null) {
				List<DataGroup> emergingGroups  = patterns.get(pattern);
				DataSet.addCorrectedField(dataSetMap, emergingGroups, emergingWinner, "speciesCorr", "emerging");
			} else if ((lastButSimilar = pattern.getLastButSimilar(exclusions, 0, 2)) != null) {
				List<DataGroup> lastButSimilarGroups  = patterns.get(pattern);
				DataSet.addCorrectedField(dataSetMap, lastButSimilarGroups, lastButSimilar, "speciesCorr", "lastButSimilar");
			} else if ((last = pattern.getLastObject(exclusions)) != null) {
				List<DataGroup> lastGroups  = patterns.get(pattern);
				DataSet.addCorrectedField(dataSetMap, lastGroups, last, "speciesCorr", "last");
			} else {
				List<DataGroup> notSetGroups  = patterns.get(pattern);
				DataSet.addCorrectedField(dataSetMap, notSetGroups, "unknown", "speciesCorr", "unknown");
				List<DataGroup> dataGroups = patterns.get(pattern);
				for (DataGroup dg : dataGroups) {
					keptDataSets.add(dataSetMap.get(dg));
				}
			}
		}
		
		for (DataPattern pattern : patterns.keySet()) { 
			if (pattern.getHomogeneousObject(exclusions) == null && 
					pattern.getEmergingObject(exclusions) == null && 
					pattern.getLastButSimilar(exclusions, 0, 2) == null &&
					pattern.getLastObject(exclusions) == null) {
				String outputStr = pattern.toString() + " - " + patterns.get(pattern).size() + " obs.";
//				if (lastButSimilarMap.containsKey(pattern)) {
//					outputStr = outputStr.concat(" --- last but similar " + lastButSimilarMap.get(pattern));
//				}
				System.out.println(outputStr); 
			}
		}
		
		DataSet correctedDataSet = DataSet.recomposeDataSet(dataSetMap.values());
		return correctedDataSet;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		
		String filename = ObjectUtility.getPackagePath(DataSet.class) + "trees.csv";
		DataSet dataSet = new DataSet(filename, false);
		new REpiceaProgressBarDialog("Reading inventory", "...", dataSet, false);

		// for species
		
		List<Integer> fieldsForSplitting = new ArrayList<Integer>();
		fieldsForSplitting.add(25);
		fieldsForSplitting.add(2);
		List<Integer> fieldsForSorting = new ArrayList<Integer>();
		fieldsForSorting.add(26);
		
		DataSet correctedDataSet = DataPattern.patternizeDataSet(dataSet, fieldsForSplitting, fieldsForSorting);

		String exportCorrectedFilename = ObjectUtility.getPackagePath(DataSet.class).replace("bin", "src") + "corrected.csv";
		correctedDataSet.save(exportCorrectedFilename);
//		new FakeDialog(dataSet.getUI());
		System.exit(0);
	}

}
