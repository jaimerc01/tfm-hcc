package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.constants.TiposDatoClinico;
import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.converter.AlergiaConverter;
import com.hcc.tfm_hcc.converter.AntecedenteClinicoConverter;
import com.hcc.tfm_hcc.converter.HistorialClinicoConverter;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.Alergia;
import com.hcc.tfm_hcc.model.AntecedenteClinico;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.DatoClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.AlergiaRepository;
import com.hcc.tfm_hcc.repository.AntecedenteClinicoRepository;
import com.hcc.tfm_hcc.repository.DatoClinicoRepository;
import com.hcc.tfm_hcc.repository.HistorialClinicoRepository;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.repository.RangoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AuditoriaCambioService;
import com.hcc.tfm_hcc.service.HistorialClinicoService;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;

@Service
public class HistorialClinicoServiceImpl implements HistorialClinicoService {

    private static final String CREATED_AT = "createdAt";
    private static final String DATO_CLINICO = "dato_clinico";
    private static final String ALERGIA = "ALERGIA";
    private static final String ALERGIA_TABLA = "alergia";
    private static final String ANTECEDENTE_CLINICO = "ANTECEDENTE_CLINICO";
    private static final String ANTECEDENTE_CLINICO_TABLA = "antecedente_clinico";
    private static final String TIPO_ANALISIS_DEFAULT = "ANALISIS";
    private static final String TIPO_CAMBIO_ANALISIS_SANGRE = "ANALISIS_SANGRE";
    private static final String TIPO_CAMBIO_SIGNOS_VITALES = "SIGNOS_VITALES";
    private static final String TIPO_CAMBIO_ANALISIS_ORINA = "ANALISIS_ORINA";
    private static final String ZONE_ID_EUROPA_MADRID = "Europe/Madrid";
    private static final String ESTADO_MEDICO_PACIENTE_ACTIVA = "ACTIVA";

    private final HistorialClinicoRepository historiaRepo;
    private final UsuarioFacade usuarioFacade;
    private final UsuarioRepository usuarioRepository;
    private final DatoClinicoRepository datoClinicoRepository;
    private final AlergiaRepository alergiaRepository;
    private final AntecedenteClinicoRepository antecedenteClinicoRepository;
    private final RangoRepository rangoRepository;
    private final MedicoPacienteRepository medicoPacienteRepository;
    private final AuditoriaCambioService auditoriaCambioService;
    private final HistorialClinicoConverter historialClinicoConverter;
    private final AlergiaConverter alergiaConverter;
    private final AntecedenteClinicoConverter antecedenteClinicoConverter;
    private final ObjectMapper objectMapper;
    private final HmacSearchIndexService hmacSearchIndexService;

    public HistorialClinicoServiceImpl(
            HistorialClinicoRepository historiaRepo,
            UsuarioFacade usuarioFacade,
            UsuarioRepository usuarioRepository,
            DatoClinicoRepository datoClinicoRepository,
            AlergiaRepository alergiaRepository,
            AntecedenteClinicoRepository antecedenteClinicoRepository,
            RangoRepository rangoRepository,
            MedicoPacienteRepository medicoPacienteRepository,
            AuditoriaCambioService auditoriaCambioService,
            HistorialClinicoConverter historialClinicoConverter,
            AlergiaConverter alergiaConverter,
            AntecedenteClinicoConverter antecedenteClinicoConverter,
            HmacSearchIndexService hmacSearchIndexService) {
        this.historiaRepo = historiaRepo;
        this.usuarioFacade = usuarioFacade;
        this.usuarioRepository = usuarioRepository;
        this.datoClinicoRepository = datoClinicoRepository;
        this.alergiaRepository = alergiaRepository;
        this.antecedenteClinicoRepository = antecedenteClinicoRepository;
        this.rangoRepository = rangoRepository;
        this.medicoPacienteRepository = medicoPacienteRepository;
        this.auditoriaCambioService = auditoriaCambioService;
        this.historialClinicoConverter = historialClinicoConverter;
        this.alergiaConverter = alergiaConverter;
        this.antecedenteClinicoConverter = antecedenteClinicoConverter;
        this.objectMapper = new ObjectMapper();
        this.hmacSearchIndexService = hmacSearchIndexService;
    }

    /**
     * Construye el DTO completo del historial clínico, incluyendo antecedentes,
     * alergias y datos clínicos cuantitativos (análisis, signos vitales...).
     */
    private HistorialClinicoDTO construirHistorialClinicoDTO(HistorialClinico historial) {
        List<DatoClinico> datosClinicos = datoClinicoRepository.findByHistorialClinico(historial);
        List<Alergia> alergias = alergiaRepository.findByHistorialClinico(historial);
        List<AntecedenteClinico> antecedentes = antecedenteClinicoRepository.findByHistorialClinico(historial);
        return historialClinicoConverter.toDto(historial, datosClinicos, alergias, antecedentes);
    }

    /**
     * Obtiene el usuario actual autenticado y verificado
     */
    private Usuario obtenerUsuarioAutenticado() {
        UsuarioDTO usuarioAutenticadoDTO = usuarioFacade.getUsuarioActual();
        if (usuarioAutenticadoDTO == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }
        
        String usuarioIdTexto = usuarioAutenticadoDTO.getId();
        if (usuarioIdTexto == null || usuarioIdTexto.isBlank()) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }

        UUID usuarioId = Objects.requireNonNull(UUID.fromString(usuarioIdTexto));
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }

    /**
     * Asegura que existe un historial clínico para el usuario dado
     */
    private HistorialClinico ensureForUsuario(Usuario usuario) {
        return historiaRepo.findByUsuario(usuario).orElseGet(() -> {
            HistorialClinico historial = new HistorialClinico();
            historial.setFechaCreacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));
            historial.setUsuario(usuario);
            return historiaRepo.save(historial);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialClinicoDTO obtenerHistoriaUsuarioActual() {
        UsuarioDTO usuarioActualDTO = usuarioFacade.getUsuarioActual();
        if (usuarioActualDTO == null) {
            return null;
        }
        
        String usuarioIdTexto = usuarioActualDTO.getId();
        if (usuarioIdTexto == null || usuarioIdTexto.isBlank()) {
            return null;
        }

        UUID usuarioId = Objects.requireNonNull(UUID.fromString(usuarioIdTexto));
        Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
        if (usuario.isEmpty()) {
            return null;
        }
        
        return historiaRepo.findByUsuario(usuario.get())
            .map(this::construirHistorialClinicoDTO)
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialClinicoDTO obtenerHistorialPaciente(String nifPaciente) {
        Usuario medico = obtenerUsuarioAutenticado();

        if (nifPaciente == null || nifPaciente.isBlank()) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO);
        }

        Usuario paciente = usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nifPaciente))
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));

        if (Usuario.ESTADO_CUENTA_SUSPENDIDO.equals(paciente.getEstadoCuenta())) {
            throw new IllegalStateException(ErrorMessages.ERROR_TRATAMIENTO_LIMITADO);
        }

        boolean autorizado = medicoPacienteRepository.existsByMedicoIdAndPacienteIdAndEstado(
                medico.getId(), paciente.getId(), ESTADO_MEDICO_PACIENTE_ACTIVA);
        if (!autorizado) {
            throw new IllegalStateException(ErrorMessages.ERROR_ACCESO_DENEGADO);
        }

        return historiaRepo.findByUsuario(paciente)
                .map(this::construirHistorialClinicoDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO crearAntecedente(AntecedenteClinicoDTO antecedenteDTO) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);

        AntecedenteClinico antecedente = antecedenteClinicoConverter.toEntity(antecedenteDTO, historial);
        antecedente.setFechaCreacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));
        AntecedenteClinico guardado = antecedenteClinicoRepository.save(antecedente);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            ANTECEDENTE_CLINICO,
            ANTECEDENTE_CLINICO_TABLA,
            guardado.getId().toString(),
            "",
            guardado.getDescripcion(),
            AuditoriaCambio.TipoOperacion.CREATE,
            "Creación de antecedente clínico"
        );

        return construirHistorialClinicoDTO(historial);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO editarAntecedente(UUID id, AntecedenteClinicoDTO antecedenteDTO) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = obtenerHistorialUsuario(usuario);

        AntecedenteClinico antecedente = obtenerAntecedente(id);
        validarPropiedadAntecedente(antecedente, historial);

        String valorAnterior = antecedente.getDescripcion();
        antecedente.setCategoria(antecedenteClinicoConverter.parseCategoria(antecedenteDTO.getCategoria()));
        antecedente.setDescripcion(antecedenteDTO.getDescripcion());
        antecedenteClinicoRepository.save(antecedente);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            ANTECEDENTE_CLINICO,
            ANTECEDENTE_CLINICO_TABLA,
            id.toString(),
            valorAnterior,
            antecedente.getDescripcion(),
            AuditoriaCambio.TipoOperacion.UPDATE,
            "Edición de antecedente clínico"
        );

        return construirHistorialClinicoDTO(historial);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO borrarAntecedente(UUID id) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = obtenerHistorialUsuario(usuario);

        AntecedenteClinico antecedente = obtenerAntecedente(id);
        validarPropiedadAntecedente(antecedente, historial);

        String valorAnterior = antecedente.getDescripcion();
        antecedenteClinicoRepository.delete(antecedente);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            ANTECEDENTE_CLINICO,
            ANTECEDENTE_CLINICO_TABLA,
            id.toString(),
            valorAnterior,
            "",
            AuditoriaCambio.TipoOperacion.DELETE,
            "Eliminación de antecedente clínico"
        );

        return construirHistorialClinicoDTO(historial);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO crearAlergia(AlergiaDTO alergiaDTO) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);

        Alergia alergia = alergiaConverter.toEntity(alergiaDTO, historial);
        alergia.setFechaCreacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));
        Alergia guardada = alergiaRepository.save(alergia);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            ALERGIA,
            ALERGIA_TABLA,
            guardada.getId().toString(),
            "",
            guardada.getDescripcion(),
            AuditoriaCambio.TipoOperacion.CREATE,
            "Creación de alergia"
        );

        return construirHistorialClinicoDTO(historial);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO borrarAlergia(UUID id) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = obtenerHistorialUsuario(usuario);

        Alergia alergia = obtenerAlergia(id);
        validarPropiedadAlergia(alergia, historial);

        String valorAnterior = alergia.getDescripcion();
        alergiaRepository.delete(alergia);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            ALERGIA,
            ALERGIA_TABLA,
            id.toString(),
            valorAnterior,
            "",
            AuditoriaCambio.TipoOperacion.DELETE,
            "Eliminación de alergia"
        );

        return construirHistorialClinicoDTO(historial);
    }

    /**
     * Obtiene un antecedente clínico por ID o lanza excepción si no existe
     */
    private AntecedenteClinico obtenerAntecedente(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_ID_DATO_INVALIDO);
        }
        return antecedenteClinicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_ANTECEDENTE_NO_ENCONTRADO));
    }

    /**
     * Valida que el antecedente pertenezca al historial del usuario
     */
    private void validarPropiedadAntecedente(AntecedenteClinico antecedente, HistorialClinico historial) {
        if (antecedente.getHistorialClinico() == null
                || !antecedente.getHistorialClinico().getId().equals(historial.getId())) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_NO_PERMITIDO);
        }
    }

    /**
     * Obtiene una alergia por ID o lanza excepción si no existe
     */
    private Alergia obtenerAlergia(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_ID_DATO_INVALIDO);
        }
        return alergiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_ALERGIA_NO_ENCONTRADA));
    }

    /**
     * Valida que la alergia pertenezca al historial del usuario
     */
    private void validarPropiedadAlergia(Alergia alergia, HistorialClinico historial) {
        if (alergia.getHistorialClinico() == null
                || !alergia.getHistorialClinico().getId().equals(historial.getId())) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_NO_PERMITIDO);
        }
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarAnalisisSangre(String analisisJson) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);
        
        if (historial == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_HISTORIAL_NO_EXISTE);
        }
        
        procesarAnalisisSangre(analisisJson, historial, true);

        historiaRepo.save(historial);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            TIPO_CAMBIO_ANALISIS_SANGRE,
            DATO_CLINICO,
            historial.getId().toString(),
            "",
            analisisJson,
            AuditoriaCambio.TipoOperacion.UPDATE,
            "Reemplazo completo de análisis de sangre"
        );

        return historialClinicoConverter.toDto(historial);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO añadirAnalisisSangre(String analisisJson) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);

        if (historial == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_HISTORIAL_NO_EXISTE);
        }

        procesarAnalisisSangre(analisisJson, historial, false);

        historiaRepo.save(historial);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            TIPO_CAMBIO_ANALISIS_SANGRE,
            DATO_CLINICO,
            historial.getId().toString(),
            "",
            analisisJson,
            AuditoriaCambio.TipoOperacion.CREATE,
            "Adición de nuevos análisis de sangre"
        );

        return historialClinicoConverter.toDto(historial);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarSignosVitales(String signosVitalesJson) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);

        if (historial == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_HISTORIAL_NO_EXISTE);
        }

        procesarSignosVitales(signosVitalesJson, historial, true);

        historiaRepo.save(historial);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            TIPO_CAMBIO_SIGNOS_VITALES,
            DATO_CLINICO,
            historial.getId().toString(),
            "",
            signosVitalesJson,
            AuditoriaCambio.TipoOperacion.UPDATE,
            "Reemplazo completo de signos vitales"
        );

        return historialClinicoConverter.toDto(historial);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO añadirSignosVitales(String signosVitalesJson) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);

        if (historial == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_HISTORIAL_NO_EXISTE);
        }

        procesarSignosVitales(signosVitalesJson, historial, false);

        historiaRepo.save(historial);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            TIPO_CAMBIO_SIGNOS_VITALES,
            DATO_CLINICO,
            historial.getId().toString(),
            "",
            signosVitalesJson,
            AuditoriaCambio.TipoOperacion.CREATE,
            "Adición de nuevos signos vitales"
        );

        return historialClinicoConverter.toDto(historial);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarAnalisisOrina(String analisisOrinaJson) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);

        if (historial == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_HISTORIAL_NO_EXISTE);
        }

        procesarAnalisisOrina(analisisOrinaJson, historial, true);

        historiaRepo.save(historial);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            TIPO_CAMBIO_ANALISIS_ORINA,
            DATO_CLINICO,
            historial.getId().toString(),
            "",
            analisisOrinaJson,
            AuditoriaCambio.TipoOperacion.UPDATE,
            "Reemplazo completo de análisis de orina"
        );

        return historialClinicoConverter.toDto(historial);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO añadirAnalisisOrina(String analisisOrinaJson) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);

        if (historial == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_HISTORIAL_NO_EXISTE);
        }

        procesarAnalisisOrina(analisisOrinaJson, historial, false);

        historiaRepo.save(historial);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            TIPO_CAMBIO_ANALISIS_ORINA,
            DATO_CLINICO,
            historial.getId().toString(),
            "",
            analisisOrinaJson,
            AuditoriaCambio.TipoOperacion.CREATE,
            "Adición de nuevos datos de análisis de orina"
        );

        return historialClinicoConverter.toDto(historial);
    }

    /**
     * Procesa los análisis de sangre del JSON y los almacena como datos clínicos.
     *
     * @param analisisJson JSON con los análisis de sangre
     * @param historial Historial clínico al que pertenecen
     * @param eliminarExistentes Si true, elimina todos los análisis existentes antes de guardar los nuevos (reemplazo completo).
     *                           Si false, solo añade los nuevos análisis sin eliminar los existentes.
     */
    // TODO Revisar esto, no tiene mucho sentido lo de eliminar todo para guardar una nueva
    private void procesarAnalisisSangre(String analisisJson, HistorialClinico historial, boolean eliminarExistentes) {
        procesarDatosClinicos(analisisJson, historial, eliminarExistentes, TiposDatoClinico.ANALISIS_SANGRE);
    }

    /**
     * Procesa los signos vitales del JSON y los almacena como datos clínicos.
     *
     * @param signosVitalesJson JSON con los signos vitales
     * @param historial Historial clínico al que pertenecen
     * @param eliminarExistentes Si true, elimina todos los signos vitales existentes antes de guardar los nuevos.
     *                           Si false, solo añade los nuevos sin eliminar los existentes.
     */
    private void procesarSignosVitales(String signosVitalesJson, HistorialClinico historial, boolean eliminarExistentes) {
        procesarDatosClinicos(signosVitalesJson, historial, eliminarExistentes, TiposDatoClinico.SIGNOS_VITALES);
    }

    /**
     * Procesa el análisis de orina del JSON y lo almacena como datos clínicos.
     *
     * @param analisisOrinaJson JSON con los datos de análisis de orina
     * @param historial Historial clínico al que pertenecen
     * @param eliminarExistentes Si true, elimina todos los datos de orina existentes antes de guardar los nuevos.
     *                           Si false, solo añade los nuevos sin eliminar los existentes.
     */
    private void procesarAnalisisOrina(String analisisOrinaJson, HistorialClinico historial, boolean eliminarExistentes) {
        procesarDatosClinicos(analisisOrinaJson, historial, eliminarExistentes, TiposDatoClinico.ANALISIS_ORINA);
    }

    /**
     * Procesa un JSON de datos clínicos cuantitativos (análisis de sangre, signos vitales
     * o análisis de orina) y los almacena, opcionalmente reemplazando los existentes del
     * mismo dominio.
     *
     * @param datosJson JSON con los datos clínicos a añadir
     * @param historial Historial clínico al que pertenecen
     * @param eliminarExistentes Si true, elimina los datos existentes cuyo tipo esté en {@code tiposConocidos}
     *                           antes de guardar los nuevos (reemplazo completo).
     *                           Si false, solo añade los nuevos sin eliminar los existentes.
     * @param tiposConocidos Tipos de dato clínico que delimitan el dominio (p. ej. {@link TiposDatoClinico#ANALISIS_SANGRE})
     */
    // TODO Revisar esto, no tiene mucho sentido lo de eliminar todo para guardar una nueva
    private void procesarDatosClinicos(String datosJson, HistorialClinico historial, boolean eliminarExistentes, List<String> tiposConocidos) {
        if (eliminarExistentes) {
            List<String> tiposConocidosHash = tiposConocidos.stream().map(hmacSearchIndexService::indexar).toList();
            List<DatoClinico> datosExistentes = datoClinicoRepository.findByHistorialClinicoAndTipoHashIn(historial, tiposConocidosHash);

            if (!datosExistentes.isEmpty()) {
                datoClinicoRepository.deleteAll(datosExistentes);
            }
        }

        // Procesar y guardar los nuevos datos si existen
        if (datosJson == null || datosJson.trim().isEmpty()) {
            return;
        }

        try {
            JsonNode nodoJson = objectMapper.readTree(datosJson);
            validarJsonAnalisis(nodoJson);

            List<DatoClinico> nuevosDatos = procesarElementosAnalisis(nodoJson, historial);

            if (!nuevosDatos.isEmpty()) {
                datoClinicoRepository.saveAll(nuevosDatos);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_JSON_INVALIDO, e);
        }
    }

    /**
     * Valida que el JSON de análisis tenga el formato correcto
     */
    private void validarJsonAnalisis(JsonNode nodoJson) {
        if (!nodoJson.isArray()) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_ANALISIS_JSON_ARRAY_ESPERADO);
        }
    }

    /**
     * Procesa cada elemento del array JSON de análisis
     */
    private List<DatoClinico> procesarElementosAnalisis(JsonNode nodoJson, HistorialClinico historial) {
        List<DatoClinico> nuevosAnalisis = new ArrayList<>();
        
        for (JsonNode item : nodoJson) {
            validarItemAnalisis(item);
            DatoClinico datoClinico = crearDatoClinicoAnalisis(item, historial);
            nuevosAnalisis.add(datoClinico);
        }
        
        return nuevosAnalisis;
    }

    /**
     * Valida que un item de análisis tenga los campos requeridos
     */
    private void validarItemAnalisis(JsonNode item) {
        if (!item.has("value")) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_ANALISIS_VALUE_REQUERIDO);
        }
    }

    /**
     * Crea un dato clínico a partir de un item de análisis JSON
     */
    private DatoClinico crearDatoClinicoAnalisis(JsonNode item, HistorialClinico historial) {
        String tipo = obtenerTipoAnalisis(item);
        String unidad = obtenerUnidadAnalisis(item);
        float valor = obtenerValorAnalisis(item);
        LocalDateTime fechaCreacion = obtenerFechaCreacionAnalisis(item);

        DatoClinico datoClinico = new DatoClinico();
        datoClinico.setTipo(tipo);
        datoClinico.setTipoHash(hmacSearchIndexService.indexar(tipo));
        datoClinico.setValor(String.valueOf(valor));
        datoClinico.setUnidad(unidad);
        datoClinico.setObservacion(null);
        datoClinico.setHistorialClinico(historial);
        datoClinico.setFechaCreacion(fechaCreacion);
        
        // Buscar y asignar el rango correspondiente para este tipo de dato clínico
        buscarYAsignarRango(datoClinico, tipo);
        
        return datoClinico;
    }

    /**
     * Busca y asigna el rango correspondiente a un dato clínico basado en su tipo
     * @param datoClinico El dato clínico al que asignar el rango
     * @param tipo El tipo/nombre del análisis para buscar el rango
     */
    private void buscarYAsignarRango(DatoClinico datoClinico, String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return; // No asignar rango si no hay tipo definido
        }
        
        // Intentar buscar el rango exacto primero
        var rangoOptional = rangoRepository.findByNombreIgnoreCase(tipo.trim());
        
        // Si no se encuentra exacto, buscar por coincidencia parcial
        if (rangoOptional.isEmpty()) {
            rangoOptional = rangoRepository.findByNombreContainingIgnoreCase(tipo.trim());
        }
        
        // Asignar el rango si se encontró uno
        rangoOptional.ifPresent(datoClinico::setRango);
    }

    /**
     * Obtiene el tipo de análisis del JSON
     */
    private String obtenerTipoAnalisis(JsonNode item) {
        if (item.has("label")) {
            return item.get("label").asText();
        } else if (item.has("key")) {
            return item.get("key").asText();
        } else {
            return TIPO_ANALISIS_DEFAULT;
        }
    }

    /**
     * Obtiene la unidad del análisis del JSON
     */
    private String obtenerUnidadAnalisis(JsonNode item) {
        String unidad = item.has("unit") ? item.get("unit").asText() : "";
        return unidad != null ? unidad : "";
    }

    /**
     * Obtiene y valida el valor numérico del análisis
     */
    private float obtenerValorAnalisis(JsonNode item) {
        String valorTexto = item.get("value").asText();
        try {
            return Float.parseFloat(valorTexto.replace(',', '.'));
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_VALOR_NUMERICO_INVALIDO + ": " + valorTexto);
        }
    }

    /**
     * Obtiene la fecha de creación del análisis, o la actual si no se proporciona
     */
    private LocalDateTime obtenerFechaCreacionAnalisis(JsonNode item) {
        if (!item.has(CREATED_AT) || item.get(CREATED_AT).isNull()) {
            return LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID));
        }

        String fechaTexto = item.get(CREATED_AT).asText();
        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(fechaTexto);
            return offsetDateTime.toLocalDateTime();
        } catch (Exception _) {
            try {
                return LocalDateTime.parse(fechaTexto);
            } catch (Exception _) {
                return LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID));
            }
        }
    }

    @Override
    @Transactional
    public void borrarDatoClinico(UUID id) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = obtenerHistorialUsuario(usuario);
        
        DatoClinico datoClinico = obtenerDatoClinico(id);
        if (datoClinico == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_DATO_NO_ENCONTRADO);
        }
        validarPropiedadDatoClinico(datoClinico, historial);
        
        String valorAnterior = datoClinico.getTipo() + ": " + datoClinico.getValor() + " " + datoClinico.getUnidad();
        
        datoClinicoRepository.delete(datoClinico);
        
        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            datoClinico.getTipo(),
            DATO_CLINICO,
            id.toString(),
            valorAnterior,
            "",
            AuditoriaCambio.TipoOperacion.DELETE,
            "Eliminación de dato clínico"
        );
    }

    /**
     * Obtiene el historial clínico del usuario o lanza excepción si no existe
     */
    private HistorialClinico obtenerHistorialUsuario(Usuario usuario) {
        return historiaRepo.findByUsuario(usuario)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_HISTORIAL_NO_EXISTE));
    }

    /**
     * Obtiene un dato clínico por ID o lanza excepción si no existe
     */
    private DatoClinico obtenerDatoClinico(UUID id) {
        if(id == null) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_ID_DATO_INVALIDO);
        }
        return datoClinicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_DATO_NO_ENCONTRADO));
    }

    /**
     * Valida que el dato clínico pertenezca al historial del usuario
     */
    private void validarPropiedadDatoClinico(DatoClinico datoClinico, HistorialClinico historial) {
        if (datoClinico.getHistorialClinico() == null || 
            !datoClinico.getHistorialClinico().getId().equals(historial.getId())) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_NO_PERMITIDO);
        }
    }

}
