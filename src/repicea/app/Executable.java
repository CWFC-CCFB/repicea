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

public interface Executable extends Runnable {

	/**
	 * This method returns true if the task is correctly terminated or false otherwise.
	 * @return a Boolean instance
	 */
	public boolean isCorrectlyTerminated();
	
	/**
	 * This method returns the exception that has caused the task to terminate incorrectly.
	 * @return an Exception
	 */
	public Exception getFailureReason();

}
