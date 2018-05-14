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
@SuppressWarnings("serial")
public class BootstrapHybridPointEstimate extends Estimate<UnknownDistribution>{

	public class VariancePointEstimate {
		private final Matrix modelRelatedVariance;
		private final Matrix samplingRelatedVariance;
		private final Matrix totalVariance;
		
		private VariancePointEstimate(Matrix modelRelatedVariance, Matrix samplingRelatedVariance, Matrix totalVariance) {
			this.modelRelatedVariance = modelRelatedVariance;
			this.samplingRelatedVariance = samplingRelatedVariance;
			this.totalVariance = totalVariance; 
		}

		/**
		 * This method returns the estimate of the total variance.
		 * @return a Matrix instance
		 */
		public Matrix getTotalVariance() {return totalVariance;}

		/**
		 * This method returns the estimate of the model-related variance.
		 * @return a Matrix instance
		 */
		public Matrix getModelRelatedVariance() {return modelRelatedVariance;}
		
		/**
		 * This method returns the estimate of the sampling-related variance.
		 * @return a Matrix instance
		 */
		public Matrix getSamplingRelatedVariance() {return samplingRelatedVariance;}
	
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
		if (estimates.isEmpty() || estimates.get(0).isCompatible(estimate)) {
			estimates.add(estimate);
		} else {
			throw new InvalidParameterException("The point estimate is not compatible with the previous estimates!");
		}
	}
	
	/**
	 * This method returns the estimate of the total, which is the mean of the 
	 * realized point estimates.
	 * @return a Matrix instance
	 */
	@Override
	public Matrix getMean() {
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
	public final Matrix getUncorrectedVariance() {
		MonteCarloEstimate variance = new MonteCarloEstimate();
		MonteCarloEstimate mean = new MonteCarloEstimate();
		for (PointEstimate<?> estimate : estimates) {
			mean.addRealization(estimate.getMean());
			variance.addRealization(estimate.getVariance());
		}
		return mean.getVariance().add(variance.getMean());
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
			VariancePointEstimate varEst = new VariancePointEstimate(modelRelatedComponent, samplingRelatedComponent, totalVariance);
			return varEst;
		} catch (Exception e) {
			throw new InvalidParameterException("An error occured while instantiating the correct PointEstimate class!");
		}
	}

	@Override
	public Matrix getVariance() {
		return getVarianceOfTotalEstimate().getTotalVariance();
	}
	
	protected int getNumberOfRealizations() {
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

		
}
