package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.impl.JwtServiceImpl;

import io.jsonwebtoken.Claims;

class JwtServiceImplTest {

    private static final String SECRETO_BASE64 =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private JwtServiceImpl jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(SECRETO_BASE64, 3_600_000L);
        userDetails = User.withUsername("12345678A").password("irrelevante").authorities("ROLE_PACIENTE").build();
    }

    @Test
    void generateTokenYExtractUsername_devuelveElMismoUsername() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("12345678A", jwtService.extractUsername(token));
    }

    @Test
    void generateToken_conClaimsExtra_seRecuperanConExtractClaim() {
        String token = jwtService.generateToken(Map.of("rol", "MEDICO"), userDetails);

        String rol = jwtService.extractClaim(token, claims -> claims.get("rol", String.class));

        assertEquals("MEDICO", rol);
    }

    @Test
    void getExpirationTime_devuelveElValorConfigurado() {
        assertEquals(3_600_000L, jwtService.getExpirationTime());
    }

    @Test
    void isTokenValid_conTokenRecienGeneradoYMismoUsuario_devuelveTrue() {
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_conUsuarioDistinto_devuelveFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otroUsuario = User.withUsername("87654321B").password("x").authorities("ROLE_PACIENTE").build();

        assertFalse(jwtService.isTokenValid(token, otroUsuario));
    }

    @Test
    void isTokenValid_conTokenExpirado_devuelveFalse() {
        JwtServiceImpl servicioExpiracionInmediata = new JwtServiceImpl(SECRETO_BASE64, -1000L);
        String tokenExpirado = servicioExpiracionInmediata.generateToken(userDetails);

        assertFalse(servicioExpiracionInmediata.isTokenValid(tokenExpirado, userDetails));
    }

    @Test
    void isTokenValid_conTokenAnteriorAlCambioDeContrasena_devuelveFalse() {
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        usuario.setLastPasswordChange(LocalDateTime.now().plusDays(1));

        String token = jwtService.generateToken(usuario);

        assertFalse(jwtService.isTokenValid(token, usuario));
    }

    @Test
    void isTokenValid_conTokenPosteriorAlCambioDeContrasena_devuelveTrue() {
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        usuario.setLastPasswordChange(LocalDateTime.now().minusDays(1));

        String token = jwtService.generateToken(usuario);

        assertTrue(jwtService.isTokenValid(token, usuario));
    }

    @Test
    void isTokenValid_conTokenMalformado_devuelveFalseSinLanzarExcepcion() {
        assertFalse(jwtService.isTokenValid("token-invalido", userDetails));
    }

    @Test
    void extractUsername_conTokenNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> jwtService.extractUsername(null));
    }

    @Test
    void extractUsername_conTokenVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> jwtService.extractUsername("   "));
    }

    @Test
    void extractUsername_conTokenManipulado_lanzaExcepcion() {
        String token = jwtService.generateToken(userDetails);
        String tokenManipulado = token.substring(0, token.length() - 2) + "xx";

        assertThrows(IllegalArgumentException.class, () -> jwtService.extractUsername(tokenManipulado));
    }

    @Test
    void generateToken_conUserDetailsNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> jwtService.generateToken((UserDetails) null));
    }

    @Test
    void extractClaim_conResolverNulo_lanzaExcepcion() {
        String token = jwtService.generateToken(userDetails);

        assertThrows(IllegalArgumentException.class, () -> jwtService.extractClaim(token, (java.util.function.Function<Claims, Object>) null));
    }

    @Test
    void extractIssuedAt_devuelveUnaFechaNoNula() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(jwtService.extractIssuedAt(token));
    }

    @Test
    void generateToken_conClaveSecretaInvalida_lanzaExcepcion() {
        JwtServiceImpl servicioClaveInvalida = new JwtServiceImpl("no-es-base64-valido!!", 3_600_000L);

        assertThrows(IllegalStateException.class, () -> servicioClaveInvalida.generateToken(userDetails));
    }
}
