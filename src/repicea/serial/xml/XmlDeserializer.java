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
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import repicea.util.ObjectUtility;

/**
 * The XMLDeserializer class handles the deserialisation from a XML file. See
 * XMLSerializer class.
 * @author Mathieu Fortin - October 2012
 */
public class XmlDeserializer { 

	static {
		String jvmVersion = ObjectUtility.getJVMVersion();
		String[] splittedDigits = jvmVersion.split("\\.");
		int firstDigit = Integer.parseInt(splittedDigits[0]);
		int secondDigit = Integer.parseInt(splittedDigits[1]);
		@SuppressWarnings("unused")
		int thirdDigit = 0;
		if (splittedDigits.length > 2) {
			thirdDigit = Integer.parseInt(splittedDigits[2]);
		}
		if (firstDigit > 1 || secondDigit > 7) {
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
	 * @throws FileNotFoundException
	 */
	public XmlDeserializer(String filename) throws FileNotFoundException {
		file = new File(filename);
		if (!file.isFile()) {
			throw new FileNotFoundException("The file is either a directory or does not exist!");
		}
		readMode = ReadMode.File;
	}

	/**
	 * Constructor.
	 * @param is an InputStream instance from which the object is deserialized
	 * @throws FileNotFoundException
	 */
	public XmlDeserializer(InputStream is) {
		this.is = is;
		readMode = ReadMode.InputStream;
	}

	
	
	
	/**
	 * This method returns the object that has been deserialized.
	 * @return an Object instance
	 * @throws XmlMarshallException 
	 */
	public Object readObject() throws XmlMarshallException {
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(XmlMarshallingUtilities.boundedClasses);
	 		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	 		Object obj;
	 		if (readMode == ReadMode.File) {
	 			obj = jaxbUnmarshaller.unmarshal(file);
	 		} else {
	 			obj = jaxbUnmarshaller.unmarshal(is);
	 		}
	 		XmlUnmarshaller unmarshaller = new XmlUnmarshaller();
	 		Object unmarshalledObj = null;
			unmarshalledObj = unmarshaller.unmarshall((XmlList) obj);
	 		return unmarshalledObj;
		} catch (Exception e) {
			e.printStackTrace();
			throw new XmlMarshallException(e);
		}
	}

}
