package com.hcc.tfm_hcc.config;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.hcc.tfm_hcc.service.JwtService;

import jakarta.servlet.FilterChain;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private HandlerExceptionResolver handlerExceptionResolver;
    private FilterChain filterChain;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsService.class);
        handlerExceptionResolver = mock(HandlerExceptionResolver.class);
        filterChain = mock(FilterChain.class);
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService, handlerExceptionResolver);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_sinCabeceraAuthorization_continuaSinAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_conCabeceraSinPrefijoBearer_continuaSinAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic algo");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_conTokenValido_estableceAutenticacionEnElContexto() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = User.withUsername("12345678A").password("x").authorities("ROLE_PACIENTE").build();
        when(jwtService.extractUsername("token-valido")).thenReturn("12345678A");
        when(userDetailsService.loadUserByUsername("12345678A")).thenReturn(userDetails);
        when(jwtService.isTokenValid("token-valido", userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_conTokenInvalido_noEstableceAutenticacion() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = User.withUsername("12345678A").password("x").authorities("ROLE_PACIENTE").build();
        when(jwtService.extractUsername("token-invalido")).thenReturn("12345678A");
        when(userDetailsService.loadUserByUsername("12345678A")).thenReturn(userDetails);
        when(jwtService.isTokenValid("token-invalido", userDetails)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_conUsuarioDeshabilitado_noEstableceAutenticacionAunqueElTokenSeaValido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = User.withUsername("12345678A").password("x").disabled(true)
                .authorities("ROLE_PACIENTE").build();
        when(jwtService.extractUsername("token-valido")).thenReturn("12345678A");
        when(userDetailsService.loadUserByUsername("12345678A")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).isTokenValid(any(), any());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_conAutenticacionYaPresente_noVuelveACargarElUsuario() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetailsExistente = User.withUsername("existente").password("x").authorities(List.of()).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetailsExistente, null, userDetailsExistente.getAuthorities()));
        when(jwtService.extractUsername("token-valido")).thenReturn("12345678A");

        filter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_conExcepcionAlProcesarElToken_delegaEnElHandlerExceptionResolver() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-problematico");
        MockHttpServletResponse response = new MockHttpServletResponse();

        RuntimeException fallo = new RuntimeException("token corrupto");
        when(jwtService.extractUsername("token-problematico")).thenThrow(fallo);

        filter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver, times(1)).resolveException(eq(request), eq(response), eq(null), eq(fallo));
        verify(filterChain, never()).doFilter(request, response);
    }
}
