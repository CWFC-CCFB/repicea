/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2013 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;

import javax.swing.filechooser.FileSystemView;

import repicea.util.ObjectUtility;

/**
 * The ParentDrivenFileSystemView class is associated with the JFileChooser class. It handles what can be and
 * what cannot be done when using a JFileChooser instance. In this case, the ParentDrivenFileSystemView is designed
 * to work from a root directory. If the member restricted is true, then the JFileChooser instance will be restricted
 * to the root directory and no more. Otherwise, the user can navigate into child directories.
 * @author Mathieu Fortin - January 2013
 */
public class ParentDrivenFileSystemView extends FileSystemView {

	private File rootDirectory;
	private boolean restricted;
	
	/**
	 * Constructor.
	 * @param rootDirectory a File instance that represents an existing directory
	 * @param restricted a boolean, if true the JFileChooser instance will be restricted to the root directory
	 */
	public ParentDrivenFileSystemView(File rootDirectory, boolean restricted) {
		super();
		if (!rootDirectory.isDirectory()) {
			throw new InvalidParameterException("The parent directory file is not a directory or it does not exists!");
		}
		this.rootDirectory = rootDirectory;
		this.restricted = restricted;
	}
	
	@Override
	public File getParentDirectory(File dir) {
		File parentFile = dir.getParentFile();
		URI relativeURI = ObjectUtility.relativizeTheseFile(rootDirectory, parentFile);
		if (restricted || relativeURI == null) {
			return rootDirectory;
		} else {
			return parentFile;
		}
	}

	@Override
	public File getHomeDirectory() {
		return rootDirectory;
	}
	
	@Override
	public File[] getRoots() {
		return new File[] {rootDirectory};
	}
			
	@Override
	public File createNewFolder(File containingDir) throws IOException {
		URI relativeURI = ObjectUtility.relativizeTheseFile(rootDirectory, containingDir);
		if (restricted || relativeURI == null) {
			return null;
		} else {
			return containingDir;
		}
	}

}
