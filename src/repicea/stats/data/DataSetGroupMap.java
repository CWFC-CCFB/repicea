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

public class DataSetGroupMap extends HashMap<DataGroup, DataSet> { 

	protected static enum PatternMode {
		Homogenize,
		Sequence;
	}

	protected final DataSet originalDataSet;
	
	protected DataSetGroupMap(DataSet originalDataSet) {
		this.originalDataSet = originalDataSet;
	}
	
	public DataPatternMap getPatternAbundance(int fieldIndexForPattern) {
		DataPatternMap patternMap = new DataPatternMap(this);
		for (DataGroup id : keySet()) {
			DataSet ds = get(id);
			DataPattern pattern = new DataPattern(fieldIndexForPattern, patternMap);
			for (Observation obs : ds.observations) {
				pattern.add(obs.values.get(fieldIndexForPattern));
			}
			if (!patternMap.containsKey(pattern)) {
				patternMap.put(pattern, new ArrayList<DataGroup>());
			}
			List<DataGroup> census = patternMap.get(pattern);
			census.add(id);
		}
		return patternMap;
	}
	
	
	protected void addCorrectedField(PatternMode mode,
			List<DataGroup> groups, 
			Object pattern, 
			String fieldName, 
			String correctionMethod) {
		for (DataGroup dg : groups) {
			DataSet ds = get(dg);
			Object[] field;
			if (mode == PatternMode.Homogenize) {
				field = DataPattern.getHomogeneousField(ds.getNumberOfObservations(), pattern);
				ds.addField(fieldName, field);
			}
			field = DataPattern.getHomogeneousField(ds.getNumberOfObservations(), correctionMethod);
			ds.addField(fieldName.concat("Met"), field);
		}
	}

	
	protected void homogenizePattern(List<DataPattern> unsolvedPatterns, 
			DataPatternMap patterns, 
			List<Object> exclusions,
			String corrFieldName) {
		for (DataPattern pattern : patterns.keySet()) {
			Object homogenenousPattern = pattern.getHomogeneousObject(exclusions);
			Object emergingWinner = null;
			Object lastButSimilar = null;
			Object last = null;
			if (homogenenousPattern != null) {	// test if they are homogeneous
				List<DataGroup> homogeneousGroups  = patterns.get(pattern);
				addCorrectedField(PatternMode.Homogenize, homogeneousGroups, homogenenousPattern, corrFieldName, "homogeneous");
				unsolvedPatterns.remove(pattern);
			} else if ((emergingWinner = pattern.getEmergingObject(exclusions)) != null) {
				List<DataGroup> emergingGroups  = patterns.get(pattern);
				addCorrectedField(PatternMode.Homogenize, emergingGroups, emergingWinner, corrFieldName, "emerging");
				unsolvedPatterns.remove(pattern);
			} else if ((lastButSimilar = pattern.getLastButSimilar(exclusions, 0, 2)) != null) {
				List<DataGroup> lastButSimilarGroups  = patterns.get(pattern);
				addCorrectedField(PatternMode.Homogenize, lastButSimilarGroups, lastButSimilar, corrFieldName, "lastButSimilar");
				unsolvedPatterns.remove(pattern);
			} else if ((last = pattern.getLastObject(exclusions)) != null) {
				List<DataGroup> lastGroups  = patterns.get(pattern);
				addCorrectedField(PatternMode.Homogenize, lastGroups, last, corrFieldName, "last");
				unsolvedPatterns.remove(pattern);
			} else {
				List<DataGroup> notSetGroups  = patterns.get(pattern);
				addCorrectedField(PatternMode.Homogenize, notSetGroups, "unknown", corrFieldName, "unknown");
			}
		}

	}
	
	
	protected void patternize(PatternMode mode, int indexFieldPattern, List<Object> exclusions, Object...parms) {
		DataPatternMap patterns = getPatternAbundance(indexFieldPattern);

		List<DataPattern> unsolvedPatterns = new ArrayList<DataPattern>();
		unsolvedPatterns.addAll(patterns.keySet());
		
		if (mode == PatternMode.Homogenize) {
			homogenizePattern(unsolvedPatterns, patterns, exclusions, "speciesCorr");
		} else if (mode == PatternMode.Sequence) {
			DataSequence acceptedSequences = (DataSequence) parms[0];
			DataSequence twoTreesSequences = (DataSequence) parms[1];
			checkSequences(unsolvedPatterns, patterns, exclusions, acceptedSequences, twoTreesSequences);
		}
		
		for (DataPattern pattern : unsolvedPatterns) { 
				String outputStr = pattern.toString() + " - " + patterns.get(pattern).size() + " obs.";
				System.out.println(outputStr); 
		}
		
	}


	private void checkSequences(List<DataPattern> unsolvedPatterns, 
			Map<DataPattern, List<DataGroup>> patterns,
			List<Object> exclusions,
			DataSequence... sequences) {
		for (DataPattern pattern : patterns.keySet()) {
			boolean fit = false;
			for (DataSequence seq : sequences) {
				fit = seq.testPattern(pattern, exclusions);
				if (fit) {
					unsolvedPatterns.remove(pattern);
					break;
				}
			}
			
//			Integer observationIndex = null;
//			if (acceptableSequences.doesFitInThisSequence(pattern, exclusions, true)) {
//				pattern.comment("status = C");
//				unsolvedPatterns.remove(pattern);
//			} else if ((observationIndex = perhapsTwoTreesSequences.doesPartlyFitInThisSequence(pattern, exclusions)) != null) {
//				for (int i = 0; i < pattern.size(); i++) {
//					if (i >= observationIndex) {
//						pattern.updateField(i, "NO_ARBRE", 1000, Action.Add);
//						pattern.comment(i, "renumbered");
//					} else {
//						pattern.comment(i, "status = C");
//					}
//				}
////				List<DataGroup> groups  = patterns.get(pattern);
////				addOffsetToField("NO_ARBRE", observationIndex, 1000, groups);
////				addCorrectedField(PatternMode.Sequence, groups, observationIndex, "statusCorr", "renumberedTree");
//				unsolvedPatterns.remove(pattern);
//			} else {
			if (!fit) {
				pattern.comment("status = NC");
			}
//			}
		}
	}

	
}
