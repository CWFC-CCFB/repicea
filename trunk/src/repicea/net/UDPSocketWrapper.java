/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2019 Mathieu Fortin for Rouge Epicea.
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * The UDPSocketWrapper class handles the output and input of a UDP socket. 
 * @author Mathieu Fortin - January 2019 (still under development)
 */
public class UDPSocketWrapper implements SocketWrapper {

	private final boolean isJavaObjectExpected;
	private final DatagramSocket socket;
	private final byte[] buffer = new byte[10000];
	private InetSocketAddress sendToAddress;
	private InetSocketAddress receiveFromAddress;

	/**
	 * Constructor for client.
	 * @param address an InetSocketAddress
	 * @param isJavaObjectExpected true if the caller is a Java application
	 * @throws Exception
	 */
	public UDPSocketWrapper(InetSocketAddress address, boolean isJavaObjectExpected) throws Exception {
		this.sendToAddress = address;
		this.isJavaObjectExpected = isJavaObjectExpected;
		this.socket = new DatagramSocket(50000);	// TODO draw a random address here
		receiveFromAddress = new InetSocketAddress(InetAddress.getLocalHost(), 50000);
		writeObject(receiveFromAddress.toString());
	}

	/**
	 * Constructor for the server socket.
	 * @param port the port to listen to.
	 * @param isJavaObjectExpected true if the caller is a Java application.
	 * @throws SocketException
	 */
	public UDPSocketWrapper(int port, boolean isJavaObjectExpected) throws Exception {
		receiveFromAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
		this.socket = new DatagramSocket(port);
		this.isJavaObjectExpected = isJavaObjectExpected;
		String str = readObject().toString();
		InetAddress sendToAddress = InetAddress.getByName(str.substring(str.indexOf("/") + 1, str.indexOf(":")));
		int sendToPort = Integer.parseInt(str.substring(str.indexOf(":") + 1));
		this.sendToAddress = new InetSocketAddress(sendToAddress, sendToPort);
	}
	
	@Override
	public void close() throws IOException {
		socket.close();
	}

	@Override
	public boolean isClosed() {
		return socket.isClosed();
	}

	@Override
	public Object readObject() throws Exception {
		return readObject(0);
	}

	@Override
	public Object readObject(int numberOfSeconds) throws Exception {
		socket.setSoTimeout(numberOfSeconds * 1000);
		DatagramPacket dp = new DatagramPacket(buffer, 1256);
		socket.receive(dp);
		int length = dp.getLength();
		Object result = null;
		if (UDPSocketWrapper.this.isJavaObjectExpected) {
			// TODO try to implement the objectstream here
		} else {
			String str = new String(dp.getData(), 0, length);
			result = str;
		}
		return result;

	}

	@Override
	public void writeObject(Object obj) throws IOException {
		byte[] buf = obj.toString().getBytes();
		DatagramPacket dp = new DatagramPacket(buf, buf.length, sendToAddress.getAddress(), sendToAddress.getPort());
		socket.send(dp);
	}


	@Override
	public InetAddress getInetAddress() {
		return sendToAddress.getAddress();
	}

}
