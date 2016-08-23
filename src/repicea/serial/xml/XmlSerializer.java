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

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import repicea.serial.xml.XmlMarshallingUtilities.FakeList;

/**
 * The XMLSerializer class handles the serialization process for any Object. The
 * OutputStream instances are also handled by this class.
 * @author Mathieu Fortin - Octobre 2012
 */
public class XmlSerializer { 

	private File file;
	
	/**
	 * Constructor.
	 * @param filename the xml file that serves as output
	 * @throws FileNotFoundException if something goes wrong with the file
	 */
	public XmlSerializer(String filename) throws FileNotFoundException {
		file = new File(filename);
		if (file.isDirectory()) {
			throw new FileNotFoundException("The filename parameter denotes a directory!");
			
		}
	}
	
	/**
	 * This method writes an object to the xml file.
	 * @param obj any Object instance
	 * @throws XmlMarshallException 
	 */
	@SuppressWarnings("unchecked")
	public void writeObject(Object obj) throws XmlMarshallException {
		try {
			XmlMarshaller marshaller = new XmlMarshaller();
			if (XmlMarshallingUtilities.isStringOrSimpleObject(obj)) { // then we embed the object into a wrapper
				FakeList wrapper = new FakeList();
				wrapper.add(obj);
				obj = wrapper;
			}
			Object xmlObject = marshaller.marshall(obj);
			JAXBContext jaxbContext = JAXBContext.newInstance(XmlMarshallingUtilities.boundedClasses);
			Marshaller jabxMarshaller = jaxbContext.createMarshaller();
			jabxMarshaller.marshal(xmlObject, file);
		} catch (Exception e) {
			e.printStackTrace();
			throw new XmlMarshallException(e);
		}
		
	}

//	@Override
//	public void close() {
//		e.close();
//	}
	
}
