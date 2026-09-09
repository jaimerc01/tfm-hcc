package com.hcc.tfm_hcc.facade;

import java.util.List;

import com.hcc.tfm_hcc.dto.MedicoResumenDTO;

/**
 * Facade para la gestión de la relación asistencial médico-paciente por parte de
 * cualquiera de los dos implicados: el paciente puede consultar y finalizar la
 * relación con sus médicos, y el médico puede finalizar la relación con sus pacientes.
 * En ambos casos se avisa a la otra parte.
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface RelacionMedicoPacienteFacade {

    /**
     * Lista los médicos con una relación asistencial activa sobre el usuario autenticado.
     *
     * @return lista de MedicoResumenDTO de los médicos asignados
     */
    List<MedicoResumenDTO> listarMisMedicos();

    /**
     * Finaliza la relación asistencial entre el usuario autenticado (paciente) y el
     * médico indicado. El médico recibe una notificación.
     *
     * @param nifMedico NIF del médico con el que se finaliza la relación
     * @throws com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException si el médico no existe
     * @throws com.hcc.tfm_hcc.exception.RelacionMedicoPacienteNoEncontradaException si no hay relación activa
     */
    void desasignarMiMedico(String nifMedico);

    /**
     * Finaliza la relación asistencial entre el usuario autenticado (médico) y el
     * paciente indicado. El paciente recibe una notificación.
     *
     * @param nifPaciente NIF del paciente con el que se finaliza la relación
     * @throws com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException si el paciente no existe
     * @throws com.hcc.tfm_hcc.exception.RelacionMedicoPacienteNoEncontradaException si no hay relación activa
     */
    void desasignarMiPaciente(String nifPaciente);
}
