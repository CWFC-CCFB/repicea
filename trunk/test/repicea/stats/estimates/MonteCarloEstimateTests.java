package repicea.stats.estimates;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;

public class MonteCarloEstimateTests {

	
	@Test
	public void percentilesTest() {
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Random random = new Random();
		Matrix m;
		for (int i = 0; i < 1000000; i++) {
			m = new Matrix(2,1);
			m.m_afData[0][0] = random.nextDouble();
			m.m_afData[1][0] = random.nextDouble() * 2;
			estimate.addRealization(m);
		}
		
		Matrix percentiles = estimate.getPercentile(0.95);
		Assert.assertEquals(0.95, percentiles.m_afData[0][0], 1E-3);
		Assert.assertEquals(0.95 * 2, percentiles.m_afData[1][0], 2E-3);
	}
	
	@Test
	public void univariateTotalVarianceLawTest() {
		double outerStd = 5d;
		double innerStd = 5d;
		int sampleSize = 100;
		Random generator = new Random();
		LawOfTotalVarianceMonteCarloEstimate output = new LawOfTotalVarianceMonteCarloEstimate();
		for (int i = 0; i < 10000; i++) {
			double meanForThisRealization = generator.nextGaussian() * outerStd;
			SampleMeanEstimate estimate = new SampleMeanEstimate();
			Matrix obs;
			for (int j = 0; j < sampleSize; j++) {
				obs = new Matrix(1,1);
				obs.m_afData[0][0] = generator.nextGaussian() * innerStd + meanForThisRealization; 
				estimate.addObservation(obs);
			}
			output.addRealization(estimate);
		}
		double totalVariance = output.getVariance().m_afData[0][0];
		double expectedVariance = outerStd * outerStd + innerStd * innerStd / sampleSize;
		double relativeDifference = (totalVariance - expectedVariance)/expectedVariance; 
		Assert.assertEquals(0d, relativeDifference, 1E-2);
	}
	
}
