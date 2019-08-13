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
import repicea.stats.data.DataSetGroupMap.PatternMode;
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
	
	protected DataPattern getDataPattern(int start, int end) {
		DataPattern subPattern = new DataPattern();
		for (Object obj : this) {
			subPattern.add(obj.toString().substring(start, end));
		}
		return subPattern;
	}
	
	
	protected Object getLastButSimilar(List<Object> exclusions, int start, int end) {
		DataPattern clone = getCleanClone(exclusions);
		DataPattern subPattern = clone.getDataPattern(start, end);
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
	
	protected DataPattern getCleanPattern(List<Object> exclusions) {
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
	


	protected boolean doesFitInThisSequence(DataSequence sequence, List<Object> exclusions, boolean isEmptyPatternAccepted) {
		DataPattern cleanPattern = getCleanPattern(exclusions);
		if (cleanPattern == null || cleanPattern.isEmpty()) {
			return isEmptyPatternAccepted;
		} else  if (cleanPattern.size() == 1) {
			Object singleObj = cleanPattern.get(0);
			return sequence.containsKey(singleObj);
		} else {
			for (int i = 1; i < cleanPattern.size(); i++) {
				Object obj0 = cleanPattern.get(i - 1);
				Object obj1 = cleanPattern.get(i);
				if (!sequence.containsKey(obj0) || !sequence.get(obj0).contains(obj1)) {
					return false;
				} 
			}
			return true;
		}
	}

	protected Integer doesPartlyFitInThisSequence(DataSequence sequence, List<Object> exclusions, boolean isEmptyPatternAccepted) {
		if (size() > 2) {
			for (int i = 1; i < size(); i++) {
				Object obj0 = get(i - 1);
				Object obj1 = get(i);
				if (sequence.containsKey(obj0) && sequence.get(obj0).contains(obj1)) {
					return i;
				} 
			}
		} 
		return null;
	}

	
	
	public static void main(String[] args) throws IOException {


		
		
		String filename = ObjectUtility.getPackagePath(DataSet.class) + "trees.csv";
		DataSet dataSet = new DataSet(filename, false);
		new REpiceaProgressBarDialog("Reading inventory", "...", dataSet, false);

		// for species
		List<Object> exclusions = new ArrayList<Object>();
		exclusions.add("NA");
		
		List<Integer> fieldsForSplitting = new ArrayList<Integer>();
		fieldsForSplitting.add(1);
		fieldsForSplitting.add(4);
		List<Integer> fieldsForSorting = new ArrayList<Integer>();
		fieldsForSorting.add(2);
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
		
		DataSequence acceptableDataSequence = new DataSequence();

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
			acceptableDataSequence.put(obj, possibleOutcomes);
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
			acceptableDataSequence.put(obj, possibleOutcomes);
		}

		possibleOutcomes = new ArrayList<Object>();
		possibleOutcomes.add("GM");
		acceptableDataSequence.put("GM", possibleOutcomes);
		
		for (Object obj : terminalStatuses) {
			possibleOutcomes = new ArrayList<Object>();
			possibleOutcomes.addAll(terminalStatuses);
			acceptableDataSequence.put(obj, possibleOutcomes);
		}
		
		DataSequence twoDifferentTreesSequence = new DataSequence();
		List<Object> deadOrMissingStatuses = new ArrayList<Object>();
		deadOrMissingStatuses.addAll(terminalStatuses);
		deadOrMissingStatuses.add("NA");
		for (Object obj : deadOrMissingStatuses) {
			possibleOutcomes = new ArrayList<Object>();
			possibleOutcomes.addAll(recruitStatuses);
			possibleOutcomes.addAll(recruitDeadStatuses);
			twoDifferentTreesSequence.put(obj, possibleOutcomes);
		}
		
		
		
		dataSetGroupMap.patternize(PatternMode.Sequence, 3, exclusions, acceptableDataSequence, twoDifferentTreesSequence);

		
//		dataSetGroupMap.patternize(PatternMode.Homogenize, 3, exclusions);

		
		
		DataSet correctedDataSet = dataSetGroupMap.recomposeDataSet();

		String exportCorrectedFilename = ObjectUtility.getPackagePath(DataSet.class).replace("bin", "src") + "corrected.csv";
		correctedDataSet.save(exportCorrectedFilename);
//		new FakeDialog(dataSet.getUI());
		System.exit(0);
	}


}
