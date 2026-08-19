package com.hcc.tfm_hcc.config;

import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.model.PerfilUsuario;
import com.hcc.tfm_hcc.converter.AESEncryptionConverter;
import com.hcc.tfm_hcc.service.JwtService;
import com.hcc.tfm_hcc.service.AccessLogService;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    
    private final UsuarioRepository usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;

    @Value("${security.enforce-https:false}")
    private boolean enforceHttps;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Bean
    public UserDetailsService userDetailsService() {
        return nif -> {
            Usuario usuario = findUsuarioByNifLegacyAware(nif)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            var perfiles = findPerfilesByUsuarioLegacyAware(usuario);
            var authorities = perfiles.stream()
                .map(p -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + p.getRol()))
                .toList();
            usuario.setAuthorities(authorities);
            return usuario;
        };
    }

    private Optional<Usuario> findUsuarioByNifLegacyAware(String nif) {
        Optional<Usuario> usuarioDirecto = usuarioRepository.findByNif(nif);
        if (usuarioDirecto.isPresent()) {
            return usuarioDirecto;
        }

        for (Usuario usuario : usuarioRepository.findAll()) {
            if (usuario != null && nif != null && nif.equals(usuario.getNif())) {
                return Optional.of(usuario);
            }
        }

        return Optional.empty();
    }

    private List<com.hcc.tfm_hcc.model.Perfil> findPerfilesByUsuarioLegacyAware(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return List.of();
        }

        List<com.hcc.tfm_hcc.model.Perfil> perfiles = perfilUsuarioRepository.getPerfilesByNif(usuario.getNif());
        if (!perfiles.isEmpty()) {
            return perfiles;
        }

        List<com.hcc.tfm_hcc.model.Perfil> perfilesRespaldo = new java.util.ArrayList<>();
        for (PerfilUsuario perfilUsuario : perfilUsuarioRepository.findAll()) {
            if (perfilUsuario != null
                && perfilUsuario.getUsuario() != null
                && usuario.getId().equals(perfilUsuario.getUsuario().getId())
                && perfilUsuario.getPerfil() != null) {
                perfilesRespaldo.add(perfilUsuario.getPerfil());
            }
        }

        return perfilesRespaldo;
    }

    @Bean
    public PasswordEncoder passwordEncoder(AESEncryptionConverter encryptionConverter) {
        return new LegacyAwarePasswordEncoder(encryptionConverter);
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, HandlerExceptionResolver handlerExceptionResolver) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService, handlerExceptionResolver);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, AccessLogFilter accessLogFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Agregar CORS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/authentication/login").permitAll()
                .requestMatchers("/authentication/signup").permitAll()
                .requestMatchers("/authentication/google/login").permitAll()
                .requestMatchers("/authentication/google/signup").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/medico/**").hasRole("MEDICO")
                .anyRequest().authenticated()
            );

        if (googleClientId != null && !googleClientId.isBlank()) {
            http.oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/", true));
        }

        http
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Registrar el filtro de logging después de que la autenticación JWT se haya procesado
            .addFilterAfter(accessLogFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true))
                .referrerPolicy(ref -> ref.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            );

    // Nota: Enforcement HTTPS se recomienda via reverse proxy (Nginx/Apache) para evitar APIs deprecated.
        return http.build();
    }

    @Bean
    public AccessLogFilter accessLogFilter(AccessLogService accessLogService) {
        return new AccessLogFilter(accessLogService, usuarioRepository);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:8080", "https://localhost:8080")); // Frontend Vue.js (HTTP y HTTPS dev)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Incluir OPTIONS
        configuration.setAllowedHeaders(List.of("*")); // Permitir todos los headers
        configuration.setAllowCredentials(true); // Permitir credenciales
        configuration.setMaxAge(3600L); // Cache preflight por 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**",configuration);

        return source;
    }
}
