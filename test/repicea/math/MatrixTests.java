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

import org.junit.Assert;

import org.junit.Test;

import repicea.stats.StatisticalUtility;
import repicea.stats.StatisticalUtility.TypeMatrixR;

/**
 * This test class performs some tests on matrix calculation.
 * @author Mathieu Fortin - September 2013
 */
public class MatrixTests {

	/**
	 * This test is performed on the calculation of the inverse of a blocked matrix.
	 */
	@Test
	public void BlockedInversedMatrixTest() {
		
		Matrix mat = new Matrix(9,9);
		mat.m_afData[0][0] = 5.49;
		mat.m_afData[0][4] = 1.85;
		mat.m_afData[1][1] = 3.90;
		mat.m_afData[2][2] = 2.90;
		mat.m_afData[2][3] = 1.02;
		mat.m_afData[2][5] = 0.70;
		mat.m_afData[2][6] = 0.76;
		mat.m_afData[2][7] = 0.77;
		mat.m_afData[2][8] = 0.80;
		mat.m_afData[3][3] = 3.20;
		mat.m_afData[3][5] = 0.89;
		mat.m_afData[3][6] = 0.87;
		mat.m_afData[3][7] = 0.89;
		mat.m_afData[3][8] = 0.93;
		mat.m_afData[4][4] = 4.55;
		mat.m_afData[5][5] = 2.70;
		mat.m_afData[5][6] = 0.66;
		mat.m_afData[5][7] = 0.67;
		mat.m_afData[5][8] = 0.70;
		mat.m_afData[6][6] = 2.69;
		mat.m_afData[6][7] = 0.66;
		mat.m_afData[6][8] = 0.69;
		mat.m_afData[7][7] = 2.70;
		mat.m_afData[7][8] = 0.70;
		mat.m_afData[8][8] = 2.76;
		
		for (int i = 0; i < mat.m_iRows; i++) {
			for (int j = i; j < mat.m_iCols; j++) {
				if (i != j) {
					mat.m_afData[j][i] = mat.m_afData[i][j];
				}
			}
		}
		
		Matrix invMat = mat.getInverseMatrix();
		Matrix ident = mat.multiply(invMat);
		Matrix diff = ident.subtract(Matrix.getIdentityMatrix(ident.m_iCols)).getAbsoluteValue();
		boolean equalToIdentity = !diff.anyElementLargerThan(1E-15);
		Assert.assertEquals(true, equalToIdentity);
	}
	
	/**
	 * This test is performed on the calculation of the inverse of a large symmetric matrix with many zero cells.
	 */
	@Test
	public void InversionWithZeroCellsTest() {
		
		Matrix coordinates = new Matrix(20,1,0,1);
		
		Matrix rMatrix = StatisticalUtility.constructRMatrix(coordinates, 2, 0.2, TypeMatrixR.LINEAR);
		Matrix invMat = rMatrix.getInverseMatrix();
	
		Matrix ident = rMatrix.multiply(invMat);

		Matrix diff = ident.subtract(Matrix.getIdentityMatrix(ident.m_iCols)).getAbsoluteValue();

		boolean equalToIdentity = !diff.anyElementLargerThan(1E-10);
		
		Assert.assertEquals(true, equalToIdentity);
	
	}
	
	
	
}
