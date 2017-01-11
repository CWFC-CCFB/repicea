/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge-Epicea
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
import java.util.Random;

import repicea.math.Matrix;

public class ChiSquaredUtility {

	private static final Random RANDOM = new Random();
	
	/**
	 * This method returns a Chi squared random value.
	 * @param df the degrees of freedom
	 * @return a double
	 */
	protected static double randomValue(int df) {
		if (df <= 0) {
			throw new InvalidParameterException("The number of degrees of freedom should be larger than 0");
		}
		double sumSquared = 0;
		for (int i = 0; i < df; i++) {
			double gaussian = RANDOM.nextGaussian();
			sumSquared += gaussian * gaussian;
		}
		return sumSquared;
	}
	
	/**
	 * This method returns the matrix A in the Bartlett decomposition.
	 * @param degreesOfFreedom
	 * @param dim the dimensions of the matrix
	 * @return a Matrix
	 */
	protected static Matrix getBartlettDecompositionMatrix(int degreesOfFreedom, int dim) {
		Matrix aMat = new Matrix(dim, dim);
		for (int i = 0; i < aMat.m_iRows; i++) {
			for (int j = 0; j <= i; j++) {
				if (i == j) {
					aMat.m_afData[i][j] = Math.sqrt(randomValue(degreesOfFreedom - i));	
				} else {
					aMat.m_afData[i][j] = RANDOM.nextGaussian();
				}
			}
		}
		return aMat;
	}
	
	
}
