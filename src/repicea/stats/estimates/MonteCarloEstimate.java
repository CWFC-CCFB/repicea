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
package repicea.stats.estimates;

import java.util.List;

import repicea.math.Matrix;
import repicea.stats.distributions.NonparametricDistribution;

/**
 * This estimate contains the realizations of a Monte Carlo simulations.
 * @author Mathieu Fortin - October 2011
 */
public class MonteCarloEstimate extends Estimate<NonparametricDistribution> {
	
	private static final long serialVersionUID = 20110912L;
	
	/**
	 * Constructor.
	 */
	public MonteCarloEstimate() {
		super(new NonparametricDistribution());
		estimatorType = EstimatorType.MonteCarlo;
	}
	
	public int getNumberOfRealizations() {return getDistribution().getNumberOfRealizations();}
	
	public void addRealization(Matrix value) {getDistribution().addRealization(value);}
	
	public List<Matrix> getRealizations() {return getDistribution().getRealizations();}
	
}
