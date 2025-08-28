package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;

public interface HistorialClinicoService {
    HistorialClinicoDTO obtenerHistoriaUsuarioActual();
    HistorialClinicoDTO actualizarIdentificacion(String identificacionJson);
    HistorialClinicoDTO actualizarAntecedentes(String antecedentesFamiliares);
    HistorialClinicoDTO actualizarAlergias(String alergiasJson);
    HistorialClinicoDTO actualizarAnalisisSangre(String analisisJson);
    void borrarDatoClinico(java.util.UUID id);
    HistorialClinicoDTO borrarAntecedente(int index);
    HistorialClinicoDTO editarAntecedente(int index, String texto);
}
