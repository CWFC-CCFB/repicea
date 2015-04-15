/*
 * This file is part of the repicea-simulation library.
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
package repicea.simulation.processsystem;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaDialog;
import repicea.gui.REpiceaPanel;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class ProcessorInternalDialog extends REpiceaDialog {// implements ActionListener {


	public static enum MessageID implements TextableEnum {
		ProcessorTitle("Processor features", "Caract\u00E9ristiques du processeur"),
		ProcessorName("Processor name", "Nom du processeur"),
		ProcessorIntake("Processor intake", "Intrant au processeur"),
		ProcessorYield("Processor yield", "Rendement du processeur"),
		SendToAnotherOutletLabel("Send to another outlet", "Envoyer vers un autre d\u00E9bouch\u00E9"),
		AvailableOutlets("Available outlets", "D\u00E9bouch\u00E9s disponibles");

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
	
	private final Processor caller;

	protected JTextField processorTextField;
	
//	private final JButton close;

	private JPanel bottomComponent;



	/**
	 * The constructor is called by its underlying class WoodProductProcessor.
	 * @param parent a Window instance
	 * @param callerButton a ProcessorButton instance
	 */
	protected ProcessorInternalDialog(Window parent, ProcessorButton callerButton) {
		super(parent);
		setCancelOnClose(false);
		caller = callerButton.getOwner();
//		close = UIControlManager.createCommonButton(CommonControlID.Close);
		
		processorTextField = new JTextField();
		processorTextField.setColumns(25);
		processorTextField.setText(getCaller().getName());
		processorTextField.setPreferredSize(new Dimension(100, processorTextField.getFontMetrics(processorTextField.getFont()).getHeight() + 2));

		initUI();
		pack();
//		close.setDefaultCapable(true);
//		getRootPane().setDefaultButton(close);
//		close.requestFocusInWindow();
	}

	protected Processor getCaller() {return caller;}

	@Override
	protected void initUI() {
		setTitle(MessageID.ProcessorTitle.toString());

		setLayout(new BorderLayout());
		
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		getContentPane().add(mainPanel, BorderLayout.WEST);
		
		JPanel upperPart = new JPanel();
		upperPart.setLayout(new BoxLayout(upperPart, BoxLayout.Y_AXIS));

		JPanel processorNameSubPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		processorNameSubPanel.add(new JLabel(REpiceaTranslator.getString(MessageID.ProcessorName)));
		processorNameSubPanel.add(Box.createHorizontalStrut(5));
		processorNameSubPanel.add(processorTextField);
		processorNameSubPanel.add(Box.createHorizontalStrut(5));

		Component verticalStrut = Box.createVerticalStrut(10);
		upperPart.add(verticalStrut);
		upperPart.add(processorNameSubPanel);
		upperPart.add(Box.createVerticalStrut(10));

		mainPanel.add(upperPart);
		
		bottomComponent = new JPanel();
		mainPanel.add(bottomComponent);
		
//		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//		controlPanel.add(close);
//		getContentPane().add(controlPanel, BorderLayout.SOUTH);
	}


	private void setBottomComponent(JComponent bottomPanel) {
		bottomComponent.removeAll();
		bottomComponent.add(bottomPanel);
		pack();
		validate();
		repaint();
	}


	@Override
	public void listenTo() {
		processorTextField.addCaretListener(getCaller());
//		close.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		processorTextField.removeCaretListener(getCaller());
//		close.removeActionListener(this);
	}

	@Override
	public void setVisible(boolean bool) {
		if (!isVisible() && bool) {
			REpiceaPanel featurePanel = caller.getProcessFeaturesPanel();
			if (featurePanel != null) {
				if (caller.hasSubProcessors()) {
					setBottomComponent(new JPanel());
				} else {
					SystemManagerDialog dlg = (SystemManagerDialog) CommonGuiUtility.getParentComponent(this, SystemManagerDialog.class);
					boolean isEnablingGranted = dlg.getCaller().getGUIPermission().isEnablingGranted();
					if (!isEnablingGranted) {
						CommonGuiUtility.enableThoseComponents(featurePanel, JTextComponent.class, isEnablingGranted);
						CommonGuiUtility.enableThoseComponents(featurePanel, AbstractButton.class, isEnablingGranted);
						CommonGuiUtility.enableThoseComponents(featurePanel, JComboBox.class, isEnablingGranted);
						CommonGuiUtility.enableThoseComponents(featurePanel, JSlider.class, isEnablingGranted);
					}
					setBottomComponent(featurePanel);
				}
			}
		}
		super.setVisible(bool);
	}

//	@Override
//	public void actionPerformed(ActionEvent arg0) {
//		if (arg0.getSource().equals(close)) {
//			okAction();
//		}
//		
//	}

}
