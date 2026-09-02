package com.hcc.tfm_hcc.model;

import com.hcc.tfm_hcc.converter.AESEncryptionConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
@NoArgsConstructor
@Entity
@Table(name = "dato_clinico")
@Getter
@Setter
@ToString
public class DatoClinico extends BaseEntity {

    private static final long serialVersionUID = 6L;

    /**
     * Tipo o categoría del dato clínico.
     * Ejemplos: "GLUCOSA", "PRESION_ARTERIAL", "ALERGIA", "COLESTEROL", etc.
     * Campo cifrado con AES/GCM (no determinista): la búsqueda por igualdad ya no
     * se aplica sobre esta columna, sino sobre {@link #tipoHash}.
     */
    @Column(name = "tipo", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String tipo;

    /**
     * Índice de búsqueda determinista del tipo (HMAC-SHA256), calculado por
     * {@link com.hcc.tfm_hcc.service.HmacSearchIndexService}. Permite filtrar por
     * tipo (p. ej. "dame todos los datos de tipo GLUCOSA de este historial") sin
     * depender de la igualdad sobre el valor cifrado, que nunca coincide dos veces.
     */
    @Column(name = "tipo_hash", nullable = false)
    private String tipoHash;

    /**
     * Valor numérico del dato clínico, almacenado como texto para poder cifrarlo.
     * Para datos no numéricos, puede usarse como indicador booleano (0/1).
     * Campo encriptado por tratarse de un dato clínico del paciente.
     */
    @Column(name = "valor", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String valor;

    /**
     * Unidad de medida del valor.
     * Ejemplos: "mg/dL", "mmHg", "text", "UI/L", etc.
     * Campo encriptado por tratarse de un dato clínico del paciente.
     */
    @Column(name = "unidad", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String unidad;

    /**
     * Observaciones adicionales o comentarios sobre el dato clínico.
     * Campo opcional para información textual complementaria.
     * Campo encriptado por tratarse de un dato clínico del paciente.
     */
    @Column(name = "observacion")
    @Convert(converter = AESEncryptionConverter.class)
    private String observacion;

    /**
     * Rango de valores recomendados asociado a este dato clínico.
     * 
     * Esta relación permite establecer los valores normales o recomendados
     * para el tipo de dato clínico específico, facilitando la interpretación
     * de los resultados y la identificación de valores fuera del rango normal.
     */
    @ManyToOne
    @JoinColumn(name = "id_rango")
    private Rango rango;

    /**
     * Historial clínico al que pertenece este dato.
     * Establece la relación con el paciente propietario de la información.
     */
    @ManyToOne
    @JoinColumn(name = "id_historial_clinico", nullable = false)
    private HistorialClinico historialClinico;
}
