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

import java.io.Serializable;
import java.security.InvalidParameterException;

import repicea.gui.REpiceaUIObject;
import repicea.io.FormatField;


/**
 * This class contains the field details for the importation
 * @author Jean-Francois Lavoie and Mathieu Fortin - August 2009
 */
public final class ImportFieldElement implements Cloneable, 
										Serializable, 
										REpiceaUIObject {

	private static final long serialVersionUID = 20100804L;
	
	public static enum FieldType {
		String(String.class),
		Double(Double.class),
		Integer(Integer.class);
		
		final Class clazz;
		
		FieldType(Class clazz) {
			this.clazz = clazz;
		}

		boolean isAlreadyInTheAppropriateFormat(Object obj) {
			return clazz.equals(obj.getClass());
		}
		
	}
	
	
	/*
	 * Members of this class
	 */
	public String description;				// description of the field
	private String fieldName;				// name of the field
	public String propertyName;				// property in which the corresponding DBF field is saved
	private int matchingFieldIndex;			// index of the corresponding DBF field in the input file
	public boolean isOptional;				// specifies whether or not the field is optional
	public String helpDescription;			// a string that appears in the popup window to help the used
	public FieldType fieldTypeClass;
	protected Enum<?> fieldID;					// an enum that can be used to refer to the field
	
	private transient ImportFieldElementPanel guiInterface;
	
	/**
	 * General constructor
	 * @param fieldID the field id enum variable which implements the LevelProviderEnum
	 * @param description a String that describes the field
	 * @param propertyName the property name for saving
	 * @param isOptional a boolean that specifies whether the field is optional
	 * @param helpDescription a String that helps the user (may appear in a popup window
	 * @param fieldType an FieldType enum 
	 */
	@SuppressWarnings("rawtypes")
	public ImportFieldElement(LevelProviderEnum fieldID,
			String description, 
			String propertyName, 
			boolean isOptional, 
			String helpDescription,
			FieldType fieldType) {
		if (!(fieldID instanceof Enum)) {
			throw new InvalidParameterException("The fieldID parameter should be an Enum that implements the LevelProviderEnum interface");
		} else {
			this.fieldID = (Enum) fieldID;
		}
		this.description = description;
		this.propertyName = propertyName;
		this.isOptional = isOptional;
		this.helpDescription = helpDescription;
		this.fieldTypeClass = fieldType;
		setFieldMatch(FormatField.NON_AVAILABLE_FIELD);
	}

	
	/**
	 * Short constructor for JUnit test
	 * @param fieldID the field id enum variable
	 * @param fieldIndex the matching field index in the dbf file
	 * @param fieldTypeClass an FieldType enum 
	 */
	@SuppressWarnings("rawtypes")
	public ImportFieldElement(LevelProviderEnum fieldID,
			int fieldIndex,
			FieldType fieldTypeClass) {
		if (!(fieldID instanceof Enum)) {
			throw new InvalidParameterException("The fieldID parameter should be an Enum that implements the LevelProviderEnum interface");
		} else {
			this.fieldID = (Enum) fieldID;
		}
		this.matchingFieldIndex = fieldIndex;
		this.fieldTypeClass = fieldTypeClass;
		setFieldMatch(FormatField.NON_AVAILABLE_FIELD);
	}

	
	/**
	 * This method sets the name and the match index of the ImportFieldElement instance.
	 * @param formatField a FormatField instance
	 */
	public void setFieldMatch(FormatField formatField) {
		fieldName = formatField.getName();
//		matchingFieldIndex = formatField.getIndex();
		setMatchingFieldIndex(formatField.getIndex());
	}

	void setMatchingFieldIndex(int index) {
		matchingFieldIndex = index;
	}
	
//	/**
//	 * This method returns the level at which the field applies.
//	 * @return an Enum variable
//	 */
//	public Enum<?> getLevel() {
//		if (fieldID instanceof LevelProviderEnum) {
//			return ((LevelProviderEnum) fieldID).getFieldLevel();
//		} else {
//			return null;
//		}
//	}

	@Override
	public ImportFieldElementPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new ImportFieldElementPanel(this);
		}
		return guiInterface;
	}
	


	public String getFieldName() {return fieldName;}
	
	public int getMatchingFieldIndex() {return matchingFieldIndex;}
	

	/**
	 * This method returns the Enum variable who serves as ID for the field.
	 * @return an Enum variable instance
	 */
	@SuppressWarnings("rawtypes")
	public Enum getFieldID() {return fieldID;}


	/**
	 * This method returns the type of field which can be a String, a Double or an Integer.
	 * @return a FieldType enum variable
	 */
	public FieldType getFieldType() {return fieldTypeClass;}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	/**
	 * Provide the field enum, its type and the index of the field it is matched to.
	 * @return a String
	 */
	public String getShortDescription() {
		String optional;
		if (isOptional) {
			optional = " (optional)";
		} else {
			optional = " (mandatory)";
		}
		return getFieldID().name() + optional + "; " + fieldTypeClass.name() + "; Match: " + matchingFieldIndex;
	}
	
}