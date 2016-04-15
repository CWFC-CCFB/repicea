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

import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;

public class TriggerSettings implements Memorizable {

	
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
	private int allocatedMemoryJVM;
	private Language language;
	private Encoding encoding;
	private final String jreVersion;
	private final String revision;
	private final String architecture;
	protected final int maxAllowedMemoryJVM;
	protected final int minAllowedMemoryJVM;
	
	protected TriggerSettings(Trigger caller) {
		this.caller = caller;
		String languageString = caller.getSettingMemory().getProperty("language", "en");
		if (languageString.equals("fr")) {
			language = Language.French;
		} else {
			language = Language.English;  
		}
		
		String completeJREVersion = System.getProperty("java.version");
		jreVersion = completeJREVersion.substring(0, completeJREVersion.indexOf("_"));
		revision = completeJREVersion.substring(completeJREVersion.indexOf("_") + 1);

		architecture = System.getProperty("os.arch");
		if (architecture.endsWith("64")) {
			minAllowedMemoryJVM = 1024;
			maxAllowedMemoryJVM = 16 * 1024;
		} else {
			minAllowedMemoryJVM = 256;
			maxAllowedMemoryJVM = 2 * 1024;
		}
		
		setAllocatedMemoryJVM(caller.getSettingMemory().getProperty("memory", 512));
		encoding = Encoding.valueOf(caller.getSettingMemory().getProperty("encoding", "UTF_8"));
	}

	
	public int getAllocatedMemoryJVM() {return allocatedMemoryJVM;}
	protected void setAllocatedMemoryJVM(int allocatedMemoryJVM) {
		if (allocatedMemoryJVM < minAllowedMemoryJVM) {
			this.allocatedMemoryJVM = minAllowedMemoryJVM;
		} else if (allocatedMemoryJVM > maxAllowedMemoryJVM) {
			this.allocatedMemoryJVM = maxAllowedMemoryJVM;
		} else {
			this.allocatedMemoryJVM = allocatedMemoryJVM;
		}
	}
	
	public Language getLanguage() {return language;}
	
	protected void setLanguage(Language language) {
		this.language = language;
		REpiceaTranslator.setCurrentLanguage(language);
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
		mp.add(allocatedMemoryJVM);
		mp.add(language);
		mp.add(encoding);
		return mp;
	}


	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		setAllocatedMemoryJVM((Integer) wasMemorized.get(0));
		language = (Language) wasMemorized.get(1);
		encoding = (Encoding) wasMemorized.get(2);
	}


	protected void recordSettings() {
		caller.getSettingMemory().setProperty("language", getLanguage().getCode());
		caller.getSettingMemory().setProperty("memory", getAllocatedMemoryJVM());
		caller.getSettingMemory().setProperty("encoding", getEncoding().name());
	}
	
	protected String getJreVersion() {return jreVersion;}
	protected String getRevision() {return revision;}
	protected String getArchitecture() {return architecture;}
}
