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

import java.io.Serializable;

import repicea.math.AbstractMathematicalFunction;
import repicea.math.Matrix;
import repicea.stats.model.lmm.InternalLogLikelihood.ParameterID;
import repicea.stats.model.lmm.InternalLogLikelihood.VariableID;

/**
 * The InternalLogLikelihoodProcessor class takes in charge all the calculations behind the log likelihood evaluations. 
 * @author Mathieu Fortin - November 2012
 */
@SuppressWarnings("serial")
class InternalLogLikelihood extends AbstractMathematicalFunction<ParameterID, MatrixVFunction, VariableID, Matrix> implements Serializable {

	protected static enum ParameterID {MatrixV}
	protected static enum VariableID {Residuals, MatrixX}
	
	private Matrix gradient;
	private Matrix hessian;

	@Override
	public Double getValue() {
		Matrix V = getParameterValue(ParameterID.MatrixV).getValue();
		double llk = Math.log(V.getDeterminant());
		
		Matrix r = getVariableValue(VariableID.Residuals);
		Matrix invV = V.getInverseMatrix();
		llk += r.transpose().multiply(invV).multiply(r).m_afData[0][0];

//		Matrix X = getVariableValue(VariableID.MatrixX);
//		llk += Math.log(X.transpose().multiply(invV).multiply(X).getDeterminant());
		
		return llk;
	}

	
	
	@Override
	public Matrix getGradient() {
		MatrixVFunction matrixVFunction = getParameterValue(ParameterID.MatrixV);
		int numberOfParameters = matrixVFunction.getNumberOfParameters();
		if (gradient == null) {
			gradient = new Matrix(numberOfParameters, 1);
		} else {
			gradient.resetMatrix();
		}
		
		Matrix invV = matrixVFunction.getValue().getInverseMatrix();
		Matrix r = getVariableValue(VariableID.Residuals);
		Matrix X = getVariableValue(VariableID.MatrixX);
		Matrix C = X.transpose().multiply(invV).multiply(X).getInverseMatrix().getLowerCholTriangle();
		Matrix Xstar = X.multiply(C);
		for (int i = 0; i < numberOfParameters; i++) {
			Matrix derV_i = matrixVFunction.getGradient(i);
			double gradientValue = invV.multiply(derV_i).getTrace();
			gradientValue += - r.transpose().multiply(invV).multiply(derV_i).multiply(invV).multiply(r).m_afData[0][0];
			gradientValue += - Xstar.transpose().multiply(invV).multiply(derV_i).multiply(invV).multiply(Xstar).getTrace();
			gradient.m_afData[i][0] = gradientValue;
		}
//		return gradient.scalarMultiply(-.5);
		return gradient;
	}

	@Override
	public Matrix getHessian() {
		MatrixVFunction matrixVFunction = getParameterValue(ParameterID.MatrixV);
		int numberOfParameters = matrixVFunction.getNumberOfParameters();
		if (hessian == null) {
			hessian = new Matrix(numberOfParameters, numberOfParameters);
		} else {
			hessian.resetMatrix();
		}
		
		Matrix invV = matrixVFunction.getValue().getInverseMatrix();
		Matrix r = getVariableValue(VariableID.Residuals);
		Matrix X = getVariableValue(VariableID.MatrixX);
		Matrix C = X.transpose().multiply(invV).multiply(X).getInverseMatrix().getLowerCholTriangle();
		Matrix Xstar = X.multiply(C);
		
		for (int i = 0; i < numberOfParameters; i++) {
			for (int j = 0; j < numberOfParameters; j++) {
				Matrix derV_i = matrixVFunction.getGradient(i);
				Matrix derV_j = matrixVFunction.getGradient(j);
				Matrix der2V_ij = matrixVFunction.getHessian(i, j);
				double hessianValue = - invV.multiply(derV_i).multiply(invV).multiply(derV_j).getTrace() 
						+ invV.multiply(der2V_ij).getTrace();
				hessianValue += 2 * r.transpose().multiply(invV).multiply(derV_i).multiply(invV).multiply(derV_j).multiply(invV).multiply(r).m_afData[0][0]
						- 2 * r.transpose().multiply(invV).multiply(derV_i).multiply(invV).multiply(Xstar).multiply(Xstar.transpose()).multiply(invV).multiply(derV_j).multiply(invV).multiply(r).m_afData[0][0]
						- r.transpose().multiply(invV).multiply(der2V_ij).multiply(invV).multiply(r).m_afData[0][0];
//				hessianValue += 2 * Xstar.transpose().multiply(invV).multiply(derV_i).multiply(invV).multiply(derV_j).multiply(invV).multiply(Xstar).getTrace() 
//						- Xstar.transpose().multiply(invV).multiply(derV_i).multiply(invV).multiply(Xstar).multiply(Xstar.transpose()).multiply(invV).multiply(derV_j).multiply(invV).multiply(Xstar).getTrace()
//						- Xstar.transpose().multiply(invV).multiply(der2V_ij).multiply(invV).multiply(Xstar).getTrace();
				hessian.m_afData[i][j] = hessianValue;
			}
		}
//		return hessian.scalarMultiply(-.5);
		return hessian;
	}

}
