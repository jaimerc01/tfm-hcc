import { vi } from 'vitest'
import { ref } from 'vue'
import { runSectionContract } from '../../helpers/datoClinicoSectionContract'

const composable = vi.hoisted(() => ({
  analytes: { value: [{ key: 'IMC', labelKey: 'bmi', unit: 'kg/m²', recommendedMin: null, recommendedMax: null }] },
  entries: { value: [] },
  saving: { value: false },
  load: vi.fn(),
  loadRangos: vi.fn(),
  addEntry: vi.fn(),
  deleteEntryById: vi.fn(),
  clearAllEntries: vi.fn()
}))
vi.mock('@/composables/useSignosVitales', () => ({ useSignosVitales: () => composable }))

composable.analytes = ref(composable.analytes.value)
composable.entries = ref(composable.entries.value)
composable.saving = ref(false)

runSectionContract({
  name: 'SignosVitalesSection',
  loadComponent: () => import('@/components/SignosVitalesSection.vue').then(m => m.default),
  composable
})
