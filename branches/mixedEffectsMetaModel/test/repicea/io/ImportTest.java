package repicea.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.io.javadbf.DBFReader;
import repicea.io.javasql.SQLReader;
import repicea.lang.REpiceaSystem;
import repicea.util.ObjectUtility;

public class ImportTest {
	
	/**
	 * This test reads to copy of the same file: a DBF copy and a CSV copy. Each line read is compared across the files to make
	 * sure the readers read the same.
	 * @throws IOException
	 */
	@Test
	public void CSVReaderAndDBFReaderReadTheSameTest() throws IOException {
		String filePath = ObjectUtility.getPackagePath(ImportTest.class);
		DBFReader dbfReader = new DBFReader(filePath + "TEST6152.DBF");
		CSVReader csvReader = new CSVReader(filePath + "TEST6152.csv");
		assertEquals("Number of records", dbfReader.getRecordCount(), csvReader.getRecordCount());
		assertEquals("Number of fields", dbfReader.getFieldCount(), csvReader.getFieldCount());
		
		Object[] readDBF;
		Object[] readCSV;
		
		readDBF = dbfReader.nextRecord();
		readCSV = csvReader.nextRecord();
		int i = 0;
		while (readDBF != null || readCSV != null) {
			for (int j = 0; j < dbfReader.getFieldCount(); j++) {
				Object comparCSV;
				Object comparDBF;
				if (readDBF[j] instanceof Double) {
					comparDBF = readDBF[j];
					comparCSV = Double.parseDouble(readCSV[j].toString());
				} else {
					comparDBF = readDBF[j].toString().trim();
					comparCSV = readCSV[j].toString().trim();
					
				}
				assertEquals("Comparing record " + i + "; field no " + j, comparDBF, comparCSV);
			}
			readDBF = dbfReader.nextRecord();
			readCSV = csvReader.nextRecord();
		}
		dbfReader.close();
		csvReader.close();

	}
	
	/**
	 * This test reads to copy of the same file: a MS-ACCESS copy and a CSV copy. Each line read is compared across the files to make
	 * sure the readers read the same.
	 * @throws IOException
	 * .MDB NO LONGER SUPPORTED
	 */
	@Test
	public void CSVReaderAndSQLReaderReadTheSameTestAccessVersion() throws IOException {
		String filePath = ObjectUtility.getPackagePath(ImportTest.class);
		
		String sourcePath = filePath + "TEST6152.accdb";
		String targetPath = REpiceaSystem.getJavaIOTmpDir() + "TEST6152.accdb";

		if (!FileUtility.copy(sourcePath, targetPath)) {
			throw new IOException("Unable to copy the database file to tmp directory!");
		}
		
		SQLReader sqlReader = new SQLReader(targetPath, "TEST6152");
		CSVReader csvReader = new CSVReader(filePath + "TEST6152.csv");
		assertEquals("Number of records", sqlReader.getRecordCount(), csvReader.getRecordCount());
		assertEquals("Number of fields", sqlReader.getFieldCount(), csvReader.getFieldCount());
		
		Object[] readSQL;
		Object[] readCSV;
		
		readSQL = sqlReader.nextRecord();
		readCSV = csvReader.nextRecord();
		int i = 0;
		while (readSQL != null || readCSV != null) {
			for (int j = 0; j < sqlReader.getFieldCount(); j++) {
				Object comparCSV;
				Object comparSQL;
				if (readSQL[j] instanceof Double) {
					comparSQL = readSQL[j];
					comparCSV = Double.parseDouble(readCSV[j].toString());
				} else {
					comparSQL = readSQL[j].toString().trim();
					comparCSV = readCSV[j].toString().trim();
				}
				assertEquals("Comparing record " + i + "; field no " + j, comparSQL, comparCSV);
			}
			readSQL = sqlReader.nextRecord();
			readCSV = csvReader.nextRecord();
		}
		sqlReader.close();
		csvReader.close();

	}

	
	
	/**
	 * This test reads to copy of the same file: a 2007 MS-ACCESS copy and a CSV copy. Each line read is compared across the files to make
	 * sure the readers read the same.
	 * @throws IOException
	 */
	@Test
	public void CSVReaderAndSQLReaderReadTheSameTest2007AccessVersion() throws IOException {
		String filePath = ObjectUtility.getPackagePath(ImportTest.class);
		
		String sourcePath = filePath + "TEST6152.accdb";
		String targetPath = REpiceaSystem.getJavaIOTmpDir() + "TEST6152.accdb";

		if (!FileUtility.copy(sourcePath, targetPath)) {
			throw new IOException("Unable to copy the database file to tmp directory!");
		}
		
		SQLReader sqlReader = new SQLReader(targetPath, "TEST6152");
		CSVReader csvReader = new CSVReader(filePath + "TEST6152.csv");
		assertEquals("Number of records", sqlReader.getRecordCount(), csvReader.getRecordCount());
		assertEquals("Number of fields", sqlReader.getFieldCount(), csvReader.getFieldCount());
		
		Object[] readSQL;
		Object[] readCSV;
		
		readSQL = sqlReader.nextRecord();
		readCSV = csvReader.nextRecord();
		int i = 0;
		while (readSQL != null || readCSV != null) {
			for (int j = 0; j < sqlReader.getFieldCount(); j++) {
				Object comparCSV;
				Object comparSQL;
				if (readSQL[j] instanceof Double) {
					comparSQL = readSQL[j];
					comparCSV = Double.parseDouble(readCSV[j].toString());
				} else {
					comparSQL = readSQL[j].toString().trim();
					comparCSV = readCSV[j].toString().trim();
				}
				assertEquals("Comparing record " + i + "; field no " + j, comparSQL, comparCSV);
			}
			readSQL = sqlReader.nextRecord();
			readCSV = csvReader.nextRecord();
		}
	
		sqlReader.close();
		csvReader.close();
	}

	
	
	/**
	 * This test write a copy of a VSC file and tests if it is identical to this original.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void ReaderAndWriterReadTheSameTest(String filename) throws IOException {
		GFileFilter fileFilter = GFileFilter.getFileType(filename).getFileFilter();
		String[] inputSpec = new String[2];
		String[] outputSpec = new String[2];

		String filePath = ObjectUtility.getPackagePath(ImportTest.class);
		String inputFilename = filePath + filename;
		String outputFilename = filePath + "tmp" + fileFilter.getExtension();
		inputSpec[0] = inputFilename;
		outputSpec[0] = outputFilename;
		if (fileFilter.equals(GFileFilter.ACCDB)) {
			String targetPath = REpiceaSystem.getJavaIOTmpDir() + "tmp.accdb";
			if (!FileUtility.copy(inputFilename, targetPath)) {
				throw new IOException("Unable to copy the database file to tmp directory!");
			}
			System.out.println("File copied");

			inputSpec[1] = "TEST6152";
			outputSpec[0] = targetPath;
			outputSpec[1] = "copy";
		} else if (fileFilter.equals(GFileFilter.MDB)) {
			String targetPath = REpiceaSystem.getJavaIOTmpDir() + "tmp.mdb";
			if (!FileUtility.copy(inputFilename, targetPath)) {
				throw new IOException("Unable to copy the database file to tmp directory!");
			}
			System.out.println("File copied");

			inputSpec[1] = "Sommaire_programme";
			outputSpec[0] = targetPath;
			outputSpec[1] = "copy";
		}
		
		FormatReader<?> reader = FormatReader.createFormatReader(inputSpec);
		FormatWriter<?> writer = FormatWriter.createFormatWriter(false, outputSpec);
		assertEquals("Number of records > 0", reader.getRecordCount() > 0, true);

		
		
		writer.setFields(reader.getHeader());
		
		Object[] read;
		
		while ((read = reader.nextRecord()) != null) {
			writer.addRecord(read);
		}

		reader.close();
		writer.close();
		
		FormatReader<?> reader1 = FormatReader.createFormatReader(inputSpec);
		FormatReader<?> reader2 = FormatReader.createFormatReader(outputSpec);

		
		assertEquals("Number of records", reader1.getRecordCount(), reader2.getRecordCount());
		assertEquals("Number of fields", reader1.getFieldCount(), reader2.getFieldCount());
		
		Object[] read1;
		Object[] read2;
		
		read1 = reader1.nextRecord();
		read2 = reader2.nextRecord();
		while (read1 != null || read2 != null) {
			CompareTwoRecords(read1, read2, reader1.getFieldCount());
			read1 = reader1.nextRecord();
			read2 = reader2.nextRecord();
		}
	}

	private static void CompareTwoRecords(Object[] read1, Object[] read2, int expectedFieldCount) {
		for (int j = 0; j < expectedFieldCount; j++) {
			Object comparCSV1;
			Object comparCSV2;
			if (read1[j] instanceof Double) {
				comparCSV1 = Double.parseDouble(read1[j].toString());
				comparCSV2 = Double.parseDouble(read2[j].toString());
			} else {
				comparCSV1 = read1[j].toString().trim();
				comparCSV2 = read2[j].toString().trim();
			}
			assertEquals("Comparing records", comparCSV1, comparCSV2);
		}
	}
	
	/**
	 * This test write a copy of a CSV file and tests if it is identical to this original.
	 * @throws IOException
	 */
	@Test
	public void CSVReaderAndWriterReadTheSameTest() throws IOException {
		ImportTest.ReaderAndWriterReadTheSameTest("TEST6152.csv");
	}
	

	/**
	 * This test write a copy of a DBF file and tests if it is identical to this original.
	 * @throws IOException
	 */
	@Test
	public void DBFReaderAndWriterReadTheSameTest() throws IOException {
		ImportTest.ReaderAndWriterReadTheSameTest("TEST6152.DBF");
	}
	
	/**
	 * This test write a copy of a MSACCESS file and tests if it is identical to this original.
	 * @throws IOException
	 * 
	 */
	@Ignore // just too long on Windows
	@Test
	public void MSACCESSReaderAndWriterReadTheSameTest() throws IOException {
		ImportTest.ReaderAndWriterReadTheSameTest("TEST6152.accdb");
	}
	
	@Test
	public void CSVReaderResetTest() throws IOException {
		String filePath = ObjectUtility.getPackagePath(ImportTest.class);
		String inputFilename = filePath + "TEST6152.csv";

		CSVReader reader = (CSVReader) FormatReader.createFormatReader(inputFilename);
		List<Object[]> firstRun = new ArrayList<Object[]>();
		for (int i = 0; i < 1000; i++) {
			firstRun.add(reader.nextRecord());
		}
		List<Object[]> secondRun = new ArrayList<Object[]>();
		reader.reset();
		for (int i = 0; i < 1000; i++) {
			secondRun.add(reader.nextRecord());
		}
		
		for (int i = 0; i < 1000; i++) {
			Object[] read1 = firstRun.get(i);
			Object[] read2 = secondRun.get(i);
			CompareTwoRecords(read1, read2, reader.getFieldCount());
		}
		
	}

	@Test
	public void DBFReaderResetTest() throws IOException {
		String filePath = ObjectUtility.getPackagePath(ImportTest.class);
		String inputFilename = filePath + "TEST6152.DBF";

		DBFReader reader = (DBFReader) FormatReader.createFormatReader(inputFilename);
		List<Object[]> firstRun = new ArrayList<Object[]>();
		for (int i = 0; i < 1000; i++) {
			firstRun.add(reader.nextRecord());
		}
		List<Object[]> secondRun = new ArrayList<Object[]>();
		reader.reset();
		for (int i = 0; i < 1000; i++) {
			secondRun.add(reader.nextRecord());
		}
		
		for (int i = 0; i < 1000; i++) {
			Object[] read1 = firstRun.get(i);
			Object[] read2 = secondRun.get(i);
			CompareTwoRecords(read1, read2, reader.getFieldCount());
		}
		
	}

	
	@Test
	public void SQLReaderResetTest() throws IOException {
		String filePath = ObjectUtility.getPackagePath(ImportTest.class);
		
		String sourcePath = filePath + "TEST6152.accdb";
		String targetPath = REpiceaSystem.getJavaIOTmpDir() + "TEST6152.accdb";

		if (!FileUtility.copy(sourcePath, targetPath)) {
			throw new IOException("Unable to copy the database file to tmp directory!");
		}
		
		SQLReader reader = (SQLReader) FormatReader.createFormatReader(targetPath, "TEST6152");
		List<Object[]> firstRun = new ArrayList<Object[]>();
		for (int i = 0; i < 1000; i++) {
			firstRun.add(reader.nextRecord());
		}
		List<Object[]> secondRun = new ArrayList<Object[]>();
		reader.reset();
		for (int i = 0; i < 1000; i++) {
			secondRun.add(reader.nextRecord());
		}
		
		for (int i = 0; i < 1000; i++) {
			Object[] read1 = firstRun.get(i);
			Object[] read2 = secondRun.get(i);
			CompareTwoRecords(read1, read2, reader.getFieldCount());
		}

		
	}
	
}
