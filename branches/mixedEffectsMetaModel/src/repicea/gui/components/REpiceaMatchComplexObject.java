/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui.components;

import java.util.List;

/**
 * The REpiceaMatchComplexObject allows to use complex object in the REpiceaMatchSelector class.
 * @author Mathieu Fortin - February 2021
 */
public interface REpiceaMatchComplexObject<E>  {

	/**
	 * Return the number of fields contain in this object
	 * @return
	 */
	public int getNbAdditionalFields();

	/**
	 * Return the values of the additional field
	 * @return a List of instances
	 */
	public List<Object> getAdditionalFields();
	
	/**
	 * Set the value of a particular additional field.
	 * @param indexOfThisAdditionalField the index of the additional field
	 * @param value the new value
	 */
	public void setValueAt(int indexOfThisAdditionalField, Object value);
	
	/**
	 * Return a clone of this object
	 * @return an instance of class E
	 */
	public E copy();
}
