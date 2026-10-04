package com.enterprise.project_management.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expiration;

  private SecretKey getKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(String email, String role) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .subject(email)
        .claim("role", role)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(getKey())
        .compact();
  }

  public String extractEmail(String token) {
    return getClaims(token).getSubject();
  }

  public String extractRole(String token) {
    return getClaims(token).get("role", String.class);
  }

  public boolean isTokenValid(String token, String email) {
    try {
      Claims claims = getClaims(token);

      return claims.getSubject().equals(email) && claims.getExpiration().after(new Date());

    } catch (Exception e) {
      return false;
    }
  }

  private Claims getClaims(String token) {
    return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
  }
}
