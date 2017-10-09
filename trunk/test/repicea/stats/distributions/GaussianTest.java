package repicea.stats.distributions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import repicea.stats.distributions.utility.GaussianUtility;

public class GaussianTest {

	@Test
	public void bivariateCumulativeProbabilitiesTest() {
		double x1 = -0.2876339;
		double x2 = 1.125041;
		double rho = -0.6018568;
		double biv11 = GaussianUtility.getBivariateCumulativeProbability(x1, x2, false, false, rho);
		assertEquals(0.2832904938411274, biv11, 1E-10);

		double biv01 = GaussianUtility.getBivariateCumulativeProbability(x1, x2, true, false, rho);
		assertEquals(0.5864236757641986, biv01, 1E-10);

		double biv10 = GaussianUtility.getBivariateCumulativeProbability(x1, x2, false, true, rho);
		assertEquals(0.10352300242645603, biv10, 1E-10);

		double biv00 = GaussianUtility.getBivariateCumulativeProbability(x1, x2, true, true, rho);
		assertEquals(0.026762827968218002, biv00, 1E-10);
	}
	
	@Test
	public void quantileTests() {
		for (int i = 1; i < 20; i++) {
			double expectedCDFValue = i * .05;
			double quantile = GaussianUtility.getQuantile(expectedCDFValue);
			double cdfValue = GaussianUtility.getCumulativeProbability(quantile);
			assertEquals(expectedCDFValue, cdfValue, 1E-9);
		}
	}

}
