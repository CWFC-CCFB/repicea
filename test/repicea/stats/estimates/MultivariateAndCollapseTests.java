package repicea.stats.estimates;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;
import repicea.stats.Distribution.Type;
import repicea.stats.StatisticalUtility;

public class MultivariateAndCollapseTests {

	@Test
	public void completeCollapsingWithMonteCarloEstimateTest() {
		int nElements = 10;
		MonteCarloEstimate est = new MonteCarloEstimate();
		for (int i = 0; i < 1000; i++) {
			est.addRealization(StatisticalUtility.drawRandomVector(nElements, Type.GAUSSIAN));
		}
		
		Matrix basicMean = est.getMean();
		Assert.assertTrue("Testing we have 10 rows", basicMean.m_iRows == nElements);
		Matrix basicVariance = est.getVariance();
		Assert.assertTrue("Testing we have 10 rows", basicVariance.m_iRows == nElements);
		Assert.assertTrue("Testing we have 10 cols", basicVariance.m_iCols == nElements);
		
		List<String> rowIndex = new ArrayList<String>();
		for (int i = 0; i < nElements; i++) {
			rowIndex.add("" + i);
		}
		est.setRowIndex(rowIndex);
		List<List<String>> collapseIndices = new ArrayList<List<String>>();
		collapseIndices.add(rowIndex);
		est.setCollapseIndexList(collapseIndices);
		
		Matrix collapsedMean = est.getMean();
		Assert.assertTrue("Testing we have 1 row", collapsedMean.m_iRows == 1);
		Assert.assertEquals("Testing consistency", basicMean.getSumOfElements(), collapsedMean.m_afData[0][0], 1E-8);
		Matrix collapsedVariance = est.getVariance();
		Assert.assertTrue("Testing we have 1 row", collapsedVariance.m_iRows == 1);
		Assert.assertTrue("Testing we have 1 col", collapsedVariance.m_iCols == 1);
		Assert.assertEquals("Testing consistency", basicVariance.getSumOfElements(), collapsedVariance.m_afData[0][0], 1E-8);
		
		est.setRowIndex(null);
		Matrix newBasicMean = est.getMean();
		Matrix diffMean = newBasicMean.subtract(basicMean);
		Assert.assertTrue("New basic mean is equal to the original", !diffMean.getAbsoluteValue().anyElementLargerThan(1E-8));
		Matrix newBasicVariance = est.getVariance();
		Matrix diffVar = newBasicVariance.subtract(basicVariance);
		Assert.assertTrue("New basic variance is equal to the original", !diffVar.getAbsoluteValue().anyElementLargerThan(1E-8));
		
	}
	
}
