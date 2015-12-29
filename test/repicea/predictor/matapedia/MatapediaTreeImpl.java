package repicea.predictor.matapedia;

import repicea.simulation.HierarchicalLevel;

public class MatapediaTreeImpl implements MatapediaTree {

	private MatapediaTreeSpecies species;
	private double dbh;
	private double bal;
	
	public MatapediaTreeImpl(MatapediaTreeSpecies species, double dbh, double bal) {
		this.species = species;
		this.dbh = dbh;
		this.bal = bal;
	}
	
//	@Override
//	public Object getSubjectPlusMonteCarloSpecificId() {
//		return this;
//	}

	@Override
	public int getSubjectId() {
		return this.hashCode();
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.TREE;
	}


	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}



	@Override
	public double getDbhCm() {
		return dbh;
	}

	@Override
	public double getSquaredDbhCm() {
		return dbh * dbh;
	}

	@Override
	public MatapediaTreeSpecies getMatapediaTreeSpecies() {
		return species;
	}

	@Override
	public double getBasalAreaLargerThanSubjectM2Ha() {
		return bal;
	}

}
