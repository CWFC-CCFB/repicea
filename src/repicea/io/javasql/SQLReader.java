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
	private Statement statement;
	private ResultSet resultSet;
	
	/**
	 * General constructor.
	 * @param dataBaseUrl the database URL
	 * @param table the table to be read
	 * @throws IOException if an I/O error has occurred
	 */
	public SQLReader(String dataBaseUrl, String table) throws IOException {
		super(dataBaseUrl);
		try {
			DatabaseConnectionManager.registerConnectionUser(this, dataBaseUrl);
			dbConnection = DatabaseConnectionManager.getUserConnection(this);
			statement = dbConnection.createStatement();
			this.table = table;
			setFormatHeader(new SQLHeader());
			getHeader().read(statement, table);
			linePointer = 0;
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			close();
			throw new IOException("Unable to connect to the database!" + e);
		}
	}

	@Override
	public void reset() throws IOException {
		resultSet = null;		// the resultSet is set to null so that the next call to nextRecord(int) will re-instantiate this member 
		linePointer = 0;
		isClosed = false;
	}
	
	@Override
	public void closeInternalStream() {
		try {
			DatabaseConnectionManager.removeUser(this);
		} catch (SQLException e) {
			System.out.println("Error while closing the database!");
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
					linePointer++;
				}
			}
			
			int numberOfFields = getHeader().getNumberOfFields();
			Object[] objs = new Object[numberOfFields];
			for (int i = 0; i < numberOfFields; i++) {
				objs[i] = resultSet.getObject(i + 1);
				if (objs[i] == null) {
					if (getHeader().getField(i).getTypeName().toLowerCase().equals("varchar")) { // patch because empty strings are returned as null
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

	/**
	 * Returns the row index at which the reader is. The indices starts from 1.
	 * @return an integer
	 */
	public int getRowIndex() {
		return linePointer + 1;
	}

	
	
}
