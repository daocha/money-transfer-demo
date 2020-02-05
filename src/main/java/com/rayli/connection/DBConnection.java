package com.rayli.connection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection related
 * 
 * @author Ray LI
 * @date 4 Feb 2020
 * @company ray@dcha.xyz
 */
public interface DBConnection {

	/**
	 * Obtain database connection
	 */
	Connection getConnection() throws SQLException;

	/**
	 * In case connection is shared, to close and clear shared connection
	 */
	void clearSharedConnection() throws SQLException;

	void setJUnitTest();

	boolean isJUnitTest();

}
