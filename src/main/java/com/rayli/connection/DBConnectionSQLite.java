package com.rayli.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SQLite instance
 * 
 * @author Ray LI
 * @date 5 Feb 2020
 * @company ray@dcha.xyz
 */
public class DBConnectionSQLite implements DBConnection {

	private static final String DB_FILE = "bank.db";

	private boolean junitTest = false;

	private Connection conn;

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(DB_FILE);
	}

	private Connection getConnection(final String dbfile) throws SQLException {
		String url = String.format("jdbc:sqlite:%s", dbfile);

		Connection conn = null;
		try {
			if (junitTest) {
				conn = this.conn;
			}

			if (conn == null) {
				conn = DriverManager.getConnection(url);
			}

			if (junitTest) {
				this.conn = conn;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		if (conn != null) {
			// disable auto commit
			conn.setAutoCommit(false);
		}

		return conn;
	}

	@Override
	public void setJUnitTest() {
		this.junitTest = true;
	}

	@Override
	public boolean isJUnitTest() {
		return this.junitTest;
	}

	@Override
	public void clearSharedConnection() throws SQLException {
		if (this.conn != null) {
			this.conn.close();
			this.conn = null;
		}
	}

}
