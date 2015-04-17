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
package repicea.predictor.volume.honertotalvolume;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;

/**
 * This interface ensures the tree instance is compatible with the HonerTotalVolumePredictor class.
 * @author Mathieu Fortin - March 2013
 */
public interface HonerTotalVolumeTree extends SquaredDbhCmProvider, 
												HeightMProvider {

	
	public static enum HonerTotalVolumeTreeSpecies {
		BOJ,
		BOP,
		CET,
		CHR,
		EPB,
		EPN,
		EPR,
		ERN,
		ERR,
		ERS,
		HEG,
		ORA,
		OSV,
		PEB,
		PET,
		PIB,
		PIG,
		PIR,
		PRU,
		SAB,
		THO,
		TIL;
		
		private static Set<String> eligibleSpeciesNames;
		private final static Map<String, HonerTotalVolumeTreeSpecies> OtherEligibleSpecies = new HashMap<String, HonerTotalVolumeTreeSpecies>();
		static {
			OtherEligibleSpecies.put("F_0", HonerTotalVolumeTreeSpecies.BOP);
			OtherEligibleSpecies.put("F_1", HonerTotalVolumeTreeSpecies.BOP);
			OtherEligibleSpecies.put("RES", HonerTotalVolumeTreeSpecies.EPB);
			OtherEligibleSpecies.put("F0R", HonerTotalVolumeTreeSpecies.BOP);
			OtherEligibleSpecies.put("FEU", HonerTotalVolumeTreeSpecies.BOP);
			OtherEligibleSpecies.put("AUT", HonerTotalVolumeTreeSpecies.BOP);
			OtherEligibleSpecies.put("CHX", HonerTotalVolumeTreeSpecies.CHR);
			OtherEligibleSpecies.put("EPX", HonerTotalVolumeTreeSpecies.EPB);
			OtherEligibleSpecies.put("PEU", HonerTotalVolumeTreeSpecies.PET);
			OtherEligibleSpecies.put("PIN", HonerTotalVolumeTreeSpecies.PIB);
			OtherEligibleSpecies.put("BOU", HonerTotalVolumeTreeSpecies.BOP);
			OtherEligibleSpecies.put("EP", HonerTotalVolumeTreeSpecies.EPB);
		}
		
		
		private static boolean isEligibleSpecies(String speciesName) {
			if (eligibleSpeciesNames == null) {
				eligibleSpeciesNames = new HashSet<String>();
				for (HonerTotalVolumeTreeSpecies species : HonerTotalVolumeTreeSpecies.values()) {
					eligibleSpeciesNames.add(species.name());
				}
			}
			if (eligibleSpeciesNames.contains(speciesName)) {
				return true;
			} else {
				return false;
			}
		}
		
		/**
		 * This method retrieves the appropriate HonerTotalVolumeSpecies that corresponds to 
		 * the species name or null otherwise.
		 * @param speciesName a String
		 * @return a HonerTotalVolumeSpecies instance
		 */
		public static HonerTotalVolumeTreeSpecies retrieveSpecies(String speciesName) {
			speciesName = speciesName.trim().toUpperCase();
			if (isEligibleSpecies(speciesName)) {
				return HonerTotalVolumeTreeSpecies.valueOf(speciesName);
			} else {
				return OtherEligibleSpecies.get(speciesName); 
			}
		}

		
		
	}
	
	
	/**
	 * This method returns the enum that corresponds to the tree species.
	 * @return a HonerTotalVolumeTreeSpecies enum instance
	 */
	public HonerTotalVolumeTreeSpecies getHonerSpecies();
	
}
