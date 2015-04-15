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

import repicea.io.javadbf.DBFField;


/**
 * This class makes it possible to create objects that contain a field and its description.
 * @author	Mathieu Fortin - August 2009
 * 	modified August - 2010
 */
public class GExportFieldDetails {
	/*
	 * Members of the class
	 */
	private String fieldName;
	private Object value;
	private int type;				// same field as com.linuxense.javadbf.DBFField;
	private int length;
	private int decimalNb;

	
	/**
	 * Constructor
	 * Requires the field name and its value. The type is automatically set through a "instanceof" function
	 * @param name
	 * @param value
	 */
	public GExportFieldDetails(String name, Object value) {
		this.fieldName = name;

		if (value instanceof Number) {
			this.value = value;
			this.type = DBFField.FIELD_TYPE_N;
			this.length = 12;
			if (getValue() instanceof Double || getValue() instanceof Float) {
				this.decimalNb = 3;
			}
		} else {
			this.value = value.toString();
			this.type = DBFField.FIELD_TYPE_C;
			this.length = 12;
		} 
	}
	
	public GExportFieldDetails(String name, Object value, int numberOfDecimals) {
		this(name, value);
		setDecimalNb(numberOfDecimals);
	}
	
		
	/*
	 * Cloning method (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public GExportFieldDetails clone() {
		GExportFieldDetails field = new GExportFieldDetails(getName(), getValue());
		field.setDecimalNb(this.decimalNb);
		return field;
	}
	
	
	
	/*
	 * List of setters
	 */
	public void setDecimalNb(int decimalNb) {this.decimalNb = decimalNb;}
	public void setLength(int length) {this.length = length;}
	public void setValue(Object value) {this.value = value;}

	
	/*
	 * List of getters
	 */
	public String getName() {return this.fieldName;}
	public int getLength() {return this.length;}
	public int getType() {return this.type;}
	public int getDecimalNb() {return this.decimalNb;}
	
	
	
	/**
	 * This method returns the unformatted value of this field.
	 * @return an Object instance
	 */
	public Object getValue() {return value;}
	
}

