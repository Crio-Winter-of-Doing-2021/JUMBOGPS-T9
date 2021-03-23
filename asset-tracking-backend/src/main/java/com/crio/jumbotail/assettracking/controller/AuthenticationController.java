package com.crio.jumbotail.assettracking.controller;

import com.crio.jumbotail.assettracking.exceptions.JwtAuthException;
import com.crio.jumbotail.assettracking.exchanges.request.AuthRequest;
import com.crio.jumbotail.assettracking.exchanges.request.CreateUserRequest;
import com.crio.jumbotail.assettracking.exchanges.response.AuthResponse;
import com.crio.jumbotail.assettracking.service.UserCreationService;
import com.crio.jumbotail.assettracking.utils.JwtUtil;
import io.jsonwebtoken.impl.DefaultClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "The Authentication and Authorization Resource")
@RestController
public class AuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private UserCreationService userCreationService;

	@Autowired
	private JwtUtil jwtUtil;

	@Operation(summary = "Authentication API", description = "Authenticate with username and password")
	@PostMapping(value = "/authenticate")
	@ResponseStatus(HttpStatus.OK)
	public AuthResponse createAuthenticationToken(@RequestBody AuthRequest authenticationRequest) {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					authenticationRequest.getUsername(), authenticationRequest.getPassword()));
		} catch (DisabledException e) {
			throw new JwtAuthException("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new JwtAuthException("INVALID_CREDENTIALS", e);
		}

		UserDetails userdetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
		String token = jwtUtil.generateToken(userdetails);
		return new AuthResponse(token);
	}

	@Operation(summary = "User Registration", description = "Create New User")
	@PostMapping(value = "/register")
	@ResponseStatus(HttpStatus.CREATED)
	public void saveUser(@RequestBody CreateUserRequest user) {
		userCreationService.save(user);
	}

	@Operation(summary = "Refresh Expired JWT token", description = "Refresh Expired JWT token")
	@GetMapping(value = "/refreshtoken")
	@ResponseStatus(HttpStatus.OK)
	public AuthResponse refreshtoken(HttpServletRequest request) {
		// From the HttpRequest get the claims
		DefaultClaims claims = (DefaultClaims) request.getAttribute("claims");

		Map<String, Object> expectedMap = getMapFromIoJsonwebtokenClaims(claims);
		String token = jwtUtil.doGenerateRefreshToken(expectedMap, expectedMap.get("sub").toString());
		return new AuthResponse(token);
	}

	public Map<String, Object> getMapFromIoJsonwebtokenClaims(DefaultClaims claims) {
		if (claims == null) {
			throw new JwtAuthException("Token Not Expired");
		}
		Map<String, Object> expectedMap = new HashMap<>();
		for (Entry<String, Object> entry : claims.entrySet()) {
			expectedMap.put(entry.getKey(), entry.getValue());
		}
		return expectedMap;
	}

}
