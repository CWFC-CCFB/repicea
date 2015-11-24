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
package repicea.stats.model.glm.copula;

import java.security.InvalidParameterException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.math.MatrixUtility;
import repicea.stats.data.DataBlock;
import repicea.stats.data.DataSet;
import repicea.stats.data.GenericHierarchicalSpatialDataStructure;
import repicea.stats.data.HierarchicalStatisticalDataStructure;
import repicea.stats.data.StatisticalDataException;
import repicea.stats.estimates.CorrelationEstimate;
import repicea.stats.model.glm.GeneralizedLinearModel;

/**
 * The FGMCopulaGLModel class implements a Farlie-Gumbel-Morgenstein (FGM) copula for binary outcomes. The method is described in 
 * Bhat and Sener 2009.
 * @see <a href=http://www.springerlink.com/content/t33474476r231w60/> 
 * Bhat, C.R., and Sener, I. N. 2009. A copula-based closed form binary logit choice model for accomodating spatial correlation across observational units. Journal of Geographical Systems 11(3): 243-272.
 * </a>
 * @author Mathieu Fortin - June 2011
 */
public class FGMCopulaGLModel extends GeneralizedLinearModel {
	
	private final static double VERY_SMALL = 1E-8;

	@SuppressWarnings("rawtypes")
	protected static class LikelihoodValue implements Comparable {

		private double llk;
		private Matrix beta;
		
		protected LikelihoodValue(Matrix beta, double llk) {
			this.beta = beta.getDeepClone();
			this.llk = llk;
		}
		
		@Override
		public int compareTo(Object arg0) {
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
	
	
	protected static class OverallLogLikelihood extends GeneralizedLinearModel.OverallLogLikelihood {

		protected double llk;
		protected boolean llkUptoDate;
		
		protected Map<List<Integer>, Double> additionalLlkTerm;
		protected boolean additionalLlkTermUptoDate;
		
		protected Matrix gradientVector;
		protected boolean gradientVectorUptoDate;
		
		protected Map<List<Integer>, Matrix> additionalGradients;
		protected boolean additionalGradientTermUptoDate;
		
		protected Matrix hessianMatrix;
		protected boolean hessianMatrixUptoDate;
		
		protected Map<List<Integer>, Matrix> additionalHessians;
		protected boolean additionalHessianTermUptoDate;

		
		protected OverallLogLikelihood(FGMCopulaGLModel copulaGLModel) {
			super(copulaGLModel);
		}
				
		protected void init() {
			llkUptoDate = false;
			additionalLlkTermUptoDate = false;
			gradientVectorUptoDate = false;
			additionalGradientTermUptoDate = false;
			hessianMatrixUptoDate = false;
			additionalHessianTermUptoDate = false;
		}
		
		@Override
		protected FGMCopulaGLModel getModel() {
			return (FGMCopulaGLModel) super.getModel(); 
		}

		@Override
		public Matrix getGradient() {
			if (!gradientVectorUptoDate) {
				int numberParameters = getModel().getParameters().getNumberOfElements();
				Matrix gradient = new Matrix(numberParameters, 1);
				gradient.setSubMatrix(super.getGradient(),0,0);		// get the gradient under the assumption of independence
				
				for (Matrix additionalGradient : getAdditionalGradients().values()) {
//					gradient = gradient.add(additionalGradient);			// get the additional part of the gradient on both the beta vector and the copula parameters
					MatrixUtility.add(gradient, additionalGradient);			// get the additional part of the gradient on both the beta vector and the copula parameters
				}
				
				gradientVector = gradient;
				gradientVectorUptoDate = true;
			}
			return gradientVector;
		}

		@Override
		public Matrix getHessian() {
			if (!hessianMatrixUptoDate) {
				int numberParameters = getModel().getParameters().getNumberOfElements();
				Matrix hessian = new Matrix(numberParameters, numberParameters);
				hessian.setSubMatrix(super.getHessian(), 0, 0); 	// get the hessian under the assumption of independence
				
				for (Matrix additionalHessian : getAdditionalHessians().values()) {
//					hessian = hessian.add(additionalHessian);
					MatrixUtility.add(hessian, additionalHessian);
				}
				
				hessianMatrix = hessian;
				hessianMatrixUptoDate = true;
			}
			return hessianMatrix;
		}

		@Override
		public Double getValue() {
			if (!llkUptoDate) {
				double logLikelihood = super.getValue();
				for (Double additionalTerm : getAdditionalLikelihoodTerm().values()) {
					logLikelihood += Math.log(additionalTerm);
				}
				llk = logLikelihood;
				llkUptoDate = true;
			}
			return llk;
		}
		
		
		private Map<List<Integer>, Double> getAdditionalLikelihoodTerm() {
			Map<List<Integer>, Double> results = new HashMap<List<Integer>, Double>();
			
			if (!additionalLlkTermUptoDate) {
				
				int indexFirstObservation;
				double likelihoodFirst;
				double observedFirst;
				
				int indexSecondObservation;
				double likelihoodSecond;
				double observedSecond;
				
				double sumObserved;
				double multiplyingFactor;
				
				Map<String, DataBlock> map = getModel().getDataStructure().getHierarchicalStructure();
				for (DataBlock db : map.values()) {
					List<Integer> index = db.getIndices();
					double additionalTerm = 1d;

					for (int i = 0; i < index.size() - 1; i++) {
						indexFirstObservation = index.get(i);
						getModel().setObservationInLogLikelihoodFunction(indexFirstObservation);
						likelihoodFirst = Math.exp(getModel().individualLLK.getValue());
						observedFirst = getModel().individualLLK.getObserved();
						for (int j = i + 1; j < index.size(); j++) {
							indexSecondObservation = index.get(j);
							getModel().setObservationInLogLikelihoodFunction(indexSecondObservation);
							likelihoodSecond = Math.exp(getModel().individualLLK.getValue());
							observedSecond = getModel().individualLLK.getObserved();

							sumObserved = observedFirst + observedSecond;

							multiplyingFactor = 1d;
							if (Math.abs(sumObserved - 1) < VERY_SMALL) {
								multiplyingFactor = -1d;
							}

							getModel().copula.setX(indexFirstObservation, indexSecondObservation);
							double copulaValue = getModel().copula.getValue();

							additionalTerm += multiplyingFactor * copulaValue * (1 - likelihoodFirst) * (1 - likelihoodSecond);
						}
					}
					results.put(index, additionalTerm);
				}
				additionalLlkTerm = results;
				additionalLlkTermUptoDate = true;
			}
			return additionalLlkTerm;
		}
		
		
		private Map<List<Integer>, Matrix> getAdditionalGradients() {
			if (!additionalGradientTermUptoDate) {
				Map<List<Integer>, Matrix> additionalGradients = new HashMap<List<Integer>, Matrix>();
				
				int indexFirstObservation;
				double likelihoodFirst;
				Matrix du_dbetaFirst;
				double observedFirst;
				
				int indexSecondObservation;
				double likelihoodSecond;
				Matrix du_dbetaSecond;
				double observedSecond;
				
				double sumObserved;
				double multiplyingFactor;

				Matrix tmp;

				Map<String, DataBlock> map = getModel().getDataStructure().getHierarchicalStructure();

				for (DataBlock db : map.values()) {
					List<Integer> index = db.getIndices();
					Matrix additionalGradient = new Matrix(getModel().getParameters().m_iRows,1);
					double inverseAdditionalLikelihoodTerm = 1d / getAdditionalLikelihoodTerm().get(index);			

					for (int i = 0; i < index.size() - 1; i++) {

						indexFirstObservation = index.get(i);
						getModel().setObservationInLogLikelihoodFunction(indexFirstObservation);
						likelihoodFirst = Math.exp(getModel().individualLLK.getValue());
						observedFirst = getModel().individualLLK.getObserved();
						du_dbetaFirst = getModel().individualLLK.getLikelihoodFunction().getGradient();

						for (int j = i + 1; j < index.size(); j++) {

							indexSecondObservation = index.get(j);
							getModel().setObservationInLogLikelihoodFunction(indexSecondObservation);
							likelihoodSecond = Math.exp(getModel().individualLLK.getValue());
							observedSecond = getModel().individualLLK.getObserved();
							du_dbetaSecond = getModel().individualLLK.getLikelihoodFunction().getGradient();

							sumObserved = observedFirst + observedSecond;

							multiplyingFactor = 1d;
							if (Math.abs(sumObserved - 1) < VERY_SMALL) {
								multiplyingFactor = -1d;
							}

							getModel().copula.setX(indexFirstObservation, indexSecondObservation);
							double copulaValue = getModel().copula.getValue();

							Matrix expansion1 = du_dbetaFirst.scalarMultiply((1 - likelihoodSecond) * -1d).add(
									du_dbetaSecond.scalarMultiply((1 - likelihoodFirst) * -1d)).scalarMultiply(copulaValue * multiplyingFactor * inverseAdditionalLikelihoodTerm);

							Matrix expansion2 = getModel().copula.getGradient().scalarMultiply((1 - likelihoodFirst) * 
									(1 - likelihoodSecond) * 
									multiplyingFactor * 
									inverseAdditionalLikelihoodTerm);

							tmp = expansion1.matrixStack(expansion2, true);

							MatrixUtility.add(additionalGradient, tmp);

						}
					}

					additionalGradients.put(index, additionalGradient);

				}

				this.additionalGradients = additionalGradients;
				additionalGradientTermUptoDate = true;
			}
			
			return additionalGradients;
		}

		
		private Map<List<Integer>,Matrix> getAdditionalHessians() {
			if (!additionalHessianTermUptoDate) {
				Map<List<Integer>,Matrix> additionalHessians = new HashMap<List<Integer>, Matrix>();
				
				Matrix additionalGradient;
					
				int indexFirstObservation;
				double likelihoodFirst;
				Matrix du_dbetaFirst;
				Matrix d2u_d2betaFirst;
				double observedFirst;

				int indexSecondObservation;
				double likelihoodSecond;
				Matrix du_dbetaSecond;
				Matrix d2u_d2betaSecond;
				double observedSecond;

				double sumObserved;
				double multiplyingFactor;

				Matrix tmp;

				Map<String, DataBlock> map = getModel().getDataStructure().getHierarchicalStructure();
				for (DataBlock db : map.values()) {
					List<Integer> index = db.getIndices();
					Matrix additionalHessian = new Matrix(getModel().getParameters().m_iRows, getModel().getParameters().m_iRows);

					double inverseAdditionalLikelihoodTerm = 1d / getAdditionalLikelihoodTerm().get(index);		
					additionalGradient = additionalGradients.get(index);

					additionalHessian.setSubMatrix(additionalGradient.multiply(additionalGradient.transpose()).scalarMultiply(-1d), 0, 0);	// first term corresponding to -1 * d1 ^ 2

					for (int i = 0; i < index.size() - 1; i++) {

						indexFirstObservation = index.get(i);
						getModel().setObservationInLogLikelihoodFunction(indexFirstObservation);
						likelihoodFirst = Math.exp(getModel().individualLLK.getValue());
						observedFirst = getModel().individualLLK.getObserved();
						du_dbetaFirst = getModel().individualLLK.getLikelihoodFunction().getGradient();
						d2u_d2betaFirst = getModel().individualLLK.getLikelihoodFunction().getHessian();

						for (int j = i + 1; j < index.size(); j++) {

							indexSecondObservation = index.get(j);
							getModel().setObservationInLogLikelihoodFunction(indexSecondObservation);
							likelihoodSecond = Math.exp(getModel().individualLLK.getValue());
							observedSecond = getModel().individualLLK.getObserved();
							du_dbetaSecond = getModel().individualLLK.getLikelihoodFunction().getGradient();
							d2u_d2betaSecond = getModel().individualLLK.getLikelihoodFunction().getHessian();

							sumObserved = observedFirst + observedSecond;

							multiplyingFactor = 1d;
							if (Math.abs(sumObserved - 1) < VERY_SMALL) {
								multiplyingFactor = -1d;
							}

							getModel().copula.setX(indexFirstObservation, indexSecondObservation);
							double copulaValue = getModel().copula.getValue();
							Matrix copulaGradient = getModel().copula.getGradient();
							Matrix copulaHessian = getModel().copula.getHessian();

							Matrix gradientMultipliedTemp = du_dbetaFirst.multiply(du_dbetaSecond.transpose());

							Matrix expansion11 = d2u_d2betaFirst.scalarMultiply((1 - likelihoodSecond) * -1d).add(
									d2u_d2betaSecond.scalarMultiply((1 - likelihoodFirst) * -1d)).add(							
											gradientMultipliedTemp.add(gradientMultipliedTemp.transpose())).scalarMultiply(
													copulaValue * multiplyingFactor * inverseAdditionalLikelihoodTerm);


							Matrix expansion12 = du_dbetaFirst.scalarMultiply((1 - likelihoodSecond) * -1d).add(
									du_dbetaSecond.scalarMultiply((1 - likelihoodFirst) * -1d)).multiply(copulaGradient.transpose()).scalarMultiply(multiplyingFactor * inverseAdditionalLikelihoodTerm);

							Matrix expansion22 = copulaHessian.scalarMultiply((1 - likelihoodFirst) * (1 - likelihoodSecond) * multiplyingFactor * inverseAdditionalLikelihoodTerm);

							tmp = expansion11.matrixStack(expansion12, false).matrixStack(expansion12.transpose().matrixStack(expansion22, false), true);

							MatrixUtility.add(additionalHessian, tmp);

						}
					}

					additionalHessians.put(index, additionalHessian);
				}
				this.additionalHessians = additionalHessians;
				additionalHessianTermUptoDate = true;
			}
			return additionalHessians;
		}
		
	}
	
	
	private CopulaExpression copula;
	
	/**
	 * Constructor for this class
	 * @param glm a GeneralizedLinearModel instance
	 * @param copula a CopulaExpression instance
	 * @throws StatisticalDataException if the hierarchical level specification in the copula is not found in the data set
	 */
	public FGMCopulaGLModel(GeneralizedLinearModel glm, CopulaExpression copula) throws StatisticalDataException {
		super(glm);
		if (!glm.getEstimator().isConvergenceAchieved()) {
			glm.doEstimation();
		}
		glm.setParameters(glm.getEstimator().getParameterEstimates().getMean());
		this.copula = copula;
		this.copula.initialize(this, getDataStructure());
	}
	
	private Matrix getCovarianceParameters() {return copula.getBeta();}
	
	@Override
	public Matrix getParameters() {return le.getBeta().matrixStack(getCovarianceParameters(), true);}
	

	@Override
	public void setParameters(Matrix beta) {
		((FGMCopulaGLModel.OverallLogLikelihood) getLogLikelihood()).init();
		if (beta == null) {
			le.setBeta(new Matrix(matrixX.m_iCols, 1));		// default starting parameters at 0
		} else {
			le.setBeta(beta.getSubMatrix(0, beta.m_iRows - copula.getNumberOfParameters() - 1, 0, 0));
			copula.setBeta(beta.getSubMatrix(beta.m_iRows - copula.getNumberOfParameters(), beta.m_iRows - 1, 0, 0));
		}
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
			beta.m_afData[parameterName][0] = value;
			setParameters(beta);
			llk = getLogLikelihood().getValue();
			likelihoodValues.add(new LikelihoodValue(beta, llk));
			System.out.println("Parameter value : " + value + "; Log-likelihood : " + llk);
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
	protected void setCompleteLLK() {completeLLK = new OverallLogLikelihood(this);}
	
	@Override
	public String toString() {
		return "Generalized linear model based on FGM copula";
	}

	@Override
	public HierarchicalStatisticalDataStructure getDataStructure() {
		return (HierarchicalStatisticalDataStructure) super.getDataStructure();
	}
	
	@Override
	public void getSummary() {
		super.getSummary();
		if (getEstimator().isConvergenceAchieved()) {
			NumberFormat formatter = NumberFormat.getInstance();
			formatter.setMaximumFractionDigits(4);
			SpearmanCorrelationCoefficient scc = new SpearmanCorrelationCoefficient();
			CorrelationEstimate[] spearmanCorr = scc.getSpearmanCorrelationCoefficient(this);
			System.out.println("Spearman's correlation coefficients");
			for (int i = 0; i < spearmanCorr.length; i++) {
				CorrelationEstimate estimate = spearmanCorr[i];
				System.out.println(i + " : " + "Estimate : " + formatter.format(estimate.getMean()) +
						"; n : " + estimate.getSampleSize() + 
						"; t-value : " + formatter.format(estimate.getStudentT()));
			}
		}
	}
	
	protected CopulaExpression getCopula() {return copula;}
	
	
	@Override
	protected HierarchicalStatisticalDataStructure getDataStructureFromDataSet(	DataSet dataSet) {
		return new GenericHierarchicalSpatialDataStructure(dataSet);
	}

}
