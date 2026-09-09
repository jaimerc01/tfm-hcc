import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const historia = vi.hoisted(() => ({ getRangos: vi.fn() }))
vi.mock('@/services/historiaClinicaService', () => ({ default: historia }))
vi.mock('@/components/DatoClinicoChart.vue', () => ({
  default: { name: 'DatoClinicoChart', props: ['entries', 'analytes'], template: '<div class="chart-stub" :data-count="entries.length" />' }
}))

import PatientClinicalCharts from '@/components/PatientClinicalCharts.vue'

beforeEach(() => {
  vi.clearAllMocks()
  historia.getRangos.mockResolvedValue({ data: [] })
})

const factory = async (props = {}) => {
  const w = mount(PatientClinicalCharts, { props })
  await flushPromises()
  return w
}

describe('PatientClinicalCharts', () => {
  it('muestra el estado vacío cuando no hay datos en ningún dominio', async () => {
    const w = await factory()
    expect(w.find('.empty-hint').exists()).toBe(true)
    expect(w.find('.chart-stub').exists()).toBe(false)
  })

  it('genera una pestaña por dominio con datos y renderiza su gráfica', async () => {
    const w = await factory({
      analisisSangre: [{ key: 'glucosa', value: 95, fechaCreacion: '2026-01-01T00:00:00Z' }],
      signosVitales: [],
      analisisOrina: '[]'
    })
    expect(w.findAll('.patient-charts__tab')).toHaveLength(1)
    expect(w.find('.chart-stub').attributes('data-count')).toBe('1')
  })

  it('acepta los dominios como string JSON', async () => {
    const w = await factory({
      analisisSangre: '[{"key":"glucosa","value":80}]'
    })
    expect(w.findAll('.patient-charts__tab')).toHaveLength(1)
  })

  it('pide los rangos de referencia al crearse y no rompe si fallan', async () => {
    historia.getRangos.mockRejectedValueOnce(new Error('boom'))
    const w = await factory({ analisisSangre: [{ key: 'glucosa', value: 95 }] })
    expect(historia.getRangos).toHaveBeenCalled()
    expect(w.find('.chart-stub').exists()).toBe(true)
  })

  it('cambia de pestaña activa al pulsar otra', async () => {
    const w = await factory({
      analisisSangre: [{ key: 'glucosa', value: 95 }],
      signosVitales: [{ key: 'IMC', value: 22 }]
    })
    const tabs = w.findAll('.patient-charts__tab')
    expect(tabs).toHaveLength(2)
    await tabs[1].trigger('click')
    expect(w.vm.dominioActivo).toBe('signos')
  })
})
