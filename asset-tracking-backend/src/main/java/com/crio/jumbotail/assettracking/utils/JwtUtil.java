package com.crio.jumbotail.assettracking.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;
	@Value("${jwt.expirationDateInMs}")
	private int jwtExpirationInMs;
	@Value("${jwt.refreshExpirationDateInMs}")
	private int refreshExpirationDateInMs;

	public String generateToken(UserDetails userDetails) {

		Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
		claims.put("scopes", userDetails.getAuthorities().stream().map(Object::toString).collect(Collectors.toList()));

		return doGenerateToken(claims, userDetails.getUsername());
	}

	private String doGenerateToken(Map<String, Object> claims, String subject) {

		return Jwts.builder().setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
				.signWith(SignatureAlgorithm.HS512, secret)
				.compact();

	}

	public String doGenerateRefreshToken(Map<String, Object> claims, String subject) {

		return Jwts.builder().setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + refreshExpirationDateInMs))
				.signWith(SignatureAlgorithm.HS512, secret)
				.compact();

	}

	public boolean validateToken(String authToken) {
		try {
			Jws<Claims> claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
			throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
		} catch (ExpiredJwtException ex) {
			throw ex;
		}
	}

	public String getUsernameFromToken(String token) {
		Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
		return claims.getSubject();

	}

	public List<GrantedAuthority> getRolesFromToken(String token) {
		Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

		List<String> scopes = claims.get("scopes", List.class);
		List<GrantedAuthority> authorities = scopes.stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		return authorities;

	}

	public UserDetails getUserDetailsFromToken(String jwtToken) {

		final Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(jwtToken).getBody();
		final String subject = claims.getSubject();

		List<String> scopes = claims.get("scopes", List.class);
		List<GrantedAuthority> authorities = scopes.stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());


		return new User(subject, "", authorities);
	}
}
