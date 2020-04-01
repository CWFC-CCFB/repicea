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
package repicea.io.javasql;

import java.util.HashMap;
import java.util.Map;

import repicea.io.FormatField;

public class SQLField extends FormatField {

	private static Map<Class, String> classToTypeMap = new HashMap<Class, String>();
	static {
		classToTypeMap.put(String.class, "VARCHAR");
		classToTypeMap.put(Double.class, "DOUBLE");
		classToTypeMap.put(Float.class, "FLOAT");
		classToTypeMap.put(Integer.class, "INTEGER");
	}
	
	private static Map<String, Class> typeToClassMap = new HashMap<String, Class>();
	static {
		typeToClassMap.put("VARCHAR", String.class);
		typeToClassMap.put("DOUBLE", Double.class);
		typeToClassMap.put("FLOAT", Float.class);
		typeToClassMap.put("INTEGER", Integer.class);
		typeToClassMap.put("TIMESTAMP", String.class);
	}
	
	private final Class clazz;
	private final int precision;
	

	/**
	 * General constructor.
	 * @param name the name of the field
	 * @param className the name of the class
	 * @param precision the length of the text field or the precision of the double.
	 */
	public SQLField(String name, String typeName, int precision) {
		setName(name);
		this.clazz = typeToClassMap.get(typeName);
		this.precision = precision;
	}

	
	/**
	 * This method returns the string code associated with the type.
	 * @return a String
	 */
	public String getTypeName() {
		return classToTypeMap.get(clazz);
	}
	
	protected String getStatement() {
		String statement = getName() + " " + getTypeName();
		if (getTypeName().toLowerCase().equals("varchar")) {
			statement += "(" + precision + ")";
		}
		return statement;
	}

	/**
	 * Return the precision of the field.
	 * @return an integer
	 */
	public int getPrecision() {
		return precision;
	}

}
