package com.rayli.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.networknt.service.SingletonServiceFactory;
import com.rayli.connection.DBConnection;
import com.rayli.connection.UtilsConnection;
import com.rayli.exception.ServerException;
import com.rayli.model.Account;
import com.rayli.model.Transaction;
import com.rayli.model.Transaction.Type;
import com.rayli.model.User;
import com.rayli.utils.Utils;

/**
 * @author Ray LI
 * @date 4 Feb 2020
 * @company ray@dcha.xyz
 */
public class TransactionServiceImpl implements TransactionService {

	private DBConnection connection = SingletonServiceFactory.getBean(DBConnection.class);

	@Override
	public String addAccount(String userId, Account account) {
		final String sql = String.format(
				"INSERT INTO `ACCOUNT`(`userid`, `currency`, `balance`) values ('%s', '%s', %f);", userId,
				account.getCurrency(), 0d);

		Connection conn = null;
		try {
			conn = connection.getConnection();
			try (Statement stmt = conn.createStatement()) {

				// this is not supported by SQLite
				/*
				 * int inserted = stmt.executeUpdate(sql,
				 * Statement.RETURN_GENERATED_KEYS);
				 */

				int inserted = stmt.executeUpdate(sql);
				ResultSet autogenKey = conn.createStatement().executeQuery("SELECT last_insert_rowid();");
				String generatedAccId = null;
				if (autogenKey.next()) {
					generatedAccId = autogenKey.getString(1);
				}

				if (inserted > 0) {
					UtilsConnection.commit(conn);
					return generatedAccId;
				} else {
					UtilsConnection.rollback(conn);
				}

			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			try {
				UtilsConnection.rollback(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} finally {
			try {
				UtilsConnection.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public Account getAccount(String accountId) {
		final String sql = "SELECT A.`id` as `accountId`, A.`currency`, A.`balance`, A.`version`, U.`id` as `userId`, "
				+ "U.`username` as `username` FROM `ACCOUNT` A JOIN `USER` U ON A.`userId` = U.`id` WHERE A.`id` = ? ";

		Account account = null;
		Connection conn = null;
		try {
			conn = connection.getConnection();
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setString(1, accountId);
				ResultSet rs = pstmt.executeQuery();

				// loop through the result set
				if (rs.next()) {
					// prepare Account object
					account = new Account();
					account.setId(rs.getString("accountId"));
					account.setCurrency(rs.getString("currency"));
					account.setBalance(rs.getBigDecimal("balance"));
					account.setVersion(rs.getString("version"));

					// prepare User object
					User user = new User();
					user.setId(rs.getString("userId"));
					user.setUsername(rs.getString("username"));

					account.setOwner(user);
				}

			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				UtilsConnection.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return account;
	}

	@Override
	public boolean depositWithdraw(Type type, String accountId, BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServerException(String.format("Amount should be POSITIVE: %f", amount));
		}

		// insert record to `TRANSACTION` table
		final String insertTransSql;
		if (type == Type.DEPOSIT) {
			insertTransSql = "INSERT INTO `TRANSACTION`(`type`, `ownerAccountId`, `creditAccountId`, `currency`, `amount`, `balance`) "
					+ "SELECT ?, ?, A.id, A.currency, ?, ? FROM `ACCOUNT` A WHERE A.`id` = ? and A.`version` = ?;";
		} else if (type == Type.WITHDRAW) {
			insertTransSql = "INSERT INTO `TRANSACTION`(`type`, `ownerAccountId`,`debitAccountId`, `currency`, `amount`, `balance`) "
					+ "SELECT ?, ?,  A.id, A.currency, ?, ? FROM `ACCOUNT` A WHERE A.`id` = ? and A.balance >= ? and A.`version` = ?;";

		} else {
			throw new IllegalArgumentException(String.format("Unsupported transaction type: %s", type.toString()));
		}

		// query the current balance & version from `ACCOUNT` table
		final String querySql = "SELECT A.`currency`, A.`balance`, A.`version` FROM `ACCOUNT` A WHERE A.`id` = ? ";

		// update balance in `ACCOUNT` table
		final String updateAccSql = "UPDATE `ACCOUNT` SET `balance`= ?, `version` = `version` + 1 WHERE `id` = ? and version = ?;";

		Account account = null;

		Connection conn = null;

		try {
			conn = connection.getConnection();
			try (PreparedStatement query_pstmt = conn.prepareStatement(querySql)) {
				// query Account

				query_pstmt.setString(1, accountId);
				ResultSet rs = query_pstmt.executeQuery();
				if (rs.next()) {
					account = new Account();
					account.setCurrency(rs.getString("currency"));
					account.setBalance(rs.getBigDecimal("balance"));
					account.setVersion(rs.getString("version"));
				}

				int insertedCount = 0;
				int updatedCount = 0;

				if (account != null) {
					BigDecimal newBalance;
					if (type == Type.DEPOSIT) {
						newBalance = account.getBalance().add(amount);
					} else {
						newBalance = account.getBalance().subtract(amount);
					}

					// additional checking
					if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
						throw new ServerException(String.format(
								"Operation aborted, attempted to withdraw %f , but balance is not enough: %f", amount,
								account.getBalance()));
					}

					PreparedStatement insertTrans_pstmt = conn.prepareStatement(insertTransSql);
					if (type == Type.DEPOSIT) {
						insertTrans_pstmt.setString(1, Transaction.Type.DEPOSIT.toString());
						insertTrans_pstmt.setString(2, accountId);
						insertTrans_pstmt.setBigDecimal(3, amount);
						insertTrans_pstmt.setBigDecimal(4, newBalance);
						insertTrans_pstmt.setString(5, accountId);
						insertTrans_pstmt.setString(6, account.getVersion());
					} else {
						insertTrans_pstmt.setString(1, Transaction.Type.WITHDRAW.toString());
						insertTrans_pstmt.setString(2, accountId);
						insertTrans_pstmt.setBigDecimal(3, amount);
						insertTrans_pstmt.setBigDecimal(4, newBalance);
						insertTrans_pstmt.setString(5, accountId);
						insertTrans_pstmt.setBigDecimal(6, amount);
						insertTrans_pstmt.setString(7, account.getVersion());
					}
					insertedCount += insertTrans_pstmt.executeUpdate();

					PreparedStatement updateAcc_pstmt = conn.prepareStatement(updateAccSql);
					updateAcc_pstmt.setBigDecimal(1, newBalance);
					updateAcc_pstmt.setString(2, accountId);
					updateAcc_pstmt.setString(3, account.getVersion());
					updatedCount += updateAcc_pstmt.executeUpdate();

				} else {
					throw new ServerException(String.format("Can not find account with id: %s", accountId));
				}

				// commit only if both statements are inserted/updated
				// successfully simultaneously
				if (insertedCount == 1 && updatedCount == 1) {
					UtilsConnection.commit(conn);
				} else {
					UtilsConnection.rollback(conn);
					return false;
				}

				return true;

			}

		} catch (SQLException e1) {
			e1.printStackTrace();
			try {
				UtilsConnection.rollback(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} finally {
			try {
				UtilsConnection.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	@Override
	public boolean transfer(String creditAccountId, String debitAccountId, String currency, BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServerException(String.format("Amount should be POSITIVE: %f", amount));
		}

		// query the current balance & version from `ACCOUNT` table
		final String queryCreditSql = "SELECT A.`currency`, A.`balance`,  A.`version` FROM `ACCOUNT` A WHERE A.`id` = ? ";
		final String queryDebitSql = "SELECT A.`currency`, A.`balance`,  A.`version` FROM `ACCOUNT` A WHERE A.`id` = ? ";

		// insert CREDIT record to `TRANSACTION` table
		final String insertCreditTransSql = "INSERT INTO `TRANSACTION`(`type`, `ownerAccountId`, `creditAccountId`, `debitAccountId`, `currency`, `amount`, `balance`) "
				+ "SELECT ?, A.id, A.id, ?, A.currency, ?, ? FROM `ACCOUNT` A WHERE A.`id` = ? and A.`version` = ?;";

		// insert DEBIT record to `TRANSACTION` table
		final String insertDebitTransSql = "INSERT INTO `TRANSACTION`(`type`, `ownerAccountId`, `creditAccountId`, `debitAccountId`, `currency`, `amount`, `balance`) "
				+ "SELECT ?, A.id, ?, A.id, A.currency, ?, ? FROM `ACCOUNT` A WHERE A.`id` = ? and A.balance >= ? and A.`version` = ?;";

		// update balance in `ACCOUNT` table
		final String updateCreditAccSql = "UPDATE `ACCOUNT` SET `balance`= ?,  `version` = `version` + 1 WHERE `id` = ? and version = ?;";
		final String updateDebitAccSql = "UPDATE `ACCOUNT` SET `balance`= ?,  `version` = `version` + 1 WHERE `id` = ? and version = ?;";

		Account creditAccount = null;
		Account debitAccount = null;
		Connection conn = null;
		try {
			conn = connection.getConnection();
			try (PreparedStatement credit_query_pstmt = conn.prepareStatement(queryCreditSql);
					PreparedStatement debit_query_pstmt = conn.prepareStatement(queryDebitSql);
					PreparedStatement credit_insertTrans_pstmt = conn.prepareStatement(insertCreditTransSql);
					PreparedStatement credit_updateAcc_pstmt = conn.prepareStatement(updateCreditAccSql);
					PreparedStatement debit_insertTrans_pstmt = conn.prepareStatement(insertDebitTransSql);
					PreparedStatement debit_updateAcc_pstmt = conn.prepareStatement(updateDebitAccSql);) {
				// query credit Account
				{

					credit_query_pstmt.setString(1, creditAccountId);
					ResultSet rs = credit_query_pstmt.executeQuery();
					if (rs.next()) {
						creditAccount = new Account();
						creditAccount.setCurrency(rs.getString("currency"));
						creditAccount.setBalance(rs.getBigDecimal("balance"));
						creditAccount.setVersion(rs.getString("version"));
					}
				}

				// query debit Account
				{

					debit_query_pstmt.setString(1, debitAccountId);
					ResultSet rs = debit_query_pstmt.executeQuery();
					if (rs.next()) {
						debitAccount = new Account();
						debitAccount.setCurrency(rs.getString("currency"));
						debitAccount.setBalance(rs.getBigDecimal("balance"));
						debitAccount.setVersion(rs.getString("version"));
					}
				}

				int insertedCreditCount = 0;
				int updatedCreditCount = 0;
				int insertedDebitCount = 0;
				int updatedDebitCount = 0;

				if (creditAccount != null) {
					BigDecimal newBalance = creditAccount.getBalance().add(amount);

					credit_insertTrans_pstmt.setString(1, Transaction.Type.TRANSFER.toString());
					credit_insertTrans_pstmt.setString(2, debitAccountId);
					credit_insertTrans_pstmt.setBigDecimal(3, amount);
					credit_insertTrans_pstmt.setBigDecimal(4, newBalance);
					credit_insertTrans_pstmt.setString(5, creditAccountId);
					credit_insertTrans_pstmt.setString(6, creditAccount.getVersion());
					insertedCreditCount += credit_insertTrans_pstmt.executeUpdate();

					credit_updateAcc_pstmt.setBigDecimal(1, newBalance);
					credit_updateAcc_pstmt.setString(2, creditAccountId);
					credit_updateAcc_pstmt.setString(3, creditAccount.getVersion());
					updatedCreditCount += credit_updateAcc_pstmt.executeUpdate();

				} else {
					throw new ServerException(
							String.format("Can not find account with accountId: %s", creditAccountId));
				}

				if (debitAccount != null) {
					BigDecimal newBalance = debitAccount.getBalance().subtract(amount);

					// additional checking
					if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
						throw new ServerException(String.format(
								"Operation aborted, attempted to transfer %f from account: %s, but balance is not enough: %f",
								amount, debitAccountId, debitAccount.getBalance()));
					}

					debit_insertTrans_pstmt.setString(1, Transaction.Type.TRANSFER.toString());
					debit_insertTrans_pstmt.setString(2, creditAccountId);
					debit_insertTrans_pstmt.setBigDecimal(3, amount);
					debit_insertTrans_pstmt.setBigDecimal(4, newBalance);
					debit_insertTrans_pstmt.setString(5, debitAccountId);
					debit_insertTrans_pstmt.setBigDecimal(6, amount);
					debit_insertTrans_pstmt.setString(7, debitAccount.getVersion());
					insertedDebitCount += debit_insertTrans_pstmt.executeUpdate();

					debit_updateAcc_pstmt.setBigDecimal(1, newBalance);
					debit_updateAcc_pstmt.setString(2, debitAccountId);
					debit_updateAcc_pstmt.setString(3, debitAccount.getVersion());
					updatedDebitCount += debit_updateAcc_pstmt.executeUpdate();

				} else {
					throw new ServerException(String.format("Can not find account with id: %s", debitAccountId));
				}

				// commit only if both statements are inserted/updated
				// successfully simultaneously
				if (insertedCreditCount == 1 && insertedDebitCount == 1 && updatedCreditCount == 1
						&& updatedDebitCount == 1) {
					UtilsConnection.commit(conn);
				} else {
					UtilsConnection.rollback(conn);
					return false;
				}

				return true;
			}

		} catch (SQLException e1) {
			e1.printStackTrace();
			try {
				UtilsConnection.rollback(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} finally {
			try {
				UtilsConnection.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	@Override
	public List<Transaction> getTransactions(String accountId) {
		final String sql = "SELECT `id`, `type`, `time`, `creditAccountId`, `debitAccountId`, `currency`, `amount`, `balance` from"
				+ " `TRANSACTION` WHERE `ownerAccountId` = ? ORDER BY `id` DESC LIMIT 0,100;";

		List<Transaction> list = new ArrayList<>();

		Connection conn = null;
		try {
			conn = connection.getConnection();
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setString(1, accountId);
				ResultSet rs = pstmt.executeQuery();

				// loop through the result set
				while (rs.next()) {
					// prepare Transaction object
					Transaction trans = new Transaction();
					trans.setId(rs.getString("id"));
					trans.setType(Type.valueOf(rs.getString("type")));
					trans.setTime(Utils.parseDatabaseTime(rs.getString("time")));
					trans.setCreditAccountId(rs.getString("creditAccountId"));
					trans.setDebitAccountId(rs.getString("debitAccountId"));
					trans.setCurrency(rs.getString("currency"));
					trans.setAmount(rs.getBigDecimal("amount"));
					trans.setBalance(rs.getBigDecimal("balance"));
					list.add(trans);
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				UtilsConnection.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	@Override
	public DBConnection getConnectionInstance() {
		return connection;
	}

}
