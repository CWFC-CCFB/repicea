/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.io.tools;

import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import repicea.app.AbstractGenericTask;
import repicea.gui.REpiceaShowableUIWithParent;
import repicea.io.FormatField;
import repicea.io.FormatHeader;
import repicea.io.FormatWriter;
import repicea.io.GExportFieldDetails;
import repicea.io.GExportRecord;
import repicea.io.GFileFilter;
import repicea.io.GFileFilter.FileType;
import repicea.io.REpiceaRecordSet;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


/**
 * The REpiceaExportTool class is an abstract class that can save a file from particular record sets.
 * The UI interface of this class also provides an export option list and a file selection panel. The export 
 * options are defined in the abstract method defineExportOptions() while the record sets are built through the
 * createRecordSets(List<Enum> selectedExportOptions) method. By default, the format is dbf, the user can select 
 * other format though. </br>
 * </br>
 * Each record set is given a worker that computes the record and passes them to an associated thread that write
 * them down in a file. The first worker has a queue from which the saving thread picks the records. Consequently,
 * the exportRecordSets method returns a series of empty REpiceaRecordSet instances. If the saving is disabled 
 * through the setSaveFileEnabled method, then the exportRecordSets method returns a series of complete 
 * REpiceaRecordSet instances. </br>
 * </br>
 * To use this class, first define a derived class that implements the two abstract methods. To export a dbf file,
 * first instantiate an object of the derived class. Then use the three following method: </br>
 * </br>
 * {@code setFilename(myFile);} </br>
 * {@code setSelectedOptions(mySelectedOptions);} </br>
 * {@code exportRecordSets();} </br>
 * 
 * @author Mathieu Fortin - April 2016 
 * @author Mathieu Fortin - refactoring September 2020 
 * 
 */
public abstract class REpiceaExportTool implements REpiceaShowableUIWithParent, CaretListener, ListSelectionListener {
	
	final class InternalWorkerForSaveMethod extends Thread {

		private final File file;
		private final REpiceaRecordSet recordSet;
		private Exception failure;
		
		protected InternalWorkerForSaveMethod(File file, REpiceaRecordSet recordSet) {
			super("Export tool save-to-file thread");
			this.file = file;
			this.recordSet = recordSet;
			this.recordSet.setSaveThread(this);
		}
		
		public void run() {
			try {
				doThisJob();
			} catch (Exception e) {
				failure = e;
			}
		}
		
		protected void doThisJob() throws Exception {
			GExportRecord record;
			GExportRecord refRecord = null;
			FormatWriter<? extends FormatHeader<? extends FormatField>> formatWritter = null;
			try {
				formatWritter = FormatWriter.createFormatWriter(REpiceaExportTool.this.isAppendFileEnabled(), file.getAbsolutePath());    //using sync mode constructor
	
				int nbFields = -1;
				while (!(record = recordSet.take()).equals(REpiceaExportTool.this.finalRecordForClosingFile)) {
					if (refRecord == null) {
						refRecord = record;
						nbFields = refRecord.getFieldList().size();

						List<FormatField> aoFields = new ArrayList<FormatField>();

						for (int fieldID = 0; fieldID < nbFields; fieldID++) {
							GExportFieldDetails fieldDetails = refRecord.getFieldList().get(fieldID);
							FormatField formatField = formatWritter.convertGExportFieldDetailsToFormatField(fieldDetails);
							aoFields.add(formatField);
						}

						if (!REpiceaExportTool.this.isAppendFileEnabled()) {
							formatWritter.setFields(aoFields);
						}
					}

					Object rowData[] = new Object[nbFields];

					List<GExportFieldDetails> vTmp = record.getFieldList();

					for (int fieldID = 0; fieldID < vTmp.size(); fieldID++) {
						Object value = vTmp.get(fieldID).getValue();
						if (value instanceof Number) {
							rowData[fieldID] = (double) ((Number) value).doubleValue();
						} else if (value instanceof String) {
							rowData[fieldID] = (String) value;
						}
					}

					formatWritter.addRecord(rowData);
			
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			} finally {
				if (formatWritter != null) {
					try {
						formatWritter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void terminate() throws Exception {
			if (isAlive()) {
				recordSet.add(finalRecordForClosingFile);
			} else {
				throw new Exception("The save thread has crashed before the end!");
			}
		}
	
	}
	
	/**
	 * The abstract InternalSwingWorkerForRecordSet class is derived from the AbstractGenericTask class.
	 * Derived classes from this one have to handle the construction of the GRecordSet through the doThisJob() method.
	 * The protected setRecordSet(GRecordSet recordSet) method should be used to keep the just built GRecordSet instance 
	 * in a variable of this class.
	 * @author Mathieu Fortin - April 2012
	 */
	@SuppressWarnings("serial")
	public abstract static class InternalSwingWorkerForRecordSet extends AbstractGenericTask {
		
		@SuppressWarnings("rawtypes")
		private final Enum exportOption;
		private InternalWorkerForSaveMethod saveThread;
		private final REpiceaRecordSet recordSet;
		
		@SuppressWarnings("rawtypes")
		protected InternalSwingWorkerForRecordSet(Enum exportOption, REpiceaRecordSet recordSet) {
			this.exportOption = exportOption;
			this.recordSet = recordSet;
		}

		protected final void addSaveThread(InternalWorkerForSaveMethod saveThread) {
			this.saveThread = saveThread;
		}
		
		@SuppressWarnings("rawtypes")
		protected Enum getExportOption() {return exportOption;}

		protected final void addRecord(GExportRecord record) throws Exception {
			if (saveThread != null && !saveThread.isAlive()) {
				throw new Exception("The save thread has crashed!"); 
			}
			recordSet.add(record);
		}
		
		protected final void addRecordSet(REpiceaRecordSet incomingRecordSet) throws Exception {
			if (saveThread != null && !saveThread.isAlive()) {
				throw new Exception("The save thread has crashed!"); 
			}
			recordSet.addAll(incomingRecordSet);
		}
		
		protected final InternalWorkerForSaveMethod getSaveThread() {return saveThread;}
		
		protected final void terminateSaveThreadIfAny() throws Exception {
			if (getSaveThread() != null) {
				getSaveThread().terminate();
			}
		}
	}
	
	
	/**
	 * The FieldName enum class defines the field names during the export.
	 * @author Mathieu Fortin - November 2012
	 */
	public static enum FieldName implements TextableEnum {
		StratumID("StratumID", "StrateID"),
		Year("Year", "Annee"),
		MonteCarloIteration("IterMC", "IterMC"),
		PlotID("PlotID", "PlacetteID"),
		PlotAreaHa("PlotAreaHa", "PlacetteSurfHa"),
		PlotWeight("Weight", "Poids"),
		Residual("Residual", "Residuel"),
		TreeID("TreeID", "ArbreID"),
		Species("Species", "Espece"),
		SpeciesGroup("GrSpecies", "GrEspece"),
		Status("Status", "Etat"),
		Number("Number", "Nombre"),
		TreeDBH("DBHcm", "DHPcm"),
		TreeHeight("Heightm", "Hautm"),
		TreeBasalArea("BA_m2", "ST_m2"),
		TreeVolume("Vol_dm3", "Vol_dm3"),
		StandDensity("nbStems_HA", "nbTi_HA"),
		StandBasalArea("BA_HA", "ST_HA"),
		StandMQD("MQDiameter", "DMQ"),
		StandVolume("Vol_HA", "Vol_HA"),
		TotalStandVolume("VolTotHA", "VolTotHA"),
		DominantHeight("DomHeightM", "HDomM"),
		NumberOfPlots("NbPlots", "NbPlac"),
		paiSurvivor("PAISurvM2Ha", "AACSurvM2Ha"),
		paiMortality("PAIMortM2Ha", "AACMortM2Ha"),
		paiRecruitment("PAIRecrM2Ha", "AACRecrM2Ha"),
		NumberOfDecades("nbDecades", "nbDecen"),
		SpruceBudworm("SBW", "TBE"),
		TreatmentType("treatType", "traitType"),
		PesticideSpraying("InsSpray", "EpandPest"),
		SiteIndex("SiteIndexM", "SiteIndexM"),
		AboveGroundBiomass("AbGrBiom", "AbGrBiom"),
		AverageProduction("ProductivityM3HaYr","ProductiviteM3HaAn");
		

		FieldName(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		
	}

	
	private final GExportRecord finalRecordForClosingFile = new GExportRecord();
	
	private String filename;
	private boolean isCanceled;

	@SuppressWarnings("rawtypes")
	private List<Enum> availableExportOptions;
	@SuppressWarnings("rawtypes")
	protected List<Enum> selectedExportOptions;
	
	protected transient REpiceaExportToolDialog guiInterface;
	
	protected boolean multipleSelection;
	protected boolean saveFileEnabled = true;
	
	protected boolean appendFileEnabled = false;			// by default this option is set to false

	/**
	 * General constructor.
	 * @param multipleSelection true to enable the multiple selection list in the user interface.
	 */
	@SuppressWarnings("rawtypes")
	protected REpiceaExportTool(boolean multipleSelection) {
		this.multipleSelection = multipleSelection;
		selectedExportOptions = new ArrayList<Enum>();
		availableExportOptions = defineAvailableExportOptions();	
		if (availableExportOptions == null || availableExportOptions.isEmpty()) {
			throw new InvalidParameterException("There is no valid export option!");
		}
	}

	/**
	 * This method makes it possible to enable or disable the multiple export.
	 * @param bool a boolean
	 */
	public void setMultipleSelection(boolean bool) {
		multipleSelection = bool;
	}

	/**
	 * General constructor with multiple selection mode set to false.
	 */
	protected REpiceaExportTool() {
		this(false);
	}
	
	
	
	/**
	 * This method sets the available export options. It is to be defined in the derived classes.
	 * @return a vector that contains the different export options
	 */
	@SuppressWarnings("rawtypes")
	protected abstract List<Enum> defineAvailableExportOptions();	
	
	/**
	 * This method instantiates a InternalSwingWorkerForRecordSet-derived class, which handles the construction
	 * of the GRecordSet instance. If the worker does not terminate correctly, an exception is thrown.
	 * @param selectedOptions a List of Enum instances
	 * @return a GRecordSet instance
	 * @throws Exception if the worker does not terminate correctly
	 */
	@SuppressWarnings("rawtypes")
	protected final Map<Enum, REpiceaRecordSet> createRecordSets(List<Enum> selectedOptions) throws Exception {
		Map<Enum, InternalSwingWorkerForRecordSet> workers = new LinkedHashMap<Enum, InternalSwingWorkerForRecordSet>();
		Map<Enum, REpiceaRecordSet> recordSetMap = new LinkedHashMap<Enum, REpiceaRecordSet>();
		
		REpiceaRecordSet recordSet;
		InternalSwingWorkerForRecordSet buildRecordSetWorker;
		
		for (Enum selectedOption : selectedOptions) {
			if (!availableExportOptions.contains(selectedOption)) {
				throw new InvalidParameterException("Export option " + selectedOption.name() + " is not recognized!");
			}
			
			recordSet = new REpiceaRecordSet();
			buildRecordSetWorker = instantiateInternalSwingWorkerForRecordSet(selectedOption, recordSet);
			workers.put(selectedOption, buildRecordSetWorker);
			recordSetMap.put(selectedOption, recordSet);
			InternalWorkerForSaveMethod saveRecordSetWorker = null;
			if (saveFileEnabled) {
				saveRecordSetWorker = new InternalWorkerForSaveMethod(getExportFilename(selectedOption), recordSet);
				saveRecordSetWorker.start();
				buildRecordSetWorker.addSaveThread(saveRecordSetWorker);
			}
		}

		if (guiInterface != null && guiInterface.isVisible()) {
			// will be executed in the EventDispatchThread but the window will block because it is modal
			guiInterface.showProgressBar(workers, true);	// true : is creating dataset
		} else {
			for (InternalSwingWorkerForRecordSet worker : workers.values()) {
				worker.run();	// is executed in the current thread
			}
		}
		
		
		
		for (Enum selectedOption : selectedOptions) {
			recordSet = recordSetMap.get(selectedOption);
			buildRecordSetWorker = workers.get(selectedOption);
			
//			if (guiInterface != null && guiInterface.isVisible()) {
//				// will be executed in the EventDispatchThread but the window will block because it is modal
//				guiInterface.showProgressBar(buildRecordSetWorker, selectedOption, true);	// true : is creating dataset
//			} else {
//				buildRecordSetWorker.run();	// is executed in the current thread
//			}

			if (!buildRecordSetWorker.isCorrectlyTerminated()) {
				recordSet.clear();
				buildRecordSetWorker.terminateSaveThreadIfAny();
				throw buildRecordSetWorker.getFailureReason();
			} else if (buildRecordSetWorker.hasBeenCancelled()) {
				recordSet.clear();
				buildRecordSetWorker.terminateSaveThreadIfAny();
				throw new CancellationException();
			}

			if (saveFileEnabled) {
				buildRecordSetWorker.terminateSaveThreadIfAny();
				InternalWorkerForSaveMethod saveRecordSetWorker = buildRecordSetWorker.getSaveThread();
				saveRecordSetWorker.join();
				if (saveRecordSetWorker.failure != null) {
					throw saveRecordSetWorker.failure;
				}
			}
		}
		
		return recordSetMap;
	}
	

	/**
	 * This abstract method instantiates an InternalSwingWorkerForRecordSet-derived class, which is supposed to
	 * handle the construction of the GRecordSet instance.
	 * @param recordSet a REpiceaRecordSet instance
	 * @param selectedOption the selected output format
	 * @return the InternalSwingWorkerForRecordSet-derived instance
	 */
	@SuppressWarnings("rawtypes")
	protected abstract InternalSwingWorkerForRecordSet instantiateInternalSwingWorkerForRecordSet(Enum selectedOption, REpiceaRecordSet recordSet);
	
	
	@SuppressWarnings("rawtypes")
	protected List<Enum> getSelectedExportOptions() {return selectedExportOptions;}
	
	/**
	 * This method sets the export options. If the selectedOption Enum variable is not part of the 
	 * available export options, an exception is thrown. If multiple selections are not allowed, a 
	 * list of two or more Enum will throw an exception. The multiple selection mode can be enabled 
	 * through the setMultipleSelection method.
	 * @param selectedOptions a set of Enum variables that should be among the available export options
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public void setSelectedOptions(List<Enum> selectedOptions) throws Exception {
		selectedExportOptions.clear();
		for (Enum selectedOption : selectedOptions) {
			if (availableExportOptions.contains(selectedOption)) {
				selectedExportOptions.add(selectedOption);
				if (!multipleSelection && selectedExportOptions.size() > 1) {
					throw new InvalidParameterException("The export tool was not set for multiple selection!");
				}
			} else {
				selectedExportOptions.clear();
				throw new Exception("This export option is not compatible!");
			}
		}
	}

	/**
	 * This method makes it possible to remove one export option if desired. The method is protected
	 * against the removal of all options: the last one will always remain. If the specified export option
	 * is not found in the available export option nothing happens.
	 * @param exportOption an enum variable that corresponds to the export option to be removed
	 */
	@SuppressWarnings("rawtypes")
	protected void removeExportOption(Enum exportOption) {
		if (availableExportOptions.contains(exportOption)) {
			if (availableExportOptions.size() > 1) {
				if (selectedExportOptions.contains(exportOption)) {
					selectedExportOptions.remove(exportOption);
				}
				availableExportOptions.remove(exportOption);
			}
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	protected List<Enum> getAvailableExportOptions() {return availableExportOptions;}
	
	protected boolean isAppendFileEnabled() {return appendFileEnabled;}	
	
	/**
	 * If set to true, the export tool does not delete the file but rather it appends the records
	 * to the existing file.
	 * @param appendFileEnabled
	 */
	public void setAppendFileEnabled(boolean appendFileEnabled) {this.appendFileEnabled = appendFileEnabled;}	
	
	protected void setCanceled(boolean isCanceled) {this.isCanceled = isCanceled;}
	
	/**
	 * Returns true if the cancel button of the dialog has been pressed.
	 * @return a boolean
	 */
	public boolean isCanceled() {return isCanceled;}

	/**
	 * This method sets the filename of the output file.
	 * @param filename a String
	 * @throws IOException if the file type is unknown
	 */
	public void setFilename(String filename) {
		if (!filename.trim().isEmpty() && GFileFilter.getFileType(filename) == FileType.UNKNOWN) {
			filename += GFileFilter.CSV.getExtension();
		} 
		this.filename = filename;
	}
	
	protected String getFilename() {
		return filename;
	}
	

	@Override
	public REpiceaExportToolDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new REpiceaExportToolDialog(this, (Window) parent);
		}
		return guiInterface;
	}

	@Override
	public void showUI(Window parent) {
		if (!getUI(parent).isVisible()) {
			guiInterface.setVisible(true);
		}
	}
	
	@Override
	public void caretUpdate(CaretEvent evt) {
		if (evt.getSource().equals(guiInterface.filenameField)) {
			setFilename(guiInterface.filenameField.getText());
			guiInterface.refreshTitle();
		}
	}

	@SuppressWarnings({ "rawtypes", "deprecation"})
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		if (arg0.getSource().equals(guiInterface.optionList)) {
			Object[] objs = guiInterface.optionList.getSelectedValues();
			if (objs.length == 0) {		// means no options are selected, then the option that was previously selected is selected again to prevent a "no option situation"
				Enum lastSelectedValue = selectedExportOptions.iterator().next();
				guiInterface.optionList.setSelectedIndex(availableExportOptions.indexOf(lastSelectedValue));
			} else {
				selectedExportOptions.clear();
				List<Enum> selectedOptions = new ArrayList<Enum>();
				for (Object selectedValue : objs) {
					selectedOptions.add((Enum) selectedValue);  
				}
				try {
					setSelectedOptions(selectedOptions);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * This method creates the different record sets depending on the selected export options. If 
	 * the saving is enabled, and it is by default, then the method returns a series of empty
	 * REpiceaRecordSet instance. These are empty because the saving thread picks all the records
	 * to write them into a file. If the saving is disabled, then the REpiceaRecordSet instances are
	 * full.
	 * @throws Exception
 	 * @return a Map with the selected options (keys) and their associated record sets (values)
	 */
	@SuppressWarnings("rawtypes")
	public Map<Enum, REpiceaRecordSet> exportRecordSets() throws Exception {
//		Map<Enum, REpiceaRecordSet> outputMap = new HashMap<Enum, REpiceaRecordSet>();
//		for (Enum selectedOutputOption : selectedExportOptions) {
//			outputMap.put(selectedOutputOption, createRecordSet(selectedOutputOption));
//		}
//		return outputMap;
		return createRecordSets(selectedExportOptions);
	}
	
	
	@SuppressWarnings("rawtypes")
	private File getExportFilename(Enum exportOption) {
		if (saveFileEnabled) {
			if (selectedExportOptions.size() == 1) {
				return new File(getFilename());
			} else {
				int indexOfLastDot = getFilename().lastIndexOf(".");
				String extension = getFilename().substring(indexOfLastDot, getFilename().length()).trim();
				String originalFilename = getFilename().substring(0, indexOfLastDot).trim();
				String optionType = exportOption.name().trim();
				return new File(originalFilename + optionType + extension);
			}
		} else {
			return null;
		}
	}
	
	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	/**
	 * This method makes it possible to disable the saving to file. Then the Map that results from exportRecordSets() won't be empty.
	 * @param bool a boolean
	 */
	protected void setSaveFileEnabled(boolean bool) {
		saveFileEnabled = bool;
	}
}
