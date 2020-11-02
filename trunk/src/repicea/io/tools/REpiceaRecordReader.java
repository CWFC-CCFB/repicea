/*
 * This file is part of the repicea-iotools library.
 *
 * Copyright (C) 2009-2020 Mathieu Fortin for Rouge-Epicea
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

import java.awt.Window;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import repicea.app.AbstractGenericTask;
import repicea.gui.UIControlManager;
import repicea.gui.genericwindows.REpiceaProgressBarDialog;
import repicea.gui.genericwindows.REpiceaSimpleListDialog;
import repicea.io.FormatReader;
import repicea.io.tools.ImportFieldElement.FieldType;
import repicea.simulation.UseModeProvider.UseMode;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


/**
 * The RecordReader class does two things: 1- it defines the field to be imported and 2-
 *  it reads the different records.
 * @author Mathieu Fortin - December 2010
 */
public abstract class REpiceaRecordReader implements Serializable {
	
	@SuppressWarnings("serial")
	protected class InternalTask extends AbstractGenericTask {

		final int groupId;
		
		protected InternalTask(int groupId) {
			this.groupId = groupId;
		}
		
		@Override
		protected void doThisJob() throws Exception {
			int lineCounter = 0;

			List<ImportFieldElement> importFieldElements = importFieldManager.getFields();
			List<Integer> rowIndex = groupingRegistryReader.getObservationIndicesForThisGroup(groupId);

			Object[] oArray;

			FormatReader<?> reader = null;
			try {
				reader = importFieldManager.instantiateFormatReader();
				oArray = new Object[importFieldElements.size()];

				if (rowIndex==null) {							// if the index is null a false index that contains all the observations is created
					rowIndex = new ArrayList<Integer>();
					for (int i = 0; i < reader.getRecordCount(); i++) {
						rowIndex.add(i);
					}
				}
				
				double factor = 100d / rowIndex.size();
				
				// Now, lets start reading the rows
				int numberOfLinesToSkip;
				int numberLinesRead = 0;
				Object[] rowObjects = null;
				for (Integer lineNumber : rowIndex) {
					numberOfLinesToSkip = lineNumber - lineCounter;
					rowObjects = reader.nextRecord(numberOfLinesToSkip);
					lineCounter = lineNumber + 1;  					// 1 is added to have the real reference line 1 is really line 1

					if (rowObjects!=null) {
						for (int j = 0; j < importFieldElements.size(); j++) {
							ImportFieldElement impFieldElem = importFieldElements.get(j);
							int iFieldIndex = impFieldElem.getMatchingFieldIndex();
							if (!impFieldElem.isOptional) {										// if the field is not optional
								try {
									oArray[j] = rowObjects[iFieldIndex].toString().trim();
								} catch (NullPointerException e) {
									throw new NullPointerException("A null value has been found at line " + lineNumber + " in the DBF file : field " + impFieldElem.getFieldName());
								}
							} else {																// the field is then optional
								if (iFieldIndex < 0 || rowObjects[iFieldIndex] == null || rowObjects[iFieldIndex].toString().isEmpty()) {			// if the field has not been specified or the selected field contains a null value
									oArray[j] = null;
								} else {
									oArray[j] = rowObjects[iFieldIndex].toString().trim();
								}
							}
						}
						checkInputFieldsFormat(oArray);
						readLineRecord(oArray, lineCounter);
						numberLinesRead++;
						firePropertyChange(REpiceaProgressBarDialog.PROGRESS, 0, (int) (numberLinesRead * factor));
					}
				}

			} catch (Exception e) {
				String message; 
				if (e instanceof VariableValueException) {
					message = MessageID.InconsistentValueInThisField.toString() + MessageID.AtLine.toString() + lineCounter + ": " + e.getMessage();
				} else if (e instanceof FileNotFoundException) {
					message = MessageID.FileCouldNotBeFound.toString() + importFieldManager.getFileSpecifications()[0];
				} else if (e instanceof NullInThisFieldException) {
					message = ((NullInThisFieldException) e).getMessage() + " " + MessageID.AtLine.toString() + lineCounter;
				} else {
					message = MessageID.ErrorWhileReading.toString() + importFieldManager.getFileSpecifications()[0] + " " + MessageID.AtLine.toString() + lineCounter;
				}
				throw new Exception(message);
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		}
		
	}
	
	public static class VariableValueException extends Exception {
		private static final long serialVersionUID = 20101221L;
		
		public VariableValueException(String message) {
			super(message);
		}
	}
	
	@SuppressWarnings("serial")
	public static class NullInThisFieldException extends NullPointerException {
		
		private final ImportFieldElement ife;

		public NullInThisFieldException(ImportFieldElement ife) {
			this.ife = ife;
		}
		
		@Override
		public String getMessage() {
			return MessageID.NullValueInThisField.toString() + ife.getFieldName();
		}
	}
	
	
	
	public static enum MessageID implements TextableEnum {
		ProgressMessage("Reading the strata list...",
				"Lecture des strates..."),
		StratumSelectionTitle("Stratum to be imported",
				"S\u00E9lection du peuplement ou de la strate \u00E0 importer"),
		StratumSelectionMessage("The input file contains many strata. Please select the stratum to import: ",
				"Votre fichier d'entr\u00E9e comprend plus d'un peuplement ou plus d'une strate. Veuillez s\u00E9lectionner le peuplement ou la strate \u00E0 importer :"),
		NullValueInThisField("A null value has been found in this field: ", "Une valeur nulle a \u00E9t\\u00E9 d\u00E9tect\u00E9e dans le champ suivant : "),
		InconsistentValueInThisField("Values of variables are inconsistent ", "Les valeurs de certains champs sont incoh\u00E9rentes "),
		AtLine("at line ", "\u00A0 la ligne "),
		FileCouldNotBeFound("CAPSIS cannot find file: ", "CAPSIS n'a pas pu trouv\u00E9 le fichier : "),
		ErrorWhileReading("Error while reading file: ", "Erreur lors de la lecture du fichier : "),
		ReadingFile("Reading the records...", "Lecture des enregistrements...")
		;
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	private static final long serialVersionUID = 20101220;
	
	private int selectedGroupId;
	private ImportFieldManager importFieldManager;
	private GroupingRegistryReader groupingRegistryReader;
	
	private boolean isPopUpWindowEnabled;
	private UseMode guiMode;
	
	private transient Window windowOwner;
	
	/**
	 * Constructor for GUI mode.
	 */
	protected REpiceaRecordReader() {
		guiMode = UseMode.PURE_SCRIPT_MODE;
	}


	/**
	 * This method initializes the RecordInstantiator object in GUI mode.
	 * @param guiOwner a Window instance that can be null if the dialog has no owner
	 * @param fileSpec the specifications of the file to be imported (e.g. filename, table, etc...)
	 * @throws Exception a CancellationException is thrown if the user cancels the dialog
	 */
	@SuppressWarnings("deprecation")
	public void initGUIMode(Window guiOwner, UseMode useMode, String... fileSpec) throws Exception {
		if (useMode == null || useMode == UseMode.PURE_SCRIPT_MODE) {
			throw new InvalidParameterException("The use mode with the initGUIMode should be either UseMode.GUI_MODE or UseMode.ASSISTED_SCRIPT_MODE!");
		}
		this.windowOwner = guiOwner;
		this.guiMode = useMode;
		
		importFieldManager = new ImportFieldManager(defineFieldsToImport(), fileSpec);
		importFieldManager.setStratumFieldEnum(defineGroupFieldEnum());

		importFieldManager.setPopupInGuiInterfaceEnabled(isPopUpWindowEnabled);
		importFieldManager.showUI(guiOwner);
		
		if (!importFieldManager.isUserValidated()) {
			throw new CancellationException();
		}

		groupingRegistryReader = new GroupingRegistryReader(importFieldManager);
		
		String title = REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Progress);
		String message = REpiceaTranslator.getString(MessageID.ProgressMessage);
		
		new REpiceaProgressBarDialog(guiOwner, title, message, groupingRegistryReader, false);
		
		if (!groupingRegistryReader.isCorrectlyTerminated()) {
			throw groupingRegistryReader.getFailureReason();
		} else {
			makeASelection(guiOwner);
		}
	}
	
	/**
	 * This method returns true if the field is available. 
	 * @param fieldID the enum that represents the field.
	 * @param oArray the line record from the file.
	 * @return true if the field can be found in the import field manager and if it can be found in the line record.
 	 */
	protected final boolean isThisFieldAvailable(Enum<?> fieldID, Object[] oArray) {
		int index = this.getImportFieldManager().getIndexOfThisField(fieldID);
		return index != -1 && oArray[index] != null;
	}

	
	protected final Enum<?> setCurrentFieldID(Object[] oArray, Enum<?> currentFieldID) throws Exception {
		checkInputFieldsValue(oArray, currentFieldID);
		return currentFieldID;
	}

	protected void checkInputFieldsValue(Object[] oArray, Enum<?> fieldID) throws Exception {}
	
	/**
	 * This method initializes the RecordInstantiator object in GUI mode with no owner.
	 * @param fileSpec the specifications of the file to be imported (e.g. filename, table, etc...)
	 * @throws Exception a CancellationException is thrown if the user cancels the dialog
	 */
	public void initGUIMode(UseMode useMode, String... fileSpec) throws Exception {
		initGUIMode(null, useMode, fileSpec);
	}

	/**
	 * For script mode.
	 * @param importFieldManager a valid ImportFieldManager instance
	 * @throws Exception when the groups have not been properly read
	 */
	public void initInScriptMode(ImportFieldManager importFieldManager) throws Exception {
		this.importFieldManager = importFieldManager;
		this.importFieldManager.setStratumFieldEnum(defineGroupFieldEnum());
		
		if (importFieldManager.checkFields() != null) {
			throw new Exception();
		}
		groupingRegistryReader = new GroupingRegistryReader(importFieldManager);
		groupingRegistryReader.run();
		if (!groupingRegistryReader.isCorrectlyTerminated()) {
			throw groupingRegistryReader.getFailureReason();
		}
	}
	
	/**
	 * This method serves to enable or disable the PopUp Windows in the Import dialog. By default, the pop up are disabled.
	 * @param isPopUpWindowEnabled a boolean
	 */
	protected void setPopUpWindowEnabled(boolean isPopUpWindowEnabled) {this.isPopUpWindowEnabled = isPopUpWindowEnabled;}

	/**
	 * This method reads all the records of the dataset.
	 * @throws Exception
	 */
	public void readAllRecords() throws Exception {
		readRecordsForThisGroupId(-1);
	}

	/**
	 * This method reads the records that correspond to the group ID. If the group ID is not found,
	 * the method read all the records by default.
	 * @param groupId a integer that corresponds to the group ID
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public void readRecordsForThisGroupId(int groupId) throws Exception {
		InternalTask task = new InternalTask(groupId); 

		if (guiMode == UseMode.GUI_MODE) {
			String title = REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Progress);
			String message = REpiceaTranslator.getString(MessageID.ReadingFile);
			new REpiceaProgressBarDialog(windowOwner, title, message, task, false);
		} else {	
			task.run();
		}
		if (!task.get()) {
			throw task.getFailureReason();
		}
	}

	/**
	 * This method returns the id of the selected group.
	 * @return an Integer
	 */
	public int getSelectedGroupId() {return selectedGroupId;}
	
	/**
	 * This method sets the selected group.
	 * @param selectedGroupId an Integer that represents the index of the selected group
	 */
	public void setSelectedGroupId(int selectedGroupId) {this.selectedGroupId = selectedGroupId;}
	
	/**
	 * This method returns true if the grouping is enabled.
	 * @return true if enabled or false otherwise
	 */
	public boolean isGroupingEnabled() {return groupingRegistryReader.isGroupingEnabled();}
	
	/**
	 * This method returns a list of Strings that represents the groups.
	 * @return a List of String instances
	 */
	public List<String> getGroupList() {return groupingRegistryReader.getGroupList();};

	/**
	 * This method returns the indices of the observations for a given group.
	 * @param groupId the id of the group
	 * @return a List of Integer instances
	 */
	public List<Integer> getObservationIndicesForThisGroup(int groupId) {
		return groupingRegistryReader.getObservationIndicesForThisGroup(groupId);
	}
	
	/**
	 * This method returns the group name for this particular group ID.
	 * @param groupId the id of the group
	 * @return a String
	 */
	public String getGroupName(int groupId) {return groupingRegistryReader.getGroupName(groupId);}

	/**
	 * This method returns the indices of all the observations according to their corresponding groups. 
	 * @return a Map instance with the group names as keys and the index lists as values
	 */
	public Map<String, List<Integer>> getGroupMap() {return groupingRegistryReader.getGroupMap();}
	
	/**
	 * This method defines the fields to be imported. It is to be defined in the derived classes.
	 * @return a List of ImportFieldElement instances
	 * @throws Exception
	 */
	protected abstract List<ImportFieldElement> defineFieldsToImport() throws Exception;
	
	/**
	 * This method checks if the input values of the current record are of the appropriate format.
	 * @param oArray the line record
	 * @throws Exception
	 */
	protected final void checkInputFieldsFormat(Object[] oArray) throws Exception {
		List<ImportFieldElement> oVecImport = getImportFieldManager().getFields(); 	// reference on the vector of field element in the SuccesDBFImport object
		for (int i = 0; i < oVecImport.size(); i++) {				
			if (oArray[i] != null) { // if the oArray[i] == null, it means either the field has not been associated or the field is empty in the DBF file
				FieldType ft = oVecImport.get(i).getFieldType();
				if (!ft.isAlreadyInTheAppropriateFormat(oArray[i])) { // if the object class is not the good one then we parse
					switch(oVecImport.get(i).getFieldType()) {
					case Double:							// type = float
						if (oArray[i].toString().isEmpty() || oArray[i].toString().trim().equals(".") || oArray[i].toString().trim().toUpperCase().equals("NA")) {
							oArray[i] = null;
						} else {
							oArray[i] = Double.parseDouble(oArray[i].toString().replace(",", "."));
						}
						break;
					case Integer:
						if (oArray[i].toString().isEmpty() || oArray[i].toString().trim().equals(".") || oArray[i].toString().trim().toUpperCase().equals("NA")) {
							oArray[i] = null;
						} else {
							oArray[i] = (Integer) ((Double) Double.parseDouble(oArray[i].toString().replace(",","."))).intValue();
						}
						break;
					case String:												// type = character
						if (oArray[i].toString().isEmpty() || oArray[i].toString().trim().equals(".") || oArray[i].toString().trim().toUpperCase().equals("NA")) {
							oArray[i] = "";
						} else {
							oArray[i] = (oArray[i].toString()).trim();
						}
						break;
					default:
						throw new Exception("Unknown field type");
					}
				}
			}
		}
	}

	/**
	 * If the number of strata is larger than 1, this method provides a dialog with a list
	 * that allows to select a particular stratum.
	 * @param guiOwner the Frame instance that acts as owner
	 * @throws CancellationException if the user has cancelled the dialog
	 */
	protected void makeASelection(Window guiOwner) throws CancellationException {
		if (groupingRegistryReader.getGroupList().size() > 1) {
			REpiceaSimpleListDialog strataDlg = new REpiceaSimpleListDialog(guiOwner,
					groupingRegistryReader.getGroupList(),
					REpiceaTranslator.getString(MessageID.StratumSelectionTitle),
					REpiceaTranslator.getString(MessageID.StratumSelectionMessage));
			if (strataDlg.isValidated()) {
				String stratumName = (String) strataDlg.getSelectedValue();
				selectedGroupId = groupingRegistryReader.getGroupList().indexOf(stratumName);
			} else {
				throw new CancellationException();
			}
		}
	}
	
	/**
	 * This method set the Enum var that corresponds to the group field. If the method returns null 
	 * then there is no stratum selection at all.
	 * @return an Enum variable
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Enum defineGroupFieldEnum(); 

	/**
	 * This method read the line record and set the values in the appropriate fields. To be defined in derived classes.
	 */
	protected abstract void readLineRecord(Object[] oArray, int lineCounter) throws VariableValueException, Exception;

	protected ImportFieldManager getImportFieldManager() {return importFieldManager;}


	
	
}
