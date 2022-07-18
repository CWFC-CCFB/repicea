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
package repicea.stats.model.glm;


import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import repicea.math.Matrix;
import repicea.stats.data.DataSet;
import repicea.stats.data.GenericStatisticalDataStructure;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.estimators.Estimator;
import repicea.stats.estimators.MaximumLikelihoodEstimator;
import repicea.stats.estimators.MaximumLikelihoodEstimator.MaximumLikelihoodCompatibleModel;
import repicea.stats.model.AbstractStatisticalModel;
import repicea.stats.model.CompositeLogLikelihoodWithExplanatoryVariable;
import repicea.stats.model.IndividualLogLikelihood;
import repicea.stats.model.PredictableModel;
import repicea.stats.model.WrappedIndividualLogLikelihood;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.REpiceaLogManager;

/**
 * This class implements generalized linear models. 
 * @author Mathieu Fortin - August 2011
 */
public class GeneralizedLinearModel extends AbstractStatisticalModel implements MaximumLikelihoodCompatibleModel, PredictableModel {

	protected static class LikelihoodValue implements Comparable<LikelihoodValue> {

		private double llk;
		private Matrix beta;
		
		protected LikelihoodValue(Matrix beta, double llk) {
			this.beta = beta.getDeepClone();
			this.llk = llk;
		}
		
		@Override
		public int compareTo(LikelihoodValue arg0) {
			double reference = ((LikelihoodValue) arg0).llk;
			if (this.llk < reference) {
				return 1;
			} else if (this.llk == reference) {
				return 0;
			} else {
				return -1;
			}
		}
		
		protected Matrix getParameters() {return beta;}
	}

	
	private final StatisticalDataStructure dataStruct;
	private final CompositeLogLikelihoodWithExplanatoryVariable completeLLK;
	protected final IndividualLogLikelihood individualLLK;
	protected final LinkFunction lf;
	protected Matrix matrixX;		// reference
	protected Matrix y;				// reference
	
	private double convergenceCriterion = 1E-8;

	

	/**
	 * General constructor
	 * @param dataSet the fitting data
	 * @param linkFunctionType the type of ling function (Logit, CLogLog, ...)
	 * @param modelDefinition a String that defines the dependent variable and the effects of the model
	 * @param startingBeta the starting values of the parameters
	 */
	public GeneralizedLinearModel(DataSet dataSet, Type linkFunctionType, String modelDefinition, Matrix startingBeta) {
		this(dataSet, linkFunctionType, modelDefinition, null, startingBeta);
	}
	
	/**
	 * Generic private constructor.
	 * @param dataSet
	 * @param linkFunctionType
	 * @param modelDefinition
	 * @param llk
	 * @param startingBeta
	 */
	private GeneralizedLinearModel(DataSet dataSet, Type linkFunctionType, String modelDefinition, IndividualLogLikelihood llk, Matrix startingBeta) {
		super();
		dataStruct = createDataStructure(dataSet);

		// then define the model effects and retrieve matrix X and vector y
		try {
			setModelDefinition(modelDefinition);
		} catch (StatisticalDataException e) {
			System.out.println("Unable to define this model : " + modelDefinition);
			e.printStackTrace();
		}
		lf = createLinkFunction(linkFunctionType);
		if (llk != null) {
			individualLLK = llk;
		} else {
			individualLLK = createIndividualLLK();
		}
		// first initialize the model structure
		setParameters(startingBeta);
		completeLLK = createCompleteLLK();
	}
	
	/**
	 * Constructor using a vector of 0s as starting values for the parameters
	 * @param dataSet the fitting data
	 * @param linkFunctionType the type of ling function (Logit, CLogLog, ...)
	 * @param modelDefinition a String that defines the dependent variable and the effects of the model
	 */
	public GeneralizedLinearModel(DataSet dataSet, Type linkFunctionType, String modelDefinition) {
		this(dataSet, linkFunctionType, modelDefinition, null);
	}

	/**
	 * Constructor for derived class.
	 */
	protected GeneralizedLinearModel(GeneralizedLinearModel glm) {
		this(glm.getDataStructure().getDataSet(), glm.getLinkFunctionType(), glm.getModelDefinition(), glm.individualLLK, null);
	}


	protected StatisticalDataStructure createDataStructure(DataSet dataSet) {
		return new GenericStatisticalDataStructure(dataSet);
	}

	protected StatisticalDataStructure getDataStructure() {
		return (GenericStatisticalDataStructure) dataStruct;
	}
	
	protected CompositeLogLikelihoodWithExplanatoryVariable createCompleteLLK() {
		return new CompositeLogLikelihoodWithExplanatoryVariable(individualLLK, matrixX, y);
	}

	protected LinkFunction createLinkFunction(Type linkFunctionType) {
		return new LinkFunction(linkFunctionType);
	}

	protected IndividualLogLikelihood createIndividualLLK() {
		return new WrappedIndividualLogLikelihood(new LikelihoodGLM(lf));
	}

	/**
	 * This method returns the type of the link function.
	 * @return a LinkFunction.Type enum variable
	 */
	public LinkFunction.Type getLinkFunctionType() {
		return lf.getType();
	}
	

	@Override
	protected void setModelDefinition(String modelDefinition) throws StatisticalDataException {
		super.setModelDefinition(modelDefinition);
		getDataStructure().constructMatrices(modelDefinition);
		matrixX = getDataStructure().getMatrixX();
		y = getDataStructure().getVectorY();
	}
	

	
	@Override
	public void setParameters(Matrix beta) {
		if (beta == null) {
			individualLLK.setParameters(new Matrix(matrixX.m_iCols, 1));		// default starting parameters at 0
		} else {
			individualLLK.setParameters(beta);
		}
	}
		
	@Override
	public Matrix getParameters() {return individualLLK.getParameters();}

	@Override
	protected Estimator instantiateDefaultEstimator() {
		return new MaximumLikelihoodEstimator(this);
	}

	@Override
	public String toString() {
		return "Generalized linear model";
	}

	/**
	 * This method scans the log likelihood function within a range of values for a particular parameter.
	 * @param parameterName the index of the parameter
	 * @param start the starting value
	 * @param end the ending value
	 * @param step the step between these two values.
	 */
	@SuppressWarnings("unchecked")
	public void gridSearch(int parameterName, double start, double end, double step) {
		System.out.println("Initializing grid search...");
		ArrayList<LikelihoodValue> likelihoodValues = new ArrayList<LikelihoodValue>();
		Matrix originalParameters = getParameters();
		double llk;
		for (double value = start; value < end + step; value+=step) {
			Matrix beta = originalParameters.getDeepClone();
			beta.setValueAt(parameterName, 0, value);
			setParameters(beta);
			completeLLK.reset();
			llk = completeLLK.getValue();
			likelihoodValues.add(new LikelihoodValue(beta, llk));
			REpiceaLogManager.logMessage(MaximumLikelihoodEstimator.LOGGER_NAME, Level.FINER, MaximumLikelihoodEstimator.LOGGER_NAME, "Parameter value : " + value + "; Log-likelihood : " + llk);
		}
		
		Collections.sort(likelihoodValues);
		LikelihoodValue lk;
		Matrix bestFittingParameters = null;
		for (int i = 0; i < likelihoodValues.size(); i++) {
			lk = likelihoodValues.get(i);
			if (!Double.isNaN(lk.llk)) {
				bestFittingParameters = lk.getParameters();
				break;
			}
		}
		if (bestFittingParameters == null) {
			throw new InvalidParameterException("All the likelihoods of the grid are NaN!");
		} else {
			setParameters(bestFittingParameters);
		}
	}

	@Override
	public int getNumberOfObservations() {return getDataStructure().getNumberOfObservations();}
	
	public Matrix getLinearPredictions() {
		if (getEstimator().isConvergenceAchieved()) {
			Matrix pred = new Matrix(getNumberOfObservations(), 2);
			Matrix beta = getEstimator().getParameterEstimates().getMean().getSubMatrix(0, matrixX.m_iCols - 1, 0, 0);
			Matrix linearPred = matrixX.multiply(beta);
			Matrix omega = getEstimator().getParameterEstimates().getVariance().getSubMatrix(0, matrixX.m_iCols - 1, 0, matrixX.m_iCols - 1);
			for (int i = 0; i < getNumberOfObservations(); i++) {
				pred.setValueAt(i, 0, linearPred.getValueAt(i, 0));
				Matrix x_i = matrixX.getSubMatrix(i, i, 0, matrixX.m_iCols - 1);
				pred.setValueAt(i, 1, x_i.multiply(omega).multiply(x_i.transpose()).getValueAt(0, 0));
			}
			return pred;
		} else {
			return null;
		}
	}

	@Override
	public Matrix getPredicted() {
		if (getEstimator().isConvergenceAchieved()) {
			completeLLK.setParameters(getEstimator().getParameterEstimates().getMean());
			return completeLLK.getPredictions();
		} else {
			return null;
		}
	}

	@Override
	public Matrix getResiduals() {
		if (getEstimator().isConvergenceAchieved()) {
			return y.subtract(getPredicted());
		} else {
			return null;
		}
	}

	@Override
	public CompositeLogLikelihoodWithExplanatoryVariable getCompleteLogLikelihood() {return completeLLK;}

	@Override
	public boolean isInterceptModel() {return getDataStructure().isInterceptModel();}

	@Override
	public List<String> getEffectList() {return getDataStructure().getEffectList();}

	@Override
	public double getConvergenceCriterion() {return convergenceCriterion;}
	
	public void setConvergenceCriterion(double d) {
		if (d < 1E-16) {
			throw new InvalidParameterException("The minimum value for the convergence criterion is 1E-16");
		} 
		convergenceCriterion = d;
	}



}
