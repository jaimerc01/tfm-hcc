import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const svc = vi.hoisted(() => ({ getRangos: vi.fn() }))
vi.mock('@/services/historiaClinicaService', () => ({ default: svc }))

const setActiveEvent = vi.hoisted(() => vi.fn())
const draw = vi.hoisted(() => vi.fn(() => ({ setActiveEvent })))
vi.mock('@/composables/useChart', async (importActual) => {
  const actual = await importActual()
  return { useChart: () => ({ ...actual.useChart(), drawAnnotatedTimelineChart: draw }) }
})

import ClinicalTimelineCard from '@/components/ClinicalTimelineCard.vue'

beforeEach(() => {
  svc.getRangos.mockResolvedValue({ data: [] })
})

const antecedentes = [
  { id: 'a2', categoria: 'PERSONAL', descripcion: 'Colesterol alto con estatinas', createdAt: '2026-02-10T09:00:00Z' },
  { id: 'a1', categoria: 'FAMILIAR', descripcion: 'Padre con diabetes', createdAt: '2026-01-05T09:00:00Z' }
]

const factory = async (props = {}) => {
  const w = mount(ClinicalTimelineCard, {
    props: {
      analisisSangre: [{ tipo: 'Glucosa', valor: 95, fechaCreacion: '2026-01-10T00:00:00Z' }],
      signosVitales: [],
      analisisOrina: [],
      antecedentes,
      ...props
    }
  })
  await flushPromises()
  await flushPromises()
  return w
}

describe('ClinicalTimelineCard', () => {
  it('el selector solo ofrece parámetros con datos', async () => {
    const w = await factory()
    const options = w.findAll('#timeline-param-select option')
    expect(options).toHaveLength(1)
    expect(options[0].text()).toBe('Glucosa')
  })

  it('lista los eventos ordenados por fecha y numerados', async () => {
    const w = await factory()
    const rows = w.findAll('.clinical-timeline__event')
    expect(rows).toHaveLength(2)
    expect(rows[0].text()).toContain('Familiar')
    expect(rows[0].text()).toContain('Padre con diabetes')
    expect(rows[1].text()).toContain('Colesterol alto con estatinas')
  })

  it('sin antecedentes con fecha muestra el aviso', async () => {
    const w = await factory({ antecedentes: [] })
    expect(w.find('.clinical-timeline__events .empty-hint').text())
      .toContain('no tiene antecedentes registrados')
  })

  it('dibuja la línea de tiempo con serie, eventos y rango', async () => {
    svc.getRangos.mockResolvedValue({
      data: [{ nombre: 'Glucosa', valorInferiorNumerico: 70, valorSuperiorNumerico: 100 }]
    })
    await factory()
    expect(draw).toHaveBeenCalled()
    const [, series, events, options] = draw.mock.calls[draw.mock.calls.length - 1]
    expect(series).toHaveLength(1)
    expect(events).toHaveLength(2)
    expect(options).toMatchObject({ recommendedMin: 70, recommendedMax: 100, unit: 'mg/dL' })
  })

  it('al pasar el ratón por un evento lo resalta en el gráfico', async () => {
    const w = await factory()
    await w.findAll('.clinical-timeline__event')[0].trigger('mouseenter')
    expect(setActiveEvent).toHaveBeenCalledWith(0)
  })
})
