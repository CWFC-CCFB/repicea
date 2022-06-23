/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2022 Mathieu Fortin for Rouge-Epicea
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
package repicea.math;

import java.security.InvalidParameterException;

/**
 * A class that implements the Exponential Integral function.
 * @author Mathieu Fortin - June 2022
 */
public class ExponentialIntegralFunction {

	private final static double EULER_CONSTANT = 0.57721566490153286060651209008240243104215933593992;
	private static int K_MAX = 20;
	
	/**
	 * Returns the result of the E1(z) function. 
	 * @param z a double greater than 0
	 * @return a double
	 */
	public static double getE1(double z) {
		if (z <= 0) {
			throw new InvalidParameterException("The z parameter must be greater than 0!");
		}
		double sum = 0;
		double powMinusZ = 1;
		long factK = 1;
		for (int k = 1; k < K_MAX; k++) {
			powMinusZ *= -z;
			factK *= k;
			sum += powMinusZ / (k * factK);
		}
		return -EULER_CONSTANT - Math.log(z) - sum;
	}

	/**
	 * Returns the result of the Ei(z) function. 
	 * @param z a double greater than 0
	 * @return a double
	 */
	public static double getEi(double z) {
		if (z <= 0) {
			throw new InvalidParameterException("The z parameter must be greater than 0!");
		}
		double sum = 0;
		double powMinusZ = 1;
		long factK = 1;
		for (int k = 1; k < K_MAX; k++) {
			powMinusZ *= z;
			factK *= k;
			sum += powMinusZ / (k * factK);
		}
		return EULER_CONSTANT + Math.log(z) + sum;
	}
//	public static double getE1b(double p) {
//		double sum = 0;
//		for (int k = 1; k < K_MAX; k++) {
//			double powMinusZ = Math.pow(-p, k);
//			sum += powMinusZ / (k * MathUtility.Factorial(k));
//		}
//		return -EULER_CONSTANT - Math.log(p) - sum;
//	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
//		for (int i = 0; i < 10000; i++)
		System.out.println("E1(z) = " + ExponentialIntegralFunction.getE1(2));
		System.out.println("Ei(z) = " + ExponentialIntegralFunction.getEi(2));
		System.out.println("Time = " + (System.currentTimeMillis() - startTime) + " ms.");
	}

}
