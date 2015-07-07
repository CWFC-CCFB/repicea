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
package repicea.predictor.artemis2009;

import repicea.math.Matrix;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.standlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.standlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.standlevel.DateYrProvider;
import repicea.simulation.covariateproviders.standlevel.ElevationMProvider;
import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;
import repicea.simulation.covariateproviders.standlevel.GrowthStepLengthYrProvider;
import repicea.simulation.covariateproviders.standlevel.InterventionResultProvider;
import repicea.simulation.covariateproviders.standlevel.MeanAnnualPrecipitationMmProvider;
import repicea.simulation.covariateproviders.standlevel.MeanAnnualTemperatureCProvider;
import repicea.simulation.covariateproviders.standlevel.MeanQuadraticDiameterCmProvider;
import repicea.simulation.covariateproviders.standlevel.PotentialVegetationProvider;
import repicea.simulation.covariateproviders.standlevel.StemDensityHaProvider;

public interface Artemis2009CompatibleStand extends PotentialVegetationProvider, 
													InterventionResultProvider, 
													BasalAreaM2HaProvider,
													MeanQuadraticDiameterCmProvider,
													DateYrProvider,
													MeanAnnualPrecipitationMmProvider,
													MeanAnnualTemperatureCProvider,
													ElevationMProvider,
													GeographicalCoordinatesProvider,
													StemDensityHaProvider,
													AreaHaProvider,
													MonteCarloSimulationCompliantObject,
													GrowthStepLengthYrProvider {
	
	
	/**
	 * This method returns true if the plot is going to be defoliated by spruce budworm.
	 * @return a boolean
	 */
	public boolean isGoingToBeDefoliated();
	
	/**
	 * This method returns true if the stand has just been read from a file and is not the result of an
	 * evolution.
	 * @return a boolean
	 */
	public boolean isInitialStand();
	
	/**
	 * This method returns the number of stems in the plot (NOT PER HECTARE!) for each species group
	 * @return a Matrix
	 */
	public Matrix getNumberOfStemsBySpeciesGroup();
	
	/**
	 * This method returns the basal area in the plot (NOT PER HECTARE!) for each species group
	 * @return a Matrix
	 */
	public Matrix getBasalAreaBySpeciesGroup();
	
}
