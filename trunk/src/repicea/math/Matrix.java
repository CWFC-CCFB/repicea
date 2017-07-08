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
package repicea.math;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import repicea.util.DeepCloneable;


/**
 * This class implement most of the basic function in linear algebra
 * Authors: Jean-Francois Lavoie and Mathieu Fortin (June 2009)
 */
public final class Matrix implements Serializable, DeepCloneable {

	private static final long serialVersionUID = 20100804L;
		
	protected static int SizeBeforeSwitchingToLUDecompositionInDeterminantCalculation = 6;
	
	public static int NB_ROWS_BEYOND_WHICH_MATRIX_INVERSION_TAKES_TOO_MUCH_TIME = 600;
	
	
	/*
	 * Members of this class
	 */
	private static final double VERY_SMALL = 1E-06;
	
	private static final double EPSILON = 1E-12;
	
	public double[][] m_afData;
	public int m_iRows;
	public int m_iCols;
	
	/**
	 * Constructor 1. Creates a matrix from a two-dimension array.
	 */
	public Matrix(double data[][]) {
		this(data.length, data[0].length);
		for (int i = 0; i < m_iRows; i++)
			for (int j = 0; j < m_iCols; j++)
				m_afData[i][j] = data[i][j];
	}
	
	/**
	 * Constructor 2. Creates a column vector from an array of double
	 * @param data an array of double instances.
	 */
	public Matrix(double data[]) {
		this(data.length, 1);
		for (int i = 0; i < m_iRows; i++)
			m_afData[i][0] = data[i];
	}
	
	/**
	 * Constructor 3. Creates a column vector with all the values found in the List instance.
	 * @param list a List of Number-derived instances
	 */
	public Matrix(List<? extends Number> list) {
		this(list.size(), 1);
		Number number;
		for (int i = 0; i < m_iRows; i++) {
			number = list.get(i);
			m_afData[i][0] = number.doubleValue();
		}
	}

	
	/**
	 * Constructor 4. Creates a matrix with all elements set to 0.
	 * @param iRows number of rows
	 * @param iCols number of columns
	 */
	public Matrix(int iRows, int iCols) {
		m_afData = new double[iRows][iCols];
		m_iRows = iRows;
		m_iCols = iCols;
	}
	
	/**
	 * Constructor 5. Creates a matrix with the elements starting from a given number with a particular increment.
	 * @param iRows number of rows
	 * @param iCols number of columns
	 * @param from first element of the matrix
	 * @param iIncrement increment for the next elements
	 */
	public Matrix(int iRows, int iCols, double from, double iIncrement) {
		this(iRows,iCols);
		double value = from;
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				m_afData[i][j] = value;
				value += iIncrement;
			}
		}
	}

	/**
	 * This method add matrix m to the current matrix.
	 * @param m the matrix to be added
	 * @return the result in a new Matrix instance
	 */
	public Matrix add(Matrix m) {
		Matrix mat = new Matrix(m_iRows, m_iCols);
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				mat.m_afData[i][j] = m_afData[i][j] + m.m_afData[i][j];
			}
		}
		
		return mat;
	}

	/**
	 * This method tests whether if any element of the Matrix object is 
	 * different from parameter d. 
	 * @param d the value to be checked
	 * @return true if at least one element is different from d
	 */
	public boolean anyElementDifferentFrom(double d) {
		boolean bool = false;
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				if (Math.abs(this.m_afData[i][j] - d) > EPSILON)
					bool = true;
			}
		}
		return bool;
	}

	/**
	 * This method tests whether if any element of the Matrix object is 
	 * larger than parameter d. 
	 * @param d the value to be checked
	 * @return true if at least one element is larger than d
	 */
	public boolean anyElementLargerThan(double d) {
		for (int i = 0; i < this.m_iRows; i++) {
			for (int j = 0; j < this.m_iCols; j++) {
				if (this.m_afData[i][j] > d) {
					return true;
				}
			}
		}
		return false;
	}

	
	/**
	 * This method tests whether if any element of the Matrix object is 
	 * smaller than or equal to parameter d. 
	 * @param d the value to be checked
	 * @return true if at least one element is larger than d
	 */
	public boolean anyElementSmallerOrEqualTo(double d) {
		for (int i = 0; i < this.m_iRows; i++) {
			for (int j = 0; j < this.m_iCols; j++) {
				if (this.m_afData[i][j] <= d) {
					return true;
				}
			}
		}
		return false;
	}

	
	/**
	 * This method return a vector that contains the diagonal element of this.
	 * A check is implemented to make sure this is a square matrix.
	 * @return the resulting matrix
	 * @throws UnsupportedOperationException if the matrix is not square
	 */
	public Matrix diagonalVector() {
		if (!isSquare()) {
			throw new UnsupportedOperationException("Matrix.diagonalVector() : The input matrix is not square");
		} else  {
			Matrix oMat = new Matrix(m_iRows, 1);
			for (int i = 0; i < m_iRows; i++) {
				oMat.m_afData[i][0] = this.m_afData[i][i];
			}
			return oMat;
		}
	}

	/**
	 * This method returns true if the matrix contains at least one value NaN.
	 * @return a boolean
	 */
	public boolean doesContainAnyNaN() {
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				if (Double.isNaN(m_afData[i][j])) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This method compute the elementwise division of this / m
	 * @param m
	 * @return the resulting matrix
	 */
	public Matrix elementWiseDivide(Matrix m) {
		if (isTheSameDimension(m)) {
			Matrix oMat = new Matrix(this.m_iRows,this.m_iCols);
			for (int i = 0; i < this.m_iRows; i++) {
				for (int j = 0; j < this.m_iCols; j++) {
					oMat.m_afData[i][j] = this.m_afData[i][j] / m.m_afData[i][j];
				}
			}
			return oMat;
		} else {
			throw new UnsupportedOperationException("The matrix m does not have the same dimensions than the current matrix!");
		}
	}
	
	/**
	 * This method compute the elementwise product of this x m
	 * @param m the matrix that contains the elements to be multiplied with.
	 * @return a Matrix instance
	 */
	public Matrix elementWiseMultiply(Matrix m) {
		if (isTheSameDimension(m)) {
			Matrix oMat = new Matrix(this.m_iRows,this.m_iCols);
			for (int i = 0; i < this.m_iRows; i++) {
				for (int j = 0; j < this.m_iCols; j++) {
					oMat.m_afData[i][j] = this.m_afData[i][j] * m.m_afData[i][j];
				}
			}
			return oMat;
		} else {
			throw new UnsupportedOperationException("The matrix m does not have the same dimensions than the current matrix!");
		}
	}
	
	/**
	 * Compute the exponential of the elements of this matrix.
	 * @return the results in a matrix
	 */
	public Matrix expMatrix() {
		Matrix matrix = new Matrix(m_iRows, m_iCols);
		for (int i = 0; i < matrix.m_iRows; i++) {
			for (int j = 0; j < matrix.m_iCols; j++) {
				matrix.m_afData[i][j] = Math.exp(m_afData[i][j]);
			}
		}
		return matrix;
	}
	
	/**
	 * This method compute the lower triangle of the Cholesky decomposition.
	 * Checks are implemented to make sure that this is square and symmetric.
	 * @return the resulting matrix 
	 * @throws UnsupportedOperationException if the Cholesky factorisation cannot be completed
	 */
    public Matrix getLowerCholTriangle() {
    	if (!isSquare()) {		    	// Tests to check if M1 is square and symmetric 
    		throw new UnsupportedOperationException("Matrix.lowerChol() : The input matrix is not square");
    	} else if (!isSymmetric()) {
    		throw new UnsupportedOperationException("Matrix.lowerChol() : The input square matrix is not symmetric");
    	}

    	int m1Row = m_iRows;
    	Matrix matrix = new Matrix(m1Row,m1Row);
    	double dTmp;
    	for (int i = 0; i < m1Row; i++) {
    		for (int j = 0; j <= i; j++) {
    			if (j == i) {
    				dTmp = 0;
    				for (int k = 0; k <= i - 1; k++) {
    					dTmp += matrix.m_afData[i][k]*matrix.m_afData[i][k];
    				}
    				matrix.m_afData[i][j] = Math.sqrt(m_afData[i][j] - dTmp);
    			} else {
    				dTmp = 0;
    				for (int k = 0; k <= j - 1; k++) {
    					dTmp += matrix.m_afData[i][k]*matrix.m_afData[j][k];
    				}
    				matrix.m_afData[i][j] = 1d / matrix.m_afData[j][j] * (m_afData[i][j]-dTmp);
    			}
    			if (Double.isNaN(matrix.m_afData[i][j])) {
    				throw new UnsupportedOperationException("Matrix.lowerChol(): the lower triangle of the Cholesky decomposition cannot be calculated because NaN have been generated!");
    			}
    		}
    	}
    	return matrix;
    }

	/**
	 * This method returns a submatrix of this matrix. 
	 * @param startRow the index of the first row (included)
	 * @param endRow the index of the last row (included)
	 * @param startColumn the index of the first column (included)
	 * @param endColumn the index of the last column (included)
	 * @return the submatrix in a Matrix instance
	 */
	public Matrix getSubMatrix(int startRow, int endRow, int startColumn, int endColumn) {
		int iRows = endRow - startRow + 1;
		int iCols = endColumn - startColumn + 1;
		Matrix mat = new Matrix(iRows, iCols);
		for (int i = 0; i < iRows; i++) {
			for (int j = 0; j < iCols; j++) {
				mat.m_afData[i][j] = m_afData[startRow + i][startColumn + j];
			}
		}
		return mat;		
	}
	
	/**
	 * This method returns a sub matrix whose elements correspond to the indices listed in 
	 * the row index list and the column index list.
	 * @param rowIndex a List of integers (if null all the rows are selected)
	 * @param columnIndex a List of integers (if null all the columns are selected)
	 * @return a Matrix instance
	 */
	public Matrix getSubMatrix(List<Integer> rowIndex, List<Integer> columnIndex) {
		if (rowIndex != null && !rowIndex.isEmpty()) {
			Collections.sort(rowIndex);
		} else {
			rowIndex = new ArrayList<Integer>();
			for (int i = 0; i < m_iRows; i++) {
				rowIndex.add(i);
			}
		}
		
		if (columnIndex != null && !columnIndex.isEmpty()) {
			Collections.sort(columnIndex);
		} else {
			columnIndex = new ArrayList<Integer>();
			for (int j = 0; j < m_iCols; j++) {
				columnIndex.add(j);
			}
		}
		
		Matrix outputMatrix = new Matrix(rowIndex.size(), columnIndex.size());
		for (int i = 0; i < rowIndex.size(); i++) {
			for (int j = 0; j < columnIndex.size(); j++) {
				outputMatrix.m_afData[i][j] = m_afData[rowIndex.get(i)][columnIndex.get(j)];
			}
		}
	
		return outputMatrix;
	}

	/**
	 * This method checks if this is a column vector
	 * @return a boolean that is true if this is a column vector
	 */
	public boolean isColumnVector() {return m_iCols == 1;}
	
	/**
	 * This method checks whether this matrix is a diagonal matrix, i.e. with all its off-diagonal 
	 * elements being equal to zero.
	 * @return a boolean
	 */
	public boolean isDiagonalMatrix() {
		if (isSquare()) {
			for (int i = 0; i < m_iRows; i++) {
				for (int j = 0; j < m_iCols; j++) {
					if (i != j) {
						if (Math.abs(m_afData[i][j]) != 0d) {
							return false;
						}
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method checks if the matrix is empty, ie it has no column and no row.
	 * @return true if the matrix is empty or false otherwise
	 */
	public boolean isEmpty() {
		if (this.m_iCols == 0 || this.m_iRows == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * This method checks if this is a row vector
	 * @return a boolean that is true if this is a row vector
	 */
	public boolean isRowVector() {return m_iRows == 1;}
	
	/**
	 * This method checks if this is a square matrix
	 * @return true if the matrix is square or false otherwise
	 */
	public boolean isSquare() {return m_iRows == m_iCols;}
	
	/**
	 * This method tests whether the matrix is symmetric. 
	 * @return true if the matrix is symmetric or false otherwise
	 */
	public boolean isSymmetric() {
		boolean valid = true;
		if (!isSquare()) {
			return false;
		}
		
		outerLoop:
		for (int i = 0; i < m_iRows; i++) {
			for (int j = i + 1; j < m_iCols; j++) {
				if (Math.abs(m_afData[j][i]) < 1E-50) {		// equal to 0
					if (Math.abs(m_afData[i][j]) > 1E-50) {	// not equal to 0
						valid = false;
						break outerLoop;
					}
				} else {
					double ratio = m_afData[i][j] / m_afData[j][i];
					if (Math.abs(ratio - 1) > VERY_SMALL) {
						valid = false;
						break outerLoop;
					}
				}
			}
		}
		return valid;
	}

	/**
	 * This method checks whether or not this and m have the same dimensions
	 * @param m a Matrix instance
	 * @return boolean
	 */
	public boolean isTheSameDimension(Matrix m) {
		boolean output = false;
		if (m_iCols == m.m_iCols) {
			if (m_iRows == m.m_iRows) {
				output = true;
			}
		}
		return output;
	}

	/**
	 * Compute the logarithm of the elements of this matrix 
	 * @return the results in a Matrix instance
	 * @throws UnsupportedOperationException if one element of the matrix is smaller than or equal to 0
	 */
    public Matrix logMatrix() {
    	boolean valid = true;		// default is valid
    	Matrix matrix = new Matrix(m_iRows, m_iCols);
    	outerloop:
    		for (int i = 0; i < matrix.m_iRows; i++) {
    			for (int j = 0; j < matrix.m_iCols; j++) {
    				if (m_afData[i][j] <= 0d) {
    					valid = false;
    					break outerloop;
    				}
    				matrix.m_afData[i][j] = Math.log(m_afData[i][j]);
    			}
    		}
    	if (valid) {
    		return matrix;
    	} else {
    		throw new UnsupportedOperationException("Matrix.logMatrix() : At least one argument value for the log function is smaller or equal to 0");
    	}
    }

	/**
	 * This method creates a new matrix in which the current matrix represents the first diagonal block and matrix m represents the second
	 * diagonal block.
	 * @param m the matrix to be diagonally blocked
	 * @return the result in a new Matrix instance
	 */
	public Matrix matrixDiagBlock(Matrix m) {
		int m1Row = m_iRows;
		int m1Col = m_iCols;
		int m2Row = m.m_iRows;
		int m2Col = m.m_iCols;
		Matrix matrix = new Matrix(m1Row + m2Row, m1Col + m2Col);
		matrix.setSubMatrix(this, 0, 0);
		matrix.setSubMatrix(m, m1Row, m1Col);
		return matrix;
	}

	
	/**
	 * This method compute a diagonal matrix from a row or a column vector.
	 * @return the resulting matrix
	 * @throws UnsupportedOperationException if this matrix is not a vector
	 */
	public Matrix matrixDiagonal() {
		if (!isRowVector() && !isColumnVector()) {
			throw new UnsupportedOperationException("Matrix.matrixDiagonal() : The input matrix is not a vector");
		} else {
			int dim;
			if (isColumnVector()) {
				dim = m_iRows;
			} else {
				dim = m_iCols;
			}
			Matrix matrix = new Matrix(dim, dim); 
			for (int i = 0; i < dim; i++) {
				if (isColumnVector()) {
					matrix.m_afData[i][i] = m_afData[i][0];
				} else {
					matrix.m_afData[i][i] = m_afData[0][i];
				}
			}
			return matrix;
		}
	}

	/**
	 * This method creates a new matrix that is the stack of this and matrix m. 
	 * @param m the matrix to stack.
	 * @param stackOver true if the stack is vertically or false if horizontally
	 * @return the stacked matrix
	 */
	public Matrix matrixStack(Matrix m, boolean stackOver) {
		int m1Row = m_iRows;
		int m1Col = m_iCols;
		int m2Row = m.m_iRows;
		int m2Col = m.m_iCols;
		if (m1Col == m2Col || m1Row == m2Row) {
			if (stackOver) {
				Matrix matrix = new Matrix(m1Row + m2Row, m1Col);
				matrix.setSubMatrix(this, 0, 0);
				matrix.setSubMatrix(m, m1Row, 0);
				return matrix;
			} else {
				Matrix matrix = new Matrix(m1Row,m1Col+m2Col);
				matrix.setSubMatrix(this, 0, 0);
				matrix.setSubMatrix(m, 0, m1Col);
				return matrix;
			}
		} else {
			throw new UnsupportedOperationException("Matrix m cannot be stacked on the current matrix because their dimensions do not match");
		}
	}

	/**
	 * This method compute the matrix multiplication product of this x m
	 * @param m a Matrix type object 
	 * @return a matrix type object that contains the result of the matrix multiplication
	 */
	public Matrix multiply(Matrix m) {
		if (m_iCols != m.m_iRows) {
			throw new UnsupportedOperationException("The matrix m cannot multiply the current matrix for the number of rows is incompatible!");
		} else {
			Matrix mat = new Matrix(m_iRows, m.m_iCols);
			for (int i_this = 0; i_this < m_iRows; i_this++) {
				for (int j_m = 0; j_m < m.m_iCols; j_m++ ) {
					for (int j_this = 0; j_this < m_iCols; j_this++) {
						int i_m = j_this;
						mat.m_afData[i_this][j_m] += m_afData[i_this][j_this] * m.m_afData[i_m][j_m];
					}
				}
			}
			return mat;
		}
	}

	/**
	 * This method reset all the elements of this Matrix instance to 0.
	 */
	public void resetMatrix() {
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				m_afData[i][j] = 0.0;
			}
		}
	}
	
	/**
	 * This method adds the scalar d to all the elements of the current matrix.
	 * @param d the scalar to be added
	 * @return the result in a new Matrix instance
	 */
	public Matrix scalarAdd(double d) {
		Matrix mat = new Matrix(m_iRows, m_iCols);
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				mat.m_afData[i][j] = m_afData[i][j]+d;
			}
		}
		return mat;
	}
	
	/**
	 * This method multiplies the elements of the current matrix by the scalar d.
	 * @param d the multiplier
	 * @return the result in a new Matrix instance
	 */
	public Matrix scalarMultiply(double d) {
		Matrix mat = new Matrix(m_iRows, m_iCols);
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				mat.m_afData[i][j] = m_afData[i][j]*d;
			}
		}
		return mat;
	}

	/**
	 * This method replaces some elements of the matrix by those that are contained in an array of Double.
	 * @param array a two-dimension array of Double
	 * @param i the row index of the first element to be changed
	 * @param j the column index of the first element to be changed
	 */
	public void setSubMatrix(double[][] array, int i, int j) {
		for (int ii = 0; ii < array.length; ii++) {
			for (int jj = 0; jj < array[0].length; jj++) {
				m_afData[i + ii][j + jj] = array[ii][jj];
			}
		}
	}

	/**
	 * This method replaces some elements of the matrix by those that are contained in matrix m.
	 * @param m a Matrix instance 
	 * @param i the row index of the first element to be changed
	 * @param j the column index of the first element to be changed
	 */
	public void setSubMatrix(Matrix m, int i, int j) {
		setSubMatrix(m.m_afData, i, j);
	}


	
	
	/**
	 * This method makes a square symmetric matrix from a vector.
	 * Checks are implemented to make sure the vector has the appropriate
	 * number of elements.
	 * @return the resulting matrix
	 */
	public Matrix squareSym() {
		if (!isColumnVector()) {
			throw new UnsupportedOperationException("The current matrix is not a column vector!");
		} else {
			double numberElem = m_iRows;
			Double numberRow = (-1.0 + Math.sqrt(1.0+8*numberElem))*0.5;
			int nbRow = numberRow.intValue();
			if (Math.abs(numberRow.doubleValue() - nbRow) > Matrix.VERY_SMALL) {	// check if numberRow is an integer, if not it means the matrix is not square
				throw new UnsupportedOperationException("The number of elements contained in the imput column vector is not appropriate to transform the matrix into a square symmetric matrix!");
			} else {
				Matrix matrix = new Matrix(nbRow,nbRow);
				int pointer = 0;
				for (int i = 0; i < nbRow; i++) {
					matrix.setSubMatrix(getSubMatrix(pointer, pointer + i, 0, 0).m_afData, 0, i);
					matrix.setSubMatrix(getSubMatrix(pointer, pointer + i, 0, 0).transpose().m_afData, i, 0);
					pointer += i + 1; 
				}
				return matrix;
			}
		}
	}
	
	/**
	 * This method returns a vector of the values corresponding to a symmetric matrix.
	 * @return a nx1 Matrix 
	 */
	public Matrix symSquare() {
		if (!isSymmetric()) {
			throw new UnsupportedOperationException("The current matrix is not symmetric!");
		} else {
			int numberOfElements = (m_iCols + 1) * m_iCols / 2;
			Matrix outputMatrix = new Matrix(numberOfElements, 1);
			int pointer = 0;
			Matrix tmp;
			for (int i = 0; i < m_iCols; i++) {
				tmp = getSubMatrix(i, i, 0, i).transpose();		// transpose required to get a column vector
				outputMatrix.setSubMatrix(tmp, pointer, 0);
				pointer += tmp.getNumberOfElements();
			}
			return outputMatrix; 
		}
	}

	/**
	 * This method subtracts matrix m from the current matrix.
	 * @param m the matrix to be subtracted
	 * @return the result in a new Matrix instance
	 */
	public Matrix subtract(Matrix m) {
		Matrix mat = new Matrix(m_iRows, m_iCols);
		for (int i = 0; i < m_iRows; i++)
			for (int j = 0; j < m_iCols; j++)
				mat.m_afData[i][j] = m_afData[i][j] - m.m_afData[i][j];
		
		return mat;
	}
	
	/**
	 * This method computes the trace of the matrix, i.e. the sum of the diagonal elements.
	 * @return a double
	 */
	public double getTrace() {
		if (!isSquare()) {
			throw new UnsupportedOperationException("The trace operation requires the matrix to be square!");
		}
		double sum = 0;
		for (int i = 0; i < m_iRows; i++) {
			sum += this.m_afData[i][i];
		}
		return sum;
	}

	/**
	 * Creates a transposed matrix.
	 * @return the transposed matrix in a new Matrix instance
	 */
	public Matrix transpose() {
		Matrix matrix = new Matrix(m_iCols, m_iRows);
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				matrix.m_afData[j][i] = m_afData[i][j];
			}
		}
		return matrix;
	}
	

	
	
	
	
	
	

	
	

    
	
	/**
	 * This method computes the power of the seed by the elements of the matrix. For example, if the first element of this
	 * matrix is 2, the first element of the resulting matrix will be seed ^ 2.
	 * @param seed a double
	 * @return a Matrix instance
	 */
	public Matrix powMatrix(double seed) {
		Matrix matrix = new Matrix(m_iRows, m_iCols);
		for (int i = 0; i < matrix.m_iRows; i++) {
			for (int j = 0; j < matrix.m_iCols; j++) {
				matrix.m_afData[i][j] = Math.pow(seed, m_afData[i][j]);
			}
		}
		return matrix;
	}
	
	/**
	 * This method computes the elements of the matrix at a given power.
	 * @param power a double
	 * @return a Matrix instance
	 */
	public Matrix elementWisePower(double power) {
		Matrix matrix = new Matrix(m_iRows, m_iCols);
		for (int i = 0; i < matrix.m_iRows; i++) {
			for (int j = 0; j < matrix.m_iCols; j++) {
				matrix.m_afData[i][j] = Math.pow(m_afData[i][j], power);
			}
		}
		return matrix;
	}
	

	/**
	 * This method repeats this matrix a given number of times in each dimension.
	 * @param nrow the number of times to repeat in row-wise direction
	 * @param ncol the number of times to repeat in column-wise direction
	 * @return the resulting matrix
	 */
	public Matrix repeat(int nrow, int ncol) {
		Matrix resultingMatrix = new Matrix(m_iRows * nrow, m_iCols * ncol);
		for (int i = 0; i < nrow; i++) {
			for (int j = 0; j < ncol; j++) {
				resultingMatrix.setSubMatrix(this, i * m_iRows, j * m_iCols);
			}
		}
		return resultingMatrix;
	}
	
	/**
	 * This method makes it possible to remove some elements in a 
	 * particular matrix
	 * @param index is the index of the elements to be removed
	 * @return a row vector
	 */
	public Matrix removeElements(List<Integer> index) {
		Matrix oMat = new Matrix(1, m_iRows * m_iCols - index.size());
		int pointer = 0;
		for (int i=0; i < m_iRows; i++) {
			for (int j=0; j < m_iCols; j++) {
				if (!index.contains(i * m_iCols + j)) {
					oMat.m_afData[0][pointer] = m_afData[i][j];
					pointer++;
				}
			}
		}
		return oMat;
	}
	
	/**
	 * This method returns the elements defined by the List indices in a row vector.
	 * @param indices a List of indices
	 * @return a row vector
	 */
	public Matrix getElements(List<Integer> indices) {
		Matrix oMat = new Matrix(1, indices.size());
		int pointer = 0;
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				if (indices.contains(i * m_iCols + j)) {
					oMat.m_afData[0][pointer] = m_afData[i][j];
					pointer++;
				}
			}
		}
		return oMat;
	}
	
	/**
	 * This method replaces the elements of the matrix designated through the indices by the values
	 * in the row vector m.
	 * @param indices a List of Integer representing the indices
	 * @param m a Matrix instance
	 */
	public void setElements(List<Integer> indices, Matrix m) {
		if (!m.isColumnVector()) {
			throw new InvalidParameterException("Parameter m must be a row vector!");
		}
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				if (indices.contains(i * m_iCols + j)) {
					m_afData[i][j] = m.m_afData[indices.indexOf(i * m_iCols + j)][0];
				}
			}
		}
	}

	/**
	 * This method add the elements of the parameter matrix to those designated through the indices.
	 * @param indices a List of Integer representing the indices
	 * @param m a Matrix instance
	 */
	public void addElementsAt(List<Integer> indices, Matrix m) {
		if (!m.isColumnVector()) {
			throw new InvalidParameterException("Parameter m must be a row vector!");
		}
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				if (indices.contains(i * m_iCols + j)) {
					m_afData[i][j] += m.m_afData[indices.indexOf(i * m_iCols + j)][0];
				}
			}
		}
	}


	/**
	 * This method returns a List of integers, which represent the index of the elements
	 * that are equal to parameter d. The index is calculated as i * m_iCols + j.
	 * @param d the value that is checked for
	 * @return a List of integers
	 */
	public List<Integer> getLocationIndex(double d) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i=0; i < m_iRows; i++) {
			for (int j=0; j < m_iCols; j++) {
				if (Math.abs(m_afData[i][j] - d) < VERY_SMALL) {
					list.add(i * this.m_iCols + j);
				}
			}
		}
		return list;
	}

	/**
	 * This method returns the sum of all the elements in the Matrix instance.
	 * @return a double
	 */
	public double getSumOfElements() {
		double sum = 0d;
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				sum += this.m_afData[i][j];
			}
		}
		return sum;
	}

	/**
	 * This method returns the sum of the elements of a submatrix. The submatrix bounds are determined
	 * through the parameters.
	 * @param startRow the index of the starting row
	 * @param endRow the index of the ending row 
	 * @param startColumn the index of the starting column
	 * @param endColumn the index of the ending column
	 * @return the sum (double)
	 */
	public double getSumOfElements(int startRow, int endRow, int startColumn, int endColumn) {
		if (endRow >= this.m_iRows || endColumn >= this.m_iCols) {
			throw new InvalidParameterException("The specified end row or end column exceeds the capacity of the matrix!");
		} else if (startRow < 0 || startRow > endRow) {
			throw new InvalidParameterException("The specified start row is either negative or larger than the end row!");
		} else if (startColumn < 0 || startColumn > endColumn) {
			throw new InvalidParameterException("The specified start column is either negative or larger than the end column!");
		}
		double sum = 0d;
		for (int i = startRow; i <= endRow; i++) {
			for (int j = startColumn; j <= endColumn; j++) {
				sum += this.m_afData[i][j];
			}
		}
		return sum;
	}
	
	
	
	/**
	 * This method returns a Matrix object that contains the absolute values
	 * of the original matrix.
	 * @return a Matrix instance that contains the absolute values
	 */
	public Matrix getAbsoluteValue() {
		Matrix oMat = new Matrix(m_iRows, m_iCols);
		for (int i = 0; i < m_iRows; i++) 
			for (int j = 0; j < m_iCols; j++)
				oMat.m_afData[i][j] = Math.abs(this.m_afData[i][j]);
		return oMat;
	}

	/**
	 * This method returns the number of elements in a Matrix object.
	 * @return the number of elements (integer)
	 */
	public int getNumberOfElements() {
		return m_iRows * m_iCols;
	}
	
	/**
	 * This method calculates the Kronecker product of this by the m Matrix object.
	 * @param m a Matrix instance
	 * @return the resulting product (a matrix instance)
	 */
	public Matrix getKroneckerProduct(Matrix m) {
		Matrix result = new Matrix(m_iRows * m.m_iRows, m_iCols * m.m_iCols);
		for (int i1 = 0; i1 < m_iRows; i1++) {
			for (int j1 = 0; j1 < m_iCols; j1++) {
				for (int i2 = 0; i2 < m.m_iRows; i2++) {
					for (int j2 = 0; j2 < m.m_iCols; j2++) {
						result.m_afData[i1 * m.m_iRows + i2][j1 * m.m_iCols + j2] = m_afData[i1][j1] * m.m_afData[i2][j2];
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * This method returns a Matrix that corresponds to the Isserlis theorem given that matrix this is
	 * a variance-covariance matrix.
	 * @return a Matrix instance
	 */
	public Matrix getIsserlisMatrix() {
		if (!isSymmetric()) {
			throw new UnsupportedOperationException("Matrix.getIsserlisMatrix: this matrix is not symmetric!");
		} else {
			Matrix output = new Matrix(m_iRows * m_iRows, m_iCols * m_iCols);
			double covariance;
			int indexRow;
			int indexCol;
			for (int i = 0; i < m_iRows; i++) {
				for (int j = i; j < m_iCols; j++) {
					for (int iPrime = 0; iPrime < m_iRows; iPrime++) {
						for (int jPrime = 0; jPrime < m_iCols; jPrime++) {
							covariance = m_afData[i][j] * m_afData[iPrime][jPrime] 
									+ m_afData[i][iPrime] * m_afData[j][jPrime]
									+ m_afData[i][jPrime] * m_afData[j][iPrime];
							indexRow = i * m_iRows + iPrime;
							indexCol = j * m_iCols + jPrime;
							output.m_afData[indexRow][indexCol] = covariance;
							if (indexRow != indexCol) {
								output.m_afData[indexCol][indexRow] = covariance;
							}
						}
					}
				}
			}
			
			return output;
		}
	}

	/**
	 * This method checks if the matrix is positive definite. The check is based on the Cholesky factorization. If the factorization can
	 * be computed the method returns true.
	 * @return true if it is or false otherwise
	 */
	public boolean isPositiveDefinite() {
		try {
			getLowerCholTriangle();
			return true;
		} catch (UnsupportedOperationException e) {
			return false;
		}
	}

	@Override
	public Matrix getDeepClone() {
		Matrix oMat = new Matrix(m_iRows, m_iCols);
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				oMat.m_afData[i][j] = this.m_afData[i][j];
			}
		}
		return oMat;
	}
	
	/**
	 * Returns a representation of the m_afData array.
	 */
	@Override
	public String toString() {
		String outputString = "{";
		for (int i = 0; i < m_iRows; i ++) {
			outputString += Arrays.toString(m_afData[i]);
			if (i == m_iRows - 1) {
				outputString += "}";
			} else {
				outputString += ", ";
			}
			if (outputString.length() > 200) {
				outputString += "...";
				break;
			}
		}
		return outputString;
	}
	
	/**
	 * This method returns the LU decomposition of this Matrix instance. The diagonal in the lower triangle is 1.
	 * @return an array of two matrices, the first and the second being the lower and the upper triangle, respectively
	 * @throws UnsupportedOperationException if the matrix is not square
	 */
	public Matrix[] getLUDecomposition() {
		Matrix[] outputMatrices = new Matrix[2];
		if (!isSquare()) {
			throw new UnsupportedOperationException("Matrix.getLUDecomposition(): The matrix is not square!");
		} else {
			Matrix l = new Matrix(m_iRows, m_iCols);
			Matrix u = new Matrix(m_iRows, m_iCols);
		    for (int i = 0; i < m_iRows; i++) {
		        l.m_afData[i][i] = 1d;
		        for (int j = i; j < m_iRows; j++) {
		            double sum = 0;
		            for (int s = 0; s <= i - 1; s++) {
		                sum += l.m_afData[i][s] * u.m_afData[s][j];
		            }
		            u.m_afData[i][j] = m_afData[i][j] - sum;
		        }
		        for (int iii = i + 1; iii < m_iRows; iii++) {
		            double sum = 0;
		            for(int s = 0; s <= i - 1; s++) {
		                sum += l.m_afData[iii][s] * u.m_afData[s][i];
		            }
		            if (u.m_afData[i][i] == 0d) {
//		            	this.getLUDecomposition();
		            	throw new UnsupportedOperationException("The determinant cannot be calculated because of a division by 0!");
		            }
		            l.m_afData[iii][i] = (m_afData[iii][i] - sum) / u.m_afData[i][i];
		        }
		    }
		    outputMatrices[0] = l;
		    outputMatrices[1] = u;
		    return outputMatrices;
		}
	}

	
	private void swap(int i, int j, boolean columnWise) {
		if (columnWise) {
			if (i >= m_iCols || j >= m_iCols) {
				throw new UnsupportedOperationException("Columns cannot be swapped as their indices are out of bound!");
			} else {
				double d;
				for (int k = 0; k < m_iRows; k++) {
					d = m_afData[k][i];
					m_afData[k][i] = m_afData[k][j];
					m_afData[k][j] = d;
				}
			}
		} else {
			if (i >= m_iRows || j >= m_iRows) {
				throw new UnsupportedOperationException("Columns cannot be swapped as their indices are out of bound!");
			} else {
				double d;
				for (int k = 0; k < m_iCols; k++) {
					d = m_afData[i][k];
					m_afData[i][k] = m_afData[j][k];
					m_afData[j][k] = d;
				}
			}
			
		}
	}
	
	protected void swapAlongTheDiagonal(int i, int j) {
		if (!isSquare()) {
			throw new UnsupportedOperationException("The matrix is not square!");
		}
		if (i >= m_iCols || j >= m_iCols) {
			throw new UnsupportedOperationException("The index is out of bound!");
		} else {
			swap(i,j,true);
			swap(j,i,false);
		}
	}
	

	/**
	 * This method returns a list of indices that defines the blocks in the matrix.
	 * @return a List of List of Integers
	 */
	protected List<List<Integer>> getBlockConfiguration() {
		if (!isSquare()) {
			throw new UnsupportedOperationException("The matrix is not square!");
		}
		
		List<Integer> remainingIndex = new ArrayList<Integer>();
		for (int i = 0; i < m_iCols; i++) {
			remainingIndex.add(i);
		}

		List<List<Integer>> blocks = new ArrayList<List<Integer>>();
		
		if (!isSymmetric()) {
			blocks.add(remainingIndex);
			return blocks;
		} else {
			List<Integer> potentialBlock;
			while (!remainingIndex.isEmpty()) {
				int i = 0;
				potentialBlock = new ArrayList<Integer>();
				potentialBlock.add(remainingIndex.get(i));
				while (i < potentialBlock.size()) {
					int indexI = potentialBlock.get(i);
					for (int j = remainingIndex.indexOf(indexI) + 1; j < remainingIndex.size(); j++) {
						int indexJ = remainingIndex.get(j);
						if (m_afData[indexI][indexJ] != 0) {
							if (!potentialBlock.contains(indexJ)) {
								potentialBlock.add(indexJ);
							}
							for (int k = i + 1; k < j; k++) {
								int indexK = remainingIndex.get(k);
								if (m_afData[indexK][indexJ] != 0) {
									if (!potentialBlock.contains(indexK)) {
										potentialBlock.add(indexK);
									}
								}							
							}
						}
					}
					i++;
				}
				blocks.add(potentialBlock);
				remainingIndex.removeAll(potentialBlock);
			}
			return blocks;
		}
	}
	
	
	/**
	 * This method returns the minor of this matrix, ie the determinant of the Matrix that 
	 * contains all the elements of the original matrix except those in row i and column j.
	 * @param i the index of the row to be omitted
	 * @param j the index of the column to be omitted
	 * @return the minor
	 */
	public double getMinor(int i, int j) {
		if (i >= m_iRows) {
			throw new InvalidParameterException("The index i is not within the bound of this matrix!");
		} else if (j >= m_iCols) {
			throw new InvalidParameterException("The index j is not within the bound of this matrix!");
		} else if (getNumberOfElements() == 1) {
			throw new UnsupportedOperationException("The matrix only has one element!");
		} else {
			Matrix m = new Matrix(m_iRows - 1, m_iCols - 1);
			int index_i = 0;
			for (int ii = 0; ii < m_iRows; ii++) {		// iterate in the current matrix
				if (ii != i) {
					int index_j = 0;
					for (int jj = 0; jj < m_iCols; jj++) {
						if (jj != j) {
							m.m_afData[index_i][index_j] = m_afData[ii][jj];
							index_j++;
						}
					}
					index_i++;
				}
			}
			return m.getDeterminant();
		}
	}
	
	/**
	 * This method returns the cofactor of this matrix with respect to the element i,j.
	 * @param i the row index of the element
	 * @param j the column index of the element
	 * @return the cofactor matrix
	 */
	public double getCofactor(int i, int j) {
		double minor = getMinor(i,j);
		double multiplicator = 1d;
		if ((i + j) % 2 != 0) {
			multiplicator = -1d;
		}
		return minor * multiplicator;
	}
	
	
	/**
	 * This method returns the determinant of this matrix using Laplace's method for small matrices and LU decomposition
	 * for larger matrices.
	 * @return a double
	 * @throws UnsupportedOperationException if the matrix is not square
	 */ 
	public double getDeterminant() {
		double determinant = 0;
		if (!isSquare()) {
			throw new UnsupportedOperationException("The matrix is not square!");
		} else if (m_iRows == 1) {
			return m_afData[0][0];
		} else if (m_iRows == 2) {
			return m_afData[0][0] * m_afData[1][1] - m_afData[0][1] * m_afData[1][0];
		} else if (m_iRows <= SizeBeforeSwitchingToLUDecompositionInDeterminantCalculation) {
			for (int j = 0; j < m_iRows; j++) {
				if (m_afData[0][j] != 0) {
					determinant += m_afData[0][j] * getCofactor(0, j);
				}
			}
		} else {
			Matrix triangle = getLUDecomposition()[1];
			determinant = 1d;
			for (int i = 0; i < triangle.m_iRows; i++) {
				determinant *= triangle.m_afData[i][i];
			}
		}
		return determinant;
	}

	/**
	 * This method returns the adjugate matrix of this matrix.
	 * @return a Matrix instance
	 */
	public Matrix getAdjugateMatrix() {
		Matrix adjugate = new Matrix(m_iRows, m_iCols);
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				adjugate.m_afData[j][i] = getCofactor(i, j);		// i and j are inversed for adjugate to ensure the transposition
			}
		}
		return adjugate;
	}
	
	protected Matrix getInternalInverseMatrix() {
		if (m_iRows == 1) {
			Matrix output = new Matrix(1,1);
			output.m_afData[0][0] = 1d/m_afData[0][0];
			return output;
		} else if (m_iRows > SizeBeforeSwitchingToLUDecompositionInDeterminantCalculation) {
			int index = m_iCols / 2;
			Matrix output = new Matrix(m_iRows, m_iCols);
			Matrix a = getSubMatrix(0, index - 1, 0, index - 1);
			Matrix b = getSubMatrix(0, index - 1, index, m_iCols - 1);
			Matrix c = getSubMatrix(index, m_iRows - 1, 0, index - 1);
			Matrix d = getSubMatrix(index, m_iRows - 1, index, m_iCols - 1);
			Matrix invD = d.getInternalInverseMatrix();
			Matrix tmp = b.multiply(invD).multiply(c);
			Matrix invComplement = a.subtract(tmp).getInternalInverseMatrix();
			output.setSubMatrix(invComplement, 0, 0);
			output.setSubMatrix(invComplement.multiply(b).multiply(invD).scalarMultiply(-1d), 0, index);
			output.setSubMatrix(invD.multiply(c).multiply(invComplement).scalarMultiply(-1d), index, 0);
			output.setSubMatrix(invD.multiply(c).multiply(invComplement).multiply(b).multiply(invD).add(invD), index, index);
			return output;
		} else {
			double determinant = getDeterminant();
			if (determinant == 0) {
				throw new UnsupportedOperationException("The matrix cannot be inverted as its determinant is equal to 0!");
			} else {
				return getAdjugateMatrix().scalarMultiply(1d / determinant);
			}
		}

	}
	
	/**
	 * This method returns the inverse matrix of this matrix.
	 * @return the inverse matrix
	 */
	public Matrix getInverseMatrix() {
		if (isDiagonalMatrix()) {		// procedure for diagonal matrices
			Matrix mat = new Matrix(m_iRows, m_iCols);
			for (int i = 0; i < m_iRows; i++) {
				if (m_afData[i][i] == 0d) {
					throw new UnsupportedOperationException("The matrix is diagonal but some diagonal elements are null! It cannot be inverted!");
				}
				mat.m_afData[i][i] = 1 / m_afData[i][i];
			}
			return mat;
		} 
		List<List<Integer>> indices = getBlockConfiguration();
		if (indices.size() == 1) {
			return getInternalInverseMatrix();
		} else {
			Matrix inverseMatrix = new Matrix(m_iRows, m_iCols);
			for (List<Integer> blockIndex : indices) {
				Matrix invSubMatrix = getSubMatrix(blockIndex, blockIndex).getInternalInverseMatrix();
				for (int i = 0; i < blockIndex.size(); i++) {
					for (int j = 0; j < blockIndex.size(); j++) {
						inverseMatrix.m_afData[blockIndex.get(i)][blockIndex.get(j)] = invSubMatrix.m_afData[i][j];
					}
				}
			}
			return inverseMatrix;
		}
		
	}
	
	public static Matrix getIdentityMatrix(int i) {
		if (i <= 0) {
			throw new InvalidParameterException("The parameter i must be larger than 0!");
		}
		Matrix mat = new Matrix(i,i);
		for (int j = 0; j < i; j++) {
			mat.m_afData[j][j] = 1d;
		}
		return mat;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Matrix) {
			Matrix mat = (Matrix) obj;
			if (mat.m_iCols != m_iCols || mat.m_iRows != m_iRows) {
				return false;
			} else {
				return !subtract(mat).getAbsoluteValue().anyElementLargerThan(1E-20);
			}
		} else {
			return false;
		}
	}
}
