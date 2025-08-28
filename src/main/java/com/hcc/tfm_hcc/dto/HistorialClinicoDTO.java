package com.hcc.tfm_hcc.dto;

import java.util.List;

import lombok.Data;

@Data
public class HistorialClinicoDTO {
    private String antecedentesFamiliares;
    // list of existing clinical data for allergies and other entries
    private List<DatoClinicoDTO> datosClinicos;
    // server-persisted analysis entries (if any)
    private List<DatoClinicoDTO> analisisSangre;
}
