/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * This class just contains static methods that facilitates the handling of the files.
 * @author Mathieu Fortin - July 2012
 */
public class FileUtility {

	/**
	 * This static method copy a source file over a target file
	 * @param sourcePath the path of the source file
	 * @param targetPath the path of the target file
	 * @return true if the file was copied or false otherwise
	 */
	public static boolean copy(String sourcePath, String targetPath) {
		InputStream in = null;
		OutputStream out = null;

		try{
			File sourceFile = new File(sourcePath);
			File targetFile = new File(targetPath);
			in = new FileInputStream(sourceFile);

			out = new FileOutputStream(targetFile);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			System.out.println("File copied.");
			return true;
		} catch(FileNotFoundException ex){
			System.out.println(ex.getMessage() + " in the specified directory.");
			return false;
		} catch(IOException e){
			System.out.println(e.getMessage());      
			return false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {}
			}

		}

	}

	/**
	 * Replace the extension by a new one. <br>
	 * <br>
	 * If the filename does not have an extension, the new extension is simply added to the 
	 * filename. 
	 * @param filename the original filename
	 * @param newExtension the new extension without the not (ie. exe and not .exe)
	 * @return the filename with the new extension
	 */
	public static String replaceExtensionBy(String filename, String newExtension) {
		if (filename == null || newExtension == null) {
			throw new InvalidParameterException("The filename and the newExtension arguments must be non null!");
		}
		int indexLastDot = filename.lastIndexOf(".");
		if (indexLastDot == -1) {
			return filename.concat("." + newExtension);
		} else {
			return filename.substring(0, indexLastDot).concat("." + newExtension);
		}
		
	}

	/**
	 * Compress a file.
	 * @param originalFilename the name of the file to be compressed (including the absolute path)
	 * @param compressedFilename the name of the file once compressed (including the absolute path)
	 * @throws IOException if an I/O exception occurs
	 */
	public static void zip(String originalFilename, String compressedFilename) throws IOException {
		if (originalFilename == null || compressedFilename == null) {
			throw new InvalidParameterException("The originalFilename and compressedFilename arguments cannot be null!");
		}
		File f1 = new File(originalFilename);
		FileInputStream fis = new FileInputStream(f1);
		
		File f2 = new File(compressedFilename);
		if (f2.exists()) {
			if (!f2.delete()) {
				fis.close();
				throw new IOException("The compressed file already exists and cannot be deleted!");
			}
		}
		FileOutputStream fos = new FileOutputStream(f2);
		DeflaterOutputStream dos = new DeflaterOutputStream(fos);
		byte[] byteArray = new byte[10000];
		int len;
		while ((len = fis.read(byteArray)) != -1) {
			dos.write(byteArray, 0, len);
		}
		dos.close();
		fis.close();
	}
	
	
	/**
	 * Compress a file.
	 * @param originalFilename the name of the file to be compressed (including the absolute path)
	 * @param compressedFilename the name of the file once compressed (including the absolute path)
	 * @throws IOException if an I/O exception occurs
	 */
	public static void unzip(String compressedFilename, String newFilename) throws IOException {
		if (newFilename == null || compressedFilename == null) {
			throw new InvalidParameterException("The compressedFilename and newFilename arguments cannot be null!");
		}
		File f1 = new File(compressedFilename);
		FileInputStream fis = new FileInputStream(f1);
		InflaterInputStream iis = new InflaterInputStream(fis);
		
		File f2 = new File(newFilename);
		if (f2.exists()) {
			if (!f2.delete()) {
				fis.close();
				throw new IOException("The file " + newFilename + " already exists and cannot be deleted!");
			}
		}
		FileOutputStream fos = new FileOutputStream(f2);
		byte[] byteArray = new byte[10000];
		int len;
		while ((len = iis.read(byteArray)) != -1) {
			fos.write(byteArray, 0, len);
		}
		fos.close();
		fis.close();
	}

}
