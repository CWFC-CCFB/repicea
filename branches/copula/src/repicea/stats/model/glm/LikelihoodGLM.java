package repicea.stats.model.glm;

import repicea.math.Matrix;
import repicea.stats.model.IndividualLikelihood;

@SuppressWarnings("serial")
public class LikelihoodGLM extends IndividualLikelihood {

	protected final LinkFunction linkFunction;
	
	public LikelihoodGLM(LinkFunction linkFunction) {
		super(linkFunction.getOriginalFunction());
		this.linkFunction = linkFunction;
	}

	@Override
	public Matrix getPredictionVector() {
		Matrix mat = new Matrix(1,1);
		mat.setValueAt(0, 0, linkFunction.getValue());
		return mat;
	}
	
	
	@Override
	public Double getValue() {
		double predicted = getPredictionVector().getValueAt(0, 0);
		if (observedValues.getValueAt(0, 0) == 1d) {
			return predicted;
		} else {
			return 1d - predicted; 
		}
	}

	@Override
	public Matrix getGradient() {
		Matrix lfGradient = linkFunction.getGradient();
		if (observedValues.getValueAt(0, 0) == 1d) {
			return lfGradient;
		} else {
			return lfGradient.scalarMultiply(-1d);
		}
	}

	@Override
	public Matrix getHessian() {
		Matrix lfHessian = linkFunction.getHessian();
		if (observedValues.getValueAt(0, 0) == 1d) {
			return lfHessian;
		} else {
			return lfHessian.scalarMultiply(-1d);
		}
	}


}
