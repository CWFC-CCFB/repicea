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

class QuebecPEPFormatting {

	static int ManuallyRenumberFrom(DataSetGroupMap dataSetGroupMap, DataGroup group, int index) {
		String fieldName = "NO_ARBRE";
		DataSet ds = dataSetGroupMap.get(group);	
		for (int i = ds.getNumberOfObservations() - 1; i >= 0; i--) {
			Double currentValue = (Double) ds.getValueAt(i, fieldName);
			if (i >= index) {
				ds.correctValue(i, fieldName, currentValue + 1000d, "manually renumbered", true, "status = C");
			}
		}
		return ds.getNumberOfObservations();
	}

	static int AcceptedAsIs(DataSetGroupMap dataSetGroupMap, DataGroup group) {
		String fieldName = "ETAT";
		DataSet ds = dataSetGroupMap.get(group);	
		Object currentValue = ds.getValueAt(0, fieldName);
		ds.correctValue(0, fieldName, currentValue, "accepted as is", true, "status = C");
		return ds.getNumberOfObservations();
	}
	
	static int ReplaceFirstStatusBy(DataSetGroupMap dataSetGroupMap, DataGroup group, double newStatus) {
		return ReplaceThisStatusBy(dataSetGroupMap, group, 0, newStatus, "dead status manually changed for alive");
	}

	static int ReplaceThisStatusBy(DataSetGroupMap dataSetGroupMap, DataGroup group, int index, double newStatus, String message) {
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
		return dataSetGroupMap;
	}
	
	
	public static void main(String[] args) throws IOException {
		String filename = ObjectUtility.getPackagePath(DataSet.class) + "trees.csv";
		DataSet dataSet = new DataSet(filename, false);
		new REpiceaProgressBarDialog("Reading inventory", "...", dataSet, false);

		DataSetGroupMap dataSetGroupMap = CheckStatus(dataSet);
		// for species
		
		int nbManuallyChanged = 0;
		
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(312.0, 4.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(446.0, 23.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(746.0, 64.0), 30.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(985.0, 2.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(1099.0, 14.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(1325.0, 67.0), 30.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(1605.0, 127.0), 40.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(1864.0, 47.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(1938.0, 16.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2086.0, 42.0), 40.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2126.0, 62.0), 40.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2427.0, 5.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2500.0, 45.0), 40.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2508.0, 36.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2755.0, 73.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(2872.0, 11.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3099.0, 6.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3320.0, 9.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3468.0, 13.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3490.0, 17.0), 40.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3490.0, 20.0), 40.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3647.0, 68.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3655.0, 7.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3784.0, 28.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3810.0, 51.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3847.0, 1.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3855.0, 7.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3957.0, 55.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(3987.0, 9.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4010.0, 13.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4078.0, 107.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4079.0, 3.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4327.0, 25.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4700.0, 17.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4791.0, 20.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(4810.0, 26.0), 40.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(5143.0, 55.0), 30.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(5303.0, 4.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(5444.0, 21.0), 40.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(5632.0, 36.0), 30.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(6454.0, 37.0), 40.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(6559.0, 23.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(6717.0, 44.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(6895.0, 44.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7011.0, 53.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7116.0, 7.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7301.0, 34.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7306.0, 7.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7453.0, 13.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(7481.0, 7.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(8047.0, 60.0), 50.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(8048.0, 39.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(8152.0, 13.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9128.0, 1.0), 40.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9208.0, 5.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9481.0, 14.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9550.0, 51.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9582.0, 28.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9585.0, 6.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(9957.0, 42.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10078.0, 39.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10146.0, 26.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10323.0, 8.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10780.0, 14.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845.0, 2.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845.0, 3.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845.0, 15.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845.0, 16.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845.0, 17.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845.0, 19.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845.0, 20.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845.0, 21.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(10845.0, 22.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(11014.0, 1.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(11040.0, 31.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(11516.0, 22.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(11925.0, 31.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(12156.0, 35.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(12339.0, 3.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(12339.0, 5.0), 10.0);
		nbManuallyChanged += ReplaceFirstStatusBy(dataSetGroupMap, new DataGroup(12492.0, 94.0), 40.0);

		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(387.0, 29.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(808.0, 2.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(1086.0, 99.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(1235.0, 32.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(1878.0, 46.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(2181.0, 37.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(2267.0, 30.0), 3);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(2267.0, 31.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(2874.0, 1.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3970.0, 41.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3970.0, 42.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3970.0, 43.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3971.0, 87.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3971.0, 88.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(3971.0, 89.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(5189.0, 24.0), 3);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(5258.0, 30.0), 3);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(5635.0, 19.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(5825.0, 27.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(6378.0, 2.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(6378.0, 3.0), 2);
		nbManuallyChanged += ManuallyRenumberFrom(dataSetGroupMap, new DataGroup(7778.0, 22.0), 2);

		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(443.0, 42.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(1085.0, 57.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(1660.0, 63.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(1925.0, 30.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(2295.0, 9.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5257.0, 3.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5257.0, 5.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5257.0, 9.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5257.0, 16.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5258.0, 8.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5258.0, 15.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5258.0, 19.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5258.0, 31.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5258.0, 37.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(5995.0, 88.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(6005.0, 41.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(6009.0, 82.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 11.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 23.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 35.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 42.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 52.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 57.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 59.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 64.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 65.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 77.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 81.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 89.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 101.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 106.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 107.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7292.0, 108.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293.0, 1.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293.0, 9.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293.0, 11.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293.0, 18.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293.0, 36.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293.0, 37.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293.0, 39.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7293.0, 41.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7588.0, 8.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7588.0, 24.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(7588.0, 41.0));
		nbManuallyChanged += AcceptedAsIs(dataSetGroupMap, new DataGroup(11073.0, 9.0));


		
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(1614.0, 2.0), 2, 10, "harvested status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(1616.0, 25.0), 2, 10, "harvested status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(2294.0, 44.0), 1, 10, "dead status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(2422.0, 18.0), 2, 10, "harvested status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3086.0, 24.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3718.0, 66.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3754.0, 10.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3762.0, 86.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3762.0, 87.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3811.0, 26.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3811.0, 27.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3811.0, 28.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840.0, 10.0), 2, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840.0, 11.0), 2, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840.0, 12.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840.0, 13.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840.0, 14.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840.0, 15.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3840.0, 16.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3941.0, 49.0), 1, 10, "intruder status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(3941.0, 5.0), 1, 10, "intruder status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4139.0, 42.0), 2, 10, "missing status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4679.0, 5.0), 3, 10, "forgotten status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4679.0, 7.0), 3, 10, "forgotten status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4679.0, 9.0), 1, 10, "forgotten status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4679.0, 10.0), 1, 10, "forgotten status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(4786.0, 32.0), 1, 10, "missing status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5218.0, 25.0), 1, 10, "intruder status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5252.0, 43.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5258.0, 41.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5258.0, 42.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5258.0, 43.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5258.0, 44.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5258.0, 45.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(5698.0, 23.0), 1, 10, "forgotten status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(6834.0, 8.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(6846.0, 17.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(7293.0, 44.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(7293.0, 45.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(7293.0, 46.0), 1, 10, "recruit status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(12029.0, 21.0), 2, 10, "harvest status manually changed for alive");
		nbManuallyChanged += ReplaceThisStatusBy(dataSetGroupMap, new DataGroup(12276.0, 29.0), 0, 10, "harvest status manually changed for alive");


		DataSet ds;
		ds = dataSetGroupMap.get(new DataGroup(1418.0, 18.0));			
		ds.correctValue(2, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds.correctValue(1, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(4203.0, 28.0));			
		ds.correctValue(1, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds.correctValue(2, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(4538.0, 20.0));			
		ds.correctValue(1, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds.correctValue(2, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(6137.0, 20.0));			
		ds.correctValue(2, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds.correctValue(1, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();
		ds = dataSetGroupMap.get(new DataGroup(9837.0, 4.0));			
		ds.correctValue(1, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds.correctValue(0, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();

		ds = dataSetGroupMap.get(new DataGroup(11859.0, 50.0));			
		ds.correctValue(2, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		ds.correctValue(1, "ETAT", 10.0, "dead status manually changed for alive", true, "status = C");
		nbManuallyChanged += ds.getNumberOfObservations();

		System.out.println("Number of observations manually changed = " + nbManuallyChanged);
		
		String exportCorrectedFilename = ObjectUtility.getPackagePath(DataSet.class).replace("bin", "test") + "corrected.csv";
		dataSet.save(exportCorrectedFilename);
		
		
		dataSetGroupMap = CheckStatus(dataSet);

		
		
		
//		dataSetGroupMap.patternize(PatternMode.Homogenize, 3, exclusions);

		
		
//		DataSet correctedDataSet = dataSetGroupMap.recomposeDataSet();

		exportCorrectedFilename = ObjectUtility.getPackagePath(DataSet.class).replace("bin", "test") + "corrected2.csv";
		dataSet.save(exportCorrectedFilename);
//		new FakeDialog(dataSet.getUI());
		System.exit(0);
	}


}
