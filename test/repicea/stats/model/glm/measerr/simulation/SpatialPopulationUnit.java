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

class SpatialPopulationUnit extends AbstractPopulationUnit {

	private static final List<String> FIELDNAMES = new ArrayList<String>();
	static {
		FIELDNAMES.add("y");
		FIELDNAMES.add("xCoord");
		FIELDNAMES.add("yCoord");
		FIELDNAMES.add("distanceToConsp");
		FIELDNAMES.add("isConspecificIn");
	}

	
//	class DistanceCalculator extends ArrayList<SpatialPopulationUnitDistance> {
//		void setDistanceToConspecific(List<SpatialPopulationUnit> pus, boolean isPopulation) {
//			clear();
//			if (isConspecificIn) {
//				if (isPopulation) {
//					trueDistanceToConspecific = 0d;
//				} else {
//					measuredDistanceToConspecific = 0d;
//				}
//			} else {
//				
//				double distance = Double.POSITIVE_INFINITY;
//				for (SpatialPopulationUnit pu : pus) {
//					if (pu.isConspecificIn) {
//						double thisDistance = SpatialPopulationUnit.this.getDistanceFrom(pu);
//						if (isPopulation) {
//							if (thisDistance < distance) {
//								distance = thisDistance;
//							}							
//						} else {
//							add(new SpatialPopulationUnitDistance(thisDistance, pu));
//						}
//					}
//				}
//				Collections.sort(this);
//				if (isPopulation) {
//					trueDistanceToConspecific = distance;
//				} else {
//					measuredDistanceToConspecific = get(0).distance * .5;
//				}
//			}
//
//		}
//	}
	
	
	
	final int xCoord;
	final int yCoord;
	final DistanceCalculator dc;

	SpatialPopulationUnit(int id, int xCoord, int yCoord, boolean isConspecificIn) {
		super(id, isConspecificIn);
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.dc = new DistanceCalculator(this);
	}
	
	double getDistanceFrom(SpatialPopulationUnit thatUnit) {
		double xDiff = xCoord - thatUnit.xCoord;
		double yDiff = yCoord - thatUnit.yCoord;
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	}
	
	@Override
	public int setY(Matrix parameters) {
		double pred = 1 - Math.exp(-Math.exp(parameters.getValueAt(0, 0) + parameters.getValueAt(1, 0) * trueDistanceToConspecific));
		boolean occurred = StatisticalUtility.getRandom().nextDouble() < pred;
		y = occurred ? 1 : 0;
		return y;
	}

	@Override
	public Object[] asObservation(boolean detailed) {
		if (detailed) {
			Object[] record = new Object[5];
			record[0] = y;
			record[1] = xCoord;
			record[2] = yCoord;
			record[3] = trueDistanceToConspecific;
			record[4] = isConspecificIn;
			return record;
		} else {
			Object[] record = new Object[5];
			record[0] = y;
			record[1] = measuredDistanceToConspecific;
			record[2] = measuredDistanceToConspecific * 2;
			record[3] = trueDistanceToConspecific;
			double distanceMin;
			if (isConspecificIn) {
				distanceMin = 0d;
			} else {
				double res = StatisticalUtility.getRandom().nextDouble();
				distanceMin = (trueDistanceToConspecific - res * measuredDistanceToConspecific * 2) / (1 - res);
//				DistanceCalculator secDC = new DistanceCalculator(dc.get(0).pu, false); // false: dont consider this unit
//				secDC.setDistanceToConspecific(dc.retrievePopulationUnits(), false, false); // false: it is not the population, false: we do not update fields
//				distanceMin = measuredDistanceToConspecific * 2 - secDC.get(0).distance;
//				if (distanceMin < 0) {
//					distanceMin = 0d;
//				}
			}
			record[4] = distanceMin;
			if (distanceMin > measuredDistanceToConspecific * 2) {
				int u = 0;
			}
			return record;
		}
	}

	@Override
	public SpatialPopulationUnit clone() {
		SpatialPopulationUnit pu = new SpatialPopulationUnit(this.id, this.xCoord, this.yCoord, this.isConspecificIn);
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
