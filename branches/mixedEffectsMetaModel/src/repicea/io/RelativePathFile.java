/*
 * This file is part of the repicea-iotools library.
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
import java.io.Serializable;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * The RelativePathFile class extends the File class. It also contains a reference directory, which makes it possible to
 * get a relative path from that directory. This class is useful to compare two files from different repositories for example.
 * If the specified directory has nothing to do with the file itself, the relative path is null. 
 * @author Mathieu Fortin - January 2012
 */
public class RelativePathFile extends File implements Cloneable, Serializable {

	private static final long serialVersionUID = 20120106L;
	
	private String rootPathname;
	private Vector<String> relativePathParts;

	/**
	 * Constructor 1.
	 * @param filename the filename of the file to be relative to the reference directory
	 * @param rootPathname the reference directory from which the file is relative
	 */
	public RelativePathFile(String filename, String rootPathname) {
		super(filename);
		relativePathParts = new Vector<String>();
		setRelativeTo(rootPathname);
	}
	
	/**
	 * Constructor 2.
	 * @param filename the filename of the file to be relative to the reference directory
	 * @param rootPath the reference directory from which the file is relative
	 */
	public RelativePathFile(String filename, File rootPath) {
		this(filename, rootPath.getAbsolutePath());
	}

	/**
	 * Constructor 3.
	 * @param file the file to be relative to the reference directory
	 * @param rootPath the reference directory from which the file is relative
	 */
	public RelativePathFile(File file, File rootPath) {
		this(file.getAbsolutePath(), rootPath.getAbsolutePath());
	}
	
	/**
	 * Private constructor for cloning.
	 * @param file the file that is to be cloned
	 */
	private RelativePathFile(RelativePathFile file) {
		super(file.getAbsolutePath());
		relativePathParts = new Vector<String>();
		for (String str : file.relativePathParts) {
			relativePathParts.add(str);
		}
	}
	
	
	protected void setRelativeTo(String rootPathname) {
		this.rootPathname = rootPathname;
		if (getAbsolutePath().startsWith(rootPathname)) {
			String relativePath = getAbsolutePath().replace(rootPathname + File.separator, "");
			String[] parts = relativePath.split(Pattern.quote(File.separator));
			for (String part : parts) {
				relativePathParts.add(part);
			}
		}
	}

	
	public String getRelativePath() {
		String path = null;
		for (String str : relativePathParts) {
			if (path == null) {
				path = str;
			} else {
				path = path.concat(File.separator.concat(str));
			}
		}
		return path;
	}

	
	@Override
	public int compareTo(File pathName) {
		Vector<String> relativePathPartsOfThatFile = ((RelativePathFile) pathName).relativePathParts;
		String partThis;
		String partThat;
		for (int i = 0; i < relativePathParts.size(); i++) {
			if (i < relativePathPartsOfThatFile.size()) {
				partThis = relativePathParts.get(i);
				partThat = relativePathPartsOfThatFile.get(i);
				int result = partThis.compareTo(partThat);
				if (result != 0) {
					return result;
				}
			} else {
				return 1;		// means this file has more element that the reference file
			}
		}
		if (relativePathPartsOfThatFile.size() > relativePathParts.size()) {
			return -1;			// means the comparison file has a longer part although the initial sequence is equal to this path
		} else {
			return 0;		// the path are equals
		}
	}

	protected Vector<String> getRelativePartsVector() {return relativePathParts;}
	protected String getRootPathname() {return rootPathname;}
	
	
	/**
	 * This method checks if this relative path file has a path that includes the file specified in parameter. If this 
	 * file is really a file, the method returns true if the file parameter has the same relative path and name. If this
	 * file is a directory, the method returns true if the file parameter is included in the directory or a subdirectory.
	 * @param file a RelativePathFile instance
	 * @return a boolean
	 */
	public boolean include(RelativePathFile file) {
		for (int i = 0; i < getRelativePartsVector().size(); i++) {
			if (file.getRelativePartsVector().size() < i + 1 || !getRelativePartsVector().get(i).equals(file.getRelativePartsVector().get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method returns a deep clone of the RelativePathFile.
	 */
	@Override
	public RelativePathFile clone() {
		return new RelativePathFile(this);
	}
	
}
