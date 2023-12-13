/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge Epicea.
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
package repicea.serial.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import repicea.lang.reflect.ReflectUtility;
import repicea.serial.AbstractMarshaller;
import repicea.serial.MarshallingUtilities;
import repicea.serial.SerializableEntry;

/**
 * The XmlEntry is the basic representation of a field in an object.
 * @author Mathieu Fortin - November 2012
 */
@XmlType
@XmlRootElement
final class XmlEntry implements SerializableEntry {
	
	@XmlElement
	String fieldName;
	
	@XmlElement
	Object value;
	
	XmlEntry() {}
	
	XmlEntry(AbstractMarshaller<XmlEntry, ?> marshaller, String fieldName, Object value) {
		this.fieldName = fieldName;
		if (value != null) {
			if (!ReflectUtility.PrimitiveWrappers.contains(value.getClass()) && !MarshallingUtilities.isStringOrPrimitive(value)) {	// not a primitive
					value = marshaller.marshall(value);
			}
		}
		this.value = value;
	}
	
	@Override
	public String toString() {
		String valueString;
		if (value == null) {
			valueString = "null";
		} else {
			valueString = value.toString();
		}
		return fieldName + "; " + valueString;
	}

	@Override
	public Object getValue() {return value;}

	@Override
	public String getFieldName() {return fieldName;}
}
