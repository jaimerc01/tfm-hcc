package com.hcc.tfm_hcc.service;

import java.util.List;
import java.util.UUID;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

public interface MedicoService {
    List<UsuarioDTO> listarMedicos();
    UsuarioDTO crearMedico(UsuarioDTO medicoDTO);
    UsuarioDTO actualizarMedico(UUID id, UsuarioDTO medicoDTO);
    void eliminarMedico(UUID id);

    PacienteDTO buscarPacientePorDniYFechaNacimiento(String dni, String fechaNacimiento);
    // Create assignment request for the current authenticated medico using only patient nif
    SolicitudAsignacion crearSolicitudAsignacion(String nifPaciente);
    java.util.List<com.hcc.tfm_hcc.model.SolicitudAsignacion> listarSolicitudesPendientes();
}
