package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;

public interface HistorialClinicoService {
    HistorialClinicoDTO obtenerHistoriaUsuarioActual();
    HistorialClinicoDTO actualizarIdentificacion(String identificacionJson);
    HistorialClinicoDTO actualizarAntecedentes(String antecedentesFamiliares);
    HistorialClinicoDTO actualizarAlergias(String alergiasJson);
}
