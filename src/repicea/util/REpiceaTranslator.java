/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge Epicea.
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
package repicea.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.UIManager;

/**
 * The REpiceaTranslator class handles the translation to another language.
 * @author Mathieu Fortin - November 2014
 */
public class REpiceaTranslator {
	
	public static interface TextableEnum {
		/**
		 * This method sets the english and french text for the label, control or title.
		 * @param englishText a String
		 * @param frenchText a String
		 */
		public void setText(String englishText, String frenchText);
	}
	
	public static enum Language {
		English(Locale.ENGLISH, "English", "en"),
		French(Locale.FRENCH, "Fran\u00E7ais", "fr");
		
		private static Map<String, Language> CodeMap;
		
		Locale locale;
		String code;
		String longName;
		
		Language(Locale locale, String longName, String code) {
			this.locale = locale;
			this.longName = longName;
			this.code = code;
		}
		
		@Override
		public String toString() {return longName;}
		
		/**
		 * Returns the locale associated with the language.
		 * @return a Locale instance
		 */
		public Locale getLocale() {return locale;}

		/**
		 * This method returns the two-character code associated with the Language instance.
		 * @return a String
		 */
		public String getCode() {return code;}
		
		/**
		 * This method returns the Language enum that corresponds to
		 * a particular code. For example, "en" as argument returns 
		 * Language.English. 
		 * @param code a String 
		 * @return a Language instance, can be null if there is no Language enum matching the code
		 */
		public static Language getLanguage(String code) {
			if (CodeMap == null) {
				CodeMap = new HashMap<String, Language>();
				for (Language language : Language.values()) {
					CodeMap.put(language.code, language);
				}
			}
			return CodeMap.get(code.trim().toLowerCase());
		}
	}

	private static Language currentLanguage; 
	private static Locale currentLocale = Locale.getDefault();

	static {			// sets the default language
		findMatchForThisLocale(currentLocale);
	}

	static void findMatchForThisLocale(Locale locale) {
		boolean matchFound = false;
		for (Language language : Language.values()) {
			if (locale.getLanguage().equals(language.getLocale().getLanguage())) {
				matchFound = true;
				REpiceaTranslator.setCurrentLanguage(language);
				break;
			}
		}
		if (!matchFound) {
			REpiceaTranslator.setCurrentLanguage(Language.English);		// default option if not language matches
		}
	}
	
	
		
	private static Map<Language, Map<TextableEnum, String>> strings = new HashMap<Language, Map<TextableEnum, String>>();
	static {
		for (Language language : Language.values()) {
			strings.put(language, new HashMap<TextableEnum, String>());
		}
	}
	

	/**
	 * Set the different strings for an element, i.e. a control, label or message.
	 * @param element the TextableEnum that represents the control, label or message
	 * @param englishText a String
	 * @param frenchText a String
	 */
	public static synchronized void setString(TextableEnum element, String englishText, String frenchText) {
		strings.get(Language.English).put(element, englishText);
		strings.get(Language.French).put(element, frenchText);
	}
	
	/**
	 * This method returns the message associated to this messageID Enum instance.
	 * @param messageID an TextableEnum instance
	 * @return the message or an empty string if the title has not been registered
	 */
	public static String getString(TextableEnum messageID) {
		return REpiceaTranslator.getTranslation(messageID, getCurrentLanguage());
	}
	
	/**
	 * This method sets the current language of the UIControlManager. By default, the current language is set to
	 * Language.English.
	 * @param language a Language enum variable
	 */
	public static void setCurrentLanguage(Language language) {
		currentLanguage = language;
		UIManager.getDefaults().setDefaultLocale(currentLanguage.getLocale());
		Locale.setDefault(currentLanguage.getLocale());
		if (currentLanguage == Language.English) {
			UIManager.put("OptionPane.noButtonText", "No");
			UIManager.put("OptionPane.yesButtonText", "Yes");
		} else if (currentLanguage == Language.French) {
			UIManager.put("OptionPane.noButtonText", "Non");
			UIManager.put("OptionPane.yesButtonText", "Oui");
		}
	}
	
	/**
	 * This method returns the current language of the UIControlManager.
	 * @return a Language enum variable
	 */
	public static Language getCurrentLanguage() {
		if (!Locale.getDefault().equals(currentLocale)) {
			currentLocale = Locale.getDefault();
			findMatchForThisLocale(currentLocale);
		}
		return currentLanguage;
	}


	/**
	 * This method returns the translation of a particular TextableEnum instance.
	 * @param textableEnum a TextableEnum instance
	 * @param language a Language instance
	 * @return a String which is empty if the translation does not exist
	 */
	public static String getTranslation(TextableEnum textableEnum, Language language) {
		if (strings.containsKey(language)) {
			if (strings.get(language).containsKey(textableEnum)) {
				return strings.get(language).get(textableEnum);
			}
		}
		return "";
	}
	
}
