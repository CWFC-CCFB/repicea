/*
 * This file is part of the repicea-util library.
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
package repicea.app;

import org.junit.Assert;
import org.junit.Test;

import repicea.util.JarUtility;

public class REpiceaAppVersionTest {
	
	@Test
	public void compileAndRetrieveRevision() {		
		String version = REpiceaAppVersion.getInstance().getVersion();
		System.out.println("Version is: " + version);
		if (JarUtility.isEmbeddedInJar(REpiceaAppVersion.class)) {
			String[] split = version.split("\\.");
			Assert.assertEquals(3, split.length);			
		} else {
			Assert.assertEquals("Unknown", version);
		}
	}
}
