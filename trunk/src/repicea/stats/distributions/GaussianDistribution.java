/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.distributions;

import repicea.math.Matrix;
import repicea.stats.CentralMomentsSettable;
import repicea.stats.Distribution;

/**
 * This class implements the Gaussian probability density function.
 * @author Mathieu Fortin - August 2012
 */
//public final class GaussianDistribution<N extends Number> extends AbstractMathematicalFunction<FunctionParameter, N, FunctionVariable, N> implements Distribution<N>, CentralMomentsSettable<N> {
@SuppressWarnings("serial")
public class GaussianDistribution extends StandardGaussianDistribution implements CentralMomentsSettable<Matrix> {


//	protected static enum FunctionParameter {Mu, Sigma2}
//	protected static enum FunctionVariable {yVector}

//	private N mu;
//	private N sigma2;
//	private N lowerCholTriangle;
		
	/**
	 * This constructor creates a Gaussian function with mean mu and variance sigma2. NOTE: Matrix sigma2 must be 
	 * positive definite.
	 * @param mu the mean of the function
	 * @param sigma2 the variance of the function
	 * @throws UnsupportedOperationException if the matrix sigma2 is not positive definite
	 */
	public GaussianDistribution(Matrix mu, Matrix sigma2) {
		setMean(mu);
		setVariance(sigma2);
	}
	
	@Override
	public boolean isMultivariate() {
		return getMean().m_iRows > 1;
	}
	
//	@SuppressWarnings("unchecked")
//	@Override
//	public N getRandomRealization() {
//		if (getStandardDeviation() instanceof Matrix) {
//			Matrix mean = (Matrix) getMean();
//			Matrix standardDeviation = (Matrix) getStandardDeviation();
//			Matrix normalStandardDeviates = StatisticalUtility.drawRandomVector(standardDeviation.m_iRows, Distribution.Type.GAUSSIAN);
//			return (N) mean.add(standardDeviation.multiply(normalStandardDeviates));
//		} else {
//			double mean = (Double) getMean();
//			double standardDeviation = (Double) getStandardDeviation();
//			double deviate = StatisticalUtility.getRandom().nextGaussian();
//			return (N) ((Double) (mean + standardDeviation * deviate));
//		}
//	}

//	/**
//	 * This method returns the lower triangle of the Cholesky decomposition of the variance-covariance matrix.
//	 * @return a Matrix instance
//	 */
//	@SuppressWarnings("unchecked")
//	public N getStandardDeviation() {
//		if (lowerCholTriangle == null) {
//			if (getVariance() instanceof Matrix) {
//				lowerCholTriangle = (N) ((Matrix) getVariance()).getLowerCholTriangle();
//			} else {
//				lowerCholTriangle = (N) ((Double) Math.sqrt(getVariance().doubleValue()));
//			}
//		}
//		return lowerCholTriangle;
//	}
	
//	@Override
//	public N getMean() {
//		return mu;
//	}
//
//	@Override
//	public N getVariance() {
//		return sigma2;
//	}

	@Override
	public Type getType() {return Distribution.Type.GAUSSIAN;}

	@Override
	public void setMean(Matrix mean) {
		super.setMean(mean);
	}

	@Override
	public void setVariance(Matrix variance) {
		super.setVariance(variance);
	}

	@Override
	public boolean isParametric() {
		return true;
	}

//	@Override
//	public List<double[]> getQuantile(List<double[]> probabilities) {
//		if (probabilities == null || probabilities.isEmpty()) {
//			throw new InvalidParameterException("The probabilities parameter is null or empty!");
//		} else if (isMultivariate() && probabilities.size() != ((Matrix) getMean()).m_iRows) {
//			throw new InvalidParameterException("The number of values does not correspond to the dimension of the distribution!");
//		} else {
//			List<double[]> output = new ArrayList<double[]>();
//			if (!isMultivariate()) {
//				double[] probabilityLevels = probabilities.get(0);
//				double[] quantiles = new double[probabilityLevels.length];
//				for (int i = 0; i < probabilityLevels.length; i++) {
//					double standardizedValue = (values[0] - getMean().m_afData[0][0]) / Math.sqrt(getVariance().m_afData[0][0]);
//				}
////				return GaussianUtility.getCumulativeProbability(standardizedValue);
////			} else if (values.length == 2) {
////				double std1 = Math.sqrt(getVariance().m_afData[0][0]);
////				double standardizedValue1 = (values[0] - getMean().m_afData[0][0]) / std1;
////				double std2 = Math.sqrt(getVariance().m_afData[1][1]);
////				double standardizedValue2 = (values[1] - getMean().m_afData[1][0]) / std2;
////				double correlation = getVariance().m_afData[0][1] / (std1 * std2);
////				return GaussianUtility.getBivariateCumulativeProbability(standardizedValue1, standardizedValue2, correlation);
////			}
////		}
////		return -1;
//		}
//		return null;
//	}

	
//	public static void main(String[] args) {
//		GaussianFunction gf = new GaussianFunction();
//		gf.setParameterValue(FunctionParameter.Mu, new Matrix(2,1));
//		gf.setParameterValue(FunctionParameter.Sigma2, Matrix.getIdentityMatrix(2));
//		gf.setVariableValue(FunctionVariable.yVector, new Matrix(2,1));
//		double allo = gf.getValue();
//	}

}
