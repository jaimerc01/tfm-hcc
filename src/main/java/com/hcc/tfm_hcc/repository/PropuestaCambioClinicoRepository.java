package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcc.tfm_hcc.model.PropuestaCambioClinico;

/**
 * Repositorio para las propuestas de cambio clínico que los médicos envían sobre el historial de
 * un paciente y que este debe confirmar o rechazar.
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface PropuestaCambioClinicoRepository extends JpaRepository<PropuestaCambioClinico, UUID> {

    /**
     * Todas las propuestas dirigidas a un paciente (pendientes y ya resueltas), de la más reciente
     * a la más antigua. Alimenta la pestaña «Cambios propuestos» del historial del paciente.
     *
     * @param pacienteId ID del paciente destinatario
     * @return lista de propuestas ordenada por fecha de creación descendente
     */
    List<PropuestaCambioClinico> findByPacienteIdOrderByFechaCreacionDesc(UUID pacienteId);

    /**
     * Todas las propuestas enviadas por un médico, de la más reciente a la más antigua.
     *
     * @param medicoId ID del médico proponente
     * @return lista de propuestas ordenada por fecha de creación descendente
     */
    List<PropuestaCambioClinico> findByMedicoIdOrderByFechaCreacionDesc(UUID medicoId);

    /**
     * Propuestas enviadas por un médico a un paciente concreto, de la más reciente a la más antigua.
     *
     * @param medicoId ID del médico proponente
     * @param pacienteId ID del paciente destinatario
     * @return lista de propuestas ordenada por fecha de creación descendente
     */
    List<PropuestaCambioClinico> findByMedicoIdAndPacienteIdOrderByFechaCreacionDesc(UUID medicoId, UUID pacienteId);

    /**
     * Número de propuestas dirigidas a un paciente en un estado concreto.
     *
     * @param pacienteId ID del paciente destinatario
     * @param estado estado de la propuesta
     * @return recuento de propuestas
     */
    long countByPacienteIdAndEstado(UUID pacienteId, String estado);

    /**
     * Propuestas de un médico a un paciente concreto en un estado dado. Se usa para anular las
     * propuestas pendientes cuando se revoca la relación asistencial entre ambos.
     *
     * @param medicoId ID del médico proponente
     * @param pacienteId ID del paciente destinatario
     * @param estado estado de la propuesta
     * @return lista de propuestas coincidentes
     */
    List<PropuestaCambioClinico> findByMedicoIdAndPacienteIdAndEstado(UUID medicoId, UUID pacienteId, String estado);
}
