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

import repicea.simulation.treelogger.WoodPiece;
import repicea.stats.distributions.GaussianUtility;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLoggerParameters.Grade;

/**
 * The MaritimePineBasicWoodPiece is a simple class that represents 
 * the wood pieces produced by the MaritimePineBasicTreeLogger.
 *  
 * @author Mathieu Fortin - November 2014
 */
@SuppressWarnings("serial")
public class MaritimePineBasicWoodPiece extends WoodPiece {

	private static double LowQualityPercentageWithinHighQualityGrade = 0.65;
	
	protected MaritimePineBasicWoodPiece(MaritimePineBasicTreeLogCategory logCategory, MaritimePineBasicTree tree) {
		super(logCategory, tree);
		double mqd = tree.getDbhCm();
		double dbhStandardDeviation = tree.getDbhCmStandardDeviation();

		if (dbhStandardDeviation > 0) {
			// Assumption of a normal distribution for stem distribution
			double energyWoodProportion = GaussianUtility.getCumulativeProbability((20d - mqd)/dbhStandardDeviation);
			if (logCategory.logGrade == Grade.IndustryWood) {
				setVolumeM3(energyWoodProportion * tree.getCommercialVolumeM3());
			} else {
				double lowQualitySawlogProportion = GaussianUtility.getCumulativeProbability((30d - mqd)/dbhStandardDeviation) - energyWoodProportion;
				double potentialHighQualitySawlogProportion = GaussianUtility.getCumulativeProbability((30d - mqd)/dbhStandardDeviation, true);
				lowQualitySawlogProportion += LowQualityPercentageWithinHighQualityGrade * potentialHighQualitySawlogProportion;
				if (logCategory.logGrade == Grade.SawlogLowQuality) {
					setVolumeM3(lowQualitySawlogProportion * tree.getCommercialVolumeM3());
				} else {
					double highQualitySawlogProportion = potentialHighQualitySawlogProportion * (1 - LowQualityPercentageWithinHighQualityGrade); 
					setVolumeM3(highQualitySawlogProportion * tree.getCommercialVolumeM3());
				}
			}
		} 
	}

}
