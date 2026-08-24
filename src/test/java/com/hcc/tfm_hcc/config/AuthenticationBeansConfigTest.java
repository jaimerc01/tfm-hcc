package com.hcc.tfm_hcc.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hcc.tfm_hcc.converter.AESEncryptionConverter;
import com.hcc.tfm_hcc.converter.EncryptionKeyProvider;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.model.PerfilUsuario;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;

class AuthenticationBeansConfigTest {

    private static final String CLAVE_PRUEBAS_BASE64 =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private UsuarioRepository usuarioRepository;
    private PerfilUsuarioRepository perfilUsuarioRepository;
    private AuthenticationBeansConfig config;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        perfilUsuarioRepository = mock(PerfilUsuarioRepository.class);
        config = new AuthenticationBeansConfig(usuarioRepository, perfilUsuarioRepository);
    }

    private Usuario usuarioConId(String nif) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNif(nif);
        return usuario;
    }

    @Test
    void userDetailsService_conUsuarioYPerfilesDirectos_asignaLasAuthorities() {
        Usuario usuario = usuarioConId("12345678A");
        Perfil perfil = new Perfil();
        perfil.setRol("MEDICO");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        when(perfilUsuarioRepository.getPerfilesByNif("12345678A")).thenReturn(List.of(perfil));

        UserDetailsService uds = config.userDetailsService();
        UserDetails resultado = uds.loadUserByUsername("12345678A");

        assertTrue(resultado.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEDICO")));
    }

    @Test
    void userDetailsService_conUsuarioSoloEncontradoPorFindAll_loCargaIgual() {
        Usuario usuario = usuarioConId("12345678A");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.empty());
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(perfilUsuarioRepository.getPerfilesByNif("12345678A")).thenReturn(List.of());
        when(perfilUsuarioRepository.findAll()).thenReturn(List.of());

        UserDetailsService uds = config.userDetailsService();

        assertEquals("12345678A", uds.loadUserByUsername("12345678A").getUsername());
    }

    @Test
    void userDetailsService_conUsuarioInexistente_lanzaUsernameNotFoundException() {
        when(usuarioRepository.findByNif("00000000Z")).thenReturn(Optional.empty());
        when(usuarioRepository.findAll()).thenReturn(List.of());

        UserDetailsService uds = config.userDetailsService();

        assertThrows(UsernameNotFoundException.class, () -> uds.loadUserByUsername("00000000Z"));
    }

    @Test
    void userDetailsService_conPerfilesSoloPorFindAll_losRecuperaComoRespaldo() {
        Usuario usuario = usuarioConId("12345678A");
        Perfil perfil = new Perfil();
        perfil.setRol("PACIENTE");
        PerfilUsuario relacion = new PerfilUsuario();
        relacion.setUsuario(usuario);
        relacion.setPerfil(perfil);
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        when(perfilUsuarioRepository.getPerfilesByNif("12345678A")).thenReturn(List.of());
        when(perfilUsuarioRepository.findAll()).thenReturn(List.of(relacion));

        UserDetailsService uds = config.userDetailsService();
        UserDetails resultado = uds.loadUserByUsername("12345678A");

        assertTrue(resultado.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACIENTE")));
    }

    @Test
    void passwordEncoder_devuelveUnLegacyAwarePasswordEncoder() {
        AESEncryptionConverter converter = new AESEncryptionConverter(new EncryptionKeyProvider(CLAVE_PRUEBAS_BASE64));

        PasswordEncoder encoder = config.passwordEncoder(converter);

        assertTrue(encoder instanceof LegacyAwarePasswordEncoder);
    }

    @Test
    void authenticationManager_conCredencialesCorrectas_autenticaAlUsuario() {
        AESEncryptionConverter converter = new AESEncryptionConverter(new EncryptionKeyProvider(CLAVE_PRUEBAS_BASE64));
        PasswordEncoder encoder = config.passwordEncoder(converter);
        Usuario usuario = usuarioConId("12345678A");
        usuario.setPassword(encoder.encode("password123"));
        UserDetailsService uds = nif -> usuario;

        AuthenticationManager authenticationManager = config.authenticationManager(uds, encoder);
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("12345678A", "password123"));

        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void authenticationManager_conCredencialesIncorrectas_lanzaBadCredentialsException() {
        AESEncryptionConverter converter = new AESEncryptionConverter(new EncryptionKeyProvider(CLAVE_PRUEBAS_BASE64));
        PasswordEncoder encoder = config.passwordEncoder(converter);
        Usuario usuario = usuarioConId("12345678A");
        usuario.setPassword(encoder.encode("password123"));
        UserDetailsService uds = nif -> usuario;

        AuthenticationManager authenticationManager = config.authenticationManager(uds, encoder);

        assertThrows(BadCredentialsException.class, () -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("12345678A", "incorrecta")));
    }
}
