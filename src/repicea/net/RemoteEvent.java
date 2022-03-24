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
package repicea.net;

import java.io.Serializable;
import java.util.EventObject;

/**
 * The RemoteEvent class is a wrapper for an EventObject instance. It also contains the hashcode of 
 * the local listener.
 * @author Mathieu Fortin - December 2011
 */
@Deprecated
public class RemoteEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int targetHashCode;
	private EventObject event;
	
	/**
	 * The constructor requires the hashcode of the target (local) listener and an EventObject instance.
	 * @param targetHashCode
	 * @param event
	 */
	public RemoteEvent(int targetHashCode, EventObject event) {
		this.targetHashCode = targetHashCode;
		this.event = event;
	}
	
	/**
	 * This method returns the hashcode of the target (local) class.
	 * @return an Integer instance
	 */
	public int getTargetHashCode() {return targetHashCode;}
	
	/**
	 * This method returns the embedded EventObject instance.
	 * @return an EventObject instance
	 */
	public EventObject getEvent() {return event;}

}
