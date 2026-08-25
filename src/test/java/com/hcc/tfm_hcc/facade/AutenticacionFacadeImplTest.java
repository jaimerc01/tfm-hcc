package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.AutenticacionOperacionException;
import com.hcc.tfm_hcc.exception.GoogleAuthenticationException;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.exception.InvalidLoginDataException;
import com.hcc.tfm_hcc.exception.InvalidRegistrationDataException;
import com.hcc.tfm_hcc.facade.impl.AutenticacionFacadeImpl;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.LoginResponse;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.AutenticacionService;
import com.hcc.tfm_hcc.service.JwtService;

class AutenticacionFacadeImplTest {

    private AutenticacionService autenticacionService;
    private JwtService jwtService;
    private UsuarioMapper usuarioMapper;
    private AutenticacionFacadeImpl facade;

    @BeforeEach
    void setUp() {
        autenticacionService = mock(AutenticacionService.class);
        jwtService = mock(JwtService.class);
        usuarioMapper = mock(UsuarioMapper.class);
        facade = new AutenticacionFacadeImpl(autenticacionService, jwtService, usuarioMapper);
    }

    private LoginUsuarioDTO login(String nif, String password) {
        LoginUsuarioDTO dto = new LoginUsuarioDTO();
        dto.setNif(nif);
        dto.setPassword(password);
        return dto;
    }

    @Test
    void autenticar_conCredencialesValidas_devuelveTokenYExpiracion() {
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        usuario.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_PACIENTE")));
        when(autenticacionService.autenticar(any())).thenReturn(usuario);
        when(jwtService.generateToken(anyMap(), eq(usuario))).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3_600_000L);

        ResponseEntity<LoginResponse> respuesta = facade.autenticar(login("12345678A", "password123"));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("jwt-token", respuesta.getBody().getToken());
        assertEquals(3_600_000L, respuesta.getBody().getExpirationTime());
    }

    @Test
    void autenticar_conDtoNulo_lanzaInvalidLoginDataException() {
        assertThrows(InvalidLoginDataException.class, () -> facade.autenticar(null));
    }

    @Test
    void autenticar_conNifVacio_lanzaInvalidLoginDataException() {
        assertThrows(InvalidLoginDataException.class, () -> facade.autenticar(login("  ", "password123")));
    }

    @Test
    void autenticar_conCredencialesIncorrectas_propagaIncorrectCredentials() {
        when(autenticacionService.autenticar(any())).thenThrow(new IncorrectCredentials("credenciales invalidas"));

        assertThrows(IncorrectCredentials.class, () -> facade.autenticar(login("12345678A", "mala")));
    }

    @Test
    void autenticar_conErrorInesperado_lanzaAutenticacionOperacionException() {
        when(autenticacionService.autenticar(any())).thenThrow(new RuntimeException("fallo"));

        assertThrows(AutenticacionOperacionException.class, () -> facade.autenticar(login("12345678A", "password123")));
    }

    private UsuarioDTO registroValido() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNif("12345678A");
        dto.setNombre("Ana");
        dto.setApellido1("García");
        dto.setEmail("ana@example.com");
        dto.setPassword("password123");
        return dto;
    }

    @Test
    void registrar_conDatosValidos_devuelveElUsuarioRegistrado() {
        UsuarioDTO entrada = registroValido();
        Usuario usuarioRegistrado = new Usuario();
        UsuarioDTO respuestaDto = new UsuarioDTO();
        when(autenticacionService.registrar(entrada)).thenReturn(usuarioRegistrado);
        when(usuarioMapper.toDto(usuarioRegistrado)).thenReturn(respuestaDto);

        ResponseEntity<UsuarioDTO> respuesta = facade.registrar(entrada);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(respuestaDto, respuesta.getBody());
    }

    @Test
    void registrar_conDtoNulo_lanzaInvalidRegistrationDataException() {
        assertThrows(InvalidRegistrationDataException.class, () -> facade.registrar(null));
    }

    @Test
    void registrar_conEmailSinArroba_lanzaInvalidRegistrationDataException() {
        UsuarioDTO dto = registroValido();
        dto.setEmail("sin-arroba");

        assertThrows(InvalidRegistrationDataException.class, () -> facade.registrar(dto));
    }

    @Test
    void registrar_conErrorInesperado_lanzaAutenticacionOperacionException() {
        UsuarioDTO dto = registroValido();
        when(autenticacionService.registrar(dto)).thenThrow(new RuntimeException("fallo"));

        assertThrows(AutenticacionOperacionException.class, () -> facade.registrar(dto));
    }

    @Test
    void iniciarLoginGoogle_devuelveRedireccionConLaUriDelServicio() {
        URI uri = URI.create("https://accounts.google.com/o/oauth2/auth");
        when(autenticacionService.obtenerUriAutorizacionGoogle()).thenReturn(uri);

        ResponseEntity<Void> respuesta = facade.iniciarLoginGoogle();

        assertEquals(HttpStatus.FOUND, respuesta.getStatusCode());
        assertEquals(uri, respuesta.getHeaders().getLocation());
    }

    @Test
    void procesarLoginGoogle_conEmailValido_devuelveElCodigoGenerado() {
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        usuario.setAuthorities(Collections.emptyList());
        when(autenticacionService.autenticarConGoogle("ana@example.com", true)).thenReturn(usuario);
        when(jwtService.generateToken(anyMap(), eq(usuario))).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3_600_000L);
        when(autenticacionService.generarCodigoLoginGoogle("jwt-token", 3_600_000L)).thenReturn("codigo-un-uso");

        assertEquals("codigo-un-uso", facade.procesarLoginGoogle("ana@example.com", true));
    }

    @Test
    void procesarLoginGoogle_conTotpActivado_creaElRetoYDevuelveElCodigoDeDosFactores() {
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        usuario.setTotpEnabled(true);
        when(autenticacionService.autenticarConGoogle("ana@example.com", true)).thenReturn(usuario);
        when(autenticacionService.crearChallengeDosFactores(usuario)).thenReturn("challenge-1");
        when(autenticacionService.generarCodigoLoginGoogleConDosFactores("challenge-1")).thenReturn("codigo-2fa");

        assertEquals("codigo-2fa", facade.procesarLoginGoogle("ana@example.com", true));
    }

    @Test
    void procesarLoginGoogle_conCuentaInexistente_propagaGoogleAuthenticationException() {
        when(autenticacionService.autenticarConGoogle("ana@example.com", true))
                .thenThrow(new GoogleAuthenticationException("no existe", "google_account_not_found"));

        assertThrows(GoogleAuthenticationException.class, () -> facade.procesarLoginGoogle("ana@example.com", true));
    }

    @Test
    void canjearCodigoGoogle_conCodigoValido_devuelveLaRespuestaDeLogin() {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("jwt-token");
        when(autenticacionService.canjearCodigoLoginGoogle("codigo-un-uso")).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> respuesta = facade.canjearCodigoGoogle("codigo-un-uso");

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(loginResponse, respuesta.getBody());
    }
}
