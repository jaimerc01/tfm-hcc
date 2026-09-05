import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const svc = vi.hoisted(() => ({ getMine: vi.fn(), getRangos: vi.fn() }))
vi.mock('@/services/historiaClinicaService', () => ({ default: svc }))

const setHighlight = vi.hoisted(() => vi.fn())
const drawHealthRingsChart = vi.hoisted(() => vi.fn(() => ({ setHighlight })))
vi.mock('@/composables/useChart', () => ({ useChart: () => ({ drawHealthRingsChart }) }))

import HealthRingsCard from '@/components/HealthRingsCard.vue'

beforeEach(() => {
  svc.getMine.mockResolvedValue({ data: {} })
  svc.getRangos.mockResolvedValue({ data: [] })
})

const withData = () => {
  svc.getMine.mockResolvedValue({
    data: {
      analisisSangre: [
        { tipo: 'Glucosa', valor: 92, fechaCreacion: '2026-03-01T00:00:00Z' },
        { tipo: 'Colesterol', valor: 250, fechaCreacion: '2026-03-01T00:00:00Z' }
      ],
      signosVitales: [
        { tipo: 'Presion Arterial Sistolica', valor: 118, fechaCreacion: '2026-03-01T00:00:00Z' },
        { tipo: 'IMC', valor: 21, fechaCreacion: '2026-03-01T00:00:00Z' }
      ]
    }
  })
  svc.getRangos.mockResolvedValue({
    data: [
      { nombre: 'Glucosa', valorInferiorNumerico: 70, valorSuperiorNumerico: 100 },
      { nombre: 'Colesterol', valorInferiorNumerico: 100, valorSuperiorNumerico: 200 },
      { nombre: 'Presion Arterial Sistolica', valorInferiorNumerico: 90, valorSuperiorNumerico: 120 },
      { nombre: 'IMC', valorInferiorNumerico: 18.5, valorSuperiorNumerico: 24.9 }
    ]
  })
}

const factory = async () => {
  const w = mount(HealthRingsCard)
  await flushPromises()
  await flushPromises()
  return w
}

describe('HealthRingsCard', () => {
  it('sin datos muestra el estado vacío y no dibuja', async () => {
    const w = await factory()
    expect(w.find('.chart-empty').text()).toContain('Todavía no hay valores')
    expect(drawHealthRingsChart).not.toHaveBeenCalled()
  })

  it('con datos renderiza una fila de leyenda por métrica', async () => {
    withData()
    const w = await factory()
    const items = w.findAll('.health-rings__legend-item')
    expect(items).toHaveLength(4)
    expect(items[0].text()).toContain('Glucosa')
    expect(items[0].text()).toContain('92')
  })

  it('marca la métrica fuera de rango y las que están en rango', async () => {
    withData()
    const w = await factory()
    expect(w.findAll('.health-rings__status--out')).toHaveLength(1)
    expect(w.findAll('.health-rings__status--in')).toHaveLength(3)
    expect(w.find('.health-rings__status--out').text()).toContain('Fuera de rango')
  })

  it('dibuja los anillos con el recuento "en rango" en el centro', async () => {
    withData()
    await factory()
    expect(drawHealthRingsChart).toHaveBeenCalled()
    const [, rings, options] = drawHealthRingsChart.mock.calls[0]
    expect(rings).toHaveLength(4)
    expect(options.centerPrimary).toBe('3/4')
  })

  it('al enfocar una fila de leyenda resalta su anillo', async () => {
    withData()
    const w = await factory()
    await w.findAll('.health-rings__legend-item')[1].trigger('mouseenter')
    expect(setHighlight).toHaveBeenCalledWith(1)
  })
})
