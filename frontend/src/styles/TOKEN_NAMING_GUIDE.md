# Guia De Naming De Tokens De Color

Objetivo: usar un sistema consistente para que cualquier color nuevo se defina en `variables.css` y se consuma por token, no por valor hex.

## Reglas Base

- Solo `src/styles/variables.css` puede contener colores hex.
- En componentes y vistas, usar siempre `var(--token)`.
- Priorizar tokens semanticos (que expresan intencion) sobre tokens de paleta cruda.

## Regla 60-30-10

El reparto visual de color sigue la regla 60-30-10, por FUNCION:

- **Azul (`--primary-*`) ~60% — el marco/estructura**: navbar, footer (`--footer-bg`
  apunta a azul), cabeceras de pagina y sus iconos (`--gradient-primary`), iconos de
  seccion, pestañas de navegacion, barras de acento lateral, foco, serie principal de
  graficos.
- **Verde (`--button-color` / `--secondary-*`) ~30% — las acciones**: TODOS los botones
  son verdes solidos, incluidos los destructivos (Eliminar, Cerrar sesion) — se
  distinguen por texto e icono, no por color. Botones secundarios = mismo verde en
  outline. `--button-color` (#2e7d46) es un verde oscuro accesible (texto blanco
  >= 4.5:1); `--secondary-color` (#7DBA84) es mas claro y solo vale para tintes, bordes
  y textos de estado. Serie de enfasis de graficos = `--chart-emphasis` (verde).
- **Amarillo (`--tertiary-*` y `--danger-*`) ~10% — solo avisos y errores**: alertas,
  badges, errores de formulario, texto `.error`, iconos de advertencia, toasts de error,
  contador de notificaciones. Nunca en botones. Nunca decorativo. La familia `--danger-*`
  es una rampa de oro oscuro con contraste AA; la app no usa rojo.

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
