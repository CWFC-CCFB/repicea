/* 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Authors: M. Fortin and J.-F. Lavoie - Canadian Forest Service
 * Copyright (C) 2020-21 Her Majesty the Queen in right of Canada
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package repicea.simulation.metamodel;

import java.util.ArrayList;
import java.util.List;

import repicea.math.AbstractMathematicalFunction;
import repicea.math.Matrix;
import repicea.simulation.metamodel.MetaModel.InnerModel;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.integral.GaussHermiteQuadrature;

class DataBlockWrapper extends AbstractMathematicalFunction {
	
	final Matrix vecY;
	final Matrix varCov;
	final Matrix invVarCov;
	final Matrix timeSinceBeginning;
	final Matrix timeToOrigin;
	final Matrix ageYr;
	final Matrix dummy;
	final double constant;
	final double lnConstant;
	Matrix parameters;
	final InnerModel innerModel;
	final String blockId;
	final GaussHermiteQuadrature ghq;
	final List<Integer> ghqIndices;
	final List<Integer> indices;
	
	DataBlockWrapper(InnerModel innerModel, String blockId, List<Integer> indices, HierarchicalStatisticalDataStructure structure, Matrix overallVarCov) {
		this.blockId = blockId;
		this.indices = indices;
		this.innerModel = innerModel;
		Matrix varCovTmp = overallVarCov.getSubMatrix(indices, indices);
		Matrix matX = structure.getMatrixX().getSubMatrix(indices, null);
		this.vecY = structure.getVectorY().getSubMatrix(indices, null);
		boolean useCompleteMatrix;
//		try {
//			double determinant = varCovTmp.getDeterminant();
//			if (determinant > 0) {
////			if (!Double.isNaN(determinant)) {
//				useCompleteMatrix = true;
//			} else {
//				useCompleteMatrix = false;
//			}
//		} catch (Exception e) {
//			useCompleteMatrix = false;
//		}
//		if (useCompleteMatrix) {
//			this.varCov = varCovTmp;
//		} else {
			this.varCov = correctVarCov(varCovTmp).diagonalVector().matrixDiagonal();
//			System.out.println("Ill-conditioned variance-covariance matrix for block : " + this.blockId);
//			System.out.println("The covariances will be set to null!");
//		}
		this.invVarCov = this.varCov.getInverseMatrix();

		this.timeSinceBeginning = matX.getSubMatrix(0, matX.m_iRows - 1, 1, 1);
		this.timeToOrigin = matX.getSubMatrix(0, matX.m_iRows - 1, 0, 0).scalarMultiply(-1);
		this.ageYr = matX.getSubMatrix(0, matX.m_iRows - 1, 0, 0).add(timeSinceBeginning);
		
		dummy = matX.getSubMatrix(0, matX.m_iRows - 1, 2, 3);
		int k = this.vecY.m_iRows;
		double determinant = this.varCov.getDeterminant();
		this.constant = 1d / (Math.pow(2 * Math.PI, 0.5 * k) * Math.sqrt(determinant));
		this.lnConstant = -.5 * k * Math.log(2 * Math.PI) - Math.log(determinant) * .5;
		setParameterValue(0, 0d); // potential random effect
		this.ghq = new GaussHermiteQuadrature();
		this.ghqIndices = new ArrayList<Integer>();
		ghqIndices.add(0);
	}
	
	private Matrix correctVarCov(Matrix deepClone) {
		for (int i = 0; i < deepClone.m_iRows; i++) {
			if (deepClone.getValueAt(i, i) <= 0) {
				deepClone.setValueAt(i, i, .0001);
			}
		}
		return deepClone;
	}

	@Override
	public Double getValue() {
		double llk = getLogLikelihood();
		double prob = Math.exp(llk);
		return prob;
	}

	double getLogLikelihood() {
		Matrix pred = innerModel.generatePredictions(this, getParameterValue(0));
		Matrix residuals = vecY.subtract(pred);
		Matrix rVr = residuals.transpose().multiply(invVarCov).multiply(residuals);
		double rVrValue = rVr.getSumOfElements();
		if (rVrValue < 0) {
			throw new UnsupportedOperationException("The sum of squared errors is negative!");
		} else {
			double llk = - 0.5 * rVrValue + lnConstant; 
			return llk;
		}
	}
	
	double getMarginalLogLikelihood() {
		Matrix lowerCholeskyTriangle = innerModel.getVarianceRandomEffect(this).getLowerCholTriangle();
		double integratedLikelihood = ghq.getIntegralApproximation(this, ghqIndices, lowerCholeskyTriangle);
		return Math.log(integratedLikelihood);
	}
	
	@Override
	public Matrix getGradient() {return null;}

	@Override
	public Matrix getHessian() {return null;}

}
