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
package repicea.stats;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;


/**
 * The CentralMomentsGettable interface ensures the mean and the variance are available.
 * @author Mathieu Fortin - August 2012
 */
public interface CentralMomentsGettable {

	/**
	 * This method returns the first central moment, i.e. the mean vector, of the random variable.
	 * @return a Matrix instance
	 */
	public Matrix getMean();
	
	/**
	 * This method returns the second central moment, i.e. the variance-covariance matrix, of the random variable.
	 * @return a Number instance
	 */
	public SymmetricMatrix getVariance();


}
