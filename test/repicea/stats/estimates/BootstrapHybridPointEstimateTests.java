package repicea.stats.estimates;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;
import repicea.stats.sampling.PopulationUnitWithEqualInclusionProbability;

public class BootstrapHybridPointEstimateTests {

	private static final Random RANDOM = new Random();
	
	@Test
	public void simpleTestWithoutModelVariability() {
		PopulationMeanEstimate pe = new PopulationMeanEstimate();
		Matrix obs;
		for (int i = 0; i < 50; i++) {
			obs = new Matrix(1,1);
			obs.m_afData[0][0] = RANDOM.nextGaussian() * 2 + 12;
			pe.addObservation(new PopulationUnitWithEqualInclusionProbability(obs));
		}

		BootstrapHybridPointEstimate bhpe = new BootstrapHybridPointEstimate(); 
		for (int i = 0; i < 1000; i++) {
			bhpe.addPointEstimate(pe);
		}
		
		double expectedMean = pe.getMean().m_afData[0][0];
		double actualMean = bhpe.getMean().m_afData[0][0];
		
		Assert.assertEquals("Testing mean estimates", expectedMean, actualMean, 1E-8);
		
		
		double expectedVariance = pe.getVariance().m_afData[0][0];
		double actualVariance = bhpe.getVariance().m_afData[0][0];
		System.out.println("Expected variance = " + expectedVariance + " - actual variance = " + actualVariance);
		
		Assert.assertEquals("Testing variance estimates", expectedVariance, actualVariance, 1E-8);
	}
	

	
	@Test
	public void simpleTestWithoutSamplingVariability() {
		BootstrapHybridPointEstimate bhpe = new BootstrapHybridPointEstimate(); 
		for (int i = 0; i < 100000; i++) {
			PopulationMeanEstimate pe = new PopulationMeanEstimate();
			double deviate = RANDOM.nextGaussian() * 2 + 12;
			Matrix obs;
			for (int j = 0; j < 50; j++) {
				obs = new Matrix(1,1);
				obs.m_afData[0][0] = deviate;
				pe.addObservation(new PopulationUnitWithEqualInclusionProbability(obs));
			}
			bhpe.addPointEstimate(pe);
		}
		
		double expectedMean = 12d;
		double actualMean = bhpe.getMean().m_afData[0][0];
		System.out.println("Expected mean = " + expectedMean + " - actual mean = " + actualMean);
		Assert.assertEquals("Testing mean estimates", expectedMean, actualMean, 1E-1);
		
		
		double expectedVariance = 4d;
		double actualVariance = bhpe.getVariance().m_afData[0][0];
		System.out.println("Expected variance = " + expectedVariance + " - actual variance = " + actualVariance);
		
		Assert.assertEquals("Testing variance estimates", expectedVariance, actualVariance, 1E-1);
	}

	
// 	FIXME this test is not properly built	MF2019-07-29
//	@Test
//	public void simpleTestWithCompleteVariability() {
//
//		BootstrapHybridPointEstimate bhpe = new BootstrapHybridPointEstimate(); 
//		for (int i = 0; i < 1000; i++) {
//			PopulationMeanEstimate pe = new PopulationMeanEstimate();
//			Matrix obs;
//			double mean = 12 + RANDOM.nextGaussian() * .5; 
//			for (int j = 0; j < 100; j++) {
//				obs = new Matrix(1,1);
//				obs.m_afData[0][0] = RANDOM.nextGaussian() * 4 + mean;
//				pe.addObservation(new PopulationUnitWithEqualInclusionProbability(obs));
//			}
//			bhpe.addPointEstimate(pe);
//		}
//		
//		double expectedMean = 12d;
//		double actualMean = bhpe.getMean().m_afData[0][0];
//		
//		Assert.assertEquals("Testing mean estimates", expectedMean, actualMean, 1E-1);
//		
//		
//		double expectedVariance = 0.25;
//		VariancePointEstimate varPointEstimate = bhpe.getVarianceOfTotalEstimate();
//		System.out.println("Model-related variance = " + varPointEstimate.getModelRelatedVariance().m_afData[0][0]);
//		System.out.println("Sampling-related variance = " + varPointEstimate.getSamplingRelatedVariance().m_afData[0][0]);
//		double actualVariance = varPointEstimate.getTotalVariance().m_afData[0][0];
//		
//		Assert.assertEquals("Testing variance estimates", expectedVariance, actualVariance, 1E-1);
//	}

	
	
}
