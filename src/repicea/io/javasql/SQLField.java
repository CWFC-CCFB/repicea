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
package repicea.io.javasql;

import repicea.io.FormatField;

public class SQLField extends FormatField {

	private int type;		// following java.sql.Type
	private int length;
	
	/**
	 * General constructor 1.
	 * @param name the name of the field
	 * @param type the type of the field according to java.sql.Type
	 */
	public SQLField(String name, int type) {
		setName(name);
		this.type = type;
		this.length = 256;
	}
	

	/**
	 * Special constructor adapted to text fields.
	 * @param name the name of the field
	 * @param type the type of the field according to java.sql.Type
	 * @param length the length of the text field.
	 */
	public SQLField(String name, int type, int length) {
		setName(name);
		this.type = type;
		this.length = length;
	}

	/**
	 * Returns the type of the field (see java.sql.Type).
	 * @return an Integer
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * This method returns the string code associated with the type.
	 * @return a String
	 */
	public String getTypeCode() {
		if (getType() == java.sql.Types.DOUBLE) {
			return "double";
		} else if (getType() == java.sql.Types.FLOAT) {
			return "float";
		} else if (getType() == java.sql.Types.INTEGER) {
			return "integer";
		} else if (getType() == java.sql.Types.VARCHAR) {
			return "varchar(" + length + ")";
		} else return null;
	}

}
