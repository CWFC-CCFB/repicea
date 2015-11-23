package repicea.stats.model.glm;

import java.security.InvalidParameterException;

import repicea.math.Matrix;
import repicea.stats.AbstractStatisticalExpression;
import repicea.stats.LinearStatisticalExpression;
import repicea.stats.model.Likelihood;
import repicea.stats.model.glm.LinkFunction.LFParameter;

@SuppressWarnings("serial")
public class LikelihoodGLM extends Likelihood {

	protected final LinkFunction linkFunction;
	protected final LinearStatisticalExpression eta;
	
	public LikelihoodGLM(LinkFunction linkFunction) {
		if (linkFunction == null || linkFunction.getParameterValue(LFParameter.Eta) == null) {
			throw new InvalidParameterException("The link function is null or the eta parameter has not been set!");
		} else {
			this.linkFunction = linkFunction;
			eta = linkFunction.getParameterValue(LFParameter.Eta);
		}
	}
	
	protected double getPrediction() {return linkFunction.getValue();}
	
	
	@Override
	public Double getValue() {
		double predicted = getPrediction();
		if (observedValue == 1d) {
			return predicted;
		} else {
			return 1d - predicted; 
		}
	}

	@Override
	public Matrix getGradient() {
		Matrix lfGradient = linkFunction.getGradient();
		if (observedValue == 1d) {
			return lfGradient;
		} else {
			return lfGradient.scalarMultiply(-1d);
		}
	}

	@Override
	public Matrix getHessian() {
		Matrix lfHessian = linkFunction.getHessian();
		if (observedValue == 1d) {
			return lfHessian;
		} else {
			return lfHessian.scalarMultiply(-1d);
		}
	}

	@Override
	protected AbstractStatisticalExpression getInnerExpression() {return eta;}
	
	
	
	

}
