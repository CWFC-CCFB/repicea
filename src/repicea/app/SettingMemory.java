/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge Epicea.
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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import repicea.lang.REpiceaSystem;
import repicea.serial.DeprecatedObject;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;

/**
 * The class SettingMemory makes it possible to record properties in a Map object and 
 * to retrieve them afterwards.
 * @author Mathieu Fortin - June 2012
 */
public class SettingMemory implements Memorizable, Closeable {
	
	protected String filename;
	
	protected HashMap<String, String> stringProperties;

	protected RandomAccessFile raf;
	
	protected FileLock lock;
	
	/**
	 * Constructor with default filename, which is java.io.tmpdir / settings.ser.
	 */
	public SettingMemory() {
		this(REpiceaSystem.getJavaIOTmpDir() + "settings.ser");
	}

	
	/**
	 * Constructor with filename.
	 * @param filename the file in which the settings are serialized
	 */
	public SettingMemory(String filename) {
		this.filename = filename;
		File file = new File(filename);
		if (!file.exists()) {
			saveSettings();
		} else {
			try {
				loadSettings();
			} catch (Exception e) {
				System.out.println("Deleting old settings and using new ones instead!");
				saveSettings();		// second chance
			}
		}
	}
	
	/**
	 * This method serializes the settings in the file denoted by the filename 
	 * variable.
	 */
	protected void saveSettings() {
		FileOutputStream fos = null;
		try {
//			fos = new FileOutputStream(getRandomAccessFile().getFD());
			fos = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(getMemorizerPackage());
			out.close();
		} catch(IOException ex) {
			ex.printStackTrace();
			throw new InvalidParameterException("The parameters cannot be saved!");
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
//	private RandomAccessFile getRandomAccessFile() throws IOException {
//		if (raf == null) {
//			raf = new RandomAccessFile(new File(filename), "rw");
//			lock = raf.getChannel().lock();
//		}
//		return raf;
//	}
	
	
	/**
	 * This method deserializes the settings.
	 */
	protected void loadSettings() {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
//			fis = new FileInputStream(getRandomAccessFile().getFD());
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			Object obj = in.readObject();
			if (obj instanceof DeprecatedObject) {
				obj = ((DeprecatedObject) obj).convertIntoAppropriateClass();
			}
			MemorizerPackage mp;
			if (obj instanceof HashMap) {			// to ensure compatibility with previous version
				mp = new MemorizerPackage();
				mp.add((HashMap<?,?>) obj);
			} else {
				mp = (MemorizerPackage) obj;
			}
			unpackMemorizerPackage(mp);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new InvalidParameterException("The parameters cannot be loaded!");
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	/**
	 * This method returns the Map of properties. It instantiates the map if it
	 * has not been instantiated before.
	 * @return a Map of Strings as keys and Strings as values 
	 */
	public Map<String, String> getStringProperties() {
		if (stringProperties == null) {
			stringProperties = new HashMap<String, String>();
		}
		return stringProperties;
	}

	
	/**
	 * This method set a particular property.
	 * @param property a String
	 * @param value a String
	 */
	public void setProperty(String property, String value) {
		getStringProperties().put(property, value);
	}


	/**
	 * This method set a particular property.
	 * @param property a String
	 * @param value a Double
	 */
	public void setProperty(String property, double value) {
		getStringProperties().put(property, ((Double) value).toString());
	}

	/**
	 * This method set a particular property.
	 * @param property a String
	 * @param value a Boolean
	 */
	public void setProperty(String property, boolean value) {
		getStringProperties().put(property, ((Boolean) value).toString());
	}

	/**
	 * This method set a particular property.
	 * @param property a String
	 * @param value an Integer
	 */
	public void setProperty(String property, int value) {
		getStringProperties().put(property, ((Integer) value).toString());
	}

	
	/**
	 * This method returns a previously saved property or a default value if
	 * the property has not been set yet.
	 * @param property the property name
	 * @param defaultValue the default value of the property
	 * @return a String
	 */
	public String getProperty(String property, String defaultValue) {
		String value = getStringProperties().get(property);
		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}

	/**
	 * This method returns a previously saved property or a default value if
	 * the property has not been set yet.
	 * @param property the property name
	 * @param defaultValue the default value of the property
	 * @return a double
	 */
	public double getProperty(String property, double defaultValue) {
		String value = getStringProperties().get(property);
		try {
			if (value != null) {
				return Double.parseDouble(value);
			} 
		} catch (NumberFormatException e) {}
		return defaultValue;
	}

	/**
	 * This method returns a previously saved property or a default value if
	 * the property has not been set yet.
	 * @param property the property name
	 * @param defaultValue the default value of the property
	 * @return a boolean
	 */
	public boolean getProperty(String property, boolean defaultValue) {
		String value = getStringProperties().get(property);
		if (value != null) {
			if (value.trim().toLowerCase().equals("true") ||  value.trim().toLowerCase().equals("false")) {
				return Boolean.parseBoolean(value);
			}
		} 
		return defaultValue;
	}

	/**
	 * This method returns a previously saved property or a default value if
	 * the property has not been set yet.
	 * @param property the property name
	 * @param defaultValue the default value of the property
	 * @return an integer
	 */
	public int getProperty(String property, int defaultValue) {
		String value = getStringProperties().get(property);
		try {
			if (value != null) {
				return Integer.parseInt(value);
			} 
		} catch (NumberFormatException e) {}
		return defaultValue;
	}


	@Override
	public final MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = new MemorizerPackage();
		mp.add(stringProperties);
		return mp;
	}


	@SuppressWarnings("unchecked")
	@Override
	public final void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		stringProperties = (HashMap<String, String>) wasMemorized.get(0);
	}


	@Override
	public void close() throws IOException {
		saveSettings();
		if (lock != null) {
			try {
				raf.close();
				lock.release();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
