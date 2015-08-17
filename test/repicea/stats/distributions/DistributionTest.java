package repicea.stats.distributions;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.estimates.MonteCarloEstimate;

public class DistributionTest {

	/*
	 * This test uses a Multivariate Gaussian distribution as a reference and compute a Monte Carlo estimator. The mean and the variances are then compared.
	 */
	@Test
	public void stochasticSimulationFromGaussianDistribution() {
		int nbReal = 1000000;
		NonparametricDistribution npDist = new NonparametricDistribution(); 
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
	
	/*
	 * This test uses a univariate truncated Gaussian distribution as a reference and compute a Monte Carlo estimator. The mean and the variances are then compared.
	 */
	@Test
	public void stochasticSimulationFromTruncatedGaussianDistribution() {
		int nbReal = 1000000;
		
		TruncatedGaussianDistribution distribution = new TruncatedGaussianDistribution();
		distribution.setUpperBoundValue(new Matrix(1,1));
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		
		for (int i = 0; i < nbReal; i++) {
			estimate.addRealization(distribution.getRandomRealization());
		}
		
		Matrix simulatedMean = estimate.getMean();
		Matrix simulatedVariance = estimate.getVariance();
		Matrix expectedMean = distribution.getMean();
		Matrix expectedVariance = distribution.getVariance();
		Assert.assertEquals("Testing the means", expectedMean.m_afData[0][0], simulatedMean.m_afData[0][0], 1E-3);
		Assert.assertEquals("Testing the variances", expectedVariance.m_afData[0][0], simulatedVariance.m_afData[0][0], 1E-3);

	}

}
