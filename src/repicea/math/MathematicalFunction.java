/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge-Epicea
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

public interface MathematicalFunction extends EvaluableFunction<Double>, DifferentiableMathematicalFunction {

	/**
	 * Provide the number of parameters in the function.
	 * @return an integer
	 */
	public int getNumberOfParameters();
	
	/**
	 * Provide the number of variables in the function.
	 * @return an integer
	 */
	public int getNumberOfVariables();
	
	/**
	 * Return all the parameters in a column vector.
	 * @return a Matrix instance
	 */
	public Matrix getParameters();
	
	
	/**
	 * Set a bound for a particular parameter
	 * @param parameterIndex an Integer instance that defines the parameter
	 * @param bound a ParameterBound object
	 */
	public void setBounds(int parameterIndex, ParameterBound bound);

}
