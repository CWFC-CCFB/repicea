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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;


public class StatementMaker {

	private Connection connection;
	private Vector<String> fields;
	private Vector<Object> values;
	
	public StatementMaker(Connection connection) {
		this.connection = connection;
		fields = new Vector<String>();
		values = new Vector<Object>();
	}

	public void addVFieldAndValue(String field, Object value) {
		fields.add(field);
		values.add(value);
	}
	
	public void insertIntoTable(String table) throws SQLException {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			String valueList = "values (";
			String cmd = "INSERT INTO ";
			cmd += table;
			cmd += " (";
			for (int i = 0; i < fields.size(); i ++) {
				cmd += fields.get(i);
				Object value = values.get(i);
				if (value instanceof Number) {
					valueList += value.toString();
				} else {
					valueList += "'"+ value.toString() +"'";
				}
				if (i < fields.size() - 1) {
					cmd += ", ";
					valueList += ", ";
				} else {
					cmd += ") ";
					valueList += ")";
				}	
			}

			cmd = cmd + valueList;
			statement.execute(cmd);
		} catch (SQLException e) {
			throw e;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	
}
