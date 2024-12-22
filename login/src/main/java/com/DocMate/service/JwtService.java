package com.DocMate.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:2592000000}") // Default 30 days in milliseconds
    private long jwtExpiration;
    
    @Value("${admin.contact}") 
    private String adminContact;
    
    @Value("${admin.key}") 
    private String adminKey;
    
    @Value("${admin.expiration}") // Default 30 days in milliseconds
    private Long adminExpiry;

    // Generate JWT token
    public String generateJwtToken(String phoneNumber) {
        logger.info("passkey: {}", secretKey);

        // Determine role based on the phone number
        String role = phoneNumber.equals(adminContact) ? adminKey : "user";

        try {
            return Jwts.builder()
                    .setSubject(phoneNumber)
                    .claim("role", role) // Embed role in the token
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 
                        (!role.equals("user") ? adminExpiry : jwtExpiration))) // 1 hour for admin, default for user
                    .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token for phone number: {}", phoneNumber, e);
            throw e;
        }
    }


    // Validate JWT token
    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token);
            logger.info("JWT token validated successfully.");
        } catch (ExpiredJwtException e) {
            logger.error("JWT token expired: {}", token);
            throw new RuntimeException("JWT token expired.");
        } catch (JwtException e) {
            logger.error("Invalid JWT token: {}", token);
            throw new RuntimeException("Invalid JWT token.");
        }
    }

    // Extract contact number from JWT token
    public String extractContactFromJwt(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            logger.error("Error extracting contact from JWT: {}", token, e);
            throw new RuntimeException("Invalid JWT token.");
        }
    }
    
    public String extractRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (Exception e) {
            logger.error("Error extracting admin key from token: {}", token, e);
            throw new RuntimeException("Invalid token");
        }
    }

}
