/*
 * This file is part of the repicea library.
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
package repicea.rgatewayserver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import repicea.math.Matrix;

class RCodeTranslator {

	private final static Map<Integer, Object> ENVIRONMENT = new ConcurrentHashMap<Integer, Object>();

	private final static Map<String, Class<?>> PrimitiveTypeMap = new HashMap<String, Class<?>>();
	static {
		PrimitiveTypeMap.put("integer", int.class);
		PrimitiveTypeMap.put("character", String.class);
		PrimitiveTypeMap.put("numeric", double.class);
		PrimitiveTypeMap.put("logical", boolean.class);
	}

	private	final static Set<Class<?>> PrimitiveJavaWrapper	= new HashSet<Class<?>>();
	static {
		PrimitiveJavaWrapper.add(Double.class);
		PrimitiveJavaWrapper.add(Integer.class);
		PrimitiveJavaWrapper.add(Long.class);
		PrimitiveJavaWrapper.add(Float.class);
		PrimitiveJavaWrapper.add(String.class);
		PrimitiveJavaWrapper.add(Boolean.class);
	}
	
	static class MethodWrapper implements Comparable<MethodWrapper> {

		final double score; 
		final Method method;
		
		MethodWrapper(double score, Method method) {
			this.score = score;
			this.method = method;
		}
		
		@Override
		public int compareTo(MethodWrapper o) {
			if (score < o.score) {
				return -1;
			} else  if (score == o.score) {
				return 0;
			} else {
				return 1;
			}
		}
		
	}
	
	
	private class ParameterWrapper {
		
		final Class<?> type;
		final Object value;
		
		private ParameterWrapper(Class<?> type, Object value) {
			this.type = type;
			this.value = value;
		}
		
		@Override
		public String toString() {
			if (PrimitiveJavaWrapper.contains(type)) {
				if (type.equals(Double.class) || type.equals(Float.class)) {
					return "numeric" + ((Number) value).toString();
				} else if (type.equals(Integer.class) || type.equals(Long.class)) {
					return "integer" + ((Number) value).toString();
				} else if (type.equals(Boolean.class)) {
					return "logical" + ((Boolean) value).toString();
				} else {
					return "character" + value.toString();
				}
			} else {
				return "JavaObject" + ";" + type.getName() + ";" + value.hashCode();
			}
		}
	}
		
	private static String ConstructCode = "create";
	private static String MethodCode = "method";
	private static String KillCode = "kill";

	
	public Object processCode(String request) throws Exception {
		String[] requestStrings = request.split(";");
		if (requestStrings[0].equals(ConstructCode)) {
			return createObjectFromRequestStrings(requestStrings); 
		} else if (requestStrings[0].equals(MethodCode)) {
			return processMethod(requestStrings);
		} else if (requestStrings[0].equals(KillCode)) {
			killThisObject(requestStrings);
			return null;
		} else {
			throw new InvalidParameterException("Request unknown! " + request);
		}

	}
	
	private void killThisObject(String[] requestStrings) {
		Object caller = findObjectInEnvironment(requestStrings[1]).value;
		ENVIRONMENT.remove(caller.hashCode());
	}


	private ParameterWrapper findObjectInEnvironment(String string) {
		String prefix = "java.objecthashcode";
		if (string.startsWith(prefix)) {
			int hashcodeForThisJavaObject = Integer.parseInt(string.substring(prefix.length()));
			if (ENVIRONMENT.containsKey(hashcodeForThisJavaObject)) {
				Object value = ENVIRONMENT.get(hashcodeForThisJavaObject);
				Class<?> type = value.getClass();
				return new ParameterWrapper(type, value);
			} else {
				throw new InvalidParameterException("This object does not exist: " + string);
			}
		}
		return null;
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object processMethod(String[] requestStrings) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		long start = System.currentTimeMillis();
		Object caller = findObjectInEnvironment(requestStrings[1]).value;
		List[] outputLists = marshallParameters(requestStrings, 3);
		List<Class<?>> parameterTypes = outputLists[0];
		List<Object> parameters = outputLists[1];
		String methodName = requestStrings[2];
		Method met;
		try {
			met = caller.getClass().getMethod(methodName, parameterTypes.toArray(new Class[]{}));
		} catch (NoSuchMethodException e1) {		// might be because the types are from derived classes
			Method[] methods = caller.getClass().getMethods();
			List<MethodWrapper> possibleMatches = new ArrayList<MethodWrapper>();
			for (Method method : methods) {
				if (method.getName().equals(methodName)) {	// possible match
					Class[] classes = method.getParameterTypes();
					double score = doParameterTypesMatch(classes, parameterTypes.toArray(new Class[]{}));
					if (score >= 0) {
						possibleMatches.add(new MethodWrapper(score, method));
					}
				}
			}
			if (possibleMatches.isEmpty()) {
				throw e1;
			} else {
				Collections.sort(possibleMatches);
			}
			met = possibleMatches.get(0).method;
		} 			
		Object result = met.invoke(caller, parameters.toArray());
		double elapsedTime = (System.currentTimeMillis() - start) / 1000d;
		System.out.println("Time to process method " + elapsedTime);

		if (result != null) {
			if (!PrimitiveJavaWrapper.contains(result.getClass())) {
				ENVIRONMENT.put(result.hashCode(), result);
			}
			return new ParameterWrapper(result.getClass(), result);
		} else {
			return null;
		}
	}

	private double doParameterTypesMatch(Class<?>[] ref, Class<?>[] obs) {
		if (ref == null && obs == null) {
			return 0d;
		} else if (ref != null && obs != null) {
			if (ref.length == obs.length) {
				Matrix scores = new Matrix(ref.length, 1);
				for (int i = 0; i < ref.length; i++) {
					double score = isAssignableOfThisClass(ref[i], obs[i]);
					if (score == -1d) {
						return -1d;
					} else {
						scores.m_afData[i][0] = score;
					}
				}
				return scores.getSumOfElements();
			}
		}
		return -1d;
	}

	private boolean implementThisClassAsAnInterface(Class<?> refcl1, Class<?> cl) {
		Class<?>[] interfaces = cl.getInterfaces();
		for (Class<?> inter : interfaces) {
			if (inter.getName().equals(refcl1.getName())) {
				return true;
			}
		}
		return false;
	}
	
	
	
	private double isAssignableOfThisClass(Class<?> refcl1, Class<?> cl2) {
		int degree = 0;
		Class<?> cl = cl2;
		boolean isInterfaceMatching = implementThisClassAsAnInterface(refcl1, cl);
		while (!refcl1.getName().equals(cl.getName()) && !isInterfaceMatching && !cl.getName().equals("java.lang.Object")) {
			cl = cl.getSuperclass();
			isInterfaceMatching = implementThisClassAsAnInterface(refcl1, cl);
			degree++;
		}
		if (refcl1.getName().equals(cl.getName())) {
			return degree;
		} else if (isInterfaceMatching) {
			return degree + .5;
		} else {
			return -1;
		}
	}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object createObjectFromRequestStrings(String[] requestStrings) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		long start = System.currentTimeMillis();
		Class<?> clazz = Class.forName(requestStrings[1]);
		List[] outputLists = marshallParameters(requestStrings, 2);
		List<Class<?>> parameterTypes = outputLists[0];
		List<Object> parameters = outputLists[1];
		Constructor<?> constructor = clazz.getConstructor(parameterTypes.toArray(new Class[]{}));
		Object newInstance = constructor.newInstance(parameters.toArray());
		ENVIRONMENT.put(newInstance.hashCode(), newInstance);
		double elapsedTime = (System.currentTimeMillis() - start) / 1000d;
		System.out.println("Time to construct object " + elapsedTime);
		return new ParameterWrapper(newInstance.getClass(), newInstance);
	}
	
	@SuppressWarnings("rawtypes")
	private List[] marshallParameters(String[] args, int start) {
		List[] outputLists = new List[2];
		List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
		List<Object> parameters = new ArrayList<Object>();
		outputLists[0] = parameterTypes;
		outputLists[1] = parameters;
		for (int i = start; i < args.length; i++) {
			ParameterWrapper parameterWrapper = createFromPrimitiveClass(args[i]);
			if (parameterWrapper == null) {
				parameterWrapper = findObjectInEnvironment(args[i]);
			}
			if (parameterWrapper != null) {
				parameterTypes.add(parameterWrapper.type);
				parameters.add(parameterWrapper.value);
			}
		}
		return outputLists;
	}

	
	private ParameterWrapper createFromPrimitiveClass(String string) {
		for (String type : PrimitiveTypeMap.keySet()) {
			if (string.startsWith(type)) {
				if (type == "character") {
					return new ParameterWrapper(String.class, string.substring(9));
				} else if (type == "numeric") {
					return new ParameterWrapper(double.class, Double.parseDouble(string.substring(7)));
				} else if (type == "integer") {
					return new ParameterWrapper(int.class, Integer.parseInt(string.substring(7)));
				} else if (type == "logical") {
					String subString = string.substring(7).toLowerCase();
					return new ParameterWrapper(boolean.class, Boolean.valueOf(subString).booleanValue());
				}
			}
		}
		return null;
	}

}
