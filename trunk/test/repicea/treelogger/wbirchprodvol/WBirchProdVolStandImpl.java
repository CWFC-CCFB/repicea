package repicea.treelogger.wbirchprodvol;

import java.util.HashMap;
import java.util.Map;

import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;

class WBirchProdVolStandImpl implements WBirchProdVolStand {

	private final String plotID;
	private final double elevation;
	private final Map<Integer, WBirchProdVolTreeImpl> trees;
	private int monteCarloId;
	
	WBirchProdVolStandImpl(String plotID, double elevation) {
		this.plotID = plotID;
		this.elevation = elevation;
		this.trees = new HashMap<Integer, WBirchProdVolTreeImpl>();
	}
	
	@Override
	public int getSubjectId() {return plotID.hashCode();}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.Plot;}

	protected void setMonteCarloRealizationId(int i) {monteCarloId = i;}

	@Override
	public int getMonteCarloRealizationId() {return monteCarloId;}

	@Override
	public double getElevationM() {return elevation;}
	
	protected Map<Integer, WBirchProdVolTreeImpl> getTrees() {return trees;}

}
