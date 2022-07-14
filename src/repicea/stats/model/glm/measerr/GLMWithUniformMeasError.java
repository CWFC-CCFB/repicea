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
import repicea.stats.data.GenericStatisticalDataStructure;
import repicea.stats.integral.TrapezoidalRule;
import repicea.stats.model.CompositeLogLikelihood;
import repicea.stats.model.IndividualLogLikelihood;
import repicea.stats.model.glm.GeneralizedLinearModel;
import repicea.stats.model.glm.LikelihoodGLM;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.stats.model.glm.measerr.GLMWithUniformMeasError.GLMWithUniformMeasErrorDataStructure;

/**
 * A class implementing the generalized linear model with measurement
 * error on one variable.
 * @author Mathieu Fortin - July 2022
 */
public class GLMWithUniformMeasError extends GeneralizedLinearModel<GLMWithUniformMeasErrorDataStructure> {


	static class GLMWithUniformMeasErrorDataStructure extends GenericStatisticalDataStructure {

		public GLMWithUniformMeasErrorDataStructure(DataSet dataSet) {
			super(dataSet);
		}

		Matrix setMeasErrorDefinition(GLMMeasErrorDefinition errDef) {
			Matrix errBounds = new Matrix(matrixX.m_iRows, 2);
			if (errDef.lowerBoundVar != null) {
				if (!dataSet.getFieldNames().contains(errDef.lowerBoundVar)) {
					throw new InvalidParameterException("The lowerBoundVar argument in the measurement error definition is not in the dataset!");
				} else {
					int indexField = dataSet.getIndexOfThisField(errDef.lowerBoundVar);
					for (int i = 0; i < dataSet.getNumberOfObservations(); i++) {
						errBounds.setValueAt(i, 0, (Double) dataSet.getObservations().get(i).getValueAt(indexField));
					}
				}
			}

			String upperBoundFieldName = errDef.upperBoundVar != null ? errDef.upperBoundVar : errDef.effectWithMeasError;
			if (!dataSet.getFieldNames().contains(upperBoundFieldName)) {
				throw new InvalidParameterException("The upperBound argument in the measurement error definition is not in the dataset!");
			} else {
				int indexField = dataSet.getIndexOfThisField(upperBoundFieldName);
				for (int i = 0; i < dataSet.getNumberOfObservations(); i++) {
					double maxValue = (Double) dataSet.getObservations().get(i).getValueAt(indexField);
					if (maxValue < errBounds.getValueAt(i, 0))
						throw new InvalidParameterException("The upper bound argument seems to contain values lower than the lower bound!");
					errBounds.setValueAt(i, 1, maxValue);
				}
			}
			return errBounds;
		}
	}
	
	
	@SuppressWarnings("serial")
	class GLMWithUniformMeasErrorCompositeLogLikelihood extends CompositeLogLikelihood {
		
		public GLMWithUniformMeasErrorCompositeLogLikelihood(IndividualLogLikelihood innerLogLikelihoodFunction, Matrix xValues, Matrix yValues) {
			super(innerLogLikelihoodFunction, xValues, yValues);
		}
		
		@Override
		protected void setValuesInLikelihoodFunction(int index) {
			super.setValuesInLikelihoodFunction(index);
			linkFunction.setBounds(bounds.getValueAt(index, 0), bounds.getValueAt(index, 1));
		}

	}
	
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
		private double lowerBound;
		private double upperBound;
		
//		private AdaptedTrapezoidalRule adaptedTr;
		private TrapezoidalRule adaptedTr;
		
		public LinkFunctionWithMeasError(double resolution) {
			super(Type.CLogLog);
			linkFunctionErrorFreeObs = new LinkFunction(Type.CLogLog, getOriginalFunction());
			gradientProvider = new GradientHessianProvider(linkFunctionErrorFreeObs, true);
			hessianProvider = new GradientHessianProvider(linkFunctionErrorFreeObs, false);
			adaptedTr = new TrapezoidalRule(resolution);
		}
		
		void setBounds(double lowerBound, double upperBound) {
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
		}
		
		private boolean isObservationErrorFree() {
			return getOriginalFunction().getVariableValue(indexEffectWithMeasError) == measError.minimumValueForConsideringMeasurementError;
		}
		
		@Override
		public Double getValue() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getValue();
			} else {
//				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
//				double invdensity = maxValue - measError.minimumValueForConsideringMeasurementError;
//				adaptedTr.setXValuesIntoTheAdaptedTrapezoidalRule(measError.xValuesForIntegration, maxValue);
				double invdensity = upperBound - lowerBound;
				adaptedTr.setLowerBound(lowerBound);
				adaptedTr.setUpperBound(upperBound);
				double marginalProbability = adaptedTr.getIntegralApproximation(linkFunctionErrorFreeObs, indexEffectWithMeasError, false) / invdensity; // false: it is a variable.
				return marginalProbability;
			}
		}

		@Override
		public Matrix getGradient() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getGradient();
			} else {
//				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
//				double invdensity = maxValue - measError.minimumValueForConsideringMeasurementError;
//				adaptedTr.setXValuesIntoTheAdaptedTrapezoidalRule(measError.xValuesForIntegration, maxValue);
				double invdensity = upperBound - lowerBound;
				adaptedTr.setLowerBound(lowerBound);
				adaptedTr.setUpperBound(upperBound);
				Matrix marginalGradient = adaptedTr.getIntegralApproximationForMatrixFunction(gradientProvider, indexEffectWithMeasError, false).scalarMultiply(1d/invdensity); // false: it is a variable.
				return marginalGradient;
			}
		}

		@Override
		public Matrix getHessian() {
			if (isObservationErrorFree()) {
				return linkFunctionErrorFreeObs.getHessian();
			} else {
//				double maxValue = getOriginalFunction().getVariableValue(indexEffectWithMeasError);
//				double invdensity = maxValue - measError.minimumValueForConsideringMeasurementError;
//				adaptedTr.setXValuesIntoTheAdaptedTrapezoidalRule(measError.xValuesForIntegration, maxValue);
				double invdensity = upperBound - lowerBound;
				adaptedTr.setLowerBound(lowerBound);
				adaptedTr.setUpperBound(upperBound);
				Matrix marginalHessian = adaptedTr.getIntegralApproximationForMatrixFunction(hessianProvider, indexEffectWithMeasError, false).scalarMultiply(1d/invdensity); // false: it is a variable.
				return marginalHessian;
			}
		}
	}

	public static double RESOLUTION = 0.1;
	
	protected final int indexEffectWithMeasError;
	protected final GLMMeasErrorDefinition measError;
	protected final Matrix bounds;
	private LinkFunctionWithMeasError linkFunction;
	
	public GLMWithUniformMeasError(DataSet dataSet, String modelDefinition, GLMMeasErrorDefinition measError, Matrix startingValues) {
		super(dataSet, Type.CLogLog, modelDefinition, startingValues);
		indexEffectWithMeasError = getDataStructure().indexOfThisEffect(measError.effectWithMeasError);
		if (indexEffectWithMeasError == -1) {
			throw new InvalidParameterException("The effect with measurement error " + measError.effectWithMeasError + " is not part of the model definition!");
		} 
		this.measError = measError;
		bounds = getDataStructure().setMeasErrorDefinition(this.measError);
	}

	public GLMWithUniformMeasError(DataSet dataSet, String modelDefinition, GLMMeasErrorDefinition measError) {
		this(dataSet, modelDefinition, measError, null);
	}
	
	@Override
	protected void setCompleteLLK() {completeLLK = new GLMWithUniformMeasErrorCompositeLogLikelihood(individualLLK, matrixX, y);}

	@Override
	protected GLMWithUniformMeasErrorDataStructure getDataStructureFromDataSet(DataSet dataSet) {
		return new GLMWithUniformMeasErrorDataStructure(dataSet);
	}

	@Override
	protected void initializeLinkFunction(Type linkFunctionType) {
		linkFunction = new LinkFunctionWithMeasError(RESOLUTION);
		individualLLK = new IndividualLogLikelihood(new LikelihoodGLM(linkFunction));
		setCompleteLLK();
	}

}
