package com.crio.jumbotail.assettracking.controller;

import com.crio.jumbotail.assettracking.exceptions.AssetNotFoundException;
import com.crio.jumbotail.assettracking.exceptions.InvalidFilterException;
import com.crio.jumbotail.assettracking.exceptions.JwtAuthException;
import com.crio.jumbotail.assettracking.exchanges.response.ExceptionWrapper;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

	@ExceptionHandler(ConversionFailedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ExceptionWrapper> handleConnversion(RuntimeException ex) {
		return new ResponseEntity<>(getFormattedData(ex), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(AssetNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ExceptionWrapper> handleAssetNotFound(RuntimeException ex) {
		return new ResponseEntity<>(getFormattedData(ex), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler( {JwtAuthException.class, InvalidFilterException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ExceptionWrapper> handleBadRequests(RuntimeException ex) {
		return new ResponseEntity<>(getFormattedData(ex), HttpStatus.BAD_REQUEST);
	}

	private ExceptionWrapper getFormattedData(Exception e) {
		String message;
		if (e.getCause() != null) {
			message = e.getCause().toString() + " " + e.getMessage();
		} else {
			message = e.getMessage();
		}
		return new ExceptionWrapper(message);

	}
}