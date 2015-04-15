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
package repicea.serial;


/**
 * This interface defines what is memorizable. A memorizable object knows what to send to the TemporaryMemorizer 
 * and how to unpack the package received from the TemporaryMemorizer instance.
 * @author Mathieu Fortin - February 2012
 */
public interface Memorizable {

	/**
	 * This method creates a MemorizerPackage instance that can be sent in turn to the TemporaryMemorizer instance for serialization.
	 * @return a MemorizerPackage instance
	 */
	public MemorizerPackage getMemorizerPackage();
	
	/**
	 * This method unpacks the MemorizerPackage instance retrieved by the TemporaryMemorizer class.
	 * @param wasMemorized a MemorizerPackage instance
	 */
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized);

}
