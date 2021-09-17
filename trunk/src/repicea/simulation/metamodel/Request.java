package repicea.simulation.metamodel;

public enum Request {
	/**
	 * Periodic annual growth (m3/ha)
	 */
	PeriodicAnnualGrowth(false),
	/**
	 * Periodic annual mortality (m3/ha)
	 */
	PeriodicAnnualMortality(false),
	/**
	 * Periodic annual recruitment (m3/ha)
	 */
	PeriodicAnnualRecruitment(false),
	/**
	 * Periodic annual harvesting (m3/ha)
	 */
	PeriodicAnnualHarvesting(false),
	/**
	 * Proportion of harvested area per treatment over the last growth step
	 */
	ProportionHarvestedAreaPerTreatment(false),
	/**
	 * Volume of alive trees (m3/ha)
	 */
	AliveVolume(true),
	/**
	 * Volume of harvested trees (m3/ha)
	 */
	CutVolume(true),
	/**
	 * Volume of dead trees (m3/ha)
	 */
	DeadVolume(true),
	/**
	 * Volume of windthown trees (m3/ha)
	 */
	WindfallVolume(true);
	
	final boolean isMeantForInitialStand;
	
	Request(boolean isMeantForInitialStand) {
		this.isMeantForInitialStand = isMeantForInitialStand;
	}
	
	public boolean getIsMeantForInitialStand() {
		return isMeantForInitialStand;
	}
}
