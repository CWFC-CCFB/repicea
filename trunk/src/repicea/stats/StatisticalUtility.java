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
package repicea.stats;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import repicea.math.MathUtility;
import repicea.math.Matrix;

/**
 * This class contains static methods that are useful for statistical regressions.
 * @author Mathieu Fortin - August 2012, December 2017
 */
public class StatisticalUtility {

	private static REpiceaRandom random;
	
	public static enum TypeMatrixR {LINEAR, LINEAR_LOG, COMPOUND_SYMMETRY, POWER, ARMA}
	
	
	/**
	 * Construct a within-subject correlation matrix using a variance parameter, a correlation parameter and a column vector of coordinates.
	 * @param coordinates a column vector of coordinates from which the distances are calculated
	 * @param varianceParameter the variance parameter
	 * @param covarianceParameter the covariance parameter
	 * @param type the type of correlation
	 * @return the resulting matrix
	 */
	public static Matrix constructRMatrix(Matrix coordinates, double varianceParameter, double covarianceParameter, TypeMatrixR type) {
		if (!coordinates.isColumnVector()) {
			throw new UnsupportedOperationException("Matrix.constructRMatrix() : The coordinates matrix is not a column vector");
		} else {
			int nrow = coordinates.m_iRows;
			Matrix matrixR = new Matrix(nrow,nrow);
			for (int i = 0; i < nrow; i++) {
				for (int j = i; j < nrow; j++) {
					double corr = 0d;
					switch(type) {
					case LINEAR:					// linear case
						corr = 1 - covarianceParameter * Math.abs(coordinates.getValueAt(i, 0) - coordinates.getValueAt(j, 0));
						if (corr >= 0) {
							matrixR.setValueAt(i, j, varianceParameter * corr);
							matrixR.setValueAt(j, i, varianceParameter * corr);
						}
						break;
					case LINEAR_LOG:				// linear log case
						if (Math.abs(coordinates.getValueAt(i, 0) - coordinates.getValueAt(j, 0)) == 0) {
							corr = 1d;
						} else {
							corr = 1 - covarianceParameter*Math.log(Math.abs(coordinates.getValueAt(i, 0) - coordinates.getValueAt(j, 0)));
						}
						if (corr >= 0) {
							matrixR.setValueAt(i, j, varianceParameter * corr);
							matrixR.setValueAt(j, i, varianceParameter * corr);
						}
						break;
					case COMPOUND_SYMMETRY:
						if (i == j) {
							matrixR.setValueAt(i, j, varianceParameter + covarianceParameter);
						} else {
							matrixR.setValueAt(i, j, covarianceParameter);
							matrixR.setValueAt(j, i, covarianceParameter);
						}
						break;
					case POWER:                  // power case
                        if (Math.abs(coordinates.getValueAt(i, 0) - coordinates.getValueAt(j, 0)) == 0) {
                              corr = 1d;
                        } else {
                              corr = Math.pow (covarianceParameter, (Math.abs (coordinates.getValueAt(i, 0) - coordinates.getValueAt(j, 0))));
                        }
                        if (corr >= 0) {
                              matrixR.setValueAt(i, j, varianceParameter * corr);
                              matrixR.setValueAt(j, i, varianceParameter * corr);
                        }
                        break;   

					default:
						throw new UnsupportedOperationException("Matrix.ConstructRMatrix() : This type of correlation structure is not supported in this function");
					}
				}
			}
			return matrixR;
		}
	}
	
	/**
	 * Construct a within-subject correlation matrix using a rho parameter, a gamma parameter, a residual parameter and a column vector of coordinates.
	 * @param coordinates a column vector of coordinates from which the distances are calculated
	 * @param rho the rho parameter
	 * @param gamma the gamma parameter
	 * @param residual the residual parameter
	 * @param type the type of correlation
	 * @return the resulting matrix
	 */
	public static Matrix constructRMatrix(Matrix coordinates, double rho, double gamma, double residual, TypeMatrixR type) {
		if (!coordinates.isColumnVector()) {
			throw new UnsupportedOperationException("Matrix.constructRMatrix() : The coordinates matrix is not a column vector");
		} else {
			int nrow = coordinates.m_iRows;
			Matrix matrixR = new Matrix(nrow,nrow);
			switch(type) {
			case ARMA:		
				double corr = 0d;	
				for (int i = 0; i < nrow; i++) {
					for (int j = i+1; j < nrow; j++) {							
						corr =Math.abs(i - j)-1;						
						double powCol = java.lang.Math.pow(rho, corr);
						matrixR.setValueAt(i, j, residual * gamma * powCol);
						matrixR.setValueAt(j, i, matrixR.getValueAt(i, j));						

					}
					matrixR.setValueAt(i, i, residual);
				}
				break;
			default:
				throw new UnsupportedOperationException("Matrix.ConstructRMatrix() : This type of correlation structure is not supported in this function");
			}
			return matrixR;
		}
	}
	
    /**
     * Generate a random vector
     * @param nrow the number of elements to be generated
     * @param type the distribution type (a Distribution.Type enum variable)
     * @return a Matrix instance
     */
	public static Matrix drawRandomVector(int nrow, Distribution.Type type) {
		return StatisticalUtility.drawRandomVector(nrow, type, StatisticalUtility.getRandom());
	}

	
	/**
	 * Return a Random generator.
	 * @return a Random instance
	 */
	public static REpiceaRandom getRandom() {
		if (random == null) {
			random = new REpiceaRandom();
		}
		return random;
	}
	
    /**
     * This method generates a random vector
     * @param nrow the number of elements to be generated
     * @param type the distribution type (a Distribution enum variable)
     * @param random a Random instance
     * @return a Matrix instance
     */
	public static Matrix drawRandomVector(int nrow, Distribution.Type type, Random random) {
		try {
			boolean valid = true;
			Matrix matrix = new Matrix(nrow,1);
			for (int i=0; i<nrow; i++) {
				double number = 0.0f;
				switch (type) {
				case GAUSSIAN:		// Gaussian random number ~ N(0,1)
					number = random.nextGaussian();
					break;
				case UNIFORM:		// Uniform random number [0,1]
					number = random.nextDouble();
					break;
				default:
					i = nrow;
					System.out.println("Matrix.RandomVector() : The specified distribution is not supported in the function");
					valid = false;
					break;
				}
				if (valid) {
					matrix.setValueAt(i, 0, number);
				}
			}
			if (valid) {
				return matrix;
			} else {
				return null;
			}
		} catch (Exception e) {
			System.out.println("Matrix.RandomVector() : Error while computing the random vector");
			return null;		
		}
	}

	/**
	 * Perform a special addition in which only the elements different from 0 and 1 
	 * are involved. NOTE: this method is used with SAS output. 
	 * @param originalMatrix the matrix of parameters
	 * @param matrixToAdd the matrix of parameter deviates
	 * @return the new parameters in a new Matrix instance
	 */
	public static Matrix performSpecialAdd(Matrix originalMatrix, Matrix matrixToAdd) {
		Matrix oMat = originalMatrix.getDeepClone();
		List<Integer> oVector = new ArrayList<Integer>();
		oVector.clear();
		
		for (int i = 0; i < originalMatrix.m_iRows; i++) {
			if (oMat.getValueAt(i, 0) != 0d && oMat.getValueAt(i, 0) != 1d) { 
				oVector.add(i);
			}
		}

		if (oVector.size() != matrixToAdd.m_iRows) {
			throw new InvalidParameterException("The number of rows do not match!");
		} else {
			for (int j = 0; j < oVector.size(); j++) {
				double newValue = oMat.getValueAt(oVector.get(j), 0) + matrixToAdd.getValueAt(j, 0);
				oMat.setValueAt(oVector.get(j), 0, newValue);
			}
		}
		return oMat;
	}
	
	/**
	 * Combine two row vectors of dummy variables. Useful for regressions.
	 * @param mat1 the first row vector
	 * @param mat2 the second row vector
	 * @return the resulting matrix
	 */
	public static Matrix combineMatrices(Matrix mat1, Matrix mat2) {
		if (mat1.m_iRows == mat2.m_iRows) {
			int nbCols = mat1.m_iCols * mat2.m_iCols;
			Matrix oMat = new Matrix(mat1.m_iRows, nbCols);
			for (int i = 0; i < mat1.m_iRows; i++) {
				for (int j = 0; j < mat1.m_iCols; j++) {
					for (int j_prime = 0; j_prime < mat2.m_iCols; j_prime++) {
						oMat.setValueAt(i, j*mat2.m_iCols+j_prime, mat1.getValueAt(i, j) * mat2.getValueAt(i, j_prime));
					}
				}
			}
			return oMat;
		} else {
			throw new UnsupportedOperationException("The two matrices do not have the same number of rows!");
		}
	}

//	/**
//	 * This method returns a sample from a population. 
//	 * @param observations a list of observations that compose the population
//	 * @param sampleSize the sample size (n)
//	 * @param withReplacement a boolean to indicate whether the sample is with or without replacement
//	 * @return a List that contains the sample
//	 */
//	public static List<Object> getSampleFromPopulation(List<?> observations, int sampleSize, boolean withReplacement) {
//		if (sampleSize < 1) {
//			throw new InvalidParameterException("The sample size must be at least of 1.");
//		}
//		List<Integer> sampleIndex = new ArrayList<Integer>();
//		int index;
//		while (sampleIndex.size() < sampleSize) {
//			index = (int) Math.floor(getRandom().nextDouble() * observations.size());
//			if (!sampleIndex.contains(index) || withReplacement) {
//				sampleIndex.add(index);
//			}
//		}
//		ArrayList<Object> sample = new ArrayList<Object>();
//		for (Integer ind : sampleIndex) {
//			sample.add(observations.get(ind));
//		}
//		return sample;
//	}

	/**
	 * This method returns the number of combinations.
	 * @param n the number of units
	 * @param d the number of units drawn in each combination
	 * @return a long
	 */
	public static long getCombinations(int n, int d) {
		if (n < 1 || d < 1) {
			throw new InvalidParameterException("Parameters n and d must be equal to or greater than 1!");
		} else if (d > n) {
			throw new InvalidParameterException("Parameters d must be equal to or smaller than parameter n!");
		}
		if (n - d > d) {
			return MathUtility.FactorialRatio(n, n - d) / MathUtility.Factorial(d);
		} else {
			return MathUtility.FactorialRatio(n, d) / MathUtility.Factorial(n - d);
		}
	}

//	public static void main(String[] args) {
//		List<Integer> population = new ArrayList<Integer>();
//		for (int i = 1; i <= 10; i++) {
//			population.add(i);
//		}
//		List<Object> sample = getSampleFromPopulation(population, 10, true);
//		System.out.println(sample.toString());
//	}

}
