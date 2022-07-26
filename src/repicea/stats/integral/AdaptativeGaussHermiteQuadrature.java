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
	
	/**
	 * Constructor.
	 * @param numberOfPoints the number of points the integral is based on.
	 */
	public AdaptativeGaussHermiteQuadrature(NumberOfPoints numberOfPoints) {
		super(numberOfPoints);
	}
	
	/**
	 * Constructor for the LaplaceApproximation class.
	 */
	AdaptativeGaussHermiteQuadrature() {
		super();
	}
	
	
	@Override
	public double getIntegralApproximation(AbstractMathematicalFunction functionToEvaluate,
											List<Integer> indices, 
											boolean isParameter,
											Matrix lowerCholeskyTriangle) {
		if (!isParameter) {
			throw new UnsupportedOperationException("The AdaptativeGaussianHermiteQuadrature class has not been implemented for integral over the variables yet!");
		}
		if (!lowerCholeskyTriangle.isSquare() || indices.size() != lowerCholeskyTriangle.m_iRows) {
			throw new InvalidParameterException("The indices are not compatible with the lower Cholesky triangle!");
		} else {
			int maxIndex = functionToEvaluate.getNumberOfParameters();
			for (Integer index : indices) {
				if (index < 0 || index >= maxIndex) {
					throw new InvalidParameterException("One index is either negative or it exceeds the number of parameters in the function!");
				}
			}
			Matrix matrixG = lowerCholeskyTriangle.multiply(lowerCholeskyTriangle.transpose());
			InternalLogWrapperFunction functionToBeOptimized = new InternalLogWrapperFunction(functionToEvaluate, indices, matrixG);
			
			NewtonRaphsonOptimizer nro = new NewtonRaphsonOptimizer();
			try {
				nro.optimize(functionToBeOptimized, indices);
			} catch (OptimizationException e) {
				e.printStackTrace();
			}

			Matrix newHessian = nro.getHessianAtMaximum();
			Matrix varCov = newHessian.getInverseMatrix().scalarMultiply(-1d);
			Matrix newLowerCholeskyTriangle = varCov.getLowerCholTriangle();

			ExponentialFunctionWrapper efw = new ExponentialFunctionWrapper(functionToBeOptimized);
			double approximation = super.getMultiDimensionIntegral(efw, indices, isParameter, newLowerCholeskyTriangle);
			int dimensions = newHessian.m_iRows;
			approximation *= Math.pow(2d, (2 * dimensions - 1) * .5) * Math.sqrt(varCov.getDeterminant());
			return approximation;
		}
	}

	@Override
	protected double getOneDimensionIntegral(AbstractMathematicalFunction functionToEvaluate,
			int index,
			boolean isParameter,
			double standardDeviation) {
		double originalValue = functionToEvaluate.getParameterValue(index);
		double sum = 0;
		double value;
		for (int i = 0; i < getXValues().size(); i++) {
			double z = getXValues().get(i);
			double tmp =  z * standardDeviation * Math.sqrt(2d);
			functionToEvaluate.setParameterValue(index, originalValue + tmp);
			value = functionToEvaluate.getValue() * Math.exp(z*z) * getWeights().get(i);
			sum += value;
		}
		functionToEvaluate.setParameterValue(index, originalValue);
		return sum;
	}

	
	
}
