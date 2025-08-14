package com.hcc.tfm_hcc.config;

import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.JwtService;
import com.hcc.tfm_hcc.service.AccessLogService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @Value("${security.enforce-https:false}")
    private boolean enforceHttps;

    @Bean
    public UserDetailsService userDetailsService() {
        return nif -> {
            Usuario usuario = usuarioRepository.findByNif(nif)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            var perfiles = perfilUsuarioRepository.getPerfilesByNif(nif);
            var authorities = perfiles.stream()
                .map(p -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + p.getRol()))
                .toList();
            usuario.setAuthorities(authorities);
            return usuario;
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/medico/**").hasRole("MEDICO")
                .anyRequest().authenticated()
            )
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
