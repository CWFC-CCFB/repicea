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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import repicea.serial.AbstractDeserializer;
import repicea.serial.UnmarshallingException;
import repicea.serial.xml.XmlMarshallingUtilities.FakeList;

/**
 * The XMLDeserializer class handles the deserialisation from a XML file. See
 * XMLSerializer class.
 * @author Mathieu Fortin - October 2012
 */
public final class XmlDeserializer extends AbstractDeserializer { 

	
	/**
	 * Constructor.
	 * @param filename the file from which the object is deserialized
	 */
	public XmlDeserializer(String filename) {
		super(filename);
	}

	
	/**
	 * Constructor.
	 * @param is an InputStream instance from which the object is deserialized
	 */
	public XmlDeserializer(InputStream is) {
		super(is);
	}
	

	@Override
	public Object readObject() throws UnmarshallingException {
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
		} catch (UnmarshallingException e1) {
			e1.printStackTrace();
			throw e1;
		} catch (Exception e2) {
			e2.printStackTrace();
			throw new UnmarshallingException(e2);
		}
	}

}
