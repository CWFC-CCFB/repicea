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

import java.awt.Window;

/**
 * This interface applies to classes that hold their GUI Interface as a member and can show it. Should be used with
 * object that provides a REpiceaDialog instance as user interface.
 * @author Mathieu Fortin - October 2010
 */
public interface REpiceaShowableUIWithParent extends REpiceaUIObjectWithParent {

	/**
	 * This method shows the GUI interface of the class that implements this interface.
	 * @param parent the parent Window instance (can be null)
	 */
	public void showUI(Window parent);

}
