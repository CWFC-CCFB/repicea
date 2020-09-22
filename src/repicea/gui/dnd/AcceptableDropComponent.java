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
package repicea.gui.dnd;

/**
 * This interface should be implemented by Component derived classes that accept a drop.
 * @author Mathieu Fortin - October 2012
 * @param <P> the class of the object to be accepted
 */
public interface AcceptableDropComponent<P> {


	/**
	 * This method deals with the object being accepted from the drop.
	 * @param obj an instance of class P
	 * @param evt the LocatedEvent that terminates the DnD
	 */
	public void acceptThisObject(P obj, LocatedEvent evt);
	
}
