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
package repicea.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

/**
 * The automatedHelper class contains a method and its arguments to be called when the Help button is hit.
 * @author Mathieu Fortin - January 2011
 */
public class AutomatedHelper {

	private Method method;
	private Object[] arguments;

	
	/**
	 * The constructor requires the help method and its arguments.
	 * @param method the help method
	 * @param arguments the arguments of the method
	 */
	public AutomatedHelper(Method method, Object[] arguments) {
		if (method == null) {
			throw new InvalidParameterException("The method parameter must be defined!");
		}
		this.method = method;
		this.arguments = arguments;
	}

	/**
	 * This method runs the help method with its argument. It is fully protected against exceptions.
	 */
	public void callHelp() {
		try {
			method.invoke(null, arguments);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
