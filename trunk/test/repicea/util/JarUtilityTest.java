/*
 * This file is part of the repicea library.
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
package repicea.util;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.junit.Assert;
import org.junit.Test;

public class JarUtilityTest {

	@Test
	public void testingIfAssertClassInJar() {
		boolean isInJar = JarUtility.isEmbeddedInJar(Assert.class);
		Assert.assertTrue("Testing if the Assert class is in a jar.", isInJar);
	}

	
	@Test
	public void testingIfManifestCanBeAccessed() throws IOException {
		String jarFilename = JarUtility.getJarFileImInIfAny(Assert.class);
		Manifest m = JarUtility.getManifestFromThisJarFile(jarFilename);
		String revision = m.getMainAttributes().get(Attributes.Name.IMPLEMENTATION_VERSION).toString();
		Assert.assertTrue("Testing if revision can be retrieved from manifest", revision != null && !revision.isEmpty());
	}

	@Test
	public void testingIfThisClassInJar() {
		boolean isInJar = JarUtility.isEmbeddedInJar(JarUtilityTest.class);
		Assert.assertTrue("Testing that this class is not in a jar.", !isInJar);
	}

}
