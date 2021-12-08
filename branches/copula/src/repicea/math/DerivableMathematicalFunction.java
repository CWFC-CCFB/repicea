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
 * This interface returns the first and second derivatives of a mathematical function.
 * @author Mathieu Fortin - October 2011
 */
public interface DerivableMathematicalFunction {

	/**
	 * This method returns a vector that contains the first derivatives of the function with respect to its parameters.
	 * @return a Matrix instance
	 */
	public Matrix getGradient();
	
	
	/**
	 * This method returns a matrix that contains the second derivatives of the function with respect to its parameters.
	 * @return a Matrix instance
	 */
	public Matrix getHessian();
	
}
