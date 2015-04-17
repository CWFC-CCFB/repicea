package repicea.stats.distributions;

import org.apache.commons.math.random.RandomDataImpl;


public class GammaDistributionTest {

	public static void main(String[] args) {
		double dDispersionGamma = 0.61766452969979;
		double fGammaMean = 1.813311388045523;
		double scale = dDispersionGamma;
		double shape = fGammaMean / dDispersionGamma;
		RandomDataImpl randomGenerator = new RandomDataImpl();
		
		double mean = 0;
		int maxIter = 100000;
		double meanFactor = 1d / maxIter;
		for (int i = 0; i < maxIter; i++) {
			double randomDeviate = 0;
			try {
				randomDeviate = randomGenerator.nextGamma(shape, scale);
				mean += randomDeviate * meanFactor;
			} catch (Exception e) {
			}

		}
		
		System.out.println ("Simulated mean = " + mean + "; Expected mean = " + fGammaMean);
		System.exit(0);
		
	}
}
