package repicea.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.Assert;

public class StringSplitterTests {

	@Test
	public void simpleStringTest() {
		String lineRead = "a,b,c,d,e";
		List<String> referenceStrings = new ArrayList<String>();
		referenceStrings.add("a");
		referenceStrings.add("b");
		referenceStrings.add("c");
		referenceStrings.add("d");
		referenceStrings.add("e");
		List<String> splitStrings = ObjectUtility.splitLine(lineRead, ",");
		Assert.assertTrue(referenceStrings.equals(splitStrings));
	}

	@Test
	public void simpleTestWithTwoBlanks() {
		String lineRead = "a,,,d,e";
		List<String> referenceStrings = new ArrayList<String>();
		referenceStrings.add("a");
		referenceStrings.add("");
		referenceStrings.add("");
		referenceStrings.add("d");
		referenceStrings.add("e");
		List<String> splitStrings = ObjectUtility.splitLine(lineRead, ",");
		Assert.assertTrue(referenceStrings.equals(splitStrings));
	}

	@Test
	public void simpleTestWithTokenAsString() {
		String lineRead = "a,,,\"d,\"e\"";
		List<String> referenceStrings = new ArrayList<String>();
		referenceStrings.add("a");
		referenceStrings.add("");
		referenceStrings.add("");
		referenceStrings.add("d,\"e");
		List<String> splitStrings = ObjectUtility.splitLine(lineRead, ",");
		Assert.assertTrue(referenceStrings.equals(splitStrings));
	}

	@Test
	public void simpleTestWithTokenAsString2() {
		String lineRead = "\"a,\",,d,e";
		List<String> referenceStrings = new ArrayList<String>();
		referenceStrings.add("a,");
		referenceStrings.add("");
		referenceStrings.add("d");
		referenceStrings.add("e");
		List<String> splitStrings = ObjectUtility.splitLine(lineRead, ",");
		Assert.assertTrue(referenceStrings.equals(splitStrings));
	}

	@Test
	public void simpleTestWithTokenAsString3() {
		String lineRead = "\"a,\",,d,";
		List<String> referenceStrings = new ArrayList<String>();
		referenceStrings.add("a,");
		referenceStrings.add("");
		referenceStrings.add("d");
		referenceStrings.add("");
		List<String> splitStrings = ObjectUtility.splitLine(lineRead, ",");
		Assert.assertTrue(referenceStrings.equals(splitStrings));
	}

	
	@Test
	public void simpleTestWithTokenAsString4() {
		String lineRead = ",,,\"a,\",";
		List<String> referenceStrings = new ArrayList<String>();
		referenceStrings.add("");
		referenceStrings.add("");
		referenceStrings.add("");
		referenceStrings.add("a,");
		referenceStrings.add("");
		List<String> splitStrings = ObjectUtility.splitLine(lineRead, ",");
		Assert.assertTrue(referenceStrings.equals(splitStrings));
	}

	@Test
	public void simpleTestWithTokenAsString5() {
		String lineRead = ",,,\"a,\"";
		List<String> referenceStrings = new ArrayList<String>();
		referenceStrings.add("");
		referenceStrings.add("");
		referenceStrings.add("");
		referenceStrings.add("a,");
		List<String> splitStrings = ObjectUtility.splitLine(lineRead, ",");
		Assert.assertTrue(referenceStrings.equals(splitStrings));
	}

	
}
