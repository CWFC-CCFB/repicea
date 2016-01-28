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

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import repicea.net.SocketWrapper;
import repicea.net.server.AbstractServer.ServerReply;

public class BasicClient implements Closeable {
	
	protected static enum ClientRequest {closeConnection}
	
//	public static final byte[] SIGNAL_END_OF_FILE = new byte[20];
//	static {
//		for (int i = 0; i < SIGNAL_END_OF_FILE.length; i++) {
//			SIGNAL_END_OF_FILE[i] = Byte.MAX_VALUE;
//		}
//	}
	
	private SocketWrapper socketWrapper;
	private boolean open;
	
	/**
	 * Constructor.
	 * @param socketAddress the SocketAddress instance that corresponds to the server
	 * @throws UnknownHostException if the host is unknown
	 * @throws IOException if the connection failed
	 * @throws ClassNotFoundException if the reply from the server is incorrect
	 * @throws ClientException if the connection has been lost
	 */
	protected BasicClient(SocketAddress socketAddress) throws UnknownHostException, IOException, ClassNotFoundException {
		Socket socket = new Socket();
		socket.connect(socketAddress, 5000);
		
		socketWrapper = new SocketWrapper(socket);
		
		ServerReply replyFromServer = (ServerReply) socketWrapper.readObject();
		if (replyFromServer == ServerReply.CallAccepted) {
			open = true;
		} else if (replyFromServer == ServerReply.IAmBusyCallBackLater) {
			open = false;
			close();
		}
		
	}

	
	protected Object processRequest(Object obj) {
		if (open) {
			try {
				socketWrapper.writeObject(obj);
				Object reply = socketWrapper.readObject();
				return reply;
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				try {
					close();
				} catch (IOException e1) {
					e1.printStackTrace();
				} 
			}
		}
		return null;
	}
	
	/**
	 * This method sends a request to the server to close the connection and closes the output and input streams.
	 */
	@Override
	public void close() throws IOException {
		socketWrapper.writeObject(ClientRequest.closeConnection);
		socketWrapper.close();
	}

	
}