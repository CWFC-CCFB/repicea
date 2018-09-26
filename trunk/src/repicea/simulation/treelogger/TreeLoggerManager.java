/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge-Epicea
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
package repicea.simulation.treelogger;

import java.util.ArrayList;
import java.util.List;

public class TreeLoggerManager {

	private static final List<String> TreeLoggerClassNames = new ArrayList<String>();
	static {
//		TreeLoggerClassNames.add(BasicTreeLogger.class.getName());
//		TreeLoggerClassNames.add(EuropeanBeechBasicTreeLogger.class.getName());
//		TreeLoggerClassNames.add(MaritimePineBasicTreeLogger.class.getName());
//		TreeLoggerClassNames.add(WBirchProdVolTreeLogger.class.getName());
//		TreeLoggerClassNames.add("quebecmrnfutility.treelogger.petrotreelogger.PetroTreeLogger");
//		TreeLoggerClassNames.add("quebecmrnfutility.treelogger.sybille.SybilleTreeLogger");
//		TreeLoggerClassNames.add("lerfob.treelogger.mathilde.MathildeTreeLogger");	
//		TreeLoggerClassNames.add("lerfob.treelogger.douglasfirfcba.DouglasFCBATreeLogger");
	}
	
	private static TreeLoggerManager Instance;
	
	private final List<TreeLoggerDescription> availableTreeLoggers;
	
	private TreeLoggerManager() {
		availableTreeLoggers = new ArrayList<TreeLoggerDescription>();
		for (String treeLoggerName : TreeLoggerClassNames) {
			try {
				Class<?> treeLoggerClass = ClassLoader.getSystemClassLoader().loadClass(treeLoggerName);
				availableTreeLoggers.add(new TreeLoggerDescription(treeLoggerClass.getName()));
			} catch (Exception e) {
				System.out.println("Unable to load tree logger : " + treeLoggerName);
			}
		}
	}
	
	/**
	 * This method registers the tree logger name in order to load it afterwards.
	 * @param treeLoggerCompleteName the complete name e.g. repicea.simulation.treeLogger.MyTreeLogger
	 */
	public static void registerTreeLoggerName(String treeLoggerCompleteName) {
		TreeLoggerClassNames.add(treeLoggerCompleteName);
	}
	
	
	
	/**
	 * This method returns the TreeLoggerDescription instances that are compatible with
	 * the reference object.
	 * @param referent 
	 * @return a List of TreeLoggerDescription instances
	 */
	@SuppressWarnings("rawtypes")
	public List<TreeLoggerDescription> getCompatibleTreeLoggers(Object referent) {
		List<TreeLoggerDescription> outputList = new ArrayList<TreeLoggerDescription>();		
		for (TreeLoggerDescription treeLoggerDescription : availableTreeLoggers) {
			TreeLogger treeLogger = treeLoggerDescription.instantiateTreeLogger(false);
			if (treeLogger.isCompatibleWith(referent)) {
				outputList.add(treeLoggerDescription);
			}
		}
		return outputList;
	}
	
	public static TreeLoggerManager getInstance() {
		if (Instance == null) {
			Instance = new TreeLoggerManager();
		}
		return Instance;
	}
	
	public static void main(String[] args) {
		TreeLoggerManager.getInstance();
	}
	
}
