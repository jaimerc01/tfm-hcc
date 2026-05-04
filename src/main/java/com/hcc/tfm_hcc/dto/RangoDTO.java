package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * DTO para transferir información de rangos de valores médicos.
 * 
 * <p>Este DTO encapsula la información de rangos de referencia utilizados
 * en análisis clínicos y otros parámetros médicos, proporcionando los
 * límites superior e inferior recomendados.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Data
public class RangoDTO {
    
    /**
     * Identificador único del rango.
     */
    private String id;
    
    /**
     * Nombre descriptivo del rango.
     * Ejemplo: "Glucosa", "Colesterol total", "Hemoglobina"
     */
    private String nombre;
    
    /**
     * Valor superior del rango recomendado.
     * Ejemplo: "140", "200", "16.0"
     */
    private String valorSuperior;
    
    /**
     * Valor inferior del rango recomendado.
     * Ejemplo: "70", "100", "12.0"
     */
    private String valorInferior;
    
    /**
     * Valor superior como número para cálculos en frontend.
     * Se extrae automáticamente del valorSuperior.
     */
    private Double valorSuperiorNumerico;
    
    /**
     * Valor inferior como número para cálculos en frontend.
     * Se extrae automáticamente del valorInferior.
     */
    private Double valorInferiorNumerico;
}
