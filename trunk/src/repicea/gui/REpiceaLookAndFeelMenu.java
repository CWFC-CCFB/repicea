/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2019 Mathieu Fortin for Rouge Epicea.
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class REpiceaLookAndFeelMenu  extends JMenu implements ItemListener {
	public static enum MessageID implements TextableEnum {
		LookAndFeel("Skin", "Pr\u00E9sentation");

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

	static class SkinRadioButtonMenuItem extends JRadioButtonMenuItem {
		final LookAndFeelInfo lafInfo;
		SkinRadioButtonMenuItem(LookAndFeelInfo lafInfo, ButtonGroup bg) {
			super(lafInfo.getName());
			this.lafInfo = lafInfo;
			bg.add(this);
		}
	}

	final REpiceaFrame mainFrame;

	public REpiceaLookAndFeelMenu(REpiceaFrame mainFrame) {
		super(MessageID.LookAndFeel.toString());
		this.mainFrame = mainFrame;
		ButtonGroup bgSkin = new ButtonGroup();
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			SkinRadioButtonMenuItem button = new SkinRadioButtonMenuItem(info, bgSkin);
			add(button);
			button.setSelected(info.getName().equals(UIManager.getLookAndFeel().getName()));
			button.addItemListener(this);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getStateChange() == ItemEvent.SELECTED) {
			SkinRadioButtonMenuItem menuItem = (SkinRadioButtonMenuItem) arg0.getSource();
			System.out.println("Selected L&F = " + menuItem.lafInfo.getName());
			if (mainFrame != null) {
				try { 
					UIManager.setLookAndFeel(menuItem.lafInfo.getClassName());
					SwingUtilities.updateComponentTreeUI(mainFrame);
				} catch (Exception e) {}
			}
		}
	}
}

