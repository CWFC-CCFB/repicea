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

import repicea.util.DeepCloneable;


/**
 * The REpiceaMatchComplexObject allows to use of object more complex 
 * than simple enums in the REpiceaMatchSelector class.
 * @author Mathieu Fortin - February 2021
 */
public interface REpiceaMatchWithEnumObject<K, V extends Enum<?>> extends DeepCloneable<REpiceaMatchWithEnumObject<K, V>> {

	/**
	 * Return the number of fields contain in this object
	 * @return an integer
	 */
	public default int getNbAdditionalFields() {
		List<Object> addFields = this.getAdditionalFields();
		return addFields == null ? 0 : addFields.size();
	};

	/**
	 * Return the values of the additional fields.
	 * 
	 * @return a List of instances or null if there are no additional fields
	 */
	public List<Object> getAdditionalFields();
	
	/**
	 * Set the value of a particular additional field.
	 * @param indexOfThisAdditionalField the index of the additional field
	 * @param value the new value
	 */
	public void setValueAt(int indexOfThisAdditionalField, Object value);

	public V getValue();

	public void setValue(V value);
	
	public K getKey();
}
