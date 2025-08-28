package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private HistorialClinicoRepository historiaRepo;

    @Autowired
    private UsuarioFacade usuarioFacade;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DatoClinicoRepository datoClinicoRepository;

    private HistorialClinico ensureForUsuario(Usuario u) {
        return historiaRepo.findByUsuario(u).orElseGet(() -> {
            HistorialClinico h = new HistorialClinico();
            h.setFechaCreacion(LocalDateTime.now());
            h.setUsuario(u);
            return historiaRepo.save(h);
        });
    }

    private HistorialClinicoDTO toDto(HistorialClinico h) {
        HistorialClinicoDTO d = new HistorialClinicoDTO();
        d.setAntecedentesFamiliares(h.getAntecedentesFamiliares());
        // include clinical data (e.g., allergy records) so frontend can display them
        var datos = datoClinicoRepository.findByHistorialClinico(h);
        if (datos != null && !datos.isEmpty()) {
            var alergiasList = new ArrayList<DatoClinicoDTO>();
            var analisisList = new ArrayList<DatoClinicoDTO>();
            for (var x : datos) {
                DatoClinicoDTO xd = new DatoClinicoDTO();
                xd.setId(x.getId() == null ? null : x.getId().toString());
                xd.setTipo(x.getTipo());
                xd.setValor(String.valueOf(x.getValor()));
                xd.setUnidad(x.getUnidad());
                xd.setObservacion(x.getObservacion());
                xd.setCreatedAt(x.getFechaCreacion() == null ? null : x.getFechaCreacion().toString());
                if ("ALERGIA/INTOLERANCIA".equals(x.getTipo())) {
                    alergiasList.add(xd);
                } else {
                    analisisList.add(xd);
                }
            }
            d.setDatosClinicos(alergiasList);
            d.setAnalisisSangre(analisisList);
        }
        return d;
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialClinicoDTO obtenerHistoriaUsuarioActual() {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return null;
        var opt = usuarioRepository.findById(UUID.fromString(dto.getId()));
        if (opt.isEmpty()) return null;
        var u = opt.get();
        return historiaRepo.findByUsuario(u).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarIdentificacion(String identificacionJson) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) throw new IllegalStateException("No autenticado");
        var u = usuarioRepository.findById(UUID.fromString(dto.getId())).orElseThrow();
        var h = ensureForUsuario(u);
    // Identificación persistida en otra entidad; no modificar here per model constraints
        historiaRepo.save(h);
        return toDto(h);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarAntecedentes(String antecedentesFamiliares) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) throw new IllegalStateException("No autenticado");
        var u = usuarioRepository.findById(UUID.fromString(dto.getId())).orElseThrow();
        var h = ensureForUsuario(u);
        // Overwrite stored antecedentes with the incoming text (allow clearing)
        if (antecedentesFamiliares == null || antecedentesFamiliares.trim().isEmpty()) {
            h.setAntecedentesFamiliares(null);
        } else {
            h.setAntecedentesFamiliares(antecedentesFamiliares.trim());
        }
        historiaRepo.save(h);
        return toDto(h);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarAlergias(String alergiasJson) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) throw new IllegalStateException("No autenticado");
        var u = usuarioRepository.findById(UUID.fromString(dto.getId())).orElseThrow();
        var h = ensureForUsuario(u);
        // Store each non-empty line as a DatoClinico of tipo ALERGIA or INTOLERANCIA
    // Do not delete previous entries: append new allergy/intolerance records so historical data is preserved

        if (alergiasJson != null && !alergiasJson.trim().isEmpty()) {
            String[] lines = alergiasJson.split("\\r?\\n");
            List<DatoClinico> nuevos = new ArrayList<>();
            for (String ln : lines) {
                String text = ln == null ? "" : ln.trim();
                if (text.isEmpty()) continue;
                // Store all allergy/intolerance items with the unified tipo string
                DatoClinico d = new DatoClinico();
                d.setTipo("ALERGIA/INTOLERANCIA");
                // valor and unidad are required in the model: store textual info in observacion
                d.setValor(0.0f);
                d.setUnidad("text");
                d.setObservacion(text);
                d.setHistorialClinico(h);
                d.setFechaCreacion(LocalDateTime.now());
                nuevos.add(d);
            }
            if (!nuevos.isEmpty()) {
                datoClinicoRepository.saveAll(nuevos);
            }
        }
        // persist historial (if needed) and return DTO
        historiaRepo.save(h);
        return toDto(h);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarAnalisisSangre(String analisisJson) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) throw new IllegalStateException("No autenticado");
        var u = usuarioRepository.findById(UUID.fromString(dto.getId())).orElseThrow();
        var h = ensureForUsuario(u);
        // analisisJson expected to be a JSON array of { key,label,value,unit,createdAt }
        if (analisisJson != null && !analisisJson.trim().isEmpty()) {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            final com.fasterxml.jackson.databind.JsonNode node;
            try {
                node = mapper.readTree(analisisJson);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new IllegalArgumentException("JSON inválido para analisis-sangre");
            }
            if (!node.isArray()) throw new IllegalArgumentException("Se esperaba un array JSON para analisis-sangre");
            List<DatoClinico> nuevos = new ArrayList<>();
            for (var item : node) {
                if (!item.has("value")) throw new IllegalArgumentException("Cada entrada debe incluir 'value'");
                String tipo = item.has("label") ? item.get("label").asText() : (item.has("key") ? item.get("key").asText() : "ANALISIS");
                String unidad = item.has("unit") ? item.get("unit").asText() : "";
                String valorTxt = item.get("value").asText();
                float valor;
                try { valor = Float.parseFloat(valorTxt.replace(',', '.')); } catch (Exception ex) { throw new IllegalArgumentException("Valor numérico inválido: " + valorTxt); }
                DatoClinico d = new DatoClinico();
                d.setTipo(tipo);
                d.setValor(valor);
                d.setUnidad(unidad == null ? "" : unidad);
                d.setObservacion(null);
                d.setHistorialClinico(h);
                // Respect 'createdAt' from payload if present and parseable, otherwise use now
                if (item.has("createdAt") && !item.get("createdAt").isNull()) {
                    String ca = item.get("createdAt").asText();
                    try {
                        // try parsing as OffsetDateTime then LocalDateTime
                        OffsetDateTime odt = OffsetDateTime.parse(ca);
                        d.setFechaCreacion(odt.toLocalDateTime());
                    } catch (Exception ex) {
                        try {
                            LocalDateTime ldt = LocalDateTime.parse(ca);
                            d.setFechaCreacion(ldt);
                        } catch (Exception ex2) {
                            d.setFechaCreacion(LocalDateTime.now());
                        }
                    }
                } else {
                    d.setFechaCreacion(LocalDateTime.now());
                }
                nuevos.add(d);
            }
            if (!nuevos.isEmpty()) {
                datoClinicoRepository.saveAll(nuevos);
            }
        }
        historiaRepo.save(h);
        return toDto(h);
    }

    @Override
    @Transactional
    public void borrarDatoClinico(java.util.UUID id) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) throw new IllegalStateException("No autenticado");
        var optU = usuarioRepository.findById(UUID.fromString(dto.getId()));
        if (optU.isEmpty()) throw new IllegalArgumentException("Usuario no encontrado");
        var u = optU.get();
        var h = historiaRepo.findByUsuario(u).orElseThrow(() -> new IllegalArgumentException("No existe historial"));
        var opt = datoClinicoRepository.findById(id);
        if (opt.isEmpty()) throw new IllegalArgumentException("Dato clínico no encontrado");
        var d = opt.get();
        if (d.getHistorialClinico() == null || !d.getHistorialClinico().getId().equals(h.getId())) {
            throw new IllegalArgumentException("No permitido");
        }
        datoClinicoRepository.delete(d);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO borrarAntecedente(int index) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) throw new IllegalStateException("No autenticado");
        var u = usuarioRepository.findById(UUID.fromString(dto.getId())).orElseThrow();
        var h = ensureForUsuario(u);
        String existing = h.getAntecedentesFamiliares();
        if (existing == null || existing.trim().isEmpty()) return toDto(h);
        // split entries separated by one or more blank lines
        String[] parts = existing.split("\r?\n\s*\r?\n");
        List<String> list = new ArrayList<>();
        for (String p : parts) if (p != null && !p.trim().isEmpty()) list.add(p.trim());
        if (index < 0 || index >= list.size()) throw new IllegalArgumentException("Índice fuera de rango");
        list.remove(index);
        String merged = String.join("\n\n", list);
        h.setAntecedentesFamiliares(merged);
        historiaRepo.save(h);
        return toDto(h);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO editarAntecedente(int index, String texto) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) throw new IllegalStateException("No autenticado");
        var u = usuarioRepository.findById(UUID.fromString(dto.getId())).orElseThrow();
        var h = ensureForUsuario(u);
        String existing = h.getAntecedentesFamiliares();
        if (existing == null || existing.trim().isEmpty()) throw new IllegalArgumentException("No hay antecedentes");
        String[] parts = existing.split("\r?\n\s*\r?\n");
        List<String> list = new ArrayList<>();
        for (String p : parts) if (p != null && !p.trim().isEmpty()) list.add(p.trim());
        if (index < 0 || index >= list.size()) throw new IllegalArgumentException("Índice fuera de rango");
        String ts = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String entry = "[" + ts + "] " + (texto == null ? "" : texto.trim());
        list.set(index, entry);
        String merged = String.join("\n\n", list);
        h.setAntecedentesFamiliares(merged);
        historiaRepo.save(h);
        return toDto(h);
    }
}
