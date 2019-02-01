/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2018 Mathieu Fortin for Rouge Epicea.
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
package repicea.lang;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * The REpiceaClassLoader class allows for the specification of a directory that contains java libraries.
 * @author Mathieu Fortin - December 2018
 */
public class REpiceaClassLoader extends URLClassLoader {
	
	public final static Map<Class<?>, Class<?>> JavaWrapperToPrimitiveMap = new HashMap<Class<?>, Class<?>>();
	static {
		JavaWrapperToPrimitiveMap.put(Double.class, double.class);
		JavaWrapperToPrimitiveMap.put(Integer.class, int.class);
		JavaWrapperToPrimitiveMap.put(Long.class, long.class);
		JavaWrapperToPrimitiveMap.put(Float.class, float.class);
		JavaWrapperToPrimitiveMap.put(String.class, String.class);
		JavaWrapperToPrimitiveMap.put(Boolean.class, boolean.class);
		JavaWrapperToPrimitiveMap.put(Character.class, char.class);
	}

	public final static Map<Class<?>, Class<?>> PrimitiveToJavaWrapperMap = new HashMap<Class<?>, Class<?>>();
	static {
		PrimitiveToJavaWrapperMap.put(double.class, Double.class);
		PrimitiveToJavaWrapperMap.put(int.class, Integer.class);
		PrimitiveToJavaWrapperMap.put(long.class, Long.class);
		PrimitiveToJavaWrapperMap.put(float.class, Float.class);
		PrimitiveToJavaWrapperMap.put(String.class, String.class);
		PrimitiveToJavaWrapperMap.put(boolean.class, Boolean.class);
		PrimitiveToJavaWrapperMap.put(char.class, Character.class);
	}

	
	
	
	private boolean hasExtensionDirectoryBeenRead = false;
	
	/**
	 * Constructor for this class. It is called by the JVM through 
	 * the option -Djava.system.class.loader .
	 * @param parent the parent ClassLoader
	 */
	public REpiceaClassLoader(ClassLoader parent) {
		super(((URLClassLoader) parent).getURLs(), parent);		
	}

	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		return super.findClass(name);
	}
	
	@Override
	public URL getResource(String name) {
		return super.getResource(name);
	}
	
	public void setExtensionPath(File extensionPath) {
		if (!hasExtensionDirectoryBeenRead) {
			System.out.println("Loading default extensions jar");
			try {
				if (extensionPath != null && extensionPath.exists() && extensionPath.isDirectory()) {
					for (File file : extensionPath.listFiles()) {
						if (file.getAbsolutePath().trim().endsWith(".jar")) {
							if (!file.getAbsolutePath().trim().endsWith("repicea.jar")) {
								addURL(file.toURI().toURL());						// add the jars one by one
								System.out.println("Library added to classpath: " + file.getAbsolutePath());
							}
						}
					}
				}
			} catch (MalformedURLException e) {
				throw new InvalidParameterException("The directory for the extensions is not valid!");
			}
			hasExtensionDirectoryBeenRead = true;
		}
	}
	
}
