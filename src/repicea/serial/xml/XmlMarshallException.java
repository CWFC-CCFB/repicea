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

import java.io.IOException;

/**
 * A XmlMarshallException is thrown whenever the marshalling or the unmarshalling fails.
 * @author Mathieu Fortin - November 2012
 */
@SuppressWarnings("serial")
public class XmlMarshallException extends IOException {

	XmlMarshallException() {
		super();
	}
	
	XmlMarshallException(String message) {
		super(message);
	}
	
	XmlMarshallException(Exception e) {
		super(e);
	}
}
