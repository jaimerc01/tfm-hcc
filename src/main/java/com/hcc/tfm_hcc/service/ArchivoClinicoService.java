package com.hcc.tfm_hcc.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.model.ArchivoClinico;

public interface ArchivoClinicoService {
    List<ArchivoClinico> listMine();
    ArchivoClinico uploadMine(MultipartFile file) throws IOException;
    Resource getMineResource(UUID id);
    void deleteMine(UUID id) throws IOException;
}
