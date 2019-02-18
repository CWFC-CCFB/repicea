package repicea.math;

import org.junit.Assert;
import org.junit.Test;

public class MathUtilityTests {

	@Test
	public void testSimpleFactorials() {
		long actual = MathUtility.Factorial(5);
		Assert.assertEquals("Testing factorial 5", 120, actual);

		actual = MathUtility.Factorial(0);
		Assert.assertEquals("Testing factorial 0", 1, actual); 
		
		actual = MathUtility.Factorial(10);
		Assert.assertEquals("Testing factorial 10", 3628800, actual);
	}
}
