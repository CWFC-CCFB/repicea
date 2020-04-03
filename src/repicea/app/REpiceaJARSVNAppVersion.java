/*
 * This file is part of the repicea library.
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

/**
 * This class retrieves information on the version and other features of the repicea.jar application. 
 * @author Mathieu Fortin - August 2015
 */
public class REpiceaJARSVNAppVersion extends AbstractAppVersion {

	private static REpiceaJARSVNAppVersion SINGLETON;

	private static final String AppName = "repicea";
	
	protected static final String ShortFilename = AppName + "_revision.csv";
	protected static final String CompleteFilename = ObjectUtility.getRelativePackagePath(REpiceaJARSVNAppVersion.class) + ShortFilename;
	
	private REpiceaJARSVNAppVersion() {
		super(AppName, CompleteFilename);
	}

	/**
	 * This method returns the singleton instance of this class which can be requested
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
		System.out.println(version);
	}
	
}
