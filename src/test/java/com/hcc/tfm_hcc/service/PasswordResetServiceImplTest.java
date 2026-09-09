package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hcc.tfm_hcc.exception.PasswordResetTokenInvalidoException;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.PasswordResetToken;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.PasswordResetTokenRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.PasswordResetServiceImpl;

class PasswordResetServiceImplTest {

    private static final String EMAIL = "ana@example.com";
    private static final String BASE_URL = "http://localhost:8080";

    private UsuarioRepository usuarioRepository;
    private PasswordResetTokenRepository tokenRepository;
    private HmacSearchIndexService hmacSearchIndexService;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;
    private AuditoriaCambioService auditoriaCambioService;
    private PasswordResetServiceImpl service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        tokenRepository = mock(PasswordResetTokenRepository.class);
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);
        auditoriaCambioService = mock(AuditoriaCambioService.class);

        when(hmacSearchIndexService.indexar(anyString())).thenAnswer(inv -> "hash-" + inv.getArgument(0, String.class));
        when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "bcrypt(" + inv.getArgument(0, String.class) + ")");
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service = new PasswordResetServiceImpl(usuarioRepository, tokenRepository, hmacSearchIndexService,
                passwordEncoder, emailService, auditoriaCambioService, BASE_URL, 30L);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail(EMAIL);
        usuario.setEmailHash("hash-" + EMAIL);
        usuario.setEstadoCuenta(Usuario.ESTADO_CUENTA_ACTIVO);
        when(usuarioRepository.findByEmailHash("hash-" + EMAIL)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
    }

    // ---- solicitarRestablecimiento ----

    @Test
    void solicitarRestablecimiento_conCuentaActiva_generaTokenYEnviaEnlace() {
        service.solicitarRestablecimiento(EMAIL);

        verify(tokenRepository).deleteByUsuarioId(usuario.getId().toString());
        verify(tokenRepository).save(any(PasswordResetToken.class));

        ArgumentCaptor<String> enlaceCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarEnlaceRestablecimientoPassword(eq(EMAIL), enlaceCaptor.capture());
        assertTrue(enlaceCaptor.getValue().startsWith(BASE_URL + "/restablecer-password?token="));

        verify(auditoriaCambioService).registrarCambio(
                anyString(), anyString(), eq(null), eq("SOLICITUD_RESET_PASSWORD"), eq("password_reset_token"),
                anyString(), eq(null), eq(null), eq(AuditoriaCambio.TipoOperacion.CREATE), anyString());
    }

    @Test
    void solicitarRestablecimiento_noGuardaElTokenEnClaro() {
        service.solicitarRestablecimiento(EMAIL);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        ArgumentCaptor<String> enlaceCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarEnlaceRestablecimientoPassword(eq(EMAIL), enlaceCaptor.capture());

        String tokenEnClaro = enlaceCaptor.getValue().substring(enlaceCaptor.getValue().indexOf("token=") + 6);
        PasswordResetToken guardado = tokenCaptor.getValue();

        assertEquals(DigestUtils.sha256Hex(tokenEnClaro), guardado.getTokenHash());
        org.junit.jupiter.api.Assertions.assertNotEquals(tokenEnClaro, guardado.getTokenHash());
        assertFalse(guardado.isUsado());
    }

    @Test
    void solicitarRestablecimiento_conCorreoDesconocido_noHaceNada() {
        when(usuarioRepository.findByEmailHash("hash-" + EMAIL)).thenReturn(Optional.empty());

        service.solicitarRestablecimiento(EMAIL);

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).enviarEnlaceRestablecimientoPassword(anyString(), anyString());
    }

    @Test
    void solicitarRestablecimiento_conCuentaEliminada_noHaceNada() {
        usuario.setEstadoCuenta(Usuario.ESTADO_CUENTA_ELIMINADO);

        service.solicitarRestablecimiento(EMAIL);

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).enviarEnlaceRestablecimientoPassword(anyString(), anyString());
    }

    @Test
    void solicitarRestablecimiento_conEmailVacio_noHaceNada() {
        service.solicitarRestablecimiento("   ");
        service.solicitarRestablecimiento(null);

        verify(usuarioRepository, never()).findByEmailHash(anyString());
        verify(emailService, never()).enviarEnlaceRestablecimientoPassword(anyString(), anyString());
    }

    // ---- restablecerPassword ----

    private String prepararTokenValido() {
        service.solicitarRestablecimiento(EMAIL);
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        ArgumentCaptor<String> enlaceCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarEnlaceRestablecimientoPassword(eq(EMAIL), enlaceCaptor.capture());

        PasswordResetToken persistido = tokenCaptor.getValue();
        when(tokenRepository.findByTokenHash(persistido.getTokenHash())).thenReturn(Optional.of(persistido));
        return enlaceCaptor.getValue().substring(enlaceCaptor.getValue().indexOf("token=") + 6);
    }

    @Test
    void restablecerPassword_conTokenValido_cambiaLaContrasenaYConsumeElToken() {
        String tokenEnClaro = prepararTokenValido();

        service.restablecerPassword(tokenEnClaro, "nuevaClaveSegura");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        assertEquals("bcrypt(nuevaClaveSegura)", usuarioCaptor.getValue().getPassword());
        org.junit.jupiter.api.Assertions.assertNotNull(usuarioCaptor.getValue().getLastPasswordChange());

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository, org.mockito.Mockito.atLeastOnce()).save(tokenCaptor.capture());
        assertTrue(tokenCaptor.getValue().isUsado());
        verify(tokenRepository, org.mockito.Mockito.atLeastOnce()).deleteByUsuarioId(usuario.getId().toString());

        verify(auditoriaCambioService).registrarCambio(
                anyString(), anyString(), eq(null), eq("RESET_PASSWORD"), eq("usuario"),
                anyString(), eq(null), eq(null), eq(AuditoriaCambio.TipoOperacion.UPDATE), anyString());
    }

    @Test
    void restablecerPassword_conTokenDesconocido_lanzaExcepcion() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(PasswordResetTokenInvalidoException.class,
                () -> service.restablecerPassword("token-inventado", "nuevaClaveSegura"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void restablecerPassword_conTokenEnBlanco_lanzaExcepcion() {
        assertThrows(PasswordResetTokenInvalidoException.class,
                () -> service.restablecerPassword("  ", "nuevaClaveSegura"));
    }

    @Test
    void restablecerPassword_conTokenYaUsado_lanzaExcepcion() {
        String tokenEnClaro = prepararTokenValido();
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        tokenCaptor.getValue().setUsado(true);

        assertThrows(PasswordResetTokenInvalidoException.class,
                () -> service.restablecerPassword(tokenEnClaro, "nuevaClaveSegura"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void restablecerPassword_conTokenCaducado_lanzaExcepcion() {
        String tokenEnClaro = prepararTokenValido();
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        tokenCaptor.getValue().setFechaExpiracion(Instant.now().minus(1, ChronoUnit.MINUTES));

        assertThrows(PasswordResetTokenInvalidoException.class,
                () -> service.restablecerPassword(tokenEnClaro, "nuevaClaveSegura"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void restablecerPassword_conContrasenaCorta_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.restablecerPassword("cualquier-token", "corta"));
        verify(tokenRepository, never()).findByTokenHash(anyString());
    }

    @Test
    void restablecerPassword_conCuentaEliminada_lanzaExcepcion() {
        String tokenEnClaro = prepararTokenValido();
        usuario.setEstadoCuenta(Usuario.ESTADO_CUENTA_ELIMINADO);

        assertThrows(PasswordResetTokenInvalidoException.class,
                () -> service.restablecerPassword(tokenEnClaro, "nuevaClaveSegura"));
        verify(usuarioRepository, never()).save(any());
    }
}
