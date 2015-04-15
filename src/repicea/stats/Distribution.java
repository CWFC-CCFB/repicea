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

import java.io.Serializable;



/**
 * The Distribution interface provides the basic function for a probability density (or mass) function.
 * @author Mathieu Fortin - August 2012
 *
 */
public interface Distribution<N extends Number> extends CentralMomentsGettable<N>, Serializable {

	public enum Type {GAUSSIAN, UNIFORM, NONPARAMETRIC, UNKNOWN, CHI_SQUARE}
	
	/**
	 * This method returns true if the distribution is parametric or false otherwise.
	 * @return a boolean
	 */
	public boolean isParametric();
	
	
	/**
	 * This method returns true if the GaussianFunction instance is multivariate.
	 * @return a boolean
	 */
	public boolean isMultivariate();
	
	
	/** 
	 * This method returns the type of the distribution.
	 * @return a Type enum
	 */
	public Type getType();

	
	/**
	 * This method draws a random realization from the distribution.
	 * @return the observation in a Matrix instance
	 */
	public N getRandomRealization();
	
	
//	/**
//	 * This method returns the quantile of the distribution.
//	 * @param values a array of doubles
//	 * @return a double
//	 */
//	public double getQuantile(double... values);
}
