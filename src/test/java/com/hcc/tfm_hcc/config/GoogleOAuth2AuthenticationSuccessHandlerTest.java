package com.hcc.tfm_hcc.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import com.hcc.tfm_hcc.exception.GoogleAuthenticationException;
import com.hcc.tfm_hcc.facade.AutenticacionFacade;

class GoogleOAuth2AuthenticationSuccessHandlerTest {

    private AutenticacionFacade autenticacionFacade;
    private GoogleOAuth2AuthenticationSuccessHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        autenticacionFacade = mock(AutenticacionFacade.class);
        handler = new GoogleOAuth2AuthenticationSuccessHandler(autenticacionFacade);
        ReflectionTestUtils.setField(handler, "frontendBaseUrl", "https://frontend.example.com");
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void onAuthenticationSuccess_conUsuarioOidcValido_redirigeConElCodigo() throws Exception {
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getEmail()).thenReturn("ana@example.com");
        when(oidcUser.getEmailVerified()).thenReturn(true);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(autenticacionFacade.procesarLoginGoogle("ana@example.com", true)).thenReturn("codigo-un-uso");

        handler.onAuthenticationSuccess(request, response, authentication);

        assertTrue(response.getRedirectedUrl().startsWith("https://frontend.example.com/auth/google/callback?code="));
        assertTrue(response.getRedirectedUrl().contains("codigo-un-uso"));
    }

    @Test
    void onAuthenticationSuccess_sinPrincipalOidc_redirigeALoginConError() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("no-es-oidc");

        handler.onAuthenticationSuccess(request, response, authentication);

        assertTrue(response.getRedirectedUrl().startsWith("https://frontend.example.com/login?error=google_auth_failed"));
    }

    @Test
    void onAuthenticationSuccess_conCuentaNoEncontrada_redirigeALoginConElCodigoDeError() throws Exception {
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getEmail()).thenReturn("noexiste@example.com");
        when(oidcUser.getEmailVerified()).thenReturn(true);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(autenticacionFacade.procesarLoginGoogle("noexiste@example.com", true))
                .thenThrow(new GoogleAuthenticationException("no existe", "google_account_not_found"));

        handler.onAuthenticationSuccess(request, response, authentication);

        assertTrue(response.getRedirectedUrl().startsWith("https://frontend.example.com/login?error=google_account_not_found"));
    }
}
