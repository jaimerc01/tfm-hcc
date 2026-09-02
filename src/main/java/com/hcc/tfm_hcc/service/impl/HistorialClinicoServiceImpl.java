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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.constants.TiposDatoClinico;
import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoEntradaDTO;
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
import com.hcc.tfm_hcc.model.MedicoPaciente;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HistorialClinicoServiceImpl implements HistorialClinicoService {

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
            HmacSearchIndexService hmacSearchIndexService,
            ObjectMapper objectMapper) {
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
        this.objectMapper = objectMapper;
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
                medico.getId(), paciente.getId(), MedicoPaciente.ESTADO_ACTIVA);
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
    public HistorialClinicoDTO actualizarAnalisisSangre(List<DatoClinicoEntradaDTO> analisis) {
        return guardarDatosClinicos(analisis, TiposDatoClinico.ANALISIS_SANGRE, true,
                TIPO_CAMBIO_ANALISIS_SANGRE, AuditoriaCambio.TipoOperacion.UPDATE,
                "Reemplazo completo de análisis de sangre");
    }

    @Override
    @Transactional
    public HistorialClinicoDTO añadirAnalisisSangre(List<DatoClinicoEntradaDTO> analisis) {
        return guardarDatosClinicos(analisis, TiposDatoClinico.ANALISIS_SANGRE, false,
                TIPO_CAMBIO_ANALISIS_SANGRE, AuditoriaCambio.TipoOperacion.CREATE,
                "Adición de nuevos análisis de sangre");
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarSignosVitales(List<DatoClinicoEntradaDTO> signosVitales) {
        return guardarDatosClinicos(signosVitales, TiposDatoClinico.SIGNOS_VITALES, true,
                TIPO_CAMBIO_SIGNOS_VITALES, AuditoriaCambio.TipoOperacion.UPDATE,
                "Reemplazo completo de signos vitales");
    }

    @Override
    @Transactional
    public HistorialClinicoDTO añadirSignosVitales(List<DatoClinicoEntradaDTO> signosVitales) {
        return guardarDatosClinicos(signosVitales, TiposDatoClinico.SIGNOS_VITALES, false,
                TIPO_CAMBIO_SIGNOS_VITALES, AuditoriaCambio.TipoOperacion.CREATE,
                "Adición de nuevos signos vitales");
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarAnalisisOrina(List<DatoClinicoEntradaDTO> analisisOrina) {
        return guardarDatosClinicos(analisisOrina, TiposDatoClinico.ANALISIS_ORINA, true,
                TIPO_CAMBIO_ANALISIS_ORINA, AuditoriaCambio.TipoOperacion.UPDATE,
                "Reemplazo completo de análisis de orina");
    }

    @Override
    @Transactional
    public HistorialClinicoDTO añadirAnalisisOrina(List<DatoClinicoEntradaDTO> analisisOrina) {
        return guardarDatosClinicos(analisisOrina, TiposDatoClinico.ANALISIS_ORINA, false,
                TIPO_CAMBIO_ANALISIS_ORINA, AuditoriaCambio.TipoOperacion.CREATE,
                "Adición de nuevos datos de análisis de orina");
    }

    /**
     * Flujo común de los seis endpoints de datos clínicos cuantitativos (análisis de
     * sangre, signos vitales, análisis de orina; en variante "reemplazar todo" o "añadir"):
     * asegura el historial del usuario autenticado, persiste las mediciones y registra el
     * cambio en la auditoría.
     *
     * @param entradas             mediciones recibidas del cliente (ya deserializadas por Jackson)
     * @param tiposDominio         tipos que delimitan el dominio afectado (p. ej. {@link TiposDatoClinico#ANALISIS_SANGRE})
     * @param eliminarExistentes   {@code true} para reemplazar todo el dominio, {@code false} para añadir
     * @param tipoCambioAuditoria  etiqueta de auditoría (p. ej. {@code ANALISIS_SANGRE})
     * @param operacion            operación de auditoría (UPDATE al reemplazar, CREATE al añadir)
     * @param razonCambio          descripción legible del cambio para la auditoría
     */
    private HistorialClinicoDTO guardarDatosClinicos(List<DatoClinicoEntradaDTO> entradas,
            List<String> tiposDominio, boolean eliminarExistentes, String tipoCambioAuditoria,
            AuditoriaCambio.TipoOperacion operacion, String razonCambio) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);

        procesarDatosClinicos(entradas, historial, eliminarExistentes, tiposDominio);

        historiaRepo.save(historial);

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            tipoCambioAuditoria,
            DATO_CLINICO,
            historial.getId().toString(),
            "",
            serializarParaAuditoria(entradas),
            operacion,
            razonCambio
        );

        return historialClinicoConverter.toDto(historial);
    }

    /**
     * Almacena una tanda de mediciones cuantitativas, opcionalmente reemplazando primero
     * todas las existentes cuyo tipo pertenezca al dominio indicado.
     *
     * @param entradas Mediciones a añadir
     * @param historial Historial clínico al que pertenecen
     * @param eliminarExistentes Si true, elimina los datos existentes cuyo tipo esté en {@code tiposConocidos}
     *                           antes de guardar los nuevos (reemplazo completo).
     * @param tiposConocidos Tipos de dato clínico que delimitan el dominio (p. ej. {@link TiposDatoClinico#ANALISIS_SANGRE})
     */
    private void procesarDatosClinicos(List<DatoClinicoEntradaDTO> entradas, HistorialClinico historial,
                                       boolean eliminarExistentes, List<String> tiposConocidos) {
        if (eliminarExistentes) {
            List<String> tiposConocidosHash = tiposConocidos.stream().map(hmacSearchIndexService::indexar).toList();
            List<DatoClinico> datosExistentes = datoClinicoRepository.findByHistorialClinicoAndTipoHashIn(historial, tiposConocidosHash);

            if (!datosExistentes.isEmpty()) {
                datoClinicoRepository.deleteAll(datosExistentes);
            }
        }

        if (entradas == null || entradas.isEmpty()) {
            return;
        }

        List<DatoClinico> nuevosDatos = new ArrayList<>();
        for (DatoClinicoEntradaDTO entrada : entradas) {
            validarEntradaAnalisis(entrada);
            nuevosDatos.add(crearDatoClinicoAnalisis(entrada, historial));
        }

        if (!nuevosDatos.isEmpty()) {
            datoClinicoRepository.saveAll(nuevosDatos);
        }
    }

    /**
     * Serializa las mediciones a JSON para dejarlas registradas en la auditoría de cambios.
     * Si la serialización fallara (no debería, son POJOs planos), cae a la representación
     * por defecto en vez de abortar la operación clínica.
     */
    private String serializarParaAuditoria(List<DatoClinicoEntradaDTO> entradas) {
        try {
            return objectMapper.writeValueAsString(entradas);
        } catch (JsonProcessingException e) {
            log.warn("No se pudo serializar la entrada de datos clínicos para auditoría: {}", e.getMessage());
            return String.valueOf(entradas);
        }
    }

    /**
     * Valida que una medición traiga el valor obligatorio.
     */
    private void validarEntradaAnalisis(DatoClinicoEntradaDTO entrada) {
        if (entrada == null || entrada.getValue() == null || entrada.getValue().isBlank()) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_ANALISIS_VALUE_REQUERIDO);
        }
    }

    /**
     * Crea un dato clínico a partir de una medición recibida del cliente.
     */
    private DatoClinico crearDatoClinicoAnalisis(DatoClinicoEntradaDTO item, HistorialClinico historial) {
        String tipo = obtenerTipoAnalisis(item);
        String unidad = obtenerUnidadAnalisis(item);
        String valor = obtenerValorAnalisis(item);
        LocalDateTime fechaCreacion = obtenerFechaCreacionAnalisis(item);

        DatoClinico datoClinico = new DatoClinico();
        datoClinico.setTipo(tipo);
        datoClinico.setTipoHash(hmacSearchIndexService.indexar(tipo));
        datoClinico.setValor(valor);
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
     * Nombre del parámetro: {@code label} si viene, si no {@code key}, si no un tipo genérico.
     */
    private String obtenerTipoAnalisis(DatoClinicoEntradaDTO item) {
        if (item.getLabel() != null && !item.getLabel().isBlank()) {
            return item.getLabel();
        }
        if (item.getKey() != null && !item.getKey().isBlank()) {
            return item.getKey();
        }
        return TIPO_ANALISIS_DEFAULT;
    }

    /**
     * Unidad de medida, o cadena vacía si no se indica.
     */
    private String obtenerUnidadAnalisis(DatoClinicoEntradaDTO item) {
        return item.getUnit() != null ? item.getUnit() : "";
    }

    /**
     * Valida que el valor de la medición sea numérico y lo devuelve normalizado (coma
     * decimal a punto, sin espacios). Se conserva como texto para no perder precisión:
     * convertir a {@code float} y de vuelta a {@code String} redondeaba valores como
     * {@code "5.1"} a {@code "5.0999999"}.
     */
    private String obtenerValorAnalisis(DatoClinicoEntradaDTO item) {
        String valorNormalizado = item.getValue().trim().replace(',', '.');
        try {
            Double.parseDouble(valorNormalizado);
            return valorNormalizado;
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_VALOR_NUMERICO_INVALIDO + ": " + item.getValue());
        }
    }

    /**
     * Obtiene la fecha real de la medición, o la actual si no se proporciona.
     */
    private LocalDateTime obtenerFechaCreacionAnalisis(DatoClinicoEntradaDTO item) {
        String fechaTexto = item.getCreatedAt();
        if (fechaTexto == null || fechaTexto.isBlank()) {
            return LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID));
        }

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
