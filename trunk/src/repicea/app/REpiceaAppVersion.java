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

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import repicea.util.JarUtility;

/**
 * This class retrieves information on the version and other features of the repicea.jar application. 
 * @author Mathieu Fortin - August 2015
 */
public class REpiceaAppVersion {

	private static REpiceaAppVersion SINGLETON;

	private final String version;
	private final String revision;
	
	private REpiceaAppVersion() {
		String filePath = JarUtility.getJarFileImInIfAny(getClass());
		if (filePath != null) {
			try {
				Manifest m = JarUtility.getManifestFromThisJarFile(filePath);
				version = m.getMainAttributes().get(Attributes.Name.IMPLEMENTATION_VERSION).toString();
				String[] versionSplit = version.split("\\.");
				if (versionSplit.length == 3) {
					revision = versionSplit[2].trim();
				} else {
					throw new InvalidParameterException("Can extract version but not revision from manifest!");
				}
			} catch (IOException e) {
				throw new InvalidParameterException("Cannot retrieve manifest from jar file: " + filePath);
			}
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
