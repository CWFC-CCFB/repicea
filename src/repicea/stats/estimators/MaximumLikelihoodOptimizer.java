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
package repicea.stats.optimizers;

/**
 * This interface specifies if the optimizer is based on a maximum likelihood theory.
 * @author Mathieu Fortin - August 2011
 */
public interface MaximumLikelihoodOptimizer extends Optimizer {
	
	/**
	 * Returns the maximum log likelihood value after convergence.
	 * @return a double
	 */
	public double getMaximumLogLikelihood();

}
