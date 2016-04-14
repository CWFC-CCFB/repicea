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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import repicea.app.AbstractGenericTask;
import repicea.gui.ShowableObjectWithParent;
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
 * The UI interface of this class also provides an export option list and a file selection panel. The export options are 
 * defined in the abstract method defineExportOptions() while the record sets are built through the
 * setRecordSet(Enum selectedExportOption) method. By default, the format is dbf, the user can select other format though. </br>
 * To use this class, first define a derived class that implements the two abstract methods. To export a dbf file,
 * first instantiate an object of the derived class. Then use the three following method: </br>
 * </br>
 * {@code setFilename(myFile);} </br>
 * {@code setSelectedOptions(mySelectedOptions);} </br>
 * {@code exportRecordSets();} </br>
 * @author Mathieu Fortin - April 2016
 */
public abstract class REpiceaExportTool implements ShowableObjectWithParent, CaretListener, ListSelectionListener {
	
	final class InternalWorkerForSaveMethod extends Thread {

		private final File file;
		private final REpiceaRecordSet recordSet;
		private Exception failure;
		
		protected InternalWorkerForSaveMethod(File file, REpiceaRecordSet recordSet) {
			super("Export tool save-to-file thread");
			this.file = file;
			this.recordSet = recordSet;
		}
		
		public void run() {
			try {
				doThisJob();
			} catch (Exception e) {
				failure = e;
			}
		}
		
		protected void doThisJob() throws Exception {
			
			FormatWriter<? extends FormatHeader<? extends FormatField>> formatWritter = null;
			try {
				formatWritter = FormatWriter.createFormatWriter(REpiceaExportTool.this.isAppendFileEnabled(), file.getAbsolutePath());    //using sync mode constructor
	
				GExportRecord record;
				GExportRecord refRecord = null;
				int nbFields = -1;
				while (!(record = recordSet.take()).equals(REpiceaExportTool.this.finalRecordForClosingFile)) {
					if (refRecord == null) {
						refRecord = recordSet.peek();
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
		DominantHeight("DomHeight", "HDom"),
		NumberOfPlots("NbPlots", "NbPlac"),
		paiSurvivor("PAISurv", "AACSurv"),
		paiMortality("PAIMort", "AACMort"),
		paiRecruitment("PAIRecr", "AACRecr"),
		NumberOfDecades("nbDecades", "nbDecen"),
		SpruceBudworm("SBW", "TBE"),
		TreatmentType("treatType", "traitType"),
		PesticideSpraying("InsSpray", "EpandPest"),
		SiteIndex("SiteIndex", "SiteIndex"),
		AboveGroundBiomass("AbGrBiom", "AbGrBiom");
		

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
	protected Set<Enum> selectedExportOptions;
	
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
		selectedExportOptions = new HashSet<Enum>();
		availableExportOptions = defineAvailableExportOptions();	
		if (availableExportOptions == null || availableExportOptions.isEmpty()) {
			throw new InvalidParameterException("There is no valid export option!");
		}
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
	 * @param selectedOption the selected option
	 * @return a GRecordSet instance
	 * @throws Exception if the worker does not terminate correctly
	 */
	@SuppressWarnings("rawtypes")
	protected final REpiceaRecordSet createRecordSet(Enum selectedOption, File file) throws Exception {
		
		if (!availableExportOptions.contains(selectedOption)) {
			throw new InvalidParameterException("Export option " + selectedOption.name() + " is not recognized!");
		}
		
		REpiceaRecordSet recordSet = new REpiceaRecordSet();
		InternalSwingWorkerForRecordSet buildRecordSetWorker = instantiateInternalSwingWorkerForRecordSet(selectedOption, recordSet);
		InternalWorkerForSaveMethod saveRecordSetWorker = null;
		if (saveFileEnabled) {
			saveRecordSetWorker = new InternalWorkerForSaveMethod(file, recordSet);
			saveRecordSetWorker.start();
			buildRecordSetWorker.addSaveThread(saveRecordSetWorker);
		}
		
		if (guiInterface != null && guiInterface.isVisible()) {
			// will be executed in the EventDispatchThread but the window will block because it is model
			guiInterface.showProgressBar(buildRecordSetWorker, selectedOption, true);	// true : is creating dataset
		} else {
			buildRecordSetWorker.run();	// is executed in the current thread
		}
		
		if (!buildRecordSetWorker.isCorrectlyTerminated()) {
			recordSet.clear();
		 	saveRecordSetWorker.terminate();
			throw buildRecordSetWorker.getFailureReason();
		} else if (buildRecordSetWorker.hasBeenCancelled()) {
			recordSet.clear();
		 	saveRecordSetWorker.terminate();
		 	throw new CancellationException();
		}

		if (saveFileEnabled) {
		 	saveRecordSetWorker.terminate();
			saveRecordSetWorker.join();
			if (saveRecordSetWorker.failure != null) {
				throw saveRecordSetWorker.failure;
			}
		}
		return recordSet;
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
	protected Set<Enum> getSelectedExportOptions() {return selectedExportOptions;}
	
	/**
	 * This method sets the export options. If the selectedOption Enum variable is not part of the 
	 * available export options, an exception is thrown.
	 * @param selectedOptions a set of Enum variables that should be among the available export options
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public void setSelectedOptions(Set<Enum> selectedOptions) throws Exception {
		selectedExportOptions.clear();
		for (Enum selectedOption : selectedOptions) {
			if (availableExportOptions.contains(selectedOption)) {
				selectedExportOptions.add(selectedOption);
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
	public void setAppendFileEnabled(boolean appendFileEnabled) {this.appendFileEnabled = appendFileEnabled;}	
	
	protected void setCanceled(boolean isCanceled) {this.isCanceled = isCanceled;}
	public boolean isCanceled() {return isCanceled;}

	/**
	 * This method sets the filename of the output file.
	 * @param filename a String
	 * @throws IOException if the file type is unknown
	 */
	public void setFilename(String filename) {
		if (!filename.trim().isEmpty() && GFileFilter.getFileType(filename) == FileType.UNKNOWN) {
			filename += "." + GFileFilter.CSV.getExtension();
		} 
		this.filename = filename;
	}
	
	protected String getFilename() {
		return filename;
	}
	

	@Override
	public REpiceaExportToolDialog getGuiInterface(Container parent) {
		if (guiInterface == null) {
			guiInterface = new REpiceaExportToolDialog(this, (Window) parent);
		}
		return guiInterface;
	}

	@Override
	public void showInterface(Window parent) {
		if (!getGuiInterface(parent).isVisible()) {
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
				Set<Enum> selectedOptions = new HashSet<Enum>();
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
	 * This method creates the different record sets depending on the selected export options.
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public Map<Enum, REpiceaRecordSet> exportRecordSets() throws Exception {
		Map<Enum, REpiceaRecordSet> outputMap = new HashMap<Enum, REpiceaRecordSet>();
		for (Enum selectedOutputOption : selectedExportOptions) {
			File file;
			if (selectedExportOptions.size() == 1) {
				file = new File(getFilename());
			} else {
				int indexOfLastDot = getFilename().lastIndexOf(".");
				String extension = getFilename().substring(indexOfLastDot, getFilename().length()).trim();
				String originalFilename = getFilename().substring(0, indexOfLastDot).trim();
				String optionType = selectedOutputOption.name().trim();
				file = new File(originalFilename + optionType + extension);
			}
			outputMap.put(selectedOutputOption, createRecordSet(selectedOutputOption, file));
		}
		return outputMap;
	}

	/**
	 * This method makes it possible to disable the saving to file. Then the Map that results from exportRecordSets() won't be empty.
	 * @param bool a boolean
	 */
	protected void setSaveFileEnabled(boolean bool) {
		saveFileEnabled = bool;
	}
}
