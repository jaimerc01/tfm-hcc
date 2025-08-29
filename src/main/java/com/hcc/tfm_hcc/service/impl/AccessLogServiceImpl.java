package com.hcc.tfm_hcc.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.repository.AccessLogRepository;
import com.hcc.tfm_hcc.service.AccessLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de logs de acceso.
 * Proporciona funcionalidades para registrar y gestionar logs de acceso al sistema.
 * 
 * @author Sistema HCC
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessLogServiceImpl implements AccessLogService {

    private final AccessLogRepository accessLogRepository;

    /**
     * Valida que el log de acceso no sea nulo y contenga información mínima requerida
     */
    private void validarAccessLog(AccessLog accessLog) {
        if (accessLog == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("AccessLog"));
        }
        
        if (accessLog.getUsuarioId() == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("usuario ID"));
        }
        
        if (accessLog.getRuta() == null || accessLog.getRuta().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("ruta"));
        }
        
        if (accessLog.getMetodo() == null || accessLog.getMetodo().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("metodo"));
        }
    }

    /**
     * Prepara el log con valores por defecto si es necesario
     */
    private void prepararAccessLog(AccessLog accessLog) {
        // Establecer timestamp si no está presente
        if (accessLog.getTimestamp() == null) {
            accessLog.setTimestamp(java.time.LocalDateTime.now());
        }
        
        // Establecer estado por defecto si no está presente
        if (accessLog.getEstado() == null) {
            accessLog.setEstado(200); // HTTP 200 OK por defecto
        }
        
        // Limpiar y normalizar datos
        if (accessLog.getRuta() != null) {
            accessLog.setRuta(accessLog.getRuta().trim());
        }
        
        if (accessLog.getMetodo() != null) {
            accessLog.setMetodo(accessLog.getMetodo().trim().toUpperCase());
        }
        
        if (accessLog.getIp() != null) {
            accessLog.setIp(accessLog.getIp().trim());
        }
    }

    /**
     * Persiste el log de acceso en la base de datos
     */
    @Transactional
    private void persistirAccessLog(AccessLog accessLog) {
        try {
            accessLogRepository.save(accessLog);
            log.debug("Log de acceso guardado exitosamente: usuarioId={}, ruta={}, metodo={}", 
                     accessLog.getUsuarioId(), accessLog.getRuta(), accessLog.getMetodo());
        } catch (Exception e) {
            log.error("Error al guardar log de acceso: usuarioId={}, ruta={}, metodo={}, error={}", 
                     accessLog.getUsuarioId(), accessLog.getRuta(), accessLog.getMetodo(), e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.formatError("Error al guardar log de acceso: {0}", e.getMessage()), e);
        }
    }

    /**
     * Registra un log de acceso en el sistema
     * 
     * @param accessLog Log de acceso a registrar
     * @throws IllegalArgumentException si el log es inválido
     * @throws RuntimeException si hay error al persistir el log
     */
    @Override
    public void log(AccessLog accessLog) {
        log.info("Registrando log de acceso: usuarioId={}, ruta={}, metodo={}", 
                accessLog != null ? accessLog.getUsuarioId() : "null",
                accessLog != null ? accessLog.getRuta() : "null", 
                accessLog != null ? accessLog.getMetodo() : "null");
        
        validarAccessLog(accessLog);
        prepararAccessLog(accessLog);
        persistirAccessLog(accessLog);
    }
}
