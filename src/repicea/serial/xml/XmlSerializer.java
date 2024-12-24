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

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import repicea.serial.AbstractSerializer;
import repicea.serial.MarshallingException;
import repicea.serial.MarshallingUtilities;
import repicea.serial.xml.XmlMarshallingUtilities.FakeList;

/**
 * The XMLSerializer class handles the serialization process for any Object. The
 * OutputStream instances are also handled by this class.
 * @author Mathieu Fortin - Octobre 2012
 */
public final class XmlSerializer extends AbstractSerializer { 

	/**
	 * Constructor.
	 * @param filename the file that serves as output
	 * @param enableCompression true to use compression or false otherwise
	 */
	public XmlSerializer(String filename, boolean enableCompression) {
		super(filename, enableCompression);
	}
	
	/**
	 * Constructor with compression.
	 * @param filename the xml file that serves as output
	 */
	public XmlSerializer(String filename)  {
		this(filename, true);
	}
	
	
	@SuppressWarnings("unchecked")
	public void writeObject(Object obj) throws MarshallingException {
		OutputStream os = null;
		try {
			XmlMarshaller marshaller = new XmlMarshaller();
			if (MarshallingUtilities.isStringOrPrimitive(obj)) { // then we embed the object into a wrapper
				FakeList wrapper = new FakeList();
				wrapper.add(obj);
				obj = wrapper;
			}
			XmlList xmlObject = marshaller.marshall(obj);
			FileOutputStream fos = new FileOutputStream(file);
			os = enableCompression ?
					new DeflaterOutputStream(fos) :
						fos;
			XmlWriterHandler handler = new XmlWriterHandler(xmlObject);
			String xmlString = handler.sb.toString(); 
			os.write(xmlString.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			throw new MarshallingException(e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
