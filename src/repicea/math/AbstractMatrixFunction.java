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
 * The AbstractMatrixFunction class extends the AbstractFunction class with the variables
 * being set as Matrix instances. Instances of this class can be derived with respect to
 * the parameters of the function.
 * @author Mathieu Fortin - November 2012
 *
 * @param <ParameterID>	definition of the parameter index 
 * @param <ParameterClass> class of the parameters
 * @param <VariableID> definition of the variable index
 * @param <VariableClass> class of the variables
 */
@SuppressWarnings("serial")
public abstract class AbstractMatrixFunction<ParameterID extends Serializable, ParameterClass extends Serializable, VariableID extends Serializable, VariableClass extends Serializable> 
							extends AbstractFunction<ParameterID, ParameterClass, VariableID, VariableClass> 
							implements EvaluableFunction<Matrix>, DerivableMatrixFunction<ParameterID>, Serializable {
	
	
	@Override
	public abstract Matrix getValue();
	
	@Override
	public abstract Matrix getGradient(ParameterID parameter);

	@Override
	public abstract Matrix getHessian(ParameterID parameter1, ParameterID parameter2);

	
	
	
	
}
