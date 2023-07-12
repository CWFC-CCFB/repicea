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
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import repicea.gui.REpiceaToolKit.REpiceaEventListener;
import repicea.gui.REpiceaToolKit.WindowTrackerListener;


/**
 * A class to perform tests on the GUI. <p>
 * 
 * IMPORTANT: The method shutdown must be called when the robot is no longer needed.
 * @author Mathieu Fortin - March 2022
 */
public class REpiceaGUITestRobot implements WindowTrackerListener, REpiceaEventListener {

	private static final int WAIT_TIME_BETWEEN_INSTRUCTIONS = 500;
	
	private final BlockingQueue<Window> windowQueue = new LinkedBlockingQueue<Window>();
	private final Vector<WeakReference<Window>> windows = new Vector<WeakReference<Window>>();

	private final BlockingQueue<REpiceaAWTEvent> repiceaEventQueue = new LinkedBlockingQueue<REpiceaAWTEvent>();

	public REpiceaGUITestRobot() {
		REpiceaToolKit.addWindowTrackerListener(this);
		REpiceaToolKit.addREpiceaEventListener(this);
	}

	public void shutdown() {
		REpiceaToolKit.removeWindowTrackerListener(this);
		REpiceaToolKit.removeREpiceaEventListener(this);
	}
	
	
	protected Window getLastVisibleWindow() {
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
	
	/**
	 * Find a component with a particular name in the last visible window.
	 * @param name the name of the component
	 * @return the component or null if no component bears this name
	 */
	public Component findComponentWithThisName(String name) { 
		return CommonGuiUtility.findComponentWithThisName(getLastVisibleWindow(), name);
	}
	
	/**
	 * Click on the button with this name. <p>
	 * If the property parameter is not null, then the robot waits for a particular property.
	 * @param buttonName the name of the button.
	 * @param property an REpiceaAWTProperty instance (can be null)
	 * @throws InterruptedException if the thread has been interrupted
	 * @throws InvocationTargetException if the target cannot be invoked
	 */
	public void clickThisButton(String buttonName, REpiceaAWTProperty property) throws InterruptedException, InvocationTargetException {
		repiceaEventQueue.clear();
		Object o = CommonGuiUtility.findComponentWithThisName(getLastVisibleWindow(), buttonName);
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
		if (property != null) {
			waitForThisProperty(property);
		} else {
			letDispatchThreadProcess();
		}
	}
	
	/**
	 * Click on the button with this name. <p>
	 * @param buttonName the name of the button.
	 * @throws InterruptedException if the thread has been interrupted
	 * @throws InvocationTargetException if the target cannot be invoked
	 */
	public void clickThisButton(String buttonName) throws InterruptedException, InvocationTargetException {
		clickThisButton(buttonName, (REpiceaAWTProperty) null);
	}

	/**
	 * Click on the button with this name and wait for a particular window type to show up. <p>
	 * @param buttonName the name of the button.
	 * @param windowToExpect a Window instance that is expected once the button is clicked
	 * @throws InterruptedException if the thread has been interrupted
	 * @throws InvocationTargetException if the target cannot be invoked
	 */
	public void clickThisButton(String buttonName,  Class<? extends Window> windowToExpect) throws InterruptedException, InvocationTargetException {
		windowQueue.clear();
		Object o = CommonGuiUtility.findComponentWithThisName(getLastVisibleWindow(), buttonName);
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
		if (windowToExpect != null) {
			while (!windowToExpect.isAssignableFrom(windowQueue.take().getClass())) {}
		} else {
			letDispatchThreadProcess();
		}
	}

	private void waitForThisProperty(REpiceaAWTProperty property) throws InterruptedException {
		while(!property.equals(repiceaEventQueue.take().property)) {}
	}

	/**
	 * Fill a particular text field. 
	 *
	 * @param textFieldName the name of the text field
	 * @param fillingString the text to put in this field
	 * @throws InterruptedException if the thread has been interrupted
	 * @throws InvocationTargetException if the setText method in the JTextComponent fails
	 */
	public void fillThisTextField(String textFieldName, String fillingString) throws InterruptedException, InvocationTargetException {
		repiceaEventQueue.clear();
		Object o = CommonGuiUtility.findComponentWithThisName(getLastVisibleWindow(), textFieldName);
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

	/**
	 * Start a dialog or a frame in a different thread to let the robot work in the current thread.
	 * @param toRun a Runnable that starts the dialog or the frame
	 * @param classToExpect the type of Window instance that is expected
	 * @return a Thread instance
	 * @throws InterruptedException if the thread is interrupted
	 */
	public Thread startGUI(Runnable toRun, Class<? extends Window> classToExpect) throws InterruptedException {
		windowQueue.clear();
		Thread t = new Thread(toRun, "REpiceaGUIRobot");
		t.start();
		while (!classToExpect.isAssignableFrom(windowQueue.take().getClass())) {}
		return t;
	}
	
	/**
	 * Show a window in a different thread to let the robot work in the current thread
	 * @param showable a REpiceaShowableUIWithParent instance (this instance must return a Window type instance when the getUI method is called)
	 * @return a Thread instance
	 * @throws InterruptedException if the Thread is interrupted
	 */
	public Thread showWindow(REpiceaShowableUIWithParent showable) throws InterruptedException {
		windowQueue.clear();
		Component w = showable.getUI(null); 
		if (!(w instanceof Window)) {
			throw new InvalidParameterException("The showWindow method only works with REpiceaShowableUIWithParent instances that provide a Window as UI.");
		}
		Runnable doRun = new Runnable() {
			public void run() {
				showable.showUI(null);
			}
		};
		Thread t = new Thread(doRun, "REpiceaGUIRobot");
		t.start();
		while (!w.getClass().isAssignableFrom(windowQueue.take().getClass())) {}
		return t;
	}

	
	public static void letDispatchThreadProcess() throws InterruptedException {
		Thread.sleep(WAIT_TIME_BETWEEN_INSTRUCTIONS);
	}
	
	@Override
	public void receiveThisWindow(Window retrievedWindow) {
		windows.add(new WeakReference<Window>(retrievedWindow));
		windowQueue.add(retrievedWindow);
	}

	@Override
	public void receiveREpiceaEvent(REpiceaAWTEvent e) {
		repiceaEventQueue.add(e);
	}
	
}
