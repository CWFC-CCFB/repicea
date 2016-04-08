package repicea.stats.estimates;

import repicea.math.Matrix;
import repicea.stats.distributions.SampleMeanDistribution;

/**
 * This class implements the estimate for a sample, the sample mean being the estimator of
 * the mean and the sample variance corrected by n/(n-1) being the estimator of the
 * variance.
 * @author Mathieu Fortin - April 2016
 */
@SuppressWarnings("serial")
public class SampleMeanEstimate extends Estimate<SampleMeanDistribution> {

	public SampleMeanEstimate() {
		super(new SampleMeanDistribution());
		estimatorType = EstimatorType.Unknown;
	}

	
	/**
	 * This method adds an observation to the sample.
	 * @param observation a Matrix instance
	 */
	public void addObservation(Matrix observation) {
		getDistribution().addRealization(observation);
	}
	
	
}
