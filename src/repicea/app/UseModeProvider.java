/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2018 Mathieu Fortin for Rouge-Epicea
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
package repicea.app;

/**
 * The UseModeProvider interface ensures a model can be run in different modes (GUI only, GUI assisted,
 * or Script). The interface allows to set the use mode and the instance can provide its use model.
 * @author Mathieu Fortin - December 2018
 *
 */
public interface UseModeProvider {
	
	public enum UseMode {
		/**
		 * Model is run from the user interface.
		 */
		GUI_MODE, 
		/*
		 * The model is run from user interface but nested in some other meta model. Only
		 * some warning messages are then displayed.
		 */
		ASSISTED_SCRIPT_MODE, 
		/*
		 * The model is run in script mode and the user interface is disabled.
		 */
		PURE_SCRIPT_MODE}


	/**
	 * This method returns the use mode of the model.
	 * @return a UseMode enum
	 */
	public UseMode getUseMode();
	
	/**
	 * This method makes it possible to set the use mode of the model.
	 * @param useMode a UseMode enum
	 */
	public void setUseMode(UseMode useMode);
	
	/**
	 * This method returns true if the use mode is set to GUI_MODE.
	 * @return a boolean
	 */
	default public boolean isGuiEnabled() {return getUseMode() == UseMode.GUI_MODE;}


}
