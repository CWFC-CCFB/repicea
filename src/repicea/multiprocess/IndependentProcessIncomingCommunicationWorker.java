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
package repicea.multiprocess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;

/**
 * This static private class handles the object coming from an outer process through the standard input stream.
 * One way to close this stream if it exists is to call the method 
 * @author Mathieu Fortin - November 2011
 */
public abstract class IndependentProcessIncomingCommunicationWorker extends Thread {

	private ObjectInputStream ois;
	protected Vector<Object> receivedObjects;

	protected IndependentProcessIncomingCommunicationWorker() {
		receivedObjects = new Vector<Object>();
		setName("JVM message receiver");
		setDaemon(true);
		start();
	}


	@Override
	public void run() {
		Object received = null;
		do {
			try {
				ois = new ObjectInputStream(System.in);
				received = ois.readObject();
				receivedObjects.add(received);
				whatShouldIDoWithThisObject(received);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} while (conditionToListenIsMet());
	}

	/**
	 * This method should return true if the thread has to keep listening to the input standard stream for some
	 * message. 
	 * @return a boolean
	 */
	protected abstract boolean conditionToListenIsMet();
	

	/**
	 * This method handles the received object.
	 * @param obj the incoming Object instance
	 */
	protected abstract void whatShouldIDoWithThisObject(Object obj);
	
}

