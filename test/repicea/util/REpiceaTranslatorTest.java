/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import repicea.util.REpiceaTranslator.Language;

public final class REpiceaTranslatorTest {

	@Test
	public void frenchLocaleTest() {
		Language referenceLanguage = REpiceaTranslator.getCurrentLanguage();
		Locale locale = Locale.FRENCH;
		REpiceaTranslator.findMatchForThisLocale(locale);
		Assert.assertEquals("Testing language set to French",
				Language.French,
				REpiceaTranslator.getCurrentLanguage());
		REpiceaTranslator.setCurrentLanguage(referenceLanguage);
	}
	
	@Test
	public void franceLocaleTest() {
		Language referenceLanguage = REpiceaTranslator.getCurrentLanguage();
		Locale locale = Locale.FRANCE;
		REpiceaTranslator.findMatchForThisLocale(locale);
		Assert.assertEquals("Testing language set to French",
				Language.French,
				REpiceaTranslator.getCurrentLanguage());
		REpiceaTranslator.setCurrentLanguage(referenceLanguage);
	}

	@Test
	public void canadaFrenchLocaleTest() {
		Language referenceLanguage = REpiceaTranslator.getCurrentLanguage();
		Locale locale = Locale.CANADA_FRENCH;
		REpiceaTranslator.findMatchForThisLocale(locale);
		Assert.assertEquals("Testing language set to French",
				Language.French,
				REpiceaTranslator.getCurrentLanguage());
		REpiceaTranslator.setCurrentLanguage(referenceLanguage);
	}

	@Test
	public void defaultLocaleChangeTest() {
		Language referenceLanguage = REpiceaTranslator.getCurrentLanguage();
		Locale locale = Locale.CANADA_FRENCH;
		Locale.setDefault(locale);
		Assert.assertEquals("Testing language set to French",
				Language.French,
				REpiceaTranslator.getCurrentLanguage());
		REpiceaTranslator.setCurrentLanguage(referenceLanguage);
	}

}
