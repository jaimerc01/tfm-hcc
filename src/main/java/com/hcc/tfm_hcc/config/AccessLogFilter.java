package com.hcc.tfm_hcc.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StopWatch;

import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AccessLogService;

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

    public AccessLogFilter(AccessLogService accessLogService, UsuarioRepository usuarioRepository) {
        this.accessLogService = accessLogService;
        this.usuarioRepository = usuarioRepository;
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
        if (auth != null 
            && !(auth instanceof AnonymousAuthenticationToken)
            && auth.getPrincipal() instanceof UserDetails userDetails) {
        String nif = userDetails.getUsername();
        usuario = usuarioRepository.findByNif(nif).isPresent() ? usuarioRepository.findByNif(nif).get().getId().toString() : null;
        }

            try {
                AccessLog log = new AccessLog();
                log.setTimestamp(LocalDateTime.now());
                log.setUsuarioId(usuario);
                log.setMetodo(httpReq.getMethod());
                log.setRuta(httpReq.getRequestURI());
                log.setEstado(httpRes.getStatus());
                log.setIp(getClientIp(httpReq));
                log.setUserAgent(httpReq.getHeader("User-Agent"));
                log.setDuracionMs(sw.getTotalTimeMillis());
                accessLogService.log(log);
            } catch (Exception e) {
                // No propagar para no romper la petición; podrías usar un logger aquí
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
