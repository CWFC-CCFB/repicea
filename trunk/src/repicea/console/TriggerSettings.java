/*
 * This file is part of the repicea-console library.
 *
 * Copyright (C) 2012 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.console;

import java.io.Serializable;

import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.util.REpiceaTranslator;

public class TriggerSettings implements Memorizable {

	
	@SuppressWarnings("serial")
	public static class Language implements Serializable {

		public static final Language ENGLISH = new Language("English", "en");
		public static final Language FRENCH = new Language("Fran\u00E7ais", "fr");

		private String longName;
		private String capsisCode;

		private Language(String longName, String capsisCode) {
			this.longName = longName;
			this.capsisCode = capsisCode;
		}

		@Override
		public String toString() {return longName;}
		public String getLanguageCode() {return capsisCode;}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Language) {
				Language otherLanguage = (Language) obj;
				if (longName.equals(otherLanguage.longName) && capsisCode.equals(otherLanguage.capsisCode)) {
					return true;
				}
			} 
			return false;
		}
	}


	public static enum Encoding {
		US_ASCII("US-ASCII"),
		ISO_8859_1("ISO-8859-1"),
		UTF_8("UTF-8");

		String encodingName;

		Encoding(String encodingName) {
			this.encodingName = encodingName;
		}

		public String getEncodingName() {
			return encodingName;
		}

		@Override
		public String toString() {
			return getEncodingName();
		}
	}

	
	private final Trigger caller;
	private int maxMemoryJVM;
	private Language language;
	private Encoding encoding;

	protected TriggerSettings(Trigger caller) {
		this.caller = caller;
		String languageString = caller.getSettingMemory().getProperty("language", "en");
		if (languageString.equals("fr")) {
			language = Language.FRENCH;
		} else {
			language = Language.ENGLISH;  
		}
		maxMemoryJVM = caller.getSettingMemory().getProperty("memory", 512);
		encoding = Encoding.valueOf(caller.getSettingMemory().getProperty("encoding", "UTF_8"));
	}

	
	public int getMaxMemoryJVM() {return maxMemoryJVM;}
	protected void setMaxMemoryJVM(int maxMemoryJVM) {this.maxMemoryJVM = maxMemoryJVM;}
	
	public Language getLanguage() {return language;}
	
	protected void setLanguage(Language language) {
		this.language = language;
		if (language.equals(Language.ENGLISH)) {
			REpiceaTranslator.setCurrentLanguage(REpiceaTranslator.Language.English);
		} else if (language.equals(Language.FRENCH)) {
			REpiceaTranslator.setCurrentLanguage(REpiceaTranslator.Language.French);
		}
	}
	
	protected void setEncoding(Encoding encoding) {
		this.encoding = encoding;
	}
	
	public Encoding getEncoding() {
		if (encoding == null) {
			encoding = Encoding.UTF_8;
		}
		return encoding;
	}


	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = new MemorizerPackage();
		mp.add(maxMemoryJVM);
		mp.add(language);
		mp.add(encoding);
		return mp;
	}


	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		maxMemoryJVM = (Integer) wasMemorized.get(0);
		language = (Language) wasMemorized.get(1);
		encoding = (Encoding) wasMemorized.get(2);
	}


	protected void recordSettings() {
		caller.getSettingMemory().setProperty("language", getLanguage().getLanguageCode());
		caller.getSettingMemory().setProperty("memory", getMaxMemoryJVM());
		caller.getSettingMemory().setProperty("encoding", getEncoding().name());
	}
	
}
