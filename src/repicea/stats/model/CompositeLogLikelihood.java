package repicea.stats.model;

import repicea.math.AbstractMathematicalFunctionWrapper;
import repicea.math.Matrix;
import repicea.math.MatrixUtility;

@SuppressWarnings("serial")
public class CompositeLogLikelihood extends AbstractMathematicalFunctionWrapper {

	
	private Matrix yValues;
	private Matrix xValues;
	
	public CompositeLogLikelihood(IndividualLogLikelihood innerLogLikelihoodFunction, Matrix xValues, Matrix yValues) {
		super(innerLogLikelihoodFunction);
		this.xValues = xValues;
		this.yValues = yValues;
	}
		
	@Override
	public IndividualLogLikelihood getOriginalFunction() {return (IndividualLogLikelihood) super.getOriginalFunction();}
	
	@Override
	public Double getValue() {
		double loglikelihood = 0;
		for (int i = 0; i < xValues.m_iRows; i++) {
			setValuesInLikelihoodFunction(i);
			loglikelihood += getOriginalFunction().getValue();
		}
		return loglikelihood;
	}

	@Override
	public Matrix getGradient() {
		Matrix resultingGradient = new Matrix(getNumberOfParameters(), 1);
		for (int i = 0; i < xValues.m_iRows; i++) {
			setValuesInLikelihoodFunction(i);
			MatrixUtility.add(resultingGradient, getOriginalFunction().getGradient());
		}
		return resultingGradient;
	}

	@Override
	public Matrix getHessian() {
		Matrix resultingHessian = new Matrix(getNumberOfParameters(), getNumberOfParameters());
		for (int i = 0; i < xValues.m_iRows; i++) {
			setValuesInLikelihoodFunction(i);
			MatrixUtility.add(resultingHessian, getOriginalFunction().getHessian());
		}
		return resultingHessian;
	}

	protected void setValuesInLikelihoodFunction(int index) {
//		getOriginalFunction().getOriginalFunction().setX(xValues.getSubMatrix(index, index, 0, xValues.m_iCols - 1));
//		getOriginalFunction().getOriginalFunction().setYVector(yValues.getSubMatrix(index, index, 0, 0));
		getOriginalFunction().setVariables(xValues.getSubMatrix(index, index, 0, xValues.m_iCols - 1));
		getOriginalFunction().setYVector(yValues.getSubMatrix(index, index, 0, 0));
	}

	/**
	 * This method returns all the predicted values.
	 * @return a Matrix instance
	 */
	public Matrix getPredictions() {
		Matrix predictedValues = new Matrix(yValues.m_iRows, 1);
		for (int i = 0; i < xValues.m_iRows; i++) {
			setValuesInLikelihoodFunction(i);
			predictedValues.setSubMatrix(getOriginalFunction().getPredictionVector(), i, 0);
		}
		return predictedValues;
	}
		
	@Override
	public void setParameters(Matrix beta) {
		getOriginalFunction().setParameters(beta);
	}

	@Override
	public Matrix getParameters() {return getOriginalFunction().getParameters();}
	
	/**
	 * Resets this composite likelihood to its initial values.
	 */
	public void reset() {}

	
}
