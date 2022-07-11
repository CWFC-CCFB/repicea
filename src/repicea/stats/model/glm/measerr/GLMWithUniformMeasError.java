/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.model.glm.measerr;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import repicea.math.AbstractMathematicalFunction;
import repicea.math.EvaluableFunction;
import repicea.math.Matrix;
import repicea.stats.data.DataSet;
import repicea.stats.integral.TrapezoidalRule;
import repicea.stats.model.IndividualLogLikelihood;
import repicea.stats.model.glm.GeneralizedLinearModel;
import repicea.stats.model.glm.LikelihoodGLM;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;

/**
 * A class implementing the generalized linear model with measurement
 * error on one variable.
 * @author Mathieu Fortin - July 2022
 */
public class GLMWithUniformMeasError extends GeneralizedLinearModel {

	
	static class GradientHessianProvider implements EvaluableFunction<Matrix> {

		private final AbstractMathematicalFunction originalFunction;
		private final boolean isForGradient;
		
		GradientHessianProvider(AbstractMathematicalFunction originalFunction, boolean isForGradient) {
			this.originalFunction = originalFunction;
			this.isForGradient = isForGradient;
		}
		
		@Override
		public Matrix getValue() {
			return isForGradient ? originalFunction.getGradient() : originalFunction.getHessian();
		}

		@Override
		public void setVariableValue(int variableIndex, double variableValue) {
			originalFunction.setVariableValue(variableIndex, variableValue);
		}

		@Override
		public void setParameterValue(int parameterIndex, double parameterValue) {
			originalFunction.setParameterValue(parameterIndex, parameterValue);
		}

		@Override
		public double getVariableValue(int variableIndex) {
			return originalFunction.getVariableValue(variableIndex);
		}

		@Override
		public double getParameterValue(int parameterIndex) {
			return originalFunction.getParameterValue(parameterIndex);
		}
		
	}
	
	
	class AdaptedTrapezoidalRule extends TrapezoidalRule {
		
		AdaptedTrapezoidalRule() {
			super();
		}

		protected void setXValuesIntoTheAdaptedTrapezoidalRule(List<Double> potentialXValues, double maxValue) {
			List<Double> pointList = new ArrayList<Double>();
			for (Double d : potentialXValues) {
				if (d < maxValue) {
					pointList.add(d);
				} else if (d >= maxValue) {
					pointList.add(maxValue);
					break;
				}
			}
			super.setXValuesFromListOfPoints(pointList);
		}
	}
		
	@SuppressWarnings("serial")
	class LinkFunctionWithMeasError extends LinkFunction {

		private final LinkFunction linkFunctionErrorFreeObs;
		private final GradientHessianProvider gradientProvider;
		private final GradientHessianProvider hessianProvider;
		
		private AdaptedTrapezoidalRule adaptedTr;
		
		public LinkFunctionWithMeasError() {
			super(Type.CLogLog);
			linkFunctionErrorFreeObs = new LinkFunction(Type.CLogLog, getOriginalFunction());
			gradientProvider = new GradientHessianProvider(linkFunctionErrorFreeObs, true);
			hessianProvider = new GradientHessianProvider(linkFunctionErrorFreeObs, false);
			adaptedTr = new AdaptedTrapezoidalRule();
		}
		
		private boolean isObservationErrorFree() {
			return getOriginalFunction().getVariableValue(indexEffectWithMeasError) == measError.minimumValueForConsideringMeasurementError;
		}
		
		@Override
		public Double getValue() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getValue();
			} else {
				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
				double invdensity = maxValue - measError.minimumValueForConsideringMeasurementError;
				adaptedTr.setXValuesIntoTheAdaptedTrapezoidalRule(measError.xValuesForIntegration, maxValue);
//				tr.setLowerBound(measError.minimumValueForConsideringMeasurementError);
//				tr.setUpperBound(maxValue);
				double marginalProbability = adaptedTr.getIntegralApproximation(linkFunctionErrorFreeObs, indexEffectWithMeasError, false) / invdensity; // false: it is a variable.
				return marginalProbability;
			}
		}

		@Override
		public Matrix getGradient() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getGradient();
			} else {
				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
				double invdensity = maxValue - measError.minimumValueForConsideringMeasurementError;
				adaptedTr.setXValuesIntoTheAdaptedTrapezoidalRule(measError.xValuesForIntegration, maxValue);
//				tr.setLowerBound(measError.minimumValueForConsideringMeasurementError);
//				tr.setUpperBound(maxValue);
				Matrix marginalGradient = adaptedTr.getIntegralApproximationForMatrixFunction(gradientProvider, indexEffectWithMeasError, false).scalarMultiply(1d/invdensity); // false: it is a variable.
				return marginalGradient;
			}
		}

		@Override
		public Matrix getHessian() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getHessian();
			} else {
				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
				double invdensity = maxValue - measError.minimumValueForConsideringMeasurementError;
				adaptedTr.setXValuesIntoTheAdaptedTrapezoidalRule(measError.xValuesForIntegration, maxValue);
//				tr.setLowerBound(measError.minimumValueForConsideringMeasurementError);
//				tr.setUpperBound(maxValue);
				Matrix marginalHessian = adaptedTr.getIntegralApproximationForMatrixFunction(hessianProvider, indexEffectWithMeasError, false).scalarMultiply(1d/invdensity); // false: it is a variable.
				return marginalHessian;
			}
		}
	}

	protected final int indexEffectWithMeasError;
	protected final GLMMeasErrorDefinition measError;
	
	public GLMWithUniformMeasError(DataSet dataSet, String modelDefinition, GLMMeasErrorDefinition measError, Matrix startingValues) {
		super(dataSet, Type.CLogLog, modelDefinition, startingValues);
		indexEffectWithMeasError = getDataStructure().indexOfThisEffect(measError.effectWithMeasError);
		if (indexEffectWithMeasError == -1) {
			throw new InvalidParameterException("The effect with measurement error " + measError.effectWithMeasError + " is not part of the model definition!");
		} 
		this.measError = measError;
	}

	public GLMWithUniformMeasError(DataSet dataSet, String modelDefinition, GLMMeasErrorDefinition measError) {
		this(dataSet, modelDefinition, measError, null);
	}
	
	@Override
	protected void initializeLinkFunction(Type linkFunctionType) {
		LinkFunction lf = new LinkFunctionWithMeasError();
		individualLLK = new IndividualLogLikelihood(new LikelihoodGLM(lf));
		setCompleteLLK();
	}

}
