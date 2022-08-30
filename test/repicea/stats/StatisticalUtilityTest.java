package repicea.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;
import repicea.stats.StatisticalUtility.TypeMatrixR;

public class StatisticalUtilityTest {

	@Test
	public void testSimpleCombinationCounts() {
		
		long actual = StatisticalUtility.getCombinations(5, 5);
		Assert.assertEquals("Testing combination (5 - 5)", 1, actual);
		
		actual = StatisticalUtility.getCombinations(10, 8);
		Assert.assertEquals("Testing combination (10 - 8)", 45, actual);

		actual = StatisticalUtility.getCombinations(10, 2);
		Assert.assertEquals("Testing combination (10 - 2)", 45, actual);

	}

	@Test
	public void testSimpleAR1Matrix() {
		Matrix ar1Matrix = StatisticalUtility.constructRMatrix(new Matrix(10,1,1,1), 1, 0.95, TypeMatrixR.POWER);
		Matrix ar1Matrix2 = StatisticalUtility.constructRMatrix(Arrays.asList(new Double[]{1d, 0.95}), TypeMatrixR.POWER, new Matrix(10,1,1,1));
		Assert.assertTrue(!ar1Matrix.subtract(ar1Matrix2).anyElementDifferentFrom(0d));
		double expected = Math.pow(0.95,9);
		double actual = ar1Matrix2.getValueAt(9, 0);
		Assert.assertEquals("Testing correlation", expected, actual, 1E-8);
	}

	@Test
	public void testRapidAR1MatrixInversion() {
		Matrix ar1Matrix = StatisticalUtility.constructRMatrix(new Matrix(10,1,1,1), 1, 0.95, TypeMatrixR.POWER);
		Matrix ar1Matrix2 = StatisticalUtility.constructRMatrix(Arrays.asList(new Double[]{1d, 0.95}), TypeMatrixR.POWER, new Matrix(10,1,1,1));
		Assert.assertTrue(!ar1Matrix.subtract(ar1Matrix2).anyElementDifferentFrom(0d));
		
		Matrix invMatrix = StatisticalUtility.getInverseCorrelationAR1Matrix(ar1Matrix2.m_iRows, 0.95);
		Matrix originalInvMatrix = ar1Matrix2.getInverseMatrix();
		Matrix diff = invMatrix.subtract(originalInvMatrix).getAbsoluteValue();
		boolean isDifferent = diff.anyElementLargerThan(1E-8);
		Assert.assertTrue("Testing if the two methods for computing the inverse are equavalent", !isDifferent);
	}

	@Test
	public void testInversionCovarianceMatrixBasedOnAR1MatrixInversion() {
		Matrix diag = new Matrix(10,1,0.5,0.25).matrixDiagonal();
		Matrix ar1Matrix = StatisticalUtility.constructRMatrix(new Matrix(10,1,1,1), 1, 0.95, TypeMatrixR.POWER);
		Matrix ar1Matrix2 = StatisticalUtility.constructRMatrix(Arrays.asList(new Double[]{1d, 0.95}), TypeMatrixR.POWER, new Matrix(10,1,1,1));
		Assert.assertTrue(!ar1Matrix.subtract(ar1Matrix2).anyElementDifferentFrom(0d));

		Matrix completeMatrix = diag.multiply(ar1Matrix2).multiply(diag);
		Matrix originalInvMatrix = completeMatrix.getInverseMatrix();
		
		Matrix invCorrMatrix = StatisticalUtility.getInverseCorrelationAR1Matrix(ar1Matrix2.m_iRows, 0.95);
		Matrix invDiag = new Matrix(10,1,0.5,0.25).elementWisePower(-1d).matrixDiagonal();
		Matrix invMatrix = invDiag.multiply(invCorrMatrix).multiply(invDiag);
		Matrix diff = invMatrix.subtract(originalInvMatrix).getAbsoluteValue();
		boolean isDifferent = diff.anyElementLargerThan(1E-8);
		Assert.assertTrue("Testing if the two methods for computing the inverse are equavalent", !isDifferent);
	}

	@Test
	public void testInversionCovarianceMatrixBasedOnAR1MatrixInversion2() {
		Matrix diag = new Matrix(10,1,0.5,0.25).matrixDiagonal();
		Matrix ar1Matrix = StatisticalUtility.constructRMatrix(new Matrix(10,1,1,1), 1, 0.95, TypeMatrixR.POWER);
		Matrix ar1Matrix2 = StatisticalUtility.constructRMatrix(Arrays.asList(new Double[]{1d, 0.95}), TypeMatrixR.POWER, new Matrix(10,1,1,1));
		Assert.assertTrue(!ar1Matrix.subtract(ar1Matrix2).anyElementDifferentFrom(0d));

		Matrix completeMatrix = diag.multiply(ar1Matrix2).multiply(diag);
		Matrix originalInvMatrix = completeMatrix.getInverseMatrix();
		
		Matrix invCorrMatrix = StatisticalUtility.getInverseCorrelationAR1Matrix(ar1Matrix2.m_iRows, 0.95);
		Matrix std = diag.diagonalVector();
		Matrix invDiag = std.multiply(std.transpose()).elementWisePower(-1d);
		Matrix invMatrix = invDiag.elementWiseMultiply(invCorrMatrix);
		Matrix diff = invMatrix.subtract(originalInvMatrix).getAbsoluteValue();
		boolean isDifferent = diff.anyElementLargerThan(1E-8);
		Assert.assertTrue("Testing if the two methods for computing the inverse are equavalent", !isDifferent);
	}

	
	
	@Test
	public void testComparisonBetweenFormerAndNewImplementationOfMatrixR() {
		List<Double> covParms = Arrays.asList(new Double[]{1d, 0.85, 0.45});
		Matrix coordinate = new Matrix(10,1,1,1);
		Matrix formerImpl, newImpl;
		for (TypeMatrixR type : TypeMatrixR.values()) {
			if (type != TypeMatrixR.EXPONENTIAL) { // has been implemented after this function got deprecated MF2022-05-11
				if (type == TypeMatrixR.ARMA) {
					formerImpl = StatisticalUtility.constructRMatrix(coordinate, covParms.get(1), covParms.get(2), covParms.get(0), type);
				} else {	
					formerImpl = StatisticalUtility.constructRMatrix(coordinate, covParms.get(0), covParms.get(1), type);
				}
				newImpl = StatisticalUtility.constructRMatrix(covParms, type, coordinate);
				Assert.assertTrue("Testing type " + type.name(), !formerImpl.subtract(newImpl).anyElementDifferentFrom(0d));
			}
		}
	}

	@Test
	public void simpleTestWith2DimensionEuclideanDistance() {
		List<Double> covParms = Arrays.asList(new Double[]{1d, 0.85});
		Matrix coordinateX = new Matrix(2,1,0,1);
		Matrix coordinateY = new Matrix(2,1,1,-1);
		Matrix matR = StatisticalUtility.constructRMatrix(covParms, TypeMatrixR.POWER, coordinateX, coordinateY);
		double expected = Math.pow(covParms.get(1), Math.sqrt(2d));
		double actual = matR.getValueAt(0, 1);
		Assert.assertEquals("Testing correlation at 0, 1", expected, actual, 1E-8);
	}

	@Test
	public void simpleComparisonBetweenExponentialAndPowerCovarianceStructure() {
		List<Double> covParmsExponential = Arrays.asList(new Double[]{1d, 1.00});
		double conversionToPower = Math.exp(-1d/covParmsExponential.get(1));
		List<Double> covParmsPower = Arrays.asList(new Double[]{1d, conversionToPower});
		
		Matrix coordinateX = new Matrix(2,1,0,1);
		Matrix coordinateY = new Matrix(2,1,1,-1);
		Matrix matRPower = StatisticalUtility.constructRMatrix(covParmsPower, TypeMatrixR.POWER, coordinateX, coordinateY);
		Matrix matRExp = StatisticalUtility.constructRMatrix(covParmsExponential, TypeMatrixR.EXPONENTIAL, coordinateX, coordinateY);
		double expectedPow = Math.pow(covParmsPower.get(1), Math.sqrt(2d));
		double actualPow = matRPower.getValueAt(0, 1);
		Assert.assertEquals("Testing correlation at 0, 1", expectedPow, actualPow, 1E-8);

		double expectedExp = Math.exp(-Math.sqrt(2d) / covParmsExponential.get(1));
		double actualExp = matRPower.getValueAt(0, 1);
		Assert.assertEquals("Testing correlation at 0, 1", expectedExp, actualExp, 1E-8);

		boolean areEqual = !matRPower.subtract(matRExp).getAbsoluteValue().anyElementLargerThan(1E-8);
		Assert.assertTrue("Testing if the two matrices are equal", areEqual);
	}
	
	@Test
	public void quantileTest() {
		List<Double> myList = new ArrayList<Double>();
		for (double i = 0; i < 100;  i++) {
			myList.add(i);
		}
		double q = StatisticalUtility.getQuantileEstimateFromSample(myList, 0.3, null);
		Assert.assertEquals("Comparing quantile", 29.4333333333, q, 1E-8);
	}
	
	@Test
	public void quantileTest2() {
		List<Double> myList = new ArrayList<Double>();
		for (double i = 0; i < 200;  i++) {
			myList.add(i);
		}
		double q = StatisticalUtility.getQuantileEstimateFromSample(myList, 0.3, null);
		Assert.assertEquals("Comparing quantile", 59.4333333333, q, 1E-8);
	}

	
	@Test
	public void quantileTest3() {
		List<Double> myList = new ArrayList<Double>();
		for (double i = 0; i < 200;  i = i + 2) {
			myList.add(i);
		}
		double q = StatisticalUtility.getQuantileEstimateFromSample(myList, 0.3, null);
		Assert.assertEquals("Comparing quantile", 58.8666666666, q, 1E-8);
	}

	
	@Test
	public void quantileTest4() {
		List<Double> myList = new ArrayList<Double>();
		for (double i = 0; i < 100;  i++) {
			myList.add(i * i);
		}
		double q = StatisticalUtility.getQuantileEstimateFromSample(myList, 0.3, null);
		Assert.assertEquals("Comparing quantile", 866.5666666666, q, 1E-8);
	}
	
	@Test
	public void quantileTest5() {
		List<Double> myList = new ArrayList<Double>();
		for (double i = 0; i < 200;  i++) {
			myList.add(i * i);
		}
		double q = StatisticalUtility.getQuantileEstimateFromSample(myList, 0.3, null);
		Assert.assertEquals("Comparing quantile", 3532.5666666666666, q, 1E-8);
	}

	
	@Test
	public void quantileTest6() {
		List<Double> myList = new ArrayList<Double>();
		for (double i = 0; i < 200;  i = i + 2) {
			myList.add(i * i);
		}
		double q = StatisticalUtility.getQuantileEstimateFromSample(myList, 0.3, null);
		Assert.assertEquals("Comparing quantile", 3466.2666666666, q, 1E-8);
	}

	
	@Test
	public void quantileTest7() {
		List<Double> myList = new ArrayList<Double>();
		List<Integer> myWeights = new ArrayList<Integer>();
		for (double i = 0; i < 200;  i = i + 2) {
			myList.add(i * i);
			if (i==0) {
				myWeights.add(20);
			} else {
				myWeights.add(1);
			}
		}
		double q = StatisticalUtility.getQuantileEstimateFromSample(myList, 0.3, myWeights);
		Assert.assertEquals("Comparing quantile", 1041.6, q, 1E-8);
	}

	
}