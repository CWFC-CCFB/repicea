/*
 * This file is part of the repicea-iotools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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

import javax.swing.filechooser.FileFilter;

/**
 * The IOUserInterfaceableObject should be implemented by any object whose UI offers the save/saveas/load options.
 * @author Mathieu Fortin - April 2014
 */
public interface IOUserInterfaceableObject extends Saveable, Loadable {
	
	/**
	 * This method returns the appropriate FileFilter instance for this class.
	 * @return a FileFilter instance
	 */
	public FileFilter getFileFilter();

	/**
	 * This method returns the filename of the parameters.
	 * @return a String
	 */
	public String getFilename();

}
