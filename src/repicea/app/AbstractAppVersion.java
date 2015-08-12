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
package repicea.app;

import repicea.io.javacsv.CSVReader;

/**
 * This class retrieves the revision that defines the version of the application.
 * This revision number can be return using the getRevision() method.
 * @author Mathieu Fortin - August 2015
 */
public abstract class AbstractAppVersion {

	private String revision; 
	
	protected AbstractAppVersion(String csvRevisionFilename) {
		try {
			CSVReader reader = new CSVReader(csvRevisionFilename);
			Object[] record = reader.nextRecord();
			revision = record[reader.getHeader().getIndexOfThisField(AbstractAppVersionCompiler.REVISION_STRING)].toString();
		} catch (Exception e) {
			revision = "Unknown";
		}
	}

	/**
	 * This method returns the revision number.
	 * @return a String
	 */
	public final String getRevision() {return "Revision " + revision.trim();}
	
}
