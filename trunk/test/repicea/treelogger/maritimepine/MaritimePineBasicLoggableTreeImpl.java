package repicea.treelogger.maritimepine;

import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.species.REpiceaSpecies.Species;

class MaritimePineBasicLoggableTreeImpl implements MaritimePineBasicLoggableTree {

	private final double dbhCm;
	private final double standardDeviationCm;
	private final double stumpVolumeM3;
	private final double branchVolumeM3;
	
	protected MaritimePineBasicLoggableTreeImpl(double dbhCm, double standardDeviationCm, double stumpVolumeM3, double branchVolumeM3) {
		this.dbhCm = dbhCm;
		this.standardDeviationCm = standardDeviationCm;
		this.stumpVolumeM3 = stumpVolumeM3;
		this.branchVolumeM3 = branchVolumeM3;
	}
	
	@Override
	public double getNumber() {
		return 100;
	}

/*	@Override
	public TreeStatusPriorToLogging getTreeStatusPriorToLogging() {
		return TreeStatusPriorToLogging.Alive;
	}
*/
	@Override
	public double getCommercialVolumeM3() {
		return 1;
	}

	@Override
	public Species getSpecies() {
		return REpiceaSpecies.Species.Pinus_pinaster;
	}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getDbhCmStandardDeviation() {return standardDeviationCm;}

	@Override
	public double getHarvestedStumpVolumeM3() {return stumpVolumeM3;}

	@Override
	public double getHarvestedCrownVolumeM3() {return branchVolumeM3;}

}
