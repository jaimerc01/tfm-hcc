package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.exception.MedicoValidationException;
import com.hcc.tfm_hcc.model.AnotacionMedica;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.MedicoPaciente;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.AnotacionMedicaRepository;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AnotacionMedicaService;
import com.hcc.tfm_hcc.service.AuditoriaCambioService;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.util.LogMaskUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de anotaciones médicas.
 *
 * @author Sistema HCC
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnotacionMedicaServiceImpl implements AnotacionMedicaService {

    private static final String ZONE_ID_EUROPA_MADRID = "Europe/Madrid";
    private static final String CAMPO_MENSAJE = "mensaje";
    private static final String TIPO_CAMBIO_ANOTACION_MEDICA = "ANOTACION_MEDICA";
    private static final String TABLA_ANOTACION_MEDICA = "anotacion_medica";
    private static final String RAZON_CREACION_ANOTACION = "Creación de anotación médica";

    private final AnotacionMedicaRepository anotacionMedicaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicoPacienteRepository medicoPacienteRepository;
    private final HmacSearchIndexService hmacSearchIndexService;
    private final AuditoriaCambioService auditoriaCambioService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AnotacionMedica crearAnotacion(String nifMedico, String nifPaciente, String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            String error = ErrorMessages.campoRequerido(CAMPO_MENSAJE);
            log.warn(error);
            throw new MedicoValidationException(error);
        }

        Usuario medico = resolverMedicoAutenticado(nifMedico);
        Usuario paciente = buscarUsuarioPorNif(nifPaciente);

        validarAccesoMedicoAPaciente(medico, paciente);

        AnotacionMedica anotacion = new AnotacionMedica();
        anotacion.setMedico(medico);
        anotacion.setPaciente(paciente);
        anotacion.setMensaje(mensaje.trim());
        anotacion.setFechaCreacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));

        AnotacionMedica guardada = anotacionMedicaRepository.save(anotacion);

        auditoriaCambioService.registrarCambio(
            medico.getId().toString(),
            paciente.getId().toString(),
            medico.getId().toString(),
            TIPO_CAMBIO_ANOTACION_MEDICA,
            TABLA_ANOTACION_MEDICA,
            guardada.getId().toString(),
            "",
            guardada.getMensaje(),
            AuditoriaCambio.TipoOperacion.CREATE,
            RAZON_CREACION_ANOTACION
        );

        log.info("Anotación médica creada: {} del médico {} para el paciente {}",
            guardada.getId(), LogMaskUtil.enmascarar(nifMedico), LogMaskUtil.enmascarar(nifPaciente));

        return guardada;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AnotacionMedica> listarAnotacionesPaciente(String nifPaciente, String nifMedicoFiltro, LocalDateTime desde, LocalDateTime hasta) {
        Usuario paciente = buscarUsuarioPorNif(nifPaciente);

        List<AnotacionMedica> anotaciones = obtenerAnotaciones(paciente, nifMedicoFiltro);

        return anotaciones.stream()
            .filter(anotacion -> dentroDelRango(anotacion.getFechaCreacion(), desde, hasta))
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AnotacionMedica> listarAnotacionesEscritasPorMedico(String nifMedico, String nifPaciente) {
        Usuario medico = resolverMedicoAutenticado(nifMedico);
        Usuario paciente = buscarUsuarioPorNif(nifPaciente);

        validarAccesoMedicoAPaciente(medico, paciente);

        return anotacionMedicaRepository.findByPacienteIdAndMedicoIdOrderByFechaCreacionDesc(paciente.getId(), medico.getId());
    }

    private List<AnotacionMedica> obtenerAnotaciones(Usuario paciente, String nifMedicoFiltro) {
        if (nifMedicoFiltro == null || nifMedicoFiltro.isBlank()) {
            return anotacionMedicaRepository.findByPacienteIdOrderByFechaCreacionDesc(paciente.getId());
        }

        // Un filtro que no corresponde a ningún médico real se trata como "sin resultados",
        // no como un error: es solo un criterio de búsqueda, no un recurso que deba existir.
        Optional<Usuario> medicoFiltro = usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nifMedicoFiltro));
        if (medicoFiltro.isEmpty()) {
            return List.of();
        }
        return anotacionMedicaRepository.findByPacienteIdAndMedicoIdOrderByFechaCreacionDesc(paciente.getId(), medicoFiltro.get().getId());
    }

    private boolean dentroDelRango(LocalDateTime fecha, LocalDateTime desde, LocalDateTime hasta) {
        if (fecha == null) {
            return false;
        }
        if (desde != null && fecha.isBefore(desde)) {
            return false;
        }
        return hasta == null || !fecha.isAfter(hasta);
    }

    private void validarAccesoMedicoAPaciente(Usuario medico, Usuario paciente) {
        if (Usuario.ESTADO_CUENTA_SUSPENDIDO.equals(paciente.getEstadoCuenta())) {
            throw new IllegalStateException(ErrorMessages.ERROR_TRATAMIENTO_LIMITADO);
        }
        boolean autorizado = medicoPacienteRepository.existsByMedicoIdAndPacienteIdAndEstado(
            medico.getId(), paciente.getId(), MedicoPaciente.ESTADO_ACTIVA);
        if (!autorizado) {
            throw new IllegalStateException(ErrorMessages.ERROR_ACCESO_DENEGADO);
        }
    }

    /**
     * Busca un usuario referenciado externamente (el paciente, o el médico de un filtro
     * de búsqueda). Su ausencia es un dato de entrada incorrecto, no un fallo del sistema.
     */
    private Usuario buscarUsuarioPorNif(String nif) {
        if (nif == null || nif.isBlank()) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO);
        }
        return usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nif))
            .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }

    /**
     * Resuelve al médico autor a partir del NIF del usuario ya autenticado. Si no se
     * encuentra su cuenta es un estado inconsistente del sistema, no un dato de entrada
     * incorrecto (por eso lanza {@link IllegalStateException} en vez de
     * {@link IllegalArgumentException}, igual que {@code HistorialClinicoServiceImpl}
     * al resolver al usuario autenticado).
     */
    private Usuario resolverMedicoAutenticado(String nifMedico) {
        return usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nifMedico))
            .orElseThrow(() -> new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }
}
