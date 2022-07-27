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
import repicea.math.ProductFunctionWrapper;
import repicea.math.functions.LogGaussianFunction;
import repicea.stats.data.DataSet;
import repicea.stats.data.GenericStatisticalDataStructure;
import repicea.stats.data.StatisticalDataStructure;
import repicea.stats.distributions.utility.GaussianUtility;
//import repicea.stats.integral.GaussHermiteQuadrature.GaussHermiteQuadratureCompatibleFunction;
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
		
		private AlteredGaussianFunction() {
			setParameterValue(SIGMA2_INDEX, 1d);
			setVariableValue(X_INDEX, 0d);
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
			integrator.setLowerBound(0.1);
			integrator.setUpperBound(100);
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
			return integrator.getIntegralApproximation(getOriginalFunction(), measErr.indexEffectWithMeasError, false);
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
	
	private LikelihoodGLMWithNormalClassicalMeasErr lk;
	
	public GLMNormalClassicalMeasErrorDefinition(String effectWithMeasError, double resolution) {
		super(MeasurementErrorModel.Classical, effectWithMeasError);
		if (resolution <= 0) {
			throw new InvalidParameterException("The resolution argument must be strictly positive!");
		} else {
			this.resolution = resolution;
		}
	}

	private void setValuesForObservation(int index) {
		errorModelLikelihood.wValue = wVector.get(index);
	}

	/**
	 * Approximate the first two central moments of the distribution of x from the sample of w.
	 * @return an 2-slot array (the first is the mean, the second is the variance)
	 */
	private double[] getLogScaleMuAndVariance() {
		double[] muAndSigma2 = new double[2];
		double sumW = 0d;
		for (Double w : wVector) {
			sumW += Math.log(w);
		}
		muAndSigma2[0] = sumW / wVector.size();
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
		errorModelLikelihood = new AlteredGaussianFunction();
		List<Integer> newVariableIndices = new ArrayList<Integer>(); 
		newVariableIndices.add(indexEffectWithMeasError);
		InternalMathematicalFunctionWrapper wrapper2 = new InternalMathematicalFunctionWrapper(errorModelLikelihood, 
				InternalMathematicalFunctionWrapper.produceListFromTo(originalModelLikelihood.getNumberOfParameters(), originalModelLikelihood.getNumberOfParameters()),
				newVariableIndices);
		double[] approxMuAndSigma2 = getLogScaleMuAndVariance();
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
