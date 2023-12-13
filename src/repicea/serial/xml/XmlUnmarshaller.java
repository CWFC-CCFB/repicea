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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import repicea.serial.AbstractUnmarshaller;
import repicea.serial.UnmarshallingException;

/**
 * The XmlUnmarshaller class handles the deserialization from XmlEntry and XmlList classes.
 * @author Mathieu Fortin - November 2012
 */
public final class XmlUnmarshaller extends AbstractUnmarshaller<XmlEntry, XmlList>{

	@Override
	protected void performPostMarshallingActionIfAny(Object newInstance) {
		if (newInstance instanceof PostXmlUnmarshalling) {
			((PostXmlUnmarshalling) newInstance).postUnmarshallingAction();
		}
	}

	@Override
	protected String getEntriesTag() {return XmlMarshaller.EntriesTag;}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void putEntriesIntoMap(Class clazz, Object newInstance, Object entries) throws UnmarshallingException {
		if (entries != null && entries.getClass().isArray()) {
			Object[] mapEntries = (Object[]) entries;
			if (mapEntries.length > 0) {
				try {
					Method putMethod = clazz.getMethod("put", Object.class, Object.class);
					Class declaringClass = putMethod.getDeclaringClass();
					if (declaringClass.getClassLoader() != null) {
						issueWarning(clazz, "put");
					}
					for (Object mapEntry : mapEntries) {
						if (mapEntry instanceof Entry) {
							((Map) newInstance).put(((Entry) mapEntry).getKey(), ((Entry) mapEntry).getValue()); 
						} else {
							throw new UnmarshallingException("Map entries are expected to come as Entry instance!");
						}
					}
				} catch (Exception e) {
					throw new UnmarshallingException(e);
				}
			}
		} else {
			throw new UnmarshallingException("The XmlDeserializer expects map entries to come as an array!");
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected final void addEntriesToCollection(Class clazz, Object newInstance, Object entries) throws UnmarshallingException {
		if (entries != null && entries.getClass().isArray()) {
			Object[] mapEntries = (Object[]) entries;
			if (mapEntries.length > 0) {
				try {
					Method addMethod = clazz.getMethod("add", Object.class);
					Class declaringClass = addMethod.getDeclaringClass();
					if (declaringClass.getClassLoader() != null) {
						issueWarning(clazz, "add");
					};
					for (Object listEntry : mapEntries) {
						((Collection) newInstance).add(listEntry);  // FIXME this does not work if the add method has been overriden
					}
				} catch (Exception e) {
					throw new UnmarshallingException(e);
				}
			}
		} else {
			throw new UnmarshallingException("The XmlDeserializer expects map entries to come as an array!");
		}
	}

	@Override
	protected Object unmarshalMapOrCollectionEntries(XmlEntry entry) throws ReflectiveOperationException, UnmarshallingException {
		return unmarshall((XmlList) entry.getValue());
	}


	
}
