package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.AuditoriaCambio.TipoOperacion;
import com.hcc.tfm_hcc.repository.AuditoriaCambioRepository;
import com.hcc.tfm_hcc.service.AuditoriaCambioService;
import com.hcc.tfm_hcc.service.AuditoriaCambioStats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de auditoría de cambios en datos clínicos.
 * 
 * Proporciona trazabilidad completa de quién cambió qué, cuándo y por qué,
 * respetando requisitos de seguridad y RGPD.
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditoriaCambioServiceImpl implements AuditoriaCambioService {

    private static final String EUROPE_MADRID = "Europe/Madrid";
    private final AuditoriaCambioRepository auditoriaCambioRepository;
    @Lazy
    private final AuditoriaCambioService auditoriaCambioService;

    @Override
    @Transactional
    public AuditoriaCambio registrarCambio(AuditoriaCambio auditoria) {
        if (auditoria == null) {
            log.warn("Intento de registrar auditoría nula");
            throw new IllegalArgumentException("El registro de auditoría no puede ser nulo");
        }

        // Asegurar que el ID y la fecha están establecidos
        if (auditoria.getId() == null) {
            auditoria.setId(java.util.UUID.randomUUID().toString());
        }
        if (auditoria.getFechaCambio() == null) {
            auditoria.setFechaCambio(LocalDateTime.now(ZoneId.of(EUROPE_MADRID)));
        }

        AuditoriaCambio guardado = auditoriaCambioRepository.save(auditoria);
        
        log.info(
            "Auditoría registrada: usuario={}, paciente={}, tipo={}, operacion={}",
            guardado.getIdUsuario(), guardado.getIdPaciente(), 
            guardado.getTipoCambio(), guardado.getTipoOperacion()
        );

        return guardado;
    }

    @Override
    public AuditoriaCambio registrarCambio(
            String idUsuario,
            String idPaciente,
            String idMedico,
            String tipoCambio,
            String tabla,
            String idRecurso,
            String valorAnterior,
            String valorNuevo,
            TipoOperacion tipoOperacion,
            String razonCambio) {

        AuditoriaCambio auditoria = AuditoriaCambio.crear();
        auditoria.setIdUsuario(idUsuario);
        auditoria.setIdPaciente(idPaciente);
        auditoria.setIdMedico(idMedico);
        auditoria.setTipoCambio(tipoCambio);
        auditoria.setTabla(tabla);
        auditoria.setIdRecurso(idRecurso);
        auditoria.setValorAnterior(valorAnterior);
        auditoria.setValorNuevo(valorNuevo);
        auditoria.setTipoOperacion(tipoOperacion);
        auditoria.setRazonCambio(razonCambio);

        return auditoriaCambioService.registrarCambio(auditoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaCambio> obtenerHistorialCambiosUsuario(String idUsuario) {
        if (idUsuario == null || idUsuario.isBlank()) {
            log.warn("Intento de obtener historial con ID de usuario nulo o vacío");
            return List.of();
        }

        List<AuditoriaCambio> cambios = auditoriaCambioRepository.findByIdUsuarioOrderByFechaCambioDesc(idUsuario);
        log.debug("Se recuperaron {} cambios para usuario {}", cambios.size(), idUsuario);
        return cambios;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaCambio> obtenerHistorialCambiosPaciente(String idPaciente) {
        if (idPaciente == null || idPaciente.isBlank()) {
            log.warn("Intento de obtener historial con ID de paciente nulo o vacío");
            return List.of();
        }

        List<AuditoriaCambio> cambios = auditoriaCambioRepository.findByIdPacienteOrderByFechaCambioDesc(idPaciente);
        log.debug("Se recuperaron {} cambios para paciente {}", cambios.size(), idPaciente);
        return cambios;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaCambio> obtenerHistorialCambiosPaciente(
            String idPaciente,
            LocalDateTime desde,
            LocalDateTime hasta) {

        if (idPaciente == null || idPaciente.isBlank()) {
            log.warn("Intento de obtener historial con ID de paciente nulo o vacío");
            return List.of();
        }

        if (desde == null || hasta == null) {
            log.warn("Intento de obtener historial con fechas nulas");
            return List.of();
        }

        List<AuditoriaCambio> cambios = auditoriaCambioRepository.findByIdPacienteAndFechaCambioBetween(
            idPaciente, desde, hasta
        );
        log.debug(
            "Se recuperaron {} cambios para paciente {} entre {} y {}",
            cambios.size(), idPaciente, desde, hasta
        );
        return cambios;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaCambio> obtenerHistorialCambiosRecurso(String idRecurso) {
        if (idRecurso == null || idRecurso.isBlank()) {
            log.warn("Intento de obtener historial con ID de recurso nulo o vacío");
            return List.of();
        }

        List<AuditoriaCambio> cambios = auditoriaCambioRepository.findByIdRecursoOrderByFechaCambioDesc(idRecurso);
        log.debug("Se recuperaron {} cambios para recurso {}", cambios.size(), idRecurso);
        return cambios;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaCambio> obtenerCambiosPorTipo(String idPaciente, String tipoCambio) {
        if (idPaciente == null || idPaciente.isBlank() || tipoCambio == null || tipoCambio.isBlank()) {
            log.warn("Intento de obtener cambios con parámetros nulos o vacíos");
            return List.of();
        }

        List<AuditoriaCambio> cambios = auditoriaCambioRepository
            .findByIdPacienteAndTipoCambioOrderByFechaCambioDesc(idPaciente, tipoCambio);
        log.debug("Se recuperaron {} cambios de tipo {} para paciente {}", 
            cambios.size(), tipoCambio, idPaciente);
        return cambios;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaCambio> obtenerCambiosPorMedico(String idMedico) {
        if (idMedico == null || idMedico.isBlank()) {
            log.warn("Intento de obtener cambios con ID de médico nulo o vacío");
            return List.of();
        }

        List<AuditoriaCambio> cambios = auditoriaCambioRepository.findByIdMedicoOrderByFechaCambioDesc(idMedico);
        log.debug("Se recuperaron {} cambios realizados por médico {}", cambios.size(), idMedico);
        return cambios;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaCambio> obtenerCambiosPorTipoOperacion(
            String idPaciente,
            TipoOperacion tipoOperacion) {

        if (idPaciente == null || idPaciente.isBlank() || tipoOperacion == null) {
            log.warn("Intento de obtener cambios con parámetros nulos o vacíos");
            return List.of();
        }

        List<AuditoriaCambio> cambios = auditoriaCambioRepository
            .findByIdPacienteAndTipoOperacionOrderByFechaCambioDesc(idPaciente, tipoOperacion);
        log.debug("Se recuperaron {} cambios de tipo {} para paciente {}", 
            cambios.size(), tipoOperacion, idPaciente);
        return cambios;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hayAuditoriaSospechosa(String idPaciente, LocalDateTime desde, LocalDateTime hasta) {
        if (idPaciente == null || idPaciente.isBlank()) {
            log.warn("Intento de verificar auditoría sospechosa con ID de paciente nulo o vacío");
            return false;
        }

        List<AuditoriaCambio> cambios = auditoriaCambioService.obtenerHistorialCambiosPaciente(idPaciente, desde, hasta);
        
        // Detectar actividad anómala:
        // - Cambios sin razón/justificación
        // - Múltiples cambios del mismo dato en poco tiempo
        // - Cambios de datos sensibles sin autorización (no implementado acá por ahora)
        
        long cambiosSinRazon = cambios.stream()
            .filter(c -> c.getRazonCambio() == null || c.getRazonCambio().isBlank())
            .count();

        boolean sospechosa = cambiosSinRazon > (cambios.size() * 0.3); // Más del 30% sin razón
        
        if (sospechosa) {
            log.warn("Se detectó auditoría sospechosa para paciente {}: {} de {} cambios sin razón",
                idPaciente, cambiosSinRazon, cambios.size());
        }
        
        return sospechosa;
    }

    @Override
    @Transactional(readOnly = true)
    public AuditoriaCambioStats obtenerEstadisticas(String idPaciente) {
        if (idPaciente == null || idPaciente.isBlank()) {
            log.warn("Intento de obtener estadísticas con ID de paciente nulo o vacío");
            return new AuditoriaCambioStats();
        }

        List<AuditoriaCambio> cambios = auditoriaCambioService.obtenerHistorialCambiosPaciente(idPaciente);
        
        AuditoriaCambioStats stats = new AuditoriaCambioStats();
        stats.setIdPaciente(idPaciente);
        stats.setTotalCambios(cambios.size());

        // Contar por tipo de operación
        stats.setCreaciones(cambios.stream()
            .filter(c -> c.getTipoOperacion() == TipoOperacion.CREATE)
            .count());
        stats.setActualizaciones(cambios.stream()
            .filter(c -> c.getTipoOperacion() == TipoOperacion.UPDATE)
            .count());
        stats.setEliminaciones(cambios.stream()
            .filter(c -> c.getTipoOperacion() == TipoOperacion.DELETE)
            .count());

        // Cambios por tipo
        Map<String, Long> cambiosPorTipo = cambios.stream()
            .collect(Collectors.groupingBy(
                AuditoriaCambio::getTipoCambio,
                Collectors.counting()
            ));
        stats.setCambiosPorTipo(cambiosPorTipo);

        // Cambios por usuario/médico
        Map<String, Long> cambiosPorUsuario = cambios.stream()
            .collect(Collectors.groupingBy(
                c -> c.getIdMedico() != null ? c.getIdMedico() : c.getIdUsuario(),
                Collectors.counting()
            ));
        stats.setCambiosPorUsuario(cambiosPorUsuario);

        // Cambios por tabla
        Map<String, Long> cambiosPorTabla = cambios.stream()
            .collect(Collectors.groupingBy(
                AuditoriaCambio::getTabla,
                Collectors.counting()
            ));
        stats.setCambiosPorTabla(cambiosPorTabla);

        // Cambios sin razón
        stats.setCambiosSinRazon(cambios.stream()
            .filter(c -> c.getRazonCambio() == null || c.getRazonCambio().isBlank())
            .count());

        // Detectar actividad anómala
        LocalDateTime hace24Horas = LocalDateTime.now(ZoneId.of(EUROPE_MADRID)).minusHours(24);
        stats.setActividadAnomala(auditoriaCambioService.hayAuditoriaSospechosa(idPaciente, hace24Horas, LocalDateTime.now(ZoneId.of(EUROPE_MADRID))));

        log.debug("Estadísticas generadas para paciente {}: {}", idPaciente, stats);
        return stats;
    }
}
