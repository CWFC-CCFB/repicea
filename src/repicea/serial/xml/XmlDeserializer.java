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
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
	 * Constructor.<p>
	 * 
	 * The deserializer can work with files or streams. It first tries to 
	 * locate the physical file represented by the argument filename. If it
	 * fails, it then tries to find the resource and to get it as a stream. 
	 * 
	 * @param filename the file from which the object is deserialized
	 */
	public XmlDeserializer(String filename) {
		super(filename);
	}

	
	@Override
	public Object readObject() throws UnmarshallingException {
		try {
			XmlReaderHandler handler = new XmlReaderHandler();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
	 		InflaterInputStream iis = null;
	 		if (readMode == ReadMode.File) {
	 			try {	// we first assume that the file is compressed
	 				FileInputStream fis = new FileInputStream(file);
	 				iis = new InflaterInputStream(fis);
	 				saxParser.parse(iis, handler);
	 			} catch (Exception e) {
	 				if (iis != null) { // we try to close the inflater and we move on to an uncompressed file
	 					try {
	 						iis.close();
	 					} catch (Exception e2) {}
	 				}
	 				saxParser.parse(file, handler);
	 			}
	 		} else {
	 			BufferedInputStream bis = new BufferedInputStream(is);
	 			bis.mark(10000);	// we mark the buffered input stream for eventual reset in the case the file is not compress
	 			iis = new InflaterInputStream(bis); 
	 			try { // we first assume the stream comes from a compressed file
	 				saxParser.parse(iis, handler);
	 			} catch (Exception e) {
	 				bis.reset();	// we reset the stream to beginning
	 				saxParser.parse(bis, handler);
	 			}
	 		}
	 		XmlUnmarshaller unmarshaller = new XmlUnmarshaller();
	 		Object unmarshalledObj = null;
			unmarshalledObj = unmarshaller.unmarshall(handler.getRoot());
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
