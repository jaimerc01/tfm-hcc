package com.hcc.tfm_hcc.facade;

import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

public interface MedicoFacade {
    PacienteDTO buscarPacientePorDniYFechaNacimiento(String dni, String fechaNacimiento);
    SolicitudAsignacion crearSolicitudAsignacion(String nifPaciente);
    java.util.List<com.hcc.tfm_hcc.model.SolicitudAsignacion> listarSolicitudesPendientes();
}
