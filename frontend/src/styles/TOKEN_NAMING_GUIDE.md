# Guia De Naming De Tokens De Color

Objetivo: usar un sistema consistente para que cualquier color nuevo se defina en `variables.css` y se consuma por token, no por valor hex.

## Reglas Base

- Solo `src/styles/variables.css` puede contener colores hex.
- En componentes y vistas, usar siempre `var(--token)`.
- Priorizar tokens semanticos (que expresan intencion) sobre tokens de paleta cruda.

## Convencion Recomendada

Formato general: `--<categoria>-<uso>-<estado>`

Categorias actuales en el proyecto:

- `primary`, `success`, `danger`, `warning`, `secondary`
- `text`
- `bg`, `surface`
- `border`
- `alert`, `badge`
- `chart`
- `brand`

Estados habituales:

- `hover`, `active`, `light`, `soft-bg`

## Ejemplos

- Boton principal: `--primary-color`, hover `--primary-hover`
- Texto secundario: `--text-secondary`
- Fondo informativo: `--surface-info-bg`
- Alerta de error: `--alert-danger-bg`, `--alert-danger-text`
- Serie de grafico destacada: `--chart-emphasis`

## Cuando Crear Un Token Nuevo

Crea token si:

- El color representa una intencion reusable (error, exito, enlace, estado).
- El mismo valor aparece en mas de una pantalla.
- Es parte de una familia de estados (`default/hover/active`).

No crees token si:

- Es un uso unico y temporal (valorar si realmente debe existir).

## Flujo Recomendado

1. Definir token en `src/styles/variables.css`.
2. Reemplazar usos en CSS/Vue por `var(--token)`.
3. Si el color se usa desde JS (D3), leerlo con `getThemeColor('--token')`.
4. Ejecutar `npm run lint:colors` y `npm run lint`.

## Checklist Rapido

- [ ] No hay hex fuera de `variables.css`.
- [ ] Nombre del token expresa intencion.
- [ ] Si hay hover/active, existen ambos tokens.
- [ ] Pasa `npm run lint:colors`.
