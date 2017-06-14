/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation.covariateproviders.standlevel;

import java.util.ArrayList;
import java.util.List;

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * This interface ensures that the stand or the plot instance can return its slope class 
 * according to the Quebec Ministry of Natural resources classification.
 * @author Mathieu Fortin - March 2017
 */
public interface SlopeMRNFClassProvider {

	public static enum SlopeMRNFClass implements TextableEnum {
		A("0-3%"),
		B("4-8%"),
		C("9-15%"),
		D("16-30%"),
		E("31-40%"),
		F(">40%");
		
		private static List<String> AvailableSlopeClasses;
		
		SlopeMRNFClass(String text) {
			setText(text,text);
		}
		
		public static boolean isThisClassRecognized(String slopeClassString) {
			if (AvailableSlopeClasses == null) {
				AvailableSlopeClasses = new ArrayList<String>();
				for (SlopeMRNFClass slopeClass : SlopeMRNFClass.values()) {
					AvailableSlopeClasses.add(slopeClass.name());
				}
			}
			return AvailableSlopeClasses.contains(slopeClassString);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	
	/**
	 * This method returns the slope class according to the Quebec Ministry of Natural resources classification.
	 * @return SlopeMRNFClass enum
	 */
	public SlopeMRNFClass getSlopeClass();
	
}
