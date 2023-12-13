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

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import repicea.serial.SerializableList;

/**
 * The XmlList class is the basic serialization for any object.
 * @author Mathieu Fortin - November 2012
 */
@XmlType
@XmlRootElement
public class XmlList implements SerializableList<XmlEntry> {
	
	@XmlElement
	String className;
	
	@XmlElement
	int refHashCode;
	
	@XmlElement
	boolean isArray;

	@XmlElement
	boolean isPrimitive;
	
	@XmlElement
	List<XmlEntry> list = new LinkedList<XmlEntry>();

	XmlList() {}
	
	XmlList(Object root) {
		this.isArray = root.getClass().isArray();
		if (isArray) {
			className = root.getClass().getComponentType().getName();
			isPrimitive = root.getClass().getComponentType().isPrimitive();
		} else {
			className = root.getClass().getName();
			isPrimitive = root.getClass().isPrimitive();
		}
		refHashCode = System.identityHashCode(root);
	}
	
	@Override
	public void add(XmlEntry entry) {
		list.add(entry);
	}
	
	@Override
	public void addAll(List<XmlEntry> entries) {
		list.addAll(entries);
	}
	
	@Override
	public List<XmlEntry> getEntries() {
		return list;
	}

	@Override
	public String toString() {return className + refHashCode;}

	@Override
	public boolean isArray() {return isArray;}

	@Override
	public int getRefHashCode() {return refHashCode;}

	@Override
	public boolean isPrimitive() {return isPrimitive;}

	@Override
	public String getClassName() {return className;}

}
