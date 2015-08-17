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

import repicea.math.Matrix;
import repicea.stats.distributions.GaussianDistribution;
import repicea.stats.estimates.Estimate;

/**
 * This class is the main class for all optimizers.
 * @author Mathieu Fortin - August 2011
 */
abstract class AbstractOptimizer implements Optimizer {

	protected boolean convergenceAchieved;
	
	protected Estimate<GaussianDistribution> betaVector;
	
	private boolean verboseEnabled;
	
	/**
	 * Default constructor.
	 */
	public AbstractOptimizer() {
		verboseEnabled = false;
	}
	

	@Override
	public boolean isConvergenceAchieved() {return convergenceAchieved;}

	@Override
	public Estimate<GaussianDistribution> getParameters() {return betaVector;}

	public Matrix getCorrelationMatrix() {
		Matrix std;
		try {
			Matrix omegaMatrix = betaVector.getVariance();
			std = omegaMatrix.diagonalVector().matrixDiagonal().getLowerCholTriangle().diagonalVector();
			Matrix correlationMatrix = omegaMatrix.elementWiseDivide(std.multiply(std.transpose()));
			return correlationMatrix;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void setVerboseEnabled(boolean verboseEnabled) {this.verboseEnabled = verboseEnabled;}

	protected boolean isVerboseEnabled() {return verboseEnabled;}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
}
