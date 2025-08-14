package com.hcc.tfm_hcc.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ArchivoClinicoDTO {
    private String id;
    private String nombreOriginal;
    private String contentType;
    private Long sizeBytes;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
}
