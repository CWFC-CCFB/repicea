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
import java.util.Vector;

/**
 * This class handles the records for export procedure. A GExportRecord contains a list of fields.
 */
public class GExportRecord {
	/*
	 * Member of this class
	 */
	private List<GExportFieldDetails> fieldList;
	
	/**
	 * Constructor
	 */
	public GExportRecord() {
		fieldList = new Vector<GExportFieldDetails>();
	}
	
	/**
	 * This method provides the list of fields contained in this record.
	 * @return a Vector of GExportFieldDetails instances
	 */
	public List<GExportFieldDetails> getFieldList() {
		return fieldList;
	}
	
	/**
	 * This method returns the list of field names.
	 * @return a Vector of String instances
	 */
	public Vector<String> getFieldNameList() {
		Vector<String> oVec = new Vector<String>();
		if (fieldList.size()>=1) {
			for (int i=0; i<fieldList.size(); i++) {
				oVec.add(fieldList.get(i).getName());
			}
		}
		return oVec;
	}
	
	/**
	 * this method returns a List instance that contains all the values in this record.
	 * @return a List of objects
	 */
	public List<Object> getValues() {
		List<Object> oVec = new ArrayList<Object>();
		for (GExportFieldDetails details : fieldList) {
			oVec.add(details.getValue());
		}
		return oVec;
	}
	
	/**
	 * This method provides a deep clone of the record.
	 */
	@Override
	public GExportRecord clone() {
		GExportRecord record = new GExportRecord();
		if (!this.fieldList.isEmpty()) {
			for (int i = 0; i < this.fieldList.size(); i++) {
				record.fieldList.add(this.fieldList.get(i).clone());
			}
		}
		return record;
	}
	
	/**
	 * This method serves to add a field to the record.
	 * @param fieldDetails a GExportFieldDetails object
	 */
	public void addField(GExportFieldDetails fieldDetails) {
		this.fieldList.add(fieldDetails);
	}
	
	/**
	 * This method makes it possible to remove a field from the records
	 * @param i the index of the field
	 */
	public void removeField(int i) {
		fieldList.remove(i);
	}
}
