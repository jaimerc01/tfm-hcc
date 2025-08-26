package com.hcc.tfm_hcc.facade;

import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import java.util.List;

public interface MedicoFacade {
    PacienteDTO buscarPacientePorDniYFechaNacimiento(String dni, String fechaNacimiento);
    SolicitudAsignacion crearSolicitudAsignacion(String nifPaciente);
    List<SolicitudAsignacion> listarSolicitudesPendientes();
    List<SolicitudAsignacion> listarSolicitudesEnviadas();
}
