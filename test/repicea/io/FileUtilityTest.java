/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge-Epicea
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
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import repicea.util.ObjectUtility;

public class FileUtilityTest {

	@Test
	public void test01NewExtension() {
		String filename = ".allo.txt";
		String newFilename = FileUtility.replaceExtensionBy(filename, "csv");
		Assert.assertEquals("Testing new filename", ".allo.csv", newFilename);
	}
	
	@Test
	public void test02CompressFile() throws IOException {
		String filename = ObjectUtility.getPackagePath(getClass()) + "TEST6152.csv";
		String compressedFilename = ObjectUtility.getPackagePath(getClass()) + "myCompressFile.zip";
		File compressedFile = new File(compressedFilename);
		if (compressedFile.exists()) {
			if (!compressedFile.delete()) {
				throw new IOException("Cannot delete compressed file prior to compression!");
			}
		}
		FileUtility.zip(filename, compressedFilename);
		Assert.assertTrue("Testing if compressed file exists", compressedFile.exists());
		Assert.assertTrue("Compressed file is smaller than original file", compressedFile.length() < new File(filename).length());
	}

	@Test
	public void test03CompressDecompressFile() throws IOException {
		String filename = ObjectUtility.getPackagePath(getClass()) + "TEST6152.csv";
		String compressedFilename = ObjectUtility.getPackagePath(getClass()) + "myCompressFile.zip";
		File compressedFile = new File(compressedFilename);
		if (compressedFile.exists()) {
			if (!compressedFile.delete()) {
				throw new IOException("Cannot delete compressed file prior to compression!");
			}
		}
		FileUtility.zip(filename, compressedFilename);
		Assert.assertTrue("Testing if compressed file exists", compressedFile.exists());
		String newFilename = ObjectUtility.getPackagePath(getClass()) + "myUncompressedFile.csv";
		FileUtility.unzip(compressedFilename, newFilename);
		long expectedFileSize = new File(filename).length();
		long actualFileSize = new File(newFilename).length();
		Assert.assertEquals("Testing if decompressed file is the exact same size as the original file", expectedFileSize, actualFileSize);
	}

	public static void main(String[] args) throws IOException {
		String rootPath = "C:" + File.separator +
				"Users" + File.separator + "matforti" + File.separator +
				"7_Developpement" + File.separator + "ModellingProjects" + File.separator +
				"MetaModelSet" + File.separator + "incubator" + File.separator + 
				"QC" + File.separator + "5EST" + File.separator +
				"PET3" + File.separator + "Natura2014" + File.separator;
		String filename = rootPath + "QC_5EST_MS23_NoChange_AliveStemDensity_AllSpecies.zml";
		FileUtility.unzip(filename, rootPath + "myUncompressedFile.xml");
	}
}
