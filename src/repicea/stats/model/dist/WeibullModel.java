package repicea.stats.model.dist;

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.estimators.Estimator;
import repicea.stats.estimators.MaximumLikelihoodEstimator;
import repicea.stats.estimators.MaximumLikelihoodEstimator.MaximumLikelihoodCompatibleModel;
import repicea.stats.model.AbstractStatisticalModel;
import repicea.stats.model.CompositeLogLikelihood;
import repicea.stats.model.CompositeLogLikelihoodWithExplanatoryVariable;

public class WeibullModel extends AbstractStatisticalModel implements MaximumLikelihoodCompatibleModel {

	private final List<Double> values;
	private Matrix beta;
	private CompositeLogLikelihoodWithExplanatoryVariable cLL;
	
	public WeibullModel(List<Double> values) {
		super();
		this.values = new ArrayList<Double>();
		this.values.addAll(values);
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
	protected Estimator instantiateDefaultEstimator() {return new MaximumLikelihoodEstimator(this);}

	@Override
	public boolean isInterceptModel() {return false;}

	@Override
	public List<String> getEffectList() {return null;}

	@Override
	public int getNumberOfObservations() {return values.size();}

	@Override
	public double getConvergenceCriterion() {
		return 1E-8;
	}

	@Override
	public CompositeLogLikelihood getCompleteLogLikelihood() {
		return null;
	}

}
