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
import repicea.util.ObjectUtility;

class QuebecPEPFormatting {

	static int ManuallyRenumberFrom(DataSetGroupMap dataSetGroupMap, DataGroup group, int index) {
		String fieldName = "NO_ARBRE";
		DataSet ds = dataSetGroupMap.get(group);	
		for (int i = ds.getNumberOfObservations() - 1; i >= 0; i--) {
			Integer currentValue = (Integer) ds.getValueAt(i, fieldName);
			if (i >= index) {
				ds.correctValue(i, fieldName, currentValue + 1000, "manually renumbered", true, "status = C");
			}
		}
		return ds.getNumberOfObservations();
	}

	static int AcceptedAsIs(DataSetGroupMap dataSetGroupMap, DataGroup group) {
		String fieldName = "ETAT";
		DataSet ds = dataSetGroupMap.get(group);	
		Object currentValue = ds.getValueAt(0, fieldName);
		ds.correctValue(0, fieldName, currentValue, "accepted as is", true, "accepted as is");
		return ds.getNumberOfObservations();
	}
	
	static int ReplaceFirstStatusBy(DataSetGroupMap dataSetGroupMap, DataGroup group, Object newStatus) {
		return ReplaceThisStatusBy(dataSetGroupMap, group, 0, newStatus, "dead status manually changed for alive");
	}

	static int ReplaceThisStatusBy(DataSetGroupMap dataSetGroupMap, DataGroup group, int index, Object newStatus, String message) {
		String fieldName = "ETAT";
		DataSet ds = dataSetGroupMap.get(group);	
		ds.correctValue(index, fieldName, newStatus, message, true, "status = C");
		return ds.getNumberOfObservations();
	}

	
	private static DataSetGroupMap CheckStatus(DataSet dataSet) {
		List<Object> exclusions = new ArrayList<Object>();
		exclusions.add("NA");
		
		List<Integer> fieldsForSplitting = new ArrayList<Integer>();
		fieldsForSplitting.add(1);	// field i
		fieldsForSplitting.add(3);	// field NO_ARBRE
		List<Integer> fieldsForSorting = new ArrayList<Integer>();
		fieldsForSorting.add(2);	// field year
		DataSetGroupMap dataSetGroupMap = dataSet.splitAndOrder(fieldsForSplitting, fieldsForSorting);
		
		
		List<Object> terminalStatuses = new ArrayList<Object>();
		terminalStatuses.add("23");
		terminalStatuses.add("24");
		terminalStatuses.add("25");
		terminalStatuses.add("26");
		terminalStatuses.add("29");
		List<Object> deadStatuses = new ArrayList<Object>();
		deadStatuses.add("14");
		deadStatuses.add("16");
		List<Object> forgottenDeadStatuses = new ArrayList<Object>();
		deadStatuses.add("34");
		deadStatuses.add("36");
		List<Object> recruitDeadStatuses = new ArrayList<Object>();
		deadStatuses.add("44");
		deadStatuses.add("46");
		List<Object> renumberedDeadStatuses = new ArrayList<Object>();
		deadStatuses.add("54");
		deadStatuses.add("56");
		List<Object> aliveStatuses = new ArrayList<Object>();
		aliveStatuses.add("10");
		aliveStatuses.add("12");
		List<Object> forgottenStatuses = new ArrayList<Object>();
		forgottenStatuses.add("30");
		forgottenStatuses.add("32");
		List<Object> recruitStatuses = new ArrayList<Object>();
		recruitStatuses.add("40");
		recruitStatuses.add("42");
		List<Object> renumberedStatuses = new ArrayList<Object>();
		renumberedStatuses.add("50");
		renumberedStatuses.add("52");
		
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
					if (i >= observationIndex + 1) {
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
						pattern.updateField(i, "ETAT", "10", ActionType.Replace);
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
			oMap.put("10", DataSequence.convertListToMap(possibleOutcomes));
			measurementErrorSequence1.put(obj, oMap);
		}

		action = new ActionOnPattern() {
			@Override
			protected void doAction(DataPattern pattern, Object... parms) {
				int observationIndex = (Integer) parms[0];
				for (int i = 0; i < pattern.size(); i++) {
					if (i == observationIndex + 1) {
						pattern.updateField(i, "ETAT", "10", ActionType.Replace);
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
			possibleOutcomes.add("10");
			Map<Object, Map> oMap = new HashMap<Object, Map>();
			oMap.put("14", DataSequence.convertListToMap(possibleOutcomes));
			measurementErrorSequence2.put(obj, oMap);
		}


		List<DataSequence> sequences = new ArrayList<DataSequence>();
		sequences.add(acceptableDataSequence);
		sequences.add(twoDifferentTreesSequence);
		sequences.add(measurementErrorSequence1);
		sequences.add(measurementErrorSequence2);
		
		dataSetGroupMap.patternize("ETAT", exclusions, sequences);
		return dataSetGroupMap;
	}
	
	
	private static void StatusCorrection() throws IOException {
		String filename = ObjectUtility.getPackagePath(DataSet.class).replace("bin", "test") + "trees.csv";
		DataSet dataSet = new DataSet(filename, false);
		new REpiceaProgressBarDialog("Reading inventory", "...", dataSet, false);

		DataSetGroupMap dataSetGroupMap = CheckStatus(dataSet);
		
		int nbManuallyChanged = 0;
		
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(312, 4), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(446, 23), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(746, 64), "30");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(1099, 14), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(1325, 67), "30");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(1605, 127), "40");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(1864, 47), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(1938, 16), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2086, 42), "40");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2126, 62), "40");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2427, 5), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2500, 45), "40");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2508, 36), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2755, 73), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2872, 11), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3099, 6), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3320, 9), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3468, 13), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3490, 17), "40");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3490, 20), "40");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3647, 68), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3655, 7), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3784, 28), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3810, 51), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3847, 1), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3855, 7), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3957, 55), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3987, 9), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4010, 13), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4078, 107), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4079, 3), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4327, 25), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4700, 17), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4791, 20), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4810, 26), "40");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(5143, 55), "30");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(5303, 4), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(5444, 21), "40");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(5632, 36), "30");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(5995, 88), "40");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(6454, 37), "40");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(6559, 23), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(6717, 44), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(6895, 44), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7011, 53), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7116, 7), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7301, 34), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7306, 7), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7453, 13), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7481, 7), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(8047, 60), "50");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(8048, 39), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(8152, 13), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9128, 1), "40");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9208, 5), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9481, 14), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9550, 51), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9582, 28), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9585, 6), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9957, 42), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10078, 39), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10146, 26), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10323, 8), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10780, 14), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845, 2), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845, 3), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845, 15), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845, 16), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845, 17), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845, 19), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845, 20), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845, 21), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845, 22), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(11014, 1), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(11040, 31), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(11516, 22), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(11925, 31), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(12156, 35), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(12339, 3), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(12339, 5), "10");
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(12492, 94), "40");

		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(387, 29), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(808, 2), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(1086, 99), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(1235, 32), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(1878, 46), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(2181, 37), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(2267, 30), 3);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(2267, 31), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(2874, 1), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3970, 41), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3970, 42), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3970, 43), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3971, 87), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3971, 88), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3971, 89), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(5189, 24), 3);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(5258, 30), 3);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(5635, 19), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(5825, 27), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(6378, 2), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(6378, 3), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(7778, 22), 2);

		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(1660, 63));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(1925, 30));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(2295, 9));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5257, 3));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5257, 5));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5257, 9));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5257, 16));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5258, 8));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5258, 15));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5258, 19));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5258, 31));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5258, 37));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(6005, 41));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 11));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 23));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 35));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 42));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 52));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 57));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 59));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 64));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 65));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 77));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 81));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 89));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 101));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 106));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 107));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292, 108));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293, 1));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293, 9));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293, 11));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293, 18));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293, 36));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293, 37));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293, 39));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293, 41));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7588, 8));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7588, 24));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7588, 41));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(11073, 9));


		
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(443, 42), 1, "10", "disapperead status manually replaced by alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(1085, 57), 1, "10", "disapperead status manually replaced by alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(1614, 2), 2, "10", "harvested status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(1616, 25), 2, "10", "harvested status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(2294, 44), 1, "10", "dead status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(2422, 18), 2, "10", "harvested status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3086, 24), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3718, 66), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3754, 10), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3762, 86), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3762, 87), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3811, 26), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3811, 27), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3811, 28), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840, 10), 2, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840, 11), 2, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840, 12), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840, 13), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840, 14), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840, 15), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840, 16), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4139, 42), 2, "10", "missing status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4679, 5), 3, "10", "forgotten status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4679, 7), 3, "10", "forgotten status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4679, 9), 1, "10", "forgotten status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4679, 10), 1, "10", "forgotten status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4786, 32), 1, "10", "missing status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5218, 25), 1, "10", "intruder status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5252, 43), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5258, 41), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5258, 42), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5258, 43), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5258, 44), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5258, 45), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5698, 23), 1, "10", "forgotten status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(6009, 82), 1, "10", "forgotten status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(6834, 8), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(6846, 17), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(7293, 44), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(7293, 45), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(7293, 46), 1, "10", "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(12029, 21), 2, "10", "harvest status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(12276, 29), 0, "10", "harvest status manually changed for alive");

		DataSet ds;
		ds = dataSetGroupMap.get(new DataGroup(985, 2));			
		ds.correctValue(1, "ETAT", "10", "disappeared status manually changed for alive", true, "status = C");
		ds.correctValue(0, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(1418, 18));			
		ds.correctValue(2, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		ds.correctValue(1, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(3941, 5));			
		ds.correctValue(2, "ETAT", "10", "forgotten status manually changed for alive", true, "status = C");
		ds.correctValue(1, "ETAT", "10", "intruder status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(3941, 49));			
		ds.correctValue(2, "ETAT", "10", "forgotten status manually changed for alive", true, "status = C");
		ds.correctValue(1, "ETAT", "10", "intruder status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(4203, 28));			
		ds.correctValue(1, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		ds.correctValue(2, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(4538, 20));			
		ds.correctValue(1, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		ds.correctValue(2, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(6137, 20));			
		ds.correctValue(2, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		ds.correctValue(1, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		ds.correctValue(0, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(9837, 4));			
		ds.correctValue(1, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		ds.correctValue(0, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(11859, 50));			
		ds.correctValue(2, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		ds.correctValue(1, "ETAT", "10", "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();

		System.out.println("Number of observations manually changed = " + nbManuallyChanged);
		
		dataSetGroupMap = CheckStatus(dataSet);
		
		nbManuallyChanged = 0;
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(11857, 7), "10");	// this one is manually changed in the first place
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5698, 23));
		System.out.println("Number of observations manually changed = " + nbManuallyChanged);

		int index = dataSet.fieldNames.indexOf(DataPattern.JavaComments);
		dataSet.fieldNames.set(index, DataPattern.JavaComments.concat("Status"));
		
		String exportCorrectedFilename = ObjectUtility.getPackagePath(DataSet.class).replace("bin", "test") + "statusCorrected.csv";
		dataSet.save(exportCorrectedFilename);
		
		System.exit(0);
		
//		dataSetGroupMap.patternize(PatternMode.Homogenize, 3, exclusions);

	}

	private static void SpeciesCorrection() throws IOException {
		String filename = ObjectUtility.getPackagePath(DataSet.class).replace("bin", "test") + "statusCorrected.csv";
		DataSet dataSet = new DataSet(filename, false);
		new REpiceaProgressBarDialog("Reading inventory", "...", dataSet, false);

		CheckSpecies(dataSet);
		
		String exportCorrectedFilename = ObjectUtility.getPackagePath(DataSet.class).replace("bin", "test") + "speciesCorrected.csv";
		dataSet.save(exportCorrectedFilename);
		
		System.exit(0);
		

		
	}
	
	
	private static DataSetGroupMap CheckSpecies(DataSet dataSet) {
		List<Object> exclusions = new ArrayList<Object>();
		exclusions.add("NA");
		
		List<Integer> fieldsForSplitting = new ArrayList<Integer>();
		fieldsForSplitting.add(1);	// field i
		fieldsForSplitting.add(3);	// field NO_ARBRE
		List<Integer> fieldsForSorting = new ArrayList<Integer>();
		fieldsForSorting.add(2);	// field year
		DataSetGroupMap dataSetGroupMap = dataSet.splitAndOrder(fieldsForSplitting, fieldsForSorting);
		
		ActionOnPattern action = new ActionOnPattern() {
			@Override
			protected void doAction(DataPattern pattern, Object... parms) {
				Object species = parms[0];
				for (int i = 0; i < pattern.size(); i++) {
					if (!pattern.get(i).equals(species)) {
						pattern.updateField(i, "ESSENCE", species, ActionType.Replace);
						pattern.comment(i, "species set according to homogeneous method");
					}
				}
			}

		};
		
		DataHomogeneousSequence homogeneousSequence = new DataHomogeneousSequence("Homogeneous", action) {
			@Override
			protected Object doesPartOfPatternFitThisSequence(DataPattern pattern, List<Object> exclusions) {
				List<Object> clone = pattern.getCleanPattern(exclusions);
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
		};
		
		action = new ActionOnPattern() {
			@Override
			protected void doAction(DataPattern pattern, Object... parms) {
				Object species = parms[0];
				for (int i = 0; i < pattern.size(); i++) {
					if (!pattern.get(i).equals(species)) {
						pattern.updateField(i, "ESSENCE", species, ActionType.Replace);
						pattern.comment(i, "species set to according to emerging method");
					}
				}
			}

		};

		DataHomogeneousSequence emergingObjectSequence = new DataHomogeneousSequence("Emerging object", action) {
			@Override
			protected Object doesPartOfPatternFitThisSequence(DataPattern pattern, List<Object> exclusions) {
				DataPattern clone = pattern.getCleanPattern(exclusions);
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
		};
		
		action = new ActionOnPattern() {
			@Override
			protected void doAction(DataPattern pattern, Object... parms) {
				Object species = parms[0];
				for (int i = 0; i < pattern.size(); i++) {
					if (!pattern.get(i).equals(species)) {
						pattern.updateField(i, "ESSENCE", species, ActionType.Replace);
						pattern.comment(i, "species set according to last-but-similar method");
					}
				}
			}

		};
		DataHomogeneousSequence lastButSimilarSequence = new DataHomogeneousSequence("Last but similar", action) {
			@Override
			protected Object doesPartOfPatternFitThisSequence(DataPattern pattern, List<Object> exclusions) {
				DataPattern clone = pattern.getCleanPattern(exclusions);
				DataPattern subPattern = clone.getSubDataPattern(0, 2);
				
				if (subPattern.isEmpty()) {
					return null;
				} else if (subPattern.size() == 1) {
					return clone.get(0);
				} else {
					for (int i = 1; i < subPattern.size(); i++) {
						if (!subPattern.get(i).equals(subPattern.get(i - 1))) {
							return null;
						}
					}
					return clone.get(0);
				}
			}
		};

		action = new ActionOnPattern() {
			@Override
			protected void doAction(DataPattern pattern, Object... parms) {
				Object species = parms[0];
				for (int i = 0; i < pattern.size(); i++) {
					if (!pattern.get(i).equals(species)) {
						pattern.updateField(i, "ESSENCE", species, ActionType.Replace);
						pattern.comment(i, "species set according to last-in-sequence method");
					}
				}
			}

		};		DataHomogeneousSequence lastInSequence = new DataHomogeneousSequence("Last in sequence", action) {
			@Override
			protected Object doesPartOfPatternFitThisSequence(DataPattern pattern, List<Object> exclusions) {
				DataPattern clone = pattern.getCleanPattern(exclusions);
				if (clone.size() > 0) {
					return clone.get(clone.size() - 1);
				} else {
					return null;
				}
			}
		};
		
		
		
		List<DataSequence> sequences = new ArrayList<DataSequence>();
		sequences.add(homogeneousSequence);
		sequences.add(emergingObjectSequence);
		sequences.add(lastButSimilarSequence);
		sequences.add(lastInSequence);
		
		dataSetGroupMap.patternize("ESSENCE", exclusions, sequences);
		
		
		return dataSetGroupMap;
	}

	public static void main(String[] args) throws IOException {
//		StatusCorrection();
		SpeciesCorrection();
	}

}
