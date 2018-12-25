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

import java.net.SocketException;

import repicea.net.SocketWrapper;
import repicea.net.server.AbstractServer;
import repicea.net.server.BasicClient.ClientRequest;
import repicea.net.server.ClientThread;
import repicea.net.server.ServerConfiguration;

// TODO try to work with streams instead of socket. A communication sender already exists in the class 
// TODO AbstractIndependentProcess. Create a receiver that would replace the socketWrapper in the abstract server class

class RGatewayServer extends AbstractServer {

	public final RCodeTranslator translator = new RCodeTranslator();
	
	
	private class RClientThread extends ClientThread {

		protected RClientThread(AbstractServer caller, int workerID) {
			super(caller, workerID);
		}

		@Override
		protected Object processRequest() throws Exception {
			Object crudeRequest = getSocket().readObject();
			if (crudeRequest instanceof ClientRequest) {
				return crudeRequest;
			} else if (crudeRequest instanceof String) {
				String request = (String) crudeRequest;
				return RGatewayServer.this.translator.processCode(request);
			}
			return null;
		}
	}
	
	@Override
	protected SocketWrapper getWaitingClients() throws InterruptedException {
		SocketWrapper socketWrapper = super.getWaitingClients();
		try {
			socketWrapper.getSocket().setTcpNoDelay(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return socketWrapper;
	}
	
	protected RGatewayServer(ServerConfiguration configuration) throws Exception {
		super(configuration, Mode.LocalServerMode, false);	// the caller is not a Java application
	}

	@Override
	protected ClientThread createClientThread(AbstractServer server, int id) {
		return new RClientThread(server, id);
	}

	public static void main(String[] args) throws Exception {
		ServerConfiguration conf = new ServerConfiguration(1, 18011, 18013);
		RGatewayServer server = new RGatewayServer(conf);
		server.startApplication();
	}
}
