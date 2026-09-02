package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
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
import com.hcc.tfm_hcc.model.GoogleLoginCode;
import com.hcc.tfm_hcc.model.LoginResponse;
import com.hcc.tfm_hcc.model.TwoFactorChallenge;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.GoogleLoginCodeRepository;
import com.hcc.tfm_hcc.repository.TwoFactorChallengeRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.AutenticacionServiceImpl;

import java.util.Optional;
import java.util.UUID;

class AutenticacionServiceImplTest {

    private UsuarioFacade usuarioFacade;
    private UsuarioRepository userRepository;
    private UsuarioConverter usuarioConverter;
    private AuthenticationManager authenticationManager;
    private UserDetailsService userDetailsService;
    private HmacSearchIndexService hmacSearchIndexService;
    private GoogleLoginCodeRepository googleLoginCodeRepository;
    private TwoFactorChallengeRepository twoFactorChallengeRepository;
    private TotpService totpService;
    private MongoTemplate mongoTemplate;
    private AutenticacionServiceImpl service;

    /**
     * Almacenes "de mentira" que sustituyen a MongoDB en estos tests: sirven para
     * probar la orquestación de AutenticacionServiceImpl (generar código/reto,
     * canjearlo una vez, rechazar un segundo canjeo), no el comportamiento real
     * de MongoTemplate ni de los repositorios de Spring Data.
     */
    private final Map<String, GoogleLoginCode> googleLoginCodesFalsas = new HashMap<>();
    private final Map<String, TwoFactorChallenge> challengesFalsos = new HashMap<>();

    @BeforeEach
    void setUp() {
        usuarioFacade = mock(UsuarioFacade.class);
        userRepository = mock(UsuarioRepository.class);
        usuarioConverter = mock(UsuarioConverter.class);
        authenticationManager = mock(AuthenticationManager.class);
        userDetailsService = mock(UserDetailsService.class);
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        when(hmacSearchIndexService.indexar("test@example.com")).thenReturn("hash-test@example.com");
        when(hmacSearchIndexService.indexar("noexiste@example.com")).thenReturn("hash-noexiste@example.com");

        googleLoginCodesFalsas.clear();
        googleLoginCodeRepository = mock(GoogleLoginCodeRepository.class);
        when(googleLoginCodeRepository.save(any(GoogleLoginCode.class))).thenAnswer(inv -> {
            GoogleLoginCode codigo = inv.getArgument(0);
            googleLoginCodesFalsas.put(codigo.getId(), codigo);
            return codigo;
        });
        mongoTemplate = mock(MongoTemplate.class);
        when(mongoTemplate.findAndRemove(any(Query.class), org.mockito.ArgumentMatchers.eq(GoogleLoginCode.class)))
                .thenAnswer(inv -> {
                    Query query = inv.getArgument(0);
                    String id = (String) query.getQueryObject().get("_id");
                    return googleLoginCodesFalsas.remove(id);
                });

        challengesFalsos.clear();
        twoFactorChallengeRepository = mock(TwoFactorChallengeRepository.class);
        when(twoFactorChallengeRepository.save(any(TwoFactorChallenge.class))).thenAnswer(inv -> {
            TwoFactorChallenge challenge = inv.getArgument(0);
            challengesFalsos.put(challenge.getId(), challenge);
            return challenge;
        });
        when(twoFactorChallengeRepository.findById(any())).thenAnswer(inv ->
                Optional.ofNullable(challengesFalsos.get(inv.getArgument(0, String.class))));
        org.mockito.Mockito.doAnswer(inv -> {
            challengesFalsos.remove(inv.getArgument(0, String.class));
            return null;
        }).when(twoFactorChallengeRepository).deleteById(any());

        totpService = mock(TotpService.class);

        service = new AutenticacionServiceImpl(usuarioFacade, userRepository, usuarioConverter, authenticationManager,
                userDetailsService, hmacSearchIndexService, googleLoginCodeRepository, twoFactorChallengeRepository,
                totpService, mongoTemplate);
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
    void autenticar_conCredencialesIncorrectas_lanzaIncorrectCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("credenciales invalidas"));

        assertThrows(IncorrectCredentials.class, () -> service.autenticar(login("12345678A", "malacontrasena")));
    }

    @Test
    void autenticar_conCuentaDeshabilitada_lanzaIncorrectCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.DisabledException("cuenta eliminada"));

        assertThrows(IncorrectCredentials.class, () -> service.autenticar(login("12345678A", "password123")));
    }

    @Test
    void autenticar_conFalloDeInfraestructura_lanzaAutenticacionOperacionException() {
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("BD caída"));

        assertThrows(com.hcc.tfm_hcc.exception.AutenticacionOperacionException.class,
                () -> service.autenticar(login("12345678A", "password123")));
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
        when(userRepository.findByEmailHash("hash-test@example.com")).thenReturn(Optional.of(usuarioPorEmail));
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
        when(userRepository.findByEmailHash("hash-noexiste@example.com")).thenReturn(Optional.empty());

        assertThrows(GoogleAuthenticationException.class, () -> service.autenticarConGoogle("noexiste@example.com", true));
    }

    @Test
    void autenticarConGoogle_conUserDetailsServiceSinUsuario_lanzaGoogleAuthenticationException() {
        Usuario usuarioPorEmail = new Usuario();
        usuarioPorEmail.setNif("12345678A");
        when(userRepository.findByEmailHash("hash-test@example.com")).thenReturn(Optional.of(usuarioPorEmail));
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

    @Test
    void generarYCanjearCodigoLoginGoogleConDosFactores_devuelveLoginResponsePendienteDeSegundoFactor() {
        String code = service.generarCodigoLoginGoogleConDosFactores("challenge-1");

        LoginResponse respuesta = service.canjearCodigoLoginGoogle(code);

        assertTrue(respuesta.isRequiresTwoFactor());
        assertEquals("challenge-1", respuesta.getChallengeId());
        assertNull(respuesta.getToken());
    }

    // ---- crearChallengeDosFactores / verificarCodigoDosFactores ----

    @Test
    void crearChallengeDosFactores_devuelveUnIdentificadorAsociadoAlUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        String challengeId = service.crearChallengeDosFactores(usuario);

        assertNotNull(challengeId);
        assertEquals(usuario.getId().toString(), challengesFalsos.get(challengeId).getUsuarioId());
    }

    @Test
    void verificarCodigoDosFactores_conCodigoValido_devuelveElUsuarioConAuthorities() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNif("12345678A");
        usuario.setTotpSecret("SECRETO");
        Usuario usuarioConAuthorities = new Usuario();
        usuarioConAuthorities.setNif("12345678A");

        String challengeId = service.crearChallengeDosFactores(usuario);
        when(userRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(totpService.validarCodigo("SECRETO", "123456")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("12345678A")).thenReturn(usuarioConAuthorities);

        Usuario resultado = service.verificarCodigoDosFactores(challengeId, "123456");

        assertEquals(usuarioConAuthorities, resultado);
        assertEquals(null, challengesFalsos.get(challengeId));
    }

    @Test
    void verificarCodigoDosFactores_conChallengeIdNulo_lanzaIncorrectCredentials() {
        assertThrows(IncorrectCredentials.class, () -> service.verificarCodigoDosFactores(null, "123456"));
    }

    @Test
    void verificarCodigoDosFactores_conChallengeInexistente_lanzaIncorrectCredentials() {
        assertThrows(IncorrectCredentials.class, () -> service.verificarCodigoDosFactores("no-existe", "123456"));
    }

    @Test
    void verificarCodigoDosFactores_conChallengeCaducado_lanzaIncorrectCredentialsYLoElimina() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        String challengeId = service.crearChallengeDosFactores(usuario);
        challengesFalsos.get(challengeId).setFechaExpiracion(java.time.Instant.now().minusSeconds(1));

        assertThrows(IncorrectCredentials.class, () -> service.verificarCodigoDosFactores(challengeId, "123456"));
        assertEquals(null, challengesFalsos.get(challengeId));
    }

    @Test
    void verificarCodigoDosFactores_conCodigoInvalido_incrementaIntentosYLanzaIncorrectCredentials() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setTotpSecret("SECRETO");
        String challengeId = service.crearChallengeDosFactores(usuario);
        when(userRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(totpService.validarCodigo("SECRETO", "000000")).thenReturn(false);

        assertThrows(IncorrectCredentials.class, () -> service.verificarCodigoDosFactores(challengeId, "000000"));

        assertEquals(1, challengesFalsos.get(challengeId).getIntentos());
    }

    @Test
    void verificarCodigoDosFactores_conMaximoDeIntentosSuperado_eliminaElChallenge() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setTotpSecret("SECRETO");
        String challengeId = service.crearChallengeDosFactores(usuario);
        when(userRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(totpService.validarCodigo("SECRETO", "000000")).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            assertThrows(IncorrectCredentials.class, () -> service.verificarCodigoDosFactores(challengeId, "000000"));
        }

        assertEquals(null, challengesFalsos.get(challengeId));
    }

    @Test
    void verificarCodigoDosFactores_conUsuarioDelChallengeYaNoExiste_lanzaIncorrectCredentials() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        String challengeId = service.crearChallengeDosFactores(usuario);
        when(userRepository.findById(usuario.getId())).thenReturn(Optional.empty());

        assertThrows(IncorrectCredentials.class, () -> service.verificarCodigoDosFactores(challengeId, "123456"));
    }

    @Test
    void verificarCodigoDosFactores_conUserDetailsServiceSinUsuario_lanzaIncorrectCredentials() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNif("12345678A");
        usuario.setTotpSecret("SECRETO");
        String challengeId = service.crearChallengeDosFactores(usuario);
        when(userRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(totpService.validarCodigo("SECRETO", "123456")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("12345678A")).thenReturn(mock(UserDetails.class));

        assertThrows(IncorrectCredentials.class, () -> service.verificarCodigoDosFactores(challengeId, "123456"));
    }
}
