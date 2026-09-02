package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.AuditoriaCambio.TipoOperacion;
import com.hcc.tfm_hcc.repository.AuditoriaCambioRepository;
import com.hcc.tfm_hcc.service.AuditoriaCambioService;
import com.hcc.tfm_hcc.service.AuditoriaCambioStats;
import com.hcc.tfm_hcc.service.FieldEncryptionService;

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
    private final FieldEncryptionService fieldEncryptionService;

    /**
     * {@inheritDoc}
     *
     * <p>El registro de auditoría vive en MongoDB, mientras que el cambio clínico que lo
     * origina se persiste en PostgreSQL dentro de una transacción JPA. Para que no queden
     * registros de auditoría huérfanos (auditoría escrita pero cambio revertido), cuando
     * hay una transacción activa la escritura en Mongo se aplaza a {@code afterCommit}: solo
     * ocurre si la transacción de Postgres confirma. Fuera de transacción se escribe al
     * momento.</p>
     */
    @Override
    public AuditoriaCambio registrarCambio(AuditoriaCambio auditoria) {
        if (auditoria == null) {
            log.warn("Intento de registrar auditoría nula");
            throw new IllegalArgumentException("El registro de auditoría no puede ser nulo");
        }

        if (auditoria.getId() == null) {
            auditoria.setId(java.util.UUID.randomUUID().toString());
        }
        if (auditoria.getFechaCambio() == null) {
            auditoria.setFechaCambio(LocalDateTime.now(ZoneId.of(EUROPE_MADRID)));
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        persistirAuditoria(auditoria);
                    } catch (RuntimeException e) {
                        // La transacción clínica ya está confirmada: no se puede revertir. Se
                        // deja constancia a nivel ERROR para poder detectar el hueco de auditoría.
                        log.error("No se pudo persistir el registro de auditoría tras confirmar el cambio "
                                + "(usuario={}, recurso={}, operacion={}): {}",
                                auditoria.getIdUsuario(), auditoria.getIdRecurso(),
                                auditoria.getTipoOperacion(), e.getMessage(), e);
                    }
                }
            });
            return auditoria;
        }

        return persistirAuditoria(auditoria);
    }

    /**
     * Cifra los valores sensibles y guarda el registro en MongoDB. Devuelve la entidad con
     * los valores otra vez en claro (el cifrado es un detalle de persistencia).
     */
    private AuditoriaCambio persistirAuditoria(AuditoriaCambio auditoria) {
        String valorAnteriorPlano = auditoria.getValorAnterior();
        String valorNuevoPlano = auditoria.getValorNuevo();
        auditoria.setValorAnterior(fieldEncryptionService.cifrar(valorAnteriorPlano));
        auditoria.setValorNuevo(fieldEncryptionService.cifrar(valorNuevoPlano));

        AuditoriaCambio guardado = auditoriaCambioRepository.save(auditoria);

        guardado.setValorAnterior(valorAnteriorPlano);
        guardado.setValorNuevo(valorNuevoPlano);

        log.info(
            "Auditoría registrada: usuario={}, paciente={}, tipo={}, operacion={}",
            guardado.getIdUsuario(), guardado.getIdPaciente(),
            guardado.getTipoCambio(), guardado.getTipoOperacion()
        );

        return guardado;
    }

    /**
     * Descifra {@code valorAnterior}/{@code valorNuevo} de cada registro obtenido del
     * repositorio, para que quien llama a este servicio siempre trabaje con texto en claro.
     */
    private List<AuditoriaCambio> descifrarValores(List<AuditoriaCambio> cambios) {
        cambios.forEach(c -> {
            c.setValorAnterior(fieldEncryptionService.descifrar(c.getValorAnterior()));
            c.setValorNuevo(fieldEncryptionService.descifrar(c.getValorNuevo()));
        });
        return cambios;
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

        return registrarCambio(auditoria);
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
        return descifrarValores(cambios);
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
        return descifrarValores(cambios);
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
        return descifrarValores(cambios);
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
        return descifrarValores(cambios);
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
        return descifrarValores(cambios);
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
        return descifrarValores(cambios);
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
        return descifrarValores(cambios);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hayAuditoriaSospechosa(String idPaciente, LocalDateTime desde, LocalDateTime hasta) {
        if (idPaciente == null || idPaciente.isBlank()) {
            log.warn("Intento de verificar auditoría sospechosa con ID de paciente nulo o vacío");
            return false;
        }

        List<AuditoriaCambio> cambios = obtenerHistorialCambiosPaciente(idPaciente, desde, hasta);
        
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

        List<AuditoriaCambio> cambios = obtenerHistorialCambiosPaciente(idPaciente);
        
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
        stats.setActividadAnomala(hayAuditoriaSospechosa(idPaciente, hace24Horas, LocalDateTime.now(ZoneId.of(EUROPE_MADRID))));

        log.debug("Estadísticas generadas para paciente {}: {}", idPaciente, stats);
        return stats;
    }
}
