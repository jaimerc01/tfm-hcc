package com.hcc.tfm_hcc.model;

import java.time.LocalDateTime;

import com.hcc.tfm_hcc.converter.AESEncryptionConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad que representa un cambio en el historial clínico de un paciente <b>propuesto por un
 * médico</b> y pendiente de que el paciente lo confirme.
 *
 * <p>El anteproyecto del TFM contempla que los médicos puedan corregir y añadir datos del
 * historial de sus pacientes. Para que ninguna modificación ocurra sin permiso del interesado,
 * el cambio no se aplica en el momento: el médico crea una propuesta ({@link #ESTADO_PENDIENTE}),
 * el paciente recibe una notificación y la resuelve aceptándola ({@link #ESTADO_ACEPTADA}, y
 * entonces el cambio se reproduce sobre el historial) o rechazándola ({@link #ESTADO_RECHAZADA}).
 * Si la relación asistencial se revoca mientras la propuesta sigue pendiente, pasa a
 * {@link #ESTADO_ANULADA}. Es la misma máquina de estados que {@link SolicitudAsignacion}.</p>
 *
 * <p>Los campos {@link #payloadJson}, {@link #descripcionActual} y {@link #motivo} contienen
 * texto clínico o justificaciones que pueden revelar información de salud, por lo que se
 * persisten cifrados con {@link AESEncryptionConverter}, igual que el resto de datos clínicos
 * de la aplicación.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@NoArgsConstructor
@Entity
@Getter
@Setter
@ToString
@Table(name = "propuesta_cambio_clinico")
public class PropuestaCambioClinico extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** Propuesta enviada por el médico, a la espera de que el paciente la resuelva. */
    public static final String ESTADO_PENDIENTE = "PENDIENTE";

    /** El paciente ha aceptado la propuesta y el cambio se ha aplicado sobre su historial. */
    public static final String ESTADO_ACEPTADA = "ACEPTADA";

    /** El paciente ha rechazado la propuesta; el cambio no se aplica. */
    public static final String ESTADO_RECHAZADA = "RECHAZADA";

    /** Propuesta anulada sin resolver (p. ej. al finalizar la relación médico-paciente). */
    public static final String ESTADO_ANULADA = "ANULADA";

    /**
     * Apartado del historial clínico al que afecta la propuesta.
     */
    public enum Dominio {
        ANTECEDENTE,
        ALERGIA,
        ANALISIS_SANGRE,
        SIGNOS_VITALES,
        ANALISIS_ORINA
    }

    /**
     * Tipo de operación que el médico propone sobre el apartado indicado.
     */
    public enum Operacion {
        CREATE,
        UPDATE,
        DELETE
    }

    /**
     * Médico que envía la propuesta de cambio.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_medico", referencedColumnName = "id", nullable = false, updatable = false)
    private Usuario medico;

    /**
     * Paciente propietario del historial afectado y responsable de confirmar el cambio.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_paciente", referencedColumnName = "id", nullable = false, updatable = false)
    private Usuario paciente;

    /**
     * Historial clínico sobre el que se aplicaría el cambio si el paciente lo acepta.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_historial_clinico", referencedColumnName = "id", nullable = false, updatable = false)
    private HistorialClinico historialClinico;

    /**
     * Apartado del historial afectado.
     */
    @Column(name = "dominio", nullable = false, length = 20, updatable = false)
    @Enumerated(EnumType.STRING)
    private Dominio dominio;

    /**
     * Operación propuesta (alta, edición o borrado).
     */
    @Column(name = "operacion", nullable = false, length = 10, updatable = false)
    @Enumerated(EnumType.STRING)
    private Operacion operacion;

    /**
     * Identificador (UUID en texto) del recurso concreto sobre el que actúa la propuesta en
     * las operaciones {@link Operacion#UPDATE} y {@link Operacion#DELETE} (un {@code DatoClinico},
     * un {@code AntecedenteClinico} o una {@code Alergia}). Nulo en las altas.
     */
    @Column(name = "id_recurso_objetivo", updatable = false)
    private String idRecursoObjetivo;

    /**
     * Valores propuestos serializados en JSON (para altas y ediciones). Cifrado por poder
     * contener datos clínicos del paciente.
     */
    @Column(name = "payload_json")
    @Convert(converter = AESEncryptionConverter.class)
    @ToString.Exclude
    private String payloadJson;

    /**
     * Instantánea legible del valor actual del recurso, para que el paciente pueda comparar
     * "lo que hay ahora" con "lo que se propone" al decidir. Nulo en las altas. Cifrado por
     * tratarse de un dato clínico.
     */
    @Column(name = "descripcion_actual")
    @Convert(converter = AESEncryptionConverter.class)
    @ToString.Exclude
    private String descripcionActual;

    /**
     * Motivo del cambio indicado por el médico. Es obligatorio y se traslada a
     * {@code AuditoriaCambio.razonCambio} cuando el cambio se aplica. Cifrado por poder
     * revelar información clínica.
     */
    @Column(name = "motivo", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    @ToString.Exclude
    private String motivo;

    /**
     * Estado actual de la propuesta. Valores posibles: {@link #ESTADO_PENDIENTE},
     * {@link #ESTADO_ACEPTADA}, {@link #ESTADO_RECHAZADA}, {@link #ESTADO_ANULADA}.
     */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    /**
     * Fecha y hora en que el paciente resolvió la propuesta (o en que se anuló). Nula
     * mientras sigue pendiente.
     */
    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
}
