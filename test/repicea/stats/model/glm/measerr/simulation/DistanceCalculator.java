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
import java.util.Collections;
import java.util.List;

import repicea.stats.model.glm.measerr.simulation.DistanceCalculator.SpatialPopulationUnitDistance;


@SuppressWarnings("serial")
class DistanceCalculator extends ArrayList<SpatialPopulationUnitDistance> {
	
	static final class SpatialPopulationUnitDistance implements Comparable<SpatialPopulationUnitDistance> {
		final double distance;
		final SpatialPopulationUnit pu;
		
		SpatialPopulationUnitDistance(double distance, SpatialPopulationUnit pu) {
			this.distance = distance;
			this.pu = pu;
		}

		@Override
		public int compareTo(SpatialPopulationUnitDistance o) {
			if (distance < o.distance) {
				return -1;
			} else if (distance > o.distance) {
				return 1;
			}
			return 0;
		}
		
	}

	final SpatialPopulationUnit thisUnit;
	
	DistanceCalculator(SpatialPopulationUnit pu) {
		thisUnit = pu;
	}
	
	void setDistanceToConspecific(List<SpatialPopulationUnit> pus, boolean isPopulation, boolean updateFields) {
		clear();
		if (thisUnit.isConspecificIn) {
			if (isPopulation) {
				thisUnit.trueDistanceToConspecific = 0d;
			} else {
				thisUnit.measuredDistanceToConspecific = 0d;
			}
		} else {
			double distance = Double.POSITIVE_INFINITY;
			for (SpatialPopulationUnit pu : pus) {
				double thisDistance = thisUnit.getDistanceFrom(pu);
				if (isPopulation && pu.isConspecificIn) {
					if (thisDistance < distance) {
						distance = thisDistance;
					}							
				} else {
					add(new SpatialPopulationUnitDistance(thisDistance, pu));
				}
			}
			Collections.sort(this);
			if (isPopulation) {
				thisUnit.trueDistanceToConspecific = distance;
			} else {
				for (SpatialPopulationUnitDistance spdu : this) {
					if (spdu.pu.isConspecificIn) {
						distance = spdu.distance;
						break;
					}
				}
				thisUnit.measuredDistanceToConspecific = distance;
			}
		}
	}
	
	List<SpatialPopulationUnit> retrievePopulationUnits() {
		List<SpatialPopulationUnit> newList = new ArrayList<SpatialPopulationUnit>();
		for (SpatialPopulationUnitDistance spud : this) {
			newList.add(spud.pu);
		}
		return newList;
	}
}
