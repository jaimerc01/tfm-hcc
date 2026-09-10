package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoEntradaDTO;
import com.hcc.tfm_hcc.dto.PropuestaCambioClinicoRequestDTO;
import com.hcc.tfm_hcc.exception.PropuestaCambioClinicoException;
import com.hcc.tfm_hcc.exception.PropuestaCambioNoEncontradaException;
import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico.Dominio;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico.Operacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.PropuestaCambioClinicoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AuditoriaCambioService;
import com.hcc.tfm_hcc.service.HistorialClinicoService;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.PropuestaCambioClinicoService;
import com.hcc.tfm_hcc.service.RelacionMedicoPacienteService;
import com.hcc.tfm_hcc.util.LogMaskUtil;
import com.hcc.tfm_hcc.util.NombreUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de propuestas de cambio clínico (médico propone → paciente confirma).
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropuestaCambioClinicoServiceImpl implements PropuestaCambioClinicoService {

    private static final String ZONE_ID_EUROPA_MADRID = "Europe/Madrid";
    private static final String TIPO_CAMBIO_PROPUESTA = "PROPUESTA_CAMBIO_CLINICO";
    private static final String TABLA_PROPUESTA = "propuesta_cambio_clinico";
    private static final String RAZON_PROPUESTA_CREADA = "Propuesta de cambio pendiente de confirmación del paciente";
    private static final String RAZON_PROPUESTA_ACEPTADA = "Propuesta de cambio aceptada por el paciente";
    private static final String RAZON_PROPUESTA_RECHAZADA = "Propuesta de cambio rechazada por el paciente";

    private static final String MENSAJE_PROPUESTA_RECIBIDA_PREFIJO = "El médico ";
    private static final String MENSAJE_PROPUESTA_RECIBIDA_SUFIJO =
            " propone un cambio en tu historial clínico y necesita tu confirmación";
    private static final String MENSAJE_RESOLUCION_PREFIJO = "El paciente ";
    private static final String MENSAJE_PROPUESTA_ACEPTADA_SUFIJO = " ha aceptado tu propuesta de cambio en el historial";
    private static final String MENSAJE_PROPUESTA_RECHAZADA_SUFIJO = " ha rechazado tu propuesta de cambio en el historial";

    private final PropuestaCambioClinicoRepository propuestaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HmacSearchIndexService hmacSearchIndexService;
    private final RelacionMedicoPacienteService relacionMedicoPacienteService;
    private final HistorialClinicoService historialClinicoService;
    private final AuditoriaCambioService auditoriaCambioService;
    private final NotificacionFacade notificacionFacade;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PropuestaCambioClinico crearPropuesta(String nifMedico, String nifPaciente,
            PropuestaCambioClinicoRequestDTO request) {
        if (request == null) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_DATOS_REQUERIDOS);
        }

        // Valida la relación asistencial activa + que el paciente no ha limitado el tratamiento.
        Usuario paciente = relacionMedicoPacienteService.verificarAccesoMedicoActivo(nifMedico, nifPaciente);
        Usuario medico = usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nifMedico))
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));

        Dominio dominio = parseDominio(request.getDominio());
        Operacion operacion = parseOperacion(request.getOperacion());
        String motivo = validarMotivo(request.getMotivo());

        validarAlergiaSinEdicion(dominio, operacion);

        UUID recursoId = resolverRecursoId(request.getIdRecursoObjetivo(), operacion);
        String payloadJson = construirPayload(dominio, operacion, request);
        String descripcionActual = operacion == Operacion.CREATE
                ? null
                : historialClinicoService.describirRecursoHistorial(paciente.getId(), dominio, recursoId);

        HistorialClinico historial = historialClinicoService.asegurarHistorial(paciente.getId());

        PropuestaCambioClinico propuesta = new PropuestaCambioClinico();
        propuesta.setMedico(medico);
        propuesta.setPaciente(paciente);
        propuesta.setHistorialClinico(historial);
        propuesta.setDominio(dominio);
        propuesta.setOperacion(operacion);
        propuesta.setIdRecursoObjetivo(recursoId != null ? recursoId.toString() : null);
        propuesta.setPayloadJson(payloadJson);
        propuesta.setDescripcionActual(descripcionActual);
        propuesta.setMotivo(motivo);
        propuesta.setEstado(PropuestaCambioClinico.ESTADO_PENDIENTE);
        propuesta.setFechaCreacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));

        PropuestaCambioClinico guardada = propuestaRepository.save(propuesta);

        notificarPaciente(paciente, medico);
        auditar(guardada, "", dominio.name() + "/" + operacion.name(),
                AuditoriaCambio.TipoOperacion.CREATE, RAZON_PROPUESTA_CREADA);

        log.info("Propuesta de cambio {} creada por el médico {} para el paciente {} ({} / {})",
                guardada.getId(), LogMaskUtil.enmascarar(nifMedico), LogMaskUtil.enmascarar(nifPaciente),
                dominio, operacion);

        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropuestaCambioClinico> listarPropuestasEnviadasParaPaciente(String nifMedico, String nifPaciente) {
        Usuario medico = buscarUsuario(nifMedico);
        Usuario paciente = buscarUsuario(nifPaciente);
        return propuestaRepository.findByMedicoIdAndPacienteIdOrderByFechaCreacionDesc(medico.getId(), paciente.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropuestaCambioClinico> listarPropuestasParaPaciente(String nifPaciente) {
        Usuario paciente = buscarUsuario(nifPaciente);
        return propuestaRepository.findByPacienteIdOrderByFechaCreacionDesc(paciente.getId());
    }

    @Override
    @Transactional
    public PropuestaCambioClinico resolver(String nifPacienteAutenticado, UUID idPropuesta, boolean aceptar) {
        if (idPropuesta == null) {
            throw new PropuestaCambioNoEncontradaException(ErrorMessages.ERROR_PROPUESTA_NO_ENCONTRADA);
        }
        PropuestaCambioClinico propuesta = propuestaRepository.findById(idPropuesta)
                .orElseThrow(() -> new PropuestaCambioNoEncontradaException(ErrorMessages.ERROR_PROPUESTA_NO_ENCONTRADA));

        String hashPaciente = hmacSearchIndexService.indexar(nifPacienteAutenticado);
        if (propuesta.getPaciente() == null || !hashPaciente.equals(propuesta.getPaciente().getNifHash())) {
            throw new PropuestaCambioNoEncontradaException(ErrorMessages.ERROR_PROPUESTA_NO_ENCONTRADA);
        }
        if (!PropuestaCambioClinico.ESTADO_PENDIENTE.equals(propuesta.getEstado())) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_YA_RESUELTA);
        }

        if (aceptar) {
            aplicarCambio(propuesta);
            resolverPropuesta(propuesta, PropuestaCambioClinico.ESTADO_ACEPTADA);
            notificarMedico(propuesta, MENSAJE_PROPUESTA_ACEPTADA_SUFIJO);
            auditar(propuesta, PropuestaCambioClinico.ESTADO_PENDIENTE, PropuestaCambioClinico.ESTADO_ACEPTADA,
                    AuditoriaCambio.TipoOperacion.UPDATE, RAZON_PROPUESTA_ACEPTADA);
        } else {
            resolverPropuesta(propuesta, PropuestaCambioClinico.ESTADO_RECHAZADA);
            notificarMedico(propuesta, MENSAJE_PROPUESTA_RECHAZADA_SUFIJO);
            auditar(propuesta, PropuestaCambioClinico.ESTADO_PENDIENTE, PropuestaCambioClinico.ESTADO_RECHAZADA,
                    AuditoriaCambio.TipoOperacion.UPDATE, RAZON_PROPUESTA_RECHAZADA);
        }

        log.info("Propuesta de cambio {} {} por el paciente {}", propuesta.getId(),
                aceptar ? "aceptada" : "rechazada", LogMaskUtil.enmascarar(nifPacienteAutenticado));

        return propuesta;
    }

    @Override
    @Transactional
    public void anularPendientes(UUID medicoId, UUID pacienteId) {
        if (medicoId == null || pacienteId == null) {
            return;
        }
        List<PropuestaCambioClinico> pendientes = propuestaRepository.findByMedicoIdAndPacienteIdAndEstado(
                medicoId, pacienteId, PropuestaCambioClinico.ESTADO_PENDIENTE);
        if (pendientes.isEmpty()) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID));
        pendientes.forEach(propuesta -> {
            propuesta.setEstado(PropuestaCambioClinico.ESTADO_ANULADA);
            propuesta.setFechaResolucion(ahora);
        });
        propuestaRepository.saveAll(pendientes);
        log.info("Anuladas {} propuestas de cambio pendientes al finalizar la relación médico {} / paciente {}",
                pendientes.size(), medicoId, pacienteId);
    }

    // ===============================
    // APLICACIÓN DEL CAMBIO
    // ===============================

    private void aplicarCambio(PropuestaCambioClinico propuesta) {
        UUID pacienteId = propuesta.getPaciente().getId();
        UUID medicoId = propuesta.getMedico().getId();
        Operacion operacion = propuesta.getOperacion();
        UUID recursoId = propuesta.getIdRecursoObjetivo() != null
                ? UUID.fromString(propuesta.getIdRecursoObjetivo())
                : null;
        String motivo = propuesta.getMotivo();
        String payload = propuesta.getPayloadJson();

        switch (propuesta.getDominio()) {
            case ANTECEDENTE -> historialClinicoService.aplicarCambioAntecedente(pacienteId, medicoId, operacion,
                    recursoId, leerPayload(payload, AntecedenteClinicoDTO.class), motivo);
            case ALERGIA -> historialClinicoService.aplicarCambioAlergia(pacienteId, medicoId, operacion,
                    recursoId, leerPayload(payload, AlergiaDTO.class), motivo);
            case ANALISIS_SANGRE, SIGNOS_VITALES, ANALISIS_ORINA -> historialClinicoService.aplicarCambioMedicion(
                    pacienteId, medicoId, operacion, recursoId, leerPayload(payload, DatoClinicoEntradaDTO.class), motivo);
        }
    }

    private void resolverPropuesta(PropuestaCambioClinico propuesta, String nuevoEstado) {
        propuesta.setEstado(nuevoEstado);
        propuesta.setFechaResolucion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));
        propuestaRepository.save(propuesta);
    }

    // ===============================
    // VALIDACIÓN Y CONSTRUCCIÓN
    // ===============================

    private Dominio parseDominio(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_DOMINIO_INVALIDO);
        }
        try {
            return Dominio.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_DOMINIO_INVALIDO, e);
        }
    }

    private Operacion parseOperacion(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_OPERACION_INVALIDA);
        }
        try {
            return Operacion.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_OPERACION_INVALIDA, e);
        }
    }

    private String validarMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_MOTIVO_REQUERIDO);
        }
        return motivo.trim();
    }

    private void validarAlergiaSinEdicion(Dominio dominio, Operacion operacion) {
        if (dominio == Dominio.ALERGIA && operacion == Operacion.UPDATE) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_ALERGIA_SIN_EDICION);
        }
    }

    private UUID resolverRecursoId(String idRecursoObjetivo, Operacion operacion) {
        if (operacion == Operacion.CREATE) {
            return null;
        }
        if (idRecursoObjetivo == null || idRecursoObjetivo.isBlank()) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_RECURSO_REQUERIDO);
        }
        try {
            return UUID.fromString(idRecursoObjetivo.trim());
        } catch (IllegalArgumentException e) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_RECURSO_REQUERIDO, e);
        }
    }

    /**
     * Serializa a JSON el sub-DTO que corresponde al dominio de la propuesta. Devuelve
     * {@code null} para los borrados (no llevan datos propuestos).
     */
    private String construirPayload(Dominio dominio, Operacion operacion, PropuestaCambioClinicoRequestDTO request) {
        if (operacion == Operacion.DELETE) {
            return null;
        }
        Object datos = switch (dominio) {
            case ANTECEDENTE -> exigirDescripcion(request.getAntecedente(),
                    request.getAntecedente() != null ? request.getAntecedente().getDescripcion() : null);
            case ALERGIA -> exigirDescripcion(request.getAlergia(),
                    request.getAlergia() != null ? request.getAlergia().getDescripcion() : null);
            case ANALISIS_SANGRE, SIGNOS_VITALES, ANALISIS_ORINA -> exigirValor(request.getMedicion());
        };
        try {
            return objectMapper.writeValueAsString(datos);
        } catch (JsonProcessingException e) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_DATOS_REQUERIDOS, e);
        }
    }

    private Object exigirDescripcion(Object dto, String descripcion) {
        if (dto == null || descripcion == null || descripcion.trim().isEmpty()) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_DATOS_REQUERIDOS);
        }
        return dto;
    }

    private DatoClinicoEntradaDTO exigirValor(DatoClinicoEntradaDTO medicion) {
        if (medicion == null || medicion.getValue() == null || medicion.getValue().trim().isEmpty()) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_DATOS_REQUERIDOS);
        }
        return medicion;
    }

    private <T> T leerPayload(String payload, Class<T> tipo) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(payload, tipo);
        } catch (JsonProcessingException e) {
            throw new PropuestaCambioClinicoException(ErrorMessages.ERROR_PROPUESTA_DATOS_REQUERIDOS, e);
        }
    }

    private Usuario buscarUsuario(String nif) {
        if (nif == null || nif.isBlank()) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO);
        }
        return usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nif))
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }

    // ===============================
    // NOTIFICACIÓN Y AUDITORÍA
    // ===============================

    private void notificarPaciente(Usuario paciente, Usuario medico) {
        String nombreMedico = nombreONif(medico);
        try {
            notificacionFacade.crearNotificacionParaUsuario(paciente.getNif(),
                    MENSAJE_PROPUESTA_RECIBIDA_PREFIJO + nombreMedico + MENSAJE_PROPUESTA_RECIBIDA_SUFIJO);
        } catch (Exception e) {
            log.warn("No se pudo notificar la propuesta al paciente {}: {}",
                    LogMaskUtil.enmascarar(paciente.getNif()), e.getMessage());
        }
    }

    private void notificarMedico(PropuestaCambioClinico propuesta, String sufijo) {
        Usuario medico = propuesta.getMedico();
        Usuario paciente = propuesta.getPaciente();
        if (medico == null || paciente == null) {
            return;
        }
        try {
            notificacionFacade.crearNotificacionParaUsuario(medico.getNif(),
                    MENSAJE_RESOLUCION_PREFIJO + nombreONif(paciente) + sufijo);
        } catch (Exception e) {
            log.warn("No se pudo notificar la resolución de la propuesta al médico {}: {}",
                    LogMaskUtil.enmascarar(medico.getNif()), e.getMessage());
        }
    }

    private String nombreONif(Usuario usuario) {
        if (usuario == null) {
            return "";
        }
        String nombre = NombreUtil.nombreCompleto(usuario);
        return nombre.isBlank() ? usuario.getNif() : nombre;
    }

    private void auditar(PropuestaCambioClinico propuesta, String valorAnterior, String valorNuevo,
            AuditoriaCambio.TipoOperacion operacion, String razon) {
        auditoriaCambioService.registrarCambio(
                propuesta.getMedico().getId().toString(),
                propuesta.getPaciente().getId().toString(),
                propuesta.getMedico().getId().toString(),
                TIPO_CAMBIO_PROPUESTA,
                TABLA_PROPUESTA,
                propuesta.getId().toString(),
                valorAnterior,
                valorNuevo,
                operacion,
                razon
        );
    }
}
