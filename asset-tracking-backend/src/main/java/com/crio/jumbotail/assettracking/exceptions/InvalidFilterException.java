package com.crio.jumbotail.assettracking.exceptions;

public class InvalidFilterException extends RuntimeException {
	public InvalidFilterException(String message) {
		super(message);
	}

	public InvalidFilterException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidFilterException(Throwable cause) {
		super(cause);
	}
}
