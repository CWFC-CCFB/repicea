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

import repicea.util.ObjectUtility;

public class REpiceaJARSVNAppVersion extends AbstractAppVersion {

	private static REpiceaJARSVNAppVersion SINGLETON;
	
	private static final String FilenamePath = ObjectUtility.getRootPath(REpiceaJARSVNAppVersion.class) + "revision.csv";
	
	private REpiceaJARSVNAppVersion() {
		super(FilenamePath);
	}

	/**
	 * This method returns the singleton instance of REpiceaJARSVNAppVersion class which can be requested
	 * to return the revision number of this version.
	 * @return the singleton instance of the REpiceaJARSVNAppVersion class
	 */
	public static REpiceaJARSVNAppVersion getInstance() {
		if (SINGLETON == null) {
			SINGLETON = new REpiceaJARSVNAppVersion();
		}
		return SINGLETON;
	}
	
	
	public static void main(String[] args) {
		String version = REpiceaJARSVNAppVersion.getInstance().getRevision();
		int u = 0;
	}
	
}
