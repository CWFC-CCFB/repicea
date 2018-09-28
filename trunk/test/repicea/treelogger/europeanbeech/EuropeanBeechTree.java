/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge-Epicea
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
package repicea.treelogger.europeanbeech;

import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.species.REpiceaSpecies.Species;

public class EuropeanBeechTree implements EuropeanBeechBasicTree {

	private final double dbhCm;
	private final double standardDeviationCm;
	
	protected EuropeanBeechTree(double dbhCm, double standardDeviationCm) {
		this.dbhCm = dbhCm;
		this.standardDeviationCm = standardDeviationCm;
	}
	
	@Override
	public double getNumber() {
		return 100;
	}

/*	@Override
	public TreeStatusPriorToLogging getTreeStatusPriorToLogging() {
		return TreeStatusPriorToLogging.Alive;
	}
*/
	@Override
	public double getCommercialVolumeM3() {
		return 1;
	}

//	@Override
//	public String getSpeciesName() {
//		return EuropeanBeechBasicTree.Species.EuropeanBeech.toString();
//	}

	@Override
	public double getDbhCm() {
		return dbhCm;
	}

	@Override
	public double getDbhCmStandardDeviation() {
		return standardDeviationCm;
	}

	@Override
	public Species getSpecies() {
		return REpiceaSpecies.Species.Fagus_sylvatica;
	}

}
