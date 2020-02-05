package com.rayli.connection;

import java.sql.Connection;
import java.sql.SQLException;

import com.networknt.service.SingletonServiceFactory;

/**
 * @author Ray LI
 * @date 5 Feb 2020
 * @company ray@dcha.xyz
 */
public class UtilsConnection {

	/**
	 * Check if 'commit' action is enabled, invoke conn.commit()
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public static void commit(Connection conn) throws SQLException {

		DBConnection connection = SingletonServiceFactory.getBean(DBConnection.class);

		if (!connection.isJUnitTest()) {
			conn.commit();
		}

	}

	/**
	 * invoke conn.rollback()
	 */
	public static void rollback(Connection conn) throws SQLException {
		conn.rollback();
	}

	public static void close(Connection conn) throws SQLException {
		DBConnection connection = SingletonServiceFactory.getBean(DBConnection.class);
		if (!connection.isJUnitTest() && conn != null) {
			conn.close();
		}
	}

}
