/*
 * This file is part of the repicea-simulation library.
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
package repicea.simulation.covariateproviders.treelevel;

import repicea.math.Matrix;

/**
 * This interface ensures the tree instance can provide its quality
 * class, i.e. A, B, C, D.
 * @author Mathieu Fortin - November 2012
 */
public abstract interface ABCDQualityProvider {

	/** 
	 * Tree log classification ABCD
	 * @author Mathieu Fortin - May 2010
	 */
	public static enum ABCDQuality {
		A,
		B,
		C,
		D;
		
		private Matrix dummy;
		
		ABCDQuality() {
			dummy = new Matrix(1,4);
			dummy.m_afData[0][this.ordinal()] = 1d;
		}
		
		public Matrix getDummy() {return dummy;}
	}

	/**
	 * This method returns the quality class of the stem according to the ABCD classification.
	 * @return an ABCDQuality enum variable
	 */
	public ABCDQuality getABCDQuality();

	
}
