package repicea.stats.model.glm;

import repicea.math.Matrix;
import repicea.stats.model.Likelihood;
import repicea.stats.model.glm.LinkFunction.LFParameter;

@SuppressWarnings("serial")
public class LikelihoodGLM extends Likelihood {

	protected final LinkFunction linkFunction;
	
	public LikelihoodGLM(LinkFunction linkFunction) {
		super(linkFunction.getParameterValue(LFParameter.Eta));
		this.linkFunction = linkFunction;
	}

	@Override
	public double getPrediction() {return linkFunction.getValue();}
	
	
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


}
