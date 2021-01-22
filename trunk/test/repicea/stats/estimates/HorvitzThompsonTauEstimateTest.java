package repicea.stats.estimates;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;
import repicea.stats.sampling.PopulationUnitWithUnequalInclusionProbability;

public class HorvitzThompsonTauEstimateTest {

	@Test
	public void simpleTotalAndVarianceEstimateTest() {
		int populationSize = 1000;
		List<Double> sample = new ArrayList<Double>();
		sample.add(2d);
		sample.add(4d);
		sample.add(2d);
		sample.add(5d);
		sample.add(7d);
		sample.add(1d);
		sample.add(5d);
		sample.add(4d);
		sample.add(7d);
		PopulationTotalEstimate estimate = new PopulationTotalEstimate();
		Matrix obs;
		for (Double value : sample) {
			obs = new Matrix(1,1);
			obs.m_afData[0][0] = value;
			estimate.addObservation(new PopulationUnitWithUnequalInclusionProbability(obs, 1d/populationSize));
		}
		Matrix total = estimate.getMean();
		Assert.assertEquals("Testing the estimate of the total", 4111.11111111111, total.m_afData[0][0], 1E-8);
		Matrix totalVariance = estimate.getVariance();
		Assert.assertEquals("Testing the variance of the total", 507734.5679, totalVariance.m_afData[0][0], 1E-4);
	}
	
	
	
	
}
