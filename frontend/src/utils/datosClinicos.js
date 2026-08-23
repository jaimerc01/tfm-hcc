// Funciones puras de apoyo para el dominio de análisis de sangre

export function normalizarNombre(nombre) {
  if (!nombre) return ''
  return String(nombre)
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .replace(/\s+/g, '')
}

// Mapea el 'tipo' devuelto por el servidor (p. ej. "Glucosa") a la key local del analito,
// normalizando acentos/mayúsculas/espacios y comparando contra key y aliases.
export function mapTipoToKey(tipo, analytes) {
  if (!tipo) return null
  const norm = normalizarNombre(tipo)
  const analyte = (analytes || []).find(a => {
    const keyMatch = normalizarNombre(a.key) === norm
    const aliasMatch = (a.aliases || []).some(alias => normalizarNombre(alias) === norm)
    return keyMatch || aliasMatch
  })
  return analyte ? analyte.key : norm
}

export function getAnalyteLabel(analyte, t) {
  if (!analyte) return ''
  return t(analyte.labelKey || analyte.key)
}

export function formatDate(iso) {
  if (!iso) return ''
  try {
    const d = new Date(iso)
    return d.toLocaleString()
  } catch (e) {
    return iso
  }
}
