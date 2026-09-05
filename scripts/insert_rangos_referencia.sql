-- Script para insertar rangos de referencia médicos comunes.
-- Añade rangos normales para análisis de sangre, análisis de orina y signos
-- vitales en la tabla `rangos` (entidad Rango: columnas id, nombre,
-- valor_inferior, valor_superior, fecha_creacion, fecha_ultima_modificacion
-- -- heredadas de BaseEntity).
--
-- `nombre` es UNIQUE (ver Rango.java y la migración
-- db/manual-migrations/001-rangos-nombre-unico.sql), así que este script es
-- seguro de ejecutar más de una vez: ON CONFLICT hace que una recarga no
-- duplique filas ni rompa RangoRepository.findByNombreIgnoreCase /
-- findByNombreContainingIgnoreCase (que esperan un único resultado por
-- nombre). Por el mismo motivo no se listan variantes en mayúsculas del
-- mismo parámetro: colisionarían con la búsqueda case-insensitive.
--
-- Cobertura: los 13 parámetros que rastrea el frontend
-- (frontend/src/composables/useAnalisisSangre.js, useAnalisisOrina.js,
-- useSignosVitales.js). El `nombre` de cada fila coincide EXACTAMENTE con el
-- `key` del analito en esos ficheros (sin tildes): así enlaza tanto en el
-- frontend, que normaliza tildes/mayúsculas al comparar, como en el backend
-- (findByNombreIgnoreCase), que no las normaliza.
--
-- Valores de referencia (adultos, población general; simplificados a un
-- único rango unisex cuando la guía clínica distingue por sexo):
--   Glucosa (ayunas): 70-100 mg/dL (ADA). 100-125 = prediabetes, >=126 = diabetes.
--   Hemoglobina: 12.0-16.0 g/dL (real: hombres 13.5-17.5, mujeres 12.0-15.5).
--   Colesterol total / Colesterol: <200 mg/dL "deseable".
--   Colesterol LDL: óptimo/casi óptimo, <=130 mg/dL (ATP III). No hay techo de
--     "demasiado alto" clínicamente relevante para el límite inferior; se usa 0.
--   Colesterol HDL: bajo (riesgo) <40 mg/dL; sin techo de riesgo por HDL alto,
--     se usa 90 como límite superior orientativo.
--   Triglicéridos: <150 mg/dL "normal".
--   Creatinina: 0.6-1.3 mg/dL (real: hombres 0.7-1.3, mujeres 0.6-1.1).
--   Hematocrito: 35-50 % (real: hombres 38.8-50, mujeres 34.9-44.5).
--   PH Orina: 4.5-8.0 pH (rango de referencia estándar de urianálisis).
--   Frecuencia Cardiaca (reposo): 60-100 lpm (AHA).
--   Presión arterial: normal <120/80 mmHg (AHA 2017); se usa 90 como límite
--     inferior de la sistólica para no marcar como "bajo" la hipotensión leve
--     asintomática, y 60 mmHg para la diastólica.
--   IMC: 18.5-24.9 kg/m² (normopeso, clasificación OMS).

INSERT INTO rangos (id, nombre, valor_inferior, valor_superior, fecha_creacion, fecha_ultima_modificacion) VALUES
(gen_random_uuid(), 'Glucosa', '70', '100', NOW(), NOW()),
(gen_random_uuid(), 'Hemoglobina', '12.0', '16.0', NOW(), NOW()),
(gen_random_uuid(), 'Colesterol total', '100', '200', NOW(), NOW()),
(gen_random_uuid(), 'Triglicéridos', '50', '150', NOW(), NOW()),
(gen_random_uuid(), 'Creatinina', '0.6', '1.3', NOW(), NOW()),
(gen_random_uuid(), 'Hematocrito', '35', '50', NOW(), NOW()),
(gen_random_uuid(), 'Colesterol', '100', '200', NOW(), NOW()),
(gen_random_uuid(), 'Colesterol LDL', '0', '130', NOW(), NOW()),
(gen_random_uuid(), 'Colesterol HDL', '40', '90', NOW(), NOW()),
(gen_random_uuid(), 'PH Orina', '4.5', '8.0', NOW(), NOW()),
(gen_random_uuid(), 'Frecuencia Cardiaca', '60', '100', NOW(), NOW()),
(gen_random_uuid(), 'Presion Arterial Sistolica', '90', '120', NOW(), NOW()),
(gen_random_uuid(), 'Presion Arterial Diastolica', '60', '80', NOW(), NOW()),
(gen_random_uuid(), 'IMC', '18.5', '24.9', NOW(), NOW())
ON CONFLICT (nombre) DO NOTHING;

-- Verificar que se insertaron los datos
SELECT id, nombre, valor_inferior, valor_superior FROM rangos ORDER BY nombre;
