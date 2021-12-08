/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2020 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui.dnd;

import java.awt.Point;
import java.awt.dnd.DropTargetDropEvent;
import java.security.InvalidParameterException;
import java.util.EventObject;

/**
 * The LocatedEvent class is a simple class for event that includes a source and a location.
 * @author Mathieu Fortin - September 2020
 */
public class LocatedEvent extends EventObject {

	private final Point location;
	private final boolean absolute;

	/**
	 * Constructor for LocatedEvent object with absolute location. Typically, these objects are not
	 * directly created through the GUI, but rather automatically created through an action.
	 * @param source
	 * @param location
	 */
	public LocatedEvent(Object source, Point location) {
		this(source, location, true);
	}
	
	private LocatedEvent(Object source, Point location, boolean absolute) {
		super(source);
		if (location == null) {
			throw new InvalidParameterException("The location argument cannot be null!");
		}
		this.location = location;
		this.absolute = absolute;
	}

	/**
	 * Constructor derived from a DropTargetDropEvent. It retrieves the source and the location
	 * from that object.
	 * @param evt a DropTargetDropEvent instance
	 */
	public LocatedEvent(DropTargetDropEvent evt) {
		this(evt.getSource(), evt.getLocation(), false);
	}
	
	/**
	 * Provides the location of thee event
	 * @return a Point instance
	 */
	public Point getLocation() {
		return location;
	}
	
	public boolean isAbsolute() {return absolute;}
	
}
