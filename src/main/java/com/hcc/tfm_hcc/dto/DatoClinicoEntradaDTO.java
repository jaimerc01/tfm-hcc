package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Una medición cuantitativa que el cliente envía para incorporar al historial clínico
 * (una fila de un análisis de sangre, un signo vital, un parámetro de análisis de orina...).
 *
 * <p>Sustituye al antiguo {@code @RequestBody String} con el JSON crudo: los endpoints de
 * análisis reciben ahora {@code List<DatoClinicoEntradaDTO>} y es Jackson quien deserializa,
 * no la capa de servicio a mano.</p>
 *
 * <ul>
 *   <li>{@code label} / {@code key}: nombre del parámetro (p. ej. "Glucosa"). Se usa
 *       {@code label} si está presente, si no {@code key}; si faltan ambos se cataloga como
 *       genérico.</li>
 *   <li>{@code value}: valor numérico como texto (obligatorio). Admite coma o punto decimal.</li>
 *   <li>{@code unit}: unidad de medida (p. ej. "mg/dL"). Opcional.</li>
 *   <li>{@code createdAt}: fecha real de la medición en ISO-8601. Opcional; si falta se usa
 *       la fecha actual.</li>
 * </ul>
 */
@Data
public class DatoClinicoEntradaDTO {

    private String label;
    private String key;
    private String value;
    private String unit;
    private String createdAt;
}
