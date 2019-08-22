package repicea.stats.data;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;

public class PatternCorrectionTests {

	@Test
	public void testStatusCorrection() throws IOException {
		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "test") + "statusCorrected.csv";
		String refFilename = ObjectUtility.getPackagePath(getClass()).replace("bin", "test") + "statusCorrectedRef.csv";
		
		CSVReader reader = new CSVReader(filename);
		CSVReader refReader = new CSVReader(refFilename);
		Object[] record, refRecord;

		Assert.assertEquals("Testing number of records in each file", refReader.getRecordCount(), reader.getRecordCount()); 
		int i = 0;
		while ((record = reader.nextRecord()) != null) {
			refRecord = refReader.nextRecord();
			compareRecord(refRecord, record);
			i++;
		}
		reader.close();
		refReader.close();
		System.out.println("Number of records successfully tested (should be 1 676 351) = " + i);
	}

	private void compareRecord(Object[] refRecord, Object[] record) {
		Assert.assertEquals("Testing number of fields in each record", refRecord.length, record.length); 
		for (int i = 0; i < refRecord.length; i++) {
			Assert.assertEquals("Testing individual fields", refRecord[i], record[i]);
		}
	}
}
