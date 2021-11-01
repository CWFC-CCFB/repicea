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
package repicea.io;

import java.util.ArrayList;
import java.util.List;

public abstract class FormatHeader<P extends FormatField> {
	
	private List<P> fieldList;       /* each 32 bytes */	
	private int numberOfRecords = 0;

	protected FormatHeader() {
		fieldList = new ArrayList<P>();
	}

	/**
	 * This method adds a field into the field list and synchronizes the field index.
	 * @param field the FormatField instance to be added
	 */
	protected void addField(P field) {
		field.setIndex(getFieldList().size());
		getFieldList().add(field);
	}
	
	/**
	 * This method returns the list of fields contained in the header.
	 * @return a Vector of FormatReaderField instances
	 */
	protected List<P> getFieldList() {return fieldList;}
	
	/**
	 * This method returns the index of a particular field.
	 * @param fieldName the name of the field
	 * @return the index of the field or -1 if there is no field that matches the fieldName parameter.
	 */
	public int getIndexOfThisField(String fieldName) {
		for (P field : getFieldList()) {
			if (field.getName().equals(fieldName)) {
				return field.getIndex();
			}
		}
		return -1;
	}

	/**
	 * This method sets the list of fields	
	 * @param fieldList a List of FormatField instances
	 */
	protected void setFieldList(List<P> fieldList) {
		getFieldList().clear();
		for (P field : fieldList) {
			addField(field);
		}
	}
	
	
	/**
	 * This method returns the field contained at the specified index in the list of fields.
	 * @param index the index of the field (an integer)
	 * @return a FormatReaderField instance
	 */
	public P getField(int index) {return fieldList.get(index);}
	
	public int getNumberOfFields() {return fieldList.size();}

	/**
	 * This method returns the number of records in the file.
	 * @return an integer
	 */
	protected int getNumberOfRecords() {return numberOfRecords;}
	
	protected void setNumberOfRecords(int numberOfRecords) {this.numberOfRecords = numberOfRecords;} 
	
}
