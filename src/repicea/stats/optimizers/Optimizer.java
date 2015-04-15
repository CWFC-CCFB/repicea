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
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.estimates.Estimate;
import repicea.stats.model.StatisticalModel;

public interface Optimizer {

	public static enum LineSearchMethod {TEN_EQUAL(10),	SINGLE_TRIAL(0);
		private Number numberOfTrial;
	
		private LineSearchMethod(int numberOfTrial) {
			this.numberOfTrial = numberOfTrial;
		}
	
		public Number getParameter() {return numberOfTrial;}
	}

	/**
	 * The OptimizationException class encompasses all the exception that can be thrown when the
	 * optimizer fails to reach convergence.
	 * @author Mathieu Fortin - June 2011
	 */
	public static class OptimizationException extends Exception {
		private static final long serialVersionUID = 20110614L;

		public OptimizationException(String message) {
			super(message);
		}
	}

	/**
	 * This method optimizes the likelihood of the StatisticalModel instance.
	 * @param model a StatisticalModel object
	 */
	public boolean optimize(StatisticalModel<? extends StatisticalDataStructure> model) throws OptimizationException;
	public boolean isConvergenceAchieved();
	public Estimate<Matrix, ?> getParameters();
	public Matrix getCorrelationMatrix();

	/**
	 * This method sets the verbose to true or false.
	 * @param verbose true to get the information about the subiteration or false (DEFAULT VALUE).
	 */
	public void setVerboseEnabled(boolean verbose);

}
