package com.hcc.tfm_hcc.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Representación de una propuesta de cambio clínico tal como cruza la API.
 *
 * <p>Sustituye a la exposición directa de la entidad JPA {@code PropuestaCambioClinico}, que
 * arrastra asociaciones {@code @ManyToOne} a {@code Usuario} (médico y paciente) e
 * {@code HistorialClinico}. Aquí solo viajan el estado de la propuesta, la operación
 * propuesta, el motivo, la instantánea del valor actual, los valores propuestos ya
 * parseados y los datos identificativos mínimos de médico y paciente.</p>
 */
@Data
public class PropuestaCambioClinicoDTO {

    private String id;
    private String dominio;
    private String operacion;
    private String estado;
    private String idRecursoObjetivo;
    private String motivo;
    private String descripcionActual;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaResolucion;
    private PersonaResumenDTO medico;
    private PersonaResumenDTO paciente;

    /** Antecedente propuesto, si el dominio es {@code ANTECEDENTE} y la operación no es un borrado. */
    private AntecedenteClinicoDTO antecedente;

    /** Alergia propuesta, si el dominio es {@code ALERGIA} y la operación no es un borrado. */
    private AlergiaDTO alergia;

    /** Medición propuesta, si el dominio es un análisis o signos vitales y la operación no es un borrado. */
    private DatoClinicoEntradaDTO medicion;
}
