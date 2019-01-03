/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2018 Mathieu Fortin for Rouge Epicea.
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

import java.io.IOException;

import repicea.lang.codetranslator.REpiceaCodeTranslator;
import repicea.net.server.BasicClient.ClientRequest;

/**
 * The JavaLocalGatewayServer class is a one-to-one local server that makes it possible 
 * to create objects and to execute methods in Java from a non-Java application.
 * @author Mathieu Fortin - December 2018
 */
public class JavaLocalGatewayServer extends AbstractServer {
	
	private class JavaGatewayClientThread extends ClientThread {

		protected JavaGatewayClientThread(AbstractServer caller, int workerID) {
			super(caller, workerID);
		}

		@Override
		public void run() {
			try {
				socketWrapper = caller.getWaitingClients();
				
				while (!socketWrapper.isClosed()) {
					try {
						Object somethingInParticular = processRequest();
						if (somethingInParticular != null) {
							if (somethingInParticular.equals(BasicClient.ClientRequest.closeConnection)) {
								socketWrapper.writeObject(ServerReply.ClosingConnection);
								closeSocket();
								caller.requestShutdown();
								break;
							} else {
								socketWrapper.writeObject(somethingInParticular);
							}
						} else {
							socketWrapper.writeObject(ServerReply.RequestReceivedAndProcessed);
						}
					} catch (Exception e) {		// something wrong happened during the processing of the request
						try {
							e.printStackTrace();
							if (e instanceof IOException) {	// seems that the connection was lost
								closeSocket();
							} else if (!socketWrapper.isClosed()) {
								socketWrapper.writeObject(e);
							}
						} catch (IOException e1) {}
					}
				}
				caller.requestShutdown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected Object processRequest() throws Exception {
			Object crudeRequest = getSocket().readObject();
			if (crudeRequest instanceof ClientRequest) {
				return crudeRequest;
			} else if (crudeRequest instanceof String) {
				String request = (String) crudeRequest;
				return JavaLocalGatewayServer.this.translator.processCode(request);
			}
			return null;
		}
	}

	protected final REpiceaCodeTranslator translator;	

	/**
	 * Constructor.
	 * @param outerPort a port for communication between the local server and the other application.
	 * @param translator an instance that implements the REpiceaCodeTranslator interface
	 * @throws Exception
	 */
	public JavaLocalGatewayServer(int outerPort, REpiceaCodeTranslator translator) throws Exception {
		super(new ServerConfiguration(1, outerPort, null), false); // false: the client is not a Java application
		this.translator = translator;
	}

	@Override
	protected ClientThread createClientThread(AbstractServer server, int id) {
		return new JavaGatewayClientThread(server, id);
	}

}
