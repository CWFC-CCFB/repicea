/*
 * This file is part of the repicea library.
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
 * This interface ensures the tree instance can provide its vigor 
 * class, i.e. I, II, III, IV.
 * @author Mathieu Fortin - November 2012
 */
public interface VigorClassProvider {

	/**
	 * Vigour class according to Majcen (1990).
	 * @author Mathieu Fortin - May 2010
	 */
	public static enum VigorClass {
		V1,
		V2,
		V3,
		V4;
		
		private Matrix dummy;
		private Matrix dummyVig;
		private Matrix dummyProd;
		
		VigorClass() {
			dummy = new Matrix(1,4);
			dummy.m_afData[0][this.ordinal()] = 1d;
			
			dummyVig = new Matrix(1,2);
			if (this.ordinal() == 2 || this.ordinal() == 3) {		// non vigourous
				dummyVig.m_afData[0][0] = 1d;
			} else {												// vigourous
				dummyVig.m_afData[0][1] = 1d;
			}

			dummyProd = new Matrix(1,2);
			if (this.ordinal() == 1 || this.ordinal() == 3) {		// pulp and paper
				dummyProd.m_afData[0][0] = 1d;
			} else {												// sawlog potential
				dummyProd.m_afData[0][1] = 1d;
			}

		}
		
		public Matrix geDummy() {return dummy;}
		public Matrix geDummyVig() {return dummyVig;}
		public Matrix geDummyProd() {return dummyProd;}
		
	}

	/**
	 * This method returns the vigor class for braodleaved stems according to Majcen's system. 
	 * @return a VigorClass enum variable
	 */
	public VigorClass getVigorClass();

}
