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
package repicea.net;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.EventObject;

import repicea.util.AbstractEventExecutor;

/**
 * This class handles a combination of listener:event. The run method finds the matching method in the listener 
 * for the event object. If the object belongs to a component, it is run on the Event Dispatch Thread.
 * @author Mathieu Fortin - December 2011
 */
@Deprecated
@SuppressWarnings("rawtypes")
public class RemoteEventExecutor extends AbstractEventExecutor {
	
	/**
	 * Protected constructor with unchecked listener and event.
	 * @param listener
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	protected RemoteEventExecutor(EventListener listener, EventObject event) {
		super(listener, event);
	}
	
	
	@Override
	public void run() {
		Method[] methods = listener.getClass().getMethods();
		for (Method method : methods) {
			Class[] classes = method.getParameterTypes();
			if (classes.length == 1 && classes[0].isInstance(event)) {		// the method should have only one argument and this argument has to be compatible with the class the event member belongs to
				try {
					method.invoke(listener, event);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
