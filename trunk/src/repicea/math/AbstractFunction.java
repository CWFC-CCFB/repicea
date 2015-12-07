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

import java.io.Serializable;

/**
 * The AbstractFunction class is a private abstract class that any mathematical function with parameters
 * and variables.
 * @author Mathieu Fortin - November 2012
 *
 * @param <ParameterID>	definition of the parameter index 
 * @param <ParameterClass> class of the parameters
 * @param <VariableID> definition of the variable index
 * @param <VariableClass> class of the variables
 */
@SuppressWarnings("serial")
abstract class AbstractFunction implements Serializable {
	
	protected FastArrayList<Integer> parameterNames;
	protected FastArrayList<Double> parameterValues;
	protected FastArrayList<Integer> variableNames;
	protected FastArrayList<Double> variableValues;
	
	protected AbstractFunction() {
		parameterNames = new FastArrayList<Integer>();
		parameterValues = new FastArrayList<Double>();
		variableNames = new FastArrayList<Integer>();
		variableValues = new FastArrayList<Double>();
	}

	/**
	 * This method sets the parameter value.
	 * @param parameterName the parameter name
	 * @param parameterValue the parameter value
	 */
	public void setParameterValue(Integer parameterName, Double parameterValue) {
		if (!parameterNames.contains(parameterName)) {
			parameterNames.add(parameterName);
			parameterValues.add(parameterValue);
		} else {
			int index = parameterNames.indexOf(parameterName);
			parameterValues.set(index, parameterValue);
		}
	}

	/**
	 * This method retrieve the parameter defined by the parameterName parameter.
	 * @param parameterName the name of the parameter to be retrieved
	 * @return the parameter object
	 */
	public Double getParameterValue(Integer parameterName) {
		int index = parameterNames.indexOf(parameterName);
		return parameterValues.get(index);
	}

	/**
	 * This method sets the variable value associated with this variable name.
	 * @param variableName the name of the variable 
	 * @param variableValue its value (VariableClass)
	 */
	public void setVariableValue(Integer variableName, Double variableValue) {
		if (!variableNames.contains(variableName)) {
			variableNames.add(variableName);
			variableValues.add(variableValue);
		} else {
			int index = variableNames.indexOf(variableName);
			variableValues.set(index, variableValue);
		}
	}
	
	public Double getVariableValue(Integer variableName) {
		int index = variableNames.indexOf(variableName);
		return variableValues.get(index);
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

}
