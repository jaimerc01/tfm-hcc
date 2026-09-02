package com.hcc.tfm_hcc.controller.impl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.MedicoController;
import com.hcc.tfm_hcc.converter.AnotacionMedicaConverter;
import com.hcc.tfm_hcc.converter.SolicitudAsignacionConverter;
import com.hcc.tfm_hcc.dto.AnotacionMedicaDTO;
import com.hcc.tfm_hcc.dto.AnotacionMedicaRequestDTO;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.dto.SolicitudAsignacionDTO;
import com.hcc.tfm_hcc.facade.MedicoFacade;
import com.hcc.tfm_hcc.model.AnotacionMedica;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.exception.PacienteNoEncontradoException;
import com.hcc.tfm_hcc.exception.SolicitudAsignacionException;
import com.hcc.tfm_hcc.exception.SolicitudExistenteException;
import com.hcc.tfm_hcc.exception.MedicoOperacionException;
import com.hcc.tfm_hcc.exception.MedicoValidationException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.util.LogMaskUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del controlador REST para operaciones médicas.
 * Proporciona endpoints REST para funcionalidades específicas de médicos,
 * delegando la lógica de negocio al facade correspondiente.
 * 
 * <p>Características de seguridad:</p>
 * <ul>
 *   <li>Todos los endpoints requieren rol MEDICO</li>
 *   <li>Validación de autorización delegada a la capa facade</li>
 *   <li>Logging detallado de operaciones médicas</li>
 *   <li>Gestión centralizada de errores específicos</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping(RestUrls.MEDICO_BASE)
@RequiredArgsConstructor
public class MedicoControllerImpl implements MedicoController {

    /** Facade para operaciones médicas */
    private final MedicoFacade medicoFacade;

    /** Convierte las entidades de solicitud a DTO antes de exponerlas en la API */
    private final SolicitudAsignacionConverter solicitudAsignacionConverter;

    /** Convierte las entidades de anotación médica a DTO antes de exponerlas en la API */
    private final AnotacionMedicaConverter anotacionMedicaConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.MEDICO_PACIENTES_BUSCAR)
    public ResponseEntity<PacienteDTO> buscarPaciente(@RequestParam("dni") String dni, 
                                                      @RequestParam("fechaNacimiento") String fechaNacimiento) {
        String dniLog = LogMaskUtil.enmascarar(dni);
        log.info("Buscando paciente con DNI: {} y fecha nacimiento: {}", dniLog, fechaNacimiento);

        try {
            PacienteDTO paciente = medicoFacade.buscarPacientePorDniYFechaNacimiento(dni, fechaNacimiento);
            if (paciente == null) {
                log.warn("Paciente no encontrado con DNI: {} y fecha nacimiento: {}", dniLog, fechaNacimiento);
                throw new PacienteNoEncontradoException("No se encontró paciente con los datos proporcionados");
            }

            log.info("Paciente encontrado exitosamente: {}", dniLog);
            return ResponseEntity.ok(paciente);
            
        } catch (PacienteNoEncontradoException e) {
            log.warn("Error: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al buscar paciente: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error al buscar paciente", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.MEDICO_PACIENTES)
    public ResponseEntity<List<PacienteDTO>> listarMisPacientes() {
        log.info("Listando pacientes asignados al médico autenticado");

        try {
            List<PacienteDTO> pacientes = medicoFacade.listarMisPacientes();
            log.info("Se obtuvieron {} pacientes asignados al médico", pacientes.size());
            return ResponseEntity.ok(pacientes);
        } catch (Exception e) {
            log.error("Error al listar pacientes asignados: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error al listar pacientes asignados", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.MEDICO_PACIENTE_HISTORIAL)
    public ResponseEntity<HistorialClinicoDTO> obtenerHistorialPaciente(@PathVariable("nif") String nif) {
        String nifLog = LogMaskUtil.enmascarar(nif);
        log.info("Consultando historial clínico del paciente NIF: {}", nifLog);

        try {
            HistorialClinicoDTO historial = medicoFacade.obtenerHistorialPaciente(nif);
            log.info("Historial clínico consultado exitosamente para el paciente: {}", nifLog);
            return ResponseEntity.ok(historial != null ? historial : new HistorialClinicoDTO());
        } catch (PacienteNoEncontradoException e) {
            log.warn("Paciente no encontrado al consultar historial: {}", nifLog);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al consultar historial del paciente {}: {}", nifLog, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (UsuarioSinPermisoException e) {
            log.warn("Acceso denegado al historial del paciente {}: {}", nifLog, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error al consultar historial del paciente {}: {}", nifLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error al consultar historial del paciente", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.MEDICO_SOLICITUDES_ASIGNACION)
    public ResponseEntity<SolicitudAsignacionDTO> crearSolicitudAsignacion(@RequestParam("nifPaciente") String nifPaciente) {
        String nifPacienteLog = LogMaskUtil.enmascarar(nifPaciente);
        log.info("Creando solicitud de asignación para paciente NIF: {}", nifPacienteLog);

        try {
            SolicitudAsignacion solicitud = medicoFacade.crearSolicitudAsignacion(nifPaciente);
            if (solicitud == null) {
                log.warn("No se pudo crear solicitud para paciente: {}", nifPacienteLog);
                throw new SolicitudAsignacionException("No se pudo crear la solicitud de asignación");
            }

            log.info("Solicitud de asignación creada exitosamente para paciente: {}", nifPacienteLog);
            return ResponseEntity.ok(solicitudAsignacionConverter.toDto(solicitud));

        } catch (SolicitudExistenteException e) {
            log.warn("Solicitud ya existe para paciente {}: {}", nifPacienteLog, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (SolicitudAsignacionException e) {
            log.warn("Error al crear solicitud de asignación para paciente {}: {}", nifPacienteLog, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error al crear solicitud de asignación: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error al crear solicitud de asignación", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.MEDICO_SOLICITUDES_PENDIENTES)
    public ResponseEntity<List<SolicitudAsignacionDTO>> listarSolicitudesPendientes() {
        log.info("Listando solicitudes pendientes para médico autenticado");

        try {
            List<SolicitudAsignacion> lista = medicoFacade.listarSolicitudesPendientes();
            log.info("Se obtuvieron {} solicitudes pendientes para el médico", lista.size());
            return ResponseEntity.ok(solicitudAsignacionConverter.toDtoList(lista));

        } catch (Exception e) {
            log.error("Error al listar solicitudes pendientes: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error al listar solicitudes pendientes", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.MEDICO_SOLICITUDES_ENVIADAS)
    public ResponseEntity<List<SolicitudAsignacionDTO>> listarSolicitudesEnviadas() {
        log.info("Listando solicitudes enviadas para médico autenticado");

        try {
            List<SolicitudAsignacion> lista = medicoFacade.listarSolicitudesEnviadas();
            log.info("Se obtuvieron {} solicitudes enviadas para el médico", lista.size());
            return ResponseEntity.ok(solicitudAsignacionConverter.toDtoList(lista));

        } catch (Exception e) {
            log.error("Error al listar solicitudes enviadas: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error al listar solicitudes enviadas", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.MEDICO_PACIENTE_ANOTACIONES)
    public ResponseEntity<AnotacionMedicaDTO> crearAnotacion(@PathVariable("nif") String nifPaciente,
                                                             @RequestBody AnotacionMedicaRequestDTO request) {
        String nifPacienteLog = LogMaskUtil.enmascarar(nifPaciente);
        log.info("Creando anotación médica para paciente NIF: {}", nifPacienteLog);

        try {
            String mensaje = request != null ? request.getMensaje() : null;
            AnotacionMedica anotacion = medicoFacade.crearAnotacion(nifPaciente, mensaje);

            log.info("Anotación médica creada exitosamente para paciente: {}", nifPacienteLog);
            return ResponseEntity.ok(anotacionMedicaConverter.toDto(anotacion));

        } catch (PacienteNoEncontradoException e) {
            log.warn("Paciente no encontrado al crear anotación médica: {}", nifPacienteLog);
            return ResponseEntity.notFound().build();
        } catch (UsuarioSinPermisoException e) {
            log.warn("Acceso denegado al crear anotación médica para el paciente {}: {}", nifPacienteLog, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error al crear anotación médica para el paciente {}: {}", nifPacienteLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error al crear anotación médica", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.MEDICO_PACIENTE_ARCHIVOS)
    public ResponseEntity<List<ArchivoClinicoDTO>> listarArchivosPaciente(@PathVariable("nif") String nif) {
        String nifLog = LogMaskUtil.enmascarar(nif);
        log.info("Listando documentos del paciente NIF: {}", nifLog);
        try {
            return ResponseEntity.ok(medicoFacade.listarArchivosPaciente(nif));
        } catch (UsuarioSinPermisoException e) {
            log.warn("Acceso denegado al listar documentos del paciente {}: {}", nifLog, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error al listar documentos del paciente {}: {}", nifLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error al listar los documentos del paciente", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.MEDICO_PACIENTE_ARCHIVOS)
    public ResponseEntity<ArchivoClinicoDTO> subirArchivoPaciente(@PathVariable("nif") String nif,
                                                                  @RequestParam("file") MultipartFile file) throws IOException {
        String nifLog = LogMaskUtil.enmascarar(nif);
        log.info("Subiendo documento para el paciente NIF: {}", nifLog);
        try {
            return ResponseEntity.ok(medicoFacade.subirArchivoPaciente(nif, file));
        } catch (UsuarioSinPermisoException e) {
            log.warn("Acceso denegado al subir documento para el paciente {}: {}", nifLog, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.warn("Documento inválido para el paciente {}: {}", nifLog, e.getMessage());
            throw new MedicoValidationException(e.getMessage());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al subir documento para el paciente {}: {}", nifLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error al subir el documento del paciente", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.MEDICO_PACIENTE_ARCHIVO_ID)
    public ResponseEntity<Resource> descargarArchivoPaciente(@PathVariable("nif") String nif, @PathVariable("id") UUID id) {
        String nifLog = LogMaskUtil.enmascarar(nif);
        log.info("Descargando documento {} del paciente NIF: {}", id, nifLog);
        try {
            ArchivoClinicoDTO metadatos = medicoFacade.obtenerArchivoPaciente(nif, id);
            Resource resource = medicoFacade.descargarArchivoPaciente(nif, id);

            String nombreSeguro = sanitizarNombreArchivo(metadatos.getNombreOriginal());
            MediaType mediaType = metadatos.getContentType() != null
                    ? MediaType.parseMediaType(metadatos.getContentType())
                    : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreSeguro + "\"")
                    .header("Access-Control-Expose-Headers", HttpHeaders.CONTENT_DISPOSITION)
                    .body(resource);
        } catch (UsuarioSinPermisoException e) {
            log.warn("Acceso denegado al descargar documento del paciente {}: {}", nifLog, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.warn("Documento no encontrado para el paciente {}: {}", nifLog, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al descargar documento del paciente {}: {}", nifLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error al descargar el documento del paciente", e);
        }
    }

    /**
     * Deja el nombre de fichero apto para la cabecera {@code Content-Disposition}:
     * sin comillas ni saltos de línea que permitan inyectar cabeceras adicionales.
     */
    private String sanitizarNombreArchivo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "documento";
        }
        return nombre.replaceAll("[\"\\r\\n]", "_");
    }
}
