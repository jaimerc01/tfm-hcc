package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.exception.SolicitudExistenteException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.SolicitudAsignacionRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.service.SolicitudAsignacionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudAsignacionServiceImpl implements SolicitudAsignacionService {

    private final SolicitudAsignacionRepository solicitudAsignacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionFacade notificacionFacade;

    @Override
    @Transactional
    public SolicitudAsignacion crearSolicitud(String nifMedico, String nifPaciente) {
        Usuario medico = usuarioRepository.findByNif(nifMedico).orElse(null);
        Usuario paciente = usuarioRepository.findByNif(nifPaciente).orElse(null);
        if (medico == null || paciente == null) {
            throw new UsuarioNoEncontradoException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO);
        }

        boolean exists = solicitudAsignacionRepository.existsByMedicoNifAndPacienteNifAndEstado(nifMedico, nifPaciente, "PENDIENTE");
        if (exists) {
            throw new SolicitudExistenteException("Ya existe una solicitud pendiente para este médico y paciente");
        }

        SolicitudAsignacion solicitud = new SolicitudAsignacion();
        solicitud.setMedico(medico);
        solicitud.setPaciente(paciente);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaCreacion(LocalDateTime.now());

        var saved = solicitudAsignacionRepository.save(solicitud);

        // Crear notificación (no lanzar si falla)
        try {
            String mensaje = "Has recibido una solicitud de asignación del médico " + (medico.getNombre()!=null ? medico.getNombre() : medico.getNif());
            notificacionFacade.crearNotificacionParaUsuario(paciente.getNif(), mensaje);
        } catch (Exception e) {
            log.warn("Error al crear notificación tras crear solicitud: {}", e.getMessage());
        }

        return saved;
    }

    @Override
    public List<SolicitudAsignacion> listarSolicitudesPendientesPorMedico(String nifMedico) {
        return solicitudAsignacionRepository.findByMedicoNifAndEstado(nifMedico, "PENDIENTE");
    }

    @Override
    public List<SolicitudAsignacion> listarSolicitudesEnviadasPorMedico(String nifMedico) {
        return solicitudAsignacionRepository.findByMedicoNifOrderByFechaCreacionDesc(nifMedico);
    }
}
