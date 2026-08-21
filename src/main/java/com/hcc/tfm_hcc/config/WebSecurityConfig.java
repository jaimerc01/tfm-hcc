package com.hcc.tfm_hcc.config;

import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.JwtService;
import com.hcc.tfm_hcc.service.AccessLogService;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    private final GoogleOAuth2AuthenticationSuccessHandler googleOAuth2AuthenticationSuccessHandler;
    private final GoogleOAuth2AuthenticationFailureHandler googleOAuth2AuthenticationFailureHandler;

    @Value("${security.enforce-https:false}")
    private boolean enforceHttps;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, HandlerExceptionResolver handlerExceptionResolver) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService, handlerExceptionResolver);
    }

    /**
     * Cadena de filtros dedicada exclusivamente al handshake OAuth2 con Google
     * ({@code /oauth2/**} y {@code /login/oauth2/**}). Necesita permitir sesión HTTP
     * (a diferencia del resto de la API, que es STATELESS) porque Spring Security guarda
     * en sesión la petición de autorización pendiente entre la redirección a Google y la
     * vuelta. Esa sesión es efímera: solo vive durante el propio intercambio OAuth2, ya
     * que la aplicación no la usa para nada más (login normal y llamadas posteriores siguen
     * siendo JWT sin estado).
     */
    @Bean
    @Order(1)
    public SecurityFilterChain oauth2LoginFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/oauth2/**", "/login/oauth2/**")
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(requests -> requests.anyRequest().permitAll());

        if (googleClientId != null && !googleClientId.isBlank()) {
            http.oauth2Login(oauth2 -> oauth2
                .successHandler(googleOAuth2AuthenticationSuccessHandler)
                .failureHandler(googleOAuth2AuthenticationFailureHandler));
        }

        aplicarCabecerasSeguridad(http);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, AccessLogFilter accessLogFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Agregar CORS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/authentication/login").permitAll()
                .requestMatchers("/authentication/signup").permitAll()
                .requestMatchers("/authentication/google/login").permitAll()
                .requestMatchers("/authentication/google/token").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/medico/**").hasRole("MEDICO")
                .anyRequest().authenticated()
            );

        http
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Registrar el filtro de logging después de que la autenticación JWT se haya procesado
            .addFilterAfter(accessLogFilter, UsernamePasswordAuthenticationFilter.class);

        aplicarCabecerasSeguridad(http);

    // Nota: Enforcement HTTPS se recomienda via reverse proxy (Nginx/Apache) para evitar APIs deprecated.
        return http.build();
    }

    /**
     * Aplica el mismo conjunto de cabeceras de seguridad (CSP, X-XSS-Protection, X-Frame-Options,
     * HSTS, Referrer-Policy) a cualquier cadena de filtros de la aplicación.
     */
    private void aplicarCabecerasSeguridad(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
            .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
            .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            .frameOptions(frame -> frame.deny())
            .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true))
            .referrerPolicy(ref -> ref.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
        );
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
