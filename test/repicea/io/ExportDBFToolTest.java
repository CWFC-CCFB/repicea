package repicea.io;

import java.util.Vector;

import repicea.io.tools.ExportTool;

@SuppressWarnings("deprecation")
public class ExportDBFToolTest extends ExportTool {


	
	
	public enum Allo {allo, byebye, bonjour};
	
	protected ExportDBFToolTest() throws Exception {
		super();
	}

	
	@SuppressWarnings("rawtypes")
	@Override
	protected Vector<Enum> defineAvailableExportOptions() {
		Vector<Enum> exportOptions = new Vector<Enum>();
		for (Allo var : Allo.values()) {
			exportOptions.add(var);
		}
		return exportOptions;
	}


	public static void main(String[] args) {
		ExportDBFToolTest test;
		try {
			test = new ExportDBFToolTest();
			test.showUI(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}


	@SuppressWarnings("rawtypes")
	@Override
	protected InternalSwingWorkerForRecordSet instantiateInternalSwingWorkerForRecordSet(Enum selectedOption) {
		return null;
	}


	
	
}
