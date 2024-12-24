/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2023 His Majesty the King in Right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.serial.json;

import repicea.serial.AbstractMarshaller;
import repicea.serial.MarshallingUtilities;
import repicea.serial.ReflectUtility;
import repicea.serial.SerializableEntry;

/**
 * A class for JSON type entries.
 * @author Mathieu Fortin - December 2023
 */
public final class JSONEntry implements SerializableEntry {

	
	final Object value;
	final String fieldName;
	
	JSONEntry(AbstractMarshaller<JSONEntry,?> marshaller, String fieldName, Object value) {
		this.fieldName = fieldName;
		if (value != null) {
			if (!ReflectUtility.PrimitiveWrappers.contains(value.getClass()) && !MarshallingUtilities.isStringOrPrimitive(value)   ) {			// not a primitive
					value = marshaller.marshall(value);
			}
		}
		this.value = value;
	}
	
	
	JSONEntry(String fieldName, Object value) {
		this.fieldName = fieldName;
		this.value = value;
	}
	
	@Override
	public Object getValue() {return value;}

	@Override
	public String getFieldName() {return fieldName;}

}
