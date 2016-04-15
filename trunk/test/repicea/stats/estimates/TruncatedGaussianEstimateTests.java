package repicea.stats.estimates;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;

public class TruncatedGaussianEstimateTests {

	@Test
	public void comparingMeanPositiveHalfDistributionTest() {
		TruncatedGaussianEstimate estimate = new TruncatedGaussianEstimate();
		estimate.setLowerBoundValue(new Matrix(1,1));
		double actualMean = 0d;
		int nbRealizations = 100000;
		double fact = 1d/nbRealizations;
		for (int i = 0; i < nbRealizations; i++) {
			actualMean += estimate.getRandomDeviate().m_afData[0][0] * fact;
		}
		double expectedMean = Math.sqrt(2) / Math.sqrt(Math.PI);
		Assert.assertEquals("Comparing mean of half-normal distribution", expectedMean, actualMean, 5E-3);}
	
	@Test
	public void comparingMeanNegativeHalfDistributionTest() {
		TruncatedGaussianEstimate estimate = new TruncatedGaussianEstimate();
		estimate.setUpperBoundValue(new Matrix(1,1));
		double actualMean = 0d;
		int nbRealizations = 100000;
		double fact = 1d/nbRealizations;
		for (int i = 0; i < nbRealizations; i++) {
			actualMean += estimate.getRandomDeviate().m_afData[0][0] * fact;
		}
		double expectedMean = - Math.sqrt(2) / Math.sqrt(Math.PI);
		Assert.assertEquals("Comparing mean of half-normal distribution", expectedMean, actualMean, 5E-3);
	}

}
