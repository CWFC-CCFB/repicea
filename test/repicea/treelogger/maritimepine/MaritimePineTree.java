package repicea.treelogger.maritimepine;

class MaritimePineTree implements MaritimePineBasicTree {

	private final double dbhCm;
	private final double standardDeviationCm;
	
	protected MaritimePineTree(double dbhCm, double standardDeviationCm) {
		this.dbhCm = dbhCm;
		this.standardDeviationCm = standardDeviationCm;
	}
	
	@Override
	public double getNumber() {
		return 100;
	}

	@Override
	public TreeStatusPriorToLogging getTreeStatusPriorToLogging() {
		return TreeStatusPriorToLogging.Alive;
	}

	@Override
	public double getCommercialVolumeM3() {
		return 1;
	}

	@Override
	public String getSpeciesName() {
		return MaritimePineBasicTree.Species.MaritimePine.toString();
	}

	@Override
	public double getDbhCm() {
		return dbhCm;
	}

	@Override
	public double getDbhCmStandardDeviation() {
		return standardDeviationCm;
	}

}
