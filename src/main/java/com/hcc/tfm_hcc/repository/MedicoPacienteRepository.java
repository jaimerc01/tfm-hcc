package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.MedicoPaciente;

/**
 * Repositorio para la gestión de relaciones médico-paciente en el sistema HCC.
 * Proporciona operaciones de persistencia y consulta para las asignaciones
 * entre médicos y pacientes del sistema.
 * 
 * <p>Este repositorio permite:</p>
 * <ul>
 *   <li>Verificar la existencia de relaciones médico-paciente</li>
 *   <li>Consultar relaciones activas por médico y estado</li>
 *   <li>Mantener la integridad de las asignaciones médicas</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface MedicoPacienteRepository extends JpaRepository<MedicoPaciente, UUID> {
    
    /**
     * Verifica si existe una relación activa entre un médico y un paciente específicos.
     * Útil para evitar duplicados y validar permisos de acceso.
     * 
     * @param medicoId ID único del médico
     * @param pacienteId ID único del paciente
     * @return true si existe la relación médico-paciente, false en caso contrario
     */
    boolean existsByMedicoIdAndPacienteId(UUID medicoId, UUID pacienteId);
    
    /**
     * Busca todas las relaciones médico-paciente de un médico específico,
     * excluyendo aquellas que tengan un estado determinado.
     * Útil para obtener pacientes activos excluyendo relaciones revocadas o inactivas.
     * 
     * @param medicoId ID único del médico del cual obtener las relaciones
     * @param estado Estado a excluir de los resultados (ej: "REVOCADA", "INACTIVA")
     * @return Lista de MedicoPaciente que no tienen el estado especificado
     */
    List<MedicoPaciente> findByMedicoIdAndEstadoNot(UUID medicoId, String estado);
}
