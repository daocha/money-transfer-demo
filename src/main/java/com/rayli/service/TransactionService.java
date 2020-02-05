package com.rayli.service;

import java.math.BigDecimal;
import java.util.List;

import com.rayli.connection.DBConnection;
import com.rayli.model.Account;
import com.rayli.model.Transaction;
import com.rayli.model.Transaction.Type;

/**
 * Service handling account related
 * 
 * @author Ray LI
 * @date 4 Feb 2020
 * @company ray@dcha.xyz
 */
public interface TransactionService {

	DBConnection getConnectionInstance();

	/**
	 * insert an account into DB
	 * 
	 * @return generated {@link Account#id}
	 */
	String addAccount(String userId, Account account);

	/**
	 * Get account balance
	 * 
	 * @param userid
	 *            {@link Account#id}
	 * @return amount wraper of {@link Account#balance}
	 */
	Account getAccount(String accountId);

	/**
	 * deposit/withdraw money into/from account (Assume user can only
	 * deposit/withdraw USD into/from USD account, GBP into/from GBP account)
	 * 
	 * @param accountId
	 *            {@link Account#id}
	 * @param amount
	 *            value to deposit
	 * @return true if success
	 */
	boolean depositWithdraw(Type type, String accountId, BigDecimal amount);

	/**
	 * Retrieve all the transaction records
	 * 
	 * @param accountId
	 *            {@link Account#id}
	 * @return
	 */
	List<Transaction> getTransactions(String accountId);

	/**
	 * Transfer from one account to the other account
	 * 
	 * @param creditAccountId
	 *            `To` account id
	 * @param debitAccountId
	 *            `From` account id
	 * @param currency
	 * @param amount
	 * @return true if success
	 */
	boolean transfer(String creditAccountId, String debitAccountId, String currency, BigDecimal amount);

}
