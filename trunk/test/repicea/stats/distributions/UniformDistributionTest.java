package repicea.stats.distributions;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;

public class UniformDistributionTest {

	@Test
	public void simpleMeanTest() {
		Matrix lowerBound = new Matrix(2,1);
		lowerBound.m_afData[0][0] = -2d;
		lowerBound.m_afData[1][0] = 1d;
			
		Matrix upperBound = new Matrix(2,1);
		upperBound.m_afData[0][0] = 1d;
		upperBound.m_afData[1][0] = 3d;
		
		UniformDistribution dist = new UniformDistribution(lowerBound, upperBound);
		Matrix mean = dist.getMean();
		Assert.assertEquals(-.5, mean.m_afData[0][0], 1E-8);
		Assert.assertEquals(2d, mean.m_afData[1][0], 1E-8);
	}
	
	
	@Test
	public void simpleVarianceTest() {
		Matrix lowerBound = new Matrix(2,1);
		lowerBound.m_afData[0][0] = -2d;
		lowerBound.m_afData[1][0] = 1d;
			
		Matrix upperBound = new Matrix(2,1);
		upperBound.m_afData[0][0] = 1d;
		upperBound.m_afData[1][0] = 3d;
		
		UniformDistribution dist = new UniformDistribution(lowerBound, upperBound);
		Matrix variance = dist.getVariance();
		Assert.assertEquals(.75, variance.m_afData[0][0], 1E-8);
		Assert.assertEquals(1d/3, variance.m_afData[1][1], 1E-8);
	}

	
	
	@Test
	public void simpleMonteCarloRandomDeviateTest() {
		Matrix lowerBound = new Matrix(2,1);
		lowerBound.m_afData[0][0] = -2d;
		lowerBound.m_afData[1][0] = 1d;
			
		Matrix upperBound = new Matrix(2,1);
		upperBound.m_afData[0][0] = 1d;
		upperBound.m_afData[1][0] = 3d;
		
		UniformDistribution dist = new UniformDistribution(lowerBound, upperBound);
		int nbMc = 1000000;
		Matrix meanMC = new Matrix(2,1); 
		for (int i = 0; i < nbMc; i++) {
			meanMC = meanMC.add(dist.getRandomRealization());
		}
		meanMC = meanMC.scalarMultiply(1d/nbMc);
		Matrix mean = dist.getMean();
		Matrix relativeResult = meanMC.elementWiseDivide(mean);
		Assert.assertEquals(1d, relativeResult.m_afData[0][0], 3E-3);
		Assert.assertEquals(1d, relativeResult.m_afData[1][0], 3E-3);
	}

	
}
