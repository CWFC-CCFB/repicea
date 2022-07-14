/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.model.glm.measerr.simulation;

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.StatisticalUtility;

class NonSpatialPopulationUnit extends AbstractPopulationUnit {

	
	private static final List<String> FIELDNAMES = new ArrayList<String>();
	static {
		FIELDNAMES.add("y");
		FIELDNAMES.add("measDistanceToConsp");
		FIELDNAMES.add("trueDistanceToConsp");
		FIELDNAMES.add("isConspecificIn");
	}

	NonSpatialPopulationUnit(int id, boolean isConspecificIn) {
		super(id, isConspecificIn);
		if (!this.isConspecificIn) {
			this.measuredDistanceToConspecific = StatisticalUtility.getRandom().nextDouble() * 10;
		}
	}

	@Override
	public int setY(Matrix parameters) {
		trueDistanceToConspecific = StatisticalUtility.getRandom().nextDouble() * 2 * measuredDistanceToConspecific;
		double pred = 1 - Math.exp(-Math.exp(parameters.getValueAt(0, 0) + parameters.getValueAt(1, 0) * trueDistanceToConspecific));
		boolean occurred = StatisticalUtility.getRandom().nextDouble() < pred;
		y = occurred ? 1 : 0;
		return y;
	}

	@Override
	public Object[] asObservation(boolean detailed) {
		if (detailed) {
			Object[] record = new Object[4];
			record[0] = y;
			record[1] = measuredDistanceToConspecific;
			record[2] = trueDistanceToConspecific;
			record[3] = isConspecificIn;
			return record;
		} else {
			Object[] record = new Object[4];
			record[0] = y;
			record[1] = measuredDistanceToConspecific;
			record[2] = measuredDistanceToConspecific * 2;
			record[3] = trueDistanceToConspecific;
			return record;
		}
	}

	@Override
	public NonSpatialPopulationUnit clone() {
		NonSpatialPopulationUnit pu = new NonSpatialPopulationUnit(this.id, this.isConspecificIn);
		pu.trueDistanceToConspecific = trueDistanceToConspecific;
		pu.measuredDistanceToConspecific = measuredDistanceToConspecific;
		pu.y = y;
		return pu;
	}

	@Override
	public List<String> getFieldname(boolean detailed) {
		if (detailed) 
			return FIELDNAMES;
		else
			return AbstractPopulationUnit.FIELDNAMES_INPUT_DATASET;
	}

}
