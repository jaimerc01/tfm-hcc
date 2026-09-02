package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.SolicitudAsignacionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudAsignacionServiceImpl implements SolicitudAsignacionService {

    private static final String ZONE_ID_EUROPA_MADRID = "Europe/Madrid";
    private static final String MENSAJE_SOLICITUD_RECIBIDA_PREFIJO = "Has recibido una solicitud de asignación del médico ";

    private final SolicitudAsignacionRepository solicitudAsignacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionFacade notificacionFacade;
    private final HmacSearchIndexService hmacSearchIndexService;

    @Override
    @Transactional
    public SolicitudAsignacion crearSolicitud(String nifMedico, String nifPaciente) {
        Usuario medico = usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nifMedico)).orElse(null);
        Usuario paciente = usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nifPaciente)).orElse(null);
        if (medico == null || paciente == null) {
            throw new UsuarioNoEncontradoException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO);
        }

        boolean exists = solicitudAsignacionRepository.existsByMedicoNifHashAndPacienteNifHashAndEstado(
                medico.getNifHash(), paciente.getNifHash(), SolicitudAsignacion.ESTADO_PENDIENTE);
        if (exists) {
            throw new SolicitudExistenteException(ErrorMessages.ERROR_SOLICITUD_YA_EXISTE);
        }

        SolicitudAsignacion solicitud = new SolicitudAsignacion();
        solicitud.setMedico(medico);
        solicitud.setPaciente(paciente);
        solicitud.setEstado(SolicitudAsignacion.ESTADO_PENDIENTE);
        solicitud.setFechaCreacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));

        var saved = solicitudAsignacionRepository.save(solicitud);

        // Crear notificación (no lanzar si falla)
        try {
            String nombreMedico = medico.getNombre() != null ? medico.getNombre() : medico.getNif();
            notificacionFacade.crearNotificacionParaUsuario(
                    paciente.getNif(), MENSAJE_SOLICITUD_RECIBIDA_PREFIJO + nombreMedico);
        } catch (Exception e) {
            log.warn("Error al crear notificación tras crear solicitud: {}", e.getMessage());
        }

        return saved;
    }

    @Override
    public List<SolicitudAsignacion> listarSolicitudesPendientesPorMedico(String nifMedico) {
        return solicitudAsignacionRepository.findByMedicoNifHashAndEstado(hmacSearchIndexService.indexar(nifMedico), SolicitudAsignacion.ESTADO_PENDIENTE);
    }

    @Override
    public List<SolicitudAsignacion> listarSolicitudesEnviadasPorMedico(String nifMedico) {
        return solicitudAsignacionRepository.findByMedicoNifHashOrderByFechaCreacionDesc(hmacSearchIndexService.indexar(nifMedico));
    }
}
