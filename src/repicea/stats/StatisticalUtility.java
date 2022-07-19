/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge-Epicea
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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import repicea.math.MathUtility;
import repicea.math.Matrix;

/**
 * This class contains static methods that are useful for statistical regressions.
 * @author Mathieu Fortin - August 2012, December 2017, April 2022
 */
public final class StatisticalUtility {

	private static REpiceaRandom random;
	
	public static enum TypeMatrixR {
		LINEAR(2), 
		LINEAR_LOG(2), 
		COMPOUND_SYMMETRY(2), 
		POWER(2), 
		ARMA(3),
		EXPONENTIAL(2);
		
		public final int nbParameters;
		
		TypeMatrixR(int nbParameters) {
			this.nbParameters = nbParameters;
		}
		
	}
	
	/**
	 * This method is a shortcut for inverting an AR1 correlation matrix.
	 * @param size the size of the matrix
	 * @param rho the correlation between two successive observations
	 * @return a Matrix
	 */
	public static Matrix getInverseCorrelationAR1Matrix(int size, double rho) {
		if (size < 1) {
			throw new InvalidParameterException("The size parameter must be equal to or greater than 1!");
		}
		if (rho <= 0 || rho >= 1) {
			throw new InvalidParameterException("The rho parameter must be greater than 0 and smaller than 1!");
		}
		double rho2 = rho * rho;
		Matrix mat = new Matrix(size, size); 
		for (int i = 0; i < mat.m_iRows; i++) {
			for (int j = i; j < mat.m_iCols; j++) {
				if (j == i) {
					if (i == 0 || i == mat.m_iRows - 1) {
						mat.setValueAt(i, j, 1d / (1d - rho2));
					} else {
						mat.setValueAt(i, j, (1d + rho2) / (1d - rho2));
					}
				} else if (j == i + 1) {
					mat.setValueAt(i, j, -rho/(1d - rho2));
					mat.setValueAt(j, i, -rho/(1d - rho2));
				}
			}
		}
		return mat;
	}
	
	/**
	 * Construct a within-subject correlation matrix using a variance parameter, a correlation parameter and a column vector of coordinates. <br>
	 * <br>
	 * @deprecated
     * This method is no longer acceptable.
     * <p> Use {@link constructRMatrix(covParms, type, coordinates)} instead.
	 * @param coordinates a column vector of coordinates from which the distances are calculated
	 * @param varianceParameter the variance parameter
	 * @param covarianceParameter the covariance parameter
	 * @param type the type of correlation
	 * @return the resulting matrix
	 */
	@Deprecated
	protected static Matrix constructRMatrix(Matrix coordinates, double varianceParameter, double covarianceParameter, TypeMatrixR type) {
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
						throw new UnsupportedOperationException("Matrix.ConstructRMatrix() : This type of correlation structure: " + type + " is not supported in this function");
					}
				}
			}
			return matrixR;
		}
	}
	
	/**
	 * Compute the Euclidean distance between two points. <br>
	 * <br>
	 * This method assumes that the checks have been performed on the coordinates argument. Basically,
	 * these matrices should be column vectors of the same size. Each one of them represents a dimensions.
	 * 
	 * @param i the index of the first point
	 * @param j the index of the second point
	 * @param coordinates A series of column matrices that stand for the coordinates. 
	 * @return
	 */
	protected final static double getEuclideanDistance(int i, int j, Matrix... coordinates) {
		double squareDiffSum = 0d;
		for (int k = 0; k < coordinates.length; k++) {
			Matrix c = coordinates[k];
			double diff = c.getValueAt(i, 0) - c.getValueAt(j, 0); 
			squareDiffSum += diff * diff;
		}
		return Math.sqrt(squareDiffSum);
	}
	
	
	/**
	 * Compute the R matrix <br>
	 * <br>
	 * Compute the R matrix of the type set by the type argument. 
	 * 
	 * @param covParms a List of double containing the parameter. The first is the variance parameter, the second is the 
	 * covariance parameter. In case of ARMA type, there is a third parameter which is the gamma parameter.
	 * @param type a TypeMatrixR enum
	 * @param coordinates a series of Matrices instance that stand for the coordinates. These should be column vectors of 
	 * the same size. Specifying two matrices implies that the Euclidean distance is based on two dimensions. Three matrices
	 * means three dimensions and so on.
	 * @return a Matrix instance
	 */
	public static Matrix constructRMatrix(List<Double> covParms, TypeMatrixR type, Matrix... coordinates) {
		if (covParms == null || covParms.size() < type.nbParameters) {
			throw new InvalidParameterException("The covParms list should contain this number of parameters: " + type.nbParameters + " when using type " + type.name());
		}
		if (coordinates == null || coordinates.length == 0) {
			throw new InvalidParameterException("The coordinates argument should contain at least one matrix.");
		}
		int nrow = -1;
		// check if the coordinates argument complies
		for (int i = 0; i < coordinates.length; i++) {
			if (!coordinates[i].isColumnVector()) {
				throw new InvalidParameterException("The coordinates should contain only column vectors!");
			} else {
				if (nrow == -1) {
					nrow = coordinates[i].m_iRows;
				} else {
					if (coordinates[i].m_iRows != nrow) {
						throw new InvalidParameterException("The coordinates should contain only column vectors of the same size!");
					}
				}	
			}
		}
		
		double varianceParameter = covParms.get(0);
		double covarianceParameter = covParms.get(1);
		double gamma = type == TypeMatrixR.ARMA ? covParms.get(2) : 0;
		
		double distance;
		Matrix matrixR = new Matrix(nrow,nrow);
		for (int i = 0; i < nrow; i++) {
			for (int j = i; j < nrow; j++) {
				double corr = 0d;
				switch(type) {
				case LINEAR:					// linear case
					distance = getEuclideanDistance(i, j, coordinates);
					corr = 1 - covarianceParameter * distance;
					if (corr >= 0) {
						matrixR.setValueAt(i, j, varianceParameter * corr);
						matrixR.setValueAt(j, i, varianceParameter * corr);
					}
					break;
				case LINEAR_LOG:				// linear log case
					distance = getEuclideanDistance(i, j, coordinates);
					if (distance == 0) {
						corr = 1d;
					} else {
						corr = 1 - covarianceParameter*Math.log(distance);
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
					distance = getEuclideanDistance(i, j, coordinates);
					if (distance == 0) {
						corr = 1d;
						matrixR.setValueAt(i, j, varianceParameter * corr);
					} else {
						corr = Math.pow (covarianceParameter, distance);
						if (corr >= 0) {
							matrixR.setValueAt(i, j, varianceParameter * corr);
							matrixR.setValueAt(j, i, varianceParameter * corr);
						}
					}
					break;   
				case ARMA:		
					if (i == j) {
						matrixR.setValueAt(i, i, varianceParameter);
					} else {
						distance = Math.abs(i - j) - 1;
						double powCol = Math.pow(covarianceParameter, distance);
						matrixR.setValueAt(i, j, varianceParameter * gamma * powCol);
						matrixR.setValueAt(j, i, matrixR.getValueAt(i, j));						
					}
					break;
				case EXPONENTIAL:
					distance = getEuclideanDistance(i, j, coordinates);
					if (distance == 0) {
						corr = 1d;
						matrixR.setValueAt(i, j, varianceParameter * corr);
					} else {
						corr = Math.exp(-distance / covarianceParameter);
						if (corr >= 0) {
							matrixR.setValueAt(i, j, varianceParameter * corr);
							matrixR.setValueAt(j, i, varianceParameter * corr);
						}
					}
					break;   
				default:
					throw new UnsupportedOperationException("Matrix.ConstructRMatrix() : This type of correlation structure is not supported in this function");
				}
			}
		}
		return matrixR;
	}

	
	
	
	
	
	/**
	 * Construct a within-subject correlation matrix using a rho parameter, a gamma parameter, a residual parameter and a column vector of coordinates. <br>
	 * @deprecated
     * This method is no longer acceptable.
     * <p> Use {@link constructRMatrix(covParms, type, coordinates)} instead.
	 * <br>
	 * @param coordinates a column vector of coordinates from which the distances are calculated
	 * @param rho the rho parameter
	 * @param gamma the gamma parameter
	 * @param residual the residual parameter
	 * @param type the type of correlation
	 * @return the resulting matrix
	 */
	@Deprecated
	protected static Matrix constructRMatrix(Matrix coordinates, double rho, double gamma, double residual, TypeMatrixR type) {
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

	/**
	 * Return the quantile of distribution estimated from a sample. <br>
	 * <br>
	 * The quantile is calculated following the Definition 8 found in <a href=https://doi.org/10.1080/00031305.1996.10473566>
	 * Hyndman, R. J. and Fan, Y. 1996. Sample quantiles in statistical packages. The American Statistician
	 * 50(4): 361-365. </a>
	 * 
	 * @param sample the sample of the distribution
	 * @param p the probability of the quantile (between 0 and 1)
	 * @return the estimated quantile of the distribution
	 */
	public static double getQuantileFromSample(List<Double> sample, double p) {
		if (p < 0d || p > 1d)
			throw new InvalidParameterException("The p argument must range from 0 to 1!");
		if (sample == null || sample.isEmpty()) {
			throw new InvalidParameterException("The sample argument should be a non empty list of doubles!");
		}
		List<Double> copyList = new ArrayList<Double>();
		copyList.addAll(sample);
		Collections.sort(copyList);
		double N = copyList.size();
		double h = (N + 1d/3) * p + 1d/3;
		int h_floor = (int) Math.floor(h);
		int h_ceiling = (int) Math.ceil(h);
		double x_floor = copyList.get(h_floor - 1);
		double q = x_floor + (h - h_floor) * (copyList.get(h_ceiling - 1) - x_floor);
		return q;
	}
	
	
	
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
