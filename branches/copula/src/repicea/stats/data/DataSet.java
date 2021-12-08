/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2019 Mathieu Fortin for Rouge-Epicea
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
package repicea.stats.data;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import repicea.app.AbstractGenericTask;
import repicea.gui.REpiceaUIObject;
import repicea.gui.components.REpiceaTable;
import repicea.gui.components.REpiceaTableModel;
import repicea.gui.genericwindows.REpiceaProgressBarDialog;
import repicea.io.FormatField;
import repicea.io.FormatReader;
import repicea.io.FormatWriter;
import repicea.io.GExportFieldDetails;
import repicea.io.Saveable;
import repicea.math.Matrix;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The DataSet class contains many observations and implements the method to read a dataset with a FormatReader instance.
 * @author Mathieu Fortin - November 2012
 */
public class DataSet extends AbstractGenericTask implements Saveable, REpiceaUIObject {

	protected static enum ActionType {Replace,
		Add;
	}

	private static enum MessageID implements TextableEnum {

		ReadingFileMessage("Reading file...", "Lecture du fichier...");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	

	
	protected List<String> fieldNames;
	protected List<Class<?>> fieldTypes;
	protected List<Observation> observations;
	private final String originalFilename;
	
	private transient REpiceaTable table;
	
	protected DataSet(String filename) {
		this.originalFilename = filename;
		fieldNames = new ArrayList<String>();
		fieldTypes = new ArrayList<Class<?>>();
		observations = new ArrayList<Observation>();
	}

	
	/**
	 * General constructor.
	 * @param filename the name of the file to be read
	 * @param autoLoad true if the file is to be read now. Typically, this boolean is set to false when the swingworker is
	 * launched from a window that retrieves some events.
	 */
	public DataSet(String filename, boolean autoLoad) {
		this(filename);
		if (autoLoad) {
			run();
		}
	}
	
	/**
	 * An empty dataset ready to be filled with observations.
	 * @param fieldNames a List of String instances that represent the field names
	 */
	public DataSet(List<String> fieldNames) {
		this((String) null);
		for (String fieldName : fieldNames) {
			addFieldName(fieldName);
		}
	}
	

	/**
	 * This method returns any object in the dataset at row i and column j.
	 * @param i the index of the row
	 * @param j the index of the column
	 * @return an Object instance
	 */
	protected Object getValueAt(int i, int j) {
		return observations.get(i).values.get(j);
	}

	protected Object getValueAt(int i, String fieldName) {
		int j = getIndexOfThisField(fieldName);
		if (j != -1) {
			return getValueAt(i,j);
		} else {
			return null;
		}
	}

	private void setValueAt(int i, int j, Object value) {
		if (value.getClass().equals(fieldTypes.get(j))) {
			observations.get(i).values.remove(j);
			observations.get(i).values.add(j, value);
		} 
	}
	
	private Object addTwoNumbers(Number number1, Number number2) {
		if (number1 instanceof Integer && number2 instanceof Integer) {
			return number1.intValue() + number2.intValue();
		} else {
			return number1.doubleValue() + number2.doubleValue();
		}
	}
	
	
	protected void setValueAt(int i, int j, Object newValue, ActionType actionType) {
		if (actionType == ActionType.Replace) {
			setValueAt(i, j, newValue);
		} else if (actionType == ActionType.Add) {
			Object formerValue = getValueAt(i, j);
			Object addedNewValue;
			if (formerValue instanceof Number && newValue instanceof Number) {
				addedNewValue = addTwoNumbers((Number) newValue, (Number) formerValue);
			} else {
				if (formerValue.toString().isEmpty()) {
					addedNewValue = newValue.toString();
				} else {
					addedNewValue = formerValue.toString().concat(" - " + newValue.toString());
				}
			}
			setValueAt(i, j, addedNewValue);
		}
	}

	protected void setValueAt(int i, String fieldName, Object value, ActionType actionType) {
		int j = getIndexOfThisField(fieldName);
		if (j != -1) {
			setValueAt(i, j, value, actionType);
		}
	}

	/**
	 * Indexes the different field types. More specifically, it goes 
	 * through the columns and find the appropriate class for a particular
	 * field. This method should be called after adding all the observations.
	 */
	public void indexFieldType() {
		fieldTypes.clear();
		for (int j = 0; j < fieldNames.size(); j++) {
			setClassOfThisField(j);
		}
	}
	
	private void setClassOfThisField(int fieldIndex) {
		if (isInteger(fieldIndex)) {
			setFieldType(fieldIndex, Integer.class);
		} else if (isDouble(fieldIndex)) {
			setFieldType(fieldIndex, Double.class);
			reconvertToDoubleIfNeedsBe(fieldIndex);
		} else {
			setFieldType(fieldIndex, String.class);
			reconvertToStringIfNeedsBe(fieldIndex);
		}
	}

	private void setFieldType(int fieldIndex, Class clazz) {
		if (fieldIndex < fieldTypes.size()) {
			fieldTypes.set(fieldIndex, clazz);	
		} else if (fieldIndex == fieldTypes.size()) {
			fieldTypes.add(clazz);	
		} else {
			throw new InvalidParameterException("The field type cannot be set!");
		}
	}
	
	
	private boolean isInteger(int j) {
		boolean isInteger = true;
		for (int i = 0; i < getNumberOfObservations(); i++) {
			if (!(getValueAt(i,j) instanceof Integer)) {
					isInteger = false;
					break;
			} 
		}
		return isInteger;
	}


	private void reconvertToStringIfNeedsBe(int j) {
		for (int i = 0; i < getNumberOfObservations(); i++) {
			Object value = getValueAt(i,j);
			if ((value instanceof Number)) {
				setValueAt(i,j, value.toString());
			}
		} 
	}

	private void reconvertToDoubleIfNeedsBe(int j) {
		for (int i = 0; i < getNumberOfObservations(); i++) {
			Object value = getValueAt(i,j);
			if ((value instanceof Integer)) {
				setValueAt(i,j, ((Integer) value).doubleValue()); // MF2020-04-30 Bug corrected 
			}
		} 
	}


	private boolean isDouble(int indexJ) {
		boolean isDouble = true;
		for (int i = 0; i < getNumberOfObservations(); i++) {
			if (!(getValueAt(i,indexJ) instanceof Number)) {
					isDouble = false;
					break;
			} 
		}
		return isDouble;
	}
	
	/**
	 * This method returns the index of a particular field.
	 * @param fieldName the name of the field
	 * @return an integer
	 */
	protected int getIndexOfThisField(String fieldName) {return fieldNames.indexOf(fieldName);}

	/**
	 * This method sorts the data according to the fields represented by the indices in fieldIndices parameter.
	 * @param fieldIndices a List of field indices
	 */
	@SuppressWarnings("unchecked")
	public void sortObservations(List<Integer> fieldIndices) {
		Observation.comparableFields = fieldIndices;
		Collections.sort(observations);
	}
	
	/**
	 * Split the data set into different data sets following the values of the fields specified in the
	 * fieldIndicesForSplitting parameters. 
	 * @param fieldIndicesForSplitting
	 * @param fieldIndicesForSorting
	 * @return a Map of DataSet
	 */
	DataSetGroupMap splitAndOrder(List<Integer> fieldIndicesForSplitting, List<Integer> fieldIndicesForSorting) {
		DataSetGroupMap outputMap = new DataSetGroupMap(this);
		for (Observation obs : observations) {
			DataGroup id = new DataGroup();
			for (Integer index : fieldIndicesForSplitting) {
				id.add(obs.values.get(index));
			}
			if (!outputMap.containsKey(id)) {
				DataSet ds = new DataSet("");
				ds.fieldNames = this.fieldNames;
				ds.fieldTypes = this.fieldTypes;
				outputMap.put(id, ds);
			} 
			DataSet ds = outputMap.get(id);
			ds.observations.add(obs);
		}
		if (fieldIndicesForSorting != null && !fieldIndicesForSorting.isEmpty()) {
			for (DataSet ds : outputMap.values()) {
				ds.sortObservations(fieldIndicesForSorting);
			}
		}
		return outputMap;
	}
	
	/**
	 * This method returns the number of observations in the dataset.
	 * @return an integer
	 */
	public int getNumberOfObservations() {
		return observations.size();
	}
	
	
	@SuppressWarnings("rawtypes")
	protected Class getFieldTypeOfThisField(int i) {
		return fieldTypes.get(i);
	}
	
	@SuppressWarnings("rawtypes")
	protected Class getFieldTypeOfThisField(String fieldName) {
		return getFieldTypeOfThisField(getIndexOfThisField(fieldName));
	}
	

	protected Matrix getVectorOfThisField(String fieldName) {
		return getVectorOfThisField(getIndexOfThisField(fieldName));
	}


	protected Matrix getVectorOfThisField(int j) {
		Matrix output = new Matrix(observations.size(), 1);
		for (int i = 0; i < observations.size(); i++) {
			output.setValueAt(i, 0, ((Number) getValueAt(i,j)).doubleValue());
		}
		return output;
	}
	
	
	
	
	/**
	 * This method returns all the possible values in this field.  
	 * @param j the index of the field.
	 * @return a SORTED list of all the possible value.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List getPossibleValuesInThisField(int j) {
		List possibleValues = new ArrayList();
		for (int i = 0; i < observations.size(); i++) {
			Object value = getValueAt(i,j);
			if (!possibleValues.contains(value)) {
				possibleValues.add(value);
			}
		}
		Collections.sort(possibleValues);
		return possibleValues;
	}

	protected Matrix getDummyMatrix(List possibleValues, int fieldIndex) {
		Matrix outputMatrix = new Matrix(getNumberOfObservations(), possibleValues.size());
		for (int i = 0; i < getNumberOfObservations(); i++) {
			int position = possibleValues.indexOf(getValueAt(i, fieldIndex));
			if (position >= 0 && position < outputMatrix.m_iCols) {
				outputMatrix.setValueAt(i, position, 1d);
			}
		}
		return outputMatrix;
	}
	
	public void addObservation(Object[] observationFrame) {
		parseDifferentFields(observationFrame);
		observations.add(new Observation(observationFrame));
	}
	
	private void addFieldName(String name) {
		int index = 0;
		while (fieldNames.contains(name)) {
			name = name.concat(((Integer) index).toString());
		}
		fieldNames.add(name);
	}
	
	public void addField(String name, Object[] field) {
		if (field.length != observations.size()) {
			throw new InvalidParameterException("The number of observations in the new field does not match the number of observations in the dataset!");
		}
		addFieldName(name);
		
		for (int i = 0; i < field.length; i++) {
			observations.get(i).values.add(field[i]);
		}
		
		setClassOfThisField(fieldNames.size() - 1);
//		if (isDouble(fieldNames.size() - 1)) {
//			fieldTypes.add(Double.class);
//		} else {
//			fieldTypes.add(String.class);
//		}
	}

	@Override
	public void save(String filename) throws IOException {
		try {
			FormatWriter<?> writer = FormatWriter.createFormatWriter(false, filename);
			GExportFieldDetails exportField;
			List<FormatField> headerFields = new ArrayList<FormatField>();
			Object[] record;
			for (int i = 0; i < observations.size(); i++) {
				record = new Object[fieldNames.size()];
				for (int j = 0; j < fieldNames.size(); j++) {
					record[j] = getValueAt(i,j);
					if (i == 0) {
						exportField = new GExportFieldDetails(fieldNames.get(j), getValueAt(i,j));
						headerFields.add(writer.convertGExportFieldDetailsToFormatField(exportField));
					}
				}
				if (i == 0) {
					writer.setFields(headerFields);
				}
				writer.addRecord(record);
			}
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	
	@Override
	protected void doThisJob() throws Exception {
		fieldNames.clear();
		observations.clear();

		try {
			FormatReader<?> reader = FormatReader.createFormatReader(originalFilename);
			FormatField field;
			for (int i = 0; i < reader.getHeader().getNumberOfFields(); i++) {
				field = reader.getHeader().getField(i);
				fieldNames.add(field.getName());
			}

			int nbRecords = reader.getRecordCount();
			int recordsRead = 0;
			
			firePropertyChange(REpiceaProgressBarDialog.LABEL, 0d, MessageID.ReadingFileMessage.toString());
			
			Object[] lineRead = reader.nextRecord();
			while (lineRead != null) {
				addObservation(lineRead);
				recordsRead++;
				int progress = (int) ((recordsRead * 100d) / nbRecords);
				firePropertyChange(REpiceaProgressBarDialog.PROGRESS, recordsRead, progress);
				lineRead = reader.nextRecord();
			}
			
			indexFieldType();
			

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void parseDifferentFields(Object[] lineRead) {
		for (int i = 0; i < fieldNames.size(); i++) {
			try {
				lineRead[i] = (Integer) Integer.parseInt(lineRead[i].toString());
			} catch (NumberFormatException e1) {
				try {
					lineRead[i] = (Double) Double.parseDouble(lineRead[i].toString());
				} catch (NumberFormatException e2) {
					lineRead[i] = lineRead[i].toString();
				}
			}
		}
	}
	
	
	/**
	 * Returns the field names in a list. The list is a new list so that changes will
	 * not affect the fieldNames member.
	 * @return a List instance
	 */
	public List<String> getFieldNames() {
		List<String> fieldNames = new ArrayList<String>();
		fieldNames.addAll(this.fieldNames);
		return fieldNames;
	}
	
	public List<Class<?>> getFieldTypes() {
		List<Class<?>> fieldTypes = new ArrayList<Class<?>>();
		fieldTypes.addAll(this.fieldTypes);
		return fieldTypes;
	}
	
	@Override
	public REpiceaTable getUI() {
		if (table == null) {
			REpiceaTableModel model = new REpiceaTableModel(this);
			table = new REpiceaTable(model, false);	// no pop up 
		}
		return table;
	}


	@Override
	public boolean isVisible() {
		return table.isVisible();
	}


	/**
	 * Returns the observations of the data set.
	 * @return a List of Observation instances
	 */
	public List<Observation> getObservations() {
		return observations;
	}
	
	void correctValue(int i, String fieldName, Object newValue, String javaComment, boolean setOtherObservationsToOk, String javaCommentOtherObservations) {
		for (int j = 0; j < getNumberOfObservations(); j++) {
			if (j == i) {
				setValueAt(j, fieldName, newValue, ActionType.Replace);
				setValueAt(j, DataPattern.JavaComments, javaComment, ActionType.Replace);
			} else {
				if (setOtherObservationsToOk) {
					setValueAt(j, DataPattern.JavaComments, javaCommentOtherObservations, ActionType.Replace);
				}
			}
			 
		}
	}
	
	@Override
	public String toString() {
		String output = "";
		output += Arrays.toString(fieldNames.toArray());
		boolean exceeds = false;
		int maxLength;
		if (getNumberOfObservations() <= 100) {
			maxLength = getNumberOfObservations();
		} else {
			exceeds = true;
			maxLength = 100;
		}
		for (int i = 0; i < maxLength; i++) {
			output += "\n" + Arrays.toString(getObservations().get(i).values.toArray());
		}
		if (exceeds) {
			output += "\n" + "Only " + maxLength + " out of " + getNumberOfObservations() + " observations printed!";
		}
		return output;
	}
	
}
