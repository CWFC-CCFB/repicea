/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2020 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.estimates;

import java.lang.reflect.Constructor;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.distributions.EmpiricalDistribution;
import repicea.stats.distributions.UnknownDistribution;
import repicea.stats.distributions.utility.GaussianUtility;
import repicea.stats.sampling.PopulationUnit;
import repicea.stats.sampling.PopulationUnitWithEqualInclusionProbability;
import repicea.stats.sampling.PopulationUnitWithUnequalInclusionProbability;

/**
 * This class implements the bootstrap estimator of the total as developed by Fortin et al. 
 * (2018) in the context of hybrid inference. More specifically, it applies when (i) the 
 * variable of interest has not been observed but predicted by a model and (ii) the covariates 
 * are not censused but only observed in a sample of the population. The estimator takes into 
 * account the variance that stems from the model as well as that of the sampling design. 
 * IMPORTANT: The model must  benefit from a full stochastic implementation.
 * @author Mathieu Fortin - May 2018
 * @see <a href=https://academic.oup.com/forestry/article/91/3/354/4647707>
 * Fortin, M., Manso, R., and Schneider, R. 2018. Parametric bootstrap estimators for hybrid 
 * inference in forest inventories. Forestry 91(3): 354-365. </a>
 */
public final class BootstrapHybridPointEstimate extends Estimate<UnknownDistribution> implements NumberOfRealizationsProvider {

	
	public static class VariancePointEstimate extends SimpleEstimate {
		
		private final Matrix modelRelatedVariance;
		private final Matrix samplingRelatedVariance;
//		private final Matrix varianceBiasCorrection;
		
		private VariancePointEstimate(Matrix pointEstimate, Matrix totalVariance, Matrix modelRelatedVariance, Matrix samplingRelatedVariance, List<String> rowIndex) {
			super(pointEstimate, totalVariance); 
			this.modelRelatedVariance = modelRelatedVariance;
			this.samplingRelatedVariance = samplingRelatedVariance;
//			Matrix denominator = pointEstimate.multiply(pointEstimate.transpose()).subtract(samplingRelatedVariance);
//			Matrix numerator = grossModelRelatedVariance.elementWiseMultiply(samplingRelatedVariance);
//			Matrix correction = customizedElementWiseDivide(numerator, denominator);
//			varianceBiasCorrection = correction.scalarMultiply(-1d);
//			if (totalVariance != null) {
			setRowIndex(rowIndex);  
//			}
		}

//		private Matrix customizedElementWiseDivide(Matrix mat1, Matrix mat2) {
//			if (mat1.m_iRows != mat2.m_iRows || mat1.m_iCols != mat2.m_iCols) {
//				throw new InvalidParameterException("The two matrix arguments do not have the same dimensions!");
//			}
//			Matrix outputMatrix = new Matrix(mat1.m_iRows, mat2.m_iCols); 
//			for (int i = 0; i < mat1.m_iRows; i++) {
//				for (int j = 0; j < mat1.m_iCols; j++) {
//					if (mat2.m_afData[i][j] != 0) {
//						outputMatrix.m_afData[i][j] = mat1.m_afData[i][j] / mat2.m_afData[i][j];
//					}
//				}
//			}
//			return outputMatrix;
//		}

		/**
		 * This method returns the estimate of the total variance.
		 * @return a Matrix instance
		 */
		public Matrix getTotalVariance() {
			return getVariance();
		}
		
		/**
		 * Provide the model-related variance. 
		 * @return a Matrix instance
		 */
		public Matrix getModelRelatedVariance() {
//			return grossModelRelatedVariance.subtract(varianceBiasCorrection);
			return modelRelatedVariance;
		}
		
		/**
		 * This method returns the estimate of the sampling-related variance.
		 * @return a Matrix instance
		 */
		public Matrix getSamplingRelatedVariance() {
			return samplingRelatedVariance;
		}
	}
	
	
	private final List<PointEstimate<?>> estimates;
	
	public BootstrapHybridPointEstimate() {
		super(new UnknownDistribution());
		estimates = new ArrayList<PointEstimate<?>>();
	}

	/**
	 * This method adds a realization of the point estimate. The compatibility of 
	 * the instance with previously added instances is checked. If the check fails
	 * an InvalidParameterException is thrown.
	 * @param estimate a PointEstimate instance
	 */
	public void addPointEstimate(PointEstimate<?> estimate) {
		if (estimates.isEmpty() || estimates.get(0).isMergeableEstimate(estimate)) {
			estimates.add(estimate);
		} else {
			throw new InvalidParameterException("The point estimate is not compatible with the previous estimates!");
		}
	}
	
	@Override
	protected boolean isMergeableEstimate(Estimate<?> estimate) {
		if (estimate instanceof BootstrapHybridPointEstimate) {
			if (((BootstrapHybridPointEstimate) estimate).getNumberOfRealizations() == getNumberOfRealizations()) {
				if (estimates.get(0).isMergeableEstimate(((BootstrapHybridPointEstimate) estimate).estimates.get(0)));
				return true;
			};
		}
		return false;
	}
	
	/**
	 * This method is useful for large simulations. The simulation can be run in different threads or batches
	 * and the resulting BootstrapHybridPointEstimate can then be combined through this method.
	 * @param estimate a BootstrapHybridPointEstimate instance that relies on the same PointEstimate class in the estimates member
	 */
	public void addBootstrapHybridEstimate(BootstrapHybridPointEstimate estimate) {
		for (PointEstimate<?> pointEstimate : estimate.estimates) {
			addPointEstimate(pointEstimate);
		}
	}

	@Override
	protected Matrix getMeanFromDistribution() {
		Matrix mean = null;
		for (PointEstimate<?> estimate : estimates) {
			if (mean == null) {
				mean = estimate.getMean();
			} else {
				mean = mean.add(estimate.getMean());
			}
		}
		mean = mean.scalarMultiply(1d / estimates.size());
		return mean;
	}
	
	
	

	/**
	 * This method returns the uncorrected variance of the total estimate. 
	 * This estimator is based on the law of total variance. It tends to overestimate 
	 * the true variance. This method is deprecated and the getCorrectedVariance method
	 * should be used in place.
	 * @return a Matrix
	 * @see <a href=https://academic.oup.com/forestry/article/91/3/354/4647707>
	 * Fortin, M., Manso, R., and Schneider, R. 2018. Parametric bootstrap estimators for hybrid 
	 * inference in forest inventories. Forestry 91(3): 354-365. </a>
	 */
	@Deprecated
	public final VariancePointEstimate getUncorrectedVariance() {
		MonteCarloEstimate variance = new MonteCarloEstimate();
		MonteCarloEstimate mean = new MonteCarloEstimate();
		for (PointEstimate<?> estimate : estimates) {
			mean.addRealization(estimate.getMean());
			variance.addRealization(estimate.getVariance());
		}
		return new VariancePointEstimate(null, 
				mean.getVariance().add(variance.getMean()),
				mean.getVariance(), 
				variance.getMean(), 
				rowIndex);
	}

	
	/**
	 * This method returns the corrected variance of the total estimate. 
	 * This estimator is unbiased. 
	 * @return a VariancePointEstimate
	 * 
	 * @see <a href=https://academic.oup.com/forestry/article/91/3/354/4647707>
	 * Fortin, M., Manso, R., and Schneider, R. 2018. Parametric bootstrap estimators for hybrid 
	 * inference in forest inventories. Forestry 91(3): 354-365. </a>
	 */
	public final VariancePointEstimate getCorrectedVariance() {
		if (getNumberOfRealizations() > 1) {
			MonteCarloEstimate variance = new MonteCarloEstimate();
			MonteCarloEstimate mean = new MonteCarloEstimate();
			int sampleSize = estimates.get(0).getObservations().size();
			EmpiricalDistribution observationMeans = new EmpiricalDistribution();
			int nbElementsPerObs = 0;
			for (PointEstimate<?> estimate : estimates) {
				if (nbElementsPerObs == 0) {
					nbElementsPerObs = estimate.getNumberOfElementsPerObservation();
				}
				mean.addRealization(estimate.getMean());
				variance.addRealization(estimate.getVariance());
				observationMeans.addRealization(estimate.getObservationVector());
			}
			
			PointEstimate<?> meanEstimate; 
			try {
				if (estimates.get(0).isPopulationSizeKnown()) {
					double populationSize = estimates.get(0).getPopulationSize();
					Constructor<?> cons = estimates.get(0).getClass().getConstructor(double.class);
					meanEstimate = (PointEstimate<?>) cons.newInstance(populationSize);
				} else {
					meanEstimate = estimates.get(0).getClass().newInstance();
				}
				
				PopulationUnit popUnit;
				Matrix observationMeanMatrix = observationMeans.getMean();
				for (int i = 0; i < sampleSize; i++) {
					Matrix meanForThisI = observationMeanMatrix.getSubMatrix(i * nbElementsPerObs,  (i + 1) * nbElementsPerObs - 1, 0, 0);
					if (meanEstimate instanceof PopulationTotalEstimate) {
						popUnit = new PopulationUnitWithUnequalInclusionProbability(meanForThisI,
								((PopulationTotalEstimate) estimates.get(0)).getObservations().get(i).getInclusionProbability());
						((PopulationTotalEstimate) meanEstimate).addObservation((PopulationUnitWithUnequalInclusionProbability) popUnit);
					} else {
						popUnit = new PopulationUnitWithEqualInclusionProbability(meanForThisI);
						((PopulationMeanEstimate) meanEstimate).addObservation((PopulationUnitWithEqualInclusionProbability) popUnit);
					}
				}
				
//				Matrix crudeVariances = observationMeans.getVariance();
//				double test = crudeVariances.subtract(crudeVariances.diagonalVector().matrixDiagonal()).getSumOfElements() / (sampleSize * (sampleSize - 1));
//				System.out.println("Marginal model-related variance = " + test);
				
				Matrix meanModelContribution = mean.getVariance();
				Matrix designVarianceOfMeanRealizedY = meanEstimate.getVariance();
				Matrix averageDesignVariance = variance.getMean();
				
				Matrix samplingRelatedComponent = designVarianceOfMeanRealizedY;
				Matrix grossModelRelatedComponent = meanModelContribution.add(designVarianceOfMeanRealizedY).subtract(averageDesignVariance);
				Matrix totalVariance = grossModelRelatedComponent.add(samplingRelatedComponent);

//				Matrix pointEstimate = mean.getMean();
//				Matrix denominator = pointEstimate.multiply(pointEstimate.transpose()).subtract(samplingRelatedComponent);
//				Matrix numerator = modelRelatedComponent.multiply(samplingRelatedComponent);
//				double correction = numerator.m_afData[0][0]/denominator.m_afData[0][0];
//				System.out.println("Estimated correction = " + correction);
						
				VariancePointEstimate varEst = new VariancePointEstimate(getMean(),
						totalVariance,
						grossModelRelatedComponent, 
						samplingRelatedComponent, 
						rowIndex);
				return varEst;
			} catch (Exception e) {
				throw new InvalidParameterException("An error occured while instantiating the correct PointEstimate class!");
			}
		} else {
			System.out.println("The variance of the hybrid point estimate cannot be calculated because there is not enough realizations!");
			return new VariancePointEstimate(null, null, null, null, rowIndex);
		}
	}

	@Override
	protected Matrix getVarianceFromDistribution() {
		return getCorrectedVariance().getVariance();
	}

	@Override
	public int getNumberOfRealizations() {
		return estimates.size();
	}
	
	protected Matrix getQuantileForProbability(double probability) {
		Matrix stdDev = getVariance().diagonalVector().elementWisePower(.5); 
		double quantile = GaussianUtility.getQuantile(probability);
		return getMean().add(stdDev.scalarMultiply(quantile));
	}

	@Override
	public ConfidenceInterval getConfidenceIntervalBounds(double oneMinusAlpha) {
		Matrix lowerBoundValue = getQuantileForProbability(.5 * (1d - oneMinusAlpha));
		Matrix upperBoundValue = getQuantileForProbability(1d - .5 * (1d - oneMinusAlpha));
		return new ConfidenceInterval(lowerBoundValue, upperBoundValue, oneMinusAlpha);
	}

	/**
	 * Returns the sample size the individual point estimates are based on or -1 if 
	 * no point estimate is available at the moment.
	 * @return an integer
	 */
	public int getSampleSize() {
		if (estimates == null || estimates.isEmpty()) {
			return -1;
		} else {
			return estimates.get(0).getObservations().size();
		}
	}
	
	
	
	
	/**
	 * This method returns a MonteCarloEstimate instance that results from the subtraction of two 
	 * MonteCarloEstimate instances with the same number of realizations. 
	 * @param estimate2 the estimate that is subtracted from this estimate
	 * @return a BootstrapHybridPointEstimate instance
	 */
	protected BootstrapHybridPointEstimate subtract(BootstrapHybridPointEstimate estimate2) {
		if (getNumberOfRealizations() != estimate2.getNumberOfRealizations()) {
			throw new InvalidParameterException("The number of realizations is not consistent!");
		}
		BootstrapHybridPointEstimate outputEstimate = new BootstrapHybridPointEstimate();
		for (int i = 0; i < getNumberOfRealizations(); i++) {
			outputEstimate.addPointEstimate(estimates.get(i).subtract(estimate2.estimates.get(i)));
		}
		return outputEstimate;
	}
	
	/**
	 * This method returns a MonteCarloEstimate instance that results from the sum of two 
	 * MonteCarloEstimate instances with the same number of realizations. 
	 * @param estimate2 the estimate that is added to this estimate
	 * @return a BootstrapHybridPointEstimate instance
	 */
	protected BootstrapHybridPointEstimate add(BootstrapHybridPointEstimate estimate2) {
		if (getNumberOfRealizations() != estimate2.getNumberOfRealizations()) {
			throw new InvalidParameterException("The number of realizations is not consistent!");
		}
		BootstrapHybridPointEstimate outputEstimate = new BootstrapHybridPointEstimate();
		for (int i = 0; i < getNumberOfRealizations(); i++) {
			outputEstimate.addPointEstimate(estimates.get(i).add(estimate2.estimates.get(i)));
		}
		return outputEstimate;
	}

	/**
	 * This method returns a MonteCarloEstimate instance that results from the product of original 
	 * MonteCarloEstimate instance and a scalar. 
	 * @param scalar the multiplication factor
	 * @return a BootstrapHybridPointEstimate instance
	 */
	protected BootstrapHybridPointEstimate multiply(double scalar) {
		BootstrapHybridPointEstimate outputEstimate = new BootstrapHybridPointEstimate();
		for (int i = 0; i < getNumberOfRealizations(); i++) {
			outputEstimate.addPointEstimate(estimates.get(i).multiply(scalar));
		}
		return outputEstimate;
	}

	@Override
	public Estimate<?> getDifferenceEstimate(Estimate<?> estimate2) {
		if (this.isMergeableEstimate(estimate2)) {
			return subtract((BootstrapHybridPointEstimate) estimate2);
		} else {
			return super.getDifferenceEstimate(estimate2);
		}
	}

	@Override
	public Estimate<?> getSumEstimate(Estimate<?> estimate2) {
		if (this.isMergeableEstimate(estimate2)) {
			return add((BootstrapHybridPointEstimate) estimate2);
		} else {
			return super.getSumEstimate(estimate2);
		}
	}

	@Override
	public Estimate<?> getProductEstimate(double scalar) {
		return multiply(scalar);
	}

	@Override
	public Estimate<?> collapseEstimate(LinkedHashMap<String, List<String>> desiredIndicesForCollapsing) {
		Estimate<?> simpleEstimate = collapseMeanAndVariance(desiredIndicesForCollapsing);
		VariancePointEstimate vpe = getCorrectedVariance();
		Matrix collapsedSamplingRelatedVariance = collapseSquareMatrix(vpe.getSamplingRelatedVariance(), desiredIndicesForCollapsing);
		Matrix collapsedModelRelatedVariance = collapseSquareMatrix(vpe.modelRelatedVariance, desiredIndicesForCollapsing);
		List<String> newIndexRow = new ArrayList<String>(desiredIndicesForCollapsing.keySet());
		Collections.sort(newIndexRow);
		VariancePointEstimate outputEstimate = new VariancePointEstimate(simpleEstimate.getMean(),
				simpleEstimate.getVariance(),
				collapsedModelRelatedVariance,
				collapsedSamplingRelatedVariance,
				newIndexRow);
		return outputEstimate;
	}

	
}
