package com.hcc.tfm_hcc.config;

import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.JwtService;
import com.hcc.tfm_hcc.service.AccessLogService;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
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
    private final HmacSearchIndexService hmacSearchIndexService;
    private final GoogleOAuth2AuthenticationSuccessHandler googleOAuth2AuthenticationSuccessHandler;
    private final GoogleOAuth2AuthenticationFailureHandler googleOAuth2AuthenticationFailureHandler;

    // Variables sueltas (no bajo spring.security.oauth2.client.*) a propósito: si se declarase
    // ese árbol de propiedades en application.yml, la auto-configuración de OAuth2 de Spring Boot
    // intenta construir el ClientRegistration de forma incondicional y falla el arranque en
    // cuanto el client-id está en blanco. Además, activar ese árbol solo bajo un profile de
    // Spring resultó frágil: springboot3-dotenv carga el .env después de que Spring ya haya
    // decidido qué profiles activar, así que un SPRING_PROFILES_ACTIVE puesto en .env llega
    // tarde. Al leerlas como @Value sueltas (igual que TFM_HCC_ENCRYPTION_KEY), se resuelven de
    // forma perezosa al crear el bean, momento en el que el entorno ya está completo sin
    // importar cómo se haya cargado (.env, variable de entorno real, configuración del IDE...).
    @Value("${GOOGLE_CLIENT_ID:}")
    private String googleClientId;

    @Value("${GOOGLE_CLIENT_SECRET:}")
    private String googleClientSecret;

    @Value("${security.cors.allowed-origins:http://localhost:8080,https://localhost:8080}")
    private String allowedOrigins;

    /**
     * Repositorio de registros OAuth2. Solo contiene el registro de Google cuando
     * GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET están presentes; si no, el login con Google
     * queda desactivado (ver el guard en {@link #oauth2LoginFilterChain}) y este bean no
     * llega a usarse, pero se declara siempre para evitar que la auto-configuración de
     * OAuth2 de Spring Boot intente construir uno a partir de propiedades inexistentes.
     */
    private boolean googleLoginHabilitado() {
        return googleClientId != null && !googleClientId.isBlank()
                && googleClientSecret != null && !googleClientSecret.isBlank();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        if (!googleLoginHabilitado()) {
            // OJO: el constructor de InMemoryClientRegistrationRepository que recibe una List
            // rechaza (Assert.notEmpty) una lista vacía y rompería el arranque en este caso, que
            // es el caso normal sin Google configurado. El constructor que recibe un Map sí
            // admite vacío (solo exige que no sea null).
            return new InMemoryClientRegistrationRepository(Map.of());
        }

        ClientRegistration googleRegistration = ClientRegistrations.fromOidcIssuerLocation("https://accounts.google.com")
                .registrationId("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .scope("openid", "profile", "email")
                .build();

        return new InMemoryClientRegistrationRepository(googleRegistration);
    }

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

        if (googleLoginHabilitado()) {
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
                .requestMatchers("/authentication/login/2fa").permitAll()
                .requestMatchers("/authentication/signup").permitAll()
                .requestMatchers("/authentication/google/login").permitAll()
                .requestMatchers("/authentication/google/token").permitAll()
                .requestMatchers("/authentication/password-reset/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/medico/**").hasRole("MEDICO")
                .anyRequest().authenticated()
            );

        http
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Registrar el filtro de logging después de que la autenticación JWT se haya procesado
            .addFilterAfter(accessLogFilter, UsernamePasswordAuthenticationFilter.class);

        aplicarCabecerasSeguridad(http);

        // Nota: el enforcement de HTTPS se resuelve fuera de Spring Security, vía
        // HttpToHttpsRedirectConfig (redirección a nivel de conector Tomcat, activa
        // cuando server.ssl.enabled=true) o, en despliegues detrás de reverse proxy
        // (Nginx/Apache), delegando esa redirección en el propio proxy.
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
        return new AccessLogFilter(accessLogService, usuarioRepository, hmacSearchIndexService);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos: configurables vía security.cors.allowed-origins (application.yml)
        // o la variable de entorno correspondiente, para poder ajustarlos por entorno sin tocar
        // código (en producción, el origen real del frontend desplegado).
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Incluir OPTIONS
        configuration.setAllowedHeaders(List.of("*")); // Permitir todos los headers
        configuration.setAllowCredentials(true); // Permitir credenciales
        configuration.setMaxAge(3600L); // Cache preflight por 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**",configuration);

        return source;
    }
}
