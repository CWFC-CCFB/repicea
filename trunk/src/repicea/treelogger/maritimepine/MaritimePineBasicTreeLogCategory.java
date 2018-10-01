package repicea.treelogger.maritimepine;

import repicea.simulation.covariateproviders.treelevel.DbhCmStandardDeviationProvider;
import repicea.simulation.treelogger.LoggableTree;
import repicea.stats.distributions.utility.GaussianUtility;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedLoggableTree;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedWoodPiece;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLoggerParameters.Grade;

@SuppressWarnings("serial")
public class MaritimePineBasicTreeLogCategory extends DiameterBasedTreeLogCategory {

	protected MaritimePineBasicTreeLogCategory(Enum<?> logGrade, String species, double smallEndDiameter, boolean isFromStump) {
		super(logGrade, species, smallEndDiameter, isFromStump);
	}
	
	@Override
	protected boolean isEligible(LoggableTree tree) {
		return tree instanceof MaritimePineBasicLoggableTree;
	}
	
	@Override
	protected DiameterBasedWoodPiece extractFromTree(LoggableTree tree, Object... parms) {
		DiameterBasedWoodPiece piece = null;
		if (isEligible(tree)) {
			double mqd = ((DiameterBasedLoggableTree) tree).getDbhCm();
			double dbhStandardDeviation = 0d;
			if (tree instanceof DbhCmStandardDeviationProvider) {
				dbhStandardDeviation = ((DbhCmStandardDeviationProvider) tree).getDbhCmStandardDeviation();
			}
			double energyWoodProportion;
			double highQualitySawlogProportion;
			double lowQualitySawlogProportion;

			if (dbhStandardDeviation > 0) {
				// Assumption of a normal distribution for stem distribution
				energyWoodProportion = GaussianUtility.getCumulativeProbability((20d - mqd)/dbhStandardDeviation);
				lowQualitySawlogProportion = GaussianUtility.getCumulativeProbability((30d - mqd)/dbhStandardDeviation) - energyWoodProportion;
				double potentialHighQualitySawlogProportion = GaussianUtility.getCumulativeProbability((30d - mqd)/dbhStandardDeviation, true);
				lowQualitySawlogProportion += MaritimePineBasicTreeLogger.LowQualityPercentageWithinHighQualityGrade * potentialHighQualitySawlogProportion;
				highQualitySawlogProportion = potentialHighQualitySawlogProportion * (1 - MaritimePineBasicTreeLogger.LowQualityPercentageWithinHighQualityGrade); 
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
					lowQualitySawlogProportion = MaritimePineBasicTreeLogger.LowQualityPercentageWithinHighQualityGrade;
					highQualitySawlogProportion = 1 - MaritimePineBasicTreeLogger.LowQualityPercentageWithinHighQualityGrade;
				}
			}
			
			
			Grade logGrade = (Grade) getGrade();
			
			switch(logGrade) {
			case Stump:
				double stumpVolumeM3 = ((MaritimePineBasicLoggableTree) tree).getHarvestedStumpVolumeM3();
				if  (stumpVolumeM3 > 0) {
					piece = new DiameterBasedWoodPiece(this, tree, stumpVolumeM3);
				} 
				break;
			case Crown:
				double crownVolumeM3 = ((MaritimePineBasicLoggableTree) tree).getHarvestedCrownVolumeM3();
				if  (crownVolumeM3 > 0) {
					piece = new DiameterBasedWoodPiece(this, tree, crownVolumeM3);
				} 
				break;
			case IndustryWood:
				if (energyWoodProportion > 0) {
					piece = new DiameterBasedWoodPiece(this, tree, energyWoodProportion * tree.getCommercialVolumeM3());
				} 
				break;
			case SawlogLowQuality:
				if (lowQualitySawlogProportion > 0) {
					piece = new DiameterBasedWoodPiece(this, tree, lowQualitySawlogProportion * tree.getCommercialVolumeM3());
				}
				break;
			case SawlogHighQuality:
				if (highQualitySawlogProportion > 0) {
					piece = new DiameterBasedWoodPiece(this, tree, highQualitySawlogProportion * tree.getCommercialVolumeM3());
				}
				break;
			}

		}
		return piece;
	}

}
