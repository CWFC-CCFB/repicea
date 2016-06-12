/*
 * This file is part of the repicea-iotools library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import repicea.gui.UIControlManager;
import repicea.gui.genericwindows.REpiceaProgressBarDialog;
import repicea.gui.genericwindows.REpiceaSimpleListDialog;
import repicea.io.FormatField;
import repicea.io.FormatHeader;
import repicea.io.FormatReader;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


/**
 * The RecordReader class does two things: 1- it defines the field to be imported and 2-
 *  it reads the different records.
 * @author Mathieu Fortin - December 2010
 */
public abstract class REpiceaRecordReader implements Serializable {
	
	public static class VariableValueException extends Exception {
		private static final long serialVersionUID = 20101221L;
		
		public VariableValueException(String message) {
			super(message);
		}
	}
	
	public static enum MessageID implements TextableEnum {
		ProgressMessage("Reading the strata list...",
				"Lecture des strates..."),
		StratumSelectionTitle("Stratum to be imported",
				"S\u00E9lection du peuplement ou de la strate \u00E0 importer"),
		StratumSelectionMessage("The input file contains many strata. Please select the stratum to import: ",
				"Votre fichier d'entr\u00E9e comprend plus d'un peuplement ou plus d'une strate. Veuillez s\u00E9lectionner le peuplement ou la strate \u00E0 importer :");
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
	}
	
	private static final long serialVersionUID = 20101220;
	
	private int selectedGroupId;
	private ImportFieldManager importFieldManager;
	private GroupingRegistryReader groupingRegistryReader;
	
	private boolean isPopUpWindowEnabled;
	
	/**
	 * Constructor for GUI mode.
	 */
	protected REpiceaRecordReader() {}


	/**
	 * This method initializes the RecordInstantiator object in GUI mode.
	 * @param guiOwner a Window instance that can be null if the dialog has no owner
	 * @param fileSpec the specifications of the file to be imported (e.g. filename, table, etc...)
	 * @throws Exception a CancellationException is thrown if the user cancels the dialog
	 */
	public void initGUIMode(Window guiOwner, String... fileSpec) throws Exception {
		
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
	 * This method initializes the RecordInstantiator object in GUI mode with no owner.
	 * @param fileSpec the specifications of the file to be imported (e.g. filename, table, etc...)
	 * @throws Exception a CancellationException is thrown if the user cancels the dialog
	 */
	public void initGUIMode(String... fileSpec) throws Exception {
		initGUIMode(null, fileSpec);
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
	@SuppressWarnings("unchecked")
	public void readRecordsForThisGroupId(int groupId) throws Exception {
		int lineCounter = 0;

		List<ImportFieldElement> importFieldElements = importFieldManager.getFields();
		List<Integer> index = groupingRegistryReader.getObservationIndicesForThisGroup(groupId);

		Object[] oArray;

		FormatReader<? extends FormatHeader<? extends FormatField>> reader = null;
		try {
			reader = FormatReader.createFormatReader(importFieldManager.getFileSpecifications());
			oArray = new Object[importFieldElements.size()];

			if (index==null) {							// if the index is null a false index that contains all the observations is created
				index = new ArrayList<Integer>();
				for (int i = 0; i < reader.getRecordCount(); i++) {
					index.add(i);
				}
			}

			// Now, lets start reading the rows
			int numberOfLinesToSkip;
			int lineNumber;
			Object[] rowObjects = null;
			for (Iterator<Integer> iterRow = index.iterator(); iterRow.hasNext();) {
				lineNumber = iterRow.next();
				numberOfLinesToSkip = lineNumber - lineCounter;
				rowObjects = reader.nextRecord(numberOfLinesToSkip);
				lineCounter = lineNumber + 1;  					// 1 is added to have the real reference line 1 is really line 1

				if (rowObjects!=null) {
					for (int i = 0; i < importFieldElements.size(); i++) {
						ImportFieldElement impFieldElem = importFieldElements.get(i);
						int iFieldIndex = impFieldElem.getMatchingFieldIndex();
						if (!impFieldElem.isOptional) {										// if the field is not optional
							try {
								oArray[i] = rowObjects[iFieldIndex].toString().trim();
							} catch (NullPointerException e) {
								throw new NullPointerException("A null value has been found at line " + lineNumber + " in the DBF file : field " + impFieldElem.getFieldName());
							}
						} else {																// the field is then optional
							if (iFieldIndex < 0 || rowObjects[iFieldIndex] == null || rowObjects[iFieldIndex].toString().isEmpty()) {			// if the field has not been specified or the selected field contains a null value
								oArray[i] = null;
							} else {
								oArray[i] = rowObjects[iFieldIndex].toString().trim();
							}
						}
					}
					checkInputFieldsFormat(oArray);
					readLineRecord(oArray, lineCounter);
				}
			}

		} catch (Exception e) {
			String message; 
			if (e instanceof VariableValueException) {
				message = "Values of variables are inconsistent at line : " + lineCounter + ": " + e.getMessage();
			} else if (e instanceof FileNotFoundException) {
				message = "Could not find file : " + importFieldManager.getFileSpecifications()[0];
			} else {
				message = "Error while reading file : " + importFieldManager.getFileSpecifications()[0] + " at line : " + lineCounter;
			}
			throw new Exception(message);
		} finally {
			if (reader != null) {
				reader.close();
			}
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
	protected void checkInputFieldsFormat(Object[] oArray) throws Exception {
		List<ImportFieldElement> oVecImport = getImportFieldManager().getFields(); 	// reference on the vector of field element in the SuccesDBFImport object
		for (int i = 0; i < oVecImport.size(); i++) {				
			if (oArray[i] != null) {								// if the oArray[i] == null, it means either the field has not been associated or the field is empty in the DBF file
				switch(oVecImport.get(i).getFieldType()) {
				case Double:							// type = float
					if (oArray[i].toString().trim().equals(".") || oArray[i].toString().trim().toUpperCase().equals("NA")) {
						oArray[i] = Double.NaN;
					} else {
						oArray[i] = Double.parseDouble(oArray[i].toString().replace(",", "."));
					}
					break;
				case Integer:
					oArray[i] = (Integer) ((Double) Double.parseDouble(oArray[i].toString().replace(",","."))).intValue();
					break;
				case String:												// type = character
					oArray[i] = (oArray[i].toString()).trim();
					break;
				default:
					throw new Exception("Unknown field type");
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
