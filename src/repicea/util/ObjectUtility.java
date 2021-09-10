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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public final class ObjectUtility {

	public static final String PathSeparator = "/";

	private static final double verySmall = 10E-8;
	

	/**
	 * This method makes it possible to clone a Map type object
	 * @param oMap
	 * @return the cloned map
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map copyMap(Map oMap) {
		Map returnMap;
		if (oMap instanceof TreeMap)
			returnMap = new TreeMap();
		else if (oMap instanceof LinkedHashMap)
			returnMap = new LinkedHashMap();
		else returnMap = new HashMap();
		for (Iterator iter = oMap.keySet().iterator(); iter.hasNext();) {
			Object key = iter.next();
			if (oMap.get(key) instanceof Map) {
				Map valueMap = ObjectUtility.copyMap((Map) oMap.get(key));
				returnMap.put(key, valueMap);
			} else if (oMap.get(key) instanceof Vector) {
				Vector valueVec = ObjectUtility.copyVector((Vector) oMap.get(key));
				returnMap.put(key, valueVec);
			}  else if (oMap.get(key) instanceof Set) {
				Set oSet = ObjectUtility.copySet((Set) oMap.get(key));
				returnMap.put(key, oSet);
			} else if (oMap.get(key) instanceof DeepCloneable) {
				Object valueMat = ((DeepCloneable) oMap.get(key)).getDeepClone();
				returnMap.put(key, valueMat);
			} else {
				returnMap.put(key, oMap.get(key));
			}
		}
		return returnMap;
	}


	/**
	 * This method copies all the element of a vector into a new vector.
	 * @param oVec the source vector
	 * @return a new Vector instance
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Vector copyVector(Vector oVec) {
		Vector returnVec = new Vector();
		for (int i = 0; i < oVec.size(); i++) 
			returnVec.add(oVec.get(i));
		return returnVec;
	}

	/**
	 * This method copies all the element of a List into a new ArrayList instance.
	 * @param oList the source List instance
	 * @return a new ArrayList instance
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List copyList(List oList) {
		List returnList = new ArrayList();
		for (int i = 0; i < oList.size(); i++) 
			returnList.add(oList.get(i));
		return returnList;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Set copySet(Set oSet) {
		Set oReturnSet;
		if (oSet instanceof TreeSet) {
			oReturnSet = new TreeSet();
		} else oReturnSet = new HashSet();
		oReturnSet.addAll(oSet);
		return oReturnSet;
	}


	/**
	 * This static method finds the maximum value in a one-dimension array
	 * @param oArray = an array of double values
	 * @return = the index of the maximum value 
	 */
	public static int findMaxInAnArrayOfDouble(double[] oArray) {
		double max = 0d;
		int pointer = -1;
		for (int i = 0; i < oArray.length; i++) {
			if (i == 0) {
				max = oArray[i];
				pointer = i;
			} else if (oArray[i] > max) {
				max = oArray[i];
				pointer = i;
			}
		}
		return pointer;
	}

	/**
	 * This static method finds the maximum value in a one-dimension array
	 * @param oArray = an array of double values
	 * @return = the index of the maximum value
	 */
	public static int findMinInAnArrayOfDouble(double[] oArray) {
		double min = 0d;
		int pointer = -1;
		for (int i = 0; i < oArray.length; i++) {
			if (i == 0) {
				min = oArray[i];
				pointer = i;
			} else if (oArray[i] < min) {
				min = oArray[i];
				pointer = i;
			}
		}
		return pointer;
	}

	
	
	public static boolean isThereAnyElementDifferentFrom(double[][] arrayDouble, double d) {
		boolean diff = false;
		for (int i = 0; i < arrayDouble.length; i++) {
			if (isThereAnyElementDifferentFrom(arrayDouble[i], d))
				diff = true;
		}
		return diff;
	}

	public static boolean isThereAnyElementDifferentFrom(double[] arrayDouble, double d) {
		boolean diff = false;
		for (int i = 0; i < arrayDouble.length; i++) {
			if (Math.abs(arrayDouble[i] - d) > verySmall)
				diff = true;
		}
		return diff;
	}
	
	
	/**
	 * This method decomposes a String into a vector of String instances according to a particular token
	 * @param definition the String
	 * @param token the token that defines the part of the string
	 * @return a List of String instances
	 */
	public static List<String> decomposeUsingToken(String definition, String token) {
		List<String> strings = new ArrayList<String>();
		StringTokenizer tkz = new StringTokenizer(definition, token);
		do {
			strings.add(tkz.nextToken().trim());
		} while (tkz.hasMoreTokens());
		return strings;
	}


	
	/**
	 * This method returns the path of a particular class. 
	 * @param anyClass any class of the system
	 * @return a String instance
	 */
	@SuppressWarnings("rawtypes")
	public static String getBinPath(Class anyClass) {
		String binPath;
		try {
			binPath = anyClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			int lastPathSeparator = binPath.lastIndexOf(PathSeparator);
			binPath = binPath.substring(0, lastPathSeparator).concat(PathSeparator);
	    	return binPath;
		} catch (URISyntaxException e) {
			return null;
		}
	}

	
	/**
	 * This method returns the root path of the application. 
	 * @param anyClass any class of the system
	 * @return a String instance
	 */
	@SuppressWarnings("rawtypes")
	public static String getTrueRootPath(Class anyClass) {
		try {
			String binPath = getBinPath(anyClass);
			binPath = binPath.substring(0, binPath.length() - 1); // to remove the last separator
			int lastPathSeparator = binPath.lastIndexOf(PathSeparator);
			return binPath.substring(0, lastPathSeparator).concat(PathSeparator);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * This method returns the path of a particular class. 
	 * @param anyClass any class of the system
	 * @return a String instance
	 */
	@SuppressWarnings("rawtypes")
	public static String getPackagePath(Class anyClass) {
		String binPath = getBinPath(anyClass);
		if (binPath == null) {
			return null;
		} else {
			String packagePath = binPath.concat(ObjectUtility.getRelativePackagePath(anyClass));
	    	return packagePath;
		}
	}

	
	/**
	 * This method returns the relative path of a particular class with respect to the rootpath. 
	 * @param anyClass any class of the system
	 * @return a String instance
	 */
	@SuppressWarnings("rawtypes")
	public static String getRelativePackagePath(Class anyClass) {
		return anyClass.getPackage().getName().replace(".", ObjectUtility.PathSeparator) + ObjectUtility.PathSeparator;
	}

	/**
	 * This method extracts substrings between particular sequences, typically between "(" and ")". The substrings
	 * are stored in a List and taken out of the original string.
	 * @param originalString the original string
	 * @param beginsWith the sequence that determines the beginning
	 * @param endsWith the sequence that determines the end
	 * @return a List of String with the first element being the original string cleared of the substring
	 */
	public static List<String> extractSequences(String originalString, String beginsWith, String endsWith) {
		List<String> occurrences = new ArrayList<String>();
		int beginsWithIndex;
		int endsWithIndex;
		while ((beginsWithIndex = originalString.indexOf(beginsWith)) != -1 
			&& (endsWithIndex = originalString.indexOf(endsWith)) != -1) {
			if (endsWithIndex > beginsWithIndex) {
				String occurrence = originalString.substring(beginsWithIndex, endsWithIndex + 1);
				occurrences.add(occurrence);
				originalString = originalString.replace(occurrence, "");
			}
		}
		occurrences.add(0, originalString);
		return occurrences;
	}

	
	/**
	 * This static method returns a new array of double, each double being the product of
	 * the value in the original array and the scalar parameter.
	 * @param array an array of double
	 * @param scalar the product factor
	 * @return an array of double or null if the original array is null
	 */
	public static double[] multiplyArrayByScalar(double[] array, double scalar) {
		if (array == null) {
			return null;
		}
		double[] newArray = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i] * scalar;
		}
		return newArray;
	}

	/**
	 * This method merge the two maps into a third map.
	 * @param map1 the first map of the merger
	 * @param map2 the second map of the merger
	 * @return the resulting map
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map mergeMaps(Map map1, Map map2) {
		Map outputMap = ObjectUtility.copyMap(map1);
		for (Object key : map2.keySet()) {
			Object value2 = map2.get(key);
			if (!outputMap.containsKey(key)) {
				if (value2 instanceof Map) {
					outputMap.put(key, new HashMap());
				} else if (value2 instanceof Double) {
					outputMap.put(key, 0d);
				} else {
					throw new InvalidParameterException("The method mergeMaps does not accept maps whose values are not instance of Map or Double!");
				}
			}
			Object value1 = outputMap.get(key);
			if (value2 instanceof Map) {
				outputMap.put(key, mergeMaps((Map) value1, (Map) value2));
			} else  if (value2 instanceof Double) {
				double sum = (Double) value2 + (Double) value1;
				outputMap.put(key, sum);
			} else {
				throw new InvalidParameterException("The method mergeMaps does not accept maps whose values are not instance of Map or Double!");
			}
		}
		return outputMap;
	}
	
	
	/**
	 * This method relativize a given file with respect to a parent file. If the child file does not include
	 * the parent path, the method returns null.
	 * @param parent the parent path
	 * @param child the child path
	 * @return the relativized URI or null if the child file does not contain the parent path.
	 */
	public static URI relativizeTheseFile(File parent, File child) {
		if (!child.getAbsolutePath().contains(parent.getAbsolutePath())) {
			return null;
		} else {
			return parent.toURI().relativize(child.toURI());
		}
	}

	
	
	/**
	 * This method converts a List of T instance into a Vector of the
	 * same instances
	 * @param list a List object
	 * @return a Vector object
	 */
	public static <T> Vector<T> convertFromListToVector(List<T> list) {
		Vector<T> vector = new Vector<T>();
		if (list != null && !list.isEmpty()) {
			for (T type : list) {
				vector.add(type);
			}
		}
		return vector;
	}
	
	/**
	 * This method splits a String into a list of string given a particular token and the
	 * possibility of having the \" character.
	 * @param lineRead the string
	 * @param token the field separator
	 * @return a List of String instances
	 */
	public static List<String> splitLine(String lineRead, String token) {
		List<String> strings = new ArrayList<String>();
		int i = -1;
		int j = 0;
		boolean stringOpened = false;
		while (!lineRead.isEmpty()) {
			if (j == 0 && String.valueOf(lineRead.charAt(j)).equals("\"")) {
				stringOpened = true;
			} else if (i == lineRead.length() - 1) {	// means the last character is a separator
				strings.add("");		
				break;
			} else if (!stringOpened && String.valueOf(lineRead.charAt(j)).equals(token)) {	// separator with no string on
				strings.add(lineRead.substring(i + 1, j));
				i = j;
			} else if (j == lineRead.length() - 1) {	// end of the line 
				if (stringOpened) {
					if (String.valueOf(lineRead.charAt(j)).equals("\"")) {
						strings.add(lineRead.substring(i + 2, j));
						stringOpened = false;
						break;
					} else {
						throw new InvalidParameterException("A string has been opened but there is no closing!");
					}
				} else {
					strings.add(lineRead.substring(i + 1));
					break;
				} 
			} else if (!stringOpened && j >= 1 && lineRead.substring(j - 1, j + 1).equals(token + "\"")) {
				stringOpened = true;
			} else if (stringOpened && lineRead.substring(j, j + 2).equals("\"" + token)) {
				strings.add(lineRead.substring(i + 2, j));
				stringOpened = false;
				i = j + 1;
				j = i;
			}
			j++;
		}
		return strings;
	}

	/**
	 * Returns a boolean that indicates whether the class is embedded in a Jar file or not.
	 * @param clazz the class
	 * @return a boolean true means the class is in a jar file or false otherwise
	 */
	public static boolean isEmbeddedInJar(Class<?> clazz) {
		String className = clazz.getSimpleName();
		URL resourceURL = clazz.getResource(className + ".class");
		String resourcePath = resourceURL.toString();
		return resourcePath.startsWith("jar:");
	}
	
	
	
}