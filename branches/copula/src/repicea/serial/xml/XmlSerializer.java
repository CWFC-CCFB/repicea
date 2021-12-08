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
import java.io.FileOutputStream;
import java.security.InvalidParameterException;
import java.util.zip.DeflaterOutputStream;

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
	private final boolean enableCompression;
	
	
	/**
	 * Constructor.
	 * @param filename the xml file that serves as output
	 * @param enableCompression true to use compression or false otherwise
	 */
	public XmlSerializer(String filename, boolean enableCompression) {
		this.enableCompression = enableCompression;
		file = new File(filename);
		if (file.isDirectory()) {
			throw new InvalidParameterException("The filename parameter denotes a directory!");
		}
	}

	
	/**
	 * Constructor with compression.
	 * @param filename the xml file that serves as output
	 */
	public XmlSerializer(String filename)  {
		this(filename, true);
	}
	
	
	/**
	 * This method writes an object to the xml file.
	 * @param obj any Object instance
	 * @throws XmlMarshallException 
	 */
	@SuppressWarnings("unchecked")
	public void writeObject(Object obj) throws XmlMarshallException {
		DeflaterOutputStream dos = null;
		try {
			XmlMarshaller marshaller = new XmlMarshaller();
			if (XmlMarshallingUtilities.isStringOrPrimitive(obj)) { // then we embed the object into a wrapper
				FakeList wrapper = new FakeList();
				wrapper.add(obj);
				obj = wrapper;
			}
			Object xmlObject = marshaller.marshall(obj);
			JAXBContext jaxbContext = JAXBContext.newInstance(XmlMarshallingUtilities.boundedClasses);
			Marshaller jabxMarshaller = jaxbContext.createMarshaller();
			if (enableCompression) {
				FileOutputStream fos = new FileOutputStream(file);
				dos = new DeflaterOutputStream(fos);
				jabxMarshaller.marshal(xmlObject, dos);
			} else {
				jabxMarshaller.marshal(xmlObject, file);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new XmlMarshallException(e);
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}

//	@Override
//	public void close() {
//		e.close();
//	}

	
	
}
