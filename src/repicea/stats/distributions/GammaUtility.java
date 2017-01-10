/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge-Epicea
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
 * This class implements the Gamma function.
 * @author Mathieu Fortin - November 2015
 */
public class GammaUtility {

	private static final double[] COEF = new double[]{1.000000000000000174663, 5716.400188274341379136, -14815.30426768413909044, 
			14291.49277657478554025, -6348.160217641458813289, 1301.608286058321874105,
			-108.1767053514369634679, 2.605696505611755827729, -0.7423452510201416151527e-2,
			0.5384136432509564062961e-7, -0.4023533141268236372067e-8};		// higher precision with these coefficients

	/**
	 * This method returns the result of the Gamma function. The implementation is the Lanczos approximation. 
	 * @param z 
	 * @return a double
	 */
	public static double gamma(double z) {
		if (z <= 0d) {
			throw new InvalidParameterException("The gamma function does not support null or negative values!");
		}
	   	double result;
	    if (z < 0.5) {
	    	result = Math.PI / (Math.sin(Math.PI*z) * gamma(1-z));
	    } else {
	        z -= 1;
	        double x = COEF[0];
	        double pval;
	        for (int k = 1; k < COEF.length; k++) {
	        	pval = COEF[k];
	            x += pval/(z+k);		// i+1 = k 
	        }

	        double t = z + COEF.length - 1 - 0.5;
	        result = Math.sqrt(2d*Math.PI) * Math.pow(t, (z + .5)) * Math.exp(-t) * x;
	    }
	    return result;
	}
	
	/**
	 * This method returns the logarithm of the Gamma function.
	 * @param z
	 * @return a double
	 */
	public static double logGamma(double z) {
		return Math.log(gamma(z));
	}
	
	
//	public static void main(String[] args) {
//		double z = 3.5;
//		double actual = GammaFunction.gamma(z);
//		int u = 0;
//	}
	
}
