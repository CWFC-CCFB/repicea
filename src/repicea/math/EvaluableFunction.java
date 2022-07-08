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

/**
 * This interface ensures that the class can provide a value.
 * @author Mathieu Fortin - November 2012
 * <P> either a Double or a Matrix
 */
public interface EvaluableFunction<P> {

	/**
	 * Provides the result of the function evaluation.
	 * @return a P instance
	 */
	public P getValue();

	/**
	 * Sets the variable value associated with this variable name.
	 * @param variableIndex the index of the variable 
	 * @param variableValue its value (a double)
	 */
	public void setVariableValue(int variableIndex, double variableValue);
	
	/**
	 * Sets the parameter value.
	 * @param parameterIndex the parameter index
	 * @param parameterValue the parameter value
	 */
	public void setParameterValue(int parameterIndex, double parameterValue);

	
	/**
	 * Returns the value of the variable at index variableIndex
	 * @param variableIndex an integer
	 * @return a double
	 */
	public double getVariableValue(int variableIndex);

	/**
	 * Retrieves the parameter defined by the parameterName parameter.
	 * @param parameterIndex the index of the parameter to be retrieved
	 * @return a double
	 */
	public double getParameterValue(int parameterIndex);

}
