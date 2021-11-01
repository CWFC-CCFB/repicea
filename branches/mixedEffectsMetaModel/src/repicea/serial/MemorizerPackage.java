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
package repicea.serial;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The MemorizerPackage class contains a list of Serializable object that can be memorized using the SerialCloner for a 
 * customized deserialization.
 * @author Mathieu Fortin - February 2012
 */
public class MemorizerPackage extends ArrayList<Serializable> {

	private static final long serialVersionUID = 20110103L;

	public MemorizerPackage() {}
}
