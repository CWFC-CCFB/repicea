package repicea.io.tools;

import java.util.HashSet;
import java.util.Set;

import repicea.util.ObjectUtility;

public class REpiceaExportToolTests {

	
	
	
	
	
	public static void main(String[] args) {
		REpiceaExportToolImpl exportTool = new REpiceaExportToolImpl();
		String filename = ObjectUtility.getPackagePath(REpiceaExportToolTests.class) + "test.csv";
		exportTool.setFilename(filename);
		Set<Enum> options = new HashSet<Enum>();
		options.add(REpiceaExportToolImpl.ExportOptions.TheOnlyOne);
		try {
			exportTool.showInterface(null);
//			exportTool.setSelectedOptions(options);
//			exportTool.exportRecordSets();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int u = 0;
	}
	
}
