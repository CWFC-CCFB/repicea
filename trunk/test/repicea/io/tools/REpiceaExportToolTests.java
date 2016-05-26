package repicea.io.tools;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.REpiceaRecordSet;
import repicea.util.ObjectUtility;

public class REpiceaExportToolTests {

	
	@SuppressWarnings("rawtypes")
	@Test
	public void simpleExportTest() {
		REpiceaExportToolImpl exportTool = new REpiceaExportToolImpl();
		String filename = ObjectUtility.getPackagePath(REpiceaExportToolTests.class) + "testExport.csv";
		exportTool.setFilename(filename);
		Set<Enum> options = new HashSet<Enum>();
		options.add(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
		try {
			exportTool.setSelectedOptions(options);
			exportTool.exportRecordSets();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	@Test
	public void simpleExportTestWithoutSaving() {
		REpiceaExportToolImpl exportTool = new REpiceaExportToolImpl();
		String filename = ObjectUtility.getPackagePath(REpiceaExportToolTests.class) + "testExport.csv";
		exportTool.setFilename(filename);
		Set<Enum> options = new HashSet<Enum>();
		options.add(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
		try {
			exportTool.setSaveFileEnabled(false);
			exportTool.setSelectedOptions(options);
			REpiceaRecordSet recordSet = exportTool.exportRecordSets().get(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
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
		}
	}
	
}
