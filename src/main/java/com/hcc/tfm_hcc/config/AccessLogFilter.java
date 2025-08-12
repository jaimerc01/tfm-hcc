package com.hcc.tfm_hcc.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StopWatch;

import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.service.AccessLogService;
import com.hcc.tfm_hcc.repository.UsuarioRepository;

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
public class AccessLogFilter implements jakarta.servlet.Filter {

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

        StopWatch sw = new StopWatch();
        sw.start();
        try {
            chain.doFilter(request, response);
        } finally {
            sw.stop();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuarioId = null;
        if (auth != null 
            && !(auth instanceof AnonymousAuthenticationToken)
            && auth.getPrincipal() instanceof UserDetails userDetails) {
        String nif = userDetails.getUsername();
        usuarioId = usuarioRepository.findByNif(nif)
            .map(u -> u.getId() != null ? u.getId().toString() : null)
            .orElse(null);
        }

            AccessLog log = new AccessLog();
            log.setTimestamp(LocalDateTime.now());
            log.setUsuarioId(usuarioId);
            log.setMetodo(httpReq.getMethod());
            log.setRuta(httpReq.getRequestURI());
            log.setEstado(httpRes.getStatus());
            log.setIp(getClientIp(httpReq));
            log.setUserAgent(httpReq.getHeader("User-Agent"));
            log.setDuracionMs(sw.getTotalTimeMillis());
            accessLogService.log(log);
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
