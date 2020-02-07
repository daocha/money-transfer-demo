package com.rayli.startup;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import com.networknt.server.Server;
import com.networknt.service.SingletonServiceFactory;
import com.rayli.connection.DBConnection;

/**
 * Server Endpoint, main class of the application
 * 
 * @author Ray LI
 * @date 4 Feb 2020
 * @company ray@dcha.xyz
 */
public class Application {

	private final static Logger log = Logger.getLogger(Application.class.getName());

	private final static DBConnection connection = SingletonServiceFactory.getBean(DBConnection.class);

	/**
	 * Connect to a sqlite database
	 *
	 * @param fileName
	 *            the database file name
	 */
	public static void createNewDatabase() {

		try (Connection conn = connection.getConnection()) {
			if (conn != null) {
				DatabaseMetaData meta = conn.getMetaData();
				log.info(String.format("The driver name is: %s ", meta.getDriverName()));
				log.info("Database is initialized...\n\n");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a new table in the sqlite database
	 *
	 */
	public static void createTables() {

		// SQL statement for creating `ACCOUNT`
		String accountTableSQL = "CREATE TABLE IF NOT EXISTS `ACCOUNT`(\n"
				+ "	`id` INTEGER PRIMARY KEY AUTOINCREMENT,\n" + "    `userId` INTEGER NOT NULL,\n"
				+ "    `currency` TEXT NOT NULL,\n" + "    `balance` NUMERIC DEFAULT 0,\n version INTEGER DEFAULT 1,\n"
				+ "    CONSTRAINT `USER_FK` FOREIGN KEY (`userId`) REFERENCES `USER` (`id`)\n" + ");";

		// SQL statement for creating `USER`
		String userTableSQL = "CREATE TABLE IF NOT EXISTS `USER`(\n" + "	`id` INTEGER PRIMARY KEY AUTOINCREMENT,\n"
				+ "    `username` TEXT NOT NULL UNIQUE);";

		// SQL statement for creating `TRANSACTION`
		String transactionTableSQL = "CREATE TABLE IF NOT EXISTS `TRANSACTION`(\n"
				+ "	`id` INTEGER PRIMARY KEY AUTOINCREMENT,\n    `ownerAccountId` INTEGER NOT NULL,\n"
				+ "    `creditAccountId` INTEGER NULL,\n" + "    `debitAccountId` INTEGER NULL,\n"
				+ "    `amount` NUMERIC NOT NULL,\n" + "   `type` TEXT NOT NULL, \n `currency` TEXT NOT NULL,\n"
				+ "    `time` TEXT DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')),\n"
				+ "    `balance` NUMERIC DEFAULT 0,\n"
				+ "    CONSTRAINT `ACC_FK0` FOREIGN KEY (`ownerAccountId`) REFERENCES `USER` (`id`),\n"
				+ "    CONSTRAINT `ACC_FK1` FOREIGN KEY (`creditAccountId`) REFERENCES `USER` (`id`),\n"
				+ "    CONSTRAINT `ACC_FK2` FOREIGN KEY (`debitAccountId`) REFERENCES `USER` (`id`)\n" + ");";

		try (Connection conn = connection.getConnection(); Statement stmt = conn.createStatement()) {
			// create new tables
			stmt.execute(accountTableSQL);
			log.info(String.format("Statement is executed: \n{ %s }\n\n", accountTableSQL));

			stmt.execute(userTableSQL);
			log.info(String.format("Statement is executed: \n{ %s }\n\n", userTableSQL));

			stmt.execute(transactionTableSQL);
			log.info(String.format("Statement is executed: \n{ %s }\n\n", transactionTableSQL));
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void createInitData() {
		final String sql = "INSERT INTO `USER`(`id`, `username`) VALUES ('1', 'ray'), ('2', 'chris');";
		final String sql2 = "INSERT INTO `ACCOUNT`(`id`, `userId`, `currency`, `balance`, `version`) VALUES ('1', '1', 'USD', '1000.0', '1'), "
				+ "('2', '2', 'USD', '5000.0', '1');";

		try (Connection conn = connection.getConnection(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
			stmt.executeUpdate(sql2);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

		if (args != null && args.length > 0) {

			if (args[0].equalsIgnoreCase("true")) {
				// initialize database
				createNewDatabase();

				// initialize table structure
				createTables();

				// add some init data
				createInitData();
			}
		}
		// start server
		Server.main(args);
	}
}
