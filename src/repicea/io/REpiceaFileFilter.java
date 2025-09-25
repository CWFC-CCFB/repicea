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
public class REpiceaFileFilter extends FileFilter implements ExtendedFileFilter {

	public static enum FileType implements TextableEnum {
		DBF("dBase IV file (*.dbf)", "Fichier dBase IV (*.dbf)"), 
		CSV("delimited text file (*.csv)", "Fichier texte d\u00E9limit\u00E9 (*.csv)"),
		IFE("ImportFieldElements file (*.ife)",	"Fichier ImportFieldElements (*.ife)"),
//		ACCDB("Microsoft Access 2007 DataBase (*.accdb", "Base de donn\u00E9e Microsoft Access 2007 (*.accdb)"),
//		MDB("Microsoft Access DataBase (*.mdb)", "Base de donn\u00E9e Microsoft Access (*.mdb)"),
		XML("XML file (*.xml)", "Fichier XML (*.xml)"),
		JAVACLASS("Java class file (*.class)", "Fichier de classe Java (*.class)"),
		HTML("Hypertext Markup Language (*.html)", "Langage Hypertext Markup (*.html)"),
		TXT("Text File (*.txt)", "Fichier texte  (*.txt)"),
		SVG("Scalable Vector Graphics file (*.svg)", "Fichier Scalable Vector Graphics (*.svg)"),
		JSON("JavaScript Object Notation (*.json)", "Format JavaScript Object Notation (*.json)"),
		XLSX("Microsoft Excel Spreadsheet (*.xlsx)", "Tableur Excel (*.xlsx)"),
		UNKNOWN("","");

		private static Map<FileType, REpiceaFileFilter> FileFilterMap;
		
		
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
		
		private static Map<FileType, REpiceaFileFilter> getFileFilterMap() {
			if (FileFilterMap == null) {
				FileFilterMap = new HashMap<FileType, REpiceaFileFilter>();
//				FileFilterMap.put(ACCDB, REpiceaFileFilter.ACCDB);
				FileFilterMap.put(DBF, REpiceaFileFilter.DBF);
				FileFilterMap.put(CSV, REpiceaFileFilter.CSV);
				FileFilterMap.put(IFE, REpiceaFileFilter.IFE);
				FileFilterMap.put(XML, REpiceaFileFilter.XML);
				FileFilterMap.put(JAVACLASS, REpiceaFileFilter.JAVACLASS);
				FileFilterMap.put(HTML, REpiceaFileFilter.HTML);
				FileFilterMap.put(TXT, REpiceaFileFilter.TXT);
//				FileFilterMap.put(MDB,  REpiceaFileFilter.MDB);
				FileFilterMap.put(SVG,  REpiceaFileFilter.SVG);
				FileFilterMap.put(JSON, REpiceaFileFilter.JSON);
				FileFilterMap.put(XLSX, REpiceaFileFilter.XLSX);
			}
			return FileFilterMap;
		}
	
		/**
		 * This method returns the GFileFilter instance associated with this FileType.
		 * @return a GFileFilter or null if the FileType is UNKNOWN
		 */
		public REpiceaFileFilter getFileFilter() {
			return getFileFilterMap().get(this);
		}
		
	} 
	
	public final static REpiceaFileFilter DBF = new REpiceaFileFilter(".dbf", FileType.DBF);
	public final static REpiceaFileFilter CSV = new REpiceaFileFilter(".csv", FileType.CSV);
	public final static REpiceaFileFilter IFE = new REpiceaFileFilter(".ife", FileType.IFE);
//	public final static REpiceaFileFilter MDB = new REpiceaFileFilter(".mdb", FileType.MDB);
//	public final static REpiceaFileFilter ACCDB = new REpiceaFileFilter(".accdb", FileType.ACCDB);
	public final static REpiceaFileFilter XML = new REpiceaFileFilter(".xml", FileType.XML);
	public final static REpiceaFileFilter JAVACLASS = new REpiceaFileFilter(".class", FileType.JAVACLASS);
	public final static REpiceaFileFilter HTML = new REpiceaFileFilter(".html", FileType.HTML);
	public final static REpiceaFileFilter TXT = new REpiceaFileFilter(".txt", FileType.TXT);
	public final static REpiceaFileFilter SVG = new REpiceaFileFilter(".svg", FileType.SVG);
	public final static REpiceaFileFilter JSON = new REpiceaFileFilter(".json", FileType.JSON);
	public final static REpiceaFileFilter XLSX = new REpiceaFileFilter(".xlsx", FileType.XLSX);
	
	
	private String extension;
	private FileType fileType;
	
	private REpiceaFileFilter(String extension, FileType fileType) {
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
