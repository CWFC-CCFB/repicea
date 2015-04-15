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

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import repicea.io.FormatReader;

/**
 * The SQLReader class makes it possible to read tables in 
 * Microsoft Access databases (*.mdb, *.accdb).
 * @author Mathieu Fortin - July 2012
 */
public class SQLReader extends FormatReader<SQLHeader> {

	private Connection dbConnection;
	private String table;
	private int rowIndex;
	private Statement statement;
	private ResultSet resultSet;
	
	/**
	 * General constructor.
	 * @param dataBaseUrl the database URL
	 * @param table the table to be read
	 * @throws IOException
	 */
	public SQLReader(String dataBaseUrl, String table) throws IOException {
		super(dataBaseUrl);
		try {
			DatabaseConnectionManager.registerConnectionUser(this, dataBaseUrl);
//			dbConnection = DatabaseConnector.getConnectionFromThisMSACCESSDataBase(dataBaseUrl)	
			dbConnection = DatabaseConnectionManager.getUserConnection(this);
			statement = dbConnection.createStatement();
			this.table = table;
			setFormatHeader(new SQLHeader());
			getHeader().read(statement, table);
			rowIndex = 1;
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			close();
			throw new IOException("Unable to connect to the database!" + e);
		}
	}
	
	@Override
	public void close() throws IOException {
		try {
			DatabaseConnectionManager.removeUser(this);
//			if (dbConnection != null && !dbConnection.isClosed()) {
//				dbConnection.close();
//			}
		} catch (SQLException e) {
			throw new IOException("Error while closing the database!" + e);
		}
	}
	
	@Override
	public Object[] nextRecord(int skipThisNumberOfLines) throws IOException {
		try {
			if (resultSet == null) {
				statement = dbConnection.createStatement();
				resultSet = statement.executeQuery("SELECT * FROM " + table);
			}
			
			int numberOfLinesSkipped = 0;

			while (numberOfLinesSkipped++ <= skipThisNumberOfLines) {
				if (!resultSet.next()) {
					return null;
				} else {
					rowIndex++;
				}
			}
			
			int numberOfFields = getHeader().getNumberOfFields();
			Object[] objs = new Object[numberOfFields];
			for (int i = 0; i < numberOfFields; i++) {
				objs[i] = resultSet.getObject(i + 1);
				if (objs[i] == null) {
					if (getHeader().getField(i).getType() == java.sql.Types.VARCHAR) {		// patch because empty strings are returned as null
						objs[i] = "";
					}
				}
			}
			return objs;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public int getRow() {
		return rowIndex;
	}
	
	public static void main(String[] args) throws IOException {
		String url = "D:/Travail/MRNF - Projets/Lac.mdb";
		for (int i = 0; i < 1000; i++) {
			System.out.println(((Integer) i).toString());
			SQLReader reader = new SQLReader(url, "2_Tiges");
			Object[] objs = reader.nextRecord();
			while (objs != null) {
				objs = reader.nextRecord();
			}
		}
	}
	
	
}
