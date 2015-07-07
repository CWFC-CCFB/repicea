package repicea.treelogger.europeanbeech;

import repicea.simulation.treelogger.LoggableTree;
import repicea.stats.distributions.GaussianUtility;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTree;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogger;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedWoodPiece;
import repicea.treelogger.europeanbeech.EuropeanBeechBasicTreeLoggerParameters.Grade;

public class EuropeanBeechBasicTreeLogger extends DiameterBasedTreeLogger {

	private static double LowQualityPercentageWithinHighQualityGrade = 0.65;


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
	protected DiameterBasedWoodPiece producePiece(DiameterBasedTree tree, DiameterBasedTreeLogCategory logCategory) {
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
		
		if (logCategory.getGrade() == Grade.IndustryWood) {
			if (energyWoodProportion > 0) {
				piece = new DiameterBasedWoodPiece(logCategory, tree, energyWoodProportion * tree.getCommercialVolumeM3());
			} 
 		} else {
			if (logCategory.getGrade() == Grade.SawlogLowQuality) {
				if (lowQualitySawlogProportion > 0) {
					piece = new DiameterBasedWoodPiece(logCategory, tree, lowQualitySawlogProportion * tree.getCommercialVolumeM3());
				}
			} else {
				if (highQualitySawlogProportion > 0) {
					piece = new DiameterBasedWoodPiece(logCategory, tree, highQualitySawlogProportion * tree.getCommercialVolumeM3());
				}
			}
		}

		return piece;
	}
	
	
}

