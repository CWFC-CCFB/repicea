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
package repicea.stats.model.lmm;

import java.util.Map;

import repicea.math.Matrix;
import repicea.stats.data.DataBlock;

/**
 * The RandomEffectsMatrixComposition class handles all the random effects.
 * @author Mathieu Fortin - November 2012
 */
final class RandomEffectsMatrixComposition extends AbstractVComponent {

	private final Matrix matrixG;
	private final Matrix gGradient;
	private final Matrix gHessian;
	private final Matrix zMatrix;
	
	RandomEffectsMatrixComposition(String hierarchicalLevel, Map<String, Matrix> zMatrices, int matrixSize) {
		super(hierarchicalLevel);
		this.zMatrix = zMatrices.get(hierarchicalLevel);
		matrixG = new Matrix(matrixSize, matrixSize);
		gGradient = new Matrix(matrixSize, matrixSize);
		gHessian = new Matrix(matrixSize, matrixSize);
	}
	

	protected void setParameter(int index, double value) {
		setElementInMatrix(matrixG, index, value);
	}

	@Override
	public Matrix getValue() {
		return computeResult(matrixG);
	}
	
	@Override
	public Matrix getGradient(Integer parameter) {
		gGradient.resetMatrix();
		setElementInMatrix(gGradient, parameter, 1d);		// return a matrix with 1 at the location of the parameter
		return computeResult(gGradient);
	}

	private void setElementInMatrix(Matrix mat, int index, double value) {
		int columnIndex = 0;
		while (index + 1 > (columnIndex + 1) * (columnIndex + 2) * .5) {
			columnIndex++;
		}
		int rowIndex = index - columnIndex;
		mat.m_afData[rowIndex][columnIndex] = value;
	}
	
	private Matrix computeResult(Matrix mat) {
		Matrix output = null;
		for (DataBlock db :	currentDataBlock.getBlocksOfThisLevel(hierarchicalLevel)) {
			Matrix z_i = zMatrix.getSubMatrix(db.getIndices(), null);
			Matrix z_iGz_iT = z_i.multiply(mat).multiply(z_i.transpose());
			if (output == null) {
				output = z_iGz_iT;
			} else {
				output = output.matrixDiagBlock(z_iGz_iT);
			}
		}
		return output;
	}
	
	
	@Override
	public Matrix getHessian(Integer parameter1, Integer parameter2) {
		return computeResult(gHessian);
	}
	
	@Override
	protected int getNumberOfParameters() {
		int nbCols = matrixG.m_iCols;
		return (int) (nbCols * (nbCols + 1) * .5);
	}
	

}
