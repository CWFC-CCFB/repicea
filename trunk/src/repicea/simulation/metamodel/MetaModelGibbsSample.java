/* 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Authors: M. Fortin and J.-F. Lavoie - Canadian Forest Service
 * Copyright (C) 2020-21 Her Majesty the Queen in right of Canada
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package repicea.simulation.metamodel;

import repicea.math.Matrix;

class MetaModelGibbsSample implements Comparable<MetaModelGibbsSample> {

	final Matrix parms;
	final double llk;
	
	MetaModelGibbsSample(Matrix parms, double lk) {
		this.parms = parms;
		this.llk = lk;
	}

	@Override
	public int compareTo(MetaModelGibbsSample arg0) {
		if (llk > arg0.llk) {
			return 1;
		} else if (llk < arg0.llk) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return "LLK=" + llk + ", " + parms.toString();
	}
	
}
