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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.DerivableMatrixFunction;
import repicea.math.EvaluableFunction;
import repicea.math.Matrix;
import repicea.stats.data.DataBlock;

/**
 * The MatrixVFunction class represents the V matrix computed as Z*G*Z' + R.
 * @author Mathieu Fortin - November 2012
 */
@SuppressWarnings("serial")
class MatrixVFunction implements EvaluableFunction<Matrix>, DerivableMatrixFunction<Integer>, Serializable {

	private static class ParameterIndex {
		private AbstractVComponent component;
		private int index;
		
		ParameterIndex(int index, AbstractVComponent component) {
			this.index = index;
			this.component = component;
		}
		
		private void updateParameter(double value) {
			component.setParameter(index, value);
		}
	}
	
	private final List<AbstractVComponent> vComponents;
	private final Map<Integer, ParameterIndex> parameterIndexRegistry;
	private final Matrix theta;
	private DataBlock currentDataBlock;
	
	protected MatrixVFunction(List<AbstractVComponent> vComponents) {
		this.vComponents = vComponents;
		parameterIndexRegistry = new HashMap<Integer, ParameterIndex>();
		setParameterIndexRegistry();
		theta = new Matrix(parameterIndexRegistry.size(), 1);
	}
	
	protected void setDataBlock(DataBlock dataBlock) {
		currentDataBlock = dataBlock;
	}
	
	private void setParameterIndexRegistry() {
		int nbParams = 0;
		for (AbstractVComponent comp : vComponents) {
			nbParams += comp.getNumberOfParameters();
		}
		for (int i = 0;  i < nbParams; i++) {
			int nbParamInComponent = 0;
			for (AbstractVComponent comp : vComponents) {
				if (i < nbParamInComponent + comp.getNumberOfParameters()) {
					int paramIndex = i - nbParamInComponent;
					parameterIndexRegistry.put(i, new ParameterIndex(paramIndex, comp));
					break;
				} else {
					nbParamInComponent += comp.getNumberOfParameters(); 
				}
			}
		}
	}

	@Override
	public Matrix getValue() {
		Matrix output = null;
		for (AbstractVComponent comp : vComponents) {
			comp.setDataBlock(currentDataBlock);
			if (output == null) {
				output = comp.getValue();
			} else {
				output = output.add(comp.getValue());
			}
		}
		return output;
	}

	@Override
	public Matrix getGradient(Integer parameter) {
		ParameterIndex pi = parameterIndexRegistry.get(parameter);
		AbstractVComponent avc = pi.component;
		avc.setDataBlock(currentDataBlock);
		int indexWithinComponent = pi.index;
		return avc.getGradient(indexWithinComponent);
	}

	@Override
	public Matrix getHessian(Integer parameter1, Integer parameter2) {
		ParameterIndex p1 = parameterIndexRegistry.get(parameter1);
		ParameterIndex p2 = parameterIndexRegistry.get(parameter2);
		if (p1.component.equals(p2.component)) {
			AbstractVComponent avc = p1.component;
			avc.setDataBlock(currentDataBlock);
			int i = p1.index;
			int j = p2.index;
			return avc.getHessian(i, j);
		} else {
			int matrixSize = currentDataBlock.getIndices().size();
			return new Matrix(matrixSize, matrixSize);
		}
	}
	
	/**
	 * This method sets the covariance parameters.
	 * @param theta a Matrix
	 */
	public void setParameters(Matrix theta) {
		for (int i = 0; i < theta.m_iRows; i++) {
			this.theta.m_afData[i][0] = theta.m_afData[i][0];
			ParameterIndex pi = parameterIndexRegistry.get(i);
			pi.updateParameter(theta.m_afData[i][0]);
		}
	}
	
	/**
	 * This method returns the number of covariance parameters involved in the computation of matrix V.
	 * @return an Integer
	 */
	public int getNumberOfParameters() {
		return theta.m_iRows;
	}

	protected Matrix getParameters() {return theta;}
	
}
