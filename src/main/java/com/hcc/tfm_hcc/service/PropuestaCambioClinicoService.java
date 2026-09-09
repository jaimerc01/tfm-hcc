package com.hcc.tfm_hcc.service;

import java.util.List;
import java.util.UUID;

import com.hcc.tfm_hcc.dto.PropuestaCambioClinicoRequestDTO;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico;

/**
 * Servicio para las propuestas de cambio en el historial clínico de un paciente que un médico
 * asignado envía y el paciente debe confirmar antes de que se apliquen.
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface PropuestaCambioClinicoService {

    /**
     * Registra una propuesta de cambio de un médico sobre el historial de un paciente asignado.
     * No aplica el cambio: lo deja {@code PENDIENTE} de que el paciente lo confirme y le envía
     * una notificación.
     *
     * @param nifMedico NIF del médico autenticado
     * @param nifPaciente NIF del paciente destinatario
     * @param request dominio, operación, recurso objetivo, motivo y datos propuestos
     * @return la propuesta creada, con estado {@code PENDIENTE}
     * @throws com.hcc.tfm_hcc.exception.UsuarioSinPermisoException si no hay relación asistencial
     *         activa con el paciente o el paciente ha limitado el tratamiento de sus datos
     * @throws com.hcc.tfm_hcc.exception.PropuestaCambioClinicoException si la petición es inválida
     *         (motivo ausente, operación no soportada, faltan datos o el recurso objetivo)
     */
    PropuestaCambioClinico crearPropuesta(String nifMedico, String nifPaciente, PropuestaCambioClinicoRequestDTO request);

    /**
     * Lista las propuestas que un médico ha enviado a un paciente concreto, de la más reciente a
     * la más antigua.
     *
     * @param nifMedico NIF del médico autenticado
     * @param nifPaciente NIF del paciente
     * @return lista de propuestas enviadas por el médico a ese paciente
     */
    List<PropuestaCambioClinico> listarPropuestasEnviadasParaPaciente(String nifMedico, String nifPaciente);

    /**
     * Lista todas las propuestas de cambio dirigidas a un paciente, de la más reciente a la más
     * antigua: tanto las que siguen {@code PENDIENTE} de confirmar como las ya resueltas
     * ({@code ACEPTADA}, {@code RECHAZADA} o {@code ANULADA}), para que el paciente pueda revisar
     * también su histórico.
     *
     * @param nifPaciente NIF del paciente autenticado
     * @return lista de propuestas ordenada por fecha de creación descendente
     */
    List<PropuestaCambioClinico> listarPropuestasParaPaciente(String nifPaciente);

    /**
     * Resuelve una propuesta pendiente. Si el paciente la acepta, el cambio se aplica sobre su
     * historial y queda auditado con el médico como autor y su motivo; si la rechaza, se descarta.
     * En ambos casos el médico recibe una notificación.
     *
     * @param nifPacienteAutenticado NIF del paciente que resuelve la propuesta
     * @param idPropuesta ID de la propuesta
     * @param aceptar {@code true} para aceptar (y aplicar), {@code false} para rechazar
     * @return la propuesta resuelta
     * @throws com.hcc.tfm_hcc.exception.PropuestaCambioNoEncontradaException si la propuesta no
     *         existe o no está dirigida a ese paciente
     * @throws com.hcc.tfm_hcc.exception.PropuestaCambioClinicoException si la propuesta ya estaba resuelta
     */
    PropuestaCambioClinico resolver(String nifPacienteAutenticado, UUID idPropuesta, boolean aceptar);

    /**
     * Anula (sin aplicar) todas las propuestas pendientes entre un médico y un paciente. Se
     * invoca al revocar la relación asistencial entre ambos.
     *
     * @param medicoId ID del médico
     * @param pacienteId ID del paciente
     */
    void anularPendientes(UUID medicoId, UUID pacienteId);
}
