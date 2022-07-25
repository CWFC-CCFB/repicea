package repicea.math.utility;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.math.utility.GammaUtility;
import repicea.util.ObjectUtility;

public class GammaUtilityTest {

	@Test
	public void testValues() throws IOException {
		String filename = ObjectUtility.getPackagePath(getClass()) + "gammaTest.csv";
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		int i = 0;
		while ((record = reader.nextRecord()) != null) {
			double x = Double.parseDouble(record[1].toString());
			double expectedValue = Double.parseDouble(record[2].toString());
			double actualValue = GammaUtility.gamma(x);
			Assert.assertEquals("Testing observation" + i, expectedValue, actualValue, 1E-8);
			i++;
		}
		reader.close();
		System.out.println("GammaUtility successfully tested on " + i + " observations");
	}
	
	
	@Test
	public void testInverseGammaFunction() {
		for (double d = 2; d < 15; d+=0.5) {
			double gammaValue = GammaUtility.gamma(d);
			double actual = GammaUtility.inverseGamma(gammaValue);
			System.out.println("Expected = " + d + "; Gamma value = " + gammaValue + "; Actual = " + actual);
			double tolerance = 1E-2;
			if (d == 2) {
				tolerance = 2.5E-2;
			}
			Assert.assertEquals("Testing value d = " + d, d, actual, tolerance);
		}
	}
	
}
