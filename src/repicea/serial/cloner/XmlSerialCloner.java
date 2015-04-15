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
package repicea.serial.cloner;

import repicea.serial.xml.XmlMarshallingUtilities;

/**
 * The XmlSerialCloner class relies on the repicea.serial.xml package for cloning objects.
 * @author Mathieu Fortin - May 2014
 */
public class XmlSerialCloner<P> implements SerialCloner<P> {

	@SuppressWarnings("unchecked")
	@Override
	public P cloneThisObject(P obj) {
		try {
			return (P) XmlMarshallingUtilities.createDeepCopyOf(obj);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
