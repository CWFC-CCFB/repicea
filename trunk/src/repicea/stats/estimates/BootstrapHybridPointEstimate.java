package repicea.stats.estimates;

import java.lang.reflect.Constructor;
import java.security.InvalidParameterException;
import java.util.ArrayList;
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

	public class VariancePointEstimate {
		private final Matrix modelRelatedVariance;
		private final Matrix samplingRelatedVariance;
		private final Matrix totalVariance;
		private final List<String> rowIndex;
		private final List<List<String>> collapseIndexList;
		
		private VariancePointEstimate(Matrix modelRelatedVariance, Matrix samplingRelatedVariance, Matrix totalVariance, List<String> rowIndex) {
			this.modelRelatedVariance = modelRelatedVariance;
			this.samplingRelatedVariance = samplingRelatedVariance;
			this.totalVariance = totalVariance; 
			this.rowIndex = BootstrapHybridPointEstimate.this.rowIndex;  // reference to the original rowIndex in the estimate. 
			this.collapseIndexList = BootstrapHybridPointEstimate.this.collapseIndexList;  // reference to the original list in the estimate. 
		}

		/**
		 * This method returns the estimate of the total variance.
		 * @return a Matrix instance
		 */
		public Matrix getTotalVariance() {
			return collapseVarianceMatrixIfNeeded(totalVariance);
//			return totalVariance;
		}
		
		private Matrix collapseVarianceMatrixIfNeeded(Matrix varianceMat) {
			Matrix basicVariance = varianceMat;
			if (!rowIndex.isEmpty()) {
				if (rowIndex.size() == basicVariance.m_iRows && !collapseIndexList.isEmpty()) {
					Matrix newVariance = new Matrix(collapseIndexList.size(), collapseIndexList.size());
					for (int i = 0; i < newVariance.m_iRows; i++) {
						List<String> requestedIndices_i = collapseIndexList.get(i);
						for (int j = 0; j < newVariance.m_iRows; j++) {
							List<String> requestedIndices_j = collapseIndexList.get(j);
							newVariance.m_afData[i][j] = basicVariance.getSubMatrix(convertIndexIntoInteger(requestedIndices_i), 
									convertIndexIntoInteger(requestedIndices_j)).getSumOfElements();
						}
					}
					return newVariance;
				} 
			}
			return basicVariance;
		}
		
		
		/**
		 * This method returns the estimate of the model-related variance.
		 * @return a Matrix instance
		 */
		public Matrix getModelRelatedVariance() {
			return collapseVarianceMatrixIfNeeded(modelRelatedVariance);
//			return modelRelatedVariance;
		}
		
		/**
		 * This method returns the estimate of the sampling-related variance.
		 * @return a Matrix instance
		 */
		public Matrix getSamplingRelatedVariance() {
			return collapseVarianceMatrixIfNeeded(samplingRelatedVariance);
//			return samplingRelatedVariance;
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
	 * the true variance. 
	 * @return a Matrix
	 * @see <a href=https://academic.oup.com/forestry/article/91/3/354/4647707>
	 * Fortin, M., Manso, R., and Schneider, R. 2018. Parametric bootstrap estimators for hybrid 
	 * inference in forest inventories. Forestry 91(3): 354-365. </a>
	 */
	public final VariancePointEstimate getUncorrectedVariance() {
		MonteCarloEstimate variance = new MonteCarloEstimate();
		MonteCarloEstimate mean = new MonteCarloEstimate();
		for (PointEstimate<?> estimate : estimates) {
			mean.addRealization(estimate.getMean());
			variance.addRealization(estimate.getVariance());
		}
		return new VariancePointEstimate(mean.getVariance(), 
				variance.getMean(), 
				mean.getVariance().add(variance.getMean()),
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
	public final VariancePointEstimate getVarianceOfTotalEstimate() {
		MonteCarloEstimate variance = new MonteCarloEstimate();
		MonteCarloEstimate mean = new MonteCarloEstimate();
		int nbObs = estimates.get(0).getObservations().size();
//		double populationSize = estimates.get(0).populationSize;
		EmpiricalDistribution[] observationMeans = new EmpiricalDistribution[nbObs];
		for (int i = 0; i < nbObs; i++) {
			observationMeans[i] = new EmpiricalDistribution();
		}
		for (PointEstimate<?> estimate : estimates) {
			mean.addRealization(estimate.getMean());
			variance.addRealization(estimate.getVariance());
			for (int i = 0; i < nbObs; i++) {
				observationMeans[i].addRealization(estimate.getObservations().get(i).getData());	// storing the realizations of the same observation in the same SampleMeanEstimate instance 
			}
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
			for (int i = 0; i < nbObs; i++) {
				if (meanEstimate instanceof PopulationTotalEstimate) {
					popUnit = new PopulationUnitWithUnequalInclusionProbability(observationMeans[i].getMean(),
							((PopulationTotalEstimate) estimates.get(0)).getObservations().get(i).getInclusionProbability());
					((PopulationTotalEstimate) meanEstimate).addObservation((PopulationUnitWithUnequalInclusionProbability) popUnit);
				} else {
					popUnit = new PopulationUnitWithEqualInclusionProbability(observationMeans[i].getMean());
					((PopulationMeanEstimate) meanEstimate).addObservation((PopulationUnitWithEqualInclusionProbability) popUnit);
				}
			}
			
			Matrix meanContribution = mean.getVariance();
			Matrix meanDesignVariance = meanEstimate.getVariance();
			Matrix averageVariance = variance.getMean();
			
			Matrix samplingRelatedComponent = meanDesignVariance;
			Matrix modelRelatedComponent = meanContribution.add(meanDesignVariance).subtract(averageVariance);
			Matrix totalVariance = modelRelatedComponent.add(samplingRelatedComponent);
			VariancePointEstimate varEst = new VariancePointEstimate(modelRelatedComponent, 
					samplingRelatedComponent, 
					totalVariance,
					rowIndex);
			return varEst;
		} catch (Exception e) {
			throw new InvalidParameterException("An error occured while instantiating the correct PointEstimate class!");
		}
	}

	@Override
	protected Matrix getVarianceFromDistribution() {
		return getVarianceOfTotalEstimate().totalVariance;
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

	
}
