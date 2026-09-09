package com.hcc.tfm_hcc.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.exception.GoogleAuthenticationException;
import com.hcc.tfm_hcc.facade.AutenticacionFacade;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Maneja el éxito de la autenticación OAuth2 con Google: busca la cuenta existente asociada
 * al email verificado por Google, emite un código de un solo uso que envuelve el JWT de la
 * aplicación y redirige al frontend para que lo canjee.
 *
 * <p>No crea cuentas nuevas: Google solo sirve para iniciar sesión en cuentas ya registradas
 * por el formulario tradicional (que exige NIF, campo que Google no proporciona).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String CALLBACK_PATH = "/auth/google/callback";
    private static final String LOGIN_PATH = "/login";

    private final AutenticacionFacade autenticacionFacade;

    @Value("${app.frontend.base-url:http://localhost:8080}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            log.warn("Login con Google sin perfil OIDC válido");
            redirigirConError(response, ErrorMessages.GOOGLE_ERROR_CODE_AUTH_FAILED);
            return;
        }

        try {
            String email = oidcUser.getEmail();
            boolean emailVerificado = Boolean.TRUE.equals(oidcUser.getEmailVerified());
            String codigo = autenticacionFacade.procesarLoginGoogle(email, emailVerificado);
            response.sendRedirect(frontendBaseUrl + CALLBACK_PATH + "?code=" + URLEncoder.encode(codigo, StandardCharsets.UTF_8));
        } catch (GoogleAuthenticationException e) {
            log.warn("Login con Google rechazado: {}", e.getMessage());
            redirigirConError(response, e.getErrorCode());
        }
    }

    private void redirigirConError(HttpServletResponse response, String errorCode) throws IOException {
        response.sendRedirect(frontendBaseUrl + LOGIN_PATH + "?error=" + URLEncoder.encode(errorCode, StandardCharsets.UTF_8));
    }
}
