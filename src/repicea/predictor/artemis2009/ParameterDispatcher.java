/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2013 Mathieu Fortin for Rouge-Epicea
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
package repicea.predictor.artemis2009;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.util.Index;
import repicea.util.ObjectUtility;

public class ParameterDispatcher {
	
	private static final Set<String> SPECIES_FOR_TBE = new HashSet<String>();
	static {
		SPECIES_FOR_TBE.add("SAB");
		SPECIES_FOR_TBE.add("EPB");
		SPECIES_FOR_TBE.add("EPN");
		SPECIES_FOR_TBE.add("EPR");
		SPECIES_FOR_TBE.add("EPO");
		SPECIES_FOR_TBE.add("EPX");
	}

	private static ParameterDispatcher instance;
	
	private final ParameterMap beta;
	private final ParameterMap omega;
	private final ParameterMap covparms;
	private final ParameterMap effectID;
	private final Index<Integer, String> vegpotIndex;
	private final Index<Integer, String> speciesIndex;
	private final Index<Integer, String> speciesGroupIndex;
	private final Index<Integer, String> moduleIndex;
	private final Map<String, Map<String, String>> speciesMatches;
	private final Map<String, Map<String,Matrix>> dummySpeciesGroup;
	private final Map<String, List<String>> speciesGroupByVegPot;
	
	private ParameterDispatcher() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MatchModuleParameters.csv";
			String omegaFilename = path + "0_MatchModuleOmega.csv";
			String covparmsFilename = path + "0_MatchModuleCovparms.csv";
			String effectIDFilename = path + "0_MatchModuleEffects.csv";

			beta = ParameterLoader.loadVectorFromFile(2, betaFilename);
			omega = ParameterLoader.loadVectorFromFile(2, omegaFilename);
			covparms = ParameterLoader.loadVectorFromFile(2, covparmsFilename);
			effectID = ParameterLoader.loadVectorFromFile(2, effectIDFilename);

			String vegpotIndexFilename = path + "0_Vegpot.csv";
			String speciesIndexFilename = path + "0_Species.csv";
			String speciesGroupIndexFilename = path + "0_SpeciesGroups.csv";
			String moduleIndexFilename = path + "0_Modules.csv";

			vegpotIndex = getIndex(vegpotIndexFilename);
			speciesIndex = getIndex(speciesIndexFilename);
			speciesGroupIndex = getIndex(speciesGroupIndexFilename);
			moduleIndex = getIndex(moduleIndexFilename);

			String matchSpeciesGroupFilename = path + "0_MatchSpeciesGroups.csv";
			ParameterMap pm = ParameterLoader.loadVectorFromFile(2, matchSpeciesGroupFilename);

			speciesMatches = new HashMap<String, Map<String, String>>();
			dummySpeciesGroup = new HashMap<String, Map<String, Matrix>>();
			speciesGroupByVegPot = new HashMap<String, List<String>>();
			
			List<Integer> speciesGroupUnique = new ArrayList<Integer>();
			for (String vegpotName : vegpotIndex.values()) {
				speciesGroupUnique.clear();
				int vegpotID = vegpotIndex.getKeyForThisValue(vegpotName);
				Map<String, String> innerMap = new HashMap<String, String>();
				speciesMatches.put(vegpotName, innerMap);
				for (Integer speciesID : speciesIndex.keySet()) {
					String speciesName = speciesIndex.get(speciesID);
					int speciesGroupID = (int) pm.get(vegpotID, speciesID).m_afData[0][0];
					String speciesGroupName = speciesGroupIndex.get(speciesGroupID);
					innerMap.put(speciesName, speciesGroupName);
					if (!speciesGroupUnique.contains(speciesGroupID)) {
						speciesGroupUnique.add(speciesGroupID);
					}
				}

				Map<String, Matrix> innerDummyMap = new HashMap<String, Matrix>();
				dummySpeciesGroup.put(vegpotName, innerDummyMap);
				Collections.sort(speciesGroupUnique);
				
				List<String> speciesGroups = new ArrayList<String>();
				speciesGroupByVegPot.put(vegpotName, speciesGroups);

				for (Integer speciesGroupID : speciesGroupUnique) {
					Matrix dummy = new Matrix(1, speciesGroupUnique.size());
					innerDummyMap.put(speciesGroupIndex.get(speciesGroupID), dummy);
					dummy.m_afData[0][speciesGroupUnique.indexOf(speciesGroupID)] = 1d;
					speciesGroups.add(speciesGroupIndex.get(speciesGroupID));
				}
				
			}
		} catch (Exception e) {
			throw new InvalidParameterException("Unable to load the parameters in the ParameterDispatcher singleton");
		}
	}
	
	public static ParameterDispatcher getInstance() {
		if (instance == null) {
			instance = new ParameterDispatcher();
		}
		return instance;
	}
	
	private Index<Integer, String> getIndex(String filename) throws IOException {
		Index<Integer, String> index = new Index<Integer, String>();
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		int i;
		String value;
		while ((record = reader.nextRecord()) != null) {
			i = Integer.parseInt(record[0].toString());
			value = record[1].toString();
			index.put(i, value);
		}
		return index;
	}
	
	
	public Index<Integer, String> getVegpotIndex() {return vegpotIndex;}
	
	public Index<Integer, String> getModuleIndex() {return moduleIndex;} 
	
	public ParameterMap getParameters() {return beta;}
	
	public ParameterMap getCovarianceParameters() {return covparms;}
	
	public ParameterMap getCovarianceOfParameterEstimates() {return omega;}
	
	public ParameterMap getEffectID() {return effectID;}
	
	public String getSpeciesGroupName(Artemis2009CompatibleStand stand, String speciesName) {
		String initGroupingSpecies = getInitPrioriSpeciesGrouping(speciesName);
		return speciesMatches.get(stand.getPotentialVegetation()).get(initGroupingSpecies);
	}

	
	/**
	 * Get the initial priori grouping for oak, pine, spruce and poplar species
	 * @return the initial priori grouping for oak, pine, spruce and poplar species
	 */
	protected String getInitPrioriSpeciesGrouping(String iniSpeciesName){
		String result = iniSpeciesName;
		if(iniSpeciesName != null){		
			if (iniSpeciesName.startsWith("CH")) {
				result ="CHX";
			} else if (iniSpeciesName.startsWith("PE")) {
				result="PEU";
			} else if (iniSpeciesName.startsWith("EP")) {
				result="EPX";
			} else if (iniSpeciesName.equals("PIB") || iniSpeciesName.equals("PIR")) {
				result="PIN";
			}
		}
		return result;
	}

	/**
	 * This method returns true if this species is recognised by Artemis modules.
	 * @param speciesName a String (3-character code)
	 * @return a boolean
	 */
	public boolean isRecognizedSpecies(String speciesName) {
		return speciesIndex.containsValue(speciesName.trim().toUpperCase());
	}

	/**
	 * This method returns the species groups for a particular stand.
	 * @param stand an Artemis2009CompatibleStand instance
	 * @return a Set of Strings
	 */
	public List<String> getSpeciesGroups(Artemis2009CompatibleStand stand) {
		return speciesGroupByVegPot.get(stand.getPotentialVegetation());
	}
	
	protected void constructXVector(Matrix oXVector, Artemis2009CompatibleStand stand, Artemis2009CompatibleTree tree, String moduleName, List<Integer> effectList) {		
		oXVector.resetMatrix();
		
		int pointer = 0;
		Matrix dummyEssence = dummySpeciesGroup.get(stand.getPotentialVegetation()).get(tree.getSpeciesGroupName());

		oXVector.m_afData[0][0] = 1.0;
		pointer++;

		int dummyTBE = 0;
		int moduleID = moduleIndex.getKeyForThisValue(moduleName);
		if (stand.isGoingToBeDefoliated()) {
			if (moduleID == 2 || SPECIES_FOR_TBE.contains(tree.getSpeciesGroupName())) {
				dummyTBE = 1;
			}
		}
//		if (moduleID == 2) {
//			if (stand.isGoingToBeDefoliated()) {		// the dummyTBE affects all the species in the diameter growth model
//				dummyTBE = 1;
//			}
//		} else {
//			if (stand.isGoingToBeDefoliated() && SPECIES_FOR_TBE.contains(tree.getSpeciesName())) {	// in this case dummyTBE applies only for vulnerable species
//				dummyTBE = 1;
//			}
//		}
//		
//		switch (moduleID) {
//		case 1:
//		case 3:
//		case 4:
//		case 5:
////			dummyEssence = getSettings().getSpeciesGroupDummyMap().get(stand.getPotentialVegetationID()).get(tree.getSpecies().getValue());
////			oXVector.m_afData[0][0] = 1.0;
////			pointer = 1;
//			if (stand.isGoingToBeDefoliated() && SPECIES_FOR_TBE.contains(tree.getSpeciesName())) {	// in this case dummyTBE applies only for vulnerable species
//				dummyTBE = 1;
//			}
//			break;
//		case 2:
////			dummyEssence = getSettings().getSpeciesGroupDummyMap().get(stand.getPotentialVegetationID()).get(tree.getSpecies().getValue());
////			oXVector.m_afData[0][0] = 1.0;
////			pointer = 1;
//			if (stand.isGoingToBeDefoliated()) {		// the dummyTBE affects all the species in the diameter growth model
//				dummyTBE = 1;
//			}
//			break;
//		}	

		double fTmp = 0.0;

		for (Integer iCase : effectList) {
			//m_oCaseCounter[iCase]++;
			// cases have been ordered from most frequent to least one based on dataset VPTest and a 10-step simulation (optimizing)
			switch(iCase) {
			case 31: // 38 799 occurences
				oXVector.m_afData[0][pointer] = Math.log(stand.getTimeStepYr());
				pointer ++;
				break;
			case 14: // 36 918 occurences
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++)
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii];
				pointer += dummyEssence.m_iCols;
				break;
			case 12: // 27 854 occurences
				fTmp = tree.getDbhCm();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii]*fTmp;
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 3: // 22 091 occurences
				oXVector.m_afData[0][pointer] = 0.0;

				if (stand.isInterventionResult()) {
					if (!stand.isInitialStand() || moduleID != 2) {		// 2 : diameter growth module 
						oXVector.m_afData[0][pointer] = 1.0;
					}
				} 

				pointer ++;
				break;
			case 45: // 17 833 occurences
				oXVector.m_afData[0][pointer] = stand.getBasalAreaM2Ha();
				pointer ++;
				break;
			case 11: // 14 213 occurences
				oXVector.m_afData[0][pointer] = tree.getSquaredDbhCm();
				pointer ++;
				break;
			case 49: // 12 283 occurences
				oXVector.m_afData[0][pointer] = dummyTBE;
				pointer ++;
				break;
			case 44: // 10 557 occurences
				fTmp = tree.getBasalAreaLargerThanSubjectM2Ha();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii]*fTmp;
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 41: // 10 469 occurences
				oXVector.m_afData[0][pointer] = stand.getMeanAnnualPrecipitationMm();
				pointer ++;
				break;
			case 34: // 7436 occurences
				double logdt_cc = 0;
				if (stand.getDateYr() <= 1994) {
					logdt_cc = Math.log(1995 - stand.getDateYr());		// changed for stand.getDate() TODO check if this works properly
					//				if (getSettings().getInitialSimulationYear() <= 1994) {
					//					logdt_cc = Math.log(1995 - getSettings().getInitialSimulationYear());
				}
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii]*logdt_cc;
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 33: // 6420 occurences
				fTmp = (Math.log(stand.getTimeStepYr())*dummyTBE);
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii]*fTmp;
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 17: // 6016 occurences
				oXVector.m_afData[0][pointer] = tree.getLnDbhCm();
				pointer ++;
				break;
			case 18: // 5805 occurences
				fTmp = tree.getLnDbhCm();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii]*fTmp;
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 32: // 5600 occurences
				oXVector.m_afData[0][pointer] = Math.log(stand.getTimeStepYr())*dummyTBE;
				pointer ++;
				break;
			case 10: // 5340 occurences
				oXVector.m_afData[0][pointer] = tree.getDbhCm();
				pointer ++;
				break;
			case 47: // 5260 occurences
				fTmp = stand.getBasalAreaM2Ha();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++)
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii]*fTmp;
				pointer += dummyEssence.m_iCols;
				break;
			case 43: // 4889 occurences
				oXVector.m_afData[0][pointer] = tree.getBasalAreaLargerThanSubjectM2Ha();
				pointer ++;
				break;
			case 51: // 3700 occurences
				fTmp = (stand.getMeanAnnualTemperatureC());
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++)
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii]*fTmp;
				pointer += dummyEssence.m_iCols;
				break;
			case 36: // 2815 occurences
				oXVector.m_afData[0][pointer] = stand.getNumberOfStemsHa() * stand.getAreaHa();
				pointer ++;
				break;
			case 42: // 2366 occurences
				fTmp = (stand.getMeanAnnualPrecipitationMm());
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++)
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii]*fTmp;
				pointer += dummyEssence.m_iCols;
				break;
			case 38: // 2150 occurences
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++)
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii]*stand.getNumberOfStemsBySpeciesGroup().m_afData[0][ii];
				pointer += dummyEssence.m_iCols;
				break;
			case 50: // 1650 occurences
				oXVector.m_afData[0][pointer] = stand.getMeanAnnualTemperatureC();
				pointer ++;
				break;
			case 13: // 1181 occurences
				int dummySAB = 0; 
				if (tree.getSpeciesGroupName().compareTo("SAB") == 0) {
					dummySAB = 1;
				}
				oXVector.m_afData[0][pointer] = dummySAB * stand.getMeanAnnualPrecipitationMm();
				pointer ++;
				break;
			case 2: // 1006 occurences
				//if (stand.isCutPreviousStep()) {			// this effect no longer applies because the cut is always done immediately after the measurement MF2011-04-24
				//oXVector.m_afData[0][pointeur] = 1.0;
				//} else {
				oXVector.m_afData[0][pointer] = 0.0;
				//}
				pointer ++;
				break;
			case 4: // 995 occurences
				int currentCut = 0;
				if (stand.isInterventionResult()) {
					if (!stand.isInitialStand() || moduleID != 2) {		// 2: diameter growth module id
						currentCut = 1;
					}
				}
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii] * currentCut;
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 35: // 450 occurences
				oXVector.m_afData[0][pointer] = stand.getMeanQuadraticDiameterCm();
				pointer ++;
				break;
			case 48: // 340 occurences
				oXVector.m_afData[0][pointer] = (dummyEssence.multiply(stand.getNumberOfStemsBySpeciesGroup().transpose())).m_afData[0][0]*stand.getBasalAreaM2Ha();
				pointer ++;
				break;
			case 37: // 70 occurences
				oXVector.m_afData[0][pointer] = dummyEssence.multiply(stand.getNumberOfStemsBySpeciesGroup().transpose()).m_afData[0][0];
				pointer ++;
				break;
			case 54:	// 60 occurences
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++)
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii] * stand.getBasalAreaBySpeciesGroup().m_afData[0][ii];
				pointer += dummyEssence.m_iCols;
				break;


			case 39: // 0 occurence
				oXVector.m_afData[0][pointer] = (dummyEssence.multiply(stand.getNumberOfStemsBySpeciesGroup().transpose())).m_afData[0][0]*stand.getBasalAreaM2Ha();
				pointer ++;
				break;
			case 52: // 0 occurence
				oXVector.m_afData[0][pointer] = stand.getLatitude();
				pointer ++;
				break;
			case 53: // 0 occurence
				fTmp = (stand.getLatitude());
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii]*fTmp;
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 1: // 0 occurence
				fTmp = stand.getElevationM();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++)
					oXVector.m_afData[0][ii + pointer] = dummyEssence.m_afData[0][ii]*fTmp;
				pointer += dummyEssence.m_iCols;
				break;


			}

		}

		oXVector.m_iCols = pointer;
	}

	/**
	 * This method returns either the basal area or the number of stems contained in the
	 * collection trees. IMPORTANT: Values are not reported per hectare.
	 * @param stand a Artemis2009CompatibleStand instance
	 * @param trees a Collection of Artemis2009CompatibleTree instances
	 * @param G a boolean (true to calculate the basal area or false for the number of stems)
	 * @return a Matrix instance
	 */
	public static Matrix getGroupEssGorN(Artemis2009CompatibleStand stand, Collection<? extends Artemis2009CompatibleTree> trees, boolean G) {
		List<String> speciesGroups = ParameterDispatcher.getInstance().getSpeciesGroups(stand);
		Matrix oVector = new Matrix(1, speciesGroups.size());
 
		if ((trees == null)||(trees.isEmpty ())) {
			return oVector;
		} else {
			for (Artemis2009CompatibleTree t : trees) {
				if (t.getNumber() > 0) {
					int pointeur = speciesGroups.indexOf(t.getSpeciesGroupName());
					if (G) {
						oVector.m_afData[0][pointeur] += t.getStemBasalAreaM2() * t.getNumber();
					}
					else {
						oVector.m_afData[0][pointeur] += t.getNumber();
					}
				}
			}
			return oVector;
		}
	}

	
	/**
	 * For testing
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ParameterDispatcher.getInstance();
	}
	
	
	
}

