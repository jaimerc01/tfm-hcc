package com.hcc.tfm_hcc.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;

import lombok.RequiredArgsConstructor;

/**
 * Define {@link UserDetailsService}, {@link PasswordEncoder} y {@link AuthenticationManager}
 * en una clase de configuración separada de {@link WebSecurityConfig}.
 *
 * <p>Es necesario para evitar un ciclo de dependencias: el login con Google necesita
 * {@code AutenticacionServiceImpl}, que a su vez depende de {@code AuthenticationManager} y de
 * {@code UserDetailsService} (para cargar las authorities del usuario). Esa cadena de
 * dependencias (facade → success handler) termina siendo inyectada en el propio
 * {@code WebSecurityConfig}, así que estos beans no pueden vivir ahí: si lo hicieran, construir
 * {@code WebSecurityConfig} exigiría tener ya construidos beans que a su vez exigen tener ya
 * construido {@code WebSecurityConfig}.</p>
 */
@Configuration
@RequiredArgsConstructor
public class AuthenticationBeansConfig {

    private final UsuarioRepository usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final HmacSearchIndexService hmacSearchIndexService;

    @Bean
    public UserDetailsService userDetailsService() {
        return nif -> {
            Usuario usuario = usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nif))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            List<Perfil> perfiles = perfilUsuarioRepository.getPerfilesByNifHash(usuario.getNifHash());
            var authorities = perfiles.stream()
                    .map(p -> new SimpleGrantedAuthority("ROLE_" + p.getRol()))
                    .toList();
            usuario.setAuthorities(authorities);
            return usuario;
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }
}
