package repicea.io.tools;

import static org.junit.Assert.assertEquals;

import java.awt.Window;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;

import org.junit.Assert;
import org.junit.Test;

import repicea.gui.REpiceaGUITestRobot;
import repicea.io.FileUtility;
import repicea.io.ImportTest;
import repicea.lang.REpiceaSystem;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.simulation.UseModeProvider.UseMode;
import repicea.util.ObjectUtility;

public class ImportToolTest {

	static {
		XmlSerializerChangeMonitor.registerClassNameChange("repicea.test.tools.TestRecordReader$FieldID", "repicea.io.tools.RecordReaderImpl$FieldID");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStrataFileReading() throws Exception {
		String sourcePath = ObjectUtility.getPackagePath(ImportTest.class) + "TEST6152.accdb";
		
		String targetPath = REpiceaSystem.getJavaIOTmpDir() + "TEST6152.accdb";
		String testIfe = ObjectUtility.getPackagePath(RecordReaderImpl.class) + "test.ife";
		String results = ObjectUtility.getPackagePath(RecordReaderImpl.class) + "result1.xml";
				
		if (!FileUtility.copy(sourcePath, targetPath)) {
			throw new IOException("Unable to copy the database file to tmp directory!");
		}

		RecordReaderImpl recordReader = new RecordReaderImpl();
		ImportFieldManager ifm = ImportFieldManager.createImportFieldManager(testIfe, targetPath, "TEST6152");
		recordReader.initInScriptMode(ifm);
		List<String> strataList = recordReader.getGroupList();
//		XmlSerializer serializer = new XmlSerializer(results);
//		serializer.writeObject(strataList);
		XmlDeserializer deserializer = new XmlDeserializer(results);
		List<String>  refStrataList = (List<String>) deserializer.readObject();
		assertEquals("Testing for number of elements", refStrataList.size(), strataList.size());
		for (int i = 0; i < strataList.size(); i++) {
			assertEquals("Testing for group name at index " + ((Integer) i).toString(), 
					refStrataList.get(i),
					strataList.get(i));
		}
	}

	
	/*
	 * Rerun the same test than above but using the csv file instance + it tests 
	 * the resetting of the CSVReader within the ImportFieldManager instance
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testResettingFormatReader() throws Exception {
		String sourcePath = ObjectUtility.getPackagePath(ImportTest.class) + "TEST6152.csv";
		
		String testIfe = ObjectUtility.getPackagePath(RecordReaderImpl.class) + "test.ife";
		String results = ObjectUtility.getPackagePath(RecordReaderImpl.class) + "result1.xml";
				
		RecordReaderImpl recordReader = new RecordReaderImpl();
		ImportFieldManager ifm = ImportFieldManager.createImportFieldManager(testIfe, sourcePath);
		recordReader.initInScriptMode(ifm);
		List<String> strataList = recordReader.getGroupList();
//		XmlSerializer serializer = new XmlSerializer(results);
//		serializer.writeObject(strataList);
		XmlDeserializer deserializer = new XmlDeserializer(results);
		List<String>  refStrataList = (List<String>) deserializer.readObject();
		assertEquals("Testing for number of elements", refStrataList.size(), strataList.size());
		for (int i = 0; i < strataList.size(); i++) {
			assertEquals("Testing for group name at index " + ((Integer) i).toString(), 
					refStrataList.get(i),
					strataList.get(i));
		}
		recordReader.readAllRecords();
		Assert.assertEquals("Testing nb records read", 3647, recordReader.nbRecordsRead);
		recordReader.getImportFieldManager().getFormatReader().reset();
		recordReader.readAllRecords();
		Assert.assertEquals("Testing nb records read", 3647 * 2, recordReader.nbRecordsRead);
	}

	
	/*
	 * Rerun the same test than above but using the csv file instance + it tests 
	 * the resetting of the CSVReader within the ImportFieldManager instance
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testWithGUI() throws Exception {
		String sourcePath = ObjectUtility.getPackagePath(ImportTest.class) + "TEST6152.csv";
		String results = ObjectUtility.getPackagePath(RecordReaderImpl.class) + "result1.xml";
			
		RecordReaderImpl recordReader = new RecordReaderImpl();
		
		Runnable toRun = new Runnable() {
			public void run() {
				try {
					recordReader.initGUIMode(UseMode.GUI_MODE, sourcePath);
				} catch(Exception e) {}
			}
		};
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		Thread t = robot.startGUI(toRun, ImportFieldManagerDialog.class);
		robot.clickThisButton("Ok");
		robot.clickThisButton("Cancel");
		t.join();
		robot.shutdown();
		
		List<String> strataList = recordReader.getGroupList();
		assertEquals("Testing for number of elements", 212, strataList.size());
		recordReader.readAllRecords();
		Assert.assertEquals("Testing nb records read", 3647, recordReader.nbRecordsRead);
		recordReader.getImportFieldManager().getFormatReader().reset();
		recordReader.readAllRecords();
		Assert.assertEquals("Testing nb records read", 3647 * 2, recordReader.nbRecordsRead);
	}

	
}
