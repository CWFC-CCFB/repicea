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

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Future;

public interface IndependentProcess extends Serializable, Runnable, Future<Integer> {
	
	public enum StateValue {DONE, PENDING, STARTED}
	
	/**
	 * This method add a listener to the instance of this class if it is not already included in the listeners list. The listeners are communicated 
	 * the changes of the state member as well as the message received from the process itself.
	 * @param listener a PropertyChangeListener instance
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	/**
	 * This method removes all the instances of the listener among the listeners of this class.
	 * @param listener a PropertyChangeListener instance
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener);

	/**
	 * This method starts the independent process. Can be called only once.
	 */
	public void execute();

	/**
	 * This method returns the name of the process. 
	 * @return a String
	 */
	public String getName();
	
	/**
	 * This method sets the name of this process.
	 * @param name a String
	 */
	public void setName(String name);
	
	/**
	 * This method makes it possible to send a Serializable object to the independent process, typically a callback process.
	 * @param obj a Serializable object
	 */
	public void sendObjectToProcess(Serializable obj) throws IOException;

	
}
