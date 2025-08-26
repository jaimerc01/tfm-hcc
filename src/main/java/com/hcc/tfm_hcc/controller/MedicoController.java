package com.hcc.tfm_hcc.controller;

import org.springframework.http.ResponseEntity;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

public interface MedicoController {
    ResponseEntity<PacienteDTO> buscarPaciente(@RequestParam String dni, @RequestParam String fechaNacimiento);
    ResponseEntity<?> crearSolicitudAsignacion(@RequestParam String nifPaciente);
    ResponseEntity<List<SolicitudAsignacion>> listarSolicitudesPendientes();
    ResponseEntity<List<SolicitudAsignacion>> listarSolicitudesEnviadas();
}
