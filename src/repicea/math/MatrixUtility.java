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

import java.security.InvalidParameterException;

/**
 * This class provides some matrix operations. Some update the first argument matrix instead of
 * creating a new instance. Useful for optimizing some operations.
 * @author Mathieu Fortin - May 2012
 */
public class MatrixUtility {
	
	/**
	 * This method adds a matrix to a reference matrix. The result is returned in the reference matrix. NOTE:
	 * No new matrix instance is created.
	 * @param a a Matrix instance (the reference)
	 * @param b a Matrix instance
	 */
	public static void add(Matrix a, Matrix b) {
		if (a.m_iCols != b.m_iCols || a.m_iRows != b.m_iRows) {
			throw new InvalidParameterException("Matrices a and b are not the same size!");
		}
		
		for (int i = 0 ; i < a.m_iRows; i++) {
			for (int j = 0; j < a.m_iCols; j++) {
				a.m_afData[i][j] = a.m_afData[i][j] + b.m_afData[i][j];
			}
		}
	}
	
	/**
	 * This method subtracts a matrix from a reference matrix. The result is returned in the reference matrix. NOTE:
	 * No new matrix instance is created.
	 * @param a a Matrix instance (the reference)
	 * @param b a Matrix instance
	 */
	public static void subtract(Matrix a, Matrix b) {
		if (a.m_iCols != b.m_iCols || a.m_iRows != b.m_iRows) {
			throw new InvalidParameterException("Matrices a and b are not the same size!");
		}
		
		for (int i = 0 ; i < a.m_iRows; i++) {
			for (int j = 0; j < a.m_iCols; j++) {
				a.m_afData[i][j] = a.m_afData[i][j] - b.m_afData[i][j];
			}
		}
		
	}
	
	/**
	 * This method returns the element wise product of two matrices. The result is returned in the first matrix. NOTE:
	 * No new matrix instance is created.
	 * @param a a Matrix instance (the reference)
	 * @param b a Matrix instance
	 */
	public static void elementWiseMultiply(Matrix a, Matrix b) {
		if (a.m_iCols != b.m_iCols || a.m_iRows != b.m_iRows) {
			throw new InvalidParameterException("Matrices a and b are not the same size!");
		}
		
		for (int i = 0 ; i < a.m_iRows; i++) {
			for (int j = 0; j < a.m_iCols; j++) {
				a.m_afData[i][j] = a.m_afData[i][j] * b.m_afData[i][j];
			}
		}
		
	}

	/**
	 * This method multiplies all the element of a matrix by a double. The result is returned in the same matrix. NOTE:
	 * No new matrix instance is created.
	 * @param a a Matrix instance 
	 * @param b a double instance
	 */
	public static void scalarMultiply(Matrix a, double b) {
		for (int i = 0 ; i < a.m_iRows; i++) {
			for (int j = 0; j < a.m_iCols; j++) {
				a.m_afData[i][j] = a.m_afData[i][j] * b;
			}
		}
	}
	
    /**
     * This method returns an identity matrix of size i.
     * @param i the dimension of the matrix
     * @return a Matrix instance
     */
    public static Matrix getIdentityMatrix(int i) {
    	return new Matrix(i,1,1,0).matrixDiagonal();
    }


}
