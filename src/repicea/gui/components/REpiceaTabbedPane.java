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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import repicea.gui.REpiceaPanel;

/**
 * The REpiceaTabbedPane class extends JTabbedPane. It includes an additional feature which is a close button on each tab. By default this option 
 * is enabled. If the class is instantiated with the parameter closeTabEnabled set to false, then the REpiceaTabbedPane is similar to the original JTabbedPane
 * class. 
 * 
 * The protected method postRemovalActions() is called just after removing a tab. Overriding it is possible. 
 * @author Mathieu Fortin - November 2015
 */
@SuppressWarnings("serial")
public class REpiceaTabbedPane extends JTabbedPane {

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
//			closeButton.setMargin(new Insets(2,2,2,2));
			closeButton.setContentAreaFilled(false);
			closeButton.setFocusPainted(false);
			closeButton.setOpaque(false);
			closeButton.setBorder(BorderFactory.createRaisedSoftBevelBorder());
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
		}

		@Override
		public void doNotListenToAnymore() {
			closeButton.removeActionListener(this);
			closeButton.removeMouseListener(this);
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
			closeButton.setOpaque(true);
			closeButton.setForeground(Color.WHITE);
			this.colorBeforeEntering = closeButton.getBackground();
			closeButton.setBackground(Color.RED);
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			closeButton.setForeground(Color.BLACK);
			closeButton.setOpaque(false);
			closeButton.setBackground(colorBeforeEntering);
		}

		@Override
		public void mousePressed(MouseEvent arg0) {}

		@Override
		public void mouseReleased(MouseEvent arg0) {}
		
	}
	
	private final boolean closeTabEnabled;
	
	/**
	 * General constructor.
	 * @param closeTabEnabled
	 */
	public REpiceaTabbedPane(boolean closeTabEnabled) {
		this.closeTabEnabled = closeTabEnabled;
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
	public void remove(Component component) {
		super.remove(component);
		postRemovalActions();
	}
	
	
	
	@Override
	public void insertTab(String title, Icon icon, Component component, String tip, int index) {
		super.insertTab(title, icon, component, tip, index);
		setTitleAt(index, title);
	}
	
	
	
	protected void postRemovalActions() {}

//	public static void main(String[] args) {
//		JDialog dialog = new JDialog();
//		dialog.setModal(true);
//		REpiceaTabbedPane tabbedPane = new REpiceaTabbedPane(true);
//		dialog.add(tabbedPane);
//		Component comp = new JLabel("Allo");
//		tabbedPane.addTab("Allo Tab", comp);
////		tabbedPane.setTabComponentAt(0, new TabTitleComponent(tabbedPane, "AlloTab", comp));
//		dialog.pack();
//		dialog.setVisible(true);
//		System.exit(0);
//	}

}
