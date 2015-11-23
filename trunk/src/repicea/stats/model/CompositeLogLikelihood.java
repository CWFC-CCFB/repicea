package repicea.stats.model;

import repicea.math.Matrix;
import repicea.math.MatrixUtility;
import repicea.stats.AbstractStatisticalExpression;

@SuppressWarnings("serial")
public class CompositeLogLikelihood extends AbstractStatisticalExpression {

	
	private final LogLikelihood innerLogLikelihoodFunction;
	private Matrix yValues;
	private Matrix xValues;
	
	public CompositeLogLikelihood(LogLikelihood innerLogLikelihoodFunction) {
		this.innerLogLikelihoodFunction = innerLogLikelihoodFunction;
	}
		
	@Override
	public Double getValue() {
		double loglikelihood = 0;
		for (int i = 0; i < xValues.m_iRows; i++) {
			setValuesInLikelihoodFunction(i);
			loglikelihood += innerLogLikelihoodFunction.getValue();
		}
		return loglikelihood;
	}

	@Override
	public Matrix getGradient() {
		Matrix resultingGradient = new Matrix(getNumberOfParameters(), 1);
		for (int i = 0; i < xValues.m_iRows; i++) {
			setValuesInLikelihoodFunction(i);
			MatrixUtility.add(resultingGradient, innerLogLikelihoodFunction.getGradient());
		}
		return resultingGradient;
	}

	@Override
	public Matrix getHessian() {
		Matrix resultingHessian = new Matrix(getNumberOfParameters(), getNumberOfParameters());
		for (int i = 0; i < xValues.m_iRows; i++) {
			setValuesInLikelihoodFunction(i);
			MatrixUtility.add(resultingHessian, innerLogLikelihoodFunction.getHessian());
		}
		return resultingHessian;
	}

	protected void setValuesInLikelihoodFunction(int index) {
		innerLogLikelihoodFunction.getOriginalFunction().setX(xValues.getSubMatrix(index, index, 0, xValues.m_iCols - 1));
		innerLogLikelihoodFunction.getOriginalFunction().setObservedValue(yValues.m_afData[index][0]);
	}
	
}
