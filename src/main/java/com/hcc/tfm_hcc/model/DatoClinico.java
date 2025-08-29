package com.hcc.tfm_hcc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un dato clínico específico en el sistema HCC.
 * Almacena información médica cuantificable como análisis de laboratorio,
 * mediciones vitales, alergias y otros datos clínicos relevantes.
 * 
 * <p>Esta entidad gestiona:</p>
 * <ul>
 *   <li>Clasificación del tipo de dato clínico</li>
 *   <li>Valores numéricos con sus unidades correspondientes</li>
 *   <li>Observaciones textuales adicionales</li>
 *   <li>Asociación con el historial clínico del paciente</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "dato_clinico")
@Data
public class DatoClinico extends BaseEntity {

    private static final long serialVersionUID = 6L;

    /**
     * Tipo o categoría del dato clínico.
     * Ejemplos: "GLUCOSA", "PRESION_ARTERIAL", "ALERGIA", "COLESTEROL", etc.
     */
    @Column(name = "tipo", nullable = false)
    private String tipo;

    /**
     * Valor numérico del dato clínico.
     * Para datos no numéricos, puede usarse como indicador booleano (0/1).
     */
    @Column(name = "valor", nullable = false)
    private float valor;

    /**
     * Unidad de medida del valor.
     * Ejemplos: "mg/dL", "mmHg", "text", "UI/L", etc.
     */
    @Column(name = "unidad", nullable = false)
    private String unidad;

    /**
     * Observaciones adicionales o comentarios sobre el dato clínico.
     * Campo opcional para información textual complementaria.
     */
    @Column(name = "observacion")
    private String observacion;

    /**
     * Historial clínico al que pertenece este dato.
     * Establece la relación con el paciente propietario de la información.
     */
    @ManyToOne
    @JoinColumn(name = "id_historial_clinico", nullable = false)
    private HistorialClinico historialClinico;
}
