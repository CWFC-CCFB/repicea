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
package repicea.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class implements all the basic features to generate PropertyChangeEvent on the Event Dispatch
 * Thread. It also implements two protected methods that serves to notify AWT components : firePropertyChange()
 * and setProgress(). 
 * @author Mathieu Fortin - December 2011
 */
public abstract class PropertyChangeEventGeneratingClass {

	// TODO this class does not serve for anything should be deleted initially aimed at implementing a delay in the application of the progress call
	protected static class EventHandler {
		
		public EventHandler(PropertyChangeEventGeneratingClass caller, PropertyChangeEvent event) {
			String property = event.getPropertyName();
			if (!property.equals("progress") || !caller.onGoingProgressCall) {
				if (property.equals("progress")) {
					caller.onGoingProgressCall = true;
				}
							
				PropertyChangeEvent currentEvent;
				if (property.equals("progress")) {
					int newValue = caller.getProgressVector().lastElement();
					currentEvent = new PropertyChangeEvent(event.getSource(), property, event.getOldValue(), newValue);		// actualized the progress
					caller.formerProgressFactor = newValue;
					caller.onGoingProgressCall = false;
				} else {
					currentEvent = event;
				}
				
				for (PropertyChangeListener listener : caller.getListeners()) {
					new PropertyChangeEventExecutor(listener, currentEvent);
				}
			}
		}
	}
	
	
	private Collection<PropertyChangeListener> listeners;
	private Vector<Integer> progressVector;
	private boolean onGoingProgressCall;
	private int formerProgressFactor;
//	private FixedSizeCopyOnWriteArrayList<PropertyChangeEvent> lastEvents;

	
	private Vector<Integer> getProgressVector() {
		if (progressVector == null) {
			progressVector = new Vector<Integer>();
		}
		return progressVector;
	}
	
//	private FixedSizeCopyOnWriteArrayList<PropertyChangeEvent> getLastEvents() {
//		if (lastEvents == null) {
//			lastEvents = new FixedSizeCopyOnWriteArrayList<PropertyChangeEvent>(5);
//		}
//		return lastEvents;
//	}
	
	private Collection<PropertyChangeListener> getListeners() {
		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<PropertyChangeListener>(); 
		}
		return listeners;
	}

	/**
	 * This method serves to send a PropertyChangeEvent instance on the Event Dispatch Thread.
	 * @param propertyName the name of the property
	 * @param oldValue the former value of this property if any
	 * @param newValue the current value of this property
	 */
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		new EventHandler(this, event);
//		getLastEvents().add(event);
	}

	
//	/**
//	 * This method re-fire the last events if there are any.
//	 */
//	protected void refireLastPropertyChanges() {
//		if (!getLastEvents().isEmpty()) {
//			for (PropertyChangeEvent lastEvent : getLastEvents()) {
//				new EventHandler(this, lastEvent);
//			}
//		}
//	}
	
		
	/**
	 * This method sends a PropertyChangeEvent with property set to "progress" on the Event Dispatch
	 * Thread. The method is executed asynchronously on the EDT. The event is updated just before being
	 * sent on the thread to make sure the last progress will be recorded.
	 * @param progress an integer between 0 and 100. Any value outside this range will be set to the limit.
	 */
	protected void setProgress(int progress) {
		if (progress > 100) {
			progress = 100;
		} else if (progress < 0) {
			progress = 0;
		}
		getProgressVector().add(progress);
		firePropertyChange("progress", formerProgressFactor, progress); 
	}

	/**
	 * This method adds a PropertyChangeListener instance to the list of listeners. The instance is added only
	 * if it is not already contained in the list of listeners.
	 * @param listener the PropertyChangeListener instance to be added
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (!getListeners().contains(listener)) {
			getListeners().add(listener);
		}
	}

	/**
	 * This method removes a particular PropertyChangeListener instance from the list of listeners. Nothing
	 * happens if the listeners collection does not contain the instance.
	 * @param listener the PropertyChangeListener instance to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		while (getListeners().contains(listener)) {
			getListeners().remove(listener);
		}
	}
	
	
	
//	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
//		Object obj = new Object();
//		PropertyChangeEvent event = new PropertyChangeEvent(obj, "test", null, "allo");
//		RemotePropertyChangeListener listener = new RemotePropertyChangeListener(obj.hashCode());
//		Class<? extends EventObject> eventClass = event.getClass();
//		if (listener != null) {
//			Method[] methods = listener.getClass().getMethods();
//			for (Method method : methods) {
//				Class[] classes = method.getParameterTypes();
//				for (Class clazz : classes) {
//					if (clazz.equals(eventClass)) {
//						method.invoke(listener, event);
//					}
//				}
//			}
//		}
//
//
//	}

}
