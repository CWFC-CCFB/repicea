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

import java.util.ArrayList;
import java.util.List;

import repicea.serial.MarshallingException;
import repicea.serial.UnmarshallingException;

/**
 * The XmlMarshallingUtilities class provides static methods for marshalling and unmarshalling.
 * @author Mathieu Fortin - November 2012
 */
@SuppressWarnings("rawtypes")
public class XmlMarshallingUtilities {

	protected static Class[] boundedClasses;
	static {
		List<Class> classes = new ArrayList<Class>();
		classes.add(XmlEntry.class);
		classes.add(XmlList.class);
		boundedClasses = classes.toArray(new Class[]{});
	}

		
	@SuppressWarnings("serial")
	static final class FakeList extends ArrayList {}
	
	


	
	/**
	 * This method creates a deep clone of an object. Transient fields and graphics components
	 * are not serialized though.
	 * @param obj an Object instance
	 * @return a deep copy of the object
	 * @throws ReflectiveOperationException if an reflection error has occurred
	 * @throws MarshallingException if a marshalling error has occurred
	 * @throws UnmarshallingException if an unmarshalling error has occurred
	 */
	public static Object createDeepCopyOf(Object obj) throws ReflectiveOperationException, MarshallingException, UnmarshallingException {
		XmlMarshaller xmlMarshaller = new XmlMarshaller();
		XmlList marshalledObject = xmlMarshaller.marshall(obj);
		XmlUnmarshaller xmlUnmarshaller = new XmlUnmarshaller();
		Object objCopy = xmlUnmarshaller.unmarshall(marshalledObject);
		return objCopy;
	}
	

	/**
	 * This method marshalles and compares two instances. If the two instances have the same values in their parameters, the method returns true. 
	 * If both objects are null, the method returns true. 
	 * @param obj1 a first object 
	 * @param obj2 a second object to be compared with the first
	 * @return a boolean
	 */
	public static boolean areTheseTwoObjectsComparable(Object obj1, Object obj2) {
		return new XmlMarshallComparator().compareTheseTwoObjects(obj1, obj2);
	}
	
	
	
}
