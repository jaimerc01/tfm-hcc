import { ref } from 'vue'
import historiaClinicaService from '@/services/historiaClinicaService'
import { mapHistorialEntries, applyRangos } from '@/utils/datosClinicos'
import { DEFAULT_ANALYTES as BLOOD_ANALYTES } from '@/composables/useAnalisisSangre'
import { DEFAULT_ANALYTES as VITAL_ANALYTES } from '@/composables/useSignosVitales'

/**
 * Métricas del resumen "Anillos de salud". Son las cuatro que el público
 * general reconoce de una revisión médica. Los anillos en rango alternan azul
 * y verde (regla 60-30-10); el amarillo/ámbar queda reservado para "fuera de
 * rango".
 */
export const RESUMEN_METRICS = [
  { key: 'glucosa', labelKey: 'glucose', domain: 'sangre', colorToken: '--chart-series-blue' },
  { key: 'colesterol', labelKey: 'total_cholesterol', domain: 'sangre', colorToken: '--chart-series-green' },
  { key: 'Presion Arterial Sistolica', labelKey: 'blood_pressure_systolic', domain: 'signos', colorToken: '--chart-series-blue' },
  { key: 'IMC', labelKey: 'bmi', domain: 'signos', colorToken: '--chart-series-green' }
]

function latestByKey(entries, key) {
  return (entries || [])
    .filter(e => e && e.key === key && e.createdAt && e.value !== '' && e.value != null)
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0] || null
}

/**
 * Carga el último valor de cada métrica del resumen de salud junto con su
 * rango recomendado. No es un singleton: cada instancia tiene su propio estado.
 */
export function useResumenSalud() {
  const rings = ref([])
  const loading = ref(true)
  const loaded = ref(false)

  async function load() {
    loading.value = true

    const blood = BLOOD_ANALYTES.map(a => ({ ...a }))
    const vital = VITAL_ANALYTES.map(a => ({ ...a }))

    let dto = {}
    try {
      const res = await historiaClinicaService.getMine()
      dto = res.data || {}
    } catch (e) {
      dto = {}
    }

    try {
      const res = await historiaClinicaService.getRangos()
      const rangos = res.data || []
      applyRangos(blood, rangos)
      applyRangos(vital, rangos)
    } catch (e) {
      // Los rangos son opcionales: sin ellos el anillo se muestra "sin referencia".
    }

    const bloodEntries = mapHistorialEntries(dto.analisisSangre, blood)
    const vitalEntries = mapHistorialEntries(dto.signosVitales, vital)

    rings.value = RESUMEN_METRICS.map(m => {
      const analytes = m.domain === 'sangre' ? blood : vital
      const entries = m.domain === 'sangre' ? bloodEntries : vitalEntries
      const def = analytes.find(a => a.key === m.key) || {}
      const last = latestByKey(entries, m.key)
      const numeric = last ? Number(String(last.value).replace(',', '.')) : null

      return {
        key: m.key,
        labelKey: m.labelKey,
        colorToken: m.colorToken,
        unit: def.unit || (last && last.unit) || '',
        value: (numeric != null && Number.isFinite(numeric)) ? numeric : null,
        min: def.recommendedMin != null ? def.recommendedMin : null,
        max: def.recommendedMax != null ? def.recommendedMax : null,
        createdAt: last ? last.createdAt : null
      }
    })

    loading.value = false
    loaded.value = true
  }

  return { rings, loading, loaded, load }
}
