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
import repicea.math.AbstractMathematicalFunctionWrapper;
import repicea.math.InternalMathematicalFunctionWrapper;
import repicea.math.Matrix;
import repicea.math.ParameterBound;
import repicea.math.ProductFunctionWrapper;
import repicea.math.functions.LogGaussianFunction;
import repicea.stats.data.DataSet;
import repicea.stats.data.GenericStatisticalDataStructure;
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.distributions.utility.GaussianUtility;
import repicea.stats.integral.TrapezoidalRule;
import repicea.stats.model.CompositeLogLikelihoodWithExplanatoryVariables;
import repicea.stats.model.IndividualLogLikelihood;
import repicea.stats.model.Likelihood;
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
		}

	}
	
	@SuppressWarnings("serial")
	private static class AlteredGaussianFunction extends AbstractMathematicalFunction {

		private static final int SIGMA2_INDEX = 0;
		private static final int X_INDEX = 0;

		double wValue;
		
		private AlteredGaussianFunction(double variance) {
			if (variance < MINIMUM_ACCEPTABLE_POSITIVE_VALUE) {
				throw new InvalidParameterException("The variance argument must be stricly positive!");
			}
			setParameterValue(SIGMA2_INDEX, variance);
			setVariableValue(X_INDEX, 0d);
			setBounds(SIGMA2_INDEX, new ParameterBound(MINIMUM_ACCEPTABLE_POSITIVE_VALUE, null));	
		}

		@Override
		public void setParameterValue(int index, double value) {
			if (index > 0) {
				throw new InvalidParameterException("The altered Gaussian function only has one parameter!");
			} else {
				if (index == SIGMA2_INDEX && value <= 0) {
					throw new InvalidParameterException("The sigma2 parameter must be strictly positive (ie. > 0)!");
				}
				super.setParameterValue(index, value);
			}
		}

		@Override
		public void setVariableValue(int index, double value) {
			if (index > 0) {
				throw new InvalidParameterException("The altered Gaussian function only has one variable (namely the observation x)!");
			} else {
				super.setVariableValue(index, value);
			}
		}
		
		@Override
		public Double getValue() {
			double mu = getVariableValue(X_INDEX);
			double sigma2 = getParameterValue(SIGMA2_INDEX);
			return GaussianUtility.getProbabilityDensity(wValue, mu, sigma2);
		}

		@Override
		public Matrix getGradient() {
			double mu = getVariableValue(X_INDEX);
			double sigma2 = getParameterValue(SIGMA2_INDEX);
			
			double f = GaussianUtility.getProbabilityDensity(wValue, mu, sigma2);
			
			Matrix gradient = new Matrix(1,1);
			double df_dSigma2 = f * ((wValue-mu) * (wValue-mu)/(2 * sigma2 * sigma2) - 1d / (2 * sigma2));
			gradient.setValueAt(SIGMA2_INDEX, 0, df_dSigma2);

			return gradient;
		}

		@Override
		public Matrix getHessian() {
			double mu = getVariableValue(X_INDEX);
			double sigma2 = getParameterValue(SIGMA2_INDEX);

			double f = GaussianUtility.getProbabilityDensity(wValue, mu, sigma2);

			Matrix gradient = getGradient();
			
			Matrix hessian = new Matrix(1,1);
			double d2f_d2Sigma2 = gradient.getValueAt(SIGMA2_INDEX, 0) * ((wValue-mu) * (wValue-mu)/(2 * sigma2 * sigma2) - 1d / (2* sigma2)) +
					f * (-(wValue-mu) * (wValue-mu)/(sigma2 * sigma2 * sigma2) + 1d / (2 * sigma2 * sigma2));
			hessian.setValueAt(SIGMA2_INDEX, SIGMA2_INDEX, d2f_d2Sigma2);
			return hessian;
		}

	}

	
//	@SuppressWarnings("serial")
//	static class InternalProductFunctionWrapper extends ProductFunctionWrapper implements GaussHermiteQuadratureCompatibleFunction<Double> {
//
//		private final AlteredGaussianFunction agf;
//		
//		InternalProductFunctionWrapper(AlteredGaussianFunction agf, InternalMathematicalFunctionWrapper... wrappedOriginalFunctions) {
//			super(wrappedOriginalFunctions);
//			this.agf = agf;
//		}
//		
//		@Override
//		public double convertFromGaussToOriginal(double x, double mu, int covarianceIndexI, int covarianceIndexJ) {
//			return agf.wValue - Math.sqrt(2 * agf.getParameterValue(AlteredGaussianFunction.SIGMA2_INDEX)) * x;
//		}
//
//		@Override
//		public double getIntegralAdjustment(int dimensions) {
//			return - Math.sqrt(2 * agf.getParameterValue(AlteredGaussianFunction.SIGMA2_INDEX));
//		}
//	}
	
	@SuppressWarnings("serial")
	static class LikelihoodGLMWithNormalClassicalMeasErr extends AbstractMathematicalFunctionWrapper implements Likelihood {

		private final GLMNormalClassicalMeasErrorDefinition measErr;
		private final TrapezoidalRule integrator;
		private final GradientHessianProvider gradientProvider;
		private final GradientHessianProvider hessianProvider;


		public LikelihoodGLMWithNormalClassicalMeasErr(ProductFunctionWrapper pfw, GLMNormalClassicalMeasErrorDefinition measErr) {
			super(pfw);
			this.measErr = measErr;
			integrator = new TrapezoidalRule(measErr.resolution);
			gradientProvider = new GradientHessianProvider(pfw , true) ;
			hessianProvider = new GradientHessianProvider(pfw, false);
		}

		@Override
		public void setYVector(Matrix yVector) {
			measErr.originalModelLikelihood.setYVector(yVector);
		}

		@Override
		public Matrix getYVector() {return null;}

		@Override
		public Matrix getPredictionVector() {return null;}

		@Override
		public ProductFunctionWrapper getOriginalFunction() {
			return (ProductFunctionWrapper) super.getOriginalFunction();
		}
		
		@Override
		public Double getValue() {
			double integralApprox = integrator.getIntegralApproximation(getOriginalFunction(), measErr.indexEffectWithMeasError, false);
			return integralApprox;
		}

		@Override
		public Matrix getGradient() {
			return integrator.getIntegralApproximationForMatrixFunction(gradientProvider, measErr.indexEffectWithMeasError, false);
		}

		@Override
		public Matrix getHessian() {
			return integrator.getIntegralApproximationForMatrixFunction(hessianProvider, measErr.indexEffectWithMeasError, false);
		}

	}
	

	
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
			measErr.setValuesForObservation(index);
			super.setValuesInLikelihoodFunction(index);
		}
	}

	
	private final double resolution;
	private List<Double> wVector;
	
	private LikelihoodGLM originalModelLikelihood;
	private AlteredGaussianFunction errorModelLikelihood;
	private LogGaussianFunction xDistribution;
	private final double startingVariance;
	private LikelihoodGLMWithNormalClassicalMeasErr lk;
	private double maxX;
	
	public GLMNormalClassicalMeasErrorDefinition(String effectWithMeasError, double startingVariance, double resolution) {
		super(MeasurementErrorModel.Classical, effectWithMeasError);
		if (resolution <= 0) {
			throw new InvalidParameterException("The resolution argument must be strictly positive!");
		} else {
			this.resolution = resolution;
		}
		if (startingVariance <= 0d) {
			throw new InvalidParameterException("The startingVariance argument must be strictly positive!");
		} else {
			this.startingVariance = startingVariance; 
		}
	}

	private void setValuesForObservation(int index) {
		errorModelLikelihood.wValue = wVector.get(index);
		double sigma2 = errorModelLikelihood.getParameterValue(AlteredGaussianFunction.SIGMA2_INDEX);
		double p = 1E-16;
		double delta = Math.sqrt(-2 * sigma2 * Math.log(Math.sqrt(2 * Math.PI * sigma2) * p));
		double lowerBound = errorModelLikelihood.wValue - delta;
		double upperBound = errorModelLikelihood.wValue + delta;
		if (lowerBound <= 0) {
			lowerBound = 0.1;
		}
		if (upperBound > maxX) {
			upperBound = maxX;		// TODO FP This supposes that Pr(X) = 0 for X > maxX
		}
		if (upperBound <= 0) {
			throw new InvalidParameterException("The upperbound cannot be null or negative!");
		}
//		double pLowerBound = GaussianUtility.getProbabilityDensity(errorModelLikelihood.wValue, lowerBound, sigma2);
//		double pUpperBound = GaussianUtility.getProbabilityDensity(errorModelLikelihood.wValue, upperBound, sigma2);
		lk.integrator.setLowerBound(lowerBound);
		lk.integrator.setUpperBound(upperBound);
	}

	/**
	 * Approximate the first two central moments of the distribution of X from the sample of w. <br>
	 * <br>
	 * These values are used as starting values.
	 * 
	 * @return an 2-slot array (the first is the mean, the second is the variance)
	 */
	private double[] getLogScaleMuAndVarianceAndMaximumX() {
		double[] muAndSigma2 = new double[3];
		double maxX = Double.MIN_VALUE;
		double sumW = 0d;
		for (Double w : wVector) {
			sumW += Math.log(w);
			maxX = w > maxX ? w : maxX;
		}
		muAndSigma2[0] = sumW / wVector.size();
		muAndSigma2[2] = maxX;
		double sse = 0d;
		for (Double w : wVector) {
			sse += (Math.log(w) - muAndSigma2[0]) * (Math.log(w) - muAndSigma2[0]);
		}
		muAndSigma2[1] = sse / (wVector.size() - 1);
		return muAndSigma2;
	}
	
	@Override
	public void validate(GLMWithMeasurementError glm) {
		super.validate(glm);
		((GLMWithNormalClassicalMeasErrorDataStructure) glm.getDataStructure()).initializeMeasurementErrorDefinition();
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
		originalModelLikelihood = new LikelihoodGLM(glm.getLinkFunction());
		InternalMathematicalFunctionWrapper wrapper1 = new InternalMathematicalFunctionWrapper(originalModelLikelihood, 
				InternalMathematicalFunctionWrapper.produceListFromTo(0, originalModelLikelihood.getNumberOfParameters() - 1),
				InternalMathematicalFunctionWrapper.produceListFromTo(0, originalModelLikelihood.getNumberOfVariables() - 1)); 
		errorModelLikelihood = new AlteredGaussianFunction(startingVariance);
		List<Integer> newVariableIndices = new ArrayList<Integer>(); 
		newVariableIndices.add(indexEffectWithMeasError);
		InternalMathematicalFunctionWrapper wrapper2 = new InternalMathematicalFunctionWrapper(errorModelLikelihood, 
				InternalMathematicalFunctionWrapper.produceListFromTo(originalModelLikelihood.getNumberOfParameters(), originalModelLikelihood.getNumberOfParameters()),
				newVariableIndices);
		double[] approxMuAndSigma2 = getLogScaleMuAndVarianceAndMaximumX();
		maxX = approxMuAndSigma2[2];
		xDistribution = new LogGaussianFunction(approxMuAndSigma2[0], approxMuAndSigma2[1]);
		InternalMathematicalFunctionWrapper wrapper3 = new InternalMathematicalFunctionWrapper(xDistribution, 
				InternalMathematicalFunctionWrapper.produceListFromTo(originalModelLikelihood.getNumberOfParameters() + errorModelLikelihood.getNumberOfParameters(), 
						originalModelLikelihood.getNumberOfParameters()  + errorModelLikelihood.getNumberOfParameters() + xDistribution.getNumberOfParameters() - 1),
				InternalMathematicalFunctionWrapper.produceListFromTo(indexEffectWithMeasError, indexEffectWithMeasError)); 
		ProductFunctionWrapper pfw = new ProductFunctionWrapper(wrapper1, wrapper2, wrapper3);
		lk = new LikelihoodGLMWithNormalClassicalMeasErr(pfw, this);
		return new WrappedIndividualLogLikelihood(lk);
	}

	@Override
	public LinkFunction createLinkFunction(Type linkFunctionType, GLMWithMeasurementError glm) {
		LinkFunction lf = new LinkFunction(linkFunctionType);
		int dimension = glm.getDataStructure().getMatrixX().m_iCols;
		lf.setParameters(new Matrix(dimension, 1));
		lf.setVariables(new Matrix(1, dimension));
		return lf;
	}

	@Override
	public List<String> getAdditionalEffects() {
		List<String> additionalEffects = new ArrayList<String>();
		additionalEffects.add("Variance error model");
		additionalEffects.add("Mean distribution of X");
		additionalEffects.add("Variance distribution of X");
		return additionalEffects;
	}
	

}
