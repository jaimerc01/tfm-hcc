package com.hcc.tfm_hcc.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.AuditoriaCambio;

/**
 * Repositorio para operaciones CRUD sobre registros de auditoría en MongoDB.
 * 
 * Proporciona consultas especializadas para:
 * - Obtener historial de cambios de un usuario
 * - Obtener historial de cambios de un paciente
 * - Obtener historial de cambios de un recurso específico
 * - Filtrar por tipo de operación y rango de fechas
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface AuditoriaCambioRepository extends MongoRepository<AuditoriaCambio, String> {

    /**
     * Obtiene todos los registros de auditoría de un usuario específico,
     * ordenados por fecha descendente (más recientes primero).
     * 
     * @param idUsuario UUID del usuario
     * @return Lista de registros de auditoría ordenados cronológicamente
     */
    List<AuditoriaCambio> findByIdUsuarioOrderByFechaCambioDesc(String idUsuario);

    /**
     * Obtiene todos los registros de auditoría de un paciente específico,
     * ordenados por fecha descendente.
     * 
     * @param idPaciente UUID del paciente
     * @return Lista de registros de auditoría del paciente
     */
    List<AuditoriaCambio> findByIdPacienteOrderByFechaCambioDesc(String idPaciente);

    /**
     * Obtiene los registros de auditoría de un paciente dentro de un rango de fechas.
     * 
     * @param idPaciente UUID del paciente
     * @param desde Fecha de inicio (inclusive)
     * @param hasta Fecha de fin (inclusive)
     * @return Lista de registros filtrados por fecha
     */
    @Query("{ 'id_paciente': ?0, 'fecha_cambio': { $gte: ?1, $lte: ?2 } }")
    List<AuditoriaCambio> findByIdPacienteAndFechaCambioBetween(
        String idPaciente, 
        LocalDateTime desde, 
        LocalDateTime hasta
    );

    /**
     * Obtiene los registros de auditoría de un recurso específico.
     * 
     * @param idRecurso UUID del recurso (dato clínico, historial, etc.)
     * @return Lista de cambios en ese recurso ordenados cronológicamente
     */
    List<AuditoriaCambio> findByIdRecursoOrderByFechaCambioDesc(String idRecurso);

    /**
     * Obtiene los registros de auditoría de un tipo de cambio específico para un paciente.
     * 
     * @param idPaciente UUID del paciente
     * @param tipoCambio Tipo de dato clínico (ej: "GLUCOSA", "PRESION_ARTERIAL")
     * @return Lista de cambios del tipo especificado
     */
    List<AuditoriaCambio> findByIdPacienteAndTipoCambioOrderByFechaCambioDesc(
        String idPaciente, 
        String tipoCambio
    );

    /**
     * Obtiene los registros de auditoría realizados por un médico específico,
     * ordenados por fecha descendente.
     * 
     * @param idMedico UUID del médico
     * @return Lista de cambios realizados por el médico
     */
    List<AuditoriaCambio> findByIdMedicoOrderByFechaCambioDesc(String idMedico);

    /**
     * Obtiene los registros de auditoría de una tabla específica para un paciente.
     * 
     * @param idPaciente UUID del paciente
     * @param tabla Nombre de la tabla (ej: "dato_clinico", "historial_clinico")
     * @return Lista de cambios en esa tabla
     */
    List<AuditoriaCambio> findByIdPacienteAndTablaOrderByFechaCambioDesc(
        String idPaciente, 
        String tabla
    );

    /**
     * Obtiene los registros de auditoría de un tipo de operación específico para un paciente.
     * 
     * @param idPaciente UUID del paciente
     * @param tipoOperacion Tipo de operación (CREATE, UPDATE, DELETE)
     * @return Lista de registros del tipo de operación especificado
     */
    List<AuditoriaCambio> findByIdPacienteAndTipoOperacionOrderByFechaCambioDesc(
        String idPaciente, 
        AuditoriaCambio.TipoOperacion tipoOperacion
    );

    /**
     * Cuenta el número de cambios realizados en un paciente dentro de un rango de fechas.
     * 
     * @param idPaciente UUID del paciente
     * @param desde Fecha de inicio
     * @param hasta Fecha de fin
     * @return Número de cambios en el período
     */
    @Query("{ 'id_paciente': ?0, 'fecha_cambio': { $gte: ?1, $lte: ?2 } }")
    long countByIdPacienteAndFechaCambioBetween(
        String idPaciente, 
        LocalDateTime desde, 
        LocalDateTime hasta
    );
}
