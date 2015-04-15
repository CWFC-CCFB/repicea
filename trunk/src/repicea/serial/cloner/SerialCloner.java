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

/**
 * The SerialCloner interface ensures the instance can clone the object. The results must be a 
 * deep clone of the original object.
 * @author Mathieu Fortin - May 2014
 * @param <P> the class of the object to be cloned
 */
public interface SerialCloner<P> {
	
	/**
	 * This method produces a deep clone of the object.
	 * @param obj the object of class P to be cloned
	 * @return a deep clone of the original object
	 */
	public P cloneThisObject(P obj);

}
