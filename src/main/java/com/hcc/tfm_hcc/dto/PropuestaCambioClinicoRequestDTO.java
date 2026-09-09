package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Cuerpo de la petición con la que un médico propone un cambio en el historial de un paciente
 * asignado.
 *
 * <p>Reutiliza los DTOs de dato que ya usan los endpoints del paciente: según {@link #dominio}
 * se rellena {@link #antecedente}, {@link #alergia} o {@link #medicion}. Para
 * {@link com.hcc.tfm_hcc.model.PropuestaCambioClinico.Operacion#UPDATE} y
 * {@link com.hcc.tfm_hcc.model.PropuestaCambioClinico.Operacion#DELETE} debe indicarse además
 * {@link #idRecursoObjetivo}. El {@link #motivo} es obligatorio en todas las operaciones y se
 * traslada a la auditoría cuando el cambio se aplica.</p>
 */
@Data
public class PropuestaCambioClinicoRequestDTO {

    /** Apartado del historial: ANTECEDENTE, ALERGIA, ANALISIS_SANGRE, SIGNOS_VITALES, ANALISIS_ORINA. */
    private String dominio;

    /** Operación propuesta: CREATE, UPDATE o DELETE. */
    private String operacion;

    /** UUID (en texto) del recurso objetivo para UPDATE y DELETE. */
    private String idRecursoObjetivo;

    /** Motivo del cambio indicado por el médico. Obligatorio. */
    private String motivo;

    /** Datos del antecedente propuesto (dominio ANTECEDENTE, operaciones CREATE/UPDATE). */
    private AntecedenteClinicoDTO antecedente;

    /** Datos de la alergia propuesta (dominio ALERGIA, operación CREATE). */
    private AlergiaDTO alergia;

    /** Datos de la medición propuesta (dominios de análisis/signos vitales, operaciones CREATE/UPDATE). */
    private DatoClinicoEntradaDTO medicion;
}
