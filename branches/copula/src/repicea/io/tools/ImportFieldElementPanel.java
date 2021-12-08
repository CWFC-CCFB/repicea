/*
 * This file is part of the repicea-iotools library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
package repicea.io.tools;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;

import repicea.gui.REpiceaPanel;
import repicea.gui.UIControlManager;
import repicea.gui.dnd.AcceptableDropComponent;
import repicea.gui.dnd.DropTargetImpl;
import repicea.gui.dnd.LocatedEvent;
import repicea.io.FormatField;
import repicea.io.tools.ImportFieldManagerDialog.MessageID;
import repicea.util.REpiceaTranslator;

/**
 * This private class is the Gui interface of the ImportFieldElement class.
 * @author Mathieu Fortin - October 2011
 */
@SuppressWarnings("serial")
class ImportFieldElementPanel extends REpiceaPanel { 
	
	protected class DnDJTextField extends JTextArea implements AcceptableDropComponent<FormatField> {

		protected DnDJTextField(Font font) {
			super(1,20);
			new DropTargetImpl<FormatField>(this, FormatField.class);
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			
		}
		
		@Override
		public void acceptThisObject(FormatField field, LocatedEvent evt) {
			caller.setFieldMatch(field);
			refreshInterface();
		}
	}
	

	private ImportFieldElement caller;
	private JTextComponent fieldLabel;
	protected JLabel descriptionLabel;
	
	protected ImportFieldElementPanel(ImportFieldElement caller) {
		super();
		this.caller = caller;
		createUI();
	}
	
	protected ImportFieldElement getCaller() {return caller;}

	private void createUI() {
//		setLayout(new FlowLayout(FlowLayout.TRAILING));
		setLayout(new GridBagLayout());
		if (caller.isOptional) {
			descriptionLabel = UIControlManager.getLabel(caller.description 
					+ " " 
					+ REpiceaTranslator.getString(MessageID.OptionalField)
					+ " :", 1);
		} else {
			descriptionLabel = UIControlManager.getLabel(caller.description  + " :", 1);
		}

//		descriptionLabel.setAlignmentX(RIGHT_ALIGNMENT);
		JPanel pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pane.add(descriptionLabel);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 2;
		add(pane, c);
		
		fieldLabel = new DnDJTextField(descriptionLabel.getFont());
		fieldLabel.setEditable(false);
		fieldLabel.setBackground(Color.WHITE);
		refreshInterface();

		pane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pane.add(fieldLabel);
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.weightx = 1;

		add(pane, c);
	}


	@Override
	public void refreshInterface() {
		fieldLabel.setText(caller.getFieldName());
//		Font font = fieldLabel.getFont();
//		if (caller.getMatchingFieldIndex() == -1) {
//			fieldLabel.setFont(font.deriveFont(Font.PLAIN));
//		} else {
////			fieldLabel.setFont(font.deriveFont(Font.BOLD));
//		}
	}

	@Override
	public void listenTo() {}

	@Override
	public void doNotListenToAnymore() {}
	

}
