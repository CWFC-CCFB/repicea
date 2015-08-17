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

import java.security.InvalidParameterException;


/**
 * The Gaussian class implements common static methods that are related to the density or
 * the cumulative probability of the Gaussian distribution.
 * @author Mathieu Fortin - July 2012
 */
public class GaussianUtility {

	private static final double[] x = {0.04691008, 0.23076534, 0.5, 0.76923466, 0.95308992};
	private static final double[] w = {0.018854042, 0.038088059, 0.0452707394, 0.038088059, 0.018854042};

	/**
	 * This method returns the cumulative probability probability of a bivariate standard normal 
	 * distribution for quantiles x1 and x2. The algorithm was translated from West's code.
	 * @param x1 the first quantile
	 * @param x2 the second quantile
	 * @param rho the correlation parameter
	 * @return the probability (a double)
	 * @see <a href=http://www.wilmott.com/pdfs/090721_west.pdf> West, G. Better approximations to cumulative normal functions.
	 * WILMOTT magazine. 70-76. </a> 
	 */
	public static double getBivariateCumulativeProbability(double x1, double x2, double rho) {

		double h1, h2;
		double lh, h12;
		double h3, h5, h6, h7, h8;
		double r1, r2, r3, rr;
		double aa, ab;

		double bivarcumnorm = Double.NaN;

		h1 = x1;
		h2 = x2;
		h12 = (h1 * h1 + h2 * h2) * .5; 
		if (Math.abs(rho) >= .7) {
			r2 = 1- rho * rho;
			r3 = Math.sqrt(r2);
			if (rho < 0) {
				h2 = - h2;
			}
			h3 = h1 * h2;
			h7 = Math.exp(-h3 * .5);
			if (Math.abs(rho) < 1) {
				h6 = Math.abs(h1 - h2);
				h5 = h6 * h6 *.5;
				h6 = h6 / r3;
				aa = 0.5 - h3 * .125;
				ab = 3 - 2 * aa * h5;
				lh = 0.13298076 * h6 * ab * (1 - getCumulativeProbability(h6)) - Math.exp(- h5 / r2) * (ab + aa + r2) * .053051647;
				lh = 0;
				for (int i = 0; i < x.length; i++) {
					r1 = r3 * x[i];
					rr = r1 * r1;
					r2 = Math.sqrt(1 - rr);
					if (h7 == 0) {
						h8 = 0;
					} else {
						h8 = Math.exp(- h3 / (1 + r2)) / r2 / h7;
					}
					lh += lh - w[i] * Math.exp(-h5 / rr) * (h8 - 1 - aa * rr);
				}
				bivarcumnorm = lh * r3 * h7 + getCumulativeProbability(Math.min(h1, h2));
				if (rho < 0) {
					bivarcumnorm = getCumulativeProbability(h1) - bivarcumnorm;
				}
			}
		} else {
			h3 = h1 * h2;
			lh = 0;
			if (rho != 0d) {
				for (int i = 0; i < x.length; i++) {
					r1 = rho * x[i];
					r2 = 1 - r1 * r1;
					lh += w[i] * Math.exp((r1 * h3 - h12) / r2) / Math.sqrt(r2);
				}
			}
			bivarcumnorm = getCumulativeProbability(h1) * getCumulativeProbability(h2) + rho * lh;
		}
		return bivarcumnorm;
	}


	/**
	 * This method returns the cumulative probability or complementary probability of a bivariate standard normal 
	 * distribution for quantiles x1 and x2. The algorithm was translated from West's code.
	 * @param x1 the first quantile
	 * @param x2 the second quantile
	 * @param complementary1 a boolean true to obtain the complementary probability with respect to quantile x1 or false for the cumulative probability
	 * @param complementary2 a boolean true to obtain the complementary probability with respect to quantile x2 or false for the cumulative probability
	 * @param rho the correlation parameter
	 * @return the probability (a double)
	 * @see <a href=http://www.wilmott.com/pdfs/090721_west.pdf> West, G. Better approximations to cumulative normal functions.
	 * WILMOTT magazine. 70-76. </a> 
	 */
	public static double getBivariateCumulativeProbability(double x1, double x2, boolean complementary1, boolean complementary2, double rho) {
		if (complementary1) {
			x1 = -x1;
		}
		if (complementary2) {
			x2 = - x2;
		}

		if (complementary1 != complementary2) {
			rho = - rho;
		}

		return GaussianUtility.getBivariateCumulativeProbability(x1, x2, rho);

	}	


	/**
	 * This method returns the cumulative probability probability of a standard normal 
	 * distribution for quantile x. The algorithm was translated from West's code.
	 * @param x the quantile
	 * @return the probability (a double)
	 * @see <a href=http://www.wilmott.com/pdfs/090721_west.pdf> West, G. Better approximations to cumulative normal functions.
	 * WILMOTT magazine. 70-76. </a> 
	 */
	public static double getCumulativeProbability(double x) {
		double cumnorm = Double.NaN;
		double xAbs = Math.abs(x);
		if (xAbs > 37) {
			cumnorm = 0;
		} else {
			double exp = Math.exp(- xAbs * xAbs * .5);
			if (exp < 7.07106781186547) {
				double build = 3.52624965998911E-2 * xAbs + 0.700383064443688;
				build = build * xAbs + 6.37396220353165;
				build = build * xAbs + 33.912866078383;
				build = build * xAbs + 112.079291497871;
				build = build * xAbs + 221.213596169931;
				build = build * xAbs + 220.206867912376;
				cumnorm = exp * build;
				build = 8.83883476483184E-2 * xAbs + 1.75566716318264;
				build = build * xAbs + 16.064177579207;
				build = build * xAbs + 86.7807322029461;
				build = build * xAbs + 296.564248779674;
				build = build * xAbs + 637.333633378831;
				build = build * xAbs + 793.826512519948;
				build = build * xAbs + 440.413735824752;
				cumnorm = cumnorm / build;
			} else {
				double build = xAbs + 0.65;
				build = xAbs + 4d / build;
				build = xAbs + 3d / build;
				build = xAbs + 2d / build;
				build = xAbs + 1d / build;
				cumnorm = exp / build / 2.506628274631;
			}
		}
		if (x > 0) {
			cumnorm = 1 - cumnorm;
		}
		return cumnorm;
	}


	/**
	 * This method returns the cumulative probability or the complementary probability of a standard normal 
	 * distribution for quantile x. The algorithm was translated from West's code.
	 * @param x the quantile
	 * @param complementary a boolean true to obtain the complementary probability or false to get the cumulative probability
	 * @return the probability (a double)
	 * @see <a href=http://www.wilmott.com/pdfs/090721_west.pdf> West, G. Better approximations to cumulative normal functions.
	 * WILMOTT magazine. 70-76. </a> 
	 */
	public static double getCumulativeProbability(double x, boolean complementary) {
		if (complementary) {
			return 1d - getCumulativeProbability(x);
		} else {
			return getCumulativeProbability(x);
		}
	}

	private static final double[] quantArrayA = new double[]{-3.969683028665376E+01, 2.209460984245205E+02, -2.759285104469687E+02, 1.383577518672690E+02, -3.066479806614716E+01, 2.506628277459239};
	private static final double[] quantArrayB = new double[]{-5.447609879822406E+01, 1.615858368580409E+02, -1.556989798598866E+02, 6.680131188771972E+01, -1.328068155288572E+01};
	private static final double[] quantArrayC = new double[]{-7.784894002430293E-03, -3.223964580411365E-01, -2.400758277161838, -2.549732539343734, 4.374664141464968, 2.938163982698783};
	private static final double[] quantArrayD = new double[]{7.784695709041462E-03, 3.224671290700398E-01, 2.445134137142996, 3.754408661907416};

	/**
	 * This method returns the quantiles of the distribution.
	 * @param cdfValue the cumulative density
	 * @return a quantile
	 */
	public static double getQuantile(double cdfValue) {

		double p_low = 0.02425;
		double p_high = 1 - p_low;
		double x;
		double q;
		
		if (cdfValue > 0 && cdfValue < p_low) {
			q = Math.sqrt(-2d * Math.log(cdfValue));
			x = (((((quantArrayC[0] * q + quantArrayC[1]) * q + quantArrayC[2]) * q + quantArrayC[3]) * q + quantArrayC[4]) * q + quantArrayC[5]) /	
					((((quantArrayD[0] * q + quantArrayD[1]) * q + quantArrayD[2]) * q + quantArrayD[3]) * q + 1);
		} else if (cdfValue >= p_low && cdfValue <= p_high) {
			q = cdfValue - .5;
			double r = q*q;
			x = (((((quantArrayA[0] * r + quantArrayA[1]) * r + quantArrayA[2]) * r + quantArrayA[3]) * r + quantArrayA[4]) * r +quantArrayA[5]) * q /
					(((((quantArrayB[0] * r + quantArrayB[1]) * r + quantArrayB[2]) * r + quantArrayB[3]) * r + quantArrayB[4]) * r + 1);
		} else if (cdfValue > p_high && cdfValue < 1) {
			q = Math.sqrt(-2d * Math.log(1-cdfValue));
			x = -(((((quantArrayC[0] * q + quantArrayC[1]) * q + quantArrayC[2]) * q + quantArrayC[3]) * q + quantArrayC[4]) * q + quantArrayC[5]) /	
					((((quantArrayD[0] * q + quantArrayD[1]) * q + quantArrayD[2]) * q + quantArrayD[3]) * q + 1);
		} else {
			throw new InvalidParameterException("The fValue parameter must be larger than 0 and smaller than 1!");
		}
		
		return increasePrecision(x, cdfValue);
	}

	
	private static double increasePrecision(double x, double cdfValue) {
		return x;
//		double e = 0.5 * (1 - erf(-x/Math.sqrt(2d))) - cdfValue;
//		double u = e * Math.sqrt(2d * Math.PI) * Math.exp(x*x/2d);
//		return x - u / (1d + x * u *.5);
	}
	
	
//	
//	private static final double[] erfArrayA = new double[]{0.254829592, -0.284496736, 1.421413741, -1.453152027, 1.061405429};
//
//	/**
//	 * Implementation of the formula 7.1.26 in Abramowitz and Stegun 1968. 
//	 * @param x
//	 * @return a double
//	 */
//	private static double erf(double x) {
//	    // constants
//	    double p  =  0.3275911;
//
//	    int sign = 1;
//	    if (x < 0) {
//	        sign = -1;
//	    }
//	    x = Math.abs(x);
//
//	    double t = 1d / (1d + p * x);
//	    double y = 1d - (((((erfArrayA[4] * t + erfArrayA[3]) * t) + erfArrayA[2]) * t + erfArrayA[1]) * t + erfArrayA[0]) * t *Math.exp(-x*x);
//
//	    return sign*y;
//	}

//	public static void main(String[] arg) {
//		for (int i = 1; i < 10; i++) {
//			double quantile1 = getQuantile(i * .1,true);
//			double quantile2 = getQuantile(i * .1,false);
//			double fValue1 = getCumulativeProbability(quantile1);
//			double fValue2 = getCumulativeProbability(quantile2);
//			System.out.println("fValue1 " + fValue1 + "    fValue2 " + fValue2);
//		}
//	}
	
	
//	void testErf()
//	{
//	    // Select a few input values
//	    double x[] = 
//	    {
//	        -3, 
//	        -1, 
//	        0.0, 
//	        0.5, 
//	        2.1 
//	    };
//
//	    // Output computed by Mathematica
//	    // y = Erf[x]
//	    double y[] = 
//	    { 
//	        -0.999977909503, 
//	        -0.842700792950, 
//	        0.0, 
//	        0.520499877813, 
//	        0.997020533344 
//	    };
//
//	    int numTests = sizeof(x)/sizeof(double);
//
//	    double maxError = 0.0;
//	    for (int i = 0; i < numTests; ++i)     {         double error = fabs(y[i] - erf(x[i]));         if (error > maxError)
//	            maxError = error;
//	    }
//
//	    std::cout << "Maximum error: " << maxError << "\n";
//	}  

	
}
