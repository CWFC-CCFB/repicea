/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2023 His Majesty the King in Right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.serial.json;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

import com.cedarsoftware.io.JsonIo;
import com.cedarsoftware.io.JsonReader;

import repicea.serial.AbstractDeserializer;
import repicea.serial.UnmarshallingException;

/**
 * A class to deserialize object that were previously serialized
 * using the JSONSerializer.
 * @author Mathieu Fortin 2023
 */
public final class JSONDeserializer extends AbstractDeserializer {

	/**
	 * Constructor.<p>
	 * 
	 * The deserializer can work with files or streams. It first tries to 
	 * locate the physical file represented by the argument filename. If it
	 * fails, it then tries to find the resource and to get it as a stream. 
	 * 
	 * @param filename the file from which the object is deserialized
	 */
	public JSONDeserializer(String filename) {
		super(filename);
	}


	private String getStringFromInputStream(InputStream is) throws IOException {
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    byte[] byteArray = new byte[10000];
	    int nRead;
	    while ((nRead = is.read(byteArray)) != -1) {
	    	buffer.write(byteArray, 0, nRead);
	    }
	    buffer.flush();
	    byte[] targetArray = buffer.toByteArray();

	    return new String(targetArray);
	}
	
	@Override
	public Object readObject() throws UnmarshallingException {
		try {
	 		String jsonStr = null;
	 		InflaterInputStream iis = null;
	 		if (readMode == ReadMode.File) {
 				FileInputStream fis = null;
	 			try {	// we first assume that the file is compressed
	 				fis = new FileInputStream(file);
	 				iis = new InflaterInputStream(fis);
	 				jsonStr = getStringFromInputStream(iis);
 					try {
 						iis.close();
 					} catch (Exception e2) {}
	 			} catch (ZipException e) {
	 				if (iis != null) {
	 					try {
	 						iis.close();
	 					} catch(Exception e2) {}
	 				}
	 				fis = new FileInputStream(file);
	 				jsonStr = getStringFromInputStream(fis);
	 				try {
	 					fis.close();
	 				} catch (Exception e3) {}
	 			}
	 		} else {
	 			BufferedInputStream bis = new BufferedInputStream(is);
	 			bis.mark(10000);	// we mark the buffered input stream for eventual reset in the case the file is not compress
	 			iis = new InflaterInputStream(bis); 
	 			try { // we first assume the stream comes from a compressed file
	 				jsonStr = getStringFromInputStream(iis);
 					try {
 						iis.close();
 					} catch (Exception e2) {}
	 			} catch (Exception e) {
	 				bis.reset();	// we reset the stream to beginning
	 				jsonStr = getStringFromInputStream(bis);
	 			}
	 		}
	 		JSONList obj  = JsonIo.toObjects(jsonStr, null, JSONList.class);
	 		JSONUnmarshaller unmarshaller = new JSONUnmarshaller();
	 		return unmarshaller.unmarshall(obj);
		} catch (UnmarshallingException e1) {
			e1.printStackTrace();
			throw e1;
		} catch (Exception e2) {
			e2.printStackTrace();
			throw new UnmarshallingException(e2);
		}
	}

}
