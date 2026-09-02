import { ref } from 'vue'
import { normalizarNombre, mapTipoToKey } from '@/utils/datosClinicos'

export const DEFAULT_ANALYTES = [
  {
    key: 'glucosa',
    labelKey: 'glucose',
    // 'glicosa' cubre entradas antiguas guardadas con la etiqueta traducida al gallego
    aliases: ['glucosa', 'glucose', 'glicosa'],
    unit: 'mg/dL',
    // Rangos de validación del formulario (amplios)
    validationMin: 0,
    validationMax: 1000,
    decimals: 0,
    // Rangos recomendados (se actualizarán desde servidor)
    recommendedMin: null,
    recommendedMax: null
  },
  {
    key: 'hemoglobina',
    labelKey: 'hemoglobin',
    aliases: ['hemoglobina', 'hemoglobin'],
    unit: 'g/dL',
    validationMin: 0,
    validationMax: 25,
    decimals: 1,
    recommendedMin: null,
    recommendedMax: null
  },
  {
    key: 'colesterol',
    labelKey: 'total_cholesterol',
    aliases: ['colesterol', 'colesterol total', 'colesteroltotal', 'total cholesterol'],
    unit: 'mg/dL',
    validationMin: 0,
    validationMax: 500, // Aumentado para permitir valores altos como 250-300
    decimals: 0,
    recommendedMin: null,
    recommendedMax: null
  },
  {
    key: 'Colesterol LDL',
    labelKey: 'cholesterol_ldl',
    aliases: ['Colesterol LDL', 'colesterol ldl', 'ldl'],
    unit: 'mg/dL',
    validationMin: 0,
    validationMax: 400,
    decimals: 0,
    recommendedMin: null,
    recommendedMax: null
  },
  {
    key: 'Colesterol HDL',
    labelKey: 'cholesterol_hdl',
    aliases: ['Colesterol HDL', 'colesterol hdl', 'hdl'],
    unit: 'mg/dL',
    validationMin: 0,
    validationMax: 150,
    decimals: 0,
    recommendedMin: null,
    recommendedMax: null
  },
  {
    key: 'trigliceridos',
    labelKey: 'triglycerides',
    aliases: ['trigliceridos', 'triglicéridos', 'triglycerides'],
    unit: 'mg/dL',
    validationMin: 0,
    validationMax: 2000,
    decimals: 0,
    recommendedMin: null,
    recommendedMax: null
  },
  {
    key: 'creatinina',
    labelKey: 'creatinine',
    aliases: ['creatinina', 'creatinine'],
    unit: 'mg/dL',
    validationMin: 0,
    validationMax: 50,
    decimals: 2,
    recommendedMin: null,
    recommendedMax: null
  },
  {
    key: 'hematocrito',
    labelKey: 'hematocrit',
    aliases: ['hematocrito', 'hematocrit'],
    unit: '%',
    validationMin: 0,
    validationMax: 100,
    decimals: 1,
    recommendedMin: null,
    recommendedMax: null
  }
]

/**
 * Composable para gestionar los análisis de sangre del historial clínico.
 * Cada llamada crea una instancia de estado propia (no es un singleton como useAuth),
 * ya que analytes/entries se mutan con datos del servidor y no deben compartirse entre instancias.
 */
export function useAnalisisSangre() {
  const analytes = ref(DEFAULT_ANALYTES.map(a => ({ ...a })))
  const entries = ref([])
  const saving = ref(false)

  function updateAnalyteRanges(analyte, rangoData) {
    if (!rangoData) return

    if (rangoData.valorInferiorNumerico !== undefined && rangoData.valorInferiorNumerico !== null) {
      analyte.recommendedMin = rangoData.valorInferiorNumerico
    }
    if (rangoData.valorSuperiorNumerico !== undefined && rangoData.valorSuperiorNumerico !== null) {
      analyte.recommendedMax = rangoData.valorSuperiorNumerico
    }

    analyte.rangoData = rangoData
  }

  async function loadRangos() {
    const svc = await import('@/services/historiaClinicaService').then(m => m.default)
    const res = await svc.getRangos()
    const rangos = res.data || []

    rangos.forEach(rango => {
      const nombreNormalizado = normalizarNombre(rango.nombre)

      const analyte = analytes.value.find(a => {
        const keyNormalizado = normalizarNombre(a.key)
        const aliasMatch = (a.aliases || []).some(alias => normalizarNombre(alias) === nombreNormalizado)
        return keyNormalizado === nombreNormalizado || aliasMatch
      })

      if (analyte && rango.valorInferiorNumerico !== null && rango.valorSuperiorNumerico !== null) {
        analyte.recommendedMin = rango.valorInferiorNumerico
        analyte.recommendedMax = rango.valorSuperiorNumerico
      }
    })
  }

  async function load() {
    const svc = await import('@/services/historiaClinicaService').then(m => m.default)
    const res = await svc.getMine()
    const dto = res.data || {}

    if (!dto.analisisSangre) {
      entries.value = []
      return
    }

    try {
      const parsed = typeof dto.analisisSangre === 'string' ? JSON.parse(dto.analisisSangre) : dto.analisisSangre
      if (!Array.isArray(parsed)) {
        entries.value = []
        return
      }

      entries.value = parsed.map(p => {
        const mappedKey = p.key || mapTipoToKey(p.tipo, analytes.value)
        const analyte = analytes.value.find(x => x.key === mappedKey)

        if (p.rango && analyte) {
          updateAnalyteRanges(analyte, p.rango)
        }

        return {
          id: p.id || null,
          key: mappedKey || p.key || null,
          label: p.label || p.tipo || p.key || '',
          value: (p.value !== undefined && p.value !== null) ? String(p.value) : (p.valor !== undefined && p.valor !== null) ? String(p.valor) : '',
          unit: analyte ? analyte.unit : (p.unit || p.unidad || ''),
          createdAt: p.createdAt || p.fechaCreacion || null,
          rango: p.rango || null
        }
      })
    } catch (ignore) {
      entries.value = []
    }
  }

  async function addEntry(payload) {
    const svc = await import('@/services/historiaClinicaService').then(m => m.default)
    const body = [payload]
    saving.value = true
    try {
      await svc.añadirAnalisisSangre(body)
      await load()
    } catch (e) {
      await load()
      throw e
    } finally {
      saving.value = false
    }
  }

  async function deleteEntryById(id) {
    const svc = await import('@/services/historiaClinicaService').then(m => m.default)
    try {
      if (id) {
        await svc.deleteDatoClinico(id)
      }
      await load()
    } catch (e) {
      await load()
      throw e
    }
  }

  async function clearAllEntries() {
    const svc = await import('@/services/historiaClinicaService').then(m => m.default)
    saving.value = true
    try {
      const entriesToDelete = entries.value.filter(e => e && e.id)

      if (entriesToDelete.length === 0) {
        entries.value = []
        return
      }

      await Promise.all(entriesToDelete.map(e => svc.deleteDatoClinico(e.id)))

      entries.value = []
      await load()
    } catch (e) {
      await load()
      throw e
    } finally {
      saving.value = false
    }
  }

  return {
    analytes,
    entries,
    saving,
    load,
    loadRangos,
    addEntry,
    deleteEntryById,
    clearAllEntries
  }
}
