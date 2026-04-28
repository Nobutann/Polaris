package io.polaris.sebrae.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.polaris.sebrae.config.JwtConfig;

@Service
public class JwtService {
	private final JwtConfig jwtConfig;
	
	public JwtService(JwtConfig jwtConfig) {
		this.jwtConfig = jwtConfig;
	}
	
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
	}
	
	public String generateToken(UserDetails userDetails) {
		return Jwts.builder()
				.subject(userDetails.getUsername())
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))
				.signWith(getSigningKey())
				.compact();
	}
	
	public String extractEmail(String token) {
		return extractClaims(token).getSubject();
	}
	
	public boolean isTokenValid(String token, UserDetails userDetails) {
		String email = extractEmail(token);
		return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}
	
	private boolean isTokenExpired(String token) {
		return extractClaims(token).getExpiration().before(new Date());
	}
	
	private Claims extractClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
