package repicea.treelogger.wbirchprodvol;

import repicea.math.Matrix;
import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;

class WBirchProdVolTreeImpl implements WBirchProdVolLoggableTree {

	private final int treeID;
	private final double dbhCm;
	private ABCDQuality quality;
	private final WBirchProdVolStandImpl stand;
	private final Matrix predRef;
	private final double h20Obs;
	
	WBirchProdVolTreeImpl(int treeID, 
			String qualityString, 
			double dbhCm, 
			WBirchProdVolStandImpl stand, 
			double h20Obs,
			double h20Pred, 
			double merVolPred, 
			double pulpVolPred, 
			double lowGradeSawlogVolPred,
			double sawlogVolPred,
			double lowGradeVeneerVolPred,
			double veneerVolPred) {
		this.treeID = treeID;
		if (qualityString.equals("NC")) {
			quality = null;
		} else {
			quality = ABCDQuality.valueOf(qualityString);
		}
		this.dbhCm = dbhCm;
		this.stand = stand;
		this.h20Obs = h20Obs;

		this.predRef = new Matrix(7,1);
		predRef.m_afData[0][0] = h20Pred;
		predRef.m_afData[1][0] = merVolPred;
		predRef.m_afData[2][0] = pulpVolPred;
		predRef.m_afData[3][0] = sawlogVolPred;
		predRef.m_afData[4][0] = lowGradeVeneerVolPred;
		predRef.m_afData[5][0] = veneerVolPred;
		predRef.m_afData[6][0] = lowGradeSawlogVolPred;
	}
	
	
	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public ABCDQuality getABCDQuality() {return quality;}

	@Override
	public int getSubjectId() {return treeID;}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.Tree;}

	@Override
	public int getMonteCarloRealizationId() {return stand.getMonteCarloRealizationId();}

	protected Matrix getPredRef() {return predRef;}
	
	public double getH20Obs() {return h20Obs;}


	@Override
	public WBirchProdVolTreeSpecies getWBirchProdVolTreeSpecies() {return WBirchProdVolTreeSpecies.WhiteBirch;}


	@Override
	public double getNumber() {return 1d;}


	@Override
	public TreeStatusPriorToLogging getTreeStatusPriorToLogging() {
		return TreeStatusPriorToLogging.Alive;
	}


	@Override
	public double getCommercialVolumeM3() {
		return predRef.m_afData[1][0];
	}


	@Override
	public String getSpeciesName() {
		return getWBirchProdVolTreeSpecies().toString();
	}


	@Override
	public WBirchProdVolStand getStand() {return this.stand;}

}
