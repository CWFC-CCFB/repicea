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
	
	
	@Test
	public void testFactorialRatios() {
		long actual = MathUtility.FactorialRatio(5, 3);
		Assert.assertEquals("Testing 5!/3!", 20, actual);
		
		actual = MathUtility.FactorialRatio(10, 7);
		Assert.assertEquals("Testing 10!/7!", 720, actual);

		actual = MathUtility.FactorialRatio(1, 0);
		Assert.assertEquals("Testing 1!/0!", 1, actual);
	}

}
