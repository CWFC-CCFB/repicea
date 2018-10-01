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

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLoggerCompatibilityCheck;
import repicea.stats.distributions.utility.GaussianUtility;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedLoggableTree;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogger;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedWoodPiece;
import repicea.treelogger.europeanbeech.EuropeanBeechBasicTreeLoggerParameters.Grade;

public class EuropeanBeechBasicTreeLogger extends DiameterBasedTreeLogger {

	
	@Override
	public EuropeanBeechBasicTreeLoggerParameters createDefaultTreeLoggerParameters() {
		return new EuropeanBeechBasicTreeLoggerParameters();
	}

	@Override
	public EuropeanBeechBasicTree getEligible(LoggableTree t) {
		if (t instanceof EuropeanBeechBasicTree) {
			return (EuropeanBeechBasicTree) t;
		} else {
			return null;
		}
	}
	

	@Override
	protected DiameterBasedWoodPiece producePiece(DiameterBasedLoggableTree tree, DiameterBasedTreeLogCategory logCategory) {
		double mqd = tree.getDbhCm();
		double dbhStandardDeviation = tree.getDbhCmStandardDeviation();
		DiameterBasedWoodPiece piece = null;
		double energyWoodProportion;
		double industryWoodProportion;
		double veneerProportion;
		double regularQualitySawlogProportion;
		double lowQualitySawlogProportion;

		if (dbhStandardDeviation > 0) {
			// Assumption of a normal distribution for stem distribution
			energyWoodProportion = GaussianUtility.getCumulativeProbability((17.5 - mqd)/dbhStandardDeviation);
			industryWoodProportion = GaussianUtility.getCumulativeProbability((27.5 - mqd)/dbhStandardDeviation) - energyWoodProportion;
			lowQualitySawlogProportion = GaussianUtility.getCumulativeProbability((37.5 - mqd)/dbhStandardDeviation) - energyWoodProportion - industryWoodProportion;
			regularQualitySawlogProportion = GaussianUtility.getCumulativeProbability((47.5 - mqd)/dbhStandardDeviation) - lowQualitySawlogProportion - energyWoodProportion - industryWoodProportion; 
			veneerProportion = GaussianUtility.getCumulativeProbability((47.5 - mqd)/dbhStandardDeviation, true);
		} else {	// no standard deviation
			if (mqd < 17.5) {
				energyWoodProportion = 1d;
				industryWoodProportion = 0d;
				lowQualitySawlogProportion = 0d;
				regularQualitySawlogProportion = 0d;
				veneerProportion = 0d;
			} else  if (mqd < 27.5) {
				energyWoodProportion = 0d;
				industryWoodProportion = 1d;
				lowQualitySawlogProportion = 0d;
				regularQualitySawlogProportion = 0d;
				veneerProportion = 0d;
			} else  if (mqd < 37.5) {
				energyWoodProportion = 0d;
				industryWoodProportion = 0d;
				lowQualitySawlogProportion = 1d;
				regularQualitySawlogProportion = 0d;
				veneerProportion = 0d;
			} else  if (mqd < 47.5) {
				energyWoodProportion = 0d;
				industryWoodProportion = 0d;
				lowQualitySawlogProportion = 0d;
				regularQualitySawlogProportion = 1d;
				veneerProportion = 0d;
			} else { 
				energyWoodProportion = 0d;
				industryWoodProportion = 1d;
				lowQualitySawlogProportion = 0d;
				regularQualitySawlogProportion = 0d;
				veneerProportion = 1d;
			} 
		}
		
		if (logCategory.getGrade() == Grade.EnergyWood) {
			if (energyWoodProportion > 0) {
				piece = new DiameterBasedWoodPiece(logCategory, tree, energyWoodProportion * tree.getCommercialVolumeM3());
			} 
 		} else if (logCategory.getGrade() == Grade.IndustryWood) {
			if (industryWoodProportion > 0) {
					piece = new DiameterBasedWoodPiece(logCategory, tree, industryWoodProportion * tree.getCommercialVolumeM3());
			}
 		} else if (logCategory.getGrade() == Grade.SawlogLowQuality) {
			if (lowQualitySawlogProportion > 0) {
					piece = new DiameterBasedWoodPiece(logCategory, tree, lowQualitySawlogProportion * tree.getCommercialVolumeM3());
			}
 		} else if (logCategory.getGrade() == Grade.SawlogRegularQuality) {
			if (regularQualitySawlogProportion > 0) {
					piece = new DiameterBasedWoodPiece(logCategory, tree, regularQualitySawlogProportion * tree.getCommercialVolumeM3());
			}
		} else {
			if (veneerProportion > 0) {
				piece = new DiameterBasedWoodPiece(logCategory, tree, veneerProportion * tree.getCommercialVolumeM3());
			}
		}

		return piece;
	}

	@Override
	public boolean isCompatibleWith(TreeLoggerCompatibilityCheck check) {
		return check.getTreeInstance() instanceof EuropeanBeechBasicTree; 
	}
	
	
}

