/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge Epicea.
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import repicea.gui.REpiceaToolKit.WindowTrackerListener;


/**
 * A class to perform tests on the GUI. <br>
 * <br>
 * IMPORTANT: The method shutdown must be called when the robot is no longer needed.
 * @author Mathieu Fortin - March 2022
 */
public class REpiceaGUITestRobot implements WindowTrackerListener {

	private static final int WAIT_TIME_BETWEEN_INSTRUCTIONS = 1000;
	
	private final BlockingQueue<Window> q = new LinkedBlockingQueue<Window>();
	private final Vector<WeakReference<Window>> windows = new Vector<WeakReference<Window>>();
	
	public REpiceaGUITestRobot() {
		REpiceaToolKit.addWindowTrackerListener(this);
	}

	public void shutdown() {
		REpiceaToolKit.removeWindowTrackerListener(this);
	}
	
	/**
	 * Find a component corresponding to this name within the component. <br>
	 * <br> 
	 * The method scans recursively the component if it is an instance of the Container class.
	 * @param comp a Component instance
	 * @param name the name of the Component instance we are looking for
	 * @return the Component.
	 */
	private static Component findComponentWithThisName(Component comp, String name) {
		if (comp == null)
			return null;
		if  (name == null)
			throw new InvalidParameterException("The name must be non null!");

		if (name.equals(comp.getName())) {
			return comp;
		} else if (comp instanceof JMenu) {
			JMenu m = (JMenu) comp;
			for (int i = 0; i < m.getItemCount(); i ++) {
				Component resultingC = findComponentWithThisName(m.getItem(i), name);
				if (resultingC != null) {
					return resultingC;
				}
			}
		} else if (comp instanceof Container) {
			for (Component c : ((Container) comp).getComponents()) {
				Component resultingC = findComponentWithThisName(c, name);
				if (resultingC != null) {
					return resultingC;
				}
			}
		}
		return null;
	}
	
	
	private Window getLastVisibleWindow() {
		List<WeakReference<Window>> emptyReferences = new ArrayList<WeakReference<Window>>();
		for (int i = windows.size() - 1; i >= 0; i--) {
			WeakReference<Window> ref = windows.get(i);
			if (ref.get() == null)
				emptyReferences.add(ref);
			else if (ref.get().isVisible()) {
				windows.removeAll(emptyReferences);
				return ref.get();
			}
		}
		return null;
	}
	
	
	public Component findComponentWithThisName(String name) { 
		return findComponentWithThisName(getLastVisibleWindow(), name);
	}
	
	public void clickThisButton(String buttonName) throws InterruptedException, InvocationTargetException {
		Object o = findComponentWithThisName(getLastVisibleWindow(), buttonName);
		if (o == null) 
			throw new InvalidParameterException("Unable to find the component with name: " + buttonName);
		if (!(o instanceof AbstractButton)) 
			throw new InvalidParameterException("The component named " + buttonName + " is not a button!");
		AbstractButton b = (AbstractButton) o;	
		SwingUtilities.invokeAndWait(new Runnable() { 
			public void run() {
				b.doClick();
			}
		});
		letDispatchThreadProcess();
	}

	public void fillThisTextField(String textFieldName, String fillingString) throws InterruptedException, InvocationTargetException {
		Object o = findComponentWithThisName(getLastVisibleWindow(), textFieldName);
		if (o == null) 
			throw new InvalidParameterException("Unable to find the component with name: " + textFieldName);
		if (!(o instanceof JTextComponent)) 
			throw new InvalidParameterException("The component named " + textFieldName + " is not a JTextCompoenent!");
		JTextComponent b = (JTextComponent) o;	
		SwingUtilities.invokeAndWait(new Runnable() { 
			public void run() {
				b.setText(fillingString);
			}
		});
		letDispatchThreadProcess();
	}

	
	public Thread startGUI(Runnable toRun, Class<? extends Window> classToExpect) throws InterruptedException {
		Thread t = new Thread(toRun, "REpiceaGUIRobot");
		t.start();
		while (!classToExpect.isAssignableFrom(q.take().getClass())) {}
		return t;
	}
	
	public static void letDispatchThreadProcess() throws InterruptedException {
		Thread.sleep(WAIT_TIME_BETWEEN_INSTRUCTIONS);
	}
	
	@Override
	public void receiveThisWindow(Window retrievedWindow) {
		windows.add(new WeakReference<Window>(retrievedWindow));
		q.add(retrievedWindow);
	}
	
}
