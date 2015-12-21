/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.stats.integral;

import java.security.InvalidParameterException;
import java.util.List;

import repicea.math.AbstractMathematicalFunction;
import repicea.math.ExponentialFunctionWrapper;
import repicea.math.LogFunctionWrapper;
import repicea.math.Matrix;
import repicea.math.optimizer.AbstractOptimizer.OptimizationException;
import repicea.math.optimizer.NewtonRaphsonOptimizer;

/**
 * The AdaptativeGaussHermiteQuadrature class implements the adaptative algorithm, i.e.
 * the maximum of the function is first found before integrating.
 * @author Mathieu Fortin - December 2015
 *
 */
@SuppressWarnings("serial")
public class AdaptativeGaussHermiteQuadrature extends GaussHermiteQuadrature {
	
	private static class CustomizedLogWrapperFunction extends LogFunctionWrapper {

//		private final Matrix gMatrix;
		private final double n;
		private final double gDeterminant;
		private final Matrix invG;
		private final Matrix originalParameterValues;
		private final List<Integer> parameterIndices;
		
		/**
		 * Constructor
		 * @param originalFunction the nested function
		 * @param gMatrix the variance-covariance matrix of the deviate
		 */
		private CustomizedLogWrapperFunction(AbstractMathematicalFunction originalFunction, List<Integer> parameterIndices, Matrix gMatrix) {
			super(originalFunction);
			if (!gMatrix.isSymmetric()) {
				throw new InvalidParameterException("Matrix G is supposed to be symmetric!");
			}
//			this.gMatrix = gMatrix;
			this.invG = gMatrix.getInverseMatrix();
			this.n = gMatrix.m_iRows;
			this.gDeterminant = gMatrix.getDeterminant();
			this.parameterIndices = parameterIndices;
			originalParameterValues = getParametersFromNestedFunction();
 		}
		
		@Override
		public Double getValue() {
			Matrix u = getParametersFromNestedFunction().subtract(originalParameterValues);
			return super.getValue() - 0.5 * n * Math.log(2d * Math.PI) 
					- 0.5 * Math.log(gDeterminant) - 0.5 * u.transpose().multiply(invG).multiply(u).m_afData[0][0]; 
		}
		
		
		@Override
		public Matrix getGradient() {
			Matrix u = getParametersFromNestedFunction().subtract(originalParameterValues);
			return super.getGradient().subtract(u.transpose().multiply(invG));
		}
		

		@Override
		public Matrix getHessian() {
			return super.getHessian().subtract(invG);
		}

		private Matrix getParametersFromNestedFunction() {
			Matrix parameterValues = new Matrix(parameterIndices.size(), 1);
			for (int i = 0; i < parameterIndices.size(); i++) {
				parameterValues.m_afData[i][0] = getOriginalFunction().getParameterValue(parameterIndices.get(i));
			}
			return parameterValues;
		}
		
	}

	
	
	/**
	 * Constructor.
	 * @param numberOfPoints the number of points the integral is based on.
	 */
	public AdaptativeGaussHermiteQuadrature(NumberOfPoints numberOfPoints) {
		super(numberOfPoints);
	}

	
	
	/**
	 * This method returns the value of a multi-dimension integral
	 * @param functionToEvaluate an EvaluableFunction instance that returns Double 
	 * @param parameterIndices the indices of the parameters over which the integration is made
	 * @param lowerCholeskyTriangle the lower triangle of the Cholesky factorization of the variance-covariance matrix
	 * @return the approximation of the integral
	 */
	@Override
	public double getIntegralApproximation(AbstractMathematicalFunction functionToEvaluate,
											List<Integer> parameterIndices, 
											Matrix lowerCholeskyTriangle) {
		if (!lowerCholeskyTriangle.isSquare() || parameterIndices.size() != lowerCholeskyTriangle.m_iRows) {
			throw new InvalidParameterException("The indices are not compatible with the lower Cholesky triangle!");
		} else {
			for (Integer index : parameterIndices) {
				if (index < 0 || index >= functionToEvaluate.getNumberOfParameters()) {
					throw new InvalidParameterException("One index is either negative or it exceeds the number of parameters in the function!");
				}
			}
			Matrix matrixG = lowerCholeskyTriangle.multiply(lowerCholeskyTriangle.transpose());
			CustomizedLogWrapperFunction functionToBeOptimized = new CustomizedLogWrapperFunction(functionToEvaluate, parameterIndices, matrixG);
			
			NewtonRaphsonOptimizer nro = new NewtonRaphsonOptimizer();
			try {
				nro.optimize(functionToBeOptimized, parameterIndices);
			} catch (OptimizationException e) {
				e.printStackTrace();
			}

			Matrix newHessian = nro.getHessianAtMaximum();
			Matrix varCov = newHessian.getInverseMatrix().scalarMultiply(-1d);
			Matrix newLowerCholeskyTriangle = varCov.getLowerCholTriangle();

			ExponentialFunctionWrapper efw = new ExponentialFunctionWrapper(functionToBeOptimized);
			double approximation = super.getMultiDimensionIntegral(efw, parameterIndices, newLowerCholeskyTriangle);
			approximation *= Math.sqrt(2d) * Math.pow(newHessian.scalarMultiply(-1d).getDeterminant(), -.5);
			return approximation;
		}
	}

	@Override
	protected double getOneDimensionIntegral(AbstractMathematicalFunction functionToEvaluate,
			Integer parameterIndex, 
			double standardDeviation) {
		double originalValue = functionToEvaluate.getParameterValue(parameterIndex);
		double sum = 0;
		double value;
		for (int i = 0; i < getXValues().size(); i++) {
			double z = getXValues().get(i);
			double tmp =  z * standardDeviation * Math.sqrt(2d);
			functionToEvaluate.setParameterValue(parameterIndex, originalValue + tmp);
			value = functionToEvaluate.getValue() * Math.exp(z*z) * getWeights().get(i);
			sum += value;
		}
		functionToEvaluate.setParameterValue(parameterIndex, originalValue);
		return sum;
	}



//	private Matrix findOptimum(AbstractMathematicalFunction functionToEvaluate, List<Integer> parameterIndices, Matrix gMatrix) {
//		CustomizedLogWrapperFunction functionToBeOptimized = new CustomizedLogWrapperFunction(functionToEvaluate, parameterIndices, gMatrix);
//		NewtonRaphsonOptimizer nro = new NewtonRaphsonOptimizer();
//		try {
//			nro.optimize(functionToBeOptimized, parameterIndices);
//		} catch (OptimizationException e) {
//			e.printStackTrace();
//		}
//		return nro.getHessianAtMaximum();
//	}

	
	
	
}
