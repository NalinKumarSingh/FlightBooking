package com.example.flight.v1.utils;

import com.example.flight.v1.user.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

  private final String SECRET = "supersecretkey123supersecretkey123";
  private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

  public String generateToken(String email, Long userId, Role role) {
    return Jwts.builder()
        .setSubject(email)
        .claim("userId", userId)
        .claim("role", role.name())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hrs
//        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Claims extractAllClaims(String token) throws ExpiredJwtException {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public String extractEmail(String token) {
    return extractAllClaims(token).getSubject();
  }

  public Long extractUserId(String token) {
    return extractAllClaims(token).get("userId", Long.class);
  }

  public String extractRole(String token) {
    return extractAllClaims(token).get("role", String.class);
  }

  public boolean isTokenExpired(String token) {
    return extractAllClaims(token).getExpiration().before(new Date());
  }

  public boolean isTokenValid(String token, String email) {
    try {
      return extractEmail(token).equals(email) && !isTokenExpired(token);
    } catch (Exception e) {
      return false;
    }
  }
}
