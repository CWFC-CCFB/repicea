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
package repicea.serial.cloner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;

import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;

/**
 * The TemporaryMemorizer class is useful to memorize objects for a short period of time. It can be used in the GUI to retain the initial configuration that is to be retrieve if
 * the user reinitialize the dialog.
 * @author Mathieu Fortin - February 2012
 */
public class BasicSerialCloner implements Serializable, SerialCloner<MemorizerPackage> {

	private static final long serialVersionUID = 20120211L;

	/**
	 * Nested class that deserialize the object.
	 * @author Mathieu Fortin - October 2010
	 */
	private static class Deserializer extends Thread {
		
		protected static final MemorizerPackage ERROR = new MemorizerPackage();
		
		private PipedInputStream pipedInputStream;
		private MemorizerPackage obj;
		private Object lock;
		
		protected Deserializer(PipedInputStream pis) {
			setName("TemporaryMemorizer thread");
			lock = new Object();
			this.pipedInputStream = pis;
			start();
		}
		
		@Override
		public void run() {
			MemorizerPackage o = null;
			try {
				ObjectInputStream ois = new ObjectInputStream(pipedInputStream);
				o = (MemorizerPackage) ois.readObject();
			} catch(IOException e) {
				e.printStackTrace();
			} catch(ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			}

			synchronized(lock) {
				if (o == null) {
					obj = ERROR;
				} else {
					obj = o;
				}
				lock.notify();
			}
		}
		
		protected MemorizerPackage getDeserializedObject() {
			try {
				synchronized(lock) {
					while (obj == null) {
						lock.wait(5000);		// waits 5 sec.
					}
				}
			} catch(InterruptedException ie) {}
			
			if (obj.equals(ERROR)) {
				return null;
			} else {
				return obj;
			}
		}		
	}
	
	
	private ObjectOutputStream oos;
	private Deserializer deserializer;
	private PipedInputStream pis;
	
	/**
	 * Constructor for this class.
	 */
	public BasicSerialCloner() {}
	
	/**
	 * This method memorizes a Serializable object.
	 * @param obj the Object instance to be serialized.
	 * @return true if the object has been memorized or false otherwise
	 */
	public boolean memorize(MemorizerPackage obj) {
		PipedOutputStream os = new PipedOutputStream();
		try {
			pis = new PipedInputStream(os);
			oos = new ObjectOutputStream(os);
			deserializer = new Deserializer(pis);
			oos.writeObject(obj);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	
	/**
	 * This method memorize a part of the Memorizable instance that is specified in the method getMemoryPackage().
	 * @param obj a Memorizable object
	 * @return true if the object has been memorized or false otherwise
	 */
	public boolean memorize(Memorizable obj) {
		return memorize(obj.getMemorizerPackage());
	}
	
	
	/**
	 * This method returns the serialized object. The method waits approximately 5 sec and returns even if the object has not been retrieved. This protection is 
	 * to avoid deadlock (if this method is called and no object has been serialized. 
	 * @return the Object instance that has been serialized or null if an error occurred..
	 */
	public MemorizerPackage retrieve() {
		if (deserializer != null) {
			return deserializer.getDeserializedObject();
		} else {
			return null;
		}
	}

	@Override
	public MemorizerPackage cloneThisObject(MemorizerPackage obj) {
		memorize(obj);
		return retrieve();
	}


}
