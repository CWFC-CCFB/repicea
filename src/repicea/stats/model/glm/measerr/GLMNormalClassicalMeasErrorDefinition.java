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

import repicea.math.AbstractMathematicalFunctionWrapper;
import repicea.math.Matrix;
import repicea.stats.data.DataSet;
import repicea.stats.data.GenericStatisticalDataStructure;
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.distributions.TruncatedGaussianDistribution;
import repicea.stats.distributions.utility.GaussianUtility;
import repicea.stats.integral.TrapezoidalRule;
import repicea.stats.model.CompositeLogLikelihoodWithExplanatoryVariables;
import repicea.stats.model.IndividualLogLikelihood;
import repicea.stats.model.WrappedIndividualLogLikelihood;
import repicea.stats.model.glm.LikelihoodGLM;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;

public class GLMNormalClassicalMeasErrorDefinition extends AbstractGLMMeasErrorDefinition {

	private static class GLMWithNormalClassicalMeasErrorDataStructure extends GenericStatisticalDataStructure {

		private final GLMNormalClassicalMeasErrorDefinition measErr;
		
		private GLMWithNormalClassicalMeasErrorDataStructure(DataSet dataSet, GLMNormalClassicalMeasErrorDefinition measErr) {
			super(dataSet);
			this.measErr = measErr;
		}
		
		void initializeMeasurementErrorDefinition() {
			measErr.wVector = new ArrayList<Double>(); 
			// populating the wVector
			int indexField = dataSet.getIndexOfThisField(measErr.effectWithMeasError);
			for (int i = 0; i < dataSet.getNumberOfObservations(); i++) {
				measErr.wVector.add((Double) dataSet.getObservations().get(i).getValueAt(indexField));
			}

			measErr.varianceVector = new ArrayList<Double>();
			// populating the varianceVector
			indexField = dataSet.getIndexOfThisField(measErr.varianceFieldName);
			if (indexField == -1) {
				throw new InvalidParameterException("The field " + measErr.varianceFieldName + " containing the variances is not in the dataset!");
			}
			for (int i = 0; i < dataSet.getNumberOfObservations(); i++) {
				measErr.varianceVector.add((Double) dataSet.getObservations().get(i).getValueAt(indexField));
			}
		}
		

	}
	
	@SuppressWarnings("serial")
	static class LikelihoodGLMWithNormalClassicalMeasErr extends LikelihoodGLM {

		class ConditionalLikelihood extends AbstractMathematicalFunctionWrapper {

			public ConditionalLikelihood(LikelihoodGLM originalFunction) {
				super(originalFunction);
			}

			@Override
			public Double getValue() {
				double x = getOriginalFunction().getVariableValue(measErr.indexEffectWithMeasError);
				double w_density = GaussianUtility.getProbabilityDensity(w, x, variance);
				return getOriginalFunction().getValue() * w_density;
			}

			@Override
			public Matrix getGradient() {
				double x = getOriginalFunction().getVariableValue(measErr.indexEffectWithMeasError);
				return getOriginalFunction().getGradient().scalarMultiply(GaussianUtility.getProbabilityDensity(w, x, variance));
			}

			@Override
			public Matrix getHessian() {
				double x = getOriginalFunction().getVariableValue(measErr.indexEffectWithMeasError);
				return getOriginalFunction().getHessian().scalarMultiply(GaussianUtility.getProbabilityDensity(w, x, variance));
			}
			
		}
		
		
		
		private double w;
		private double variance;
		private final TrapezoidalRule adaptedTr;
		private final GLMNormalClassicalMeasErrorDefinition measErr;
		private final LikelihoodGLM glmLikelihoodWithoutError;
		private final ConditionalLikelihood conditionalLikelihood;
		private final GradientHessianProvider gradientProvider;
		private final GradientHessianProvider hessianProvider;


		
		public LikelihoodGLMWithNormalClassicalMeasErr(LinkFunction linkFunction, GLMNormalClassicalMeasErrorDefinition measErr) {
			super(linkFunction);
			this.glmLikelihoodWithoutError = new LikelihoodGLM(linkFunction);
			this.conditionalLikelihood = new ConditionalLikelihood(glmLikelihoodWithoutError);
			this.measErr = measErr;
			adaptedTr = new TrapezoidalRule(measErr.resolution);
			gradientProvider = new GradientHessianProvider(conditionalLikelihood, true);
			hessianProvider = new GradientHessianProvider(conditionalLikelihood, false);
		}

		@Override
		public void setYVector(Matrix yVector) {
			this.observedValues = yVector;
			glmLikelihoodWithoutError.setYVector(yVector);
		}


		
		private double[] getInitialization() {
			double[] output = new double[3];
			double std = Math.sqrt(variance);
			double lowerBound = w - 3*std;
			output[0] = lowerBound < 0 ? 0 : lowerBound;
			output[1] = w + 3*std;
			output[2] = GaussianUtility.getCumulativeProbability(3) - GaussianUtility.getCumulativeProbability((output[0] - w)/std);
			return output;
		}
	
	

		
		@Override
		public Double getValue() {
			double[] v = getInitialization();
			double invDensity = v[1] - v[0];
			adaptedTr.setLowerBound(v[0]);
			adaptedTr.setUpperBound(v[1]);
			double conditionalProbability = this.glmLikelihoodWithoutError.getValue();
			double marginalProbability = adaptedTr.getIntegralApproximation(conditionalLikelihood, measErr.indexEffectWithMeasError, false) / (v[2] * invDensity); // false: it is a variable.
			return marginalProbability;
		}

		public Matrix getGradient() {
			double[] v = getInitialization();
			double invDensity = v[1] - v[0];
			adaptedTr.setLowerBound(v[0]);
			adaptedTr.setUpperBound(v[1]);
			Matrix marginalGradient = adaptedTr.getIntegralApproximationForMatrixFunction(gradientProvider, measErr.indexEffectWithMeasError, false).scalarMultiply(1d/(v[2] * invDensity)); // false: it is a variable.
			return marginalGradient;
		}

		@Override
		public Matrix getHessian() {
			double[] v = getInitialization();
			double invDensity = v[1] - v[0];
			adaptedTr.setLowerBound(v[0]);
			adaptedTr.setUpperBound(v[1]);
			Matrix marginalHessian = adaptedTr.getIntegralApproximationForMatrixFunction(hessianProvider, measErr.indexEffectWithMeasError, false).scalarMultiply(1d/(v[2] * invDensity)); // false: it is a variable.
			return marginalHessian;
		}

		
	}
	
//	@SuppressWarnings("serial")
//	static class LinkFunctionWithMeasError extends LinkFunction {
//
////		class InternalLinkFunction extends LinkFunction {
////			
////			public InternalLinkFunction(Type type, MathematicalFunction originalFunction) {
////				super(type, originalFunction);
////			}
////		
////			@Override
////			public Double getValue() {
////				double x = getOriginalFunction().getVariableValue(measErr.indexEffectWithMeasError);
////				double w_density = GaussianUtility.getProbabilityDensity(w, x, variance);
////				return super.getValue() * w_density;
////			}
////
////			@Override
////			public Matrix getGradient() {
////				double x = getOriginalFunction().getVariableValue(measErr.indexEffectWithMeasError);
////				return super.getGradient().scalarMultiply(GaussianUtility.getProbabilityDensity(w, x, variance));
////			}
////
////			@Override
////			public Matrix getHessian() {
////				double x = getOriginalFunction().getVariableValue(measErr.indexEffectWithMeasError);
////				return super.getHessian().scalarMultiply(GaussianUtility.getProbabilityDensity(w, x, variance));
////			}
////		}
//		
//		private final GradientHessianProvider gradientProvider;
//		private final GradientHessianProvider hessianProvider;
//		private double w;
//		private double variance;
//		private final InternalLinkFunction conditionOnXLinkFunctionErrorFreeObs;
//		private final TrapezoidalRule adaptedTr;
//		private final GLMNormalClassicalMeasErrorDefinition measErr;
//		private LinkFunction noErrorLinkFunction;
//		
//		public LinkFunctionWithMeasError(Type linkFunctionType, GLMNormalClassicalMeasErrorDefinition measErr) {
//			super(linkFunctionType);
//			noErrorLinkFunction = new LinkFunction(linkFunctionType, getOriginalFunction());
//			conditionOnXLinkFunctionErrorFreeObs = new InternalLinkFunction(linkFunctionType, getOriginalFunction());
//			gradientProvider = new GradientHessianProvider(conditionOnXLinkFunctionErrorFreeObs, true);
//			hessianProvider = new GradientHessianProvider(conditionOnXLinkFunctionErrorFreeObs, false);
//			adaptedTr = new TrapezoidalRule(measErr.resolution);
//			this.measErr = measErr;
//			
//		}
//
//		private double[] getInitialization() {
//			double[] output = new double[3];
//			double std = Math.sqrt(variance);
//			double lowerBound = w - 3*std;
//			output[0] = lowerBound < 0 ? 0 : lowerBound;
//			output[1] = w + 3*std;
//			output[2] = GaussianUtility.getCumulativeProbability(3) - GaussianUtility.getCumulativeProbability((output[0] - w)/std);
//			return output;
//		}
//		
//		
//		@Override
//		public Double getValue() {
//			double[] v = getInitialization();
////			double invDensity = v[1] - v[0];
//			adaptedTr.setLowerBound(v[0]);
//			adaptedTr.setUpperBound(v[1]);
//			double conditionalProbability = this.noErrorLinkFunction.getValue();
//			double marginalProbability = adaptedTr.getIntegralApproximation(conditionOnXLinkFunctionErrorFreeObs, measErr.indexEffectWithMeasError, false) / v[2]; // false: it is a variable.
//			return marginalProbability;
//		}
//
//		@Override
//		public Matrix getGradient() {
//			double[] v = getInitialization();
////			double invdensity = v[1] - v[0];
//			adaptedTr.setLowerBound(v[0]);
//			adaptedTr.setUpperBound(v[1]);
//			Matrix marginalGradient = adaptedTr.getIntegralApproximationForMatrixFunction(gradientProvider, measErr.indexEffectWithMeasError, false).scalarMultiply(1d/v[2]); // false: it is a variable.
//			return marginalGradient;
//		}
//
//		@Override
//		public Matrix getHessian() {
//			double[] v = getInitialization();
////			double invdensity = v[1] - v[0];
//			adaptedTr.setLowerBound(v[0]);
//			adaptedTr.setUpperBound(v[1]);
//			Matrix marginalHessian = adaptedTr.getIntegralApproximationForMatrixFunction(hessianProvider, measErr.indexEffectWithMeasError, false).scalarMultiply(1d/v[2]); // false: it is a variable.
//			return marginalHessian;
//		}
//	}

	
	@SuppressWarnings("serial")
	static class GLMWithNormalClassicalMeasErrorCompositeLogLikelihood extends CompositeLogLikelihoodWithExplanatoryVariables {
		
		private final GLMNormalClassicalMeasErrorDefinition measErr;
		
		public GLMWithNormalClassicalMeasErrorCompositeLogLikelihood(IndividualLogLikelihood innerLogLikelihoodFunction, 
				Matrix xValues, 
				Matrix yValues, 
				GLMNormalClassicalMeasErrorDefinition measErr) {
			super(innerLogLikelihoodFunction, xValues, yValues);
			this.measErr = measErr;
		}
		
		@Override
		protected void setValuesInLikelihoodFunction(int index) {
			super.setValuesInLikelihoodFunction(index);
			measErr.setValuesForObservation(index);
//			lf.variance = measErr.varianceVector.getValueAt(index, 0);
//			lf.w = measErr.wVector.getValueAt(index, 0);
		}
	}

	
	private final double resolution;
	private List<Double> varianceVector;
	private List<Double> wVector;
	private final String varianceFieldName;
	private LikelihoodGLMWithNormalClassicalMeasErr lk;
	private TruncatedGaussianDistribution truncGaussDist;
	
	public GLMNormalClassicalMeasErrorDefinition(String effectWithMeasError, String varianceFieldName, double resolution) {
		super(MeasurementErrorModel.Classical, effectWithMeasError);
		if (varianceFieldName == null || varianceFieldName.isEmpty()) {
			throw new InvalidParameterException("The varianceFieldName argument cannot be null or empty!");
		} else {
			this.varianceFieldName = varianceFieldName;
		}
		if (resolution <= 0) {
			throw new InvalidParameterException("The resolution argument must be strictly positive!");
		} else {
			this.resolution = resolution;
		}
	}

	private void setValuesForObservation(int index) {
		lk.w = wVector.get(index);
		lk.variance = varianceVector.get(index);
	}

	@Override
	public void validate(GLMWithMeasurementError glm) {
		super.validate(glm);
		((GLMWithNormalClassicalMeasErrorDataStructure) glm.getDataStructure()).initializeMeasurementErrorDefinition();
		double mu = 0d;
		double min = Double.MAX_VALUE;
		for (Double d : wVector) {
			mu += d;
			min = d < min ? d : min;
		}
		mu /= wVector.size();
		double sigma2 = 0;
		for (Double d : wVector) {
			sigma2 += (d - mu) * (d - mu);
		}
		sigma2 /= wVector.size() - 1;
		truncGaussDist = new TruncatedGaussianDistribution(mu, sigma2);
		
	}

	@Override
	public CompositeLogLikelihoodWithExplanatoryVariables createCompositeLikelihoodFromModel(GLMWithMeasurementError glm) {
		return new GLMWithNormalClassicalMeasErrorCompositeLogLikelihood(glm.getIndividualLogLikelihood(), 
				glm.getMatrixX(), 
				glm.getVectorY(), 
				this);
	}

	@Override
	public StatisticalDataStructure createDataStructureFromDataSet(DataSet dataSet) {
		return new GLMWithNormalClassicalMeasErrorDataStructure(dataSet, this);
	}

	@Override
	public IndividualLogLikelihood createIndividualLogLikelihoodFromModel(GLMWithMeasurementError glm) {
		lk = new LikelihoodGLMWithNormalClassicalMeasErr(glm.getLinkFunction(), this);
		return new WrappedIndividualLogLikelihood(lk);
	}

	@Override
	public LinkFunction createLinkFunction(Type linkFunctionType) {
//		return new LinkFunctionWithMeasError(linkFunctionType, this);
		return null;		// use the default method instead
	}

}
