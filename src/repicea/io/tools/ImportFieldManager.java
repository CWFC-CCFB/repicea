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

import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import repicea.gui.REpiceaShowableUIWithParent;
import repicea.io.FormatField;
import repicea.io.FormatHeader;
import repicea.io.FormatReader;
import repicea.io.IOUserInterfaceableObject;
import repicea.io.REpiceaFileFilter;
import repicea.io.REpiceaFileFilterList;
import repicea.io.tools.ImportFieldElement.ImportFieldElementIDCard;
import repicea.io.tools.StreamImportFieldManager.QueueReader;
import repicea.lang.REpiceaSystem;
import repicea.serial.UnmarshallingException;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;


/**
 * This class manages a list of ImportFieldElement.
 * @author M.Fortin - June 2010
 */
public class ImportFieldManager implements Serializable, IOUserInterfaceableObject, REpiceaShowableUIWithParent {

	private static final long serialVersionUID = 20100804L;

	private static class FakeField extends FormatField {
		private FakeField(String name) {
			super();
			setName(name);
			setIndex(-1);
		}
	}
	
	private static Map<String, String> settingsMap;
	
	private static final String SETTINGS_FILENAME = REpiceaSystem.getJavaIOTmpDir() + "ifeSettings.ser";

	private enum NO_LEVEL {NoLevel}
	
	private Map<Enum<?>, List<ImportFieldElement>> importFieldElementMap; 
	private List<Enum<?>> importFieldElementIndex;

	private Enum<?> stratumFieldEnum;
	
	private String[] fileSpec;
	private transient FormatReader<? extends FormatHeader<? extends FormatField>> formatReader;
	
	private String ifeFilename;
	
	private boolean userValidated;

	private transient ImportFieldManagerDialog guiInterface;
	private transient boolean popupInGuiInterfaceEnabled = false;
	
	/**
	 * General constructor. 
	 * @param vector a vector defining the fields requested (i.e. ImportFieldElement instances)
	 * @param fileSpec the filename of the file to be read and the other specifications such as the table for instance
	 * @throws IOException if an I/O error has occurred
	 */
	@SuppressWarnings("rawtypes")
	public ImportFieldManager(List<ImportFieldElement> vector, String... fileSpec) throws IOException {
		ifeFilename = "";
		importFieldElementMap = new HashMap<Enum<?>, List<ImportFieldElement>>();
		for (ImportFieldElement elem : vector) {
			Enum level = ((LevelProviderEnum) elem.fieldID).getFieldLevel();
			if (level != null) {
				if (!importFieldElementMap.containsKey(level)) {
					importFieldElementMap.put(level, new ArrayList<ImportFieldElement>());
				}
				importFieldElementMap.get(level).add(elem);
			} else {				// no level specified
				if (!importFieldElementMap.containsKey(NO_LEVEL.NoLevel)) {
					importFieldElementMap.put(NO_LEVEL.NoLevel, new ArrayList<ImportFieldElement>());
				}
				importFieldElementMap.get(NO_LEVEL.NoLevel).add(elem);
				
			}
		}
		
		loadDefaultValues();
		
		setFileSpecifications(fileSpec); 
		
		if (new File(fileSpec[0]).exists() && new File(fileSpec[0]).isFile()) {
			String potentialIfeFilename = fileSpec[0].substring(0, fileSpec[0].lastIndexOf(".")).concat(REpiceaFileFilter.IFE.getExtension());
			if (new File(potentialIfeFilename).exists() && new File(potentialIfeFilename).isFile()) {
				try {
					load(potentialIfeFilename);
				} catch (Exception e) {} 
			}
		}
	}
	

	/**
	 * Protected constructor for deserialization.
	 */
	protected ImportFieldManager() {}
	
	/**
	 * Provide the number of records in the file. <br>
	 * <br>
	 * Note that it is the total number of records and not
	 * only those of a particular stratum if a stratum field has been specified.
	 * @return an integer
	 */
	public int getNumberOfRecords() {
		return getFormatReader().getRecordCount();
	}
	
	/**
	 * This method sets the index of the fields.
	 */
	public void setImportElementIndex() {
		importFieldElementIndex = new ArrayList<Enum<?>>();
		List<ImportFieldElement> vecOfElements = getFields(); 
		for (int i = 0; i < vecOfElements.size(); i++) {				
			importFieldElementIndex.add(vecOfElements.get(i).getFieldID());
		}
	}

	protected boolean isPopupInGuiInterfaceEnabled() {return popupInGuiInterfaceEnabled;}
	public void setPopupInGuiInterfaceEnabled(boolean popupInGuiInterfaceEnabled) {this.popupInGuiInterfaceEnabled = popupInGuiInterfaceEnabled;}

	/**
	 * The file specification is an array of strings that first contains the filename. With 
	 * AccessDataBase file, the second String is the table name.
	 * @return an Array of String intances
	 */
	public String[] getFileSpecifications() {return fileSpec;}
	
	@SuppressWarnings("rawtypes")
	protected FormatReader instantiateFormatReader() throws IOException {
		return FormatReader.createFormatReader(getFileSpecifications());
	}
	
	protected boolean isUserValidated() {return userValidated;}
	protected void setUserValidated(boolean userValidated) {this.userValidated = userValidated;}

	protected ImportFieldElement checkFields() {
		List<ImportFieldElement> vecOfFieldElements = getFields();
		// Check if all the needed fields have been selected or not
		for (int i = 0; i < vecOfFieldElements.size(); i++) {
			ImportFieldElement oElem = vecOfFieldElements.get(i);
			if (!oElem.isOptional && oElem.getMatchingFieldIndex() == -1) {
				System.out.println("ERROR: This field is mandatory" + " : " + oElem.description);
				return oElem;
			}
		}
		return null;
	}
	
	
	/**
	 * This method sets the name of the DBF file and reads the fields of this file 
	 * @param fileSpec the DBF file
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void setFileSpecifications(String... fileSpec) throws IOException {
		this.fileSpec = fileSpec;
		try {
			formatReader = instantiateFormatReader();
			if (!QueueReader.NOT_USING_FILES.equals(fileSpec[0])) {
				synchonizeImportFieldElementsWithAvailableFields();
			}
		} catch (FileNotFoundException e1) {
			System.out.println("Could not find file : " + getFileSpecifications()[0]);
			throw e1;
		} catch (IOException e2) {
			System.out.println("Error reading file : " + getFileSpecifications()[0]);
			throw e2;
		}
	}


	/**
	 * This method looks into the DBF file to find out field names
	 * that match with the required field. Otherwise, the match is 
	 * set to none (m_iFieldIndex = -1).
	 * @throws IOException 
	 */
	private void synchonizeImportFieldElementsWithAvailableFields() throws IOException {
		List<ImportFieldElement> vecOfFieldElements = getFields();
		for (int i = 0; i < vecOfFieldElements.size(); i++) {
			ImportFieldElement oElem = vecOfFieldElements.get(i);
			String fieldName = oElem.getFieldName();
			try {
				FormatField field;
				boolean matchFound = false;
				for (int j = 0; j < formatReader.getFieldCount(); j++) {
					field = formatReader.getHeader().getField(j);
					if (fieldName != null && fieldName.toLowerCase().compareTo(field.getName().toLowerCase()) == 0) {
						oElem.setFieldMatch(field);
						matchFound = true;
					}
				}
				if (!matchFound) {															// the field in the properties has no match in the DBF file then the field is reset to none
					oElem.setFieldMatch(FormatField.NON_AVAILABLE_FIELD);
				}
			} catch (Exception e)  {
				oElem.setFieldMatch(FormatField.NON_AVAILABLE_FIELD);
			}
		}
	}

	/**
	 * This method provides the vector of ImportFieldElement objects.
	 * @return a List of ImportFieldElement instances
	 */
	@SuppressWarnings("rawtypes")
	public List<ImportFieldElement> getFields() {
		List<ImportFieldElement> vec = new ArrayList<ImportFieldElement>();
		
		// first find out if there is more than one type of enum variables
		List<Class> enumVector = new ArrayList<Class>();
		for (Enum enumVar : importFieldElementMap.keySet()) {
			if (!enumVector.contains(enumVar.getClass())) {
				enumVector.add(enumVar.getClass());
			}
		}

		for (Class enumClass : enumVector) {
			for (Object enumVar : enumClass.getEnumConstants()) {
				if (importFieldElementMap.containsKey((Enum) enumVar)) {
					vec.addAll(importFieldElementMap.get((Enum) enumVar));
				}
			}
		}
		
		return vec;
	}
	
	protected Vector<FormatField> getFormatFields() {
		Vector<FormatField> values = new Vector<FormatField>();
		values.add(FormatField.NON_AVAILABLE_FIELD);
		for (int i = 0; i < formatReader.getHeader().getNumberOfFields(); i++) {
			values.add(formatReader.getHeader().getField(i));
		}
		return values;
	}
	
	/**
	 * This method returns the index of the field within the vector of ImportFieldElement objects
	 * @param fieldID = is the id of the field
	 * @return an integer that is index
	 */
	@SuppressWarnings("rawtypes")
	public int getIndexOfThisField(Enum fieldID) {return importFieldElementIndex.indexOf(fieldID);}
	
	/**
	 * Load default values in the vector of ImportElement
	 */
	@SuppressWarnings({ "rawtypes", "unchecked"})
	private void loadDefaultValues() {
      	try {
      		FileInputStream fis = new FileInputStream(SETTINGS_FILENAME);
      		ObjectInputStream in = new ObjectInputStream(fis);
      		settingsMap = (HashMap) in.readObject();
      		in.close();
      	} catch(Exception ex) {}
		
		List<ImportFieldElement> vecFields = getFields();
		FakeField field;
		String potentialFieldName;
		for (int i = 0; i < vecFields.size(); i++) {
			ImportFieldElement oElem = vecFields.get(i);
			potentialFieldName = getProperty(oElem.propertyName, FormatField.NON_AVAILABLE_FIELD.getName());
			field = new FakeField(potentialFieldName);
			oElem.setFieldMatch(field);
		}
	}

	
	/**
	 * This method saves the values of the properties.
	 */
	protected void saveDefaultValues() {
		List<ImportFieldElement> vecFields = getFields();
		for (int i = 0; i < vecFields.size(); i++) {
			ImportFieldElement oElem = vecFields.get(i);
			setProperty (oElem.propertyName, oElem.getFieldName());
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(SETTINGS_FILENAME);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(getSettingsMap());
			out.close();
		} catch(Exception ex) {}

	}

	/**
	 * This method returns the ImportFieldElement object corresponding to the FieldID parameter.
	 * @param fieldID a FieldID enum variable
	 * @return the ImportFieldElement instance that corresponds to the FieldID variable
	 */
	@SuppressWarnings("rawtypes")
	public ImportFieldElement getField(Enum fieldID) {
		int index = this.getIndexOfThisField(fieldID);
		if (index != -1) {
			return this.getFields().get(index);
		} else {
			return null;
		}
	}
	
	private static String getProperty(String propertyName, String otherwise) {
		if (ImportFieldManager.getSettingsMap().containsKey(propertyName)) {
			return ImportFieldManager.getSettingsMap().get(propertyName);
		} else {
			return otherwise;
		}
	}
	
	private static void setProperty(String propertyName, String property) {
		ImportFieldManager.getSettingsMap().put(propertyName, property);
	}
	
	private static Map<String, String> getSettingsMap() {
		if (settingsMap == null) {
			settingsMap = new HashMap<String, String>(); 
		}
		return settingsMap; 
	}
	
	

	/**
	 * This method returns an ImportFieldManager loaded from the filename parameter.
	 * @param ifeFilename the file that contains the vector of ImportFieldElement
	 * @param fileSpec the file to be read and all the required specifications such as the table for instance
	 * @return an ImportFieldManager instance
	 * @throws IOException if an I/O error has occurred
	 * @throws UnmarshallingException if a marshalling error has occurred
	 */
	public static ImportFieldManager createImportFieldManager(String ifeFilename, String... fileSpec) throws UnmarshallingException, IOException  {
		ImportFieldManager ifm = new ImportFieldManager();
		Map<Enum<?>, List<ImportFieldElement>> oMap = ImportFieldManager.loadImportFieldElementMap(ifeFilename);
		ifm.importFieldElementMap = oMap;
		ifm.setFileSpecifications(fileSpec);
		ifm.ifeFilename = ifeFilename;
		return ifm;
	}
	
	@SuppressWarnings({ "unchecked" })
	private static Map<Enum<?>, List<ImportFieldElement>> loadImportFieldElementMap(String ifeFilename) throws IOException, UnmarshallingException {
		XmlDeserializer decoder = new XmlDeserializer(ifeFilename);
		Object obj = decoder.readObject();
		return (Map<Enum<?>, List<ImportFieldElement>>) obj;
	}
	

	@Override
	public ImportFieldManagerDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new ImportFieldManagerDialog(this, (Window) parent);
		}
		return guiInterface;
	}

	
	@Override
	public void showUI(Window parent) {
		if (!getUI(parent).isVisible()) {
			guiInterface.setVisible(true);
		}
	}

	protected void setStratumFieldEnum(Enum<?> stratumFieldEnum) {
		setImportElementIndex();
		if (stratumFieldEnum != null && getField(stratumFieldEnum) == null) {
			throw new InvalidParameterException("This field has not been defined in the list of fields to be imported! Please check the method defineFieldsToImport().");
		} else {
			this.stratumFieldEnum = stratumFieldEnum;
		}
	}

	@SuppressWarnings("rawtypes")
	protected Enum getStratumFieldEnum() {
		return stratumFieldEnum;
	}


	@Override
	public void save(String filename) throws IOException {
		XmlSerializer serializer = new XmlSerializer(filename);
		serializer.writeObject(importFieldElementMap);
		ifeFilename = filename;
	}


	@Override
	public void load(String filename) throws IOException {
		Map<Enum<?>, List<ImportFieldElement>> loadedMap = ImportFieldManager.loadImportFieldElementMap(filename);
		for (Enum<?> enumVar : importFieldElementMap.keySet()) {
			List<ImportFieldElement> loadedImportFieldElements = loadedMap.get(enumVar);
			List<ImportFieldElement> importFieldElements = importFieldElementMap.get(enumVar);
			if (loadedImportFieldElements != null && !importFieldElements.isEmpty()) {
				for (ImportFieldElement ife : importFieldElements) {
					Enum<?> fieldID = ife.getFieldID();
					for (ImportFieldElement loadedIfe : loadedImportFieldElements) {
						if (loadedIfe.getFieldID() == fieldID) { // if the match is found then
							ife.setFieldMatch(new FakeField(loadedIfe.getFieldName()));
							break;
						}
					}
				}
			}
		}

		synchonizeImportFieldElementsWithAvailableFields();
		ifeFilename = filename;
	}


	@Override
	public REpiceaFileFilterList getFileFilters() {return new REpiceaFileFilterList(REpiceaFileFilter.IFE);}


	@Override
	public String getFilename() {return ifeFilename;}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	/**
	 * Return the list of the description of the fields.
	 * @return a List of String
	 */
	public List<ImportFieldElementIDCard> getFieldDescriptions() {
		List<ImportFieldElementIDCard> fieldDescriptions = new ArrayList<ImportFieldElementIDCard>();
		for (ImportFieldElement f : getFields()) {
			fieldDescriptions.add(f.getIDCard());
		}
		return fieldDescriptions;
	}

	/**
	 * Provide the FormatReader instance which is always re-instantiated when creating the instance or
	 * when deserializing.
	 * @return a FormatReader type instance
	 */
	public FormatReader getFormatReader() {return formatReader;}
	
}