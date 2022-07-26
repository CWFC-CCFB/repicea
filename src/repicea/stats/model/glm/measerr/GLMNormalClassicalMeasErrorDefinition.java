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
import repicea.stats.integral.GaussHermiteQuadrature;
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

	
	
	
	@SuppressWarnings("serial")
	static class LikelihoodGLMWithNormalClassicalMeasErr extends AbstractMathematicalFunctionWrapper implements Likelihood {

		private double w;
		private final GLMNormalClassicalMeasErrorDefinition measErr;
		private final GaussHermiteQuadrature ghq;

		public LikelihoodGLMWithNormalClassicalMeasErr(ProductFunctionWrapper pfw, GLMNormalClassicalMeasErrorDefinition measErr) {
			super(pfw);
			this.measErr = measErr;
		}

		@Override
		public void setYVector(Matrix yVector) {
			measErr.originalModelLikelihood.setYVector(yVector);
			measErr.errorModelLikelihood.wValue = w;
		}

		@Override
		public Matrix getYVector() {return null;}

		@Override
		public Matrix getPredictionVector() {return null;}

		
		public void setVariableValue(int index, double value) {
			if (index == measErr.indexEffectWithMeasError) {	// here we rescale the value for the Gauss-Hermite quadrature
				super.setVariableValue(index, w + getMinusSquareRootOfTwiceSigma2() * value);
			} else {
				super.setVariableValue(index, value);
			}
		}
		
		private double getMinusSquareRootOfTwiceSigma2() {
			return -Math.sqrt(2 * measErr.errorModelLikelihood.getParameterValue(measErr.errorModelLikelihood.SIGMA2_INDEX));
		}
		
		
		@Override
		public Double getValue() {
			return getOriginalFunction().getValue() * getMinusSquareRootOfTwiceSigma2();
		}

		@Override
		public Matrix getGradient() {
			return getOriginalFunction().getGradient().scalarMultiply(getMinusSquareRootOfTwiceSigma2());
		}

		@Override
		public Matrix getHessian() {
			return getOriginalFunction().getGradient().scalarMultiply(getMinusSquareRootOfTwiceSigma2());
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
			super.setValuesInLikelihoodFunction(index);
			measErr.setValuesForObservation(index);
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
		lk.w = wVector.get(index);
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
		List<Integer> newVariableIndices = InternalMathematicalFunctionWrapper.produceListFromTo(originalModelLikelihood.getNumberOfVariables(), originalModelLikelihood.getNumberOfVariables()); 
		newVariableIndices.add(indexEffectWithMeasError);
		InternalMathematicalFunctionWrapper wrapper2 = new InternalMathematicalFunctionWrapper(errorModelLikelihood, 
				InternalMathematicalFunctionWrapper.produceListFromTo(originalModelLikelihood.getNumberOfParameters(), originalModelLikelihood.getNumberOfParameters()),
				newVariableIndices);
		xDistribution = new LogGaussianFunction();
		InternalMathematicalFunctionWrapper wrapper3 = new InternalMathematicalFunctionWrapper(xDistribution, 
				InternalMathematicalFunctionWrapper.produceListFromTo(originalModelLikelihood.getNumberOfParameters() + errorModelLikelihood.getNumberOfParameters(), 
						originalModelLikelihood.getNumberOfParameters()  + errorModelLikelihood.getNumberOfParameters() + xDistribution.getNumberOfParameters() - 1),
				InternalMathematicalFunctionWrapper.produceListFromTo(indexEffectWithMeasError, indexEffectWithMeasError)); 
		ProductFunctionWrapper pfw = new ProductFunctionWrapper(wrapper1, wrapper2, wrapper3);
		lk = new LikelihoodGLMWithNormalClassicalMeasErr(glm.getLinkFunction(), this);
		return new WrappedIndividualLogLikelihood(lk);
	}

	@Override
	public LinkFunction createLinkFunction(Type linkFunctionType) {
		return null;		// use the default method instead
	}

}
