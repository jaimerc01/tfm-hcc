package com.hcc.tfm_hcc.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.Date;
import java.util.function.Function;

public interface JwtService {
    String extractUsername(String token);
    <T> T extractClaim(String token, Function<io.jsonwebtoken.Claims, T> claimsResolver);
    String generateToken(UserDetails userDetails);
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);
    long getExpirationTime();
    boolean isTokenValid(String token, UserDetails userDetails);
    Date extractIssuedAt(String token);
}
