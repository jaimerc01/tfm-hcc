package com.hcc.tfm_hcc.facade.impl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.facade.HistoriaClinicaFacade;
import com.hcc.tfm_hcc.mapper.ArchivoClinicoMapper;
import com.hcc.tfm_hcc.model.ArchivoClinico;
import com.hcc.tfm_hcc.service.ArchivoClinicoService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HistoriaClinicaFacadeImpl implements HistoriaClinicaFacade {

    private final ArchivoClinicoService archivoClinicoService;
    private final ArchivoClinicoMapper archivoClinicoMapper;

    @Override
    public List<ArchivoClinicoDTO> listMine() {
        return archivoClinicoService.listMine().stream()
                .map(archivoClinicoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ArchivoClinicoDTO upload(MultipartFile file) throws IOException {
        ArchivoClinico saved = archivoClinicoService.uploadMine(file);
        return archivoClinicoMapper.toDto(saved);
    }

    @Override
    public ArchivoClinicoDTO getMine(UUID id) {
        ArchivoClinico ac = archivoClinicoService.listMine().stream().filter(a -> a.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe el archivo"));
        return archivoClinicoMapper.toDto(ac);
    }

    @Override
    public Resource getMineResource(UUID id) {
        return archivoClinicoService.getMineResource(id);
    }

    @Override
    public void delete(UUID id) throws IOException {
        archivoClinicoService.deleteMine(id);
    }
}
