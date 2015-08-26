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
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import repicea.app.GenericTaskFactory;
import repicea.gui.icons.IconFactory;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The UIControlManager class contains static methods that facilitate the design of the dialog.
 * Common messages are also included.
 * @author Mathieu Fortin - October 2012
 */
public class UIControlManager {

		
	
	public static enum FontType {
		DefaultFont,
		LabelFont,
		ButtonFont,
		MenuItemFont,
		MenuFont
	}
	
	public final static BufferedImage REpiceaIcon = getREpiceaIcon();
	
	private static BufferedImage getREpiceaIcon() {
		String path = ObjectUtility.getRelativePackagePath(UIControlManager.class);
		String iconFilename = path + "LogoRougeEpicea.png";
		InputStream in = ClassLoader.getSystemResourceAsStream(iconFilename);
		try {
			return ImageIO.read(in);
		} catch (IOException e) {
			return null;
		}
	}
	
	private static Map<Language, Map<Class<? extends Window>, String>> titles = new HashMap<Language, Map<Class<? extends Window>, String>>();
	static {
		for (Language language : Language.values()) {
			titles.put(language, new HashMap<Class<? extends Window>, String>());
		}
	}

	
	private static Map<FontType, Font> fonts = new HashMap<FontType, Font>();
//	static {
//		Font labelFont = new JLabel().getFont();
//		fonts.put(FontType.LabelFont, new Font(labelFont.getName(), Font.BOLD, labelFont.getSize()));
////		fonts.put(FontType.DefaultFont, labelFont);
////		fonts.put(FontType.ButtonFont, new JButton().getFont());
////		fonts.put(FontType.MenuFont, new JMenu().getFont());
////		fonts.put(FontType.MenuItemFont, new JMenuItem().getFont());
//	}
	
	
	private static Map<Class<? extends Window>, AutomatedHelper> helpMethods = new HashMap<Class<? extends Window>, AutomatedHelper>();
	
	/**
	 * This enum variable provides the most common controls.
	 * @author Mathieu Fortin - April 2012
	 */
	public static enum CommonControlID implements TextableEnum {
		
		Addone("Add", "Ajouter", "addone.png"),
		Browse("Browse", "Parcourir", "open.png"),
		Cancel("Cancel", "Annuler", "cancel.png"),
		Close("Close", "Fermer", null),
		Continue("Continue", "Continuer", "ok.png"),
		Cut("Cut", "Couper", "edit-cut.png"),
		Delete("Delete", "Effacer", "edit-cut.png"),
		Export("Export", "Exporter", "export.png"),
		GoDown("Move down", "Descendre", "go-down.png"),
		GoUp("Move up", "Monter", "go-up.png"),
		Help("Help", "Aide", "help.png"),
		Import("Import", "Importer", null),
		Ok("Ok", "Ok", "ok.png"),
		Open("Open", "Ouvrir", "open.png"),
		Options("Options", "Options", "option.png"),
		Preferences("Preferences", "Pr\u00E9f\u00E9rences", null),
		Quit("Quit", "Quitter", null),
		Refresh("Refresh", "Rafra\u00EEchir", "view-refresh.png"),
		RemoveOne("Remove", "Enlever", "removeone.png"),
		Reset("Reset", "R\u00E9initialiser", "edit-undo.png"),
		Save("Save", "Enregistrer", "save.png"),
		SaveAs("Save as", "Enregistrer sous", "saveas.png"),
		Stop("Stop", "Arr\u00EAter", "stop.png"),
		Undo("Undo", "Annuler", "edit-undo.png"),
		Redo("Redo", "R\u00E9tablir", "edit-redo.png"),
		StopRecord("Stop", "Arr\u00EAter", "stoprecord.png"),
		PlayRecord("Play", "Jouer", "playrecord.png"),
		Record("Record", "Enregistrer", "record.png");
		
		String iconFilename;
		
		CommonControlID(String englishText, String frenchText, String iconFilename) {
			setText(englishText, frenchText);
			this.iconFilename = iconFilename;
		}
		
		public String getIconFilename() {
			return iconFilename;
		}
		
		public Icon getIcon() {
			return IconFactory.getIcon(this);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}
	
	/**
	 * This enum variable provides the most common menu titles in the menu bar.
	 * @author Mathieu Fortin - April 2012
	 */
	public static enum CommonMenuTitle implements TextableEnum {
		File("File", "Fichier"),
		Edit("Edit", "Edition"),
		Tools("Tools", "Outils"),
		Options("Options", "Options"),
		About("?", "?");
		
		CommonMenuTitle(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}

	}
	
	/**
	 * This enum variable provides the most common message titles.
	 * @author Mathieu Fortin - April 2012
	 */
	public static enum InformationMessageTitle implements TextableEnum {
		Warning("WARNING!", "ATTENTION!"),
		Information("Information", "Information"),
		Error("ERROR!", "ERREUR!"),
		Progress("Progress", "Progression");
		
		InformationMessageTitle(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}

	}

	/**
	 * This enum variable provides the most common information messages.
	 * @author Mathieu Fortin - April 2012
	 */
	public static enum InformationMessage implements TextableEnum {
		DataCorrectlyLoaded("The data have been loaded.", 
				"Les donn\u00E9es ont \u00E9t\u00E9 charg\u00E9es avec succ\u00E8s."),
		ErrorWhileLoadingData("WARNING : An error occured while loading the data ! Please see the console for more information.",
				"ATTENTION : Une erreur est survenue lors de lecture des donn\u00E9es ! Veuillez consulter la console pour plus d'information."),
		DataCorrectlySaved("The data have been saved.",
				"Les donn\u00E9es ont \u00E9t\u00E9 sauvegard\u00E9es avec succ\u00E8s."),
		ErrorWhileSavingData("WARNING : An error occured while saving the data ! Please see the console for more information.",
				"ATTENTION : Une erreur est survenue lors de la sauvegarde des donn\u00E9es ! Veuillez consulter la console pour plus d'information."),
		FileAlreadyExists("This file already exists. Are you sure you want to continue?",
				"Ce fichier existe d\u00E9j\u00E0. Voulez-vous vraiment continuer?"),
		CannotDeleteFile("The file cannot be deleted. Please check if this file is open by another process.",
				"Ce fichier ne peut \u00EAtre effac\u00E9. Veuillez v\u00E9rifier s'il n'est pas utilis\u00E9 par un autre processus."),
		IncorrectFilename("The file or directory name is invalid.",
				"Le nom de fichier ou de r\u00E9pertoire est invalide."),
		ExpectedMemoryBustOut("The current task cannot be completed because it exceeds the memory of the Jave Virtual Machine.",
				"La t\u00E2che en cours ne peut \u00EAtre compl\u00E9t\u00E9e parce qu'elle exc\u00E8de la capacit\u00E9 de la m\u00E9moire de la machine virtuelle Java.");
		
		InformationMessage(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}

	}

	
	private static Map<Window, Point> Locations = new HashMap<Window, Point>();
	
	/**
	 * Phantom constructor to reduce the visibility of the default constructor.
	 */
	private UIControlManager() {}
	
	/**
	 * This method registers the window location.
	 * @param window a Window instance
	 */
	protected static void registerLocation(Window window) {
		Locations.put(window, window.getLocation());
	}

	/**
	 * This method sets the location of a particular window. 
	 * @param window a Window instance
	 * @param point a Point instance
	 */
	public static void setLocation(Window window, Point point) {
		Locations.put(window, point);
	}
	
	
	/**
	 * This method returns the location of the window.
	 * @param window the window that is to be shown
	 * @return a Point instance or null if the window has not been registered yet
	 */
	protected static Point getLocation(Window window) {
		return Locations.get(window);
	}
	
	/**
	 * This method registers a title for this Window-derived class.
	 * @param clazz the Window-derived class
	 * @param englishTitle the title in English
	 * @param frenchTitle the title in French
	 */
	public static void setTitle(Class<? extends Window> clazz, String englishTitle, String frenchTitle) {
		titles.get(Language.English).put(clazz, englishTitle);
		titles.get(Language.French).put(clazz, frenchTitle);
	}
	
	/**
	 * This method returns the title associated to this Window-derived class.
	 * @param clazz the Window-derived class
	 * @return the title or an empty string if the title has not been registered
	 */
	public static String getTitle(Class<? extends Window> clazz) {
		String title = titles.get(REpiceaTranslator.getCurrentLanguage()).get(clazz);
		if (title == null) {
			title = "";
		}
		return title;
	}


	/**
	 * This method registers the help method for this Window-derived class.
	 * @param clazz the Window-derived class
	 * @param helper an AutomatedHelper instance
	 */
	public static void setHelpMethod(Class<? extends Window> clazz, AutomatedHelper helper) {
		helpMethods.put(clazz, helper);
	}
	
	/**
	 * This method returns the help method for this class.
	 * @param clazz the Window-derived class
	 * @return an AutomatedHelper instance or null if the Window instance has not been registered
	 */
	public static AutomatedHelper getHelper(Class<?> clazz) {
		return helpMethods.get(clazz);
	}
	
	
	/**
	 * This method creates a button corresponding to the buttonName parameter.
	 * @param buttonID a enum that represents the button
	 * @return a JButton instance.
	 */
	public static JButton createCommonButton(CommonControlID buttonID) {
		JButton button = new JButton();
		String text = REpiceaTranslator.getString(buttonID);
		if (text == null) {
			text = "Unnamed";
		}
		button.setText(text);
		Icon icon = buttonID.getIcon();
		if (icon != null) {
			button.setIcon(buttonID.getIcon());
		}
		setFontOfThisComponent(button, FontType.ButtonFont);
		return button;
	}

	
	/**
	 * This method creates a menu item corresponding to the menuItemID parameter.
	 * @param menuItemID a enum that represents the control
	 * @return a JMenuItem instance.
	 */
	public static JMenuItem createCommonMenuItem(CommonControlID menuItemID) {
		JMenuItem menuItem = new JMenuItem();
		String text = REpiceaTranslator.getString(menuItemID);
		if (text == null) {
			text = "Unnamed";
		}
		menuItem.setText(text);
		Icon icon = menuItemID.getIcon();
		if (icon != null) {
			menuItem.setIcon(menuItemID.getIcon());
		}
		if (menuItemID == CommonControlID.Export) {
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.CTRL_MASK));
		} else if (menuItemID == CommonControlID.Save) {
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
		} else if (menuItemID == CommonControlID.Undo) {
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK));
		} else if (menuItemID == CommonControlID.Redo) {
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK));
		} else if (menuItemID == CommonControlID.Close || menuItemID == CommonControlID.Quit) {
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
		}
		setFontOfThisComponent(menuItem, FontType.MenuItemFont);
		return menuItem;
	}

	
	/**
	 * This method creates a menu item corresponding to the menuItemID parameter.
	 * @param menuItemID a enum that represents the control
	 * @param taskMaker a GenericTaskFactory insUIControlManagertance
	 * @return a RepiceaMenuItem instance.
	 */
	public static REpiceaMenuItem createCommonMenuItem(CommonControlID menuItemID, GenericTaskFactory taskMaker) {
		REpiceaMenuItem menuItem = new REpiceaMenuItem(taskMaker);
		String text = REpiceaTranslator.getString(menuItemID);
		if (text == null) {
			text = "Unnamed";
		}
		menuItem.setText(text);
		Icon icon = menuItemID.getIcon();
		if (icon != null) {
			menuItem.setIcon(menuItemID.getIcon());
		}
		setFontOfThisComponent(menuItem, FontType.MenuItemFont);
		return menuItem;
	}

	
	/**
	 * This method creates a menu corresponding to the menuTitle parameter.
	 * @param menuTitle a enum that represents the control
	 * @return a JMenu instance.
	 */
	public static JMenu createCommonMenu(CommonMenuTitle menuTitle) {
		JMenu menu = new JMenu();
		String text = REpiceaTranslator.getString(menuTitle);
		if (text == null) {
			text = "Unnamed";
		}
		menu.setText(text);
		setFontOfThisComponent(menu, FontType.MenuFont);
		return menu;
	}
	

	
	/**
	 * This method sets the font of a particular component. If the fontType has not been specified,
	 * the default font is used.
	 * @param component the Component instance
	 * @param fontType the font type as a FontType enum variable
	 */
	private static void setFontOfThisComponent(Component component, FontType fontType) {
		Font font = getFont(fontType);
		if (font != null) {
			component.setFont(font);
		}
	}
	
	
	/**
	 * This method returns a JLabel instance.
	 * @param labelString the label to be displayed
	 * @param width the width of the label (0 for no predifined width)
	 * @return a JLabel instance
	 */
	public static JLabel getLabel(String labelString, int width) {
		if (width < 0) {
			throw new InvalidParameterException("Width must be larger or equal to 0");
		}
		JLabel label = new JLabel(labelString);
		setFontOfThisComponent(label, FontType.LabelFont);
		if (width > 0) {
			int textLength = label.getFontMetrics(label.getFont()).stringWidth(labelString); 
			int fontHeight = label.getFontMetrics(label.getFont()).getHeight();

			int finalWidth = width >= textLength ? width : textLength;


			label.setPreferredSize(new Dimension (finalWidth, fontHeight+2));
			label.setMinimumSize(new Dimension (finalWidth, fontHeight+2));
			label.setMaximumSize(new Dimension (finalWidth, fontHeight+2));
		}
		return label;
	}


	/**
	 * This method returns a JLabel instance.
	 * @param textableEnum a TextableEnum enum var
	 * @param width the width of the label (0 for no predefined width)
	 * @return a JLabel instance
	 */
	public static JLabel getLabel(TextableEnum textableEnum, int width) {
		return UIControlManager.getLabel(REpiceaTranslator.getString(textableEnum), width);
	}

	
	
	/**
	 * This method returns a JLabel instance with no predefined width.
	 * @param labelString the string that appears in the label
	 * @return a JLabel instance
	 */
	public static JLabel getLabel(String labelString) {
		return UIControlManager.getLabel(labelString, 0);
	}

	/**
	 * This method returns a JLabel instance with no predefined width.
	 * @param textableEnum a TextableEnum enum var
	 * @return a JLabel instance
	 */
	public static JLabel getLabel(TextableEnum textableEnum) {
		return UIControlManager.getLabel(REpiceaTranslator.getString(textableEnum), 0);
	}
	
	

	/**
	 * This method makes it possible to change the fonts of the different controls.
	 * @param fontType a FontType instance
	 * @param font a Font instance
	 */
	public static void setFont(FontType fontType, Font font) {
		fonts.put(fontType, font);
	}
	
	/**
	 * This method returns the font associated to this FontType enum variable
	 * @param fontType a FontType enum variable
	 * @return a Font instance

	 */
	public static Font getFont(FontType fontType) {
		return fonts.get(fontType);
	}
	

	/**
	 * This method returns a Dimension instance whose width and height are the specified fraction of the screen dimension.
	 * @param widthFraction a double between 0 and 1
	 * @param heightFraction a double between 0 and 1
	 * @return a Dimension instance
	 */
	public static Dimension getDimensionRelativeToScreen(double widthFraction, double heightFraction) {
		if (widthFraction < 0 || widthFraction > 1) {
			throw new InvalidParameterException("The width faction must be between 0 and 1!");
		}
		if (heightFraction < 0 || heightFraction > 1) {
			throw new InvalidParameterException("The height faction must be between 0 and 1!");
		}
		Dimension screenResolution = Toolkit.getDefaultToolkit().getScreenSize();
		int newWidth = (int) (screenResolution.width * widthFraction);
		int newHeight = (int) (screenResolution.height * heightFraction);
		return new Dimension(newWidth, newHeight);
	}
	

	/**
	 * This static method returns a Border instance with a title. 
	 * @param title the title to appear in the Border instance
	 * @return a TitledBorder instance
	 */
	public static TitledBorder getTitledBorder(String title) {
		Border border = BorderFactory.createEtchedBorder();
		TitledBorder titledBorder = BorderFactory.createTitledBorder(border, title);
		return titledBorder;
	}
	
 }
