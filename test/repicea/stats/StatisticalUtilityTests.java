package repicea.stats;

import org.junit.Assert;
import org.junit.Test;

public class StatisticalUtilityTests {

	@Test
	public void testSimpleCombinationCounts() {
		
		long actual = StatisticalUtility.getCombinations(5, 5);
		Assert.assertEquals("Testing combination (5 - 5)", 1, actual);
		
		actual = StatisticalUtility.getCombinations(10, 8);
		Assert.assertEquals("Testing combination (10 - 8)", 45, actual);

		actual = StatisticalUtility.getCombinations(10, 2);
		Assert.assertEquals("Testing combination (10 - 2)", 45, actual);

	}
	
}