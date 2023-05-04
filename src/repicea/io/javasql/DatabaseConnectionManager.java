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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import repicea.io.GFileFilter;
import repicea.io.GFileFilter.FileType;

/**
 * The DatabaseConnectionManager ensures that only one connection is made
 * to a particular database. It handles the closing of the connection as well.
 * @author Mathieu Fortin - November 2012
 */
public class DatabaseConnectionManager {

	private static Map<String, Connection> ConnectionUrls = new HashMap<String, Connection>();
	private static Map<Object, Connection> ConnectionUsers = new HashMap<Object, Connection>();
	
	private static final Object Lock = new Object();
	private static boolean isLocked = false;

	private static void lock() {
		synchronized(Lock) {
			while (isLocked) {
				try {
					Lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			isLocked = true;
		}
	}

	private static void unlock() {
		synchronized(Lock) {
			isLocked = false;
			Lock.notifyAll();
		}
	}
	
	/**
	 * This method first checks if the connection exists through its url. If it does not, 
	 * it creates the connection. Secondly, the method register the object as a user of
	 * the connection.
	 * @param user the object instance that uses the database connection
	 * @param dataBaseUrl the database url
	 * @throws IOException if there is a connection problem
	 */
 	public static void registerConnectionUser(Object user, String dataBaseUrl) throws IOException {
 		lock();
 		dataBaseUrl = new File(dataBaseUrl).getAbsolutePath();
 		Connection connection;
 		if (!doesConnectionExist(dataBaseUrl)) {
 	 		connection = getConnectionFromThisMSACCESSDataBase(dataBaseUrl);
 	 		ConnectionUrls.put(dataBaseUrl, connection);
 		} else {
 			connection = ConnectionUrls.get(dataBaseUrl);
 		}
 		if (!ConnectionUsers.containsKey(user)) {
 	 		ConnectionUsers.put(user, connection);
 		}
 		unlock();
	}
 	
 	private static boolean doesConnectionExist(String dataBaseUrl) {
 		return ConnectionUrls.containsKey(dataBaseUrl);
 	}
 	
 	/**
 	 * This method retrieves the connection associated with a particular user.
 	 * @param user the Object instance that uses this connection
 	 * @return a Connection instance
 	 */
 	public static Connection getUserConnection(Object user) {
 		lock();
 		Connection connection = ConnectionUsers.get(user);
 		unlock();
 		return connection;
 	}
 	
 	/**
 	 * This method removes the user from the ConnectionUsers Map and
 	 * eventually closes the connection if there is no user left.
 	 * @param user the Object instance that uses this connection
 	 * @throws SQLException if a database access error has occurred
 	 */
 	public static void removeUser(Object user) throws SQLException {
 		lock();
 		Connection connection = ConnectionUsers.remove(user);
 		if (!ConnectionUsers.containsValue(connection)) {	// no user of this connection
 			Entry<String, Connection> connectionEntry = null;
 			for (Entry<String, Connection> entry : ConnectionUrls.entrySet()) {
 				if (entry.getValue().equals(connection)) {
 					connectionEntry = entry;
 					break;
 				}
 			}
 			if (connectionEntry != null) {
 				ConnectionUrls.remove(connectionEntry.getKey());
 		 		connection.close();
 			}
 		}
 		unlock();
 	}
	
	/**
	 * This static method returns a Connection instance as a result of the connection process to the
	 * MS-ACCESS database.
	 * @param dataBaseUrl the url of the MS-ACCESS database
	 * @return a Connection instance
	 * @throws IOException
	 */
	private static Connection getConnectionFromThisMSACCESSDataBase(String dataBaseUrl) throws IOException {
		try {
//			ClassLoader.getSystemClassLoader().loadClass("net.ucanaccess.jdbc.UcanaccessDriver");
//			Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
			FileType f = GFileFilter.getFileType(dataBaseUrl);
			if (f == FileType.ACCDB || f == FileType.MDB) {
				String connectionString = "jdbc:ucanaccess://" + dataBaseUrl;
				String user = System.getProperty("user.name");
				return DriverManager.getConnection(connectionString, user, "");
			} else {
				throw new IOException("The SQLReader class supports only .mdb or .accdb file!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException("Error connecting to the database!");
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//			throw new IOException("Unable to load the odbc drivers!");
		}
	}

}
