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

import java.util.Map;

public interface MathematicalFunction extends EvaluableFunction<Double>, DifferentiableMathematicalFunction {

	
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
	
	/**
	 * Check if a parameter value lies within the bounds if any.
	 * @param parameterIndex the index of the parameter
	 * @param parameterValue the parameter value
	 * @return true if either the parameter value lies within the bounds or there is no bound for this 
	 * parameter index 
	 */
	public boolean isThisParameterValueWithinBounds(int parameterIndex, double parameterValue);

	
//	/**
//	 * Provide a deep clone of the parameter bounds. <br>
//	 * <br>
//	 * This method is used to propagate the parameter bounds in wrappers. IMPORTANT:
//	 * it should not be used to set a bound since the method returns a copy of the
//	 * original bounds. Use the setBound method instead.
//	 * @return a Map of integers and parameter bounds.
//	 */
//	public Map<Integer, ParameterBound> getBounds();
	
}
