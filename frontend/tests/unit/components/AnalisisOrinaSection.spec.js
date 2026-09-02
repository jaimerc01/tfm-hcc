import { vi } from 'vitest'
import { ref } from 'vue'
import { runSectionContract } from '../../helpers/datoClinicoSectionContract'

const composable = vi.hoisted(() => ({
  analytes: { value: [{ key: 'PH Orina', labelKey: 'urine_ph', unit: 'pH', recommendedMin: null, recommendedMax: null }] },
  entries: { value: [] },
  saving: { value: false },
  load: vi.fn(),
  loadRangos: vi.fn(),
  addEntry: vi.fn(),
  deleteEntryById: vi.fn(),
  clearAllEntries: vi.fn()
}))
vi.mock('@/composables/useAnalisisOrina', () => ({ useAnalisisOrina: () => composable }))

composable.analytes = ref(composable.analytes.value)
composable.entries = ref(composable.entries.value)
composable.saving = ref(false)

runSectionContract({
  name: 'AnalisisOrinaSection',
  loadComponent: () => import('@/components/AnalisisOrinaSection.vue').then(m => m.default),
  composable
})
