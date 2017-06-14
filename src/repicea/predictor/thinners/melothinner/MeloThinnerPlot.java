/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge-Epicea
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
package repicea.predictor.thinners.melothinner;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.standlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.standlevel.CruiseLineProvider;
import repicea.simulation.covariateproviders.standlevel.EcologicalTypeProvider;
import repicea.simulation.covariateproviders.standlevel.QuebecForestRegionProvider;
import repicea.simulation.covariateproviders.standlevel.SlopeMRNFClassProvider;
import repicea.simulation.covariateproviders.standlevel.StemDensityHaProvider;

/**
 * The MeloThinnerPlot interface ensures that the plot instance is compatible with the MeloThinnerPredictor. The plot
 * may optionally implement the LandOwnership interface. If not, the land ownership is assumed to be public.
 * @author Mathieu Fortin - May 2017
 */
public interface MeloThinnerPlot extends MonteCarloSimulationCompliantObject, 
											BasalAreaM2HaProvider, 
											StemDensityHaProvider,
											SlopeMRNFClassProvider,
											EcologicalTypeProvider,
											QuebecForestRegionProvider,
											CruiseLineProvider {

	
	
	
	
	
}
