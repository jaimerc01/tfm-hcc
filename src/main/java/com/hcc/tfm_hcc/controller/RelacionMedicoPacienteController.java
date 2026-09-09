package com.hcc.tfm_hcc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.hcc.tfm_hcc.dto.MedicoResumenDTO;

/**
 * Controlador REST para la gestión de la relación asistencial médico-paciente.
 *
 * <p>El paciente puede consultar los médicos que tienen acceso a su historial y
 * finalizar la relación con cualquiera de ellos; el médico puede finalizar la
 * relación con un paciente asignado. La autorización de las operaciones exclusivas
 * de médico y la traducción de los errores de dominio a códigos HTTP se resuelven
 * en la capa de fachada mediante {@code @PreAuthorize} y excepciones anotadas con
 * {@code @ResponseStatus}.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface RelacionMedicoPacienteController {

    /**
     * Lista los médicos con una relación asistencial activa sobre el usuario autenticado.
     *
     * @return ResponseEntity con la lista de MedicoResumenDTO de los médicos asignados
     */
    ResponseEntity<List<MedicoResumenDTO>> listarMisMedicos();

    /**
     * Finaliza la relación asistencial entre el usuario autenticado (paciente) y el
     * médico indicado.
     *
     * @param nifMedico NIF del médico con el que se finaliza la relación
     * @return ResponseEntity vacío confirmando la revocación
     */
    ResponseEntity<Void> desasignarMiMedico(@PathVariable("nif") String nifMedico);

    /**
     * Finaliza la relación asistencial entre el usuario autenticado (médico) y el
     * paciente indicado.
     *
     * @param nifPaciente NIF del paciente con el que se finaliza la relación
     * @return ResponseEntity vacío confirmando la revocación
     */
    ResponseEntity<Void> desasignarMiPaciente(@PathVariable("nif") String nifPaciente);
}
