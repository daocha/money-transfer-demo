package com.rayli.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.networknt.service.SingletonServiceFactory;
import com.rayli.connection.DBConnection;
import com.rayli.exception.ServerException;
import com.rayli.model.Account;
import com.rayli.model.Transaction;
import com.rayli.model.Transaction.Type;

/**
 * @author Ray LI
 * @date 4 Feb 2020
 * @company ray@dcha.xyz
 */
public class TransferServiceTest {

	private TransactionService transactionService = SingletonServiceFactory.getBean(TransactionService.class);

	DBConnection connection = transactionService.getConnectionInstance();

	Account mockAccountA;
	Account mockAccountB;

	@Before
	public void mockData() {
		mockAccountA = new Account();
		mockAccountA.setCurrency("USD");
		mockAccountA.setBalance(BigDecimal.ZERO);

		mockAccountB = new Account();
		mockAccountB.setCurrency("USD");
		mockAccountB.setBalance(BigDecimal.ZERO);

		connection.switchJUnitTestMode();

		// initialize shared connection
		try {
			connection.getConnection();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// simulate adding a new account to db
		mockAccountA.setId(transactionService.addAccount("1", mockAccountA));
		mockAccountB.setId(transactionService.addAccount("2", mockAccountB));
	}

	@After
	public void cleanup() {
		try {
			connection.clearSharedConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Test backend logic for api /account
	 */
	@Test
	public void getGetAccount() {

		// retrieve the new account
		Account acc = transactionService.getAccount(mockAccountA.getId());

		assertTrue(acc != null);
		assertEquals("USD", acc.getCurrency());
		assertEquals("", 0.0d, acc.getBalance().doubleValue(), 0.0d);
		assertEquals("1", acc.getVersion());

	}

	/**
	 * Test backend logic for api /deposit & /withdraw
	 */
	@Test
	public void testDepositWithdraw() {
		// deposit $1500, balance $1500
		boolean success = transactionService.depositWithdraw(Type.DEPOSIT, mockAccountA.getId(),
				BigDecimal.valueOf(1500.0d));
		assertTrue(success);
		Account accA = transactionService.getAccount(mockAccountA.getId());
		assertEquals("", 1500.0d, accA.getBalance().doubleValue(), 0.0d);

		// withdraw $500, balance $1000
		success = transactionService.depositWithdraw(Type.WITHDRAW, mockAccountA.getId(), BigDecimal.valueOf(500.0d));
		assertTrue(success);

		// balance: $1500 - $500 = $1000
		accA = transactionService.getAccount(mockAccountA.getId());
		assertEquals("", 1000.0d, accA.getBalance().doubleValue(), 0.0d);

		// test depositing negative number
		try {
			success = transactionService.depositWithdraw(Type.DEPOSIT, mockAccountA.getId(),
					BigDecimal.valueOf(-1500.0d));
			assertTrue(false);
		} catch (ServerException e) {
			assertTrue(true);
		}

		// test withdrawing negative number
		try {
			success = transactionService.depositWithdraw(Type.WITHDRAW, mockAccountA.getId(),
					BigDecimal.valueOf(-500.0d));
			assertTrue(false);
		} catch (ServerException e) {
			assertTrue(true);
		}

		// withdraw $1500, not enough money
		try {
			success = transactionService.depositWithdraw(Type.WITHDRAW, mockAccountA.getId(),
					BigDecimal.valueOf(1500.0d));
			assertTrue(false);
		} catch (ServerException e) {
			assertTrue(true);
		}
	}

	/**
	 * Test backend logic for api /transfer
	 */
	@Test
	public void testTransfer() throws InterruptedException {

		// A deposits $1500, balance $1500
		boolean success = transactionService.depositWithdraw(Type.DEPOSIT, mockAccountA.getId(),
				BigDecimal.valueOf(1500.0d));
		assertTrue(success);
		Account accA = transactionService.getAccount(mockAccountA.getId());
		assertEquals("", 1500.0d, accA.getBalance().doubleValue(), 0.0d);

		// B deposits $5000, balance $5000
		success = transactionService.depositWithdraw(Type.DEPOSIT, mockAccountB.getId(), BigDecimal.valueOf(5000.0d));
		assertTrue(success);
		Account accB = transactionService.getAccount(mockAccountB.getId());
		assertEquals("", 5000.0d, accB.getBalance().doubleValue(), 0.0d);

		Thread.sleep(10);

		// A transfer $500 to B, success, A balance: $1000, B balance: $5500
		success = transactionService.transfer(mockAccountB.getId(), mockAccountA.getId(), "USD",
				BigDecimal.valueOf(500.0d));
		assertTrue(success);

		accA = transactionService.getAccount(mockAccountA.getId());
		assertEquals("", 1000.0d, accA.getBalance().doubleValue(), 0.0d);

		accB = transactionService.getAccount(mockAccountB.getId());
		assertEquals("", 5500.0d, accB.getBalance().doubleValue(), 0.0d);

		Thread.sleep(10);

		// B transfer $5000 to A, success, A balance: $6000, B balance: $500
		success = transactionService.transfer(mockAccountA.getId(), mockAccountB.getId(), "USD",
				BigDecimal.valueOf(5000.0d));
		assertTrue(success);

		accA = transactionService.getAccount(mockAccountA.getId());
		assertEquals("", accA.getBalance().doubleValue(), 6000.0d, 0.0d);

		accB = transactionService.getAccount(mockAccountB.getId());
		assertEquals("", accB.getBalance().doubleValue(), 500.0d, 0.0d);

		List<Transaction> transactionsA = transactionService.getTransactions(mockAccountA.getId());
		List<Transaction> transactionsB = transactionService.getTransactions(mockAccountB.getId());

		// latest transaction: B transfer $5000 to A, success,
		// A balance: $6000
		// B balance: $500
		assertEquals(Type.TRANSFER, transactionsA.get(0).getType());
		assertEquals("", 5000.0d, transactionsA.get(0).getAmount().doubleValue(), 0.0d);
		assertEquals(mockAccountA.getId(), transactionsA.get(0).getCreditAccountId());
		assertEquals(mockAccountB.getId(), transactionsA.get(0).getDebitAccountId());
		assertEquals("", 6000.0d, transactionsA.get(0).getBalance().doubleValue(), 0.0d);

		assertEquals(Type.TRANSFER, transactionsB.get(0).getType());
		assertEquals("", 5000.0d, transactionsB.get(0).getAmount().doubleValue(), 0.0d);
		assertEquals(mockAccountA.getId(), transactionsB.get(0).getCreditAccountId());
		assertEquals(mockAccountB.getId(), transactionsB.get(0).getDebitAccountId());
		assertEquals("", 500.0d, transactionsB.get(0).getBalance().doubleValue(), 0.0d);

		// second transaction: A transfer $500 to B, success,
		// A balance: $1000
		// B balance: $5500
		assertEquals(Type.TRANSFER, transactionsA.get(1).getType());
		assertEquals("", 500.0d, transactionsA.get(1).getAmount().doubleValue(), 0.0d);
		assertEquals(mockAccountB.getId(), transactionsA.get(1).getCreditAccountId());
		assertEquals(mockAccountA.getId(), transactionsA.get(1).getDebitAccountId());
		assertEquals("", 1000.0d, transactionsA.get(1).getBalance().doubleValue(), 0.0d);

		assertEquals(Type.TRANSFER, transactionsB.get(1).getType());
		assertEquals("", 500.0d, transactionsB.get(1).getAmount().doubleValue(), 0.0d);
		assertEquals(mockAccountB.getId(), transactionsB.get(1).getCreditAccountId());
		assertEquals(mockAccountA.getId(), transactionsB.get(1).getDebitAccountId());
		assertEquals("", 5500.0d, transactionsB.get(1).getBalance().doubleValue(), 0.0d);

		// first transaction: A deposits $1500, B deposits $5000,
		// A balance $1500
		// B balance $5000
		assertEquals(Type.DEPOSIT, transactionsA.get(2).getType());
		assertEquals("", 1500.0d, transactionsA.get(2).getAmount().doubleValue(), 0.0d);
		assertEquals(mockAccountA.getId(), transactionsA.get(2).getCreditAccountId());
		assertEquals("", 1500.0d, transactionsA.get(2).getBalance().doubleValue(), 0.0d);

		assertEquals(Type.DEPOSIT, transactionsB.get(2).getType());
		assertEquals("", 5000.0d, transactionsB.get(2).getAmount().doubleValue(), 0.0d);
		assertEquals(mockAccountB.getId(), transactionsB.get(2).getCreditAccountId());
		assertEquals("", 5000.0d, transactionsB.get(2).getBalance().doubleValue(), 0.0d);

		// A transfer $20000 to B, fail
		try {
			transactionService.transfer(mockAccountB.getId(), mockAccountA.getId(), "USD",
					BigDecimal.valueOf(20000.0d));
			assertTrue(false);
		} catch (ServerException e) {
			assertTrue(true);
		}

		// test negative number
		try {
			transactionService.transfer(mockAccountB.getId(), mockAccountA.getId(), "USD", BigDecimal.valueOf(-100.0d));
			assertTrue(false);
		} catch (ServerException e) {
			assertTrue(true);
		}

		// test negative number
		try {
			transactionService.transfer(mockAccountA.getId(), mockAccountB.getId(), "USD", BigDecimal.valueOf(-100.0d));
			assertTrue(false);
		} catch (ServerException e) {
			assertTrue(true);
		}

	}

}
