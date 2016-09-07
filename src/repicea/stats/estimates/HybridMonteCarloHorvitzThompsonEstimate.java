package repicea.stats.estimates;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.stats.distributions.UnknownDistribution;

@SuppressWarnings("serial")
public class HybridMonteCarloHorvitzThompsonEstimate extends Estimate<UnknownDistribution>{

	private final List<HorvitzThompsonTauEstimate> estimates;
	
	public HybridMonteCarloHorvitzThompsonEstimate() {
		super(new UnknownDistribution());
		estimates = new ArrayList<HorvitzThompsonTauEstimate>();
	}

	/**
	 * This method adds a realization of the HT estimate. The compatibility of 
	 * the instance with previously added instances is checked. If the chek fails
	 * an InvalidParameterException is thrown.
	 * @param estimate a HorvitzThompsonTauEstimate instance
	 */
	public void addHTEstimate(HorvitzThompsonTauEstimate estimate) {
		if (estimates.isEmpty() || estimates.get(0).isCompatible(estimate)) {
			estimates.add(estimate);
		} else {
			throw new InvalidParameterException("The HT estimate is not compatible with the previous estimates!");
		}
	}
	
	/**
	 * This method returns the estimate of the total.
	 * @return a Matrix instance
	 */
	public Matrix getTotal() {
		Matrix mean = null;
		for (HorvitzThompsonTauEstimate estimate : estimates) {
			if (mean == null) {
				mean = estimate.getTotal();
			} else {
				mean = mean.add(estimate.getTotal());
			}
		}
		mean = mean.scalarMultiply(1d/estimates.size());
		return mean;
	}

	
	public Matrix getTotalVarianceUncorrected() {
		SampleMeanEstimate variance = new SampleMeanEstimate();
		SampleMeanEstimate mean = new SampleMeanEstimate();
		for (HorvitzThompsonTauEstimate estimate : estimates) {
			mean.addObservation(estimate.getTotal());
			variance.addObservation(estimate.getVariance());
		}
		return mean.getVariance().add(variance.getMean());
	}
	
	@Override
	public Matrix getMean() {
		return getTotal().scalarMultiply(1d/getPopulationSize());
	}
	
	protected double getPopulationSize() {
		if (!estimates.isEmpty()) {
			return estimates.get(0).getPopulationSize();
		} else {
			return 0d;
		}
	}
	
	
	protected int getNumberOfRealizations() {
		return estimates.size();
	}
	
		
}
