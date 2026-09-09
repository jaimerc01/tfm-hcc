package com.hcc.tfm_hcc.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;

class WebSecurityConfigTest {

    private WebSecurityConfig config;

    @BeforeEach
    void setUp() {
        config = new WebSecurityConfig(
                mock(UsuarioRepository.class),
                mock(HmacSearchIndexService.class),
                mock(GoogleOAuth2AuthenticationSuccessHandler.class),
                mock(GoogleOAuth2AuthenticationFailureHandler.class));
    }

    @Test
    void corsConfigurationSource_leeLosOrigenesDeLaPropiedadConfigurada() {
        ReflectionTestUtils.setField(config, "allowedOrigins", "https://miapp.example.com, https://otro.example.com");

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration configuracion = source.getCorsConfiguration(new MockHttpServletRequest());

        assertEquals(
                java.util.List.of("https://miapp.example.com", "https://otro.example.com"),
                configuracion.getAllowedOrigins());
    }

    @Test
    void corsConfigurationSource_sinPropiedadConfigurada_usaLosOrigenesDeDesarrolloPorDefecto() {
        ReflectionTestUtils.setField(config, "allowedOrigins", "http://localhost:8080,https://localhost:8080");

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration configuracion = source.getCorsConfiguration(new MockHttpServletRequest());

        assertEquals(
                java.util.List.of("http://localhost:8080", "https://localhost:8080"),
                configuracion.getAllowedOrigins());
    }

    @Test
    void corsConfigurationSource_permiteCredenciales() {
        ReflectionTestUtils.setField(config, "allowedOrigins", "http://localhost:8080");

        CorsConfiguration configuracion = config.corsConfigurationSource().getCorsConfiguration(new MockHttpServletRequest());

        assertEquals(Boolean.TRUE, configuracion.getAllowCredentials());
    }
}
