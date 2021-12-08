/*
 * This file is part of the repicea-iotools library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
package repicea.io;

/**
 * This class defines a basic field in a data set. A field has at least a name and a location
 * index in the header.
 * @author Mathieu Fortin - October 2011
 */
public abstract class FormatField {

	public static class NonAvailableFormatField extends FormatField {
		private NonAvailableFormatField() {
			super("n/a");
			setIndex(-1);
		}
	}
	
	public static final NonAvailableFormatField NON_AVAILABLE_FIELD = new NonAvailableFormatField();

	
	private String name;
	private int index;
	
	protected FormatField() {}
	
	protected FormatField(String name) {
		setName(name);
	}
	
	/**
	 * This method returns the name of the instance.
	 * @return a String instance
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * This method sets the name of the field.
	 * @param name a String
	 */
	protected void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {return getName();}

	/**
	 * This method returns the index of the field in the header.
	 * @return an Integer
	 */
	public int getIndex() {
		return index;
	}
	
	protected void setIndex(int index) {
		this.index = index;
	}
}
