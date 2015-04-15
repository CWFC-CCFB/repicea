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

import java.util.List;

/**
 * A ListManager object handles a list of objects.
 * @author Mathieu Fortin - February 2014
 * @param <P> the class of the object to be visualized
 */
public interface ListManager<P extends UserInterfaceableObject> {
	
	/**
	 * This method returns the list of UserInterfaceableObject-derived instance
	 * @return a List
	 */
	public List<P> getList();
	
	/**
	 * This method add the UserInterfaceableObject-derived instance to the list.
	 * @param obj a UserInterfaceableObject-derived instance
	 */
	public void registerObject(P obj);
	
	/**
	 * This method remove the UserInterfaceableObject-derived instance from the list.
	 * @param obj a UserInterfaceableObject-derived instance
	 */
	public void removeObject(P obj);
	

}
