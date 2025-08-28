package com.hcc.tfm_hcc.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;

public interface HistorialClinicoController {
    List<ArchivoClinicoDTO> listMine();
    ResponseEntity<?> upload(MultipartFile file) throws IOException;
    ResponseEntity<?> download(UUID id);
    ResponseEntity<?> delete(UUID id) throws IOException;
    ResponseEntity<?> getMiHistoria();
    ResponseEntity<?> actualizarIdentificacion(String identificacionJson);
    ResponseEntity<?> actualizarAntecedentes(String antecedentesFamiliares);
    ResponseEntity<?> actualizarAlergias(String alergiasJson);
    ResponseEntity<?> actualizarAnalisisSangre(String analisisJson);
    ResponseEntity<?> borrarDatoClinico(java.util.UUID id);
    ResponseEntity<?> borrarAntecedente(int index);
    ResponseEntity<?> editarAntecedente(int index, String texto);
}
