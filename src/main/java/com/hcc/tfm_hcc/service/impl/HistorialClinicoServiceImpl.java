package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.Usuario;
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
    private com.hcc.tfm_hcc.repository.DatoClinicoRepository datoClinicoRepository;

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
            var list = new java.util.ArrayList<com.hcc.tfm_hcc.dto.DatoClinicoDTO>();
            for (var x : datos) {
                // include allergy/intolerance records regardless of who created them
                if (!"ALERGIA/INTOLERANCIA".equals(x.getTipo())) continue;
                com.hcc.tfm_hcc.dto.DatoClinicoDTO xd = new com.hcc.tfm_hcc.dto.DatoClinicoDTO();
                xd.setId(x.getId() == null ? null : x.getId().toString());
                xd.setTipo(x.getTipo());
                xd.setValor(String.valueOf(x.getValor()));
                xd.setUnidad(x.getUnidad());
                xd.setObservacion(x.getObservacion());
                list.add(xd);
            }
            d.setDatosClinicos(list);
        }
        return d;
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialClinicoDTO obtenerHistoriaUsuarioActual() {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return null;
        var opt = usuarioRepository.findById(java.util.UUID.fromString(dto.getId()));
        if (opt.isEmpty()) return null;
        var u = opt.get();
        return historiaRepo.findByUsuario(u).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarIdentificacion(String identificacionJson) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) throw new IllegalStateException("No autenticado");
        var u = usuarioRepository.findById(java.util.UUID.fromString(dto.getId())).orElseThrow();
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
        var u = usuarioRepository.findById(java.util.UUID.fromString(dto.getId())).orElseThrow();
        var h = ensureForUsuario(u);
    h.setAntecedentesFamiliares(antecedentesFamiliares);
        historiaRepo.save(h);
        return toDto(h);
    }

    @Override
    @Transactional
    public HistorialClinicoDTO actualizarAlergias(String alergiasJson) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) throw new IllegalStateException("No autenticado");
        var u = usuarioRepository.findById(java.util.UUID.fromString(dto.getId())).orElseThrow();
        var h = ensureForUsuario(u);
        // Store each non-empty line as a DatoClinico of tipo ALERGIA or INTOLERANCIA
    // Do not delete previous entries: append new allergy/intolerance records so historical data is preserved

        if (alergiasJson != null && !alergiasJson.trim().isEmpty()) {
            String[] lines = alergiasJson.split("\\r?\\n");
            java.util.List<com.hcc.tfm_hcc.model.DatoClinico> nuevos = new java.util.ArrayList<>();
            for (String ln : lines) {
                String text = ln == null ? "" : ln.trim();
                if (text.isEmpty()) continue;
                // Store all allergy/intolerance items with the unified tipo string
                com.hcc.tfm_hcc.model.DatoClinico d = new com.hcc.tfm_hcc.model.DatoClinico();
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
}
