package com.hcc.tfm_hcc.facade;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;

public interface HistoriaClinicaFacade {
    List<ArchivoClinicoDTO> listMine();
    ArchivoClinicoDTO upload(MultipartFile file) throws IOException;
    ArchivoClinicoDTO getMine(UUID id);
    Resource getMineResource(UUID id);
    void delete(UUID id) throws IOException;
}
