package com.hcc.tfm_hcc.controller;

import org.springframework.http.ResponseEntity;
import com.hcc.tfm_hcc.dto.PacienteDTO;
// import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import org.springframework.web.bind.annotation.RequestParam;

public interface MedicoController {
    ResponseEntity<String> ping();
    ResponseEntity<PacienteDTO> buscarPaciente(@RequestParam String dni, @RequestParam String fechaNacimiento);
    ResponseEntity<?> crearSolicitudAsignacion(@RequestParam String nifPaciente);
    ResponseEntity<java.util.List<com.hcc.tfm_hcc.model.SolicitudAsignacion>> listarSolicitudesPendientes();
}
