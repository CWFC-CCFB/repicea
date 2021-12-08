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

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class RemoteEventManager implements Closeable {

	protected enum InternalCall {CloseConnection};
	
	private static class ListenerObjectMatch {
		private RemoteListener listener;
		private Object obj;
		
		private ListenerObjectMatch(Object obj, RemoteListener listener) {
			this.obj = obj;
			this.listener = listener;
		}
	}
	
	
	private static class InternalReceiver extends Thread {

		private RemoteEventManager remoteEventManager;
		
		private InternalReceiver(RemoteEventManager remoteEventManager) {
			this.remoteEventManager = remoteEventManager;
			setDaemon(true);
			setName("RemoteEventManager - InternalReceiver");
			start();
		}
		
		@Override
		public void run() {
			Object obj;
			try {
				do {
					obj = remoteEventManager.socket.readObject();
					remoteEventManager.whatShouldIDoWithThisObjectIJustReceived(obj);
				} while (!obj.equals(InternalCall.CloseConnection));
				
			} catch (SocketException exc) {
				exc.printStackTrace();
				remoteEventManager.sendCloseCall();
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}		
	}

	
	private static class InternalSender extends Thread {

		private RemoteEventManager remoteEventManager;
		
		private InternalSender(RemoteEventManager eventConnector) {
			this.remoteEventManager = eventConnector;
			setDaemon(true);
			setName("RemoteEventManager - InternalSender");
			start();
		}
		
		@Override
		public void run() {
			Object obj;
			try {
				do {
					obj = remoteEventManager.queue.take();
					remoteEventManager.socket.writeObject(obj);
				} while (!obj.equals(InternalCall.CloseConnection));
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

	
	private static class AutoShutdownThread extends Thread {
		
		private RemoteEventManager remoteEventManager;
		
		private AutoShutdownThread(RemoteEventManager remoteEventManager) {
			this.remoteEventManager = remoteEventManager;
			setDaemon(true);
			setName("RemoteEventManager - Autoshutdown");
			start();
		}
		
		@Override
		public void run() {
			try {
				remoteEventManager.internalReceiver.join();
				remoteEventManager.internalSender.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					remoteEventManager.socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}
	
	
	private LinkedBlockingQueue<Serializable> queue;
	
	private SocketWrapper socket;
	
	private Map<Integer, EventListener> localListenersMap;
	private List<RemoteListener> remoteListeners;
	private List<ListenerObjectMatch> listenerObjectMatchList;
	
	private final Object lock = new Object();
	
	private InternalReceiver internalReceiver; 
	private InternalSender internalSender;
	private AutoShutdownThread autoShutdown;
	
	protected RemoteEventManager(SocketWrapper socket) {
		this.socket = socket;
		localListenersMap = new HashMap<Integer, EventListener>();
		listenerObjectMatchList = new ArrayList<ListenerObjectMatch>();
		queue = new LinkedBlockingQueue<Serializable>();
		internalReceiver = new InternalReceiver(this);
		internalSender = new InternalSender(this);
		autoShutdown = new AutoShutdownThread(this);
	}
	
	/**
	 * This class register the local listeners and sends the associated remote listeners through 
	 * the socket.
	 * @param listeners a List of Event Listener
	 * @throws IOException if the remote listeners cannot be sent through the socket
	 */
	protected void registerListeners(List<EventListener> listeners) throws IOException {
		localListenersMap.clear();
		List<RemoteListener> remoteListeners = new ArrayList<RemoteListener>();
		for (EventListener listener : listeners) {
			localListenersMap.put(listener.hashCode(), listener);
			RemoteListener remoteListener = RemoteListener.getInstanceFromThisListener(listener);
			remoteListeners.add(remoteListener);
		}
		socket.writeObject(remoteListeners);			// TODO: not clean fix that
	}
	
	
	private void setRemoteListeners(List<RemoteListener> remoteListeners) {
		this.remoteListeners = remoteListeners;
		for (RemoteListener remoteListener : this.remoteListeners) {
			remoteListener.setConnector(this);
		}
		synchronized(lock) {
			lock.notify();
		}
	}
	
	
	/**
	 * This method returns the remote listeners. It blocks if the remote listeners are not set already.
	 * @return a List of remoteListeners
	 * @throws InterruptedException if the lock is interrupted for unknown reason
	 */
	protected List<RemoteListener> getRemoteListeners() throws InterruptedException {
		synchronized(lock) {
			while (remoteListeners == null) {
				lock.wait();
			}
		}
		return remoteListeners;
	}
	
	
	/**
	 * This method already handles object that are either RemoteEvent instance and 
	 * List of RemoteListener instances. It can be overriden in derived class in order to
	 * handle objects from other classes.
	 * @param obj an instance from unknown instance
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void whatShouldIDoWithThisObjectIJustReceived(Object obj) {
		if (obj instanceof RemoteEvent) {
			int targetHashCode = ((RemoteEvent) obj).getTargetHashCode();
			EventObject event = ((RemoteEvent) obj).getEvent();
			EventListener listener = localListenersMap.get(targetHashCode);
			if (listener != null) {
				new RemoteEventExecutor(listener, event);
			}
		} else if (obj instanceof List) {
			List list = (List) obj;
			if (!list.isEmpty() && list.get(0) instanceof RemoteListener) {
				setRemoteListeners(list);
			}
		} else if (obj.equals(InternalCall.CloseConnection)) {
			if (internalSender.isAlive()) {		// if false means that this is the feedback and close has already been called
				sendCloseCall();
			}
		}
	}

	
	/**
	 * This method sends the local listeners. By default, it runs the registerListener method.
	 * It can be overriden to implement additional operations.
	 * @param listeners a List of EventListener instances
	 * @throws IOException
	 */
	public void sendLocalListeners(List<EventListener> listeners) throws IOException {
		registerListeners(listeners);
	}
	
	
	/**
	 * This method connects the remote listeners to the appropriate object.
	 * @throws Exception 
	 */
	public abstract void connectRemoteListeners() throws Exception;
	
	
	/**
	 * This method should be used to match the objects and the listeners. It automatically finds out the appropriate method and
	 * records the match in a list for further removal when the close method is called.
	 * @param obj the object that produces the events
	 * @param listener a RemoteListener instance
	 * @throws Exception if the method does not exist or cannot be accessed.
	 */
	protected void connectListenerAndObject(Object obj, RemoteListener listener) throws Exception {
		addOrRemoveListenerFromThisObject(obj, listener, true);
		listenerObjectMatchList.add(new ListenerObjectMatch(obj, listener));
	}
	
	
	private void addOrRemoveListenerFromThisObject(Object obj, RemoteListener listener, boolean add) throws Exception {
		String prefix;
		if (add) {
			prefix = "add";
		} else {
			prefix = "remove";
		}

		Class<?> clazz = obj.getClass();
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getName().startsWith(prefix) && method.getName().toLowerCase().trim().endsWith("listener")) {
				Class<?>[] parameterClasses = method.getParameterTypes();
				if (parameterClasses.length == 1 && parameterClasses[0].isInstance(listener)) {
					method.invoke(obj, listener);
					return;
				}
			}
		}
		
		throw new Exception("The listener cannot be added or removed from the object because no appropriate method exists");
	}

	
	protected void addToSendingQueue(Serializable object) {
		queue.add(object);
	}
	
	
	@Override
	public void close() {
		sendCloseCall();
		for (ListenerObjectMatch lom : listenerObjectMatchList) {
			try {
				addOrRemoveListenerFromThisObject(lom.obj, lom.listener, false);	// false remove this listener;
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
	
	private void sendCloseCall() {
		addToSendingQueue(InternalCall.CloseConnection);
	}
	
	
	/**
	 * This method makes the main thread waits for the receiver and the sender threads to die. When they do,
	 * the method calls close.
	 * @throws InterruptedException
	 * @throws IOException
	 */
	protected void waitForCloseCall() throws InterruptedException {
		autoShutdown.join();
	}

	
	
}
