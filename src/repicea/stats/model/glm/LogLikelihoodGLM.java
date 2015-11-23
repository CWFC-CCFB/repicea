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
package repicea.stats.model.glm;


/**
 * This class defines the log-likelihood of a generalized linear model.
 * @author Mathieu Fortin - October 2011
 */
@Deprecated
class LogLikelihoodGLM {
//	extends AbstractMathematicalFunction<LogLikelihoodGLM.LLKGLMParameter, 
//}
//																	LinkFunction, 
//																	LogLikelihoodGLM.LLKGLMVariable,
//																	Double> implements LogLikelihood, Serializable {

	
//	public static enum LLKGLMParameter {Predicted}
//	public static enum LLKGLMVariable {Response}
//	
//	
//
//	
//	static class LikelihoodGLM extends AbstractMathematicalFunction<LogLikelihoodGLM.LLKGLMParameter, 
//																	LinkFunction, 
//																	LogLikelihoodGLM.LLKGLMVariable,
//																	Double> implements Likelihood, Serializable {
//
//		private LogLikelihood owner;
//		
//		protected LikelihoodGLM(LogLikelihoodGLM logLikelihood) {
//			super();
//			this.owner = logLikelihood;
//			this.parameterNames = logLikelihood.parameterNames;
//			this.parameterValues = logLikelihood.parameterValues;
//			this.variableNames = logLikelihood.variableNames;
//			this.variableValues = logLikelihood.variableValues;
//		}
//		
//		/**
//		 * Method for optimality. Avoid checking the index before returning the prediction.
//		 * Replaces getParameterValue(LLKGLMParameter.Predicted).
//		 * @return a LinkFunction instance
//		 */
//		private LinkFunction getPredicted() {
//			return parameterValues.get(0);
//		}
//		
//		/**
//		 * Method for optimality. Avoid checking the index before returning the observed value.
//		 * Replaces getVariableValue(LLKGLMVariable.Response)
//		 * @return a double
//		 */
//		private double getObserved() {
//			return variableValues.get(0);
//		}
//		
//		@Override
//		public Double getValue() {return Math.exp(owner.getValue());}
//		
//		@Override
//		public Matrix getGradient() {
//			Type linkFunctionType = ((LogLikelihoodGLM) owner).getLinkFunctionType();
//			if (linkFunctionType == Type.Logit) {
//				LinkFunction parameter = getPredicted();
//				double predicted = parameter.getValue();
//				double observed = getObserved();
//				
//				double firstDerivative = (observed / predicted - 1) * Math.pow(predicted, observed) * Math.pow(1- predicted, - observed);
//				Matrix output = parameter.getGradient();
//				MatrixUtility.scalarMultiply(output, firstDerivative);
//				return output;
//			}
//			return null;
//		}
//
//		@Override
//		public Matrix getHessian() {
//			Type linkFunctionType = ((LogLikelihoodGLM) owner).getLinkFunctionType();
//			if (linkFunctionType == Type.Logit) {
//				LinkFunction linkFunction = getPredicted();
//				double predicted = linkFunction.getValue();
//				double observed = getObserved();
//				Matrix gradient = linkFunction.getGradient();
//				Matrix gradientProduct = gradient.multiply(gradient.transpose());
//				Matrix hessian = linkFunction.getHessian();
//				
//				double expressionTmp = observed / predicted - 1;
//				
//				
//				double multiplicator = Math.pow(predicted, observed) * Math.pow(1 - predicted, - observed);
//				Matrix output = gradientProduct.scalarMultiply(- observed / (predicted * predicted));
//				Matrix term2 = gradientProduct.scalarMultiply(expressionTmp * observed / predicted);
//				Matrix term3 = gradientProduct.scalarMultiply(expressionTmp * - observed / (1 - predicted));
//				MatrixUtility.scalarMultiply(hessian, expressionTmp);
//				Matrix term4 = hessian;
//				
//				MatrixUtility.add(output, term2);
//				MatrixUtility.add(output, term3);
//				MatrixUtility.add(output, term4);
//				MatrixUtility.scalarMultiply(output, multiplicator);
//				
//				return output;
//			}
//			return null;
//		}
//		
//	}
//
//	
//	protected Likelihood originalLikelihood;
//
//	/**
//	 * Constructor.
//	 */
//	protected LogLikelihoodGLM() {
//		super();
//		this.originalLikelihood = new LikelihoodGLM(this);
//	}
//	
//	/**
//	 * Method for optimality. Avoid checking the index before returning the prediction.
//	 * Replaces getParameterValue(LLKGLMParameter.Predicted).
//	 * @return a LinkFunction instance
//	 */
//	public LinkFunction getPredicted() {
//		return parameterValues.get(0);
//	}
//	
//	/**
//	 * Method for optimality. Avoid checking the index before returning the observed value.
//	 * Replaces getVariableValue(LLKGLMVariable.Response)
//	 * @return a double
//	 */
//	public double getObserved() {
//		return variableValues.get(0);
//	}
//
//	
//	@Override
//	public Double getValue() {
//		Type linkFunctionType = getLinkFunctionType();
//		if (linkFunctionType == Type.Logit) {
//			double predicted = getPredicted().getValue();
//			double observed = getObserved();
//			return observed * Math.log(predicted) + (1 - observed) * Math.log(1 - predicted);
//		}
//		return -1d;
//	}
//
//	private Type getLinkFunctionType() {
//		return getPredicted().getType();
//	}
//	
//
//	@Override
//	public Matrix getGradient() {
//		Type linkFunctionType = getLinkFunctionType();
//		if (linkFunctionType == Type.Logit) {
//			LinkFunction parameter = getPredicted();
//			double predicted = parameter.getValue();
//			double observed = getObserved();
//			
//			double firstDerivative = observed / predicted - (1 - observed) / (1 - predicted);
//			Matrix output = parameter.getGradient();
//			MatrixUtility.scalarMultiply(output, firstDerivative);
//			return output;
//		}
//		return null;
//	}
//
//	@Override
//	public Matrix getHessian() {
//		Type linkFunctionType = getLinkFunctionType();
//		if (linkFunctionType == Type.Logit) {
//			LinkFunction linkFunction = getPredicted();
//			double predicted = linkFunction.getValue();
//			double observed = getObserved();
//			Matrix gradient = linkFunction.getGradient();
//			Matrix gradientProduct = gradient.multiply(gradient.transpose());
//			Matrix hessian = linkFunction.getHessian();
//			
//			double firstDerivative = observed / predicted - (1 - observed) / (1 - predicted);
//			double secondDerivative = - observed / (predicted * predicted) - (1 - observed) / ((1 - predicted) * (1 - predicted));
//			
//			MatrixUtility.scalarMultiply(gradientProduct, secondDerivative);
//			Matrix output = gradientProduct;
//			
//			MatrixUtility.scalarMultiply(hessian, firstDerivative);
//			Matrix secondTerm = hessian;
//			
//			MatrixUtility.add(output, secondTerm);
//			
//			return output;
//		}
//		return null;
//	}
//	
//	
//	@Override
//	public Likelihood getLikelihoodFunction() {return originalLikelihood;}

	
}
