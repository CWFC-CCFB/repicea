package repicea.simulation.metamodel;

import repicea.simulation.covariateproviders.MethodProviderEnum.VariableForEstimation;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

public enum Request {
	/**
	 * Periodic annual growth (m3/ha)
	 */
	PeriodicAnnualGrowth(false, null, null),
	/**
	 * Periodic annual mortality (m3/ha)
	 */
	PeriodicAnnualMortality(false, null, null),
	/**
	 * Periodic annual recruitment (m3/ha)
	 */
	PeriodicAnnualRecruitment(false, null, null),
	/**
	 * Periodic annual harvesting (m3/ha)
	 */
	PeriodicAnnualHarvesting(false, null, null),
	/**
	 * Proportion of harvested area per treatment over the last growth step
	 */
	ProportionHarvestedAreaPerTreatment(false, null, null),
	/**
	 * Volume of alive trees (m3/ha)
	 */
	AliveVolume(true, StatusClass.alive, VariableForEstimation.V),
	/**
	 * Volume of harvested trees (m3/ha)
	 */
	CutVolume(true, StatusClass.cut, VariableForEstimation.V),
	/**
	 * Volume of dead trees (m3/ha)
	 */
	DeadVolume(true, StatusClass.dead, VariableForEstimation.V),
	/**
	 * Volume of windthown trees (m3/ha)
	 */
	WindfallVolume(true, StatusClass.windfall, VariableForEstimation.V),
	
	/**
	 * Biomass (Mg)
	 */
	AliveAboveGroundBiomass(true, StatusClass.alive, VariableForEstimation.B);
	
	final StatusClass statusClass;
	final VariableForEstimation variableForEstimation;
	
	final boolean isMeantForInitialStand;
	
	Request(boolean isMeantForInitialStand, StatusClass statusClass, VariableForEstimation variableForEstimation) {
		this.isMeantForInitialStand = isMeantForInitialStand;
		this.statusClass = statusClass;
		this.variableForEstimation = variableForEstimation;
	}
	
	public boolean getIsMeantForInitialStand() {
		return isMeantForInitialStand;
	}
	
	public StatusClass getStatusClass() {
		return statusClass;
	}
	
	public VariableForEstimation getVariableForEstimation() {
		return variableForEstimation;
	}
}
