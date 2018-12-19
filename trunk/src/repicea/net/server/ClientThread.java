/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge Epicea.
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
package repicea.net.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import repicea.net.SocketWrapper;
import repicea.net.server.AbstractServer.Mode;
import repicea.net.server.AbstractServer.ServerReply;
import repicea.util.PropertyChangeEventGeneratingClass;

public abstract class ClientThread extends PropertyChangeEventGeneratingClass implements Runnable, ActionListener {
	
	private AbstractServer caller;
	private SocketWrapper socketWrapper;
	
	private int workerID;
	private InetAddress clientAddress;
	
	private Thread worker;
	private final Object lock = new Object();
	
	private Map<String, PropertyChangeEvent> statusMap;
	

	/**
	 * Public constructor.
	 * @param caller a CapsisServer instance
	 * @param workerID an integer that serves to identify this client thread
	 */
	protected ClientThread(AbstractServer caller, int workerID) {
		super();
		this.caller = caller;
		this.workerID = workerID;
		statusMap = new HashMap<String, PropertyChangeEvent>();
	}

	
	@Override
	public void run() {
		while (true) {
			try {
				firePropertyChange("status", null, "Waiting");
				socketWrapper = caller.getWaitingClients();
				clientAddress = socketWrapper.getSocket().getInetAddress();
				firePropertyChange("status", null, "Connected to client: " + clientAddress.getHostAddress());

				while (!socketWrapper.isClosed()) {
					try {
						firePropertyChange("status", null, "Processing request");
						Object somethingInParticular = processRequest();
						if (caller.mode == Mode.AnswerProcessAndClose ||
								(somethingInParticular != null && somethingInParticular.equals(BasicClient.ClientRequest.closeConnection))) {
							socketWrapper.writeObject(ServerReply.ClosingConnection);
							firePropertyChange("status", null, "Disconnecting from client: " + clientAddress.getHostAddress());
							closeSocket();
						} else if (somethingInParticular != null) {
							socketWrapper.writeObject(somethingInParticular);
						} else {
							socketWrapper.writeObject(ServerReply.RequestReceivedAndProcessed);
						}
					} catch (Exception e) {		// something wrong happened during the processing of the request
						try {
							e.printStackTrace();
							if (!socketWrapper.isClosed()) {
								socketWrapper.writeObject(e);
							}
							if (caller.mode == Mode.AnswerProcessAndClose) {	// if is this mode, we hang up and we stop !!!
								closeSocket();
								firePropertyChange("status", null, "Interrupted");
								firePropertyChange("restartButton", null, true);
								synchronized (lock) {
									lock.wait();
								}
							}
						} catch (IOException e1) {
							socketWrapper = null;
						}
					}
				}
			} catch (InterruptedException e) {}
		}
	}



	protected abstract Object processRequest() throws Exception;


	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("Restart")) {
				restartAction();
		} 
	}

	protected void restartAction() {
		synchronized(lock) {
			lock.notify();
		}
		firePropertyChange("restartButton", null, false);		// to disable the restart button
	}
	
	
	protected void start() {
		worker = new Thread(this);
		worker.setName("Client thread no " + workerID);
		worker.start();
	}
	
	
	/**
	 * This method returns the ID of the worker.
	 * @return an Integer
	 */
	protected int getWorkerID() {return workerID;}
	
	private void closeSocket() throws IOException {
		if (socketWrapper != null) {
			socketWrapper.close();
		}
		clientAddress = null;
		firePropertyChange("status", null, "Waiting");
	}
	
	
	protected SocketWrapper getSocket() {return socketWrapper;}
	
	/*
	 * This method overrides the original method. It records the property in the statusMap member.
	 */
	@Override
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		new EventHandler(this, event);
		statusMap.put(propertyName, event);
	}
	
	
	/**
	 * This method fires the current status of the worker
	 */
	protected void fireCurrentStatus() {
		super.firePropertyChange("currentStatus", null, statusMap);
	}
	
}
