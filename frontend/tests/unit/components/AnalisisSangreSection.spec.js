import { vi } from 'vitest'
import { ref } from 'vue'
import { runSectionContract } from '../../helpers/datoClinicoSectionContract'

const composable = vi.hoisted(() => {
  return {
    analytes: { value: [{ key: 'glucosa', labelKey: 'glucose', unit: 'mg/dL', recommendedMin: null, recommendedMax: null }] },
    entries: { value: [] },
    saving: { value: false },
    load: vi.fn(),
    loadRangos: vi.fn(),
    addEntry: vi.fn(),
    deleteEntryById: vi.fn(),
    clearAllEntries: vi.fn()
  }
})
vi.mock('@/composables/useAnalisisSangre', () => ({ useAnalisisSangre: () => composable }))

composable.analytes = ref(composable.analytes.value)
composable.entries = ref(composable.entries.value)
composable.saving = ref(false)

runSectionContract({
  name: 'AnalisisSangreSection',
  loadComponent: () => import('@/components/AnalisisSangreSection.vue').then(m => m.default),
  composable
})
