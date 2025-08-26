package com.hcc.tfm_hcc.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Collections;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.hcc.tfm_hcc.controller.MedicoController;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.facade.MedicoFacade;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

@RestController
@RequestMapping("/medico")
public class MedicoControllerImpl implements MedicoController {
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    @GetMapping("/solicitud/asignacion")
    public ResponseEntity<?> crearSolicitudAsignacion(@RequestParam String nifPaciente) {
        try {
            SolicitudAsignacion solicitud = medicoFacade.crearSolicitudAsignacion(nifPaciente);
            if (solicitud == null) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(solicitud);
        } catch (com.hcc.tfm_hcc.exception.SolicitudExistenteException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("message", ex.getMessage()));
        } catch (Exception ex) {
            // Generic fallback
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Error creating solicitud"));
        }
    }

    @Autowired
    private MedicoFacade medicoFacade;

    // ping endpoint removed

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    @GetMapping("/paciente/buscar")
    public ResponseEntity<PacienteDTO> buscarPaciente(@RequestParam String dni, @RequestParam String fechaNacimiento) {
        PacienteDTO paciente = medicoFacade.buscarPacientePorDniYFechaNacimiento(dni, fechaNacimiento);
        if (paciente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(paciente);
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    @GetMapping("/solicitud/pendientes")
    public ResponseEntity<List<SolicitudAsignacion>> listarSolicitudesPendientes() {
        List<SolicitudAsignacion> lista = medicoFacade.listarSolicitudesPendientes();
        return ResponseEntity.ok(lista);
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    @GetMapping("/solicitud/enviadas")
    public ResponseEntity<List<SolicitudAsignacion>> listarSolicitudesEnviadas() {
        List<SolicitudAsignacion> lista = medicoFacade.listarSolicitudesEnviadas();
        return ResponseEntity.ok(lista);
    }
}
