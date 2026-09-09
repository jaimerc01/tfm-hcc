package com.hcc.tfm_hcc.facade.impl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoEntradaDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.PropuestaCambioClinicoDTO;
import com.hcc.tfm_hcc.converter.PropuestaCambioClinicoConverter;
import com.hcc.tfm_hcc.facade.HistorialClinicoFacade;
import com.hcc.tfm_hcc.mapper.ArchivoClinicoMapper;
import com.hcc.tfm_hcc.model.ArchivoClinico;
import com.hcc.tfm_hcc.exception.ArchivoClinicoException;
import com.hcc.tfm_hcc.exception.DatosClinicosValidationException;
import com.hcc.tfm_hcc.exception.HistorialClinicoException;
import com.hcc.tfm_hcc.exception.PropuestaCambioClinicoException;
import com.hcc.tfm_hcc.exception.PropuestaCambioNoEncontradaException;
import com.hcc.tfm_hcc.exception.UsuarioNoAutenticadoException;
import com.hcc.tfm_hcc.service.ArchivoClinicoService;
import com.hcc.tfm_hcc.service.HistorialClinicoService;
import com.hcc.tfm_hcc.service.PropuestaCambioClinicoService;
import com.hcc.tfm_hcc.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de la fachada para operaciones de historial clínico.
 * 
 * <p>Esta clase actúa como una capa de fachada entre los controladores y los servicios
 * de historial clínico, proporcionando una interfaz simplificada para las operaciones
 * relacionadas con la gestión de historiales médicos y archivos clínicos.</p>
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Gestión de archivos clínicos (subida, descarga, eliminación)</li>
 *   <li>Actualización de datos del historial clínico</li>
 *   <li>Gestión de identificación, antecedentes, alergias y análisis de sangre</li>
 *   <li>Operaciones CRUD sobre datos clínicos</li>
 *   <li>Validación de permisos y acceso a datos</li>
 * </ul>
 * 
 * <p>La clase utiliza inyección de dependencias por constructor y logging estructurado
 * para seguir las mejores prácticas de desarrollo empresarial.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistorialClinicoFacadeImpl implements HistorialClinicoFacade {

    // ===============================
    // DEPENDENCIAS INYECTADAS
    // ===============================
    
    /**
     * Servicio para operaciones de archivos clínicos.
     */
    private final ArchivoClinicoService archivoClinicoService;
    
    /**
     * Mapper para conversión entre entidades y DTOs de archivo clínico.
     */
    private final ArchivoClinicoMapper archivoClinicoMapper;
    
    /**
     * Servicio para operaciones del historial clínico.
     */
    private final HistorialClinicoService historiaClinicaService;

    /**
     * Servicio de propuestas de cambio clínico (médico propone, paciente confirma).
     */
    private final PropuestaCambioClinicoService propuestaCambioClinicoService;

    /**
     * Convierte las propuestas de cambio clínico a DTO antes de exponerlas en la API.
     */
    private final PropuestaCambioClinicoConverter propuestaCambioClinicoConverter;

    // ===============================
    // MÉTODOS DE ARCHIVOS CLÍNICOS
    // ===============================

    /**
     * Lista todos los archivos clínicos del usuario autenticado.
     * 
     * @return List<ArchivoClinicoDTO> Lista de archivos clínicos del usuario
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public List<ArchivoClinicoDTO> listarArchivos() {
        log.debug("Obteniendo lista de archivos clínicos del usuario autenticado");
        
        try {
            List<ArchivoClinico> archivos = archivoClinicoService.listMine();
            
            List<ArchivoClinicoDTO> resultado = archivos.stream()
                    .map(archivoClinicoMapper::toDto)
                    .toList();
            
            log.info("Archivos clínicos obtenidos: {} registros", resultado.size());
            return resultado;
        } catch (Exception e) {
            log.error("Error inesperado al obtener archivos clínicos del usuario: {}", e.getMessage(), e);
            throw new ArchivoClinicoException("Error interno durante la consulta de archivos clínicos", e);
        }
    }

    /**
     * Sube un nuevo archivo clínico para el usuario autenticado.
     * 
     * @param file El archivo a subir
     * @return ArchivoClinicoDTO El archivo clínico subido con su información
     * @throws DatosClinicosValidationException Si el archivo es inválido
     * @throws IOException Si ocurre un error durante la subida del archivo
     * @throws ArchivoClinicoException Si ocurre un error inesperado
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public ArchivoClinicoDTO upload(MultipartFile file) throws IOException {
        log.debug("Iniciando subida de archivo clínico: {}", 
                file != null ? file.getOriginalFilename() : "null");
        
        try {
            validarArchivo(file);
            
            ArchivoClinico saved = archivoClinicoService.uploadMine(file);
            ArchivoClinicoDTO resultado = archivoClinicoMapper.toDto(saved);
            
            log.info("Archivo clínico subido exitosamente: {} - ID: {}", 
                    file != null ? file.getOriginalFilename() : "unknown", 
                    resultado != null ? resultado.getId() : "unknown");
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al subir archivo: {} - Error: {}", 
                    file != null ? file.getOriginalFilename() : "null", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Error de E/S al subir archivo: {} - Error: {}", 
                     file != null ? file.getOriginalFilename() : "null", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al subir archivo: {} - Error: {}", 
                     file != null ? file.getOriginalFilename() : "null", e.getMessage(), e);
            throw new ArchivoClinicoException("Error interno durante la subida del archivo", e);
        }
    }

    /**
     * Obtiene la información de un archivo clínico específico del usuario autenticado.
     * 
     * @param id ID del archivo clínico
     * @return ArchivoClinicoDTO Los datos del archivo clínico
     * @throws DatosClinicosValidationException Si el ID es inválido
     * @throws ArchivoClinicoException Si el archivo no existe o ocurre un error durante la consulta
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public ArchivoClinicoDTO getArchivoClinico(UUID id) {
        log.debug("Obteniendo archivo clínico: {}", id);
        
        try {
            validarId(id);
            
            ArchivoClinico archivoClinico = archivoClinicoService.listMine().stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new ArchivoClinicoException("No existe el archivo especificado"));
            
            ArchivoClinicoDTO resultado = archivoClinicoMapper.toDto(archivoClinico);
            
            log.info("Archivo clínico obtenido exitosamente: {}", id);
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al obtener archivo: {} - Error: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener archivo: {} - Error: {}", id, e.getMessage(), e);
            throw new ArchivoClinicoException("Error interno durante la consulta del archivo", e);
        }
    }

    /**
     * Obtiene el recurso físico de un archivo clínico del usuario autenticado.
     * 
     * @param id ID del archivo clínico
     * @return Resource El recurso del archivo para descarga
    * @throws DatosClinicosValidationException Si el ID es inválido
    * @throws ArchivoClinicoException Si ocurre un error durante la consulta
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public Resource getMineResource(UUID id) {
        log.debug("Obteniendo recurso de archivo clínico: {}", id);
        
        try {
            validarId(id);
            
            Resource resource = archivoClinicoService.getMineResource(id);
            
            log.info("Recurso de archivo obtenido exitosamente: {}", id);
            return resource;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al obtener recurso de archivo: {} - Error: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener recurso de archivo: {} - Error: {}", id, e.getMessage(), e);
            throw new ArchivoClinicoException("Error interno durante la consulta del recurso", e);
        }
    }

    /**
     * Elimina un archivo clínico del usuario autenticado.
     * 
     * @param id ID del archivo clínico a eliminar
     * @throws DatosClinicosValidationException Si el ID es inválido
     * @throws IOException Si ocurre un error durante la eliminación del archivo
     * @throws ArchivoClinicoException Si ocurre un error inesperado
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public void borrarArchivoClinico(UUID id) throws IOException {
        log.debug("Eliminando archivo clínico: {}", id);
        
        try {
            validarId(id);
            
            archivoClinicoService.borrarArchivo(id);
            
            log.info("Archivo clínico eliminado exitosamente: {}", id);
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al eliminar archivo: {} - Error: {}", id, e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Error de E/S al eliminar archivo: {} - Error: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar archivo: {} - Error: {}", id, e.getMessage(), e);
            throw new ArchivoClinicoException("Error interno durante la eliminación del archivo", e);
        }
    }

    // ===============================
    // MÉTODOS DE HISTORIAL CLÍNICO
    // ===============================

    /**
     * Obtiene el historial clínico completo del usuario autenticado.
     * 
     * @return HistorialClinicoDTO Los datos del historial clínico del usuario
    * @throws HistorialClinicoException Si ocurre un error durante la consulta
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO obtenerMiHistoria() {
        log.debug("Obteniendo historial clínico del usuario autenticado");
        
        try {
            HistorialClinicoDTO historial = historiaClinicaService.obtenerHistoriaUsuarioActual();
            
            log.info("Historial clínico obtenido exitosamente para el usuario autenticado");
            return historial;
        } catch (Exception e) {
            log.error("Error inesperado al obtener historial clínico: {}", e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la consulta del historial clínico", e);
        }
    }


    /**
     * Crea un nuevo antecedente clínico (personal o familiar) en el historial.
     *
     * @param antecedenteDTO categoría y descripción del antecedente
     * @return HistorialClinicoDTO El historial clínico actualizado
     * @throws DatosClinicosValidationException Si los datos son inválidos
     * @throws HistorialClinicoException Si ocurre un error durante la creación
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO crearAntecedente(AntecedenteClinicoDTO antecedenteDTO) {
        log.debug("Creando antecedente clínico en historial clínico");

        try {
            validarAntecedenteDTO(antecedenteDTO);

            HistorialClinicoDTO resultado = historiaClinicaService.crearAntecedente(antecedenteDTO);

            log.info("Antecedente clínico creado exitosamente en historial clínico");
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al crear antecedente: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear antecedente: {}", e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la creación del antecedente", e);
        }
    }

    /**
     * Edita un antecedente clínico existente.
     *
     * @param id ID del antecedente a editar
     * @param antecedenteDTO categoría y descripción actualizadas
     * @return HistorialClinicoDTO El historial clínico actualizado
     * @throws DatosClinicosValidationException Si el ID o los datos son inválidos
     * @throws HistorialClinicoException Si ocurre un error durante la edición
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO editarAntecedente(UUID id, AntecedenteClinicoDTO antecedenteDTO) {
        log.debug("Editando antecedente clínico: {}", id);

        try {
            validarId(id);
            validarAntecedenteDTO(antecedenteDTO);

            HistorialClinicoDTO resultado = historiaClinicaService.editarAntecedente(id, antecedenteDTO);

            log.info("Antecedente clínico editado exitosamente: {}", id);
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al editar antecedente {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al editar antecedente {}: {}", id, e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la edición del antecedente", e);
        }
    }

    /**
     * Elimina un antecedente clínico específico.
     *
     * @param id ID del antecedente a eliminar
     * @return HistorialClinicoDTO El historial clínico actualizado
     * @throws DatosClinicosValidationException Si el ID es inválido
     * @throws HistorialClinicoException Si ocurre un error durante la eliminación
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO borrarAntecedente(UUID id) {
        log.debug("Borrando antecedente clínico: {}", id);

        try {
            validarId(id);

            HistorialClinicoDTO resultado = historiaClinicaService.borrarAntecedente(id);

            log.info("Antecedente clínico borrado exitosamente: {}", id);
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al borrar antecedente {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al borrar antecedente {}: {}", id, e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la eliminación del antecedente", e);
        }
    }

    /**
     * Crea una nueva alergia o intolerancia en el historial clínico.
     *
     * @param alergiaDTO descripción de la alergia
     * @return HistorialClinicoDTO El historial clínico actualizado
     * @throws DatosClinicosValidationException Si los datos son inválidos
     * @throws HistorialClinicoException Si ocurre un error durante la creación
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO crearAlergia(AlergiaDTO alergiaDTO) {
        log.debug("Creando alergia en historial clínico");

        try {
            validarAlergiaDTO(alergiaDTO);

            HistorialClinicoDTO resultado = historiaClinicaService.crearAlergia(alergiaDTO);

            log.info("Alergia creada exitosamente en historial clínico");
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al crear alergia: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear alergia: {}", e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la creación de la alergia", e);
        }
    }

    /**
     * Elimina una alergia específica.
     *
     * @param id ID de la alergia a eliminar
     * @return HistorialClinicoDTO El historial clínico actualizado
     * @throws DatosClinicosValidationException Si el ID es inválido
     * @throws HistorialClinicoException Si ocurre un error durante la eliminación
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO borrarAlergia(UUID id) {
        log.debug("Borrando alergia: {}", id);

        try {
            validarId(id);

            HistorialClinicoDTO resultado = historiaClinicaService.borrarAlergia(id);

            log.info("Alergia borrada exitosamente: {}", id);
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al borrar alergia {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al borrar alergia {}: {}", id, e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la eliminación de la alergia", e);
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO actualizarAnalisisSangre(List<DatoClinicoEntradaDTO> analisis) {
        return guardarMediciones("análisis de sangre", analisis, historiaClinicaService::actualizarAnalisisSangre);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO anadirAnalisisSangre(List<DatoClinicoEntradaDTO> analisis) {
        return guardarMediciones("análisis de sangre", analisis, historiaClinicaService::añadirAnalisisSangre);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO actualizarSignosVitales(List<DatoClinicoEntradaDTO> signosVitales) {
        return guardarMediciones("signos vitales", signosVitales, historiaClinicaService::actualizarSignosVitales);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO anadirSignosVitales(List<DatoClinicoEntradaDTO> signosVitales) {
        return guardarMediciones("signos vitales", signosVitales, historiaClinicaService::añadirSignosVitales);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO actualizarAnalisisOrina(List<DatoClinicoEntradaDTO> analisisOrina) {
        return guardarMediciones("análisis de orina", analisisOrina, historiaClinicaService::actualizarAnalisisOrina);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO anadirAnalisisOrina(List<DatoClinicoEntradaDTO> analisisOrina) {
        return guardarMediciones("análisis de orina", analisisOrina, historiaClinicaService::añadirAnalisisOrina);
    }

    /**
     * Flujo común de los seis endpoints de datos clínicos cuantitativos: valida la entrada,
     * delega en el servicio y normaliza el manejo de errores (validación → se propaga tal
     * cual; cualquier otro fallo → {@link HistorialClinicoException}).
     *
     * @param etiqueta  nombre del dominio para los mensajes de log ("análisis de sangre"...)
     * @param datos     mediciones recibidas del cliente
     * @param operacion método del servicio que persiste las mediciones
     */
    private HistorialClinicoDTO guardarMediciones(String etiqueta, List<DatoClinicoEntradaDTO> datos,
            java.util.function.Function<List<DatoClinicoEntradaDTO>, HistorialClinicoDTO> operacion) {
        log.debug("Guardando {} en historial clínico", etiqueta);

        try {
            validarMediciones(datos, etiqueta);

            HistorialClinicoDTO resultado = operacion.apply(datos);

            log.info("{} guardados exitosamente en historial clínico", etiqueta);
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al guardar {}: {}", etiqueta, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar {}: {}", etiqueta, e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante el guardado de " + etiqueta, e);
        }
    }

    // ===============================
    // MÉTODOS DE GESTIÓN DE DATOS
    // ===============================

    /**
     * Borra un dato clínico específico del historial.
     * 
     * @param id ID del dato clínico a borrar
     * @throws DatosClinicosValidationException Si el ID es inválido
     * @throws HistorialClinicoException Si ocurre un error durante la eliminación
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public void borrarDatoClinico(UUID id) {
        log.debug("Borrando dato clínico: {}", id);
        
        try {
            validarId(id);
            
            historiaClinicaService.borrarDatoClinico(id);
            
            log.info("Dato clínico borrado exitosamente: {}", id);
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al borrar dato clínico: {} - Error: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al borrar dato clínico: {} - Error: {}", id, e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la eliminación del dato clínico", e);
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO editarDatoClinico(UUID id, DatoClinicoEntradaDTO datos) {
        log.debug("Editando dato clínico {} del historial del usuario autenticado", id);
        try {
            validarId(id);
            if (datos == null) {
                throw new DatosClinicosValidationException(ErrorMessages.ERROR_ANALISIS_VALUE_REQUERIDO);
            }
            HistorialClinicoDTO resultado = historiaClinicaService.editarDatoClinico(id, datos);
            log.info("Dato clínico {} editado exitosamente", id);
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al editar dato clínico {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al editar dato clínico {}: {}", id, e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la edición del dato clínico", e);
        }
    }

    // ===============================
    // MÉTODOS DE PROPUESTAS DE CAMBIO CLÍNICO
    // ===============================

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<PropuestaCambioClinicoDTO> listarPropuestasCambio() {
        log.debug("Listando propuestas de cambio del usuario autenticado");
        String nif = nifUsuarioAutenticado();
        try {
            return propuestaCambioClinicoConverter.toDtoList(
                    propuestaCambioClinicoService.listarPropuestasParaPaciente(nif));
        } catch (Exception e) {
            log.error("Error inesperado al listar propuestas de cambio: {}", e.getMessage(), e);
            throw new HistorialClinicoException("Error interno al listar las propuestas de cambio", e);
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public PropuestaCambioClinicoDTO responderPropuestaCambio(UUID id, boolean aceptar) {
        log.debug("Resolviendo propuesta de cambio {} (aceptar={})", id, aceptar);
        String nif = nifUsuarioAutenticado();
        try {
            validarId(id);
            var propuesta = propuestaCambioClinicoService.resolver(nif, id, aceptar);
            log.info("Propuesta de cambio {} resuelta por el paciente ({})", id, aceptar ? "aceptada" : "rechazada");
            return propuestaCambioClinicoConverter.toDto(propuesta);
        } catch (DatosClinicosValidationException | PropuestaCambioClinicoException
                 | PropuestaCambioNoEncontradaException e) {
            log.warn("No se pudo resolver la propuesta de cambio {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al resolver la propuesta de cambio {}: {}", id, e.getMessage(), e);
            throw new HistorialClinicoException("Error interno al resolver la propuesta de cambio", e);
        }
    }

    private String nifUsuarioAutenticado() {
        String nif = SecurityUtils.getCurrentUserNif();
        if (nif == null) {
            throw new UsuarioNoAutenticadoException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }
        return nif;
    }

    // ===============================
    // MÉTODOS UTILITARIOS PRIVADOS
    // ===============================

    /**
     * Valida un archivo para subida.
     * 
     * @param file Archivo a validar
     * @throws DatosClinicosValidationException Si el archivo es inválido
     */
    private void validarArchivo(MultipartFile file) {
        if (file == null) {
            throw new DatosClinicosValidationException("El archivo no puede ser nulo");
        }
        
        if (file.isEmpty()) {
            throw new DatosClinicosValidationException("El archivo no puede estar vacío");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new DatosClinicosValidationException("El archivo debe tener un nombre válido");
        }
        
        // Validar tamaño máximo (por ejemplo, 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new DatosClinicosValidationException("El archivo no puede superar los 10MB");
        }
    }

    /**
     * Valida un ID UUID.
     * 
     * @param id ID a validar
     * @throws DatosClinicosValidationException Si el ID es inválido
     */
    private void validarId(UUID id) {
        if (id == null) {
            throw new DatosClinicosValidationException("El ID no puede ser nulo");
        }
    }

    /**
     * Valida que la lista de mediciones no sea nula ni vacía.
     *
     * @param datos mediciones a validar
     * @param tipo  nombre del dominio para el mensaje de error ("análisis de sangre"...)
     * @throws DatosClinicosValidationException si no hay ninguna medición
     */
    private void validarMediciones(List<DatoClinicoEntradaDTO> datos, String tipo) {
        if (datos == null || datos.isEmpty()) {
            throw new DatosClinicosValidationException("Los datos de " + tipo + " no pueden estar vacíos");
        }
    }

    /**
     * Valida un texto y su tipo.
     * 
     * @param texto Texto a validar
     * @param tipo Tipo de datos que representa el texto
     * @throws DatosClinicosValidationException Si el texto es inválido
     */
    private void validarTexto(String texto, String tipo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new DatosClinicosValidationException("El " + tipo + " no puede ser nulo o vacío");
        }
        
        if (texto.length() > 5000) {
            throw new DatosClinicosValidationException("El " + tipo + " no puede superar los 5000 caracteres");
        }
    }

    /**
     * Valida los datos de un antecedente clínico.
     *
     * @param antecedenteDTO DTO a validar
     * @throws DatosClinicosValidationException Si los datos son inválidos
     */
    private void validarAntecedenteDTO(AntecedenteClinicoDTO antecedenteDTO) {
        if (antecedenteDTO == null) {
            throw new DatosClinicosValidationException("Los datos del antecedente no pueden ser nulos");
        }
        validarTexto(antecedenteDTO.getCategoria(), "categoría del antecedente");
        validarTexto(antecedenteDTO.getDescripcion(), "descripción del antecedente");
    }

    /**
     * Valida los datos de una alergia.
     *
     * @param alergiaDTO DTO a validar
     * @throws DatosClinicosValidationException Si los datos son inválidos
     */
    private void validarAlergiaDTO(AlergiaDTO alergiaDTO) {
        if (alergiaDTO == null) {
            throw new DatosClinicosValidationException("Los datos de la alergia no pueden ser nulos");
        }
        validarTexto(alergiaDTO.getDescripcion(), "descripción de la alergia");
    }
}
