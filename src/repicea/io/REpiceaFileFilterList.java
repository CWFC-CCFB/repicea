/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2023 His Majestry the King in Right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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

import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class REpiceaFileFilterList extends ArrayList<FileFilter>{

	/**
	 * Constructor.
	 * @param filters a series of REpiceaFileFilter instances
	 */
	public REpiceaFileFilterList(FileFilter... filters) {
		if (filters == null || filters.length == 0) {
			throw new InvalidParameterException("The filters argument cannot be null or empty!");
		}
		for (FileFilter f : filters) {
			add(f);
		}
	}
	
	/**
	 * Constructor.
	 * @param filters a REpiceaFileFilterList instance
	 */
	public REpiceaFileFilterList(REpiceaFileFilterList filters) {
		if (filters == null || filters.isEmpty()) {
			throw new InvalidParameterException("The filters argument cannot be null or empty!");
		}
		for (FileFilter f : filters) {
			add(f);
		}
	}

	/**
	 * Check if any of the FileFilter instance accepts this file.
	 * @param f
	 * @return
	 */
	public boolean accept(File f) {
		for (FileFilter filter : this) {
			if (filter.accept(f)) {
				return true;
			}
		}
		return false;
	}

}
