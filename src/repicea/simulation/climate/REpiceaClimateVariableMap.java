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
package repicea.simulation.climate;

import java.util.HashMap;
import java.util.Map;

import repicea.simulation.climate.REpiceaClimateVariableMap.ClimateVariable;

@SuppressWarnings("serial")
public class REpiceaClimateVariableMap extends HashMap<ClimateVariable, Double> {
	
	public static enum ClimateVariable {
		/**
		 * Mean annual temperature (C)
		 */
		MeanAnnualTempC,
		
		/**
		 * Mean annual precipitation (mm)
		 */
		MeanAnnualPrecMm,
		
		/**
		 * Mean temperature of the growing season (C)
		 */
		MeanSeasonalTempC,
		
		/**
		 * Mean precipitation of the growing season (mm)
		 */
		MeanSeasonalPrecMm;
	}


	/**
	 * This interface ensures that the REpiceaClimateVariableMap instance
	 * can be updated according to climate change forecasts. 
	 * @author Mathieu Fortin - June 2019
	 */
	public static interface UpdatableClimateVariableMap {
		
		/**
		 * Performs the change on the climate variable following a linear extrapolation pattern.
		 * @param annualChanges a Map with the annual change for one or many climate variables
		 * @param dateYr the current date (years)
		 */
		public REpiceaClimateVariableMap getUpdatedClimateVariableMap(Map<ClimateVariable, Double> annualChanges, int dateYr);
		
	}
	
}
