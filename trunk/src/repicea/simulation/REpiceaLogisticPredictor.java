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
package repicea.simulation;


/**
 * This class defines the method to access the logistic model predictions. 
 * @author Mathieu Fortin - November 2012
 * @param <S> a class that represents the stand
 * @param <T> a class that represents the tree
 */
public abstract class REpiceaLogisticPredictor<S, T> extends REpiceaPredictor {

	private static final long serialVersionUID = 20131015L;

	protected REpiceaLogisticPredictor(boolean isParametersVariabilityEnabled, boolean isRandomEffectsVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
	}

	
	/**
	 * This method returns the probability of event for a particular tree represented by the parameter
	 * "tree".
	 * @param stand a S-derived instance
	 * @param tree a T-derived instance
	 * @param parms some additional parameters
	 * @return the event probability
	 */
	public abstract double predictEventProbability(S stand, T tree, Object... parms);

	
	
	/**
	 * This method returns either a boolean if isResidualVariabilityEnabled was set to true
	 * or the probability otherwise.
	 * @param stand a S-derived instance
	 * @param tree a T-derived instance
	 * @param parms some additional parameters
	 * @return a Boolean or a double
	 */
	public Object predictEvent(S stand, T tree, Object... parms) {
		double eventProbability = predictEventProbability(stand, tree, parms);
		if (eventProbability < 0 || eventProbability > 1) {
			return null;
		} else if (isResidualVariabilityEnabled) {
			double residualError = random.nextDouble();
			if (residualError < eventProbability) {
				return true;
			} else {
				return false;
			}
		} else {
			return eventProbability;
		}
	}

	
}
