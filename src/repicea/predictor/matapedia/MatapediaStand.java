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
package repicea.predictor.matapedia;

import java.util.Collection;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.standlevel.DateYrProvider;
import repicea.simulation.covariateproviders.standlevel.SpruceBudwormDefoliatedProvider;

/**
 * The MatapediaStand interface ensures the compatibility of the stand object with 
 * the MatapediaDbhIncrementPredictor and the MatapediaMortalityPredictor classes.
 * @author Mathieu Fortin - November 2012
 */
public interface MatapediaStand extends MonteCarloSimulationCompliantObject,
//										InterventionResultProvider,
										SpruceBudwormDefoliatedProvider,
										DateYrProvider {
	
	/**
	 * This method returns a boolean that takes the value true if there is a 
	 * spruce budworm outbreak in the upcoming five years.
	 * @return a boolean
	 */
	public boolean isGoingToBeDefoliated();
	

	
	/**
	 * This method returns the MatapediaTree instances in this stand.
	 * @return a Collection of MatapediaTree instances
	 */
	public Collection<MatapediaTree> getMatapediaTrees();
	
	
	
	/**
	 * This method returns a boolean if the stand is going to be sprayed in
	 * the upcoming interval.
	 * @return a boolean
	 */
	public boolean isGoingToBeSprayed();
	
}
