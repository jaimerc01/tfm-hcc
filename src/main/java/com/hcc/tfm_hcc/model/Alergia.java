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
 * Entidad que representa una alergia o intolerancia registrada por el paciente
 * en su historial clínico.
 *
 * <p>Sustituye al almacenamiento previo de alergias como {@link DatoClinico} genérico,
 * que estaba pensado para mediciones numéricas de laboratorio y no para descripciones
 * de texto libre.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "alergia")
public class Alergia extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Descripción de la alergia o intolerancia (p. ej. "Alergia al polen").
     * Campo encriptado por tratarse de un dato clínico del paciente.
     */
    @Column(name = "descripcion", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String descripcion;

    /**
     * Historial clínico al que pertenece esta alergia.
     */
    @ManyToOne
    @JoinColumn(name = "id_historial_clinico", referencedColumnName = "id", nullable = false)
    private HistorialClinico historialClinico;
}
