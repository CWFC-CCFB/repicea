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
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.model.LogLikelihood;
import repicea.stats.model.StatisticalModel;

/**
 * The NewtonRaphsonOptimizer class implements the Optimizer interface. It optimizes a log-likelihood function using the
 * Newton-Raphson algorithm. The vector of parameter changes is estimated as - inv(d2LLK/d2Beta) * dLLK/dBeta.
 * @author Mathieu Fortin - June 2011
 */
public class NewtonRaphsonOptimizer extends AbstractOptimizer implements MaximumLikelihoodOptimizer {

	protected int maxNumberOfIterations = 20;
	protected double gradientCriterion = 1E-3;
	private double maximumLogLikelihood;
	private int iterationID;
	

	/**
	 * This method optimize the log-likelihood function using the Newton-Raphson optimisation step.
	 * @param model the model that provides the log-likelihood function
	 * @param originalBeta the vector that contains the parameters of the previous outer optimisation
	 * @param optimisationStep the optimisation step from the Newton-Raphson algorithm
	 * @param previousLogLikelihood the value of the log-likelihood function in the last outer optimisation
	 * @throws OptimisationException if the inner optimisation is not successful
	 */
	protected double runInnerOptimisation(StatisticalModel<?> model, 
			Matrix originalBeta, 
			Matrix optimisationStep,
			double previousLogLikelihood,
			Optimizer.LineSearchMethod lineSearchMethod) throws OptimizationException {
		
		LogLikelihood llk = model.getLogLikelihood();

		int numberSubIter = 0;
		int maxNumberOfSubiterations = (Integer) lineSearchMethod.getParameter();
		
		double scalingFactor = 1d;
		double currentLlkValue;
		
		do {
			model.setParameters(originalBeta.add(optimisationStep.scalarMultiply(scalingFactor - numberSubIter * .1)));
			currentLlkValue = llk.getValue();
			if (isVerboseEnabled()) {
				System.out.println("    Subiteration : " + numberSubIter + "; Log-likelihood : " + currentLlkValue);
			}
			numberSubIter++;
		} while ((Double.isNaN(currentLlkValue) || currentLlkValue < previousLogLikelihood) && numberSubIter < maxNumberOfSubiterations); // loop if the number of iterations is not over the maximum number and either the likelihood is still higher or non defined
		
		if (Double.isNaN(currentLlkValue) ||  currentLlkValue < previousLogLikelihood) {
			throw new OptimizationException("Failed to improve the log-likelihood function !");
		} else {
			return currentLlkValue;
		}
	}
	
	@Override
	public boolean optimize(StatisticalModel<? extends StatisticalDataStructure> model) throws OptimizationException {
		double convergenceCriterion = model.getConvergenceCriterion();

		LogLikelihood llk = model.getLogLikelihood();
		
		double llkValue0 = llk.getValue();
		Matrix gradient = llk.getGradient();
		Matrix hessian = llk.getHessian();
	
				
		iterationID = 0;
		
		double gconv = calculateConvergence(gradient, hessian, llkValue0);
		
		convergenceAchieved = false;
		if (Math.abs(gconv) < convergenceCriterion) {
			convergenceAchieved = true;
		}
		
		try {
			while (!convergenceAchieved && iterationID <= maxNumberOfIterations) {
				iterationID++;
				Matrix optimisationStep = hessian.getInverseMatrix().multiply(gradient).scalarMultiply(-1d);
				
				Matrix originalBeta = model.getParameters().getDeepClone();

				llkValue0 = runInnerOptimisation(model, originalBeta, optimisationStep, llkValue0, LineSearchMethod.TEN_EQUAL);		// if it does not throw an Exception, it means the inner optimisation was successful. 
				gradient = llk.getGradient();
				hessian = llk.getHessian();

				gconv = calculateConvergence(gradient, hessian, llkValue0);
				
				if (gconv < 0) {
					convergenceAchieved = !gradient.getAbsoluteValue().anyElementLargerThan(1E-5);
					System.out.println("GConv is negative!");
					throw new OptimizationException("Convergence could not be achieved after " + ((Integer) iterationID).toString() + " iterations!");
				} else if (gconv < convergenceCriterion) {
					convergenceAchieved = true;
				}

				System.out.println("Iteration : " + iterationID + "; Log-likelihood : " + llkValue0 + "; df : " + gconv + "; parms : " + model.getParameters().toString());
			}  

			if (iterationID > maxNumberOfIterations && !convergenceAchieved) {
				throw new OptimizationException("Convergence could not be achieved after " + ((Integer) iterationID).toString() + " iterations!");
			}

			if (gradient.getAbsoluteValue().anyElementLargerThan(gradientCriterion)) {
				System.out.println("A least one element of the gradient vector is larger than 1E-3.");
			}

			maximumLogLikelihood = llkValue0;
			return convergenceAchieved;
		} catch (OptimizationException e) {
			throw e;
		} finally {
			betaVector = new GaussianEstimate(model.getParameters(), hessian.getInverseMatrix().scalarMultiply(-1d));
		}
	}


	/**
	 * This method returns a double that is the convergence indicator based on the gradient, the hessian and the value of the log-likelihood function.
	 * @param gradient a Matrix instance
	 * @param hessian a Matrix instance
	 * @param llk a double that is the value of the log-likelihood function
	 * @return the indicator (a Double instance)
	 */
	protected double calculateConvergence(Matrix gradient, Matrix hessian, double llk) {
		return gradient.transpose().multiply(hessian.getInverseMatrix()).multiply(gradient).scalarMultiply(1d / llk).m_afData[0][0];
	}


	@Override
	public double getMaximumLogLikelihood() {
		if (convergenceAchieved) {
			return maximumLogLikelihood;
		} else {
			return 0;
		}
	}

	/**
	 * This method returns the number of iterations to reach convergence. It returns -1 if 
	 * convergence could not be reached.
	 * @return an integer
	 */
	protected int getNumberOfIterations() {
		if (convergenceAchieved) {
			return iterationID;
		} else {
			return -1;
		}
	}
	
}
