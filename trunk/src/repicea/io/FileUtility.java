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

}
