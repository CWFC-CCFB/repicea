package repicea.stats.model.dist;

import repicea.math.Matrix;
import repicea.stats.data.DataSet;
import repicea.stats.data.GenericStatisticalDataStructure;
import repicea.stats.estimators.Estimator;
import repicea.stats.estimators.MaximumLikelihoodEstimator;
import repicea.stats.model.AbstractStatisticalModel;

public class WeibullModel extends AbstractStatisticalModel<GenericStatisticalDataStructure> {

	protected WeibullModel(DataSet dataSet, String variable) {
		super(dataSet);
	}

	@Override
	public void setParameters(Matrix beta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Matrix getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix getPredicted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix getResiduals() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected void setCompleteLLK() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected GenericStatisticalDataStructure getDataStructureFromDataSet(DataSet dataSet) {return new GenericStatisticalDataStructure(dataSet);}

	@Override
	protected Estimator instantiateDefaultEstimator() {return new MaximumLikelihoodEstimator();}

}
