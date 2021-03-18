package com.crio.jumbotail.assettracking.exceptions;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthException extends AuthenticationException {

	public JwtAuthException(String message) {
		super(message);
	}

	public JwtAuthException(String message, Throwable cause) {
		super(message, cause);
	}

}
