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
package repicea.gui;

import repicea.serial.Memorizable;

/**
 * The OwnedWindow interface ensures that a particular represents an instance. The only method
 * is called to synchronized the values of the components with the values of the owner.
 * @author Mathieu Fortin - April 2014
 */
public interface OwnedWindow {

	/**
	 * This method ensures that the values of the components are those of the owner. For instance,
	 * if the owner is somehow changed after resetting, this method should be called.
	 */
	public void synchronizeUIWithOwner();
	
	/**
	 * This method returns the owner of this window which should implement the Memorizable interface
	 * @return a Memorizable instance
	 */
	public Memorizable getWindowOwner();


}
