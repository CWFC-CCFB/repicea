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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The XmlMarshaller class handles the serialization of any object into XmlList and XmlEntry objects.
 * @author Mathieu Fortin - November 2012
 */
public final class XmlMarshaller {

	private Map<Class<?>, Set<Integer>> registeredObjects;
	
	public XmlMarshaller() {
		registeredObjects = new HashMap<Class<?>, Set<Integer>>();
	}
	
	private void registerObject(Object obj) {
		if (!registeredObjects.containsKey(obj.getClass())) {
			registeredObjects.put(obj.getClass(), new HashSet<Integer>());
		}
		Set<Integer> hashCodes = registeredObjects.get(obj.getClass());
		int hashCode = System.identityHashCode(obj);
		hashCodes.add(hashCode);
	}
	
	private boolean hasObjectBeenRegistered(Object obj) {
		if (registeredObjects.containsKey(obj.getClass())) {
			int hashCode = System.identityHashCode(obj);
			return registeredObjects.get(obj.getClass()).contains(hashCode); 
		} else {
			return false;
		}
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public XmlList marshall(Object obj) {
		XmlList xmlObj = new XmlList(obj);
		if (!hasObjectBeenRegistered(obj)) {
			registerObject(obj);
//			System.out.println("Serializing this class : " + obj.getClass().getSimpleName());
			if (xmlObj.isArray) {
				int length = Array.getLength(obj);
				for (int i = 0; i < length; i++) {
					xmlObj.add(new XmlEntry(this, ((Integer) i).toString(), Array.get(obj, i)));
				}
			} else if (obj instanceof Enum) {				// enum case: just the name is saved
				xmlObj.add(new XmlEntry(this, "name", ((Enum<?>) obj).name()));
			} else if (obj instanceof Class) {
				xmlObj.add(new XmlEntry(this, "class", ((Class) obj).getName()));
			} else if (obj instanceof Map) {				
				Set<Entry> entries = ((Map) obj).entrySet();
				xmlObj.add(new XmlEntry(this, "entries", entries.toArray()));
				List<Field> selectedObjectFields = XmlMarshallingUtilities.retrieveAllNonStaticAndNonTransientFieldFromClass(obj.getClass());
				xmlObj.addAll(formatToXmlEntries(selectedObjectFields, obj));
			} else if (obj instanceof Collection) {				
				xmlObj.add(new XmlEntry(this, "entries", ((Collection) obj).toArray()));
				List<Field> selectedObjectFields = XmlMarshallingUtilities.retrieveAllNonStaticAndNonTransientFieldFromClass(obj.getClass());
				xmlObj.addAll(formatToXmlEntries(selectedObjectFields, obj));
			} else {
				List<Field> selectedObjectFields = XmlMarshallingUtilities.retrieveAllNonStaticAndNonTransientFieldFromClass(obj.getClass());
				xmlObj.addAll(formatToXmlEntries(selectedObjectFields, obj));
			}
		}
		return xmlObj;
	}
	
		
	private List<XmlEntry> formatToXmlEntries(List<Field> fields, Object root) {
		List<XmlEntry> entries = new ArrayList<XmlEntry>();
		for (Field field : fields) {
			String fieldName = field.getName();
			field.setAccessible(true);
			Object value = null;
			try {
				value = field.get(root);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			entries.add(new XmlEntry(this, fieldName, value));
		}
		return entries;
	}

}
