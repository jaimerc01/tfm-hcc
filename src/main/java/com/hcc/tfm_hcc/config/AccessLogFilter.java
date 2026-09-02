package com.hcc.tfm_hcc.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StopWatch;

import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AccessLogService;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.util.LogMaskUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de logging de accesos. Asegúrate de registrarlo SOLO una vez en la cadena de Spring Security
 * (se elimina @Component para evitar doble registro implícito + explícito).
 */
public class AccessLogFilter implements Filter {

    private final AccessLogService accessLogService;
    private final UsuarioRepository usuarioRepository;
    private final HmacSearchIndexService hmacSearchIndexService;
    private static final String ZONE_ID = "Europe/Madrid";

    /**
     * Reconoce un NIF/NIE español (8 dígitos + letra, o letra X/Y/Z + 7 dígitos + letra)
     * dentro de una ruta, p. ej. el {@code {nif}} de {@code /pacientes/{nif}/historial}.
     * La propia ruta HTTP no debe guardarse con el identificador del paciente en claro:
     * el resto de la aplicación cifra el NIF en todas partes (ver {@code Usuario.nif}), y
     * el log de accesos no debía ser la única excepción.
     */
    private static final Pattern NIF_EN_RUTA = Pattern.compile("(?i)\\b(?:[0-9]{8}[A-Z]|[XYZ][0-9]{7}[A-Z])\\b");

    public AccessLogFilter(AccessLogService accessLogService, UsuarioRepository usuarioRepository,
            HmacSearchIndexService hmacSearchIndexService) {
        this.accessLogService = accessLogService;
        this.usuarioRepository = usuarioRepository;
        this.hmacSearchIndexService = hmacSearchIndexService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        // Evitar doble registro: si ya marcado o si es preflight OPTIONS, salir rápido
        if (httpReq.getAttribute("__ACCESS_LOGGED__") != null || "OPTIONS".equalsIgnoreCase(httpReq.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        httpReq.setAttribute("__ACCESS_LOGGED__", Boolean.TRUE);

        // Ignorar recursos estáticos comunes (ajusta rutas según necesidad)
        String uri = httpReq.getRequestURI();
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") || uri.startsWith("/favicon")) {
            chain.doFilter(request, response);
            return;
        }

        StopWatch sw = new StopWatch();
        sw.start();
        String usuario = null;
        try {
            chain.doFilter(request, response);
        } finally {
            sw.stop();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
                usuario = resolverIdUsuario(auth.getPrincipal());
            }

            try {
                AccessLog log = new AccessLog();
                log.setTimestamp(LocalDateTime.now(ZoneId.of(ZONE_ID)));
                log.setUsuarioId(usuario);
                log.setMetodo(httpReq.getMethod());
                log.setRuta(enmascararNifEnRuta(httpReq.getRequestURI()));
                log.setEstado(httpRes.getStatus());
                log.setIp(getClientIp(httpReq));
                log.setUserAgent(httpReq.getHeader("User-Agent"));
                log.setDuracionMs(sw.getTotalTimeMillis());
                accessLogService.log(log);
            } catch (Exception _) {
                // No propagar para no romper la petición
            }
        }
    }

    /**
     * Sustituye cualquier NIF/NIE que aparezca dentro de una ruta por su forma enmascarada
     * ({@link LogMaskUtil#enmascarar(String)}), para que el log de accesos no sea el único
     * sitio de la aplicación donde ese identificador queda en claro.
     */
    private String enmascararNifEnRuta(String ruta) {
        if (ruta == null) {
            return null;
        }
        Matcher matcher = NIF_EN_RUTA.matcher(ruta);
        StringBuilder resultado = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(resultado, Matcher.quoteReplacement(LogMaskUtil.enmascarar(matcher.group())));
        }
        matcher.appendTail(resultado);
        return resultado.toString();
    }

    /**
     * Devuelve el id del usuario autenticado para el registro de acceso.
     *
     * <p>En el flujo normal el {@code principal} ya es la entidad {@link Usuario} (la coloca
     * {@code JwtAuthenticationFilter}), así que su id se lee directamente y no hace falta
     * ninguna consulta a base de datos en cada petición. El {@code findByNifHash} se mantiene
     * solo como respaldo defensivo para principals que no sean nuestra entidad.</p>
     */
    private String resolverIdUsuario(Object principal) {
        if (principal instanceof Usuario usuarioAutenticado && usuarioAutenticado.getId() != null) {
            return usuarioAutenticado.getId().toString();
        }
        if (principal instanceof UserDetails userDetails) {
            return usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(userDetails.getUsername()))
                    .map(u -> u.getId().toString())
                    .orElse(null);
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
