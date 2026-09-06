-- Migración manual 003 · Registro del consentimiento informado en la cuenta de usuario.
--
-- Añade a la tabla usuario la fecha en que el interesado prestó su consentimiento explícito
-- para el tratamiento de sus datos de salud (RGPD art. 9.2.a) y la versión de la política de
-- privacidad que aceptó (RGPD art. 7.1: poder demostrar qué información se consintió).
--
-- En desarrollo lo aplica Hibernate (ddl-auto=update); en producción (ddl-auto=none) hay que
-- ejecutar este script antes de desplegar. Idempotente.

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS fecha_consentimiento        TIMESTAMP;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS version_politica_privacidad VARCHAR(255);

-- Cuentas dadas de alta antes de existir el registro explícito: se marca su consentimiento
-- con su propia fecha de alta (aceptaron la política vigente entonces al registrarse) y se
-- deja la versión a NULL para poder identificarlas. Ajustar o revisar según criterio legal.
UPDATE usuario
   SET fecha_consentimiento = fecha_creacion
 WHERE fecha_consentimiento IS NULL;
