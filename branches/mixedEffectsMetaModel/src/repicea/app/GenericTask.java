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
package repicea.app;

/**
 * GenericTask instances are used by the engine to perform any task.
 * @author Mathieu Fortin - September 2012
 */
public interface GenericTask extends Executable {
	
	/**
	 * This method returns the name of the task.
	 * @return a String object
	 */
	public String getName();
	
	/**
	 * This method is called to cancel a running task.
	 */
	public void cancel();

	/**
	 * This method returns true if the method cancel() has been called.
	 * @return a boolean
	 */
	public boolean isCancelled();
	
	/**
	 * This method returns true if the task displays messages.
	 * @return a boolean
	 */
	public boolean isVerbose();
}
