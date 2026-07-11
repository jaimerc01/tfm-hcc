package com.hcc.tfm_hcc.facade.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.facade.SolicitudAsignacionFacade;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.service.SolicitudAsignacionService;
import com.hcc.tfm_hcc.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolicitudAsignacionFacadeImpl implements SolicitudAsignacionFacade {

    private final SolicitudAsignacionService solicitudAsignacionService;

    @Override
    public SolicitudAsignacion crearSolicitudAsignacion(String nifPaciente) {
        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) {
            log.warn("Usuario actual sin NIF al crear solicitud");
            return null;
        }

        try {
            return solicitudAsignacionService.crearSolicitud(nifMedico, nifPaciente);
        } catch (RuntimeException e) {
            log.error("Error al crear solicitud de asignación: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<SolicitudAsignacion> listarSolicitudesPendientes() {
        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) return java.util.Collections.emptyList();
        return solicitudAsignacionService.listarSolicitudesPendientesPorMedico(nifMedico);
    }

    @Override
    public List<SolicitudAsignacion> listarSolicitudesEnviadas() {
        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) return java.util.Collections.emptyList();
        return solicitudAsignacionService.listarSolicitudesEnviadasPorMedico(nifMedico);
    }
}
