package com.hcc.tfm_hcc.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;

public interface HistoriaClinicaController {
    List<ArchivoClinicoDTO> listMine();
    ResponseEntity<?> upload(MultipartFile file) throws IOException;
    ResponseEntity<?> download(UUID id);
    ResponseEntity<?> delete(UUID id) throws IOException;
}
