package repicea.stats.distributions;

import repicea.math.Matrix;
import repicea.stats.Distribution;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
public class UniformDistribution implements Distribution, BoundedDistribution {

	private final BasicBound upperBound;
	private final BasicBound lowerBound;
	
	public UniformDistribution(Matrix lowerBound, Matrix upperBound) {
		this.upperBound = new BasicBound(true);
		this.upperBound.setBoundValue(upperBound);
		this.lowerBound = new BasicBound(false);
		this.lowerBound.setBoundValue(lowerBound);
	}
	
	
	
	@Override
	public Matrix getMean() {
		return upperBound.getBoundValue().add(lowerBound.getBoundValue()).scalarMultiply(.5);
	}

	@Override
	public Matrix getVariance() {
		Matrix diagonalDifference = upperBound.getBoundValue().subtract(lowerBound.getBoundValue()).matrixDiagonal();
		return diagonalDifference.multiply(diagonalDifference).scalarMultiply(1d/12);
	}

	@Override
	public boolean isParametric() {return true;}

	@Override
	public boolean isMultivariate() {return getMean().m_iRows > 1;}

	@Override
	public Type getType() {return Type.UNIFORM;}

	@Override
	public Matrix getRandomRealization() {
		Matrix diagonalDifference = upperBound.getBoundValue().subtract(lowerBound.getBoundValue()).matrixDiagonal();
		Matrix deviates = StatisticalUtility.drawRandomVector(getMean().m_iRows, Type.UNIFORM);
		return lowerBound.getBoundValue().add(diagonalDifference.multiply(deviates));
	}



	@Override
	public void setLowerBoundValue(Matrix lowerBoundValue) {
		lowerBound.setBoundValue(lowerBoundValue);
	}

	@Override
	public void setUpperBoundValue(Matrix upperBoundValue) {
		upperBound.setBoundValue(upperBoundValue);
	}
	
	public BasicBound getLowerBound() {return lowerBound;}
	public BasicBound getUpperBound() {return upperBound;}

}
