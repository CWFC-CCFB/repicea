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


/**
 * This interface ensures the visibility of the protected method firePropertyChange(String, Object, Object) in
 * the Component class.
 * @author Mathieu Fortin - April 2014
 */
public interface REpiceaEventFireComponent {
	
	/**
	 * This method fires an event when the parameters are loaded or saved
	 * @param propertyName a REpiceaAWTProperty enum
	 * @param obj1 the first parameter
	 * @param obj2 the second parameter
	 */
	public void firePropertyChange(REpiceaAWTProperty propertyName, Object obj1, Object obj2);

}
