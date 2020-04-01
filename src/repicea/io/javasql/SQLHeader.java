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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import repicea.io.FormatHeader;

public class SQLHeader extends FormatHeader<SQLField> {

	protected String fieldListString;
	
	protected SQLHeader() {
		super();
	}

	protected void read(Statement stmt, String table) throws SQLException {
		ResultSet rs = stmt.executeQuery("select * from " + table);
		ResultSetMetaData rsmd = rs.getMetaData();
	    int numColumns = rsmd.getColumnCount();
	    for (int i = 1; i < numColumns+1; i++) {
	        String fieldName = rsmd.getColumnName(i);
//	        String className = rsmd.getColumnClassName(i);
	        String columnTypeName = rsmd.getColumnTypeName(i);
	        int length = rsmd.getPrecision(i);
//	        int fieldType = rsmd.getColumnType(i);
	        addField(new SQLField(fieldName, columnTypeName, length));
	    }

	    rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);

	    // Get the number of rows from the result set
	    rs.next();
	    int rowcount = rs.getInt(1);
	    setNumberOfRecords(rowcount);
	}
	
	protected String getFieldListString() {
		if (fieldListString == null) {
			fieldListString = "(";
			for (int i = 0; i < getNumberOfFields(); i++) {
				fieldListString += getField(i).getName();
				if (i == getNumberOfFields() - 1) {
					fieldListString += ")";
				} else {
					fieldListString += ", ";
				}
			}
		}
		return fieldListString;
	}
	
	@Override
	protected int getNumberOfRecords() {return super.getNumberOfRecords();}
	
	@Override
	protected void setNumberOfRecords(int numberOfRecords) {super.setNumberOfRecords(numberOfRecords);}

}
