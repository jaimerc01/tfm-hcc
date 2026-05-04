package com.hcc.tfm_hcc.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Documento de auditoría que registra todos los cambios realizados en datos clínicos.
 * 
 * Almacenado en MongoDB para proporcionar trazabilidad completa de quién cambió qué,
 * cuándo, por qué y qué valores fueron modificados.
 * 
 * Soporta auditoría de CRUD operaciones en datos sensibles médicos, respetando
 * requisitos RGPD y de cumplimiento sanitario.
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Document(collection = "auditoria_cambio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "idx_usuario_timestamp", def = "{'id_usuario': 1, 'fecha_cambio': -1}"),
    @CompoundIndex(name = "idx_paciente_timestamp", def = "{'id_paciente': 1, 'fecha_cambio': -1}"),
    @CompoundIndex(name = "idx_tabla_timestamp", def = "{'tabla': 1, 'fecha_cambio': -1}")
})
public class AuditoriaCambio {

    /**
     * Identificador único del registro de auditoría.
     */
    @Id
    private String id;

    /**
     * UUID del usuario que realizó el cambio (paciente o médico).
     */
    @Field("id_usuario")
    @Indexed
    private String idUsuario;

    /**
     * UUID del paciente afectado por el cambio.
     * En cambios realizados por el médico, este es el paciente del que se modificó el dato.
     * En cambios realizados por el paciente sobre sí mismo, coincide con id_usuario.
     */
    @Field("id_paciente")
    @Indexed
    private String idPaciente;

    /**
     * UUID del médico que realizó el cambio (si aplica).
     * Nulo si el cambio fue realizado por el paciente.
     */
    @Field("id_medico")
    private String idMedico;

    /**
     * Tipo o categoría del dato clínico que cambió.
     * Ejemplos: "GLUCOSA", "PRESION_ARTERIAL", "ALERGIA", "ANTECEDENTES", etc.
     */
    @Field("tipo_cambio")
    @Indexed
    private String tipoCambio;

    /**
     * Nombre de la tabla o entidad que fue modificada.
     * Ejemplos: "dato_clinico", "historial_clinico", "anotacion_medica"
     */
    @Field("tabla")
    @Indexed
    private String tabla;

    /**
     * Valor anterior del dato (antes de la modificación).
     * Para eliminaciones, contiene el valor completo que fue borrado.
     * Puede ser null en inserciones nuevas.
     */
    @Field("valor_anterior")
    private String valorAnterior;

    /**
     * Valor nuevo del dato (después de la modificación).
     * Para eliminaciones, puede ser null.
     * Para inserciones, contiene el valor inicial.
     */
    @Field("valor_nuevo")
    private String valorNuevo;

    /**
     * Fecha y hora del cambio en formato LocalDateTime (UTC).
     * Generada automáticamente al crear el registro.
     */
    @Field("fecha_cambio")
    @Indexed
    private LocalDateTime fechaCambio;

    /**
     * Razón o justificación del cambio.
     * Información de contexto que ayuda a entender por qué se realizó el cambio.
     * Ejemplos: "Corrección de datos erróneos", "Actualización periódica", "Rectificación a solicitud del paciente"
     */
    @Field("razon_cambio")
    private String razonCambio;

    /**
     * Tipo de operación realizada: CREATE, UPDATE, DELETE
     */
    @Field("tipo_operacion")
    @Indexed
    private TipoOperacion tipoOperacion;

    /**
     * Identificador del recurso específico que fue modificado.
     * Para DatoClinico, sería el UUID del dato.
     * Para HistorialClinico, sería el UUID del historial.
     */
    @Field("id_recurso")
    @Indexed
    private String idRecurso;

    /**
     * Información adicional o contexto de la operación.
     * Almacenado como JSON string para flexibilidad.
     */
    @Field("metadatos")
    private String metadatos;

    /**
     * Enumeración de tipos de operación soportadas.
     */
    public enum TipoOperacion {
        CREATE,    // Creación de nuevo dato
        UPDATE,    // Modificación de dato existente
        DELETE     // Eliminación de dato
    }

    /**
     * Crea un registro de auditoría con valores iniciales.
     * Genera automáticamente el ID y la fecha.
     */
    public static AuditoriaCambio crear() {
        AuditoriaCambio auditoria = new AuditoriaCambio();
        auditoria.setId(UUID.randomUUID().toString());
        auditoria.setFechaCambio(LocalDateTime.now());
        return auditoria;
    }

    @Override
    public String toString() {
        return "AuditoriaCambio{" +
                "id='" + id + '\'' +
                ", idUsuario='" + idUsuario + '\'' +
                ", idPaciente='" + idPaciente + '\'' +
                ", idMedico='" + idMedico + '\'' +
                ", tipoCambio='" + tipoCambio + '\'' +
                ", tabla='" + tabla + '\'' +
                ", fechaCambio=" + fechaCambio +
                ", tipoOperacion=" + tipoOperacion +
                '}';
    }
}
