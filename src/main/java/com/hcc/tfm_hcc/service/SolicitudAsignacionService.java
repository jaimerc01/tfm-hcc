package com.hcc.tfm_hcc.service;

import java.util.List;

import com.hcc.tfm_hcc.model.SolicitudAsignacion;

/**
 * Servicio para la gestión de solicitudes de asignación médico-paciente.
 */
public interface SolicitudAsignacionService {

    /**
     * Crea una solicitud de asignación entre un médico y un paciente.
     *
     * @param nifMedico  NIF del médico
     * @param nifPaciente NIF del paciente
     * @return SolicitudAsignacion creada
     */
    SolicitudAsignacion crearSolicitud(String nifMedico, String nifPaciente);

    /**
     * Lista solicitudes pendientes para un médico dado.
     *
     * @param nifMedico NIF del médico
     * @return lista de solicitudes pendientes
     */
    List<SolicitudAsignacion> listarSolicitudesPendientesPorMedico(String nifMedico);

    /**
     * Lista solicitudes enviadas por un médico (todas, ordenadas por fecha).
     *
     * @param nifMedico NIF del médico
     * @return lista de solicitudes enviadas
     */
    List<SolicitudAsignacion> listarSolicitudesEnviadasPorMedico(String nifMedico);
}
