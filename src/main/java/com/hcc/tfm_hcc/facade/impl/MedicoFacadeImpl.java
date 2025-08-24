package com.hcc.tfm_hcc.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.facade.MedicoFacade;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.service.MedicoService;

@Service
public class MedicoFacadeImpl implements MedicoFacade {
    @Override
    public SolicitudAsignacion crearSolicitudAsignacion(String nifPaciente) {
        return medicoService.crearSolicitudAsignacion(nifPaciente);
    }
    @Override
    public java.util.List<SolicitudAsignacion> listarSolicitudesPendientes() {
        return medicoService.listarSolicitudesPendientes();
    }
    @Autowired
    private MedicoService medicoService;

    @Override
    public PacienteDTO buscarPacientePorDniYFechaNacimiento(String dni, String fechaNacimiento) {
        return medicoService.buscarPacientePorDniYFechaNacimiento(dni, fechaNacimiento);
    }
}
