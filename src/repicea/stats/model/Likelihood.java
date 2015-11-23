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
package repicea.stats.model;

import repicea.stats.AbstractStatisticalExpression;


/**
 * The Likelihood class provides the basic services for all Likelihood classes
 * @author Mathieu Fortin - June 2011
 */
@SuppressWarnings("serial")
public abstract class Likelihood extends AbstractStatisticalExpression {
	
	protected double observedValue;

	
	protected abstract AbstractStatisticalExpression getInnerExpression();
	
	@Override
	public void setParameterValue(Integer parameterIndex, Double parameterValue) {
		getInnerExpression().setParameterValue(parameterIndex, parameterValue);
	} 

	@Override
	public Double getParameterValue(Integer parameterIndex) {
		return getInnerExpression().getParameterValue(parameterIndex);
	}
	
	@Override
	public void setVariableValue(Integer variableIndex, Double variableValue) {
		getInnerExpression().setVariableValue(variableIndex, variableValue);
	}

	@Override
	public Double getVariableValue(Integer variableIndex) {
		return getInnerExpression().getVariableValue(variableIndex);
	}

	protected void setObservedValue(double observedValue) {
		this.observedValue = observedValue;
	}

	
}
