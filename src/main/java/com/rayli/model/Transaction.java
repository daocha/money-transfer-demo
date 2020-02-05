package com.rayli.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Transaction Table
 * 
 * @author Ray LI
 * @date 4 Feb 2020
 * @company ray@dcha.xyz
 */
@JsonInclude(Include.NON_NULL)
public class Transaction implements Serializable {

	private static final long serialVersionUID = -578219325682946024L;

	public static enum Type {
		DEPOSIT,
		WITHDRAW,
		TRANSFER
	}

	/**
	 * Transaction id
	 */
	protected String id;

	protected Type type;

	/**
	 * Owner {@link Account#id}
	 */
	protected String ownerAccountId;

	/**
	 * {@link Account#id} of 'Credit To'
	 */
	protected String creditAccountId;

	/**
	 * {@link Account#id} of 'Debit From'
	 */
	protected String debitAccountId;

	/**
	 * Transaction amount
	 */
	protected BigDecimal amount;

	/**
	 * Transaction currency
	 */
	protected String currency;

	/**
	 * Transaction time
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS Z")
	protected Date time;

	/*
	 * Post transaction balance
	 */
	protected BigDecimal balance;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getOwnerAccountId() {
		return ownerAccountId;
	}

	public void setOwnerAccountId(String ownerAccountId) {
		this.ownerAccountId = ownerAccountId;
	}

	public String getCreditAccountId() {
		return creditAccountId;
	}

	public void setCreditAccountId(String creditAccountId) {
		this.creditAccountId = creditAccountId;
	}

	public String getDebitAccountId() {
		return debitAccountId;
	}

	public void setDebitAccountId(String debitAccountId) {
		this.debitAccountId = debitAccountId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

}
