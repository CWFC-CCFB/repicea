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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.zip.InflaterInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import repicea.lang.REpiceaSystem;
import repicea.serial.xml.XmlMarshallingUtilities.FakeList;

/**
 * The XMLDeserializer class handles the deserialisation from a XML file. See
 * XMLSerializer class.
 * @author Mathieu Fortin - October 2012
 */
public class XmlDeserializer { 

	static {
		if (REpiceaSystem.isCurrentJVMLaterThanThisVersion("1.7")) {
			XmlSerializerChangeMonitor.registerClassNameChange("java.util.HashMap$Entry", "java.util.AbstractMap$SimpleEntry");
		}
	}
	
	
	private static enum ReadMode {File, InputStream}
	
	private File file;
	private InputStream is;
	private final ReadMode readMode;
	
	
	/**
	 * Constructor.
	 * @param filename the file from which the object is deserialized
	 */
	public XmlDeserializer(String filename) {
		file = new File(filename);
		if (!file.isFile()) {
			throw new InvalidParameterException("The file is either a directory or does not exist!");
		}
		readMode = ReadMode.File;
	}

	
	/**
	 * Constructor.
	 * @param is an InputStream instance from which the object is deserialized
	 */
	public XmlDeserializer(InputStream is) {
		this.is = is;
		readMode = ReadMode.InputStream;
	}
	
	
	/**
	 * This method returns the object that has been deserialized.
	 * @return an Object instance
	 * @throws XmlMarshallException if an XML marshalling error has occurred
	 */
	public Object readObject() throws XmlMarshallException {
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(XmlMarshallingUtilities.boundedClasses);
	 		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	 		Object obj = null;
	 		InflaterInputStream iis = null;
	 		if (readMode == ReadMode.File) {
	 			try {	// we first assume that the file is compressed
	 				FileInputStream fis = new FileInputStream(file);
	 				iis = new InflaterInputStream(fis);
		 			obj = jaxbUnmarshaller.unmarshal(iis);
	 			} catch (UnmarshalException e) {
	 				if (iis != null) { // we try to close the inflater and we move on to an uncompressed file
	 					try {
	 						iis.close();
	 					} catch (Exception e2) {}
	 				}
		 			obj = jaxbUnmarshaller.unmarshal(file);
	 			}
	 		} else {
	 			BufferedInputStream bis = new BufferedInputStream(is);
	 			bis.mark(10000);	// we mark the buffered input stream for eventual reset in the case the file is not compress
	 			iis = new InflaterInputStream(bis); 
	 			try { // we first assume the stream comes from a compressed file
		 			obj = jaxbUnmarshaller.unmarshal(iis);
	 			} catch (Exception e) {
	 				bis.reset();	// we reset the stream to beginning
		 			obj = jaxbUnmarshaller.unmarshal(bis); // we now assume the stream comes from an uncompressed file
	 			}
	 		}
	 		XmlUnmarshaller unmarshaller = new XmlUnmarshaller();
	 		Object unmarshalledObj = null;
			unmarshalledObj = unmarshaller.unmarshall((XmlList) obj);
			if (unmarshalledObj instanceof FakeList) {	// this was a simple object or a String then
				unmarshalledObj = ((FakeList) unmarshalledObj).get(0);
			}
	 		return unmarshalledObj;
		} catch (XmlMarshallException e1) {
			e1.printStackTrace();
			throw e1;
		} catch (Exception e2) {
			e2.printStackTrace();
			throw new XmlMarshallException(e2);
		}
	}

}
