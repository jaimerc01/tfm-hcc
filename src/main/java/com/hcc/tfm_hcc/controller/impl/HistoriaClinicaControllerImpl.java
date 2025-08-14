package com.hcc.tfm_hcc.controller.impl;

import java.io.IOException;
import java.net.URLEncoder;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.controller.HistoriaClinicaController;
import com.hcc.tfm_hcc.facade.HistoriaClinicaFacade;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/historia/archivos")
@RequiredArgsConstructor
public class HistoriaClinicaControllerImpl implements HistoriaClinicaController {

    private final HistoriaClinicaFacade historiaClinicaFacade;

    @Override
    @GetMapping
    public List<ArchivoClinicoDTO> listMine() {
        return historiaClinicaFacade.listMine();
    }

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            var dto = historiaClinicaFacade.upload(file);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al guardar el archivo");
        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<?> download(@PathVariable("id") UUID id) {
        try {
            ArchivoClinicoDTO meta = historiaClinicaFacade.getMine(id);
            Resource resource = historiaClinicaFacade.getMineResource(id);
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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        try {
            historiaClinicaFacade.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Archivo no encontrado");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("No se pudo eliminar el archivo");
        }
    }
}
