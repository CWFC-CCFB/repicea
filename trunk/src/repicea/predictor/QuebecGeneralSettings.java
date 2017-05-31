/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package repicea.predictor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.standlevel.QuebecForestRegionProvider.QuebecForestRegion;
import repicea.util.ObjectUtility;

/**
 * This class contains the general settings for Quebec context.
 * @author Mathieu Fortin - July 2014
 */
public class QuebecGeneralSettings {

	private final static Random RANDOM = new Random();
	
	private static final Map<String, Map<QuebecForestRegion, Double>> FOREST_REGION_MAP = new HashMap<String, Map<QuebecForestRegion, Double>>();
	static {
		String path = ObjectUtility.getRelativePackagePath(QuebecGeneralSettings.class);
		CSVReader reader = null;
		try {
			reader = new CSVReader(path + "correspondanceTable.csv");
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String regEco = record[0].toString();
				int regionCode = Integer.parseInt(record[1].toString());
				QuebecForestRegion forestRegion = QuebecForestRegion.getRegion(regionCode);
				double prob = Double.parseDouble(record[2].toString());
				if (!FOREST_REGION_MAP.containsKey(regEco)) {
					FOREST_REGION_MAP.put(regEco, new HashMap<QuebecForestRegion, Double>());
				}
				Map<QuebecForestRegion, Double> innerMap = FOREST_REGION_MAP.get(regEco);
				innerMap.put(forestRegion, prob);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	
	public static QuebecForestRegion getForestRegion(String ecologicalRegion, boolean stochastic) {
		Map<QuebecForestRegion, Double> forestRegionMap = FOREST_REGION_MAP.get(ecologicalRegion);
		double maxValue = 0;
		double sumValue = 0;
		double randomValue = RANDOM.nextDouble();
		QuebecForestRegion currentSelectedRegion = null;
		for (QuebecForestRegion region : forestRegionMap.keySet()) {
			double prob = forestRegionMap.get(region);
			sumValue += prob;
			if (prob > maxValue) {
				maxValue = prob;
				currentSelectedRegion = region;
			}
			if (stochastic & randomValue < sumValue) {
				return region;
			}
		}
		return currentSelectedRegion;
	}
	
	
	public static final Set<String> CLIMATIC_SUBDOMAIN_LIST = new HashSet<String>();
	static {
		CLIMATIC_SUBDOMAIN_LIST.add("1ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("2ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("2est");
		CLIMATIC_SUBDOMAIN_LIST.add("3ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("3est");
		CLIMATIC_SUBDOMAIN_LIST.add("4ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("4est");
		CLIMATIC_SUBDOMAIN_LIST.add("5ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("5est");
		CLIMATIC_SUBDOMAIN_LIST.add("6ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("6est");
	}

	public static Set<String> SHRUB_SPECIES_LIST = new HashSet<String>();
	static {
		SHRUB_SPECIES_LIST.add("ERG");
		SHRUB_SPECIES_LIST.add("ERP");
		SHRUB_SPECIES_LIST.add("ERE");
		SHRUB_SPECIES_LIST.add("AUC");
		SHRUB_SPECIES_LIST.add("AUR");
		SHRUB_SPECIES_LIST.add("AME");
		SHRUB_SPECIES_LIST.add("ARM");
		SHRUB_SPECIES_LIST.add("BEG");
		SHRUB_SPECIES_LIST.add("BEP");
		SHRUB_SPECIES_LIST.add("CAR");
		SHRUB_SPECIES_LIST.add("CEO");
		SHRUB_SPECIES_LIST.add("COP");
		SHRUB_SPECIES_LIST.add("COA");
		SHRUB_SPECIES_LIST.add("COR");
		SHRUB_SPECIES_LIST.add("COC");
		SHRUB_SPECIES_LIST.add("CRA");
		SHRUB_SPECIES_LIST.add("DIE");
		SHRUB_SPECIES_LIST.add("DIR");
		SHRUB_SPECIES_LIST.add("ILV");
		SHRUB_SPECIES_LIST.add("JUC");
		SHRUB_SPECIES_LIST.add("JUN");
		SHRUB_SPECIES_LIST.add("JUH");
		SHRUB_SPECIES_LIST.add("JUV");
		SHRUB_SPECIES_LIST.add("LON");
		SHRUB_SPECIES_LIST.add("LOH");
		SHRUB_SPECIES_LIST.add("LOV");
		SHRUB_SPECIES_LIST.add("MAS");
		SHRUB_SPECIES_LIST.add("MYG");
		SHRUB_SPECIES_LIST.add("NEM");
		SHRUB_SPECIES_LIST.add("PAQ");
		SHRUB_SPECIES_LIST.add("PRP");
		SHRUB_SPECIES_LIST.add("PRV");
		SHRUB_SPECIES_LIST.add("RHA");
		SHRUB_SPECIES_LIST.add("RHM");
		SHRUB_SPECIES_LIST.add("RHR");
		SHRUB_SPECIES_LIST.add("RHT");
		SHRUB_SPECIES_LIST.add("RIA");
		SHRUB_SPECIES_LIST.add("RIC");
		SHRUB_SPECIES_LIST.add("RIG");
		SHRUB_SPECIES_LIST.add("RIH");
		SHRUB_SPECIES_LIST.add("RIL");
		SHRUB_SPECIES_LIST.add("RIT");
		SHRUB_SPECIES_LIST.add("ROA");
		SHRUB_SPECIES_LIST.add("RUA");
		SHRUB_SPECIES_LIST.add("RUI");
		SHRUB_SPECIES_LIST.add("RUO");
		SHRUB_SPECIES_LIST.add("RUD");
		SHRUB_SPECIES_LIST.add("SAL");
		SHRUB_SPECIES_LIST.add("SAC");
		SHRUB_SPECIES_LIST.add("SAP");
		SHRUB_SPECIES_LIST.add("SHP");
		SHRUB_SPECIES_LIST.add("SOA");
		SHRUB_SPECIES_LIST.add("SOD");
		SHRUB_SPECIES_LIST.add("SPL");
		SHRUB_SPECIES_LIST.add("SPT");
		SHRUB_SPECIES_LIST.add("TAC");
		SHRUB_SPECIES_LIST.add("VIL");
		SHRUB_SPECIES_LIST.add("VIC");
		SHRUB_SPECIES_LIST.add("VIE");
		SHRUB_SPECIES_LIST.add("VIT");
		SHRUB_SPECIES_LIST.add("VIR");
		SHRUB_SPECIES_LIST.add("ERG");
		SHRUB_SPECIES_LIST.add("ERG");
		SHRUB_SPECIES_LIST.add("ERG");
	}

	public static enum DrainageGroup {
		XERIC, 
		MESIC,
		SUBHYDRIC,
		HYDRIC;
		
		private Matrix dummy;
		
		DrainageGroup() {
			dummy = new Matrix(1,4);
			dummy.m_afData[0][this.ordinal()] = 1d;
		}
		
		public Matrix getDrainageDummy() {return dummy;}
	}


	public static final Set<String> CONIFEROUS_SPECIES = new HashSet<String>();
	static {
		CONIFEROUS_SPECIES.add("EPB");
		CONIFEROUS_SPECIES.add("EPN");
		CONIFEROUS_SPECIES.add("EPR");
		CONIFEROUS_SPECIES.add("MEL");
		CONIFEROUS_SPECIES.add("PIB");
		CONIFEROUS_SPECIES.add("PIG");
		CONIFEROUS_SPECIES.add("PIR");
		CONIFEROUS_SPECIES.add("PRU");
		CONIFEROUS_SPECIES.add("SAB");
		CONIFEROUS_SPECIES.add("THO");
		CONIFEROUS_SPECIES.add("EPX");
		CONIFEROUS_SPECIES.add("PIN");
		CONIFEROUS_SPECIES.add("RES");
	}

	public static final List<String> POTENTIAL_VEGETATION_LIST = new ArrayList<String>();
	static {
		POTENTIAL_VEGETATION_LIST.add("FC1");
		POTENTIAL_VEGETATION_LIST.add("FE1");
		POTENTIAL_VEGETATION_LIST.add("FE2");
		POTENTIAL_VEGETATION_LIST.add("FE3");
		POTENTIAL_VEGETATION_LIST.add("FE4");
		POTENTIAL_VEGETATION_LIST.add("FE5");
		POTENTIAL_VEGETATION_LIST.add("FE6");
		POTENTIAL_VEGETATION_LIST.add("FO1");
		POTENTIAL_VEGETATION_LIST.add("ME1");
		POTENTIAL_VEGETATION_LIST.add("MF1");
		POTENTIAL_VEGETATION_LIST.add("MJ1");
		POTENTIAL_VEGETATION_LIST.add("MJ2");
		POTENTIAL_VEGETATION_LIST.add("MS1");
		POTENTIAL_VEGETATION_LIST.add("MS2");
		POTENTIAL_VEGETATION_LIST.add("MS4");
		POTENTIAL_VEGETATION_LIST.add("MS6");
		POTENTIAL_VEGETATION_LIST.add("RB1");
		POTENTIAL_VEGETATION_LIST.add("RB2");
		POTENTIAL_VEGETATION_LIST.add("RB5");
		POTENTIAL_VEGETATION_LIST.add("RC3");
		POTENTIAL_VEGETATION_LIST.add("RE1");
		POTENTIAL_VEGETATION_LIST.add("RE2");
		POTENTIAL_VEGETATION_LIST.add("RE3");
		POTENTIAL_VEGETATION_LIST.add("RE4");
		POTENTIAL_VEGETATION_LIST.add("RP1");
		POTENTIAL_VEGETATION_LIST.add("RS1");
		POTENTIAL_VEGETATION_LIST.add("RS2");
		POTENTIAL_VEGETATION_LIST.add("RS3");
		POTENTIAL_VEGETATION_LIST.add("RS4");
		POTENTIAL_VEGETATION_LIST.add("RS5");
		POTENTIAL_VEGETATION_LIST.add("RS7");
		POTENTIAL_VEGETATION_LIST.add("RT1");
	}

	public final static Map<String,String> ECO_REGION_MAP = new HashMap<String,String>();
	static {	
		ECO_REGION_MAP.put("1a","1ouest");
		ECO_REGION_MAP.put("2a","2ouest");
		ECO_REGION_MAP.put("2b","2est");
		ECO_REGION_MAP.put("2c","2est");
		ECO_REGION_MAP.put("3a","3ouest");
		ECO_REGION_MAP.put("3b","3ouest");
		ECO_REGION_MAP.put("3c","3est");
		ECO_REGION_MAP.put("3d","3est");
		ECO_REGION_MAP.put("4a","4ouest");
		ECO_REGION_MAP.put("4b","4ouest");
		ECO_REGION_MAP.put("4c","4ouest");
		ECO_REGION_MAP.put("4d","4est");
		ECO_REGION_MAP.put("4e","4est");
		ECO_REGION_MAP.put("4f","4est");
		ECO_REGION_MAP.put("4g","4est");
		ECO_REGION_MAP.put("4h","4est");
		ECO_REGION_MAP.put("5a","5ouest");
		ECO_REGION_MAP.put("5b","5ouest");
		ECO_REGION_MAP.put("5c","5ouest");
		ECO_REGION_MAP.put("5d","5ouest");
		ECO_REGION_MAP.put("5e","5est");
		ECO_REGION_MAP.put("5f","5est");
		ECO_REGION_MAP.put("5g","5est");
		ECO_REGION_MAP.put("5h","5est");
		ECO_REGION_MAP.put("5i","5est");
		ECO_REGION_MAP.put("5j","5est");
		ECO_REGION_MAP.put("6a","6ouest");
		ECO_REGION_MAP.put("6b","6ouest");
		ECO_REGION_MAP.put("6c","6ouest");
		ECO_REGION_MAP.put("6d","6ouest");
		ECO_REGION_MAP.put("6e","6ouest");
		ECO_REGION_MAP.put("6f","6ouest");
		ECO_REGION_MAP.put("6g","6ouest");
		ECO_REGION_MAP.put("6h","6est");
		ECO_REGION_MAP.put("6i","6est");
		ECO_REGION_MAP.put("6j","6est");
		ECO_REGION_MAP.put("6k","6est");
		ECO_REGION_MAP.put("6l","6est");
		ECO_REGION_MAP.put("6m","6est");
		ECO_REGION_MAP.put("6n","6est");
		ECO_REGION_MAP.put("6o","6est");
		ECO_REGION_MAP.put("6p","6est");
		ECO_REGION_MAP.put("6q","6est");
		ECO_REGION_MAP.put("6r","6est");
	}

	public static final Map<String, DrainageGroup> DRAINAGE_CLASS_LIST = new HashMap<String, DrainageGroup>();
	static {
		DRAINAGE_CLASS_LIST.put("0", DrainageGroup.XERIC);
		DRAINAGE_CLASS_LIST.put("1", DrainageGroup.XERIC);
		DRAINAGE_CLASS_LIST.put("2", DrainageGroup.MESIC);
		DRAINAGE_CLASS_LIST.put("3", DrainageGroup.MESIC);
		DRAINAGE_CLASS_LIST.put("4", DrainageGroup.SUBHYDRIC);
		DRAINAGE_CLASS_LIST.put("5", DrainageGroup.HYDRIC);
		DRAINAGE_CLASS_LIST.put("6", DrainageGroup.HYDRIC);
	}

	public static final Map<String, DrainageGroup> ENVIRONMENT_TYPE = new HashMap<String, DrainageGroup>();
	static {
		ENVIRONMENT_TYPE.put("0", DrainageGroup.XERIC);
		ENVIRONMENT_TYPE.put("1", DrainageGroup.MESIC);
		ENVIRONMENT_TYPE.put("2", DrainageGroup.MESIC);
		ENVIRONMENT_TYPE.put("3", DrainageGroup.MESIC);
		ENVIRONMENT_TYPE.put("4", DrainageGroup.SUBHYDRIC);
		ENVIRONMENT_TYPE.put("5", DrainageGroup.SUBHYDRIC);
		ENVIRONMENT_TYPE.put("6", DrainageGroup.SUBHYDRIC);
		ENVIRONMENT_TYPE.put("7", DrainageGroup.SUBHYDRIC);
		ENVIRONMENT_TYPE.put("8", DrainageGroup.SUBHYDRIC);
		ENVIRONMENT_TYPE.put("9", DrainageGroup.SUBHYDRIC);
	}

	
	
	public static final Map<Integer, String> TREE_STATUS_LIST = new HashMap<Integer, String>();
	static {
		TREE_STATUS_LIST.put(10, "Vivant");
		TREE_STATUS_LIST.put(12, "Vivant");
		TREE_STATUS_LIST.put(14, "Mort");
		TREE_STATUS_LIST.put(16, "Mort");
		TREE_STATUS_LIST.put(23, "Mort");
		TREE_STATUS_LIST.put(24, "Mort");
		TREE_STATUS_LIST.put(26, "Coup\u00E9");
		TREE_STATUS_LIST.put(29, "Vivant");
		TREE_STATUS_LIST.put(30, "Vivant");
		TREE_STATUS_LIST.put(32, "Vivant");
		TREE_STATUS_LIST.put(34, "Mort");
		TREE_STATUS_LIST.put(36, "Mort");
		TREE_STATUS_LIST.put(40, "Vivant");
		TREE_STATUS_LIST.put(42, "Vivant");
		TREE_STATUS_LIST.put(44, "Mort");
		TREE_STATUS_LIST.put(46, "Mort");
		TREE_STATUS_LIST.put(50, "Vivant");
		TREE_STATUS_LIST.put(52, "Vivant");
		TREE_STATUS_LIST.put(54, "Mort");
		TREE_STATUS_LIST.put(56, "Mort");
	}

	public static void main(String[] args) {
		QuebecForestRegion region = QuebecGeneralSettings.getForestRegion("2b", false);
	}
}
