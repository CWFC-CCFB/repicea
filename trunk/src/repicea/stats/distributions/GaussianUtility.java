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
		return GaussianUtility.getCumulativeProbability(x, false);
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
	
	
	
}
