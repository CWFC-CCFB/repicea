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

import repicea.math.Matrix;
import repicea.stats.data.DataBlock;

class WithinSubjectComposition extends AbstractVComponent {
	
	private double sigma2;

	WithinSubjectComposition(String hierarchicalLevel) {
		super(hierarchicalLevel);
		setSigma2(1d);
	}
	

	protected void setSigma2(double value) {
		setParameter(0, value);
	}

	@Override
	public Matrix getValue() {
		return computeResult(sigma2);
	}

	private Matrix computeResult(double d) {
		Matrix output = null;
		for (DataBlock db : currentDataBlock.getBlocksOfThisLevel(hierarchicalLevel)) {
			if (output == null) {
				output = Matrix.getIdentityMatrix(db.getIndices().size());
			} else {
				output = output.matrixDiagBlock(Matrix.getIdentityMatrix(db.getIndices().size()));
			}
		}
		return output.scalarMultiply(d);
	}
	
	@Override
	public Matrix getGradient(Integer parameter) {
		return computeResult(1d);
	}
	
	@Override
	public Matrix getHessian(Integer parameter1, Integer parameter2) {
		int matrixSize = currentDataBlock.getIndices().size();
		return new Matrix(matrixSize, matrixSize);
	}

	@Override
	protected void setParameter(int index, double value) {
		sigma2 = value;
	}

	@Override
	protected int getNumberOfParameters() {
		return 1;
	}
	
	

}
