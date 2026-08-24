package com.hcc.tfm_hcc.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

class GoogleOAuth2AuthenticationFailureHandlerTest {

    private GoogleOAuth2AuthenticationFailureHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new GoogleOAuth2AuthenticationFailureHandler();
        ReflectionTestUtils.setField(handler, "frontendBaseUrl", "https://frontend.example.com");
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void onAuthenticationFailure_redirigeALoginConElCodigoDeErrorGenerico() throws Exception {
        AuthenticationException exception = new BadCredentialsException("el usuario canceló el consentimiento");

        handler.onAuthenticationFailure(request, response, exception);

        assertEquals("https://frontend.example.com/login?error=google_auth_failed", response.getRedirectedUrl());
    }
}
