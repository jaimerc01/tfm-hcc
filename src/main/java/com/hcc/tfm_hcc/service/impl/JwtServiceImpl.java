package com.hcc.tfm_hcc.service.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Implementación del servicio de JWT (JSON Web Token).
 * Proporciona funcionalidades para generar, validar y extraer información de tokens JWT
 * utilizados en la autenticación y autorización del sistema.
 * 
 * <p>Esta implementación utiliza el algoritmo HS256 para la firma de tokens y
 * proporciona validaciones robustas incluyendo verificación de expiración,
 * coincidencia de usuario y validación contra cambios de contraseña.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Service
public class JwtServiceImpl implements JwtService {
    
    // Constantes para manejo de errores y configuración
    private static final String TOKEN_FIELD = "token";
    private static final String USER_DETAILS_FIELD = "userDetails";
    private static final String CLAIMS_RESOLVER_FIELD = "claimsResolver";
    private static final String JWT_SECRET_ERROR_MESSAGE = "Error al procesar clave secreta JWT: {0}";
    
    private final String secretKey;
    private final long jwtExpiration;

    /**
     * Constructor que inyecta las propiedades de configuración JWT.
     * 
     * @param secretKey Clave secreta para firmar los tokens JWT
     * @param jwtExpiration Tiempo de expiración de los tokens en milisegundos
     */
    public JwtServiceImpl(
            @Value("${security.jwt.secret-key}") String secretKey,
            @Value("${security.jwt.expiration-time}") long jwtExpiration) {
        this.secretKey = secretKey;
        this.jwtExpiration = jwtExpiration;
    }

    /**
     * Valida que el token no sea nulo o vacío.
     * 
     * @param token Token a validar
     * @throws IllegalArgumentException si el token es nulo o vacío
     */
    private void validarToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido(TOKEN_FIELD));
        }
    }

    /**
     * Valida que los UserDetails no sean nulos.
     * 
     * @param userDetails UserDetails a validar
     * @throws IllegalArgumentException si userDetails es nulo
     */
    private void validarUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido(USER_DETAILS_FIELD));
        }
    }

    /**
     * Extrae el nombre de usuario del token JWT
     * 
     * @param token Token JWT del cual extraer el username
     * @return Username contenido en el token
     * @throws IllegalArgumentException si el token es inválido
     */
    @Override
    public String extractUsername(String token) {
        validarToken(token);
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_JSON_INVALIDO, e);
        }
    }

    /**
     * Extrae un claim específico del token JWT
     * 
     * @param token Token JWT
     * @param claimsResolver Función para extraer el claim deseado
     * @return Valor del claim extraído
     * @throws IllegalArgumentException si el token es inválido
     */
    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        validarToken(token);
        if (claimsResolver == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido(CLAIMS_RESOLVER_FIELD));
        }
        
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_JSON_INVALIDO, e);
        }
    }

    /**
     * Genera un token JWT para el usuario sin claims adicionales
     * 
     * @param userDetails Detalles del usuario
     * @return Token JWT generado
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        validarUserDetails(userDetails);
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Genera un token JWT para el usuario con claims adicionales
     * 
     * @param extraClaims Claims adicionales a incluir en el token
     * @param userDetails Detalles del usuario
     * @return Token JWT generado
     */
    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        validarUserDetails(userDetails);
        if (extraClaims == null) {
            extraClaims = new HashMap<>();
        }
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Obtiene el tiempo de expiración configurado para los tokens
     * 
     * @return Tiempo de expiración en milisegundos
     */
    @Override
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /**
     * Construye un token JWT con los claims y configuración especificados
     * 
     * @param extraClaims Claims adicionales a incluir
     * @param userDetails Detalles del usuario
     * @param expiration Tiempo de expiración en milisegundos
     * @return Token JWT construido
     */
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

    /**
     * Valida si el username del token coincide con el usuario
     */
    private boolean validarUsuarioToken(String tokenUsername, String username) {
        return tokenUsername != null && tokenUsername.equals(username);
    }

    /**
     * Valida si el token fue emitido después del último cambio de contraseña
     */
    private boolean validarCambioContrasena(UserDetails userDetails, Date issuedAt) {
        if (!(userDetails instanceof Usuario usuario) || issuedAt == null) {
            return true; // Si no es Usuario o no hay fecha de emisión, permitir
        }
        
        if (usuario.getLastPasswordChange() == null) {
            return true; // Si no hay fecha de cambio de contraseña, permitir
        }
        
        Instant lastChangeInstant = usuario.getLastPasswordChange()
                .atZone(ZoneId.systemDefault())
                .toInstant();
                
        return !issuedAt.toInstant().isBefore(lastChangeInstant);
    }

    /**
     * Valida si un token JWT es válido para el usuario especificado
     * 
     * @param token Token JWT a validar
     * @param userDetails Detalles del usuario
     * @return true si el token es válido, false en caso contrario
     */
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            validarToken(token);
            validarUserDetails(userDetails);
            
            final String tokenUsername = extractUsername(token);
            
            // Validar que el username coincida
            if (!validarUsuarioToken(tokenUsername, userDetails.getUsername())) {
                return false;
            }
            
            // Validar que el token no haya expirado
            if (isTokenExpired(token)) {
                return false;
            }
            
            // Validar que el token no sea anterior al último cambio de contraseña
            Date issuedAt = extractIssuedAt(token);
            return validarCambioContrasena(userDetails, issuedAt);
            
        } catch (Exception e) {
            return false; // Si hay cualquier error, el token no es válido
        }
    }

    /**
     * Extrae la fecha de emisión del token JWT
     * 
     * @param token Token JWT
     * @return Fecha de emisión del token
     */
    @Override
    public Date extractIssuedAt(String token) {
        validarToken(token);
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Verifica si el token ha expirado
     * 
     * @param token Token JWT a verificar
     * @return true si el token ha expirado, false en caso contrario
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token
     * 
     * @param token Token JWT
     * @return Fecha de expiración del token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae todos los claims del token JWT
     * 
     * @param token Token JWT
     * @return Claims del token
     * @throws JwtException si el token no es válido
     */
    private Claims extractAllClaims(String token) {
        try {
            SecretKey key = getSignInKey();
            JwtParserBuilder parserBuilder = Jwts.parser().verifyWith(key);
            return parserBuilder.build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            throw new JwtException(ErrorMessages.ERROR_JSON_INVALIDO, e);
        }
    }

    /**
     * Obtiene la clave secreta para firmar/verificar tokens
     * 
     * @return SecretKey para operaciones JWT
     * @throws IllegalStateException si hay error al procesar la clave secreta
     */
    private SecretKey getSignInKey() {
        try {
            return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
        } catch (Exception e) {
            throw new IllegalStateException(ErrorMessages.formatError(JWT_SECRET_ERROR_MESSAGE, e.getMessage()), e);
        }
    }
}