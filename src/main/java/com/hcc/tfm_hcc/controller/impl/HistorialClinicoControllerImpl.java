package com.hcc.tfm_hcc.controller.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;

import com.hcc.tfm_hcc.controller.HistorialClinicoController;
import com.hcc.tfm_hcc.facade.HistorialClinicoFacade;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/historia")
@RequiredArgsConstructor
public class HistorialClinicoControllerImpl implements HistorialClinicoController {

    private final HistorialClinicoFacade historialClinicoFacade;

    @Override
    @GetMapping("/archivos")
    public List<ArchivoClinicoDTO> listMine() {
        return historialClinicoFacade.listMine();
    }

    @Override
    @PostMapping(path = "/archivos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            var dto = historialClinicoFacade.upload(file);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al guardar el archivo");
        }
    }

    @Override
    @GetMapping("/archivos/{id}")
    public ResponseEntity<?> download(@PathVariable("id") UUID id) {
        try {
            ArchivoClinicoDTO meta = historialClinicoFacade.getMine(id);
            Resource resource = historialClinicoFacade.getMineResource(id);
            String original = meta.getNombreOriginal() == null ? "archivo" : meta.getNombreOriginal();
            // Fallback ASCII/quoted filename and RFC 5987 filename*
            String quoted = original.replace("\\", "_").replace("\r", " ").replace("\n", " ").replace("\"", "'");
            String encoded = URLEncoder.encode(original, StandardCharsets.UTF_8).replace("+", "%20");
            String cd = "attachment; filename=\"" + quoted + "\"; filename*=UTF-8''" + encoded;
            MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
            if (meta.getContentType() != null) {
                try { mt = MediaType.parseMediaType(meta.getContentType()); } catch (Exception ignored) { }
            }
        return ResponseEntity.ok()
                    .contentType(mt)
            .header(HttpHeaders.CONTENT_DISPOSITION, cd)
            .header("Access-Control-Expose-Headers", HttpHeaders.CONTENT_DISPOSITION)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Archivo no encontrado");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("No se pudo descargar");
        }
    }

    @Override
    @DeleteMapping("/archivos/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        try {
            historialClinicoFacade.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Archivo no encontrado");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("No se pudo eliminar el archivo");
        }
    }

    @Override
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMiHistoria() {
        HistorialClinicoDTO dto = historialClinicoFacade.obtenerMiHistoria();
        if (dto == null) return ResponseEntity.ok(new HistorialClinicoDTO());
        return ResponseEntity.ok(dto);
    }

    @Override
    @PostMapping("/me/identificacion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> actualizarIdentificacion(@RequestBody String identificacionJson) {
        try {
            HistorialClinicoDTO res = historialClinicoFacade.actualizarIdentificacion(identificacionJson);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    @PostMapping(path = "/me/antecedentes", consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> actualizarAntecedentes(@RequestBody String antecedentesFamiliares) {
        try {
            String payload = antecedentesFamiliares;
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = attrs != null ? attrs.getRequest() : null;
            String contentType = req != null ? req.getContentType() : null;
            if (contentType != null && contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                payload = URLDecoder.decode(antecedentesFamiliares, StandardCharsets.UTF_8);
            } else if ((antecedentesFamiliares != null) && (antecedentesFamiliares.contains("%") || antecedentesFamiliares.contains("+"))) {
                try { payload = URLDecoder.decode(antecedentesFamiliares, StandardCharsets.UTF_8); } catch (Exception ignored) {}
            }
            HistorialClinicoDTO res = historialClinicoFacade.actualizarAntecedentes(payload);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    @PostMapping(path = "/me/alergias", consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> actualizarAlergias(@RequestBody String alergiasJson) {
        try {
            String payload = alergiasJson;
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = attrs != null ? attrs.getRequest() : null;
            String contentType = req != null ? req.getContentType() : null;
            if (contentType != null && contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                payload = URLDecoder.decode(alergiasJson, StandardCharsets.UTF_8);
            } else if ((alergiasJson != null) && (alergiasJson.contains("%") || alergiasJson.contains("+"))) {
                try { payload = URLDecoder.decode(alergiasJson, StandardCharsets.UTF_8); } catch (Exception ignored) {}
            }
            HistorialClinicoDTO res = historialClinicoFacade.actualizarAlergias(payload);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    @PostMapping(path = "/me/analisis-sangre", consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> actualizarAnalisisSangre(@RequestBody String analisisJson) {
        try {
            String payload = analisisJson;
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = attrs != null ? attrs.getRequest() : null;
            String contentType = req != null ? req.getContentType() : null;
            if (contentType != null && contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                payload = URLDecoder.decode(analisisJson, StandardCharsets.UTF_8);
            } else if ((analisisJson != null) && (analisisJson.contains("%") || analisisJson.contains("+"))) {
                try { payload = URLDecoder.decode(analisisJson, StandardCharsets.UTF_8); } catch (Exception ignored) {}
            }
            HistorialClinicoDTO res = historialClinicoFacade.actualizarAnalisisSangre(payload);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    @DeleteMapping("/me/datos/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> borrarDatoClinico(@PathVariable("id") UUID id) {
        try {
            historialClinicoFacade.borrarDatoClinico(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    @DeleteMapping("/me/antecedentes/{index}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> borrarAntecedente(@PathVariable("index") int index) {
        try {
            HistorialClinicoDTO res = historialClinicoFacade.borrarAntecedente(index);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    @PutMapping(path = "/me/antecedentes/{index}", consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> editarAntecedente(@PathVariable("index") int index, @RequestBody String texto) {
        try {
            String payload = texto;
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = attrs != null ? attrs.getRequest() : null;
            String contentType = req != null ? req.getContentType() : null;
            if (contentType != null && contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                payload = URLDecoder.decode(texto, StandardCharsets.UTF_8);
            } else if ((texto != null) && (texto.contains("%") || texto.contains("+"))) {
                try { payload = URLDecoder.decode(texto, StandardCharsets.UTF_8); } catch (Exception ignored) {}
            }
            HistorialClinicoDTO res = historialClinicoFacade.editarAntecedente(index, payload);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
