package com.hcc.tfm_hcc.converter;

import com.hcc.tfm_hcc.constants.TiposDatoClinico;
import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.model.Alergia;
import com.hcc.tfm_hcc.model.AntecedenteClinico;
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

    private final RangoConverter rangoConverter;
    private final AlergiaConverter alergiaConverter;
    private final AntecedenteClinicoConverter antecedenteClinicoConverter;

    /**
     * Convierte un historial clínico y todos sus datos asociados a DTO.
     *
     * @param historial historial clínico
     * @param datosClinicos lista de datos clínicos cuantitativos asociados (análisis, signos vitales...)
     * @param alergias lista de alergias del paciente
     * @param antecedentes lista de antecedentes clínicos del paciente
     * @return DTO del historial clínico
     */
    public HistorialClinicoDTO toDto(HistorialClinico historial, List<DatoClinico> datosClinicos,
            List<Alergia> alergias, List<AntecedenteClinico> antecedentes) {
        HistorialClinicoDTO dto = toDto(historial, datosClinicos);
        dto.setAlergias(mapAlergias(alergias));
        dto.setAntecedentes(mapAntecedentes(antecedentes));
        return dto;
    }

    /**
     * Convierte un historial clínico y sus datos clínicos cuantitativos a DTO.
     * No incluye alergias ni antecedentes; usar el método de cuatro parámetros para el DTO completo.
     *
     * @param historial historial clínico
     * @param datosClinicos lista de datos clínicos asociados
     * @return DTO del historial clínico
     */
    public HistorialClinicoDTO toDto(HistorialClinico historial, List<DatoClinico> datosClinicos) {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();

        if (datosClinicos == null || datosClinicos.isEmpty()) {
            dto.setAnalisisSangre(new ArrayList<>());
            dto.setSignosVitales(new ArrayList<>());
            dto.setAnalisisOrina(new ArrayList<>());
            return dto;
        }

        List<DatoClinicoDTO> analisisSangre = new ArrayList<>();
        List<DatoClinicoDTO> signosVitales = new ArrayList<>();
        List<DatoClinicoDTO> analisisOrina = new ArrayList<>();

        for (DatoClinico datoClinico : datosClinicos) {
            DatoClinicoDTO datoDto = toDatoClinicoDto(datoClinico);
            String tipo = datoClinico.getTipo();

            if (perteneceATipo(tipo, TiposDatoClinico.SIGNOS_VITALES)) {
                signosVitales.add(datoDto);
            } else if (perteneceATipo(tipo, TiposDatoClinico.ANALISIS_ORINA)) {
                analisisOrina.add(datoDto);
            } else {
                analisisSangre.add(datoDto);
            }
        }

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
     * Convierte un historial clínico a DTO sin datos clínicos, alergias ni antecedentes.
     *
     * @param historial historial clínico
     * @return DTO del historial clínico
     */
    public HistorialClinicoDTO toDto(HistorialClinico historial) {
        return new HistorialClinicoDTO();
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

    private List<AlergiaDTO> mapAlergias(List<Alergia> alergias) {
        if (alergias == null) {
            return new ArrayList<>();
        }
        return alergias.stream().map(alergiaConverter::toDto).toList();
    }

    private List<AntecedenteClinicoDTO> mapAntecedentes(List<AntecedenteClinico> antecedentes) {
        if (antecedentes == null) {
            return new ArrayList<>();
        }
        return antecedentes.stream().map(antecedenteClinicoConverter::toDto).toList();
    }
}
