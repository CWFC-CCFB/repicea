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
 * The AbstractMathematicalFunction class suits mathematical function that can be derived. The return value
 * can be an complex object to enable the nesting of different mathematical functions.
 * @author Mathieu Fortin - June 2011
 *
 * @param <ParameterID>	definition of the parameter index 
 * @param <ParameterClass> class of the parameters
 * @param <VariableID> definition of the variable index
 * @param <VariableClass> class of the variables
 */
@SuppressWarnings("serial")
public abstract class AbstractMathematicalFunction<ParameterID extends Serializable, ParameterClass extends Serializable, VariableID extends Serializable, VariableClass extends Serializable> 
							extends AbstractFunction<ParameterID, ParameterClass, VariableID, VariableClass> 
							implements EvaluableFunction<Double>, DerivableMathematicalFunction, Serializable {
	
	@Override
	public abstract Double getValue();

	@Override
	public abstract Matrix getGradient();
	
	@Override
	public abstract Matrix getHessian();

}
