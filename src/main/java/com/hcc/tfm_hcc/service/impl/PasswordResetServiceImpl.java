package com.hcc.tfm_hcc.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.exception.PasswordResetTokenInvalidoException;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.PasswordResetToken;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.PasswordResetTokenRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AuditoriaCambioService;
import com.hcc.tfm_hcc.service.EmailService;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.PasswordResetService;
import com.hcc.tfm_hcc.util.LogMaskUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del flujo de restablecimiento de contraseña.
 *
 * @author Sistema HCC
 * @version 1.0
 */
@Slf4j
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int LONGITUD_TOKEN_BYTES = 32;
    private static final int LONGITUD_MINIMA_PASSWORD = 8;
    private static final String ZONA_HORARIA = "Europe/Madrid";

    private static final String TABLA_TOKEN = "password_reset_token";
    private static final String TABLA_USUARIO = "usuario";
    private static final String TIPO_SOLICITUD = "SOLICITUD_RESET_PASSWORD";
    private static final String TIPO_RESET = "RESET_PASSWORD";
    private static final String RAZON_SOLICITUD = "Solicitud de restablecimiento de contraseña (olvido de contraseña)";
    private static final String RAZON_RESET = "Restablecimiento de contraseña completado mediante enlace de un solo uso";

    private final SecureRandom secureRandom = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final HmacSearchIndexService hmacSearchIndexService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditoriaCambioService auditoriaCambioService;
    private final String frontendBaseUrl;
    private final long tokenTtlMinutes;

    public PasswordResetServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            HmacSearchIndexService hmacSearchIndexService,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            AuditoriaCambioService auditoriaCambioService,
            @Value("${app.frontend.base-url:http://localhost:8080}") String frontendBaseUrl,
            @Value("${app.password-reset.token-ttl-minutes:30}") long tokenTtlMinutes) {
        this.usuarioRepository = usuarioRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.hmacSearchIndexService = hmacSearchIndexService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.auditoriaCambioService = auditoriaCambioService;
        this.frontendBaseUrl = frontendBaseUrl;
        this.tokenTtlMinutes = tokenTtlMinutes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void solicitarRestablecimiento(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        Usuario usuario = usuarioRepository.findByEmailHash(hmacSearchIndexService.indexar(email.trim()))
                .orElse(null);

        if (usuario == null || Usuario.ESTADO_CUENTA_ELIMINADO.equals(usuario.getEstadoCuenta())) {
            // Respuesta neutra: no se revela si el correo corresponde o no a una cuenta.
            log.info("Solicitud de restablecimiento para un correo sin cuenta activa asociada: {}",
                    LogMaskUtil.enmascarar(email));
            return;
        }

        String usuarioId = usuario.getId().toString();

        // Invalida cualquier enlace anterior que siguiera pendiente para esta cuenta.
        passwordResetTokenRepository.deleteByUsuarioId(usuarioId);

        String tokenEnClaro = generarToken();
        Instant ahora = Instant.now();

        PasswordResetToken token = new PasswordResetToken();
        token.setId(UUID.randomUUID().toString());
        token.setTokenHash(hashToken(tokenEnClaro));
        token.setUsuarioId(usuarioId);
        token.setUsado(false);
        token.setFechaCreacion(ahora);
        token.setFechaExpiracion(ahora.plus(tokenTtlMinutes, ChronoUnit.MINUTES));
        passwordResetTokenRepository.save(token);

        emailService.enviarEnlaceRestablecimientoPassword(email.trim(), construirEnlace(tokenEnClaro));

        auditoriaCambioService.registrarCambio(
                usuarioId, usuarioId, null,
                TIPO_SOLICITUD, TABLA_TOKEN, token.getId(),
                null, null,
                AuditoriaCambio.TipoOperacion.CREATE, RAZON_SOLICITUD);

        log.info("Generado enlace de restablecimiento de contraseña para el usuario {}", LogMaskUtil.enmascarar(usuarioId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void restablecerPassword(String token, String nuevaPassword) {
        validarNuevaPassword(nuevaPassword);

        if (token == null || token.isBlank()) {
            throw new PasswordResetTokenInvalidoException(ErrorMessages.ERROR_RESET_TOKEN_INVALIDO);
        }

        PasswordResetToken tokenPersistido = passwordResetTokenRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new PasswordResetTokenInvalidoException(ErrorMessages.ERROR_RESET_TOKEN_INVALIDO));

        if (tokenPersistido.isUsado() || tokenPersistido.getFechaExpiracion().isBefore(Instant.now())) {
            throw new PasswordResetTokenInvalidoException(ErrorMessages.ERROR_RESET_TOKEN_INVALIDO);
        }

        Usuario usuario = usuarioRepository.findById(UUID.fromString(tokenPersistido.getUsuarioId()))
                .filter(u -> !Usuario.ESTADO_CUENTA_ELIMINADO.equals(u.getEstadoCuenta()))
                .orElseThrow(() -> new PasswordResetTokenInvalidoException(ErrorMessages.ERROR_RESET_TOKEN_INVALIDO));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setLastPasswordChange(LocalDateTime.now(ZoneId.of(ZONA_HORARIA)));
        usuarioRepository.save(usuario);

        tokenPersistido.setUsado(true);
        passwordResetTokenRepository.save(tokenPersistido);
        // Cualquier otro enlace pendiente de la misma cuenta deja de servir.
        passwordResetTokenRepository.deleteByUsuarioId(tokenPersistido.getUsuarioId());

        auditoriaCambioService.registrarCambio(
                usuario.getId().toString(), usuario.getId().toString(), null,
                TIPO_RESET, TABLA_USUARIO, usuario.getId().toString(),
                null, null,
                AuditoriaCambio.TipoOperacion.UPDATE, RAZON_RESET);

        log.info("Contraseña restablecida mediante enlace de un solo uso para el usuario {}",
                LogMaskUtil.enmascarar(usuario.getId().toString()));
    }

    private void validarNuevaPassword(String nuevaPassword) {
        if (nuevaPassword == null || nuevaPassword.trim().length() < LONGITUD_MINIMA_PASSWORD) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_RESET_PASSWORD_DEBIL);
        }
    }

    private String generarToken() {
        byte[] bytes = new byte[LONGITUD_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String tokenEnClaro) {
        return DigestUtils.sha256Hex(tokenEnClaro.getBytes(StandardCharsets.UTF_8));
    }

    private String construirEnlace(String tokenEnClaro) {
        String base = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        return base + "/restablecer-password?token=" + tokenEnClaro;
    }
}
