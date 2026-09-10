package com.hcc.tfm_hcc.config;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hcc.tfm_hcc.converter.EncryptionKeyProvider;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.impl.HmacSearchIndexServiceImpl;

class AuthenticationBeansConfigTest {

    private static final String CLAVE_PRUEBAS_BASE64 =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private UsuarioRepository usuarioRepository;
    private PerfilUsuarioRepository perfilUsuarioRepository;
    private HmacSearchIndexService hmacSearchIndexService;
    private AuthenticationBeansConfig config;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        perfilUsuarioRepository = mock(PerfilUsuarioRepository.class);
        hmacSearchIndexService = new HmacSearchIndexServiceImpl(new EncryptionKeyProvider(CLAVE_PRUEBAS_BASE64));
        config = new AuthenticationBeansConfig(usuarioRepository, perfilUsuarioRepository, hmacSearchIndexService);
    }

    private Usuario usuarioConId(String nif) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNif(nif);
        usuario.setNifHash(hmacSearchIndexService.indexar(nif));
        return usuario;
    }

    @Test
    void userDetailsService_conUsuarioYPerfiles_asignaLasAuthorities() {
        Usuario usuario = usuarioConId("12345678A");
        Perfil perfil = new Perfil();
        perfil.setRol("MEDICO");
        when(usuarioRepository.findByNifHash(usuario.getNifHash())).thenReturn(Optional.of(usuario));
        when(perfilUsuarioRepository.getPerfilesByNifHash(usuario.getNifHash())).thenReturn(List.of(perfil));

        UserDetailsService uds = config.userDetailsService();
        UserDetails resultado = uds.loadUserByUsername("12345678A");

        assertTrue(resultado.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEDICO")));
    }

    @Test
    void userDetailsService_conUsuarioInexistente_lanzaUsernameNotFoundException() {
        when(usuarioRepository.findByNifHash(hmacSearchIndexService.indexar("00000000Z"))).thenReturn(Optional.empty());

        UserDetailsService uds = config.userDetailsService();

        assertThrows(UsernameNotFoundException.class, () -> uds.loadUserByUsername("00000000Z"));
    }

    @Test
    void userDetailsService_sinPerfilesAsignados_devuelveUsuarioSinAuthorities() {
        Usuario usuario = usuarioConId("12345678A");
        when(usuarioRepository.findByNifHash(usuario.getNifHash())).thenReturn(Optional.of(usuario));
        when(perfilUsuarioRepository.getPerfilesByNifHash(usuario.getNifHash())).thenReturn(List.of());

        UserDetailsService uds = config.userDetailsService();
        UserDetails resultado = uds.loadUserByUsername("12345678A");

        assertTrue(resultado.getAuthorities().isEmpty());
    }

    @Test
    void passwordEncoder_devuelveUnBCryptPasswordEncoder() {
        PasswordEncoder encoder = config.passwordEncoder();

        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void authenticationManager_conCredencialesCorrectas_autenticaAlUsuario() {
        PasswordEncoder encoder = config.passwordEncoder();
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
        PasswordEncoder encoder = config.passwordEncoder();
        Usuario usuario = usuarioConId("12345678A");
        usuario.setPassword(encoder.encode("password123"));
        UserDetailsService uds = nif -> usuario;

        AuthenticationManager authenticationManager = config.authenticationManager(uds, encoder);

        assertThrows(BadCredentialsException.class, () -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("12345678A", "incorrecta")));
    }
}
