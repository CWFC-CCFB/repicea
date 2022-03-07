/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui;


public class REpiceaAWTProperty {

	public static final REpiceaAWTProperty JustSaved = new REpiceaAWTProperty("JustSaved");
	public static final REpiceaAWTProperty JustLoaded = new REpiceaAWTProperty("JustLoaded");
	public static final REpiceaAWTProperty WindowCancelRequested = new REpiceaAWTProperty("WindowCancelRequested");
	public static final REpiceaAWTProperty WindowCancelledConfirmed = new REpiceaAWTProperty("WindowCancelledConfirmed");
	public static final REpiceaAWTProperty UndoClicked = new REpiceaAWTProperty("UndoClicked");
	public static final REpiceaAWTProperty WindowOkRequested = new REpiceaAWTProperty("WindowOkRequested");
	public static final REpiceaAWTProperty WindowHelpRequested = new REpiceaAWTProperty("WindowHelpRequested");
	public static final REpiceaAWTProperty WindowAcceptedConfirmed = new REpiceaAWTProperty("WindowAcceptedConfirmed");
	public static final REpiceaAWTProperty SynchronizeWithOwner = new REpiceaAWTProperty("SynchronizeWithOwner");
	public static final REpiceaAWTProperty DisconnectAutoShutdown = new REpiceaAWTProperty("DisconnectAutoShutdown");

	public static final REpiceaAWTProperty ActionPerformed = new REpiceaAWTProperty("ActionPerformed");

	private final String propertyName;
	
	protected REpiceaAWTProperty(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public String name() {return propertyName;}

}
