package repicea.io.tools;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.REpiceaRecordSet;
import repicea.util.ObjectUtility;

public class REpiceaExportToolTest {

	
	@SuppressWarnings("rawtypes")
	@Test
	public void simpleExportTest() {
		REpiceaExportToolImpl exportTool = new REpiceaExportToolImpl();
		String filename = ObjectUtility.getPackagePath(REpiceaExportToolTest.class) + "testExport.csv";
		exportTool.setFilename(filename);
		List<Enum> options = new ArrayList<Enum>();
		options.add(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
		try {
			exportTool.setSelectedOptions(options);
			exportTool.exportRecordSets();
			REpiceaRecordSet recordSet = exportTool.exportRecordSets().get(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
			Assert.assertEquals("Testing if the map is empty (saving was enabled)", 0, recordSet.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	@Test
	public void simpleExportTestWithoutSaving() {
		REpiceaExportToolImpl exportTool = new REpiceaExportToolImpl();
		String filename = ObjectUtility.getPackagePath(REpiceaExportToolTest.class) + "testExport.csv";
		exportTool.setFilename(filename);
		List<Enum> options = new ArrayList<Enum>();
		options.add(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
		try {
			exportTool.setSaveFileEnabled(false);
			exportTool.setSelectedOptions(options);
			REpiceaRecordSet recordSet = exportTool.exportRecordSets().get(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
			Assert.assertEquals("Testing if the record set is still full (saving was disabled)", 200000, recordSet.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	
	@SuppressWarnings("rawtypes")
	@Test
	public void exportTestWithTwoOptions() {
		REpiceaExportToolImpl exportTool = new REpiceaExportToolImpl();
		String filename = ObjectUtility.getPackagePath(REpiceaExportToolTest.class) + "testExport.csv";
		exportTool.setFilename(filename);
		List<Enum> options = new ArrayList<Enum>();
		options.add(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
		options.add(REpiceaExportToolImpl.ExportOptions.TheOtherOne);
		try {
			exportTool.setSelectedOptions(options);
			REpiceaRecordSet recordSet = exportTool.exportRecordSets().get(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
			Assert.assertEquals("Testing if the map is empty (saving was enabled)", 0, recordSet.size());
			recordSet = exportTool.exportRecordSets().get(REpiceaExportToolImpl.ExportOptions.TheOtherOne);
			Assert.assertEquals("Testing if the map is empty (saving was enabled)", 0, recordSet.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	@Test
	public void exportTestWithTwoOptionsWithoutSaving() {
		REpiceaExportToolImpl exportTool = new REpiceaExportToolImpl();
		String filename = ObjectUtility.getPackagePath(REpiceaExportToolTest.class) + "testExport.csv";
		exportTool.setFilename(filename);
		List<Enum> options = new ArrayList<Enum>();
		options.add(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
		options.add(REpiceaExportToolImpl.ExportOptions.TheOtherOne);
		try {
			exportTool.setSaveFileEnabled(false);
			exportTool.setSelectedOptions(options);
			REpiceaRecordSet recordSet = exportTool.exportRecordSets().get(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
			Assert.assertEquals(200000, recordSet.size());
			recordSet = exportTool.exportRecordSets().get(REpiceaExportToolImpl.ExportOptions.TheOtherOne);
			Assert.assertEquals(200000, recordSet.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	public static void main(String[] args) {
		REpiceaExportToolImpl exportTool = new REpiceaExportToolImpl();
		try {
			exportTool.showUI(null);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
	
}
