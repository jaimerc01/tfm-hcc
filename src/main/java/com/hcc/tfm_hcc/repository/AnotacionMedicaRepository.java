package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcc.tfm_hcc.model.AnotacionMedica;

/**
 * Repositorio para la gestión de anotaciones médicas en el sistema HCC.
 * Una anotación médica es una observación de texto libre que un médico registra
 * sobre un paciente con el que mantiene una relación de asignación activa.
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface AnotacionMedicaRepository extends JpaRepository<AnotacionMedica, UUID> {

    /**
     * Lista todas las anotaciones recibidas por un paciente, de la más reciente
     * a la más antigua.
     *
     * @param pacienteId ID único del paciente
     * @return lista de anotaciones ordenadas por fecha de creación descendente
     */
    List<AnotacionMedica> findByPacienteIdOrderByFechaCreacionDesc(UUID pacienteId);

    /**
     * Lista las anotaciones que un médico concreto ha escrito sobre un paciente,
     * de la más reciente a la más antigua.
     *
     * @param pacienteId ID único del paciente
     * @param medicoId ID único del médico autor de las anotaciones
     * @return lista de anotaciones ordenadas por fecha de creación descendente
     */
    List<AnotacionMedica> findByPacienteIdAndMedicoIdOrderByFechaCreacionDesc(UUID pacienteId, UUID medicoId);
}
