package com.hcc.tfm_hcc.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.Base64;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import com.hcc.tfm_hcc.model.Usuario;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService{
    
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    @Override
    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        SecretKey key = getSignInKey();
        JwtBuilder builder = Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, Jwts.SIG.HS256);
        return builder.compact();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        if (!(username.equals(userDetails.getUsername())) || isTokenExpired(token)) {
            return false;
        }
        // Invalidate tokens issued before lastPasswordChange
        Date issuedAt = extractIssuedAt(token);
        if (userDetails instanceof Usuario usuario) {
            if (usuario.getLastPasswordChange() != null && issuedAt != null) {
                // Convert LocalDateTime to Date comparison
                java.time.Instant lastChangeInstant = usuario.getLastPasswordChange().atZone(java.time.ZoneId.systemDefault()).toInstant();
                if (issuedAt.toInstant().isBefore(lastChangeInstant)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        SecretKey key = getSignInKey();
        JwtParserBuilder parserBuilder = Jwts.parser().verifyWith(key);
        return parserBuilder.build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSignInKey() {
    return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }
}