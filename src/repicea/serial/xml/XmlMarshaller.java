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

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import repicea.serial.AbstractMarshaller;

/**
 * The XmlMarshaller class handles the serialization of any object into XmlList and XmlEntry objects.
 * @author Mathieu Fortin - November 2012
 */
public final class XmlMarshaller extends AbstractMarshaller<XmlEntry, XmlList>{

	static final String EntriesTag = "entries";
		
	@Override
	protected XmlEntry createSerializableEntryObject(String fieldName, Object value) {
		return new XmlEntry(this, fieldName, value);
	}

	@Override
	protected XmlList createSerializableListObject(Object o) {
		return new XmlList(o);
	}

	@Override
	protected String getEntriesTag() {return EntriesTag;}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addMapEntriesToThisObject(XmlList objToBeSerialized, Set<Entry> entries) {
		objToBeSerialized.add(createSerializableEntryObject(getEntriesTag(), entries.toArray()));
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addCollectionEntriesToThisObject(XmlList objToBeSerialized, Collection coll) {
		objToBeSerialized.add(createSerializableEntryObject(getEntriesTag(), coll.toArray()));
	}


}
