package repicea.stats.integral;

import java.security.InvalidParameterException;
import java.util.List;

import repicea.math.EvaluableFunction;
import repicea.math.Matrix;
import repicea.stats.AbstractStatisticalExpression;

@SuppressWarnings("serial")
public class AdaptativeGaussHermiteQuadrature extends GaussHermiteQuadrature {

	public AdaptativeGaussHermiteQuadrature(NumberOfPoints numberOfPoints) {
		super(numberOfPoints);
	}

	
	
	/**
	 * This method returns the value of a multi-dimension integral
	 * @param functionToEvaluate an EvaluableFunction instance that returns Double 
	 * @param functionWithParametersToChange the AbstractStatisticalExpression with the parameters that change
	 * @param parameterIndices the indices of the parameters over which the integration is made
	 * @param lowerCholeskyTriangle the lower triangle of the Cholesky factorization of the variance-covariance matrix
	 * @return the approximation of the integral
	 */
	public double getIntegralApproximation(EvaluableFunction<Double> functionToEvaluate,
											AbstractStatisticalExpression functionWithParametersToChange,
											List<Integer> parameterIndices, 
											Matrix lowerCholeskyTriangle) {
		if (!lowerCholeskyTriangle.isSquare() || parameterIndices.size() != lowerCholeskyTriangle.m_iRows) {
			throw new InvalidParameterException("The indices are not compatible with the lower Cholesky triangle!");
		} else {
			for (Integer index : parameterIndices) {
				if (index < 0 || index >= functionWithParametersToChange.getNumberOfParameters()) {
					throw new InvalidParameterException("One index is either negative or it exceeds the number of parameters in the function!");
				}
			}
			
			findOptimum();
			
			return super.getMultiDimensionIntegral(functionToEvaluate, 
					functionWithParametersToChange, 
					parameterIndices, 
					lowerCholeskyTriangle);
		}
	}



	private void findOptimum() {
		// TODO Auto-generated method stub
	}

	
	
	
}
