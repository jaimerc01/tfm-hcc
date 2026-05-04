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
