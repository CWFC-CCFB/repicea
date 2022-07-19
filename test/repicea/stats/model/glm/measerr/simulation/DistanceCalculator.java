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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repicea.io.FormatField;
import repicea.io.Saveable;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.stats.model.dist.WeibullModel;
import repicea.stats.model.glm.measerr.simulation.DistanceCalculator.SpatialPopulationUnitDistance;


@SuppressWarnings("serial")
class DistanceCalculator extends ArrayList<SpatialPopulationUnitDistance> implements Saveable {
	
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
				if (isPopulation) {
					if (pu.isConspecificIn && thisDistance < distance) {
						distance = thisDistance;
					}							
				} else {
					if (!thisUnit.equals(pu)) {
						add(new SpatialPopulationUnitDistance(thisDistance, pu));
					}
				}
			}
			Collections.sort(this);
			if (isPopulation) {
				thisUnit.trueDistanceToConspecific = distance;
			} else {
				List<Double> distances = new ArrayList<Double>();
				for (SpatialPopulationUnitDistance spdu : this) {
					if (spdu.pu.isConspecificIn) {
						distances.add(spdu.distance);
					}
				}
				WeibullModel wm = new WeibullModel(distances, true); // true: enable location parameter
				double theta_pct = wm.getParameters().getValueAt(2, 0);
				wm.doEstimation();
				double theta_hat = wm.getParameters().getValueAt(2, 0);
				System.out.println("True distance = " + thisUnit.trueDistanceToConspecific + "; theta_pct = " + theta_pct + "; theta_hat = " + theta_hat + "; Smallest observed distance = " + distances.get(0));
//				for (SpatialPopulationUnitDistance spdu : this) {
//					if (spdu.pu.isConspecificIn) {
//						distance = spdu.distance;
//						break;
//					}
//				}
				thisUnit.measuredDistanceToConspecific = theta_pct;
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

	@Override
	public void save(String filename) throws IOException {
		CSVWriter writer = new CSVWriter(new File(filename), false);
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("id"));
		fields.add(new CSVField("distance"));
		fields.add(new CSVField("isConspecificIn"));
		writer.setFields(fields);
		
		Object[] record;
//		int nbOccurrences = 0;
		for (SpatialPopulationUnitDistance spdu : this) {
			record = new Object[3];
			record[0] = spdu.pu.id;
			record[1] = spdu.distance;
			record[2] = spdu.pu.isConspecificIn ? 1 : 0;
			writer.addRecord(record);
//			if (spdu.pu.isConspecificIn) {
//				nbOccurrences++;
//			}
//			if (nbOccurrences >= 30) {
//				break;
//			}
		}
		writer.close();
	}
}
