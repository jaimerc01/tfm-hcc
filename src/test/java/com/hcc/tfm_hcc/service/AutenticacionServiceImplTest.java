package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.hcc.tfm_hcc.converter.UsuarioConverter;
import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.GoogleAuthenticationException;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.LoginResponse;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.AutenticacionServiceImpl;

import java.util.Optional;

class AutenticacionServiceImplTest {

    private UsuarioFacade usuarioFacade;
    private UsuarioRepository userRepository;
    private UsuarioConverter usuarioConverter;
    private AuthenticationManager authenticationManager;
    private UserDetailsService userDetailsService;
    private AutenticacionServiceImpl service;

    @BeforeEach
    void setUp() {
        usuarioFacade = mock(UsuarioFacade.class);
        userRepository = mock(UsuarioRepository.class);
        usuarioConverter = mock(UsuarioConverter.class);
        authenticationManager = mock(AuthenticationManager.class);
        userDetailsService = mock(UserDetailsService.class);
        service = new AutenticacionServiceImpl(usuarioFacade, userRepository, usuarioConverter, authenticationManager, userDetailsService);
    }

    private LoginUsuarioDTO login(String nif, String password) {
        LoginUsuarioDTO dto = new LoginUsuarioDTO();
        dto.setNif(nif);
        dto.setPassword(password);
        return dto;
    }

    @Test
    void registrar_conDatosValidos_devuelveElUsuarioConvertido() {
        UsuarioDTO entrada = new UsuarioDTO();
        UsuarioDTO registrado = new UsuarioDTO();
        Usuario esperado = new Usuario();
        when(usuarioFacade.altaUsuario(entrada)).thenReturn(registrado);
        when(usuarioConverter.toEntity(registrado)).thenReturn(esperado);

        Usuario resultado = service.registrar(entrada);

        assertEquals(esperado, resultado);
    }

    @Test
    void registrar_conDtoNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(null));
    }

    @Test
    void autenticar_conCredencialesValidasYPrincipalUsuario_devuelveElUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        Usuario resultado = service.autenticar(login("12345678A", "password123"));

        assertEquals(usuario, resultado);
    }

    @Test
    void autenticar_conPrincipalQueNoEsUsuario_recargaViaUserDetailsService() {
        Usuario usuarioRecargado = new Usuario();
        usuarioRecargado.setNif("12345678A");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("principal-no-usuario");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userDetailsService.loadUserByUsername("12345678A")).thenReturn(usuarioRecargado);

        Usuario resultado = service.autenticar(login("12345678A", "password123"));

        assertEquals(usuarioRecargado, resultado);
    }

    @Test
    void autenticar_conFalloDeAutenticacion_lanzaIncorrectCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("credenciales invalidas"));

        assertThrows(IncorrectCredentials.class, () -> service.autenticar(login("12345678A", "malacontrasena")));
    }

    @Test
    void autenticar_conNifVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.autenticar(login("  ", "password123")));
    }

    @Test
    void autenticar_conPasswordVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.autenticar(login("12345678A", "  ")));
    }

    @Test
    void autenticar_conDtoNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.autenticar(null));
    }

    @Test
    void obtenerUriAutorizacionGoogle_devuelveUnaUriNoNula() {
        assertNotNull(service.obtenerUriAutorizacionGoogle());
    }

    @Test
    void autenticarConGoogle_conEmailValidoYVerificado_devuelveUsuarioConAuthorities() {
        Usuario usuarioPorEmail = new Usuario();
        usuarioPorEmail.setNif("12345678A");
        Usuario usuarioConAuthorities = new Usuario();
        usuarioConAuthorities.setNif("12345678A");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuarioPorEmail));
        when(userDetailsService.loadUserByUsername("12345678A")).thenReturn(usuarioConAuthorities);

        Usuario resultado = service.autenticarConGoogle("test@example.com", true);

        assertEquals(usuarioConAuthorities, resultado);
    }

    @Test
    void autenticarConGoogle_conEmailNulo_lanzaGoogleAuthenticationException() {
        assertThrows(GoogleAuthenticationException.class, () -> service.autenticarConGoogle(null, true));
    }

    @Test
    void autenticarConGoogle_conEmailNoVerificado_lanzaGoogleAuthenticationException() {
        assertThrows(GoogleAuthenticationException.class, () -> service.autenticarConGoogle("test@example.com", false));
    }

    @Test
    void autenticarConGoogle_conCuentaInexistente_lanzaGoogleAuthenticationException() {
        when(userRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        assertThrows(GoogleAuthenticationException.class, () -> service.autenticarConGoogle("noexiste@example.com", true));
    }

    @Test
    void autenticarConGoogle_conUserDetailsServiceSinUsuario_lanzaGoogleAuthenticationException() {
        Usuario usuarioPorEmail = new Usuario();
        usuarioPorEmail.setNif("12345678A");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuarioPorEmail));
        when(userDetailsService.loadUserByUsername("12345678A")).thenReturn(mock(UserDetails.class));

        assertThrows(GoogleAuthenticationException.class, () -> service.autenticarConGoogle("test@example.com", true));
    }

    @Test
    void generarYCanjearCodigoLoginGoogle_conCodigoValido_devuelveLoginResponseConLosDatosOriginales() {
        String code = service.generarCodigoLoginGoogle("jwt-token", 3_600_000L);

        LoginResponse respuesta = service.canjearCodigoLoginGoogle(code);

        assertEquals("jwt-token", respuesta.getToken());
        assertEquals(3_600_000L, respuesta.getExpirationTime());
    }

    @Test
    void canjearCodigoLoginGoogle_conCodigoYaCanjeado_lanzaGoogleAuthenticationException() {
        String code = service.generarCodigoLoginGoogle("jwt-token", 3_600_000L);
        service.canjearCodigoLoginGoogle(code);

        assertThrows(GoogleAuthenticationException.class, () -> service.canjearCodigoLoginGoogle(code));
    }

    @Test
    void canjearCodigoLoginGoogle_conCodigoInexistente_lanzaGoogleAuthenticationException() {
        assertThrows(GoogleAuthenticationException.class, () -> service.canjearCodigoLoginGoogle("codigo-inexistente"));
    }

    @Test
    void canjearCodigoLoginGoogle_conCodigoNulo_lanzaGoogleAuthenticationException() {
        assertThrows(GoogleAuthenticationException.class, () -> service.canjearCodigoLoginGoogle(null));
    }
}
