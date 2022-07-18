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

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import repicea.math.Matrix;
import repicea.math.optimizer.NewtonRaphsonOptimizer;
import repicea.stats.LinearStatisticalExpression;
import repicea.stats.data.DataSet;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.estimators.MaximumLikelihoodEstimator;
import repicea.stats.model.WrappedIndividualLogLikelihood;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaLogManager;

public class TwoStepGeneralizedLinearModel<P extends StatisticalDataStructure> extends GeneralizedLinearModel<P> {

	@SuppressWarnings("serial")
	static class DoubleLinkFunction extends LinkFunction {

		private final LinkFunction firstLinkFunction;
		private final LinkFunction secondLinkFunction;

		public DoubleLinkFunction(Type type) {
			super(type);
			firstLinkFunction = new LinkFunction(type);
			secondLinkFunction = new LinkFunction(type);
		}

		@Override
		public final LinearStatisticalExpression getOriginalFunction() {
			return (LinearStatisticalExpression) super.getOriginalFunction();
		}
		
		@Override
		public Double getValue() {
			setParameterValuesInLinkFunctions();
			double prob1 = firstLinkFunction.getValue();
			double prob2 = secondLinkFunction.getValue();
			return prob1 * prob2;
		}

	
		private void setParameterValuesInLinkFunctions() {
			if (getOriginalFunction().getNumberOfParameters() != getOriginalFunction().getNumberOfVariables()) {
				throw new IllegalArgumentException("Incompatible vectors");
			} 
			for (int i = 0; i < getNumberOfParameters() - 1; i++) {
				firstLinkFunction.setParameterValue(i, getParameterValue(i));;
				firstLinkFunction.setVariableValue(i, getVariableValue(i));;
			}
			secondLinkFunction.setParameterValue(0, getParameterValue(getNumberOfParameters()-1));
			secondLinkFunction.setVariableValue(0, getVariableValue(getNumberOfParameters()-1));
		}
		
		@Override
		public Matrix getGradient() {
			setParameterValuesInLinkFunctions();
			Matrix firstLinkGradient = firstLinkFunction.getGradient().scalarMultiply(secondLinkFunction.getValue());
			Matrix secondLinkGradient = secondLinkFunction.getGradient().scalarMultiply(firstLinkFunction.getValue());
			Matrix gradient = firstLinkGradient.matrixStack(secondLinkGradient, true);
			return gradient;
		}

		@Override
		public Matrix getHessian() {
			setParameterValuesInLinkFunctions();
			Matrix firstHessian = firstLinkFunction.getHessian().scalarMultiply(secondLinkFunction.getValue());
			Matrix secondHessian = secondLinkFunction.getHessian().scalarMultiply(firstLinkFunction.getValue());
			Matrix hessian = firstHessian.matrixDiagBlock(secondHessian);
			Matrix derPart1derPart2gradient = firstLinkFunction.getGradient().multiply(secondLinkFunction.getGradient());
			hessian.setSubMatrix(derPart1derPart2gradient, 0, getNumberOfParameters() - 1);
			hessian.setSubMatrix(derPart1derPart2gradient.transpose(), getNumberOfParameters() - 1, 0);
			return hessian;
		}
	}

	/**
	 * Constructor
	 * @param dataSet
	 * @param linkFunctionType
	 * @param modelDefinition
	 */
	public TwoStepGeneralizedLinearModel(DataSet dataSet, Type linkFunctionType, String modelDefinition, Matrix beta) {
		super(dataSet, linkFunctionType, modelDefinition, beta);
	}
	
	@Override
	protected void setModelDefinition(String modelDefinition) throws StatisticalDataException {
		super.setModelDefinition(modelDefinition);
		matrixX = matrixX.matrixStack(new Matrix(matrixX.m_iRows, 1, 1d, 0d), false);	// we add a column of one for the second link function
	}
	
	@Override
	protected void initializeLinkFunction(Type linkFunctionType) {
		LinkFunction lf = new DoubleLinkFunction(linkFunctionType);
		individualLLK = new WrappedIndividualLogLikelihood(new LikelihoodGLM(lf));
		setCompleteLLK();
	}
	
	@Override
	public String toString() {
		return "Two-step generalized linear model";
	}

	public static void main(String[] args) throws Exception {
		String path = ObjectUtility.getPackagePath(TwoStepGeneralizedLinearModel.class);
		String filename = path + "exampleDistanceSample.csv";
		filename = filename.replace("main", "test");
		DataSet ds = new DataSet(filename, true);
		NewtonRaphsonOptimizer.LOGGER_NAME = MaximumLikelihoodEstimator.LOGGER_NAME;
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).setLevel(Level.FINER);
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.FINER);
		REpiceaLogManager.getLogger(MaximumLikelihoodEstimator.LOGGER_NAME).addHandler(ch);
		Matrix beta = new Matrix(3,1);
		beta.setValueAt(0, 0, -10);
		beta.setValueAt(1, 0, 0.1);
		beta.setValueAt(2, 0, 0.5);
		TwoStepGeneralizedLinearModel model = new TwoStepGeneralizedLinearModel(ds, Type.Logit, "isConspecificIn ~ distance", beta);
		model.doEstimation();
		model.getSummary();
	}
}
