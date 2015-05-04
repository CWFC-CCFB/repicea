package repicea.treelogger.maritimepine;

class MaritimePineTree implements MaritimePineBasicTree {

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
		return 30;
	}

	@Override
	public double getDbhCmStandardDeviation() {
		return 10;
	}

}
