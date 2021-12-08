/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2019 Mathieu Fortin for Rouge-Epicea
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
 * This class implements simple static methods in mathematics.
 * @author Mathieu Fortin - February 2019
 */
public class MathUtility {


	/**
	 * This method returns the factorial of parameter i.
	 * @param i an integer
	 * @return the result as an integer
	 */
	public static long Factorial(int i) {
		if (i < 0) {
			throw new InvalidParameterException("Parameter i must be equal to or greater than 0!");
		} else  if (i==0) {
			return 1;
		} else {
			long result = 1;
			for (int j = 1; j <= i; j++) {
				result *= j;
			}
			return result;
		}
	}
	
	
	/**
	 * This method returns the ratio of two factorial factorial of parameter i.
	 * @param i an integer
	 * @return the result as an integer
	 */
	public static long FactorialRatio(int i, int j) {
		if (i < 0 || j < 0) {
			throw new InvalidParameterException("Parameters i and j must be equal to or greater than 0!");
		} else if (j > i) {
			throw new InvalidParameterException("Parameter j must be smaller than parameter i!");
		} else {
			if (j == 0) {
				j = 1;
			}
			long result = 1;
			for (int k = i; k > j; k--) {
				result *= k;
			}
			return result;
		}
	}

}
