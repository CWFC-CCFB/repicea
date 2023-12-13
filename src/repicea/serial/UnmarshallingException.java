/*
 * This file is part of the repicea library.
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
package repicea.serial;

import java.io.IOException;

@SuppressWarnings("serial")
public class UnmarshallingException extends IOException {

	/**
	 * Empty constructor.
	 */
	public UnmarshallingException() {
		super();
	}
	
	/**
	 * Constructor 2.
	 * @param message the error message
	 */
	public UnmarshallingException(String message) {
		super(message);
	}

	/**
	 * Constructor 3.
	 * @param e an Exception instance
	 */
	public UnmarshallingException(Exception e) {
		super(e);
	}

}
