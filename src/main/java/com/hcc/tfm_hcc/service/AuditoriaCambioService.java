package com.hcc.tfm_hcc.service;

import java.time.LocalDateTime;
import java.util.List;

import com.hcc.tfm_hcc.model.AuditoriaCambio;

/**
 * Servicio para gestionar registros de auditoría de cambios en datos clínicos.
 * 
 * Responsabilidades:
 * - Registrar cambios (CREATE, UPDATE, DELETE) en datos médicos
 * - Mantener trazabilidad completa de quién cambió qué y cuándo
 * - Consultar historial de cambios de pacientes y recursos
 * - Asegurar cumplimiento con RGPD y normativa sanitaria
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface AuditoriaCambioService {

    /**
     * Registra un cambio en un dato clínico.
     * 
     * @param auditoria Registro de auditoría con los detalles del cambio
     * @return Registro de auditoría guardado con ID asignado
     */
    AuditoriaCambio registrarCambio(AuditoriaCambio auditoria);

    /**
     * Registra un cambio específico en un dato clínico con parámetros individuales.
     * 
     * @param idUsuario UUID del usuario que realizó el cambio
     * @param idPaciente UUID del paciente afectado
     * @param idMedico UUID del médico (si aplica), o null
     * @param tipoCambio Tipo de dato que cambió (ej: "GLUCOSA")
     * @param tabla Nombre de la tabla modificada
     * @param idRecurso UUID del recurso específico
     * @param valorAnterior Valor antes del cambio
     * @param valorNuevo Valor después del cambio
     * @param tipoOperacion Tipo de operación (CREATE, UPDATE, DELETE)
     * @param razonCambio Razón o justificación del cambio
     * @return Registro de auditoría guardado
     */
    AuditoriaCambio registrarCambio(
        String idUsuario,
        String idPaciente,
        String idMedico,
        String tipoCambio,
        String tabla,
        String idRecurso,
        String valorAnterior,
        String valorNuevo,
        AuditoriaCambio.TipoOperacion tipoOperacion,
        String razonCambio
    );

    /**
     * Obtiene el historial de cambios de un usuario autenticado.
     * 
     * @param idUsuario UUID del usuario
     * @return Lista de cambios ordenados por fecha descendente
     */
    List<AuditoriaCambio> obtenerHistorialCambiosUsuario(String idUsuario);

    /**
     * Obtiene el historial de cambios de un paciente específico.
     * 
     * @param idPaciente UUID del paciente
     * @return Lista de cambios ordenados por fecha descendente
     */
    List<AuditoriaCambio> obtenerHistorialCambiosPaciente(String idPaciente);

    /**
     * Obtiene los cambios en un rango de fechas para un paciente.
     * 
     * @param idPaciente UUID del paciente
     * @param desde Fecha de inicio (inclusive)
     * @param hasta Fecha de fin (inclusive)
     * @return Lista de cambios en el período especificado
     */
    List<AuditoriaCambio> obtenerHistorialCambiosPaciente(
        String idPaciente, 
        LocalDateTime desde, 
        LocalDateTime hasta
    );

    /**
     * Obtiene el historial de cambios de un recurso específico.
     * Útil para rastrear todas las modificaciones de un dato clínico particular.
     * 
     * @param idRecurso UUID del recurso (ej: UUID de un DatoClinico)
     * @return Lista de cambios en ese recurso
     */
    List<AuditoriaCambio> obtenerHistorialCambiosRecurso(String idRecurso);

    /**
     * Obtiene los cambios de un tipo específico para un paciente.
     * 
     * @param idPaciente UUID del paciente
     * @param tipoCambio Tipo de dato (ej: "GLUCOSA", "PRESION_ARTERIAL")
     * @return Lista de cambios del tipo especificado
     */
    List<AuditoriaCambio> obtenerCambiosPorTipo(String idPaciente, String tipoCambio);

    /**
     * Obtiene los cambios realizados por un médico específico.
     * 
     * @param idMedico UUID del médico
     * @return Lista de cambios realizados por ese médico
     */
    List<AuditoriaCambio> obtenerCambiosPorMedico(String idMedico);

    /**
     * Obtiene los cambios de un tipo de operación específico para un paciente.
     * 
     * @param idPaciente UUID del paciente
     * @param tipoOperacion Tipo de operación (CREATE, UPDATE, DELETE)
     * @return Lista de registros del tipo de operación
     */
    List<AuditoriaCambio> obtenerCambiosPorTipoOperacion(
        String idPaciente, 
        AuditoriaCambio.TipoOperacion tipoOperacion
    );

    /**
     * Verifica si hay cambios sin autorizar (cambios no justificados o sospechosos).
     * Útil para detectar intentos de modificación no autorizados.
     * 
     * @param idPaciente UUID del paciente
     * @param desde Fecha de inicio
     * @param hasta Fecha de fin
     * @return true si hay cambios sin razonamiento claro, false en caso contrario
     */
    boolean hayAuditoriaSospechosa(String idPaciente, LocalDateTime desde, LocalDateTime hasta);

    /**
     * Obtiene estadísticas de cambios para un paciente.
     * 
     * @param idPaciente UUID del paciente
     * @return Objeto con estadísticas (número de cambios, tipos, usuarios, etc.)
     */
    AuditoriaCambioStats obtenerEstadisticas(String idPaciente);

}
