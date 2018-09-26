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
package repicea.treelogger.maritimepine;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLoggerManager;
import repicea.stats.distributions.utility.GaussianUtility;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedLoggableTree;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogger;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedWoodPiece;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLoggerParameters.Grade;

/**
 * The MaritimePineBasicTreeLogger class is a simple tree logger which considers
 * not only the commercial volume, but also the harvest of stumps and fine woody debris.
 *
 * @author Mathieu Fortin - November 2014
 */
public class MaritimePineBasicTreeLogger extends DiameterBasedTreeLogger {

	static {
		TreeLoggerManager.registerTreeLoggerName(MaritimePineBasicTreeLogger.class.getName());
	}
	
	private static double LowQualityPercentageWithinHighQualityGrade = 0.65;

	@Override
	public MaritimePineBasicTreeLoggerParameters createDefaultTreeLoggerParameters() {
		return new MaritimePineBasicTreeLoggerParameters();
	}

	@Override
	public MaritimePineBasicLoggableTree getEligible(LoggableTree t) {
		if (t instanceof MaritimePineBasicLoggableTree) {
			return (MaritimePineBasicLoggableTree) t;
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
		double highQualitySawlogProportion;
		double lowQualitySawlogProportion;

		if (dbhStandardDeviation > 0) {
			// Assumption of a normal distribution for stem distribution
			energyWoodProportion = GaussianUtility.getCumulativeProbability((20d - mqd)/dbhStandardDeviation);
			lowQualitySawlogProportion = GaussianUtility.getCumulativeProbability((30d - mqd)/dbhStandardDeviation) - energyWoodProportion;
			double potentialHighQualitySawlogProportion = GaussianUtility.getCumulativeProbability((30d - mqd)/dbhStandardDeviation, true);
			lowQualitySawlogProportion += LowQualityPercentageWithinHighQualityGrade * potentialHighQualitySawlogProportion;
			highQualitySawlogProportion = potentialHighQualitySawlogProportion * (1 - LowQualityPercentageWithinHighQualityGrade); 
		} else {	// no standard deviation
			if (mqd < 20) {
				energyWoodProportion = 1d;
				lowQualitySawlogProportion = 0d;
				highQualitySawlogProportion = 0d;
			} else  if (mqd < 30) {
				energyWoodProportion = 0d;
				lowQualitySawlogProportion = 1d;
				highQualitySawlogProportion = 0d;
			} else {
				energyWoodProportion = 0d;
				lowQualitySawlogProportion = LowQualityPercentageWithinHighQualityGrade;
				highQualitySawlogProportion = 1 - LowQualityPercentageWithinHighQualityGrade;
			}
		}
		
		
		Grade logGrade = (Grade) logCategory.getGrade();
		
		switch(logGrade) {
		case Stump:
			double stumpVolumeM3 = ((MaritimePineBasicLoggableTree) tree).getHarvestedStumpVolumeM3();
			if  (stumpVolumeM3 > 0) {
				piece = new DiameterBasedWoodPiece(logCategory, tree, stumpVolumeM3);
			} 
			break;
		case Crown:
			double crownVolumeM3 = ((MaritimePineBasicLoggableTree) tree).getHarvestedCrownVolumeM3();
			if  (crownVolumeM3 > 0) {
				piece = new DiameterBasedWoodPiece(logCategory, tree, crownVolumeM3);
			} 
			break;
		case IndustryWood:
			if (energyWoodProportion > 0) {
				piece = new DiameterBasedWoodPiece(logCategory, tree, energyWoodProportion * tree.getCommercialVolumeM3());
			} 
			break;
		case SawlogLowQuality:
			if (lowQualitySawlogProportion > 0) {
				piece = new DiameterBasedWoodPiece(logCategory, tree, lowQualitySawlogProportion * tree.getCommercialVolumeM3());
			}
			break;
		case SawlogHighQuality:
			if (highQualitySawlogProportion > 0) {
				piece = new DiameterBasedWoodPiece(logCategory, tree, highQualitySawlogProportion * tree.getCommercialVolumeM3());
			}
			break;
		}

		return piece;
	}

	@Override
	public boolean isCompatibleWith(Object referent) {
		return referent instanceof MaritimePineBasicLoggableTree;
	}
	
	
}
