/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import repicea.gui.REpiceaPanel;
import repicea.gui.popup.REpiceaPopupListener;
import repicea.gui.popup.REpiceaPopupMenu;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The REpiceaTabbedPane class extends JTabbedPane. It includes an additional feature which is a close button on each tab. By default this option 
 * is enabled. If the class is instantiated with the parameter closeTabEnabled set to false, then the REpiceaTabbedPane is similar to the original JTabbedPane
 * class. 
 * 
 * The protected method postRemovalActions() is called just after removing a tab. Overriding it is possible. 
 * @author Mathieu Fortin - November 2015
 */
@SuppressWarnings("serial")
public class REpiceaTabbedPane extends JTabbedPane implements ActionListener {

	protected class TabTitleComponent extends REpiceaPanel implements ActionListener, MouseListener {

		private final JButton closeButton;
		private final String title;
		private Component comp;
		private Color colorBeforeEntering;
		
		protected TabTitleComponent(String title) {
			this.title = title;
			closeButton = new JButton("\u00D7");
			closeButton.setForeground(Color.BLACK);
			Font font = closeButton.getFont();
			closeButton.setFont(font.deriveFont(Font.BOLD));
			closeButton.setMargin(new Insets(1,1,1,1));
			closeButton.setContentAreaFilled(false);
			closeButton.setFocusPainted(false);
			closeButton.setOpaque(false);
			closeButton.setBorder(BorderFactory.createRaisedSoftBevelBorder());
			addMouseListener(new REpiceaPopupListener(popupMenu));
		}
		
		
		protected void createInterface() {
			setLayout(new FlowLayout(FlowLayout.LEFT));
			JLabel titleLabel = new JLabel(title);
			add(titleLabel);
			setOpaque(false);
			if (REpiceaTabbedPane.this.closeTabEnabled) {
				add(Box.createHorizontalStrut(2));
				add(closeButton);
			}
		}
		
		@Override
		public void refreshInterface() {}

		@Override
		public void listenTo() {
			closeButton.addActionListener(this);
			closeButton.addMouseListener(this);
			addMouseListener(this);
		}

		@Override
		public void doNotListenToAnymore() {
			closeButton.removeActionListener(this);
			closeButton.removeMouseListener(this);
			removeMouseListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(closeButton)) {
				REpiceaTabbedPane.this.remove(comp);
			}
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			if (arg0.getSource().equals(closeButton)) {
				closeButton.setOpaque(true);
				closeButton.setForeground(Color.WHITE);
				this.colorBeforeEntering = closeButton.getBackground();
				closeButton.setBackground(Color.RED);
			}
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			if (arg0.getSource().equals(closeButton)) {
				closeButton.setForeground(Color.BLACK);
				closeButton.setOpaque(false);
				closeButton.setBackground(colorBeforeEntering);
			}
		}

		@Override
		public void mousePressed(MouseEvent arg0) {}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if (Arrays.asList(REpiceaTabbedPane.this.getComponents()).contains(comp)) {
				REpiceaTabbedPane.this.setSelectedComponent(comp);
			}
		}
	}

	
	
	
	private static enum MessageID implements TextableEnum {
		CloseButtonLabel("Close tab", "Supprimer cet onglet"),
		CloseOtherButtonLabel("Close other tabs", "Supprimer les autres onglets"),
		CloseAllButton("Close all tabs", "Supprimer tous les onglets");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}
	
	
	private final boolean closeTabEnabled;
	private REpiceaPopupMenu popupMenu;
	private final JMenuItem closeButton;
	private final JMenuItem closeAllButton;
	private final JMenuItem closeOtherButton;
	
	/**
	 * General constructor.
	 * @param closeTabEnabled
	 */
	public REpiceaTabbedPane(boolean closeTabEnabled) {
		this.closeTabEnabled = closeTabEnabled;
		closeButton = new JMenuItem(MessageID.CloseButtonLabel.toString());
		closeAllButton = new JMenuItem(MessageID.CloseAllButton.toString());
		closeOtherButton = new JMenuItem(MessageID.CloseOtherButtonLabel.toString());
		setPopupMenu();
	}

	public void showPopMenu(Point point) {
		if (!popupMenu.isVisible() && closeTabEnabled) {
			popupMenu.setLocation(point);
			popupMenu.setVisible(true);
		}
	}

	/**
	 * Usual constructor. Preferred one.
	 */
	public REpiceaTabbedPane() {
		this(true);
	}
	
	@Override
	public void setTitleAt(int index, String title) {
		if (closeTabEnabled) {
			setTabComponentAt(index, new TabTitleComponent(title));
		} else {
			super.setTitleAt(index, title);
		}
	}

	@Override
	public void addTab(String title, Component component) {
		super.addTab(title, component);
		if (closeTabEnabled) {
			int index = -1;
			for (int i = 0; i < this.getTabCount(); i++) {
				if (getComponentAt(i).equals(component)) {
					index = i;
					break;
				}
			}
			if (index > -1) {
				setTitleAt(index, title);
			}
		}
	}
	
	@Override
	public void setTabComponentAt(int index, Component component) {
		if (component instanceof TabTitleComponent) {
			TabTitleComponent tabTitle = (TabTitleComponent) component;
			tabTitle.createInterface();
			tabTitle.comp = getComponentAt(index);
		} 
		super.setTabComponentAt(index, component);
	}
	
	
	@Override
	public void insertTab(String title, Icon icon, Component component, String tip, int index) {
		super.insertTab(title, icon, component, tip, index);
		setTitleAt(index, title);
	}
	
	protected void setPopupMenu() {
		popupMenu = new REpiceaPopupMenu(this, closeButton, closeAllButton, closeOtherButton);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(closeButton)) {
			remove(getSelectedComponent());
		} else if (arg0.getSource().equals(closeOtherButton)) {
			while (this.getTabCount() != 1) {
				if (getComponentAt(0).equals(getSelectedComponent())) {
					remove(1);
				} else {
					remove(0);
				}
			}
		} else if (arg0.getSource().equals(closeAllButton)) {
			removeAll();
		}
	}

	public static void main(String[] args) {
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		REpiceaTabbedPane tabbedPane = new REpiceaTabbedPane(true);
		dialog.add(tabbedPane);
		for (int i = 0; i < 4; i++) {
			tabbedPane.addTab("Allo Tab " + i, new JLabel("Allo " + i));
		}
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}


}
