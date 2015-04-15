/*
 * This file is part of the repicea-iotools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package repicea.io;

import repicea.app.SettingMemory;
import repicea.gui.REpiceaWindow;


/**
 * The IOUserInterfaceableObject should be implemented by any REpiceaFrame or REpiceaDialog instance that offers the save/saveas/load options.
 * @author Mathieu Fortin - April 2014
 */
public interface IOUserInterface extends REpiceaWindow {

	
	/**
	 * This method performs a particular action immediately after the parameters have been loaded.
	 */
	public void postLoadingAction();
	
	/**
	 * This method performs a particular action immediately after the parameters have been saved.
	 */
	public void postSavingAction();
	
	/**
	 * This method returns the SettingMemory instance of the object if any. It can be null.
	 * @return a Settings instance
	 */
	public SettingMemory getSettingMemory();

	
	
}
