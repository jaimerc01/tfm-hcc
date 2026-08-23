import { ref } from 'vue'
import { normalizarNombre, mapTipoToKey } from '@/utils/datosClinicos'

const DEFAULT_ANALYTES = [
  {
    key: 'Frecuencia Cardiaca',
    labelKey: 'heart_rate',
    aliases: ['Frecuencia Cardiaca', 'frecuencia cardiaca', 'frecuencia cardíaca', 'heart rate'],
    unit: 'lpm',
    validationMin: 0,
    validationMax: 300,
    decimals: 0,
    recommendedMin: null,
    recommendedMax: null
  },
  {
    key: 'Presion Arterial Sistolica',
    labelKey: 'blood_pressure_systolic',
    aliases: ['Presion Arterial Sistolica', 'presion arterial sistolica', 'presión arterial sistólica'],
    unit: 'mmHg',
    validationMin: 0,
    validationMax: 300,
    decimals: 0,
    recommendedMin: null,
    recommendedMax: null
  },
  {
    key: 'Presion Arterial Diastolica',
    labelKey: 'blood_pressure_diastolic',
    aliases: ['Presion Arterial Diastolica', 'presion arterial diastolica', 'presión arterial diastólica'],
    unit: 'mmHg',
    validationMin: 0,
    validationMax: 200,
    decimals: 0,
    recommendedMin: null,
    recommendedMax: null
  },
  {
    key: 'IMC',
    labelKey: 'bmi',
    aliases: ['IMC', 'imc', 'índice de masa corporal'],
    unit: 'kg/m²',
    validationMin: 0,
    validationMax: 100,
    decimals: 1,
    recommendedMin: null,
    recommendedMax: null
  }
]

/**
 * Composable para gestionar los signos vitales del historial clínico
 * (frecuencia cardíaca, presión arterial, IMC).
 * Cada llamada crea una instancia de estado propia, igual que useAnalisisSangre.
 */
export function useSignosVitales() {
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

    if (!dto.signosVitales) {
      entries.value = []
      return
    }

    try {
      const parsed = typeof dto.signosVitales === 'string' ? JSON.parse(dto.signosVitales) : dto.signosVitales
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
    const body = JSON.stringify([payload])
    saving.value = true
    try {
      await svc.añadirSignosVitales(body)
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
