package com.hcc.tfm_hcc.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.constants.ErrorMessages;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Maneja los fallos que Spring Security detecta durante el propio intercambio OAuth2 con Google
 * (usuario cancela el consentimiento, error del proveedor, etc.) redirigiendo al login del
 * frontend con un código de error genérico.
 */
@Slf4j
@Component
public class GoogleOAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final String LOGIN_PATH = "/login";

    @Value("${app.frontend.base-url:http://localhost:8080}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        log.warn("Autenticación OAuth2 de Google fallida: {}", exception.getMessage());
        String errorCode = URLEncoder.encode(ErrorMessages.GOOGLE_ERROR_CODE_AUTH_FAILED, StandardCharsets.UTF_8);
        response.sendRedirect(frontendBaseUrl + LOGIN_PATH + "?error=" + errorCode);
    }
}
