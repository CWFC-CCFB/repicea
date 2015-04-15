/*
 * This file is part of the repicea-iotools library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
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
package repicea.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.filechooser.FileFilter;

import repicea.util.ExtendedFileFilter;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


/**
 * This class implements the method for any file filter.
 * @author Mathieu Fortin - August 2010
 */
public class GFileFilter extends FileFilter implements ExtendedFileFilter {

	public static enum FileType implements TextableEnum {
		DBF("dBase IV file (*.dbf)", "Fichier dBase IV (*.dbf)"), 
		CSV("delimited text file (*.csv)", "Fichier texte d\u00E9limit\u00E9 (*.csv)"),
		IFE("ImportFieldElements file (*.ife)",	"Fichier ImportFieldElements (*.ife)"),
//		MDB("Microsoft Access DataBase (*.mdb)", "Base de donn\u00E9e Microsoft Access (*.mdb)"),
		ACCDB("Microsoft Access 2007 DataBase (*.accdb", "Base de donn\u00E9e Microsoft Access 2007 (*.accdb)"),
		XML("XML file (*.xml)", "Fichier XML (*.xml)"),
		JAVACLASS("Java class file (*.class)", "Fichier de classe Java (*.class)"),
		UNKNOWN("","");

		private static Map<FileType, GFileFilter> FileFilterMap;
		
		
		FileType(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
		
		private static Map<FileType, GFileFilter> getFileFilterMap() {
			if (FileFilterMap == null) {
				FileFilterMap = new HashMap<FileType, GFileFilter>();
				FileFilterMap.put(ACCDB, GFileFilter.ACCDB);
				FileFilterMap.put(DBF, GFileFilter.DBF);
				FileFilterMap.put(CSV, GFileFilter.CSV);
				FileFilterMap.put(IFE, GFileFilter.IFE);
				FileFilterMap.put(XML, GFileFilter.XML);
				FileFilterMap.put(JAVACLASS, GFileFilter.JAVACLASS);
			}
			return FileFilterMap;
		}
	
		/**
		 * This method returns the GFileFilter instance associated with this FileType.
		 * @return a GFileFilter or null if the FileType is UNKNOWN
		 */
		public GFileFilter getFileFilter() {
			return getFileFilterMap().get(this);
		}
		
	} 
	
	public final static GFileFilter DBF = new GFileFilter(".dbf", FileType.DBF);
	public final static GFileFilter CSV = new GFileFilter(".csv", FileType.CSV);
	public final static GFileFilter IFE = new GFileFilter(".ife", FileType.IFE);
//	public final static GFileFilter MDB = new GFileFilter(".mdb", FileType.MDB);
	public final static GFileFilter ACCDB = new GFileFilter(".accdb", FileType.ACCDB);
	public final static GFileFilter XML = new GFileFilter(".xml", FileType.XML);
	public final static GFileFilter JAVACLASS = new GFileFilter(".class", FileType.JAVACLASS);
	
	private String extension;
	private FileType fileType;
	
	private GFileFilter(String extension, FileType fileType) {
		super();
		this.extension = extension;
		this.fileType = fileType;
	}
	
	public boolean accept(File file) {
		if (file.isDirectory()) { 
			return true; 
		} else {
			return file.getPath().toLowerCase().endsWith(getExtension());
		}
	}

	@Override
	public String getDescription() {return fileType.toString();}
	
	public String getExtension() {return extension.toLowerCase();}

	/**
	 * This method returns the file type associated with the file filter.
	 * @return a FileType enum instance
	 */
	public FileType getFileType() {return fileType;}
	
	
	/**
	 * This static method returns the file type of a filename string
	 * @param filename a String that contains the filename
	 * @return a FileType enum instance
	 */
	public static FileType getFileType(String filename) {
		for (FileType fileType : FileType.values()) {
			if (filename.toLowerCase().endsWith(fileType.name().toLowerCase())) {
				return fileType;
			}
		}
		return FileType.UNKNOWN;
	}

	
}
