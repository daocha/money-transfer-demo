package com.rayli.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Server Response
 * 
 * @author Ray LI
 * @date 5 Feb 2020
 * @company ray@dcha.xyz
 */
@JsonInclude(Include.NON_NULL)
public class Response implements Serializable {

	private static final long serialVersionUID = 3350500516144022330L;

	private boolean success;

	private String error;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
