package com.hcc.tfm_hcc.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AccessLogService;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;

import jakarta.servlet.FilterChain;

class AccessLogFilterTest {

    private AccessLogService accessLogService;
    private UsuarioRepository usuarioRepository;
    private HmacSearchIndexService hmacSearchIndexService;
    private AccessLogFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        accessLogService = mock(AccessLogService.class);
        usuarioRepository = mock(UsuarioRepository.class);
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        when(hmacSearchIndexService.indexar("12345678A")).thenReturn("hash-12345678A");
        when(hmacSearchIndexService.indexar("00000000Z")).thenReturn("hash-00000000Z");
        filter = new AccessLogFilter(accessLogService, usuarioRepository, hmacSearchIndexService);
        chain = mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_conPeticionOptions_noRegistraAcceso() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/historial");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(accessLogService, never()).log(any());
    }

    @Test
    void doFilter_conPeticionYaMarcadaComoRegistrada_noVuelveARegistrar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/historial");
        request.setAttribute("__ACCESS_LOGGED__", Boolean.TRUE);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(accessLogService, never()).log(any());
    }

    @Test
    void doFilter_conRecursoEstatico_noRegistraAcceso() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/css/estilos.css");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(accessLogService, never()).log(any());
    }

    @Test
    void doFilter_conPeticionAnonima_registraAccesoSinUsuario() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/historial");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<AccessLog> captor = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogService, times(1)).log(captor.capture());
        AccessLog log = captor.getValue();
        assertNull(log.getUsuarioId());
        assertEquals("GET", log.getMetodo());
        assertEquals("/api/historial", log.getRuta());
        assertEquals(200, log.getEstado());
    }

    @Test
    void doFilter_conAutenticacionAnonimaExplicita_noResuelveUsuario() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/historial");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        filter.doFilter(request, response, chain);

        verify(usuarioRepository, never()).findByNifHash(any());
    }

    @Test
    void doFilter_conUsuarioAutenticadoYEncontrado_registraSuId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/historial");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = User.withUsername("12345678A").password("x").authorities("ROLE_PACIENTE").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        Usuario usuario = new Usuario();
        UUID id = UUID.randomUUID();
        usuario.setId(id);
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));

        filter.doFilter(request, response, chain);

        ArgumentCaptor<AccessLog> captor = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogService, times(1)).log(captor.capture());
        assertEquals(id.toString(), captor.getValue().getUsuarioId());
    }

    @Test
    void doFilter_conPrincipalUsuario_usaSuIdSinConsultarLaBaseDeDatos() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/historial");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Usuario usuario = new Usuario();
        UUID id = UUID.randomUUID();
        usuario.setId(id);
        usuario.setNif("12345678A");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, AuthorityUtils.createAuthorityList("ROLE_PACIENTE")));

        filter.doFilter(request, response, chain);

        ArgumentCaptor<AccessLog> captor = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogService, times(1)).log(captor.capture());
        assertEquals(id.toString(), captor.getValue().getUsuarioId());
        verify(usuarioRepository, never()).findByNifHash(any());
    }

    @Test
    void doFilter_conUsuarioAutenticadoNoEncontradoEnBd_registraSinId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/historial");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = User.withUsername("00000000Z").password("x").authorities("ROLE_PACIENTE").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        when(usuarioRepository.findByNifHash("hash-00000000Z")).thenReturn(Optional.empty());

        filter.doFilter(request, response, chain);

        ArgumentCaptor<AccessLog> captor = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogService, times(1)).log(captor.capture());
        assertNull(captor.getValue().getUsuarioId());
    }

    @Test
    void doFilter_conCabeceraXForwardedFor_usaLaPrimeraIp() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/historial");
        request.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        ArgumentCaptor<AccessLog> captor = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogService, times(1)).log(captor.capture());
        assertEquals("203.0.113.5", captor.getValue().getIp());
    }

    @Test
    void doFilter_conNifEnLaRuta_loEnmascaraAntesDeGuardar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/pacientes/12345678A/historial");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        ArgumentCaptor<AccessLog> captor = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogService, times(1)).log(captor.capture());
        assertEquals("/pacientes/***78A/historial", captor.getValue().getRuta());
    }

    @Test
    void doFilter_conNieEnLaRuta_loEnmascaraAntesDeGuardar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/pacientes/X1234567A/historial");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        ArgumentCaptor<AccessLog> captor = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogService, times(1)).log(captor.capture());
        assertEquals("/pacientes/***67A/historial", captor.getValue().getRuta());
    }

    @Test
    void doFilter_conErrorAlRegistrarElAcceso_noPropagaLaExcepcion() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/historial");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doThrow(new RuntimeException("fallo de escritura")).when(accessLogService).log(any());

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }
}
