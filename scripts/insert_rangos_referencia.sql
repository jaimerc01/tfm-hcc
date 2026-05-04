-- Script para insertar rangos de referencia médicos comunes
-- Este script añade rangos normales para análisis de sangre típicos

INSERT INTO rangos (id, nombre, valor_inferior, valor_superior, fecha_creacion, fecha_actualizacion) VALUES
(gen_random_uuid(), 'Glucosa', '70', '140', NOW(), NOW()),
(gen_random_uuid(), 'Hemoglobina', '12.0', '16.0', NOW(), NOW()),
(gen_random_uuid(), 'Colesterol total', '100', '200', NOW(), NOW()),
(gen_random_uuid(), 'Triglicéridos', '50', '150', NOW(), NOW()),
(gen_random_uuid(), 'Creatinina', '0.6', '1.3', NOW(), NOW()),
(gen_random_uuid(), 'Hematocrito', '35', '50', NOW(), NOW()),
(gen_random_uuid(), 'Colesterol', '100', '200', NOW(), NOW()),
(gen_random_uuid(), 'GLUCOSA', '70', '140', NOW(), NOW()),
(gen_random_uuid(), 'HEMOGLOBINA', '12.0', '16.0', NOW(), NOW()),
(gen_random_uuid(), 'COLESTEROL TOTAL', '100', '200', NOW(), NOW()),
(gen_random_uuid(), 'TRIGLICÉRIDOS', '50', '150', NOW(), NOW()),
(gen_random_uuid(), 'CREATININA', '0.6', '1.3', NOW(), NOW()),
(gen_random_uuid(), 'HEMATOCRITO', '35', '50', NOW(), NOW());

-- Verificar que se insertaron los datos
SELECT id, nombre, valor_inferior, valor_superior FROM rangos ORDER BY nombre;
