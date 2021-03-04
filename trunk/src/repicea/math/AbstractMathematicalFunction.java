/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2019 Mathieu Fortin for Rouge-Epicea
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
import java.util.HashMap;
import java.util.Map;


/**
 * The AbstractMathematicalFunction class suits mathematical function that can be derived. The return value
 * can be an complex object to enable the nesting of different mathematical functions.
 * @author Mathieu Fortin - June 2011
 */
@SuppressWarnings("serial")
public abstract class AbstractMathematicalFunction implements EvaluableFunction<Double>, DerivableMathematicalFunction, Serializable {
	
	protected Map<Integer, ParameterBound> parameterBounds;

	private final FastArrayList<Double> parameterValues;
	private final FastArrayList<Double> variableValues;
	
	protected AbstractMathematicalFunction() {
		parameterValues = new FastArrayList<Double>();
		variableValues = new FastArrayList<Double>();
	}

	/**
	 * This method sets the parameter value.
	 * @param parameterIndex the parameter index
	 * @param parameterValue the parameter value
	 */
	public void setParameterValue(int parameterIndex, double parameterValue) {
		if (parameterBounds != null && parameterBounds.containsKey(parameterIndex)) {
			ParameterBound bound = parameterBounds.get(parameterIndex);
			parameterValue = bound.validateParameter(parameterValue);
		}
		if (parameterIndex < getNumberOfParameters()) {
			parameterValues.set(parameterIndex, parameterValue);
		} else if (parameterIndex == getNumberOfParameters()) {
			parameterValues.add(parameterValue);
		} else {
			throw new InvalidParameterException("The parameter index is not sequential!");
		}
	}

	/**
	 * This method retrieve the parameter defined by the parameterName parameter.
	 * @param parameterIndex the index of the parameter to be retrieved
	 * @return a double
	 */
	public double getParameterValue(int parameterIndex) {
		return parameterValues.get(parameterIndex);
	}

	/**
	 * This method sets the variable value associated with this variable name.
	 * @param variableIndex the index of the variable 
	 * @param variableValue its value (a double)
	 */
	public void setVariableValue(int variableIndex, double variableValue) {
		if (variableIndex < getNumberOfVariables()) {
			variableValues.set(variableIndex, variableValue);
		} else if (variableIndex == getNumberOfVariables()) {
			variableValues.add(variableValue);
		} else {
			throw new InvalidParameterException("The variable index is not sequential!");
		}
	}
	
	/**
	 * This method returns the value of the variable at index variableIndex
	 * @param variableIndex an integer
	 * @return a double
	 */
	public double getVariableValue(int variableIndex) {
		return variableValues.get(variableIndex);
	}
	
	/**
	 * This method returns the number of parameters involved in the function.
	 * @return a integer
	 */
	public int getNumberOfParameters() {return parameterValues.size();}
	
	/**
	 * This method returns the number of variables in the function. 
	 * @return an integer
	 */
	public int getNumberOfVariables() {return variableValues.size();}

	@Override
	public abstract Double getValue();

	@Override
	public abstract Matrix getGradient();
	
	@Override
	public abstract Matrix getHessian();

	/**
	 * This method sets a bound for a particular parameter
	 * @param parameterIndex an Integer instance that defines the parameter
	 * @param bound a ParameterBound object
	 */
	public void setBounds(int parameterIndex, ParameterBound bound) {
		if (parameterBounds == null) {
			parameterBounds = new HashMap<Integer, ParameterBound>();
		}
		parameterBounds.put(parameterIndex, bound);
	}

	/**
	 * This method sets the vector of explanatory variables. The method essentially
	 * relies on the setVariableValue() of the AbstractMathematicalFunction class.
	 * @param x a Matrix instance 
	 * @throws IllegalArgumentException if the parameter x is not a row vector
	 */
	public void setX(Matrix x) {
		if (!x.isRowVector()) {
			throw new IllegalArgumentException("The vector is not a row vector!");
		} else {
			variableValues.clear();
			for (int j = 0; j < x.m_iCols; j++) {
				setVariableValue(j, x.getValueAt(0, j));
			}
		}
	}
	
	/**
	 * This method sets the vector of parameters. The method essentially relies on
	 * the setParameterValue() of the AbstractMathematicalFunction class.
	 * @param beta a Matrix instance
	 * @throws IllegalArgumentException if beta is not a column vector
	 */
	public void setBeta(Matrix beta) {
		if (!beta.isColumnVector()) {
			throw new IllegalArgumentException("The vector is not a column vector!");
		} else {
			this.parameterValues.clear();
			for (int i = 0; i < beta.m_iRows; i++) {
				setParameterValue(i, beta.getValueAt(i, 0));
			}
		}
	}
	
	/**
	 * This method returns the vector of parameters.
	 * @return a Matrix instance
	 */
	public Matrix getBeta() {
		Matrix m = new Matrix(getNumberOfParameters(), 1);
		for (int i = 0; i < getNumberOfParameters(); i++) {
			m.setValueAt(i, 0, getParameterValue(i));
		}
		return m;
	}

}
