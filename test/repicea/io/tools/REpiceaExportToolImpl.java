package repicea.io.tools;

import java.util.ArrayList;
import java.util.List;

import repicea.io.GExportFieldDetails;
import repicea.io.GExportRecord;
import repicea.io.REpiceaRecordSet;

public class REpiceaExportToolImpl extends REpiceaExportTool {

	public static enum ExportOptions {
		TheOnlyOne;
	}
	protected static class InternalSwingWorker extends InternalSwingWorkerForRecordSet {

		protected InternalSwingWorker(Enum exportOption, REpiceaRecordSet recordSet) {
			super(exportOption, recordSet);
		}

		@Override
		protected void doThisJob() throws Exception {
			GExportFieldDetails people = new GExportFieldDetails("Personne", "Mathieu");
			GExportFieldDetails age = new GExportFieldDetails("Age", 25);
			for (int i = 0; i < 200000; i++) {
				GExportRecord record = new GExportRecord();
				record.addField(people);
				record.addField(age);
				addRecord(record);
			}
		}
		
	}
	
	@Override
	protected List<Enum> defineAvailableExportOptions() {
		List<Enum> var = new ArrayList<Enum>();
		var.add(ExportOptions.TheOnlyOne);
		return var;
	}

	
	
	
	
	@Override
	protected InternalSwingWorkerForRecordSet instantiateInternalSwingWorkerForRecordSet(Enum selectedOption, REpiceaRecordSet recordSet) {
		return new InternalSwingWorker(selectedOption, recordSet);
	}

}
