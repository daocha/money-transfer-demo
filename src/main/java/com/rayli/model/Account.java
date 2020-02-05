package com.rayli.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Account Table
 * 
 * @author Ray LI
 * @date 4 Feb 2020
 * @company ray@dcha.xyz
 */
@JsonInclude(Include.NON_NULL)
public class Account implements Serializable {

	private static final long serialVersionUID = -1668114823984510239L;

	/**
	 * Account id
	 */
	protected String id;

	/**
	 * {@link User}
	 */
	protected User owner;

	/**
	 * Account base currency
	 */
	protected String currency;

	/**
	 * Balance value
	 */
	protected BigDecimal balance;

	/**
	 * version of database record
	 */
	protected String version;

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
