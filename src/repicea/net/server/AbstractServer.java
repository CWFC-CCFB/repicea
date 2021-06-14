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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import repicea.app.AbstractGenericEngine;
import repicea.net.SocketWrapper;
import repicea.net.TCPSocketWrapper;
import repicea.net.UDPSocketWrapper;
import repicea.net.server.ServerConfiguration.Protocol;
import repicea.net.server.ServerTask.ServerTaskID;

public abstract class AbstractServer extends AbstractGenericEngine implements PropertyChangeListener {

	protected static enum ServerReply {IAmBusyCallBackLater, 
		CallAccepted, 
		ClosingConnection,
		RequestReceivedAndProcessed}

	/**
	 * This internal class handles the calls and stores these in the queue.
	 * @author Mathieu Fortin - October 2011
	 */
	private class CallReceiverThread extends Thread {

		private boolean shutdownCall;
		protected final ServerSocket serverSocket;
		private final LinkedBlockingQueue<SocketWrapper> clientQueue;
		private final int maxNumberOfWaitingClients;

		/**
		 * General constructor. 
		 * @param serverSocket a ServerSocket instance if null then the protocol is assumed to be UDP with a single client
		 * @param clientQueue
		 * @param maxNumberOfWaitingClients
		 */
		private CallReceiverThread(ServerSocket serverSocket, LinkedBlockingQueue<SocketWrapper> clientQueue, int maxNumberOfWaitingClients) {
			shutdownCall = false;
			this.serverSocket = serverSocket;
			this.clientQueue = clientQueue;
			this.maxNumberOfWaitingClients = maxNumberOfWaitingClients;
			setName("Answering calls thread");
		}
		
		/*
		 * The swingworker
		 */
		@Override
		public void run() {
			try {
				while (!shutdownCall) {
					SocketWrapper clientSocket;
					if (serverSocket != null) {
						clientSocket = new TCPSocketWrapper(serverSocket.accept(), AbstractServer.this.isCallerAJavaApplication);
					} else {
						clientSocket = new UDPSocketWrapper(AbstractServer.this.configuration.outerPort, AbstractServer.this.isCallerAJavaApplication); // this should block
						shutdownCall = true;
					}
					if (maxNumberOfWaitingClients > 0 && clientQueue.size() < maxNumberOfWaitingClients) {
						clientSocket.writeObject(ServerReply.CallAccepted);
						clientQueue.add(clientSocket);
					} else if (AbstractServer.this.isThereAtLeastOneThreadWaiting()) {	// there is no waiting list here
						clientSocket.writeObject(ServerReply.CallAccepted);
						clientQueue.add(clientSocket);
					} else {
						clientSocket.writeObject(ServerReply.IAmBusyCallBackLater);
					}
				}
				System.out.println("Call receiver thread shut down");
			} catch (Exception e) {
				if (!shutdownCall) {
					e.printStackTrace();
				}
			} finally {
				try {
					if (serverSocket != null) {
						serverSocket.close();
					}	
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}


	/**
	 * This internal class handles the calls and stores these in the queue.
	 * @author Mathieu Fortin - October 2011
	 */
	private class InterfaceConnectionThread extends Thread {

		private ServerSocket serverSocket;
		private ServerSideRemoteEventConnector eventConnector;

		private InterfaceConnectionThread(AbstractServer server) {
			try {
				serverSocket = new ServerSocket(configuration.innerPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
			setName("Interface connector");
			setDaemon(true);
			start();
		}

		/*
		 * The swingworker
		 */
		@Override
		public void run() {
			SocketWrapper interfaceSocket;
			Socket socket;
			while (true) {
				try {
					socket = serverSocket.accept();
					InetAddress socketAddress = socket.getInetAddress();
					boolean isLocal = socketAddress.isLoopbackAddress(); 
					if (isLocal) {
						interfaceSocket = new TCPSocketWrapper(socket, true);
						eventConnector = new ServerSideRemoteEventConnector(interfaceSocket, AbstractServer.this);
						eventConnector.sendLocalListeners(getAllListeners());
						eventConnector.connectRemoteListeners();			// lock here until close call from the interface
					} else {
						socket.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (eventConnector != null) {
						eventConnector.close();
					}
				} finally {
					eventConnector = null;
				}
			}
		}
	}
	

//	private ServerSocket serverSocket;
	private ArrayList<ClientThread> clientThreads;
	protected final LinkedBlockingQueue<SocketWrapper> clientQueue;
	private CallReceiverThread callReceiver;
	
	protected final boolean isCallerAJavaApplication;
	
	private final ServerConfiguration configuration;

	private InterfaceConnectionThread interfaceThread;

	private List<PropertyChangeListener> listeners;

	/**
	 * Constructor.
	 * @param configuration a ServerConfiguration instance that defines the number of threads, the reference path and the filename of the exception rules
	 * @param isCallerAJavaApplication true if the client is a Java app 
	 * @throws Exception
	 */
	protected AbstractServer(ServerConfiguration configuration, boolean isCallerAJavaApplication) throws Exception {
		super(true);  // full start
		this.configuration = configuration;
		this.isCallerAJavaApplication = isCallerAJavaApplication;
		clientThreads = new ArrayList<ClientThread>();
		clientQueue = new LinkedBlockingQueue<SocketWrapper>();
		try {
			if (configuration.protocol == Protocol.TCP) {
				callReceiver = new CallReceiverThread(new ServerSocket(configuration.outerPort), clientQueue, configuration.maxSizeOfWaitingList);
			} else {
				callReceiver = new CallReceiverThread(null, clientQueue, configuration.maxSizeOfWaitingList);
			}
			for (int i = 0; i < configuration.numberOfClientThreads; i++) {
				clientThreads.add(createClientThread(this, i + 1));		// i + 1 serves as id
			}
			if (this.configuration.innerPort != null) {
				new ActivityMonitor(this, 300);			// 5 minutes between the checks
				interfaceThread = new InterfaceConnectionThread(this);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("Unable to initialize the server");
		}
		listeners = new CopyOnWriteArrayList<PropertyChangeListener>();
	}

	
	protected abstract ClientThread createClientThread(AbstractServer server, int id);
		
	private boolean isThereAtLeastOneThreadWaiting() {
		for (ClientThread t : clientThreads) {
			Map<String, PropertyChangeEvent> statusMap = t.getCurrentStatusMap();
			if (statusMap.isEmpty()) {
				return true;
			} else {
				if (statusMap.containsKey("status")) {
					String currentStatus = t.getCurrentStatusMap().get("status").getNewValue().toString();
					if (currentStatus.equals("Waiting")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	
	private List<EventListener> getAllListeners() {
		List<EventListener> listeners = new ArrayList<EventListener>();
		listeners.addAll(clientThreads);
		listeners.add(this);
		return listeners;
	}



	/**
	 * This method waits until the head of the queue is non null and returns the socket.
	 * @return a Socket instance
	 * @throws InterruptedException 
	 */
	protected synchronized SocketWrapper getWaitingClients() throws InterruptedException {
		SocketWrapper socket = clientQueue.take();
		return socket;
	}

	/**
	 * This method starts the worker thread, which listens to the clients in the queue.
	 */
	protected void listenToClients() {	
		for (ClientThread thread : clientThreads) {
			thread.start();
		}
	}

	/**
	 * This method starts the callReceiver thread that handles the call from the clients.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	protected void startReceiverThread() throws ExecutionException, InterruptedException {
		System.out.println("Server starting");
		callReceiver.start();
		listenToClients();
//		callReceiver.join();
//		System.out.println("Server shutting down");
	}


	protected ServerConfiguration getConfiguration() {
		return configuration;
	}


	/**
	 * This method returns the vector of client threads.
	 * @return a Vector of ClientThread instances
	 */
	protected List<ClientThread> getClientThreads() {return clientThreads;}


	protected void closeAndRestartTheseThreads(Collection<ClientThread> connectionsToBeClosed) {
		for (ClientThread thread : connectionsToBeClosed) {
			try {
				thread.getSocket().close();
				thread.restartAction();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	protected void firstTasksToDo() {
		addTask(new ServerTask(ServerTaskID.StartReceiverThread, this));
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}
	
	/*
	 * Just to extend the visibility
	 */
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		PropertyChangeEvent evt = new PropertyChangeEvent("Server event", propertyName, oldValue, newValue);
		for (PropertyChangeListener listener : listeners) {
			listener.propertyChange(evt);
		}
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		if (propertyName.equals("shutdownServer")) {
			if (interfaceThread != null && interfaceThread.eventConnector != null) {
				interfaceThread.eventConnector.close();
			}
			requestShutdown();
		} 
	}

	@Override
	public void requestShutdown() {
		callReceiver.shutdownCall = true;
		callReceiver.clientQueue.clear();
		try {
			if (callReceiver.serverSocket != null) {
				callReceiver.serverSocket.close();
			}
		} catch (IOException e) {
			callReceiver.interrupt();
		} finally {
			try {
				callReceiver.join(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.requestShutdown();
	}
	




}
