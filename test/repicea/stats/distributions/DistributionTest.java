package repicea.stats.distributions;

import org.junit.Assert;

import org.junit.Test;

import repicea.math.Matrix;
import repicea.stats.estimates.GaussianEstimate;

public class DistributionTest {

	/*
	 * This test uses a Multivariate Gaussian distribution as a reference and compute a Monte Carlo estimator. The mean and the variances are then compared.
	 */
	@Test
	public void StochasticSimulationFromGaussianDistribution() {
		int nbReal = 100000;
		NonparametricDistribution<Matrix> npDist = new NonparametricDistribution<Matrix>(); 
		Matrix mean = new Matrix(2,1);
		mean.m_afData[0][0] = 2d;
		mean.m_afData[1][0] = 3d;
		Matrix variance = new Matrix(2,2);
		variance.m_afData[0][0] = 0.5;
		variance.m_afData[1][0] = 0.4;
		variance.m_afData[0][1] = 0.4;
		variance.m_afData[1][1] = 1d;
		
		GaussianEstimate estimate = new GaussianEstimate(mean, variance);
		
		for (int i = 0; i < nbReal; i++) {
			npDist.addRealization(estimate.getRandomDeviate());
		}
		
		Matrix simulatedMean = npDist.getMean();
		Matrix res = simulatedMean.subtract(mean);
		double sse = res.transpose().multiply(res).m_afData[0][0];
		System.out.println("Squared difference of the means = " + sse);
		
		Assert.assertEquals("Testing the means", 0d, sse, 1E-4);

		Matrix simulatedVariances = npDist.getVariance();
		Matrix diff = simulatedVariances.subtract(variance);
		sse = diff.elementWiseMultiply(diff).getSumOfElements();
		System.out.println("Squared differentce of the variances = " + sse);

		Assert.assertEquals("Testing the variances", 0d, sse, 1E-4);

	}
	
	
}
