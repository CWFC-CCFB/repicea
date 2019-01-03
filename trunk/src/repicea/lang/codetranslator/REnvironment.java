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
package repicea.lang.codetranslator;

import java.io.File;
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

import repicea.console.JavaProcessWrapper;
import repicea.lang.REpiceaClassLoader;
import repicea.lang.REpiceaSystem;
import repicea.lang.codetranslator.REpiceaCodeTranslator;
import repicea.math.Matrix;
import repicea.multiprocess.JavaProcess;
import repicea.multiprocess.JavaProcess.JVM_OPTION;
import repicea.net.server.BasicClient;
import repicea.net.server.JavaLocalGatewayServer;

@SuppressWarnings("serial")
public class REnvironment extends ConcurrentHashMap<Integer, Object> implements REpiceaCodeTranslator {

	private static final String EXTENSION = "-ext";
	
	private static final String FIRSTCALL = "-firstcall";


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
	
	static class JavaObjectList extends ArrayList<ParameterWrapper> {
		
		@Override
		public String toString() {
			String output = "JavaList;" ;
			for (ParameterWrapper obj : this) {
				String toBeAdded = obj.toString();
				if (toBeAdded.startsWith("JavaObject" + ";")) {
					toBeAdded = toBeAdded.substring(("JavaObject" + ";").length());
				}
				output = output + toBeAdded + ",";	
			}
			return output;
		}
	}
	
	static class ParameterList extends ArrayList<List<ParameterWrapper>> {
		int getInnerSize() {
			int currentSize = 0;
			for (int i = 0; i < size(); i++) {
				if (get(i).size() > currentSize) {
					currentSize = get(i).size();
				}
			}
			return currentSize;
		}
		
		Object[] getParameterArray(int i) {
			int currentSize = getInnerSize();
			if (i > currentSize) {
				throw new InvalidParameterException("Inconsistent parameter setup!");
			}
			Object[] parms = new Object[size()];
			for (int j = 0; j < size(); j++) {
				if (get(j).size() == 1) {
					parms[j] = get(j).get(0).value;
				} else {
					parms[j] = get(j).get(i).value;
				}
			}
			return parms;
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
				return "JavaObject" + ";" + type.getName() + "@" + System.identityHashCode(value);
			}
		}
	}
		
	private static String ConstructCode = "create";
	private static String MethodCode = "method";
	private static String SynchronizeEnvironment = "sync";

	@Override
	public Object processCode(String request) throws Exception {
		String[] requestStrings = request.split(";");
		if (requestStrings[0].equals(ConstructCode)) {
			return createObjectFromRequestStrings(requestStrings); 
		} else if (requestStrings[0].equals(MethodCode)) {
			return processMethod(requestStrings);
		} else if (requestStrings[0].equals(SynchronizeEnvironment)) {
			synchronizeEnvironment(requestStrings);
			return null;
		} else {
			try {
				return BasicClient.ClientRequest.valueOf(request);
			} catch (Exception e) {
				throw new InvalidParameterException("Request unknown! " + request);
			}
		}

	}
	
	private void synchronizeEnvironment(String[] requestStrings) {
		Map<Integer, Object> actualMap = new HashMap<Integer, Object>();
		for (int i = 1; i < requestStrings.length; i++) {
			Object caller = findObjectInEnvironment(requestStrings[i]).get(0).value;
			if (caller != null) {
				actualMap.put(System.identityHashCode(caller), caller);
			}
		}
		Map<Integer, Object> toBeRemoved = new HashMap<Integer, Object>();
		for (Object value : values()) {
			if (!actualMap.containsKey(System.identityHashCode(value))) {
				toBeRemoved.put(System.identityHashCode(value), value);
			}
		}
		for (Object value : toBeRemoved.values()) {
			remove(System.identityHashCode(value), value);
		}
	}


	private List<ParameterWrapper> findObjectInEnvironment(String string) {
		List<ParameterWrapper> wrappers = new ArrayList<ParameterWrapper>();
		String prefix = "java.objecthashcode";
		if (string.startsWith(prefix)) {
			String[] newArgs = string.substring(prefix.length()).split(",");
			for (int i = 0; i < newArgs.length; i++) {
				int hashcodeForThisJavaObject = Integer.parseInt(newArgs[i]);
				if (containsKey(hashcodeForThisJavaObject)) {
					Object value = get(hashcodeForThisJavaObject);
					Class<?> type = value.getClass();
					wrappers.add(new ParameterWrapper(type, value));
				} else {
					throw new InvalidParameterException("This object does not exist: " + string);
				}
			}
		}
		return wrappers;
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object processMethod(String[] requestStrings) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object caller = findObjectInEnvironment(requestStrings[1]).get(0).value;
		List[] outputLists = marshallParameters(requestStrings, 3);
		List<Class<?>> parameterTypes = outputLists[0];
		ParameterList parameters = (ParameterList) outputLists[1];
		String methodName = requestStrings[2];
		Method met;
		try {
			if (parameters.isEmpty()) {
				met = caller.getClass().getMethod(methodName, (Class[]) null);
			} else {
				met = caller.getClass().getMethod(methodName, parameterTypes.toArray(new Class[]{}));
			}
		} catch (NoSuchMethodException e) {		
			if (parameters.isEmpty()) {
				throw e;
			} else {	// the exception might arise from the fact that the types are from derived classes
				met = findNearestMethod(caller, methodName, parameterTypes);
			}
			
//			Method[] methods = caller.getClass().getMethods();
//			List<MethodWrapper> possibleMatches = new ArrayList<MethodWrapper>();
//			for (Method method : methods) {
//				if (method.getName().equals(methodName)) {	// possible match
//					Class[] classes = method.getParameterTypes();
//					double score = doParameterTypesMatch(classes, parameterTypes.toArray(new Class[]{}));
//					if (score >= 0) {
//						possibleMatches.add(new MethodWrapper(score, method));
//					}
//				}
//			}
//			if (possibleMatches.isEmpty()) {
//				throw e;
//			} else {
//				Collections.sort(possibleMatches);
//			}
//			met = possibleMatches.get(0).method;
		} 			
		
		JavaObjectList outputList = new JavaObjectList();
		if (parameters.isEmpty()) {
			Object result = met.invoke(caller, (Object[]) null);
			registerMethodOutput(result, outputList);
//			if (result != null) {
//				if (!PrimitiveJavaWrapper.contains(result.getClass())) {
//					put(System.identityHashCode(result), result);
//				}
//				outputList.add(new ParameterWrapper(result.getClass(), result));
//			} 
		} else {
			for (int i = 0; i < parameters.getInnerSize(); i++) {
				Object result = met.invoke(caller, parameters.getParameterArray(i));
				registerMethodOutput(result, outputList);
//				if (result != null) {
//					if (!PrimitiveJavaWrapper.contains(result.getClass())) {
//						put(System.identityHashCode(result), result);
//					}
//					outputList.add(new ParameterWrapper(result.getClass(), result));
//				} 
			}		
		}
		if (outputList.isEmpty()) {
			return null;
		} else if (outputList.size() == 1) {
			return outputList.get(0);
		} else {
			return outputList;
		}
	}

	@SuppressWarnings("rawtypes")
	private Method findNearestMethod(Object caller, String methodName, List<Class<?>> parameterTypes) throws NoSuchMethodException {
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
			throw new NoSuchMethodException("Method " + methodName + "cannot be found in the class " + caller.getClass().getSimpleName());
		} else {
			Collections.sort(possibleMatches);
		}
		return possibleMatches.get(0).method;
	}
	
	private void registerMethodOutput(Object result, JavaObjectList outputList) {
		if (result != null) {
			if (!PrimitiveJavaWrapper.contains(result.getClass())) {
				put(System.identityHashCode(result), result);
			}
			outputList.add(new ParameterWrapper(result.getClass(), result));
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
			if (cl.isPrimitive()) {	// the superclass of primitive is assumed to be java.lang.Object
				cl = Object.class;
			} else {
				cl = cl.getSuperclass();
			}
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
		JavaObjectList outputList = new JavaObjectList();
		Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(requestStrings[1]);
		List[] outputLists = marshallParameters(requestStrings, 2);
		List<Class<?>> parameterTypes = outputLists[0];
		ParameterList parameters = (ParameterList) outputLists[1];
		if (parameters.isEmpty()) { // constructor with no argument then
			Object newInstance = clazz.newInstance();
			registerNewInstance(newInstance, outputList);
//			put(System.identityHashCode(newInstance), newInstance);
//			outputList.add(new ParameterWrapper(newInstance.getClass(), newInstance));
		} else {
			for (int i = 0; i < parameters.getInnerSize(); i++) {
				Constructor<?> constructor = clazz.getConstructor(parameterTypes.toArray(new Class[]{}));
				Object newInstance = constructor.newInstance(parameters.getParameterArray(i));
				registerNewInstance(newInstance, outputList);
//				put(System.identityHashCode(newInstance), newInstance);
//				outputList.add(new ParameterWrapper(newInstance.getClass(), newInstance));
			}
		}
		if (outputList.isEmpty()) {
			return null;
		} else if (outputList.size() == 1) {
			return outputList.get(0);
		} else {
			return outputList;
		}
	}

	private void registerNewInstance(Object newInstance, JavaObjectList outputList) {
		put(System.identityHashCode(newInstance), newInstance);
		outputList.add(new ParameterWrapper(newInstance.getClass(), newInstance));
		
	}
	
	
	@SuppressWarnings("rawtypes")
	private List[] marshallParameters(String[] args, int start) {
		List[] outputLists = new List[2];
		List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
		ParameterList parameters = new ParameterList();
		outputLists[0] = parameterTypes;
		outputLists[1] = parameters;
		for (int i = start; i < args.length; i++) {
			List<ParameterWrapper> subparametersList;
			String primitiveClassIfAny = getPrimitiveClass(args[i]);
			if (primitiveClassIfAny != null) {
				subparametersList = createFromPrimitiveClass(primitiveClassIfAny, args[i]);
			} else {
				subparametersList = findObjectInEnvironment(args[i]);
			}
			parameterTypes.add(subparametersList.get(0).type);
			parameters.add(subparametersList);
		}
		return outputLists;
	}

	private String getPrimitiveClass(String string) {
		for (String key : PrimitiveTypeMap.keySet()) {
			if (string.startsWith(key)) {
				return key;
			}
		}
		return null;
	}

	
	private List<ParameterWrapper> createFromPrimitiveClass(String primitiveTypeClass, String args) {
		List<ParameterWrapper> wrappers = new ArrayList<ParameterWrapper>();
		String[] newArgs = args.substring(primitiveTypeClass.length()).split(",");
		for (String value : newArgs) {
			if (primitiveTypeClass == "character") {
				wrappers.add(new ParameterWrapper(String.class, value));
			} else if (primitiveTypeClass == "numeric") {
				wrappers.add(new ParameterWrapper(double.class, Double.parseDouble(value)));
			} else if (primitiveTypeClass == "integer") {
				wrappers.add(new ParameterWrapper(int.class, Integer.parseInt(value)));
			} else if (primitiveTypeClass == "logical") {
				String subString = value.toLowerCase();
				wrappers.add(new ParameterWrapper(boolean.class, Boolean.valueOf(subString).booleanValue()));
			}
		}
		return wrappers;
	}

	
	public static void main(String[] args) throws Exception {
		List<String> arguments = REpiceaSystem.setClassicalOptions(args);
		String firstCall = REpiceaSystem.retrieveArgument(FIRSTCALL, arguments);
		if (firstCall != null && firstCall.toLowerCase().trim().equals("true")) {
			List<String> newCommands = new ArrayList<String>();
			String extensionPath = REpiceaSystem.retrieveArgument(EXTENSION, arguments);
			if (extensionPath != null) {
				newCommands.add("repicea.jar");
				newCommands.add(EXTENSION);
				newCommands.add(extensionPath);
			}
			File jarFile = new File(REnvironment.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			File rootPath = jarFile.getParentFile();

			JavaProcessWrapper rGatewayProcessWrapper = new JavaProcessWrapper("RGateway Server", newCommands, rootPath);
			JavaProcess rGatewayProcess = rGatewayProcessWrapper.getInternalProcess();
			rGatewayProcess.setOption(JVM_OPTION.SystemClassLoader, "-Djava.system.class.loader=repicea.lang.REpiceaClassLoader");
			rGatewayProcessWrapper.run();
			System.exit(0);
		}
		String extensionPath = REpiceaSystem.retrieveArgument(EXTENSION, arguments);
		if (extensionPath != null) {
			((REpiceaClassLoader) ClassLoader.getSystemClassLoader()).setExtensionPath(new File(extensionPath));
		}
		JavaLocalGatewayServer server = new JavaLocalGatewayServer(18011, new REnvironment());
		server.startApplication();
	}
	
	
	
}
