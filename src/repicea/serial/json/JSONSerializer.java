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

import java.io.FileOutputStream;
import java.util.zip.DeflaterOutputStream;

import com.cedarsoftware.io.JsonIo;
import com.cedarsoftware.io.JsonWriter;

import repicea.serial.AbstractSerializer;
import repicea.serial.MarshallingException;

/**
 * A Serializer to JSON format.
 * @author Mathieu Fortin - December 2023
 */
public final class JSONSerializer extends AbstractSerializer {

	
	/**
	 * Constructor.
	 * @param filename the file that serves as output
	 * @param enableCompression true to use compression or false otherwise
	 */
	public JSONSerializer(String filename, boolean enableCompression) {
		super(filename, enableCompression);
	}

	
	/**
	 * Constructor with compression.
	 * @param filename the xml file that serves as output
	 */
	public JSONSerializer(String filename)  {
		this(filename, true);
	}
	
	
	public void writeObject(Object obj) throws MarshallingException {
		FileOutputStream fos = null;
		DeflaterOutputStream dos = null;
		try {
			JSONMarshaller marshaller = new JSONMarshaller();
			Object protoJSONObject = marshaller.marshall(obj);
			String toJSONString = JsonIo.toJson(protoJSONObject, null);

			if (enableCompression) {
				fos = new FileOutputStream(file);
				dos = new DeflaterOutputStream(fos);
				dos.write(toJSONString.getBytes()); 
				dos.flush();
			} else {
				fos = new FileOutputStream(file);
				fos.write(toJSONString.getBytes()); 
				fos.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new MarshallingException(e);
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	

}
