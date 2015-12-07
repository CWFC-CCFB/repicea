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
package repicea.stats;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import repicea.math.AbstractMathematicalFunction;
import repicea.math.Matrix;

/**
 * This private abstract class defines a statistical expression that is to be used in a statistical model. The expression 
 * can be either linear or nonlinear.
 * @author Mathieu Fortin - June 2011
 */
@SuppressWarnings("serial")
public abstract class AbstractStatisticalExpression extends AbstractMathematicalFunction implements Serializable {

	protected Map<Integer, ParameterBound> parameterBounds;
	
	protected AbstractStatisticalExpression() {
		super();
	}
	
//	// This override avoid using the FastArray that manages the parameter names.
//	@Override
//	public void setParameterValue(Integer parameterIndex, Double parameterValue) {
//		if (parameterBounds != null && parameterBounds.containsKey(parameterIndex)) {
//			ParameterBound bound = parameterBounds.get(parameterIndex);
//			parameterValue = bound.validateParameter(parameterValue);
//		}
//		if (parameterIndex < getNumberOfParameters()) {
//			parameterValues.set(parameterIndex, parameterValue);
//		} else if (parameterIndex == getNumberOfParameters()) {
//			parameterValues.add(parameterValue);
//		} else {
//			throw new InvalidParameterException("The parameter index is not sequential!");
//		}
//	} 
//
//	// This override avoid using the FastArray that manages the parameter names.
//	@Override
//	public Double getParameterValue(Integer parameterIndex) {
//		return parameterValues.get(parameterIndex);
//	}
	
//	// This override avoid using the FastArray that manages the variable names.
//	@Override
//	public void setVariableValue(Integer variableIndex, Double variableValue) {
//		if (variableIndex < getNumberOfVariables()) {
//			variableValues.set(variableIndex, variableValue);
//		} else if (variableIndex == getNumberOfVariables()) {
//			variableValues.add(variableValue);
//		} else {
//			throw new InvalidParameterException("The variable index is not sequential!");
//		}
//	}

//	// This override avoid using the FastArray that manages the variable names.
//	@Override
//	public Double getVariableValue(Integer variableIndex) {
//		return variableValues.get(variableIndex);
//	}
	
//	/**
//	 * This method sets the vector of explanatory variables. The method essentially
//	 * relies on the setVariableValue() of the AbstractMathematicalFunction class.
//	 * @param x a Matrix instance 
//	 * @throws IllegalArgumentException if the parameter x is not a row vector
//	 */
//	public void setX(Matrix x) {
//		if (!x.isRowVector()) {
//			throw new IllegalArgumentException("The vector is not a row vector!");
//		} else {
//			variableNames.clear();
//			variableValues.clear();
//			for (int j = 0; j < x.m_iCols; j++) {
//				setVariableValue(j, x.m_afData[0][j]);
//			}
//		}
//	}
//	
//	/**
//	 * This method sets the vector of parameters. The method essentially relies on
//	 * the setParameterValue() of the AbstractMathematicalFunction class.
//	 * @param beta a Matrix instance
//	 * @throws IllegalArgumentException if beta is not a column vector
//	 */
//	public void setBeta(Matrix beta) {
//		if (!beta.isColumnVector()) {
//			throw new IllegalArgumentException("The vector is not a column vector!");
//		} else {
//			this.parameterNames.clear();
//			this.parameterValues.clear();
//			for (int i = 0; i < beta.m_iRows; i++) {
//				setParameterValue(i, beta.m_afData[i][0]);
//			}
//		}
//	}
//	
//	/**
//	 * This method returns the vector of parameters.
//	 * @return a Matrix instance
//	 */
//	public Matrix getBeta() {
//		Matrix m = new Matrix(getNumberOfParameters(), 1);
//		for (int i = 0; i < getNumberOfParameters(); i++) {
//			m.m_afData[i][0] = getParameterValue(i);
//		}
//		return m;
//	}

	
//	@Override
//	public abstract Double getValue();
//
//	@Override
//	public abstract Matrix getGradient();
//
//	@Override
//	public abstract Matrix getHessian();
//	
//
//	/**
//	 * This method sets a bound for a particular parameter
//	 * @param parameterName an Integer instance that defines the parameter
//	 * @param bound a ParameterBound object
//	 */
//	public void setBounds(Integer parameterName, ParameterBound bound) {
//		if (parameterBounds == null) {
//			parameterBounds = new HashMap<Integer, ParameterBound>();
//		}
//		parameterBounds.put(parameterName, bound);
//	}
	
}
