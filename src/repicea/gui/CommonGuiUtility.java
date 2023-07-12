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

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;
import java.io.File;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

import repicea.gui.UIControlManager.CommonControlID;
import repicea.util.ExtendedFileFilter;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The CommonGuiUtility class includes static methods that facilitates the Gui management.
 * @author Mathieu Fortin - March 2013
 */
public class CommonGuiUtility {

	/**
	 * This private class handles an error message as well as the owner of the JOptionPane.showMessageDialog that 
	 * is to show in the GUI.
	 * @author Mathieu Fortin - March 2013
	 */
	private static class SimpleMessage implements Runnable {

		private static Map<Integer, UIControlManager.InformationMessageTitle> availableMessageTypes = new HashMap<Integer, UIControlManager.InformationMessageTitle>();
		static {
			availableMessageTypes.put(JOptionPane.ERROR_MESSAGE, UIControlManager.InformationMessageTitle.Error);
			availableMessageTypes.put(JOptionPane.WARNING_MESSAGE, UIControlManager.InformationMessageTitle.Warning);
			availableMessageTypes.put(JOptionPane.INFORMATION_MESSAGE, UIControlManager.InformationMessageTitle.Information);
		}
		
		private final String message;
		private final Component parent;
		private final int messageType;
		
		private SimpleMessage(String message, int messageType, Component parent) {
			this.message = message;
			this.parent = parent;
			if (!availableMessageTypes.containsKey(messageType)) {
				throw new InvalidParameterException("The message type is not recognized! Should be either JOptionPane.ERROR_MESSAGE, JOptionPane.WARNING_MESSAGE or JOptionPane.INFORMATION_MESSAGE.");
			}
			this.messageType = messageType;
		}
		
		@Override
		public void run() {
			JOptionPane pane = new JOptionPane(message, messageType);
			List<JButton> buttons = CommonGuiUtility.mapComponents(pane, JButton.class);
			CommonGuiUtility.nameAbstractButtonsGivenTheirText(buttons);
			pane.createDialog(parent, availableMessageTypes.get(messageType).toString()).setVisible(true);
		}
	}



	/**
	 * This class provides the result of a file chooser dialog.
	 * @author Mathieu Fortin - October 2010
	 */
	public static class FileChooserOutput {
		private String filename;
		private boolean valid;
		
		protected FileChooserOutput(String filename, boolean valid) {
			this.filename = filename;
			this.valid = valid;
		}
		
		/**
		 * This method returns true if the JFileChooser has not been cancelled.
		 * @return a boolean
		 */
		public boolean isValid() {return valid;}
		
		/**
		 * This method returns the selected path.
		 * @return a String
		 */
		public String getFilename() {return filename;}
	}
	
	
	private static void nameAbstractButtonsGivenTheirText(List<? extends AbstractButton> buttons) {
		if (buttons == null) {
			throw new InvalidParameterException("The buttons parameter cannot be null!");
		}
		for (AbstractButton b : buttons) {
			b.setName(b.getText());
		}
	}
	
	

	/**
	 * This method instantiates a JFileChooser and returns the result in the FileChooserOutput object.
	 * @param owner a Component object that calls this JFileChooser
	 * @param fileSelectionMode either JFileChooser.FILES_ONLY (default), JFileChooser.DIRECTORIES_ONLY or JFileChooser.FILES_AND_DIRECTORIES
	 * @param originalFilename a String that indicates the initial path of the JFileChooser
	 * @param fileFilters a Vector of FileFilter instances
	 * @param dialogType either JFileChooser.SAVE_DIALOG or JFileChooser.OPEN_DIALOG
	 * @return a FileChooserOutput instance
	 */
	public static FileChooserOutput browseAction(Component owner,
								int fileSelectionMode, 
								String originalFilename,
								List<FileFilter> fileFilters,
								int dialogType) {
		return CommonGuiUtility.browseAction(owner, fileSelectionMode, originalFilename, fileFilters, dialogType, null);
	}
	
	/**
	 * This method instantiates a JFileChooser and returns the result in the FileChooserOutput object.
	 * @param owner a Component object that calls this JFileChooser
	 * @param fileSelectionMode either JFileChooser.FILES_ONLY (default), JFileChooser.DIRECTORIES_ONLY or JFileChooser.FILES_AND_DIRECTORIES
	 * @param originalFilename a String that indicates the initial path of the JFileChooser
	 * @param fileFilters a Vector of FileFilter instances
	 * @param dialogType either JFileChooser.SAVE_DIALOG or JFileChooser.OPEN_DIALOG
	 * @param fsv a FileSystemView instance
	 * @return a FileChooserOutput instance (can be null)
	 */
	public static FileChooserOutput browseAction(Component owner,
								int fileSelectionMode, 
								String originalFilename,
								List<FileFilter> fileFilters,
								int dialogType,
								FileSystemView fsv) {

		JFileChooser chooser = null;
		try {
			if (originalFilename != null && !new File(originalFilename).exists()) {
				chooser = new JFileChooser(new File(originalFilename).getParent(), fsv);
			} else {
				chooser = new JFileChooser(originalFilename, fsv);
			}
			
			chooser.setFileSelectionMode(fileSelectionMode);
			
			chooser.setDialogType(dialogType);

			List<FileFilter> checkedForNoneNullFileFilters = new ArrayList<FileFilter>();
			
			if (fileFilters != null && !fileFilters.isEmpty()) {
				for (FileFilter filter : fileFilters) {			// add the file chooser
					if (filter != null) {
						checkedForNoneNullFileFilters.add(filter);
					}
				}
			}			
			
			if (!checkedForNoneNullFileFilters.isEmpty()) {
				for (FileFilter filter : checkedForNoneNullFileFilters) {			// add the file chooser
					chooser.addChoosableFileFilter(filter);
				}
				
				chooser.setFileFilter(checkedForNoneNullFileFilters.get(0));			// default 
				for (FileFilter filter: checkedForNoneNullFileFilters) {					// set the file filter corresponding to the filename if any
					if (originalFilename != null && filter != null && filter.accept(new File(originalFilename))) {
						chooser.setFileFilter(filter);
					}
				}
				
				chooser.setAcceptAllFileFilterUsed(false);
			} else {
				chooser.setAcceptAllFileFilterUsed(true);
			}
		} catch (Exception e) {
			System.out.println("Error while opening JFileChooser.");
			return new FileChooserOutput("", false);
		}
		boolean acceptable = false;
		int returnVal = JFileChooser.CANCEL_OPTION;
		CommonGuiUtility.findAndAdaptButtonOfThisKind(chooser, CommonControlID.Open);
		CommonGuiUtility.findAndAdaptButtonOfThisKind(chooser, CommonControlID.Save);
		CommonGuiUtility.findAndAdaptButtonOfThisKind(chooser, CommonControlID.Cancel);
		CommonGuiUtility.mapComponents(chooser, JTextField.class).get(0).setName("Filename");
		while (!acceptable) {
			if (dialogType == JFileChooser.OPEN_DIALOG) {
				returnVal = chooser.showOpenDialog(owner);
			} else {
				returnVal = chooser.showSaveDialog(owner);
			}
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				acceptable = true;
			} else if (returnVal == JFileChooser.CANCEL_OPTION) {
				acceptable = true;
			}
		}
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fileName = chooser.getSelectedFile().toString().trim();
			if (chooser.getDialogType() == JFileChooser.SAVE_DIALOG) {
				if (chooser.getFileFilter() != null && chooser.getFileFilter() instanceof ExtendedFileFilter) {
					ExtendedFileFilter fileFilter = (ExtendedFileFilter) chooser.getFileFilter();
					if (!fileName.toLowerCase().endsWith(fileFilter.getExtension())) {
						fileName = fileName.concat(fileFilter.getExtension());
					}
				}
			}
			return new FileChooserOutput(fileName, true);
		} else {
			return new FileChooserOutput(originalFilename, false);
		}
	}
	
	static void findAndAdaptButtonOfThisKind(Container fc, TextableEnum controlID) {
		List<JButton> buttons = CommonGuiUtility.mapComponents(fc, JButton.class);
		for (JButton c : buttons) {
			if (REpiceaTranslator.getString(controlID).equals(c.getText())) {
				if (controlID instanceof Enum)
					c.setName(((Enum) controlID).name());
				if (controlID instanceof CommonControlID) {
					Icon icon = ((CommonControlID) controlID).getIcon();
					if (icon != null) {
						c.setIcon(icon);
					}
				}
			}
		}
	}
	
	

	
	/**
	 * This static method asks the user if he/she wants to write over an
	 * existing file.
	 * @param owner the parent component
	 * @return true if the user accepts or false otherwise
	 */
	public static boolean popupWriteOverWarningDialog(Component owner) {
		JButton ok = UIControlManager.createCommonButton(CommonControlID.Ok);
		JButton cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
		
		String[] proposedButtons = new String[2];
		
		proposedButtons[0] = ok.getText();
		proposedButtons[1] = cancel.getText();

		int result = JOptionPane.showOptionDialog(owner, 
				REpiceaTranslator.getString(UIControlManager.InformationMessage.FileAlreadyExists),
				REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Warning),
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null, 
				proposedButtons, 
				cancel.getText());
		
		if (result == 0) {
			return true;
		} else {
			return false;
		}
	}

	public static JPanel createSelectAFilePanel(JTextField textField, JButton browse, JLabel label) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		labelPanel.add(label);
		
		JPanel filenamePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		filenamePanel.add(textField);
		filenamePanel.add(browse);
		
		panel.add(labelPanel);
		panel.add(filenamePanel);
		return panel;
	}

	/**
	 * This method looks for the Window instance that owns this component. It returns null if the
	 * component has no parents of the Window class.
	 * @param comp a Component instance
	 * @return the Window instance or null
	 */
	public static Window getParentWindow(Component comp) {
		Container resultingContainer = getParentComponent(comp, Window.class);
		if (resultingContainer != null) {
			return (Window) resultingContainer;
		} else {
			return null;
		}
	}
	

	/**
	 * This method looks for the Container instance that owns this component. It returns null if the
	 * component has no parents of the class.
	 * @param comp a Component instance
	 * @param clazz a Class that extends Container
	 * @return the Container instance or null
	 */
	public static Container getParentComponent(Component comp, Class<? extends Container> clazz) {
		if (comp == null) {
			return null;
		} else {
			do {
				comp = comp.getParent();
			} while (comp != null && !(clazz.isAssignableFrom(comp.getClass())));
			if (comp == null) {
				return null;
			} else {
				return (Container) comp;
			}
		}
	}

	/**
	 * This method causes a JOptionPane.showErrorMessage to be sent on the Event Dispatch Thread.
	 * @param message the error message to be displayed
	 * @param parent the Container instance that is the parent of this error message (can be null)
	 */
	public static void showErrorMessage(String message, Component parent) {
		SwingUtilities.invokeLater(new SimpleMessage(message, JOptionPane.ERROR_MESSAGE, parent));
	}

	/**
	 * This method causes a JOptionPane.showErrorMessage to be sent on the Event Dispatch Thread.
	 * @param message the error message to be displayed
	 * @param parent the Container instance that is the parent of this error message (can be null)
	 */
	public static void showInformationMessage(String message, Component parent) {
		SwingUtilities.invokeLater(new SimpleMessage(message, JOptionPane.INFORMATION_MESSAGE, parent));
	}

	/**
	 * This method causes a JOptionPane.showErrorMessage to be sent on the Event Dispatch Thread.
	 * @param message the error message to be displayed
	 * @param parent the Container instance that is the parent of this error message (can be null)
	 */
	public static void showWarningMessage(String message, Component parent) {
		SwingUtilities.invokeLater(new SimpleMessage(message, JOptionPane.WARNING_MESSAGE, parent));
	}

	/**
	 * Scan a container for all the instances of a particular class that
	 * extends the Component class.
	 * @param <T> a Class that extends the Component class
	 * @param container a Container instance
	 * @param clazz a T instance 
	 * @return a List of Component instances
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Component> List<T>   mapComponents(Container container, Class<T> clazz) {
		List<T> textComponents = new ArrayList<T>();
		Component[] components = container.getComponents();
		for (Component component : components) {
			if (clazz.isAssignableFrom(component.getClass())) {
				textComponents.add((T) component);
			} else if (component instanceof Container) {
				textComponents.addAll(mapComponents((Container) component, clazz));
			}
		}
		return textComponents;
	}

	/**
	 * A recursive method that enables/disables all the buttons in a container.
	 * @param <T> a Class that extends the Component class
	 * @param container a Container instance
	 * @param clazz a Class instance that extends Component
	 * @param enabled a boolean
	 */
	public static <T extends Component> void enableThoseComponents(Container container, Class<T> clazz, boolean enabled) {
		List<T> componentList = CommonGuiUtility.mapComponents(container, clazz);
		for (Component comp : componentList) {
			comp.setEnabled(enabled);
		}
	}

	/**
	 * Find a component corresponding to this name within the component. <p>
	 * The method scans recursively the component if it is an instance of the Container class.
	 * @param comp a Component instance
	 * @param name the name of the Component instance we are looking for
	 * @return the Component instance or null if no component bears this name
	 */
	public static Component findComponentWithThisName(Component comp, String name) {
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

	
	/**
	 * This method is a recursive method that enables/disables all the controls, ie. buttons, textcomponents, 
	 * comboboxes, list, menubar, slider, spinner, table, tableheader, toolbar, tree
	 * @param container a Container instance
	 * @param enabled a boolean
	 */
	public static void enableAllControls(Container container, boolean enabled) {
		enableThoseComponents(container, AbstractButton.class, enabled);
		enableThoseComponents(container, JTextComponent.class, enabled);
		enableThoseComponents(container, JComboBox.class, enabled);
		enableThoseComponents(container, JList.class, enabled);
		enableThoseComponents(container, JMenuBar.class, enabled);
		enableThoseComponents(container, JSlider.class, enabled);
		enableThoseComponents(container, JSpinner.class, enabled);
		enableThoseComponents(container, JTable.class, enabled);
		enableThoseComponents(container, JTableHeader.class, enabled);
		enableThoseComponents(container, JToolBar.class, enabled);
		enableThoseComponents(container, JTree.class, enabled);
	}
	

	/**
	 * This method retrieves the icon in a specific package.
	 * @param clazz a class of the package for setting the path
	 * @param iconName the filename of the icon
	 * @return an ImageIcon instance
	 */
	public static ImageIcon retrieveIcon(Class<?> clazz, String iconName) {
		String iconPath = ObjectUtility.getRelativePackagePath(clazz) + iconName;
		InputStream iconInputStream = CommonGuiUtility.class.getResourceAsStream("/" + iconPath);
		try {
			Image image = ImageIO.read(iconInputStream);
			return new ImageIcon(image);
		} catch (Exception e2) {
			return null;
		}
	}

	/**
	 * This method truncates a filename and keeps only the last characters whose length is set through 
	 * the maxLength parameter.
	 * @param filename the filename (String)
	 * @param maxLength the maximum number of characters (Integer)
	 * @return a String
	 */
	public static String convertFilenameForLabel(String filename, int maxLength) {
		String convertedFilename;
		if (filename.length() > maxLength) {
			convertedFilename = "..." + filename.subSequence(filename.length() - maxLength, filename.length());
		} else {
			convertedFilename = filename;
		}
		return convertedFilename;
	}
	
	
	
	
	
	static public void main(String[] args) {
//		REpiceaTranslator.setCurrentLanguage(Language.French);
//		CommonGuiUtility.browseAction(null, JFileChooser.FILES_ONLY, null, null, JFileChooser.SAVE_DIALOG);
		CommonGuiUtility.showErrorMessage("Test1", null);
	}

	
}
