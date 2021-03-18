package com.crio.jumbotail.assettracking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Autowired
	ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
	                     AuthenticationException authException) throws IOException, ServletException {

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		Exception exception = (Exception) request.getAttribute("exception");

		String message;
		byte[] body;

		if (exception != null) {
			body = objectMapper.writeValueAsBytes(Collections.singletonMap("cause", exception.toString()));
			response.getOutputStream().write(body);
		} else {
			message = getExceptionMessage(authException);
			body = objectMapper.writeValueAsBytes(Collections.singletonMap("error", message));
		}

		response.getOutputStream().write(body);
	}

	private String getExceptionMessage(AuthenticationException authException) {
		String message;
		if (authException.getCause() != null) {
			message = authException.getCause().toString() + " " + authException.getMessage();
		} else {
			message = authException.getMessage();
		}
		return message;
	}

}
