package com.rayli.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * User model
 * 
 * @author Ray LI
 * @date 4 Feb 2020
 * @company ray@dcha.xyz
 */
@JsonInclude(Include.NON_NULL)
public class User implements Serializable {

	private static final long serialVersionUID = 3510711978806026453L;

	/**
	 * User ID
	 */
	protected String id;

	/**
	 * Username
	 */
	protected String username;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
