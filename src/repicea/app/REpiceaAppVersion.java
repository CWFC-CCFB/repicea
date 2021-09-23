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
public class REpiceaAppVersion {

	private static REpiceaAppVersion SINGLETON;

	private final String version;
	private final String revision;
	
	private REpiceaAppVersion() {
		if (ObjectUtility.isEmbeddedInJar(getClass())) {
			version = getClass().getPackage().getImplementationVersion().trim();
			revision = version.split(".")[2].trim();
		} else {
			version = "Unknown";
			revision = "Unknown";
		}
	}

	public static REpiceaAppVersion getInstance() {
		if (SINGLETON == null) {
			SINGLETON = new REpiceaAppVersion();
		}
		return SINGLETON;
	}
	
	/**
	 * This method returns the revision number, i.e. the label "revision" + the build number.
	 * @return a String
	 */
	public final String getRevision() {return "Revision " + revision.trim();}

	/**
	 * This method returns the build. It is just the number without any other string.
	 * @return the build number as a string.
	 */
	public final String getBuild() {return revision.trim();}

	/**
	 * Return the version number. Typically, 1.1.819. The last number represents the revision.
	 * @return
	 */
	public final String getVersion() {return version;};
	

}
