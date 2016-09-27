/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2013 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package repicea.predictor.wbirchloggrades;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.standlevel.ElevationMProvider;

/**
 * This interface ensures the stand object is compatible with the tree logger WBirchProdVol.
 * @author Mathieu Fortin - September 2013
 */
public interface WBirchLogGradesStand extends MonteCarloSimulationCompliantObject, ElevationMProvider {

}
