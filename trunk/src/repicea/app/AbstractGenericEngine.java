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

import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import repicea.gui.CommonGuiUtility;
import repicea.gui.UserInterfaceableObject;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The AbstractGenericEngine class implements all the methods to run an application. Some
 * GenericTask instances are stored in a queue, and processed in order by an internal SwingWorker.
 * @author Mathieu Fortin - July 2012
 */
public abstract class AbstractGenericEngine {


	private static enum MessageID implements TextableEnum {
		ErrorMessage("An error of this type occured while running task ", "Une erreur de ce type est survenu pendant l'ex\u00E9cution de la t\u00E2che ");
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}
	
	
	/**
	 * This fake task to ensure the InternalWorker is not waiting for a job before shutting down.
	 * The only job that is carried out while shutting down is the saving of the settings.
	 * @author Mathieu Fortin - December 2011
	 */
	@SuppressWarnings("serial")
	private final class EnsuringShutdownTask extends AbstractGenericTask {

		private EnsuringShutdownTask() {
			setName("FinalTask");
		}

		@Override
		protected void doThisJob() throws Exception {
			SettingMemory settings = getSettingMemory();
			if (settings != null) {
				settings.saveSettings();		
			}
			
			synchronized(lock) {
				goAhead = true;
				lock.notifyAll();
			}			
			
			if (worker.isCorrectlyTerminated()) {
				shutdown(0);
			} else {
				worker.getFailureReason().printStackTrace();
				shutdown(1);
			}
		}		
				
	}
	
	
	
	/**
	 * This static class handles the different UpdaterTask stored in the queue of the Engine.
	 * @author Mathieu Fortin - November 2011
	 */
	private static class InternalWorker extends Thread implements Executable {

		private AbstractGenericEngine engine;
		private GenericTask currentTask;
		private Exception failureReason;
		
		private InternalWorker(AbstractGenericEngine engine) {
			setName("Engine - Internal task processor");
			this.engine = engine;
			this.setDaemon(false);
		}
		
		@Override
		public void run() {
			try {
				LinkedBlockingQueue<GenericTask> queue = engine.queue;
				do {
					currentTask = queue.take();			

					System.out.println("Running task : " + currentTask.getName());

					currentTask.run();

					if (!currentTask.isCorrectlyTerminated() && !currentTask.isCancelled()) {
						Exception exc = currentTask.getFailureReason();
						exc.printStackTrace();
						engine.decideWhatToDo(currentTask.getName(), exc);
					} else {
						engine.tasksDone.add(currentTask.getName());
					}
				} while (!currentTask.equals(engine.finalTask));
			} catch (InterruptedException e) {
				failureReason = e;
				System.out.println("The Engine has been interrupted!");
				engine.finalTask.run();
			}
		}
		
		protected void requestCancel() {
			if (currentTask != null) {
				currentTask.cancel();
			}
		}
		
		@Override
		public boolean isCorrectlyTerminated() {
			return failureReason == null;
		}

		@Override
		public Exception getFailureReason() {
			return failureReason;
		}
	}
	
	protected Logger logger;
	protected LinkedBlockingQueue<GenericTask> queue;
	protected List<String> tasksDone;
	private InternalWorker worker;
	
	private boolean goAhead = true;
	private final Object lock = new Object();
	
	private SettingMemory settings;
	protected EnsuringShutdownTask finalTask = new EnsuringShutdownTask();

	
	/**
	 * Protected constructor for derived class.
	 */
	protected AbstractGenericEngine() {
		queue = new LinkedBlockingQueue<GenericTask>();
		tasksDone = new CopyOnWriteArrayList<String>();
		worker = new InternalWorker(this);
		worker.start();
	}

	/**
	 * This method is called whenever an exception is thrown while running a task. If 
	 * the Engine has a user interface and this interface is visible, an error message
	 * is displayed. The queue of tasks is cleared.
	 * @param taskName the name of the task that failed
	 * @param failureReason the exception that was thrown
	 */
	protected void decideWhatToDo(String taskName, Exception failureReason) {
		if (this instanceof UserInterfaceableObject) {
			UserInterfaceableObject guiObject = (UserInterfaceableObject) this;
			Component component = guiObject.getGuiInterface();
			Container container = null;
			if (component instanceof Container) {
				container = (Container) component;
			}
			if (container != null && container.isVisible()) {
				String errorType = failureReason.getClass().getSimpleName();
				String message = MessageID.ErrorMessage.toString() + taskName + " : " + errorType;
				CommonGuiUtility.showErrorMessage(message, container);
			}
		}
		queue.clear();
	}
	
	/**
	 * This method sets the first tasks to execute when the engine starts. Typically,
	 * it would retrieve the settings, show the user interface and so on.
	 */
	protected abstract void firstTasksToDo();

	/**
	 * This method sets the settings of the application. Should be stated in the constructor.
	 * @param settings a SettingMemory instance
	 */
	protected void setSettingMemory(SettingMemory settings) {this.settings = settings;}
	
	/**
	 * This method returns the settings of the application.
	 * @return a Settings derived instance
	 */
	public final SettingMemory getSettingMemory() {return settings;};
	
	
	/**
	 * This method starts the client application. The abstract method firstTasksToDo() serves to pile tasks in the queue as
	 * soon as the application starts.
	 */
	public final void startApplication() {
		firstTasksToDo();
	}

	
	protected void shutdown(int shutdownCode) {
		System.out.println("Shutting down application...");
		System.exit(shutdownCode);
	}

	
	/**
	 * This method add a GenericTask instance in the queue of tasks.
	 * @param task a GenericTask instance
	 */
	public void addTask(GenericTask task) {
		queue.add(task);
	}

	
	/**
	 * This method add a bunch of tasks in the queue of tasks.
	 * @param tasks a List of GenericTask instances
	 */
	public void addTasks(List<GenericTask> tasks) {
		queue.addAll(tasks);
	}
	
	
	/**
	 * This method locks the engine while the interface can be doing something else.
	 * @param millisec the number of milliseconds to wait
	 * @throws InterruptedException
	 */
	protected void lockEngine(long millisec) throws InterruptedException {
		synchronized(lock) {
			goAhead = false;
			while(!goAhead) {
				lock.wait(millisec);
			}
		}
	}

	
	/**
	 * This method locks the engine while the interface can be doing something else.
	 * @throws InterruptedException
	 */
	protected void lockEngine() throws InterruptedException {
		lockEngine(0);
	}
	
	
	/**
	 * This method unlock the engine if locked.
	 */
	protected void unlockEngine() {
		synchronized(lock) {
			goAhead = true;
			lock.notify();
		}
	}

	
	/**
	 * This method cancels the current task if the queue is not empty.
	 */
	public void cancelRunningTask() {
//		queue.clear();
		worker.requestCancel();
//		if (!queue.isEmpty()) {
//			worker.requestCancel();
//			setCancelRequest(true);
//		} 
	}

	/**
	 * This method requests the Engine to shut down. It first clears the queue of tasks and then it 
	 * sends the FinalTask static member in the queue in order to ensures the shutting down.
	 */
	public void requestShutdown() {
		queue.clear();
		addTask(finalTask);
	}

	
//	/**
//	 * This method sets the cancel request to false or true.
//	 * @param bool a boolean
//	 */
//	protected void setCancelRequest(boolean bool) {
//	}
	
}
