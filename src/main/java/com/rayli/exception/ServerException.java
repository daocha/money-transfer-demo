package com.rayli.exception;

/**
 * @author Ray LI
 * @date 5 Feb 2020
 * @company ray@dcha.xyz
 */
public class ServerException extends RuntimeException {

	private static final long serialVersionUID = -1386439949668357851L;

	String errorMsg;

	public ServerException(String error) {
		this.errorMsg = error;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

}
