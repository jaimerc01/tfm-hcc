package com.hcc.tfm_hcc.dto;

import java.util.List;

import lombok.Data;

@Data
public class HistorialClinicoDTO {
    // antecedentes clínicos (personales y familiares) del paciente
    private List<AntecedenteClinicoDTO> antecedentes;
    // alergias e intolerancias del paciente
    private List<AlergiaDTO> alergias;
    // server-persisted analysis entries (if any)
    private List<DatoClinicoDTO> analisisSangre;
    // signos vitales: frecuencia cardíaca, presión arterial, IMC
    private List<DatoClinicoDTO> signosVitales;
    // análisis de orina: pH, etc.
    private List<DatoClinicoDTO> analisisOrina;
}
