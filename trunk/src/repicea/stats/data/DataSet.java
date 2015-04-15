/*
 * This file is part of the repicea-statistics library.
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
package repicea.stats.data;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repicea.io.FormatField;
import repicea.io.FormatReader;
import repicea.io.FormatWriter;
import repicea.io.GExportFieldDetails;
import repicea.io.Loadable;
import repicea.io.Saveable;
import repicea.math.Matrix;

/**
 * The DataSet class contains many observations and implements the method to read a dataset with a FormatReader instance.
 * @author Mathieu Fortin - November 2012
 */
public class DataSet implements Loadable, Saveable {

	protected List<String> fieldNames;
	protected List<Class<?>> fieldTypes;
	private List<Observation> observations;

	/**
	 * General constructor.
	 * @param filename the name of the file to be read
	 */
	public DataSet(String filename) {
		this();
		try {
			load(filename);
			indexFieldType();
		} catch (Exception e) {
			System.out.println("An error occured while reading the file : " + filename);
			e.printStackTrace();
		}
	}

	/**
	 * Basic constructor for derived classes.
	 */
	protected DataSet() {
		fieldNames = new ArrayList<String>();
		fieldTypes = new ArrayList<Class<?>>();
		observations = new ArrayList<Observation>();
	}
	
	@Override
	public void load(String filename) {
		fieldNames.clear();
		observations.clear();

		try {
			FormatReader<?> reader = FormatReader.createFormatReader(filename);
			FormatField field;
			for (int i = 0; i < reader.getHeader().getNumberOfFields(); i++) {
				field = reader.getHeader().getField(i);
				fieldNames.add(field.getName());
			}

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
				lineRead = reader.nextRecord();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
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
	
	
	
}
