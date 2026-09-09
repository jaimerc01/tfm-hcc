package com.hcc.tfm_hcc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un rango de valores en el sistema HCC.
 * 
 * <p>Esta entidad modela los rangos de valores utilizados en el sistema,
 * típicamente para definir rangos normales en análisis clínicos, parámetros
 * médicos o cualquier otro tipo de medición que requiera establecer límites
 * superiores e inferiores.</p>
 * 
 * <p>Características principales:</p>
 * <ul>
 *   <li>Hereda de {@link BaseEntity} para gestión automática de ID y timestamps</li>
 *   <li>Almacena valores superior e inferior como cadenas para flexibilidad</li>
 *   <li>Incluye un nombre descriptivo para identificar el tipo de rango</li>
 *   <li>Mapea a la tabla 'rangos' en la base de datos</li>
 * </ul>
 * 
 * <p>Casos de uso típicos:</p>
 * <ul>
 *   <li>Rangos normales para análisis de sangre (ej: glucosa, colesterol)</li>
 *   <li>Rangos de edad para diferentes tratamientos</li>
 *   <li>Rangos de peso o altura para categorización</li>
 *   <li>Rangos de valores vitales (presión arterial, frecuencia cardíaca)</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 * @see BaseEntity
 */
@Entity
@Table(name = "rangos")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Rango extends BaseEntity {

    /**
     * Valor superior del rango.
     * 
     * <p>Representa el límite máximo del rango. Se almacena como String
     * para permitir flexibilidad en el formato (números decimales,
     * expresiones, etc.).</p>
     * 
     * <p>Ejemplos:</p>
     * <ul>
     *   <li>Glucosa: "140 mg/dL"</li>
     *   <li>Edad: "65 años"</li>
     *   <li>Presión sistólica: "120 mmHg"</li>
     * </ul>
     */
    @Column(name = "valor_superior", length = 100)
    private String valorSuperior;

    /**
     * Valor inferior del rango.
     * 
     * <p>Representa el límite mínimo del rango. Se almacena como String
     * para permitir flexibilidad en el formato (números decimales,
     * expresiones, etc.).</p>
     * 
     * <p>Ejemplos:</p>
     * <ul>
     *   <li>Glucosa: "70 mg/dL"</li>
     *   <li>Edad: "18 años"</li>
     *   <li>Presión sistólica: "90 mmHg"</li>
     * </ul>
     */
    @Column(name = "valor_inferior", length = 100)
    private String valorInferior;

    /**
     * Nombre descriptivo del rango.
     * 
     * <p>Identificador único y descriptivo que permite categorizar
     * y buscar el rango específico. Este campo es obligatorio y
     * debe ser único en el sistema.</p>
     * 
     * <p>Ejemplos:</p>
     * <ul>
     *   <li>"Glucosa en sangre - Normal"</li>
     *   <li>"Edad adulto joven"</li>
     *   <li>"Presión arterial - Óptima"</li>
     *   <li>"Colesterol total - Deseable"</li>
     * </ul>
     */
    @Column(name = "nombre", nullable = false, length = 200, unique = true)
    private String nombre;

}
