-- Migración manual 002 · Propuestas de cambio clínico (médico propone → paciente confirma).
--
-- En desarrollo el esquema lo genera Hibernate (spring.jpa.hibernate.ddl-auto=update); en
-- producción, donde ddl-auto=none, hay que aplicar este script antes de desplegar la versión
-- que introduce la entidad PropuestaCambioClinico.
--
-- Idempotente: se puede ejecutar varias veces sin efecto adicional.

CREATE TABLE IF NOT EXISTS propuesta_cambio_clinico (
    id                       UUID         NOT NULL DEFAULT gen_random_uuid(),
    id_medico                UUID         NOT NULL REFERENCES usuario (id),
    id_paciente              UUID         NOT NULL REFERENCES usuario (id),
    id_historial_clinico     UUID         NOT NULL REFERENCES historial_clinico (id),
    dominio                  VARCHAR(20)  NOT NULL,
    operacion                VARCHAR(10)  NOT NULL,
    id_recurso_objetivo      VARCHAR(255),
    -- payload_json, descripcion_actual y motivo van cifrados con AES/GCM por la aplicación
    -- (AESEncryptionConverter), igual que el resto de datos clínicos.
    payload_json             TEXT,
    descripcion_actual       TEXT,
    motivo                   TEXT         NOT NULL,
    estado                   VARCHAR(20)  NOT NULL,
    fecha_resolucion         TIMESTAMP,
    fecha_creacion           TIMESTAMP    NOT NULL,
    fecha_ultima_modificacion TIMESTAMP,
    CONSTRAINT pk_propuesta_cambio_clinico PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_propuesta_cambio_paciente_estado
    ON propuesta_cambio_clinico (id_paciente, estado);
CREATE INDEX IF NOT EXISTS idx_propuesta_cambio_medico_paciente
    ON propuesta_cambio_clinico (id_medico, id_paciente);
