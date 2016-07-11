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
package repicea.gui;

import java.awt.Component;
import java.awt.Container;

/**
 * This interface applies to classes that hold their GUI Interface as a member. Should be used with
 * object that provides a REpiceaDialog instance as user interface.
 * @author Mathieu Fortin - October 2010
 */
public interface REpiceaUIObjectWithParent {

	/**
	 * This method returns the GUI interface of the class that implements this interface.
	 * @param parent a parent Container instance (can be null)
	 * @return a Component instance
	 */
	public Component getUI(Container parent); 

	/**
	 * This method returns true if the GUI is visible.
	 * @return a boolean
	 */
	public boolean isVisible();

}
