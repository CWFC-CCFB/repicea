package repicea.stats.model;

import repicea.math.AbstractMathematicalFunctionWrapper;
import repicea.math.Matrix;
import repicea.math.MatrixUtility;

public class SimpleCompositeLogLikelihood extends AbstractMathematicalFunctionWrapper implements CompositeLogLikelihood {

	private final Matrix yValues;
	
	public SimpleCompositeLogLikelihood(IndividualLogLikelihood innerLogLikelihoodFunction, Matrix yValues) {
		super(innerLogLikelihoodFunction);
		this.yValues = yValues;
	}
		
	@Override
	public IndividualLogLikelihood getOriginalFunction() {return (IndividualLogLikelihood) super.getOriginalFunction();}
	
	@Override
	public Double getValue() {
		double loglikelihood = 0;
		for (int i = 0; i < yValues.m_iRows; i++) {
			setValuesInLikelihoodFunction(i);
			loglikelihood += getOriginalFunction().getValue();
		}
		return loglikelihood;
	}

	@Override
	public Matrix getGradient() {
		Matrix resultingGradient = new Matrix(getNumberOfParameters(), 1);
		for (int i = 0; i < yValues.m_iRows; i++) {
			setValuesInLikelihoodFunction(i);
			MatrixUtility.add(resultingGradient, getOriginalFunction().getGradient());
		}
		return resultingGradient;
	}

	@Override
	public Matrix getHessian() {
		Matrix resultingHessian = new Matrix(getNumberOfParameters(), getNumberOfParameters());
		for (int i = 0; i < yValues.m_iRows; i++) {
			setValuesInLikelihoodFunction(i);
			MatrixUtility.add(resultingHessian, getOriginalFunction().getHessian());
		}
		return resultingHessian;
	}

	protected void setValuesInLikelihoodFunction(int index) {
		getOriginalFunction().setYVector(yValues.getSubMatrix(index, index, 0, 0));
	}

		
	@Override
	public void setParameters(Matrix beta) {
		getOriginalFunction().setParameters(beta);
	}

	@Override
	public Matrix getParameters() {return getOriginalFunction().getParameters();}
	
	
}
