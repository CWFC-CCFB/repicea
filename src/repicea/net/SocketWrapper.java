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
package repicea.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * The SocketWrapper handles the output and input streams of socket. 
 * @author Mathieu Fortin - December 2011
 */
public class SocketWrapper implements Closeable {
	
	private Socket socket;
	
	private InputStream basicIn;
	private OutputStream basicOut;
	private ObjectInputStream objectIn;
	private ObjectOutputStream objectOut;
	
	
	/**
	 * General constructor.
	 * @param socket a Socket instance
	 */
	public SocketWrapper(Socket socket) {
		this.socket = socket;
	}
	
	public Socket getSocket() {return socket;}
	
	
	public Object readObject() throws IOException, ClassNotFoundException {
		return getObjectInputStream().readObject();
	}
	
	public void writeObject(Object obj) throws IOException {
		getObjectOutputStream().writeObject(obj);
	}
	
	public int readBytes(byte[] buffer) throws IOException {
		return getBasicInputStream().read(buffer);
	}
	
	public int read() throws IOException {
		return getBasicInputStream().read();
	}
	
	public void write(int b) throws IOException {
		getBasicOutputStream().write(b);
	}

	public void writeBytes(byte[] buffer, int off, int len) throws IOException {
		getBasicOutputStream().write(buffer, off, len);
	}
	
	public void writeBytes(byte[] buffer) throws IOException {
		getBasicOutputStream().write(buffer);
	}
	
	public void flush() throws IOException {
		getBasicOutputStream().flush();
	}
	
//	public boolean isConnected() {return socket.isConnected();}
	
	public boolean isClosed() {return socket.isClosed();}
	
	public void close() throws IOException {
		getObjectOutputStream().flush();
		socket.close();
	}
	
	public InputStream getBasicInputStream() throws IOException {
		if (basicIn == null) {
			basicIn = socket.getInputStream();
		}
		return basicIn;
	}
	
	private ObjectInputStream getObjectInputStream() throws IOException {
		if (objectIn == null) {
			objectIn = new ObjectInputStream(getBasicInputStream());
		}
		return objectIn;
	}
	
	public OutputStream getBasicOutputStream() throws IOException {
		if (basicOut == null) {
			basicOut = socket.getOutputStream();
		}
		return basicOut;
	}
	
	private ObjectOutputStream getObjectOutputStream() throws IOException {
		if (objectOut == null) {
			objectOut = new ObjectOutputStream(getBasicOutputStream());
		}
		return objectOut;
	}

	
}
