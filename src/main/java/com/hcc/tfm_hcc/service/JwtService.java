package com.hcc.tfm_hcc.service;

import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Claims;

import java.util.Map;
import java.util.Date;
import java.util.function.Function;

/**
 * Servicio para la gestión de tokens JWT (JSON Web Token).
 * Proporciona operaciones para generar, validar y extraer información de tokens JWT
 * utilizados en la autenticación y autorización del sistema.
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface JwtService {
    
    /**
     * Extrae el nombre de usuario (subject) del token JWT.
     * 
     * @param token Token JWT del cual extraer el username
     * @return El nombre de usuario contenido en el token
     * @throws IllegalArgumentException si el token es nulo, vacío o inválido
     */
    String extractUsername(String token);
    
    /**
     * Extrae un claim específico del token JWT utilizando una función resolver.
     * 
     * @param <T> Tipo de dato del claim a extraer
     * @param token Token JWT del cual extraer el claim
     * @param claimsResolver Función que define qué claim extraer de los Claims
     * @return El valor del claim extraído
     * @throws IllegalArgumentException si el token es nulo, vacío, inválido o claimsResolver es nulo
     */
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    
    /**
     * Genera un token JWT para el usuario especificado sin claims adicionales.
     * 
     * @param userDetails Detalles del usuario para el cual generar el token
     * @return Token JWT generado
     * @throws IllegalArgumentException si userDetails es nulo
     */
    String generateToken(UserDetails userDetails);
    
    /**
     * Genera un token JWT para el usuario especificado con claims adicionales.
     * 
     * @param extraClaims Mapa de claims adicionales a incluir en el token
     * @param userDetails Detalles del usuario para el cual generar el token
     * @return Token JWT generado
     * @throws IllegalArgumentException si userDetails es nulo
     */
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);
    
    /**
     * Obtiene el tiempo de expiración configurado para los tokens JWT.
     * 
     * @return Tiempo de expiración en milisegundos
     */
    long getExpirationTime();
    
    /**
     * Valida si un token JWT es válido para el usuario especificado.
     * Verifica que el token no haya expirado, que el username coincida
     * y que no sea anterior al último cambio de contraseña del usuario.
     * 
     * @param token Token JWT a validar
     * @param userDetails Detalles del usuario contra el cual validar el token
     * @return true si el token es válido para el usuario, false en caso contrario
     */
    boolean isTokenValid(String token, UserDetails userDetails);
    
    /**
     * Extrae la fecha de emisión (issued at) del token JWT.
     * 
     * @param token Token JWT del cual extraer la fecha de emisión
     * @return Fecha de emisión del token
     * @throws IllegalArgumentException si el token es nulo, vacío o inválido
     */
    Date extractIssuedAt(String token);
}
