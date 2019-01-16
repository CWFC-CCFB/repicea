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

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.EventListener;
import java.util.EventObject;

/**
 * The RemoteListener class is an abstract class for all listeners that work from a remote location. It
 * includes the hashcode of the local listener, its class name and a socket. Whenever the listener receives an
 * EventObject instance from any class it listens to, it sends this instance through the socket to the local listener.
 * @author Mathieu Fortin - December 2011
 */
public abstract class RemoteListener implements Serializable {

	private static final long serialVersionUID = 20111223L;
	
	private int remoteOwnerHashCode;
	private String remoteOwnerClassName;
	private RemoteEventManager connector;

	protected RemoteListener(int remoteOwnerHashCode, Class<?> remoteOwnerClass) {
		this.remoteOwnerHashCode = remoteOwnerHashCode;
		this.remoteOwnerClassName = remoteOwnerClass.getName();
	}

	
	/**
	 * This method sets the connector through which the events are sent to the local class.
	 * @param connector a RemoteEventManager instance
	 */
	public void setConnector(RemoteEventManager connector) {
		this.connector = connector;
	}
	
//	protected SocketWrapper getSocket() {return socket;}
	protected int getOwnerHashCode() {return remoteOwnerHashCode;}
	protected String getRemoteOwnerClassName() {return remoteOwnerClassName;}

	/**
	 * This static method identifies and returns an instance from the appropriate RemoteListener derived class.
	 * @param listener an EventListener class
	 * @return the appropriate RemoteListener instance
	 */
	public static RemoteListener getInstanceFromThisListener(EventListener listener) {
		if (listener instanceof PropertyChangeListener) {
			return new RemotePropertyChangeListener(listener.hashCode(), listener.getClass());
		} else if (listener instanceof ActionListener) {
			return new RemoteActionListener(listener.hashCode(), listener.getClass());
		}
		return null;
	}

	protected final void sendEventToOriginalListener(EventObject evt) {
		connector.addToSendingQueue(new RemoteEvent(getOwnerHashCode(), evt));
	}

	/**
	 * This method returns true if the owner of this remote listener is of the class of the clazz parameter.
	 * @param clazz a Class instance
	 * @return true if the class match or false otherwise
	 */
	public boolean isTheOwnerOfThisClass(Class<?> clazz) {
		Class<?> remoteOwnerClass;
		try {
			remoteOwnerClass = ClassLoader.getSystemClassLoader().loadClass(getRemoteOwnerClassName());
			return clazz.isAssignableFrom(remoteOwnerClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
