/**
 * Prepara los datos para su visualización en gráficos
 * @param {Array} entries - Lista de entradas con estructura { key, label, value, createdAt }
 * @param {string} paramKey - Clave del parámetro a filtrar (ej: 'glucosa')
 * @returns {Array} Array de { date, value } ordenado por fecha
 */
export function prepareChartData(entries, paramKey) {
  if (!entries || !Array.isArray(entries)) return []

  const param = (paramKey || '').toLowerCase()
  
  return entries
    .filter(e => {
      if (!e) return false
      if (e.key && String(e.key).toLowerCase() === param) return true
      return String(e.label || '').toLowerCase().includes(param)
    })
    .map(e => {
      const dateValue = e.createdAt ? new Date(e.createdAt) : null
      const numValue = (e.value !== undefined && e.value !== null)
        ? Number(String(e.value).replace(',', '.'))
        : (e.valor !== undefined && e.valor !== null)
          ? Number(String(e.valor).replace(',', '.'))
          : NaN

      return {
        date: dateValue,
        value: numValue
      }
    })
    .filter(d => d.date instanceof Date && !Number.isNaN(d.value))
    .sort((a, b) => a.date - b.date)
}

/**
 * Indica si un valor cae fuera del rango recomendado para su analito.
 * @param {number} value - Valor a comprobar
 * @param {number|null} min - Mínimo recomendado (null si no hay referencia)
 * @param {number|null} max - Máximo recomendado (null si no hay referencia)
 * @returns {boolean}
 */
export function isOutOfRecommendedRange(value, min, max) {
  if (min == null || max == null || value == null || Number.isNaN(value)) return false
  return value < min || value > max
}

/**
 * Indica si el usuario ha pedido reducir el movimiento (prefers-reduced-motion).
 * Los gráficos usan esto para saltarse las animaciones de entrada.
 * @returns {boolean}
 */
export function prefersReducedMotion() {
  return typeof window !== 'undefined' &&
    typeof window.matchMedia === 'function' &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

/**
 * Indica si conviene animar la entrada del gráfico. Falso si el usuario pide
 * reducir el movimiento o si la pestaña está oculta (en ese caso el temporizador
 * de animación se ralentiza y el gráfico podría quedarse a medio dibujar; mejor
 * pintar el estado final directamente).
 * @returns {boolean}
 */
export function shouldAnimateChart() {
  if (prefersReducedMotion()) return false
  return typeof document === 'undefined' || document.visibilityState !== 'hidden'
}
