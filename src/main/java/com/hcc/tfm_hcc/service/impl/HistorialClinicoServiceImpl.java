package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.dto.DatoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.DatoClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.DatoClinicoRepository;
import com.hcc.tfm_hcc.repository.HistorialClinicoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.HistorialClinicoService;

@Service
public class HistorialClinicoServiceImpl implements HistorialClinicoService {

    private static final String TIPO_ALERGIA_INTOLERANCIA = "ALERGIA/INTOLERANCIA";
    private static final String UNIDAD_TEXTO = "text";
    private static final String TIPO_ANALISIS_DEFAULT = "ANALISIS";
    private static final String SEPARADOR_ANTECEDENTES = "\n\n";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final HistorialClinicoRepository historiaRepo;
    private final UsuarioFacade usuarioFacade;
    private final UsuarioRepository usuarioRepository;
    private final DatoClinicoRepository datoClinicoRepository;
    private final ObjectMapper objectMapper;

    public HistorialClinicoServiceImpl(
            HistorialClinicoRepository historiaRepo,
            UsuarioFacade usuarioFacade,
            UsuarioRepository usuarioRepository,
            DatoClinicoRepository datoClinicoRepository) {
        this.historiaRepo = historiaRepo;
        this.usuarioFacade = usuarioFacade;
        this.usuarioRepository = usuarioRepository;
        this.datoClinicoRepository = datoClinicoRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Obtiene el usuario actual autenticado y verificado
     */
    private Usuario obtenerUsuarioAutenticado() {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }
        
        return usuarioRepository.findById(UUID.fromString(dto.getId()))
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }

    /**
     * Asegura que existe un historial clínico para el usuario dado
     */
    private HistorialClinico ensureForUsuario(Usuario usuario) {
        return historiaRepo.findByUsuario(usuario).orElseGet(() -> {
            HistorialClinico historial = new HistorialClinico();
            historial.setFechaCreacion(LocalDateTime.now());
            historial.setUsuario(usuario);
            return historiaRepo.save(historial);
        });
    }

    /**
     * Convierte una entidad HistorialClinico a DTO
     */
    private HistorialClinicoDTO toDto(HistorialClinico historial) {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        dto.setAntecedentesFamiliares(historial.getAntecedentesFamiliares());
        
        var datosClinicos = datoClinicoRepository.findByHistorialClinico(historial);
        if (datosClinicos != null && !datosClinicos.isEmpty()) {
            var alergias = new ArrayList<DatoClinicoDTO>();
            var analisis = new ArrayList<DatoClinicoDTO>();
            
            for (var datoClinico : datosClinicos) {
                DatoClinicoDTO datoDto = convertirDatoClinicoADto(datoClinico);
                
                if (TIPO_ALERGIA_INTOLERANCIA.equals(datoClinico.getTipo())) {
                    alergias.add(datoDto);
                } else {
                    analisis.add(datoDto);
                }
            }
            
            dto.setDatosClinicos(alergias);
            dto.setAnalisisSangre(analisis);
        }
        
        return dto;
    }

    /**
     * Convierte una entidad DatoClinico a DTO
     */
    private DatoClinicoDTO convertirDatoClinicoADto(DatoClinico datoClinico) {
        DatoClinicoDTO dto = new DatoClinicoDTO();
        dto.setId(datoClinico.getId() != null ? datoClinico.getId().toString() : null);
        dto.setTipo(datoClinico.getTipo());
        dto.setValor(String.valueOf(datoClinico.getValor()));
        dto.setUnidad(datoClinico.getUnidad());
        dto.setObservacion(datoClinico.getObservacion());
        dto.setCreatedAt(datoClinico.getFechaCreacion() != null ? 
            datoClinico.getFechaCreacion().toString() : null);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialClinicoDTO obtenerHistoriaUsuarioActual() {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) {
            return null;
        }
        
        var usuario = usuarioRepository.findById(UUID.fromString(dto.getId()));
        if (usuario.isEmpty()) {
            return null;
        }
        
        return historiaRepo.findByUsuario(usuario.get())
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarIdentificacion(String identificacionJson) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);
        
        // Identificación persistida en otra entidad; no modificar aquí por restricciones del modelo
        historiaRepo.save(historial);
        return toDto(historial);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarAntecedentes(String antecedentesFamiliares) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);
        
        actualizarAntecedentesFamiliares(historial, antecedentesFamiliares);
        
        historiaRepo.save(historial);
        return toDto(historial);
    }

    /**
     * Actualiza los antecedentes familiares del historial
     */
    private void actualizarAntecedentesFamiliares(HistorialClinico historial, String antecedentes) {
        if (antecedentes == null || antecedentes.trim().isEmpty()) {
            historial.setAntecedentesFamiliares(null);
        } else {
            historial.setAntecedentesFamiliares(antecedentes.trim());
        }
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarAlergias(String alergiasJson) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);
        
        procesarAlergias(alergiasJson, historial);
        
        historiaRepo.save(historial);
        return toDto(historial);
    }

    /**
     * Procesa las alergias del JSON y las almacena como datos clínicos
     */
    private void procesarAlergias(String alergiasJson, HistorialClinico historial) {
        if (alergiasJson == null || alergiasJson.trim().isEmpty()) {
            return;
        }

        List<DatoClinico> nuevasAlergias = Arrays.stream(alergiasJson.split("\\r?\\n"))
                .map(String::trim)
                .filter(texto -> !texto.isEmpty())
                .map(texto -> crearDatoClinicoAlergia(texto, historial))
                .collect(Collectors.toList());

        if (!nuevasAlergias.isEmpty()) {
            datoClinicoRepository.saveAll(nuevasAlergias);
        }
    }

    /**
     * Crea un dato clínico para una alergia
     */
    private DatoClinico crearDatoClinicoAlergia(String textoAlergia, HistorialClinico historial) {
        DatoClinico datoClinico = new DatoClinico();
        datoClinico.setTipo(TIPO_ALERGIA_INTOLERANCIA);
        datoClinico.setValor(0.0f);
        datoClinico.setUnidad(UNIDAD_TEXTO);
        datoClinico.setObservacion(textoAlergia);
        datoClinico.setHistorialClinico(historial);
        datoClinico.setFechaCreacion(LocalDateTime.now());
        return datoClinico;
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarAnalisisSangre(String analisisJson) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);
        
        procesarAnalisisSangre(analisisJson, historial);
        
        historiaRepo.save(historial);
        return toDto(historial);
    }

    /**
     * Procesa los análisis de sangre del JSON y los almacena como datos clínicos
     */
    private void procesarAnalisisSangre(String analisisJson, HistorialClinico historial) {
        if (analisisJson == null || analisisJson.trim().isEmpty()) {
            return;
        }

        try {
            JsonNode nodoJson = objectMapper.readTree(analisisJson);
            validarJsonAnalisis(nodoJson);
            
            List<DatoClinico> nuevosAnalisis = procesarElementosAnalisis(nodoJson, historial);
            
            if (!nuevosAnalisis.isEmpty()) {
                datoClinicoRepository.saveAll(nuevosAnalisis);
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
        datoClinico.setValor(valor);
        datoClinico.setUnidad(unidad);
        datoClinico.setObservacion(null);
        datoClinico.setHistorialClinico(historial);
        datoClinico.setFechaCreacion(fechaCreacion);
        
        return datoClinico;
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
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_VALOR_NUMERICO_INVALIDO + ": " + valorTexto);
        }
    }

    /**
     * Obtiene la fecha de creación del análisis, o la actual si no se proporciona
     */
    private LocalDateTime obtenerFechaCreacionAnalisis(JsonNode item) {
        if (!item.has("createdAt") || item.get("createdAt").isNull()) {
            return LocalDateTime.now();
        }

        String fechaTexto = item.get("createdAt").asText();
        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(fechaTexto);
            return offsetDateTime.toLocalDateTime();
        } catch (Exception ex) {
            try {
                return LocalDateTime.parse(fechaTexto);
            } catch (Exception ex2) {
                return LocalDateTime.now();
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
        
        datoClinicoRepository.delete(datoClinico);
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

    @Override
    @Transactional
    public HistorialClinicoDTO borrarAntecedente(int index) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);
        
        List<String> antecedentes = obtenerListaAntecedentes(historial);
        validarIndiceAntecedente(index, antecedentes);
        
        antecedentes.remove(index);
        actualizarAntecedentesFamiliares(historial, String.join(SEPARADOR_ANTECEDENTES, antecedentes));
        
        historiaRepo.save(historial);
        return toDto(historial);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO editarAntecedente(int index, String texto) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HistorialClinico historial = ensureForUsuario(usuario);
        
        List<String> antecedentes = obtenerListaAntecedentes(historial);
        validarIndiceAntecedente(index, antecedentes);
        
        String entradaConTimestamp = crearEntradaConTimestamp(texto);
        antecedentes.set(index, entradaConTimestamp);
        actualizarAntecedentesFamiliares(historial, String.join(SEPARADOR_ANTECEDENTES, antecedentes));
        
        historiaRepo.save(historial);
        return toDto(historial);
    }

    /**
     * Obtiene la lista de antecedentes familiares como lista de strings
     */
    private List<String> obtenerListaAntecedentes(HistorialClinico historial) {
        String antecedentesTexto = historial.getAntecedentesFamiliares();
        if (antecedentesTexto == null || antecedentesTexto.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(antecedentesTexto.split("\r?\n\s*\r?\n"))
                .filter(parte -> parte != null && !parte.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Valida que el índice esté dentro del rango válido
     */
    private void validarIndiceAntecedente(int index, List<String> antecedentes) {
        if (antecedentes.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_NO_HAY_ANTECEDENTES);
        }
        if (index < 0 || index >= antecedentes.size()) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_INDICE_FUERA_RANGO);
        }
    }

    /**
     * Crea una entrada de antecedente con timestamp
     */
    private String crearEntradaConTimestamp(String texto) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String textoLimpio = texto != null ? texto.trim() : "";
        return "[" + timestamp + "] " + textoLimpio;
    }
}
