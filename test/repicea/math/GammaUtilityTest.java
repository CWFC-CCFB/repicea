package repicea.math;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
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
			double actualValue = GammaFunction.gamma(x);
			Assert.assertEquals("Testing observation" + i, expectedValue, actualValue, 1E-8);
			i++;
		}
		reader.close();
		System.out.println("GammaUtility successfully tested on " + i + " observations");
	}
}
