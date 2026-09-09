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

// Aplica los rangos de referencia del servidor sobre una lista de definiciones de
// analitos, rellenando recommendedMin/recommendedMax por coincidencia de nombre.
export function applyRangos(analytes, rangos) {
  if (!Array.isArray(analytes) || !Array.isArray(rangos)) return analytes
  rangos.forEach(rango => {
    const nombreNormalizado = normalizarNombre(rango.nombre)
    const analyte = analytes.find(a => {
      const keyMatch = normalizarNombre(a.key) === nombreNormalizado
      const aliasMatch = (a.aliases || []).some(alias => normalizarNombre(alias) === nombreNormalizado)
      return keyMatch || aliasMatch
    })
    if (analyte && rango.valorInferiorNumerico !== null && rango.valorSuperiorNumerico !== null) {
      analyte.recommendedMin = rango.valorInferiorNumerico
      analyte.recommendedMax = rango.valorSuperiorNumerico
    }
  })
  return analytes
}

// Normaliza el array crudo de un dominio del historial clínico (analisisSangre,
// signosVitales o analisisOrina, que puede venir como string JSON) a la forma
// que consumen DatoClinicoChart y DatoClinicoResultsTable.
export function mapHistorialEntries(raw, analytes) {
  let parsed = raw
  if (typeof raw === 'string') {
    try {
      parsed = JSON.parse(raw)
    } catch (e) {
      return []
    }
  }
  if (!Array.isArray(parsed)) return []

  return parsed.map(p => {
    const mappedKey = p.key || mapTipoToKey(p.tipo, analytes)
    const analyte = (analytes || []).find(x => x.key === mappedKey)
    return {
      id: p.id || null,
      key: mappedKey || p.key || null,
      label: p.label || p.tipo || p.key || '',
      value: (p.value !== undefined && p.value !== null)
        ? String(p.value)
        : (p.valor !== undefined && p.valor !== null) ? String(p.valor) : '',
      unit: analyte ? analyte.unit : (p.unit || p.unidad || ''),
      createdAt: p.createdAt || p.fechaCreacion || null,
      rango: p.rango || null
    }
  })
}
