package com.hcc.tfm_hcc.converter;

import com.hcc.tfm_hcc.constants.TiposDatoClinico;
import com.hcc.tfm_hcc.dto.DatoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.model.DatoClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Convertidor para transformar historial clínico y datos clínicos a DTOs.
 */
@Component
@RequiredArgsConstructor
public class HistorialClinicoConverter {

    private static final String TIPO_ALERGIA_INTOLERANCIA = "alergia/intolerancia";

    private final RangoConverter rangoConverter;

    /**
     * Convierte un historial clínico y sus datos asociados a DTO.
     *
     * @param historial historial clínico
     * @param datosClinicos lista de datos clínicos asociados
     * @return DTO del historial clínico
     */
    public HistorialClinicoDTO toDto(HistorialClinico historial, List<DatoClinico> datosClinicos) {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        dto.setAntecedentesFamiliares(historial != null ? historial.getAntecedentesFamiliares() : null);

        if (datosClinicos == null || datosClinicos.isEmpty()) {
            dto.setDatosClinicos(new ArrayList<>());
            dto.setAnalisisSangre(new ArrayList<>());
            dto.setSignosVitales(new ArrayList<>());
            dto.setAnalisisOrina(new ArrayList<>());
            return dto;
        }

        List<DatoClinicoDTO> alergias = new ArrayList<>();
        List<DatoClinicoDTO> analisisSangre = new ArrayList<>();
        List<DatoClinicoDTO> signosVitales = new ArrayList<>();
        List<DatoClinicoDTO> analisisOrina = new ArrayList<>();

        for (DatoClinico datoClinico : datosClinicos) {
            DatoClinicoDTO datoDto = toDatoClinicoDto(datoClinico);
            String tipo = datoClinico.getTipo();

            if (TIPO_ALERGIA_INTOLERANCIA.equals(tipo)) {
                alergias.add(datoDto);
            } else if (perteneceATipo(tipo, TiposDatoClinico.SIGNOS_VITALES)) {
                signosVitales.add(datoDto);
            } else if (perteneceATipo(tipo, TiposDatoClinico.ANALISIS_ORINA)) {
                analisisOrina.add(datoDto);
            } else {
                analisisSangre.add(datoDto);
            }
        }

        dto.setDatosClinicos(alergias);
        dto.setAnalisisSangre(analisisSangre);
        dto.setSignosVitales(signosVitales);
        dto.setAnalisisOrina(analisisOrina);
        return dto;
    }

    /**
     * Comprueba si un tipo de dato clínico pertenece a un dominio conocido, ignorando mayúsculas.
     */
    private boolean perteneceATipo(String tipo, List<String> tiposConocidos) {
        if (tipo == null) {
            return false;
        }
        return tiposConocidos.stream().anyMatch(t -> t.equalsIgnoreCase(tipo));
    }

    /**
     * Convierte un historial clínico a DTO.
     *
     * @param historial historial clínico
     * @return DTO del historial clínico
     */
    public HistorialClinicoDTO toDto(HistorialClinico historial) {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        dto.setAntecedentesFamiliares(historial != null ? historial.getAntecedentesFamiliares() : null);
        return dto;
    }

    public HistorialClinico toEntity(HistorialClinicoDTO dto) {
        HistorialClinico entity = new HistorialClinico();
        entity.setAntecedentesFamiliares(dto.getAntecedentesFamiliares());
        return entity;
    }

    /**
     * Convierte un dato clínico a DTO.
     *
     * @param datoClinico dato clínico
     * @return DTO del dato clínico
     */
    public DatoClinicoDTO toDatoClinicoDto(DatoClinico datoClinico) {
        DatoClinicoDTO dto = new DatoClinicoDTO();
        dto.setId(datoClinico.getId() != null ? datoClinico.getId().toString() : null);
        dto.setTipo(datoClinico.getTipo());
        dto.setValor(String.valueOf(datoClinico.getValor()));
        dto.setUnidad(datoClinico.getUnidad());
        dto.setObservacion(datoClinico.getObservacion());
        dto.setCreatedAt(datoClinico.getFechaCreacion() != null ? datoClinico.getFechaCreacion().toString() : null);

        if (datoClinico.getRango() != null) {
            dto.setRango(rangoConverter.toDto(datoClinico.getRango()));
        }

        return dto;
    }
}