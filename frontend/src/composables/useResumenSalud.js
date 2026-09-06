import { ref } from 'vue'
import historiaClinicaService from '@/services/historiaClinicaService'
import { mapHistorialEntries, applyRangos } from '@/utils/datosClinicos'
import { DEFAULT_ANALYTES as BLOOD_ANALYTES } from '@/composables/useAnalisisSangre'
import { DEFAULT_ANALYTES as VITAL_ANALYTES } from '@/composables/useSignosVitales'
import { DEFAULT_ANALYTES as URINE_ANALYTES } from '@/composables/useAnalisisOrina'

/**
 * Todos los parámetros que pueden ocupar un anillo de salud, con el dominio del
 * historial clínico al que pertenecen. El paciente elige cuáles de estos van en
 * cada uno de los cuatro huecos (ver HealthRingsCard).
 */
export const RESUMEN_METRICS = [
  ...BLOOD_ANALYTES.map(a => ({ key: a.key, labelKey: a.labelKey, domain: 'sangre' })),
  ...VITAL_ANALYTES.map(a => ({ key: a.key, labelKey: a.labelKey, domain: 'signos' })),
  ...URINE_ANALYTES.map(a => ({ key: a.key, labelKey: a.labelKey, domain: 'orina' }))
]

/**
 * Los cuatro anillos por defecto: los parámetros que el público general
 * reconoce de una revisión médica. El color de cada anillo (azul/verde
 * alternos, regla 60-30-10) lo pone su posición, no la métrica.
 */
export const DEFAULT_RING_KEYS = ['glucosa', 'colesterol', 'Presion Arterial Sistolica', 'IMC']

function latestByKey(entries, key) {
  return (entries || [])
    .filter(e => e && e.key === key && e.createdAt && e.value !== '' && e.value != null)
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0] || null
}

/**
 * Carga el último valor y el rango recomendado de cada parámetro clínico
 * rastreable, para alimentar los anillos de salud. No es un singleton: cada
 * instancia tiene su propio estado.
 */
export function useResumenSalud() {
  const metrics = ref([])
  const loading = ref(true)
  const loaded = ref(false)

  async function load() {
    loading.value = true

    const analytesByDomain = {
      sangre: BLOOD_ANALYTES.map(a => ({ ...a })),
      signos: VITAL_ANALYTES.map(a => ({ ...a })),
      orina: URINE_ANALYTES.map(a => ({ ...a }))
    }

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
      applyRangos(analytesByDomain.sangre, rangos)
      applyRangos(analytesByDomain.signos, rangos)
      applyRangos(analytesByDomain.orina, rangos)
    } catch (e) {
      // Los rangos son opcionales: sin ellos el anillo se muestra "sin referencia".
    }

    const entriesByDomain = {
      sangre: mapHistorialEntries(dto.analisisSangre, analytesByDomain.sangre),
      signos: mapHistorialEntries(dto.signosVitales, analytesByDomain.signos),
      orina: mapHistorialEntries(dto.analisisOrina, analytesByDomain.orina)
    }

    metrics.value = RESUMEN_METRICS.map(m => {
      const def = analytesByDomain[m.domain].find(a => a.key === m.key) || {}
      const last = latestByKey(entriesByDomain[m.domain], m.key)
      const numeric = last ? Number(String(last.value).replace(',', '.')) : null

      return {
        key: m.key,
        labelKey: m.labelKey,
        domain: m.domain,
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

  return { metrics, loading, loaded, load }
}
