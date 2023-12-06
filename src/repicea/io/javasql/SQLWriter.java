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
package repicea.io.javasql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import repicea.io.FormatField;
import repicea.io.FormatWriter;
import repicea.io.GExportFieldDetails;
import repicea.io.REpiceaFileFilter;
import repicea.io.REpiceaFileFilter.FileType;

/**
 * The SQLWriter class is an extension of the FormatWriter class, which is designed to write tables in MS Access
 * databases. 
 * @author Mathieu Fortin - October 2012
 */
public class SQLWriter extends FormatWriter<SQLHeader> {

	private Connection dbConnection;
	private Statement statement;
	private String table;

	/**
	 * Constructor
	 * @param dataBaseFile the file that represents the database
	 * @param table the table in which the data are to be written
	 * @param appendTable a boolean, with true meaning that the new record is appended to previous record
	 * @throws IOException if an I/O error has occurred
	 */
	public SQLWriter(File dataBaseFile, String table, boolean appendTable) throws IOException {
		super(dataBaseFile, true);		// append the database by default
		this.table = table;
		
		FileType f = REpiceaFileFilter.getFileType(getFilename());
		if (f != FileType.ACCDB && f != FileType.MDB) {
			throw new IOException("SQLWriter.c. The file is not a .accdb or a .mdb file");
		}
		setFormatHeader(new SQLHeader());

		File outputFile = new File(getFilename());
		try {
			if (!outputFile.exists()) {
				throw new IOException("The database file does not exist!");
			} else {
				DatabaseConnectionManager.registerConnectionUser(this, getFilename());
//				dbConnection = DatabaseConnector.getConnectionFromThisMSACCESSDataBase(getFilename());
				dbConnection = DatabaseConnectionManager.getUserConnection(this);
				dbConnection.setAutoCommit(true);
				dbConnection.setReadOnly(false);
				statement = dbConnection.createStatement();
				if (appendTable) {
					getHeader().read(statement, table);
				}
			} 
		} catch (SQLException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public void setFields(List<FormatField> fields) throws IOException {
		try {
			super.setFields(fields);
			String sqlStatementStr = "CREATE TABLE " + table + " (";
			for (int i = 0; i < getHeader().getNumberOfFields(); i++) {				
				sqlStatementStr += getHeader().getField(i).getStatement();
				if (i == getHeader().getNumberOfFields() - 1) {
					sqlStatementStr += ")";
				} else {
					sqlStatementStr += ", ";
				}
			}
			DatabaseMetaData metaData = dbConnection.getMetaData();
			ResultSet tables = metaData.getTables(null, null, table, null);
			if (tables.next()) {
				statement.execute("DROP TABLE " + table);
			}

			statement.executeUpdate(sqlStatementStr);
//			statement.close();
		} catch (SQLException e) {
			throw new IOException(e.getMessage() + "SQLWriter.setFields(). An error occured while setting the fields");
		}
	}

	
	@Override
	public void close() throws IOException {
		try {
			DatabaseConnectionManager.removeUser(this);
		} catch (SQLException e) {
			throw new IOException("Error while closing the database!" + e);
		}
	}

	@Override
	public void addRecord(Object[] record) throws IOException {
		try {
			validateRecord(record);
			String sqlStatementStr = "INSERT INTO " + table + " " + getHeader().getFieldListString() + " VALUES (";
			for (int i = 0; i < record.length; i++) {
				if (record[i] instanceof Number) {
					sqlStatementStr += record[i].toString(); 
				} else {
					sqlStatementStr += "'"+ record[i].toString() +"'";
				}
				
				if (i == record.length - 1) {
					sqlStatementStr += ")";
				} else {
					sqlStatementStr += ", ";
				}
			}
			statement.execute(sqlStatementStr);
			getHeader().setNumberOfRecords(getHeader().getNumberOfRecords() + 1);
		} catch (SQLException e) {
			throw new IOException(e.getMessage());
		}

	}
	
	
	@Override
	public FormatField convertGExportFieldDetailsToFormatField(GExportFieldDetails details) {
		String name = details.getName();
		Object value = details.getValue();
		int precision = details.getLength();
		if (value instanceof String) {
			return new SQLField(name, "varchar", precision);	
		} else if (value instanceof Double) {
			return new SQLField(name, "double", precision);
		} else if (value instanceof Integer) {
			return new SQLField(name, "integer", precision);
		} else if (value instanceof Float){
			return new SQLField(name, "float", precision);
		} else {
			return null;
		}
	}

//	public static void main(String[] args) {
//		String dataBaseUrl = "D:/Travail/MRNF - Projets/lac.mdb";
//		String table = "chapeaux";
//		try {
//			Runnable task = new Runnable() {
//				public void run() {
//					String dataBaseUrl = "D:/Travail/MRNF - Projets/lac.mdb";
//					String table = "chapeaux";
//					try {
//						SQLReader sqlReader = new SQLReader(dataBaseUrl, table);
//						Object[] readObjs = sqlReader.nextRecord();
//						while (readObjs != null) {
//							readObjs = sqlReader.nextRecord();
//						}
//						sqlReader.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					
//				}
//			};
//			new Thread(task).start();
//			SQLWriter sqlWriter = new SQLWriter(new File(dataBaseUrl), table, true);
//			Vector<FormatField> oVec = new Vector<FormatField>();
//			oVec.add(new SQLField("ChapeauID", "integer", 10));
//			oVec.add(new SQLField("ChapeauName", "varchar", 30));
////			sqlWriter.setFields(oVec);
//			for (int i = 0; i < 10000; i++) {
//				Object[] objs = new Object[2];
//				objs[0] = 1;
//				objs[1] = "Mon chapeau";
//				sqlWriter.addRecord(objs);
//				objs[0] = 2;
//				objs[1] = "Mon beau chapeau";
//				sqlWriter.addRecord(objs);
//				objs[0] = 3;
//				objs[1] = "Mon beau chapeau pointu";
//				sqlWriter.addRecord(objs);
//			}
//			sqlWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	
}
