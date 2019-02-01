package repicea.lang.reflect;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class ArrayConversionTests {

	
	@Test
	public void testArrayDimention() {
		int[][] myArray = new int[2][2];
		for (int i = 0; i < myArray.length; i++) {
			for (int j = 0; j < myArray[i].length; j++) {
				myArray[i][j] = i + j;
			}
		}
		int[] dim = ReflectUtility.getDimensions(myArray);
		Assert.assertTrue("Testing the dimensions of the array", Arrays.equals(dim, new int[]{2,2}));
	}

	
	@Test
	public void convertSimpleArray() {
		Object[] myArray = new Object[]{0,1,2};
		Object newArray = ReflectUtility.convertArrayType(myArray, int.class);
		Assert.assertTrue("Testing the class of the array", newArray.getClass().getName().equals("[I"));
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals("Testing the values of the array", Array.getInt(newArray, i), i);
		}
	}
	
	@Test
	public void convertTwoDimensionArray() {
		Object[][] myArray = new Object[2][2];
		for (int i = 0; i < myArray.length; i++) {
			for (int j = 0; j < myArray[i].length; j++) {
				myArray[i][j] = i + j;
			}
		}
		Object newArray = ReflectUtility.convertArrayType(myArray, int.class);
		Assert.assertTrue("Testing the class of the array", newArray.getClass().getName().equals("[[I"));
		for (int i = 0; i < Array.getLength(newArray); i++) {
			for (int j = 0; j < Array.getLength(Array.get(newArray, 0)); j++) {
				Assert.assertEquals("Testing the values of the array", Array.getInt(Array.get(newArray, i), j), i + j);
			}
		}
	}
	
	
}
