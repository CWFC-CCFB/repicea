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
package repicea.stats.model.lmm;

import repicea.math.DerivableMatrixFunction;
import repicea.math.EvaluableFunction;
import repicea.math.Matrix;
import repicea.stats.data.DataBlock;

/**
 * This AbstractVComponent class represents either a random effect or a covariance structure.
 * @author Mathieu Fortin - November 2012
 */
abstract class AbstractVComponent implements EvaluableFunction<Matrix>, DerivableMatrixFunction<Integer> {
	
	protected DataBlock currentDataBlock;
	protected final String hierarchicalLevel;

	AbstractVComponent(String hierarchicalLevel) {
		this.hierarchicalLevel = hierarchicalLevel;
	}

	/**
	 * This method sets the DataBlock instance on which the calculus will be performed.
	 * @param db a DataBlock instance
	 */
	protected void setDataBlock(DataBlock db) {
		this.currentDataBlock = db;
	}
	
	/**
	 * This method sets a covariance parameter. 
	 * @param index the index of the parameter
	 * @param value its value
	 */
	protected abstract void setParameter(int index, double value);

	/**
	 * This method returns the number of parameters in this AbstractVComponent-derived instance.
	 * @return an Integer
	 */
	protected abstract int getNumberOfParameters();
	
}
