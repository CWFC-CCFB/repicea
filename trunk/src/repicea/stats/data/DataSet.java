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

import java.awt.BorderLayout;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;

import repicea.app.AbstractGenericTask;
import repicea.gui.REpiceaDialog;
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
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The DataSet class contains many observations and implements the method to read a dataset with a FormatReader instance.
 * @author Mathieu Fortin - November 2012
 */
public class DataSet extends AbstractGenericTask implements Saveable, REpiceaUIObject {

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
	private List<Observation> observations;
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
	 * This method returns any object in the dataset at row i and column j.
	 * @param i the index of the row
	 * @param j the index of the column
	 * @return an Object instance
	 */
	protected Object getValueAt(int i, int j) {
		return observations.get(i).values.get(j);
	}
		
	private void indexFieldType() {
		fieldTypes.clear();
		for (int j = 0; j < fieldNames.size(); j++) {
			if (isDouble(j)) {
				fieldTypes.add(Double.class);
			} else {
				fieldTypes.add(String.class);
			}
		}
	}
	
	private boolean isDouble(int indexJ) {
		boolean isDouble = true;
		for (int i = 0; i < observations.size(); i++) {
			if (!(getValueAt(i,indexJ) instanceof Double)) {
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
	public Map<DataGroup, DataSet> splitAndOrder(List<Integer> fieldIndicesForSplitting, List<Integer> fieldIndicesForSorting) {
		Map<DataGroup, DataSet> outputMap = new HashMap<DataGroup, DataSet>();
		for (Observation obs : observations) {
			DataGroup id = new DataGroup();
			for (Integer index : fieldIndicesForSplitting) {
				id.add(obs.values.get(index));
			}
			if (!outputMap.containsKey(id)) {
				DataSet ds = new DataSet("");
				ds.fieldNames.addAll(this.fieldNames);
				ds.fieldTypes.addAll(this.fieldTypes);
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
	
	protected DataPattern getPatternForThisField(int fieldIndex) {
		DataPattern pattern = new DataPattern();
		for (Observation obs : observations) {
			pattern.add(obs.values.get(fieldIndex));
		}
		return pattern; 
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
			output.m_afData[i][0] = (Double) getValueAt(i,j);
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
				outputMatrix.m_afData[i][position] = 1d;
			}
		}
		return outputMatrix;
	}
	
	public void addObservation(Object[] observationFrame) {
		observations.add(new Observation(observationFrame));
	}
	
	public void addField(String name, Object[] field) {
		if (field.length != observations.size()) {
			throw new InvalidParameterException("The number of observations in the new field does not match the number of observations in the dataset!");
		}
		int index = 0;
		while (fieldNames.contains(name)) {
			name = name.concat(((Integer) index).toString());
		}
		fieldNames.add(name);
		
		for (int i = 0; i < field.length; i++) {
			observations.get(i).values.add(field[i]);
		}
		
		if (isDouble(fieldNames.size() - 1)) {
			fieldTypes.add(Double.class);
		} else {
			fieldTypes.add(String.class);
		}
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
				for (int i = 0; i < fieldNames.size(); i++) {
					try {
						lineRead[i] = Double.parseDouble(lineRead[i].toString());
					} catch (Exception e) {
						lineRead[i] = lineRead[i].toString();
					}
				}
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
	
	
	private static class FakeDialog extends REpiceaDialog {

		REpiceaTable table;
		FakeDialog(REpiceaTable table) {
			this.table = table;
			initUI();
			pack();
			setVisible(true);
		}
		
		@Override
		public void listenTo() {}

		@Override
		public void doNotListenToAnymore() {}

		@Override
		protected void initUI() {
			getContentPane().setLayout(new BorderLayout());
			JScrollPane scrollPane = new JScrollPane(table);
			getContentPane().add(scrollPane, BorderLayout.CENTER);
		}
	}
	
	protected static DataSet recomposeDataSet(Collection<DataSet> dataSets) {
		DataSet ds = new DataSet("");
		int i = 0;
		for (DataSet subDs : dataSets) {
			if (i == 0) {
				ds.fieldNames.addAll(subDs.fieldNames);
				ds.fieldTypes.addAll(subDs.fieldTypes);
			}
			ds.observations.addAll(subDs.observations);
			i++;
		}
		return ds;
	}
	
	protected static void addCorrectedField(Map<DataGroup, DataSet> dataSetMap, 
			List<DataGroup> groups,
			Object pattern,
			String fieldName,
			String correctionMethod) {
		for (DataGroup dg : groups) {
			DataSet ds = dataSetMap.get(dg);
			Object[] field = DataPattern.getHomogeneousField(ds.getNumberOfObservations(), pattern);
			ds.addField(fieldName, field);
			field = DataPattern.getHomogeneousField(ds.getNumberOfObservations(), correctionMethod);
			ds.addField(fieldName.concat("Met"), field);
		}
	}
	
	
	
}
