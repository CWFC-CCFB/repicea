package repicea.predictor.wbirchloggrades;

import repicea.math.Matrix;
import repicea.predictor.wbirchloggrades.WBirchLogGradesStand;
import repicea.simulation.HierarchicalLevel;
import repicea.treelogger.wbirchprodvol.WBirchProdVolLoggableTree;

class WBirchLogGradesTreeImpl implements WBirchProdVolLoggableTree {

	private final int treeID;
	private final double dbhCm;
	private ABCDQuality quality;
	private final WBirchLogGradesStandImpl stand;
	private Matrix predRef;
	private double h20Obs;

	/**
	 * Basic constructor for manuscript on hybrid estimation
	 * @param treeID
	 * @param qualityString
	 * @param dbhCm
	 * @param stand
	 */
	WBirchLogGradesTreeImpl(int treeID, 
			String qualityString, 
			double dbhCm, 
			WBirchLogGradesStandImpl stand) {
		this.treeID = treeID;
		if (qualityString.equals("NC")) {
			quality = null;
		} else {
			quality = ABCDQuality.valueOf(qualityString);
		}
		this.dbhCm = dbhCm;
		this.stand = stand;
	}


	/**
	 * Constructor for test.
	 * @param treeID
	 * @param qualityString
	 * @param dbhCm
	 * @param stand
	 * @param h20Obs
	 * @param realization
	 */
	WBirchLogGradesTreeImpl(int treeID, 
			String qualityString, 
			double dbhCm, 
			WBirchLogGradesStandImpl stand, 
			double h20Obs,
			Matrix realization) {
		this(treeID, qualityString, dbhCm, stand);
		this.h20Obs = h20Obs;

		setRealizedValues(realization);
	}
	
	
	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public ABCDQuality getABCDQuality() {return quality;}

	@Override
	public String getSubjectId() {return ((Integer) treeID).toString();}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	@Override
	public int getMonteCarloRealizationId() {return stand.getMonteCarloRealizationId();}

	protected Matrix getRealizedValues() {return predRef;}
	
	protected void setRealizedValues(Matrix realization) {this.predRef = realization;} 
	
	public double getH20Obs() {return h20Obs;}


	@Override
	public WBirchProdVolTreeSpecies getWBirchProdVolTreeSpecies() {return WBirchProdVolTreeSpecies.WhiteBirch;}


	@Override
	public double getNumber() {return 1d;}


	@Override
	public double getCommercialVolumeM3() {
		return predRef.m_afData[1][0];
	}


	@Override
	public String getSpeciesName() {
		return getWBirchProdVolTreeSpecies().toString();
	}


	@Override
	public WBirchLogGradesStand getStand() {return this.stand;}

}
