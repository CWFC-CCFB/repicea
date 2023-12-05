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
package repicea.gui.genericwindows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import repicea.gui.REpiceaDialog;
import repicea.gui.REpiceaPanel;
import repicea.gui.UIControlManager;


/**
 * This dialog only contains a progress bar that listen to the SwingWorker progress.
 * @author Mathieu Fortin - April 2010
 */
@SuppressWarnings("serial")
public class REpiceaProgressBarDialog extends REpiceaDialog implements PropertyChangeListener, WindowListener {

	public static final String PROGRESS = "progress";
	public static final String LABEL = "label";
	
	
	private String titleString;
	private List<REpiceaProgressBarDialogParameters> parms;
	private int currentJobId = 0;


	static class ProgressBarPanel extends REpiceaPanel implements PropertyChangeListener {

		final JProgressBar progressBar;
		final JLabel label;
		private SwingWorker<?,?> jobToRun;
		
		ProgressBarPanel(REpiceaProgressBarDialogParameters parm) {
			this.jobToRun = parm.jobToRun;
			progressBar = new JProgressBar();
			progressBar.setMinimum(0);
			progressBar.setMaximum(100);
			progressBar.setStringPainted(!parm.isIndeterminate);
			progressBar.setIndeterminate(parm.isIndeterminate);
			
			label = UIControlManager.getLabel(parm.labelString);
			initUI();
		}

		void initUI() {
			setLayout(new BorderLayout());
			JPanel mainPanel = new JPanel();
			add(mainPanel, BorderLayout.NORTH);
			
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

			JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			labelPanel.add(label);

			JPanel progressBarPanel = new JPanel(new BorderLayout());
			progressBarPanel.add(progressBar, BorderLayout.CENTER);

			mainPanel.add(labelPanel);
			mainPanel.add(progressBarPanel);
			
		}

		@Override
		public void refreshInterface() {}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
	        if (evt.getPropertyName().equals(PROGRESS)) {
				progressBar.setValue((Integer) evt.getNewValue());
			} else if (evt.getPropertyName().equals(LABEL)) {
				label.setText(evt.getNewValue().toString());
			}
		}

		@Override
		public void listenTo() {
			jobToRun.addPropertyChangeListener(this);
		}

		@Override
		public void doNotListenToAnymore() {
			jobToRun.removePropertyChangeListener(this);
		}

	}

	
	/**
	 * Implement the parameters of the REpiceaProgressBarDialog class.
	 */
	public static class REpiceaProgressBarDialogParameters {
		final String labelString;
		final SwingWorker<?,?> jobToRun;
		final boolean isIndeterminate;
		
		/**
		 * Constructor.
		 * @param labelString the label to be displayed in the dialog
		 * @param jobToRun the SwingWorker instance to be executed
		 * @param isIndeterminate true to set the progress bar in the indeterminate mode
		 */
		public REpiceaProgressBarDialogParameters(String labelString, SwingWorker<?,?> jobToRun, boolean isIndeterminate) {
			if (labelString == null) {
				throw new InvalidParameterException("The labelString parameter cannot be null!");
			}
			if (jobToRun == null) {
				throw new InvalidParameterException("The jobToRun parameter cannot be null!");
			}
			this.labelString = labelString;
			this.jobToRun = jobToRun;
			this.isIndeterminate = isIndeterminate;
		}
	}
	
	
	
	
	
	
	/**
	 * Constructor 1 with a Frame instance as owner.
	 * @param owner the parent frame that calls the progress bar
	 * @param titleString the title of the progress bar
	 * @param labelString the message
	 * @param jobToRun the SwingWorker instance that contains the job to be executed
	 * @param setIndeterminate true to set the progress bar in indeterminate mode
	 * @deprecated Use the constructor with REpiceaProgressBarDialogParameters instead
	 */
	@Deprecated
	@SuppressWarnings("rawtypes")
	public REpiceaProgressBarDialog(Window owner, String titleString, String labelString, SwingWorker jobToRun, boolean setIndeterminate) {
		super(owner);
		List<REpiceaProgressBarDialogParameters> parms = new ArrayList<REpiceaProgressBarDialogParameters>();
		parms.add(new REpiceaProgressBarDialogParameters(labelString, jobToRun, setIndeterminate));
		init(titleString, parms);
	}


	/**
	 * Constructor 2 with no owner.
	 * @param titleString the title of the progress bar
	 * @param labelString the message
	 * @param jobToRun the SwingWorker instance that contains the job to be executed
	 * @param setIndeterminate set the progress bar to the indeterminate mode
	 * @deprecated Use the constructor with REpiceaProgressBarDialogParameters instead
	 */
	@Deprecated
	@SuppressWarnings("rawtypes")
	public REpiceaProgressBarDialog(String titleString, String labelString, SwingWorker jobToRun, boolean setIndeterminate) {
		this(null, titleString, labelString, jobToRun, setIndeterminate);
	}
	
	/**
	 * Generic constructor for multiple progress bars. The bar parameters are set through
	 * REpiceaProgressBarDialogParameters instances.
	 * @param owner the window (can be null)
	 * @param titleString the title of the dialog
	 * @param parms a List of REpiceaProgressBarDialogParameters instances
	 */
	public REpiceaProgressBarDialog(Window owner, String titleString, List<REpiceaProgressBarDialogParameters> parms) {
		super(owner);
		init(titleString, parms);
	}
	
	private void init(String titleString, List<REpiceaProgressBarDialogParameters> parms) {
		this.titleString = titleString;
		this.parms = parms;
		initUI();
		setModal(true);
		pack();
		setVisible(true);
	}
	
	@Override
	protected void initUI() {
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		for (REpiceaProgressBarDialogParameters parm : parms) {
			getContentPane().add(new ProgressBarPanel(parm));
		}
		setTitle(titleString);
		
		Dimension dim = new Dimension(250,100);
		setMinimumSize(dim);
		pack();
	}

	@Override
	public void cancelAction() {
		for (REpiceaProgressBarDialogParameters parm : parms) {
			parm.jobToRun.cancel(true);
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("state")) {
			if (SwingWorker.StateValue.DONE == evt.getNewValue()) {
				currentJobId++;
				if (currentJobId < parms.size()) {
					parms.get(currentJobId).jobToRun.execute();
				} else {
					setVisible(false);
					dispose();
				}
			} 
		}
	}

	@Override
	public void listenTo() {
		addWindowListener(this);
		for (REpiceaProgressBarDialogParameters parm : parms) {
			parm.jobToRun.addPropertyChangeListener(this);
		}
	}

	@Override
	public void doNotListenToAnymore() {
		removeWindowListener(this);
		for (REpiceaProgressBarDialogParameters parm : parms) {
			parm.jobToRun.removePropertyChangeListener(this);
		}
	}


	@Override
	public void windowActivated(WindowEvent arg0) {}


	@Override
	public void windowClosed(WindowEvent arg0) {}


	@Override
	public void windowClosing(WindowEvent arg0) {}


	@Override
	public void windowDeactivated(WindowEvent arg0) {}


	@Override
	public void windowDeiconified(WindowEvent arg0) {}


	@Override
	public void windowIconified(WindowEvent arg0) {}


	@Override
	public void windowOpened(WindowEvent arg0) {
		parms.get(currentJobId).jobToRun.execute();
	}


}
