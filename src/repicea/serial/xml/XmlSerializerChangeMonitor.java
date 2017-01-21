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

import java.util.HashMap;
import java.util.Map;

/**
 * The XmlSerializerChangeMonitor records all the changes in package and class names. During
 * the unmarshalling, the XmlDeserializer checks if the name read in the file has been changed.
 * @author Mathieu Fortin - November 2012
 */
public class XmlSerializerChangeMonitor {

	static Map<String, String> ClassNameChangeMap = new HashMap<String, String>();
	static Map<String, Map<String, String>> EnumNameChangeMap = new HashMap<String, Map<String, String>>();

	/**
	 * This method register a class name change.
	 * @param oldName the former name of this class
	 * @param newName the new name of this class
	 */
	public static void registerClassNameChange(String oldName, String newName) {
		ClassNameChangeMap.put(oldName, newName);
	}


	/**
	 * This method register an enum name change.
	 * @param oldName the former name of this enum
	 * @param newName the new name of this enum
	 */
	public static void registerEnumNameChange(String enumClass, String oldName, String newName) {
		if (!EnumNameChangeMap.containsKey(enumClass)) {
			EnumNameChangeMap.put(enumClass, new HashMap<String, String>());
		}
		Map<String, String> innerMap = EnumNameChangeMap.get(enumClass);
		innerMap.put(oldName, newName);
	}

	
	
	
	
}
