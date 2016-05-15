package repicea.stats.distributions;

import java.io.Serializable;

import repicea.math.Matrix;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
public class TruncatedGaussianDistribution extends StandardGaussianDistribution implements BoundedDistribution {

	protected class Bound extends BasicBound implements Serializable {
		
		private boolean isCompletelySet;
		
//		private Matrix value;
		
		private Matrix pdfValue;
		
		private Matrix cdfValue;
		
//		private final boolean isUpperBound;
		
		protected Bound(boolean isUpperBound) {
			super(isUpperBound);
		}

		@Override
		protected void setBoundValue(Matrix value) {
			super.setBoundValue(value);
			isCompletelySet = false;
		}
		
		protected synchronized Matrix getPdfValue() {
			if (!isCompletelySet) {
				update();
			}
			return pdfValue;
		}
		
		protected synchronized Matrix getCdfValue() {
			if (!isCompletelySet) {
				update();
			} 
			return cdfValue;
		}

		private void update() {
			if (getBoundValue() == null) {
				pdfValue = new Matrix(1,1);
				cdfValue = new Matrix(1,1);
				if (isUpperBound()) {
					cdfValue.m_afData[0][0] = 1d;
				} else {
					cdfValue.m_afData[0][0] = 0d;
				}
			} else {
				pdfValue = new Matrix(1,1);
				pdfValue.m_afData[0][0] = TruncatedGaussianDistribution.this.getProbabilityDensity(getBoundValue());
				cdfValue = new Matrix(1,1);
				double standardizedValue = (getBoundValue().m_afData[0][0] - TruncatedGaussianDistribution.this.getMu().m_afData[0][0])/Math.sqrt(TruncatedGaussianDistribution.this.getSigma2().m_afData[0][0]);
				cdfValue.m_afData[0][0] = GaussianUtility.getCumulativeProbability(standardizedValue);
			}
			isCompletelySet = true;
		}
		
	}
	
	private final Bound lowerBound;
	private final Bound upperBound;

	/**
	 * Constructor 1. Truncated standard Gaussian distribution.
	 */
	public TruncatedGaussianDistribution() {
		super();
		setMean(new Matrix(1,1));
		Matrix sigma2 = new Matrix(1,1);
		sigma2.m_afData[0][0] = 1d;
		setVariance(sigma2);
		lowerBound = new Bound(false);	// false: lower bound
		upperBound = new Bound(true);	// true: upper bound
	}

	
	/**
	 * Constructor 2. Truncated Gaussian distribution with mu different from 0 or sigma2 different from 1.
	 * @param mu a Matrix instance
	 * @param sigma2 a Matrix instance
	 */
	public TruncatedGaussianDistribution(Matrix mu, Matrix sigma2) {
		super();
		setMean(mu);
		setVariance(sigma2);
		lowerBound = new Bound(false);	// false: lower bound
		upperBound = new Bound(true);	// true: upper bound
	}

	@Override
	public Matrix getMean() {
		double z = upperBound.getCdfValue().subtract(lowerBound.getCdfValue()).m_afData[0][0];
		Matrix diff = lowerBound.getPdfValue().subtract(upperBound.getPdfValue()).scalarMultiply(1d/z).multiply(getStandardDeviation());
		Matrix mean = this.getMu().add(diff);
		return mean;
	}

	@Override
	public Matrix getVariance() {
		double zFactor = 1/upperBound.getCdfValue().subtract(lowerBound.getCdfValue()).m_afData[0][0];
		Matrix mult1;
		if (lowerBound.getBoundValue() != null) {
			mult1 = lowerBound.getPdfValue().multiply(lowerBound.getBoundValue());
		} else {
			mult1 = lowerBound.getPdfValue();
		}
		Matrix mult2;
		if (upperBound.getBoundValue() != null) {
			mult2 = upperBound.getPdfValue().multiply(upperBound.getBoundValue());
		} else {
			mult2 = upperBound.getPdfValue();
		}
		Matrix num1 = mult1.subtract(mult2);
		Matrix num2 = lowerBound.getPdfValue().subtract(upperBound.getPdfValue());
		return getSigma2().multiply(num1.scalarMultiply(zFactor).subtract(num2.scalarMultiply(zFactor).elementWisePower(2d)).scalarAdd(1));
	}


	@Override
	public Matrix getRandomRealization() {
		double random = StatisticalUtility.getRandom().nextDouble();
		Matrix diff = upperBound.getCdfValue().subtract(lowerBound.getCdfValue()).scalarMultiply(random).add(lowerBound.getCdfValue());
		Matrix deviate = new Matrix(1,1);
		deviate.m_afData[0][0] = GaussianUtility.getQuantile(diff.m_afData[0][0]);
		deviate = deviate.multiply(getStandardDeviation()).add(getMu());
		return deviate;
	}

	@Override
	public void setLowerBoundValue(Matrix lowerBoundValue) {
		lowerBound.setBoundValue(lowerBoundValue);
	}

	@Override
	public void setUpperBoundValue(Matrix upperBoundValue) {
		upperBound.setBoundValue(upperBoundValue);
	}

}
