package com.hcc.tfm_hcc.service;

import java.util.List;

import com.hcc.tfm_hcc.model.MedicoPaciente;
import com.hcc.tfm_hcc.model.Usuario;

/**
 * Servicio para la gestión de la relación asistencial médico-paciente en el sistema HCC.
 * La relación se crea al aceptar el paciente una solicitud de asignación y puede
 * finalizarla en cualquier momento cualquiera de las dos partes.
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface RelacionMedicoPacienteService {

    /** Identifica quién inicia la revocación de la relación, para la trazabilidad. */
    enum IniciadorRevocacion { MEDICO, PACIENTE }

    /**
     * Lista los médicos con una relación asistencial activa sobre el paciente indicado.
     *
     * @param nifPaciente NIF del paciente
     * @return lista de usuarios médicos con acceso activo al historial del paciente
     * @throws com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException si el paciente no existe
     */
    List<Usuario> listarMedicosActivos(String nifPaciente);

    /**
     * Comprueba que un médico tiene acceso asistencial activo sobre un paciente: ambos
     * existen, el paciente no ha limitado el tratamiento de sus datos (art. 18 RGPD) y
     * hay una relación médico-paciente en estado "ACTIVA".
     *
     * @param nifMedico NIF del médico
     * @param nifPaciente NIF del paciente
     * @return la entidad {@link Usuario} del paciente
     * @throws com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException si el médico o el paciente no existen
     * @throws com.hcc.tfm_hcc.exception.UsuarioSinPermisoException si no hay relación activa o el paciente
     *         ha limitado el tratamiento de sus datos
     */
    Usuario verificarAccesoMedicoActivo(String nifMedico, String nifPaciente);

    /**
     * Revoca la relación asistencial activa entre un médico y un paciente. Tras la
     * revocación el médico deja de tener acceso al historial del paciente.
     *
     * @param nifMedico NIF del médico de la relación
     * @param nifPaciente NIF del paciente de la relación
     * @param iniciadaPor rol de quien inicia la revocación (para la auditoría)
     * @return la relación revocada, con médico y paciente cargados
     * @throws com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException si el médico o el paciente no existen
     * @throws com.hcc.tfm_hcc.exception.RelacionMedicoPacienteNoEncontradaException si no hay una relación activa entre ambos
     */
    MedicoPaciente revocarRelacion(String nifMedico, String nifPaciente, IniciadorRevocacion iniciadaPor);
}
