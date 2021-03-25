package repicea.io.tools;

import java.util.ArrayList;
import java.util.List;

import repicea.gui.genericwindows.REpiceaProgressBarDialog;
import repicea.io.GExportFieldDetails;
import repicea.io.GExportRecord;
import repicea.io.REpiceaRecordSet;

class REpiceaExportToolImpl extends REpiceaExportTool {

	public static enum ExportOptions {
		TheOnlyOne,
		TheOtherOne;
	}
	
	
	REpiceaExportToolImpl() {
		super();
		setMultipleSelection(true);
	}
	
	@SuppressWarnings("serial")
	protected static class InternalSwingWorker extends InternalSwingWorkerForRecordSet {

		@SuppressWarnings("rawtypes")
		protected InternalSwingWorker(Enum exportOption, REpiceaRecordSet recordSet) {
			super(exportOption, recordSet);
		}

		@Override
		protected void doThisJob() throws Exception {
			GExportFieldDetails people = new GExportFieldDetails("Personne", "Mathieu");
			GExportFieldDetails age = new GExportFieldDetails("Age", 25);
			for (int i = 0; i < 200000; i++) {
				GExportRecord record = new GExportRecord();
				if (isCancelled()) {
					break;
				}
				record.addField(people);
				record.addField(age);
				addRecord(record);
				firePropertyChange(REpiceaProgressBarDialog.PROGRESS, 0d, (int) (i/2000d));
			}
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected List<Enum> defineAvailableExportOptions() {
		List<Enum> var = new ArrayList<Enum>();
		var.add(ExportOptions.TheOnlyOne);
		var.add(ExportOptions.TheOtherOne);
		return var;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected InternalSwingWorkerForRecordSet instantiateInternalSwingWorkerForRecordSet(Enum selectedOption, REpiceaRecordSet recordSet) {
		return new InternalSwingWorker(selectedOption, recordSet);
	}

}
