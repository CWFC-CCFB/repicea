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
package repicea.gui;

import java.util.List;

import javax.swing.JMenuItem;

import repicea.app.GenericTask;
import repicea.app.GenericTaskFactory;

/**
 * The RepiceaMenuItem class extends the JMenuItem class. It also includes a list of
 * TaskGeneric instances that can be passed to any AbstractGenericEngine instance.
 * @author Mathieu Fortin - September 2012
 */
public class REpiceaMenuItem extends JMenuItem {

	private static final long serialVersionUID = 1L;

	private GenericTaskFactory taskMaker;
	
	/**
	 * Constructor with a single task to do and no name so far. 
	 * @param taskMaker a GenericTaskFactory instance
	 */
	protected REpiceaMenuItem(GenericTaskFactory taskMaker) {
		super();
		this.taskMaker = taskMaker;
	}

	/**
	 * Constructor with many tasks to do. 
	 * @param menuItemName the name of the control
	 * @param taskMaker a GenericTaskFactory instance
	 */
	public REpiceaMenuItem(String menuItemName, GenericTaskFactory taskMaker) {
		super(menuItemName);
		this.taskMaker = taskMaker;
	}


	/**
	 * This method returns the tasks to do associated with this menu item.
	 * @return a List of GenericTask instances
	 */
	public List<GenericTask> getTasksToDo() {
		return taskMaker.createTasks();
	}
	
}
