package com.hcc.tfm_hcc.dto;

import java.util.List;

import lombok.Data;

@Data
public class HistorialClinicoDTO {
    private String antecedentesFamiliares;
    private List<DatoClinicoDTO> datosClinicos;
}
