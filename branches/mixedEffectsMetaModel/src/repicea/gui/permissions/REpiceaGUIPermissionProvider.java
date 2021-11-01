/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui.permissions;

/**
 * This interface ensures the component can return its permissions.
 * 
 * @author Mathieu Fortin - January 2015
 */
public interface REpiceaGUIPermissionProvider {

	/**
	 * This method returns the Permission instance associated with the object.
	 * @return a Permission instance
	 */
	public REpiceaGUIPermission getGUIPermission();
	
}
