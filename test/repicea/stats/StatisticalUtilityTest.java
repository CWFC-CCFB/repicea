package repicea.stats;

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
		double expected = Math.pow(0.95,9);
		double actual = ar1Matrix.getValueAt(9, 0);
		Assert.assertEquals("Testing correlation", expected, actual, 1E-8);
	}

	@Test
	public void testRapidAR1MatrixInversion() {
		Matrix ar1Matrix = StatisticalUtility.constructRMatrix(new Matrix(10,1,1,1), 1, 0.95, TypeMatrixR.POWER);
		Matrix invMatrix = StatisticalUtility.getInverseCorrelationAR1Matrix(ar1Matrix.m_iRows, 0.95);
		Matrix originalInvMatrix = ar1Matrix.getInverseMatrix();
		Matrix diff = invMatrix.subtract(originalInvMatrix).getAbsoluteValue();
		boolean isDifferent = diff.anyElementLargerThan(1E-8);
		Assert.assertTrue("Testing if the two methods for computing the inverse are equavalent", !isDifferent);
	}

	@Test
	public void testInversionCovarianceMatrixBasedOnAR1MatrixInversion() {
		Matrix diag = new Matrix(10,1,0.5,0.25).matrixDiagonal();
		Matrix ar1Matrix = StatisticalUtility.constructRMatrix(new Matrix(10,1,1,1), 1, 0.95, TypeMatrixR.POWER);
		Matrix completeMatrix = diag.multiply(ar1Matrix).multiply(diag);
		Matrix originalInvMatrix = completeMatrix.getInverseMatrix();
		
		Matrix invCorrMatrix = StatisticalUtility.getInverseCorrelationAR1Matrix(ar1Matrix.m_iRows, 0.95);
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
		Matrix completeMatrix = diag.multiply(ar1Matrix).multiply(diag);
		Matrix originalInvMatrix = completeMatrix.getInverseMatrix();
		
		Matrix invCorrMatrix = StatisticalUtility.getInverseCorrelationAR1Matrix(ar1Matrix.m_iRows, 0.95);
		Matrix std = diag.diagonalVector();
		Matrix invDiag = std.multiply(std.transpose()).elementWisePower(-1d);
		Matrix invMatrix = invDiag.elementWiseMultiply(invCorrMatrix);
		Matrix diff = invMatrix.subtract(originalInvMatrix).getAbsoluteValue();
		boolean isDifferent = diff.anyElementLargerThan(1E-8);
		Assert.assertTrue("Testing if the two methods for computing the inverse are equavalent", !isDifferent);
	}

}