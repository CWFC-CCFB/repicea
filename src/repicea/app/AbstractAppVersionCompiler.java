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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/**
 * This class records the last version number from the svn repository. The derived class should be integrated 
 * to a JUnit test series (which are not exported as JAR).
 * @author Mathieu Fortin - August 2015
 */
public abstract class AbstractAppVersionCompiler {

	protected static final String REVISION_STRING = "Revision";
	
	protected AbstractAppVersionCompiler(String appSVN, String versionFilename) {
		URL svnUrl;
		try {
			svnUrl = new URL(appSVN);
			URLConnection connection = svnUrl.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			String revision = ""; 
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains(REVISION_STRING)) {
					int index = inputLine.indexOf(REVISION_STRING) + REVISION_STRING.length() + 1;
					int finalIndex = inputLine.indexOf(" ", index) - 1;
					revision = inputLine.substring(index, finalIndex);
					break;
				}
			}
			System.out.println(REVISION_STRING + ": " + revision);
			in.close();
			File file = new File(versionFilename);
			if (file.exists()) {
				file.delete();
			}
			OutputStream out = new FileOutputStream(file);
			out.write(revision.getBytes());
			out.flush();
			out.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
