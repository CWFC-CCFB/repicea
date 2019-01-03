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
import java.net.SocketException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import repicea.app.AbstractGenericTask;

/**
 * The SocketWrapper handles the output and input streams of socket. 
 * @author Mathieu Fortin - December 2011
 */
public class SocketWrapper implements Closeable {
	
	@SuppressWarnings("serial")
	private class SocketWrapperTask extends AbstractGenericTask {

		Object result;
		
		@Override
		protected void doThisJob() throws Exception {
			if (SocketWrapper.this.isJavaObjectExpected) {
				result = SocketWrapper.this.getObjectInputStream().readObject();
			} else {
				result = SocketWrapper.this.readString();
			}
		}
		
	}
	
	private final byte[] buffer = new byte[100000];
	private final boolean isJavaObjectExpected;
	
	private Socket socket;
	
	private InputStream basicIn;
	private OutputStream basicOut;
	private ObjectInputStream objectIn;
	private ObjectOutputStream objectOut;
	
	
	/**
	 * General constructor. If the caller is calling from a Java application,
	 * then the isJavaObjectExpected boolean should be set to true. Otherwise,
	 * the incoming stream is converted to String by default.
	 * @param socket a Socket instance
	 * @param isJavaObjectExpected a boolean.
	 * @throws SocketException 
	 */
	public SocketWrapper(Socket socket, boolean isJavaObjectExpected) {
		this.socket = socket;
		this.isJavaObjectExpected = isJavaObjectExpected;
	}

//	/**
//	 * Simple constructor for Java calling applications.
//	 * @param socket
//	 * @throws SocketException 
//	 */
//	public SocketWrapper(Socket socket) {
//		this(socket, true);
//	}
	
	public Socket getSocket() {return socket;}

	/**
	 * This method reads the object from the server. There is no timeout.
	 * @return the object
	 * @throws Exception
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public Object readObject() throws Exception, InterruptedException, ExecutionException, TimeoutException {
		return readObject(0);
	}
		
	
	/**
	 * This method reads the object from the server. There is a timeout after a given number of
	 * seconds. If the number of sectonds is equal to or smaller than 0, then there is no timeout.
	 * @param  numberOfSeconds number of seconds to wait before throwing a TimeoutException
	 * @return the object
	 * @throws Exception if the thread has not correctly terminated
	 * @throws InterruptedException if the thread was interrupted
	 * @throws ExecutionException if the execution failed
	 * @throws TimeoutException if the client has been wainting for more than 10 sec.
	 */
	public Object readObject(int numberOfSeconds) throws Exception, InterruptedException, ExecutionException, TimeoutException {
		SocketWrapperTask t = new SocketWrapperTask();
		t.execute();
		if (numberOfSeconds > 0) {
			t.get(numberOfSeconds, TimeUnit.SECONDS);
		} else {
			t.get();
		}
		if (t.isCorrectlyTerminated()) {
			return t.result;
		} else {
			throw t.getFailureReason();
		}
	}
	
	public void writeObject(Object obj) throws IOException {
		if (isJavaObjectExpected) {
			getObjectOutputStream().writeObject(obj);
		} else {
			writeString(obj.toString());
		}
	}
	
	public int readBytes(byte[] buffer) throws IOException {
		return getBasicInputStream().read(buffer);
	}

	/**
	 * This method returns a String from the incoming bytes. The buffer is 100000 byte long.
	 * @return a String instance
	 * @throws IOException
	 */
	public String readString() throws IOException {
		int nbBytes = readBytes(buffer);
		if (nbBytes == -1) {
			close();
			throw new IOException("Seems that the connection has been shutdown by the client...");
		}
		String incomingMessage = new String(buffer).substring(0, nbBytes);
		return incomingMessage;
	}

	/**
	 * This method sends a String on the socket connection.
	 * @param str a String instance
	 * @throws IOException
	 */
	public void writeString(String str) throws IOException {
		writeBytes(str.getBytes());
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
