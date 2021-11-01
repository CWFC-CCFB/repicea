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

import java.util.List;

/**
 * The GenericTaskFactory interface ensures a particular instance can create its own task. The
 * RepiceaMenuItem class uses some instances of this interface.
 * @author Mathieu Fortin - September 2012
 */
public interface GenericTaskFactory {
	
	/**
	 * This method returns a list of GenericTask instances.
	 * @return a List of GenericTask instances
	 */
	public List<GenericTask> createTasks();

}
