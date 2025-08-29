package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.model.AccessLog;

/**
 * Servicio para la gestión de logs de acceso en el sistema HCC.
 * Proporciona funcionalidades para registrar y auditar los accesos
 * de usuarios al sistema, manteniendo un historial detallado
 * de la actividad para fines de seguridad y cumplimiento normativo.
 * 
 * <p>Este servicio permite:</p>
 * <ul>
 *   <li>Registro automático de accesos de usuarios</li>
 *   <li>Auditoría de actividad del sistema</li>
 *   <li>Trazabilidad para cumplimiento de normativas</li>
 *   <li>Análisis de patrones de uso</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface AccessLogService {

    /**
     * Registra un nuevo acceso en el sistema de auditoría.
     * Esta operación almacena información detallada sobre el acceso
     * del usuario incluyendo timestamp, IP, user agent y resultado.
     * 
     * @param log AccessLog que contiene toda la información del acceso a registrar
     * @throws IllegalArgumentException si el log proporcionado es null o inválido
     * @throws IllegalStateException si hay problemas al persistir el log
     */
    void log(AccessLog log) throws IllegalArgumentException, IllegalStateException;
}
