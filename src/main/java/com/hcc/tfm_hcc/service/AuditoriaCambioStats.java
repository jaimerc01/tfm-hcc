package com.hcc.tfm_hcc.service;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de estadísticas de auditoría para un paciente.
 * Proporciona un resumen de cambios realizados en su historial clínico.
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaCambioStats {

    /**
     * UUID del paciente cuyas estadísticas se están generando
     */
    private String idPaciente;

    /**
     * Número total de cambios registrados
     */
    private long totalCambios;

    /**
     * Número de creaciones de datos clínicos
     */
    private long creaciones;

    /**
     * Número de actualizaciones de datos clínicos
     */
    private long actualizaciones;

    /**
     * Número de eliminaciones de datos clínicos
     */
    private long eliminaciones;

    /**
     * Mapa de cambios por tipo (ej: "GLUCOSA" -> 15, "PRESION_ARTERIAL" -> 8)
     */
    private Map<String, Long> cambiosPorTipo = new HashMap<>();

    /**
     * Mapa de cambios por usuario/médico (quien realizó los cambios)
     */
    private Map<String, Long> cambiosPorUsuario = new HashMap<>();

    /**
     * Mapa de cambios por tabla
     */
    private Map<String, Long> cambiosPorTabla = new HashMap<>();

    /**
     * Número de cambios sin razón/justificación asignada
     */
    private long cambiosSinRazon;

    /**
     * Indica si se detectó actividad anómala
     */
    private boolean actividadAnomala;

    @Override
    public String toString() {
        return "AuditoriaCambioStats{" +
                "idPaciente='" + idPaciente + '\'' +
                ", totalCambios=" + totalCambios +
                ", creaciones=" + creaciones +
                ", actualizaciones=" + actualizaciones +
                ", eliminaciones=" + eliminaciones +
                ", cambiosSinRazon=" + cambiosSinRazon +
                ", actividadAnomala=" + actividadAnomala +
                '}';
    }
}
