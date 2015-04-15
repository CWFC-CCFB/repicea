/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge Epicea.
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

/**
 * The PostXmlUnmarshalling class offers the possibility to do some actions immediately after the unmarshalling.
 * @author Mathieu Fortin - May 2014
 */
public interface PostXmlUnmarshalling {

	/**
	 * This method is called after the unmashalling. It may serve to correct for former implementation.
	 */
	public void postUnmarshallingAction();
	
}
