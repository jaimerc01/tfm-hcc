import { describe, it, expect, vi, beforeEach } from 'vitest'

const svc = vi.hoisted(() => ({
  getMine: vi.fn(),
  getRangos: vi.fn()
}))
vi.mock('@/services/historiaClinicaService', () => ({ default: svc }))

import { useResumenSalud, RESUMEN_METRICS } from '@/composables/useResumenSalud'

beforeEach(() => {
  vi.clearAllMocks()
  svc.getMine.mockResolvedValue({ data: {} })
  svc.getRangos.mockResolvedValue({ data: [] })
})

describe('useResumenSalud', () => {
  it('expone las cuatro métricas del resumen', () => {
    expect(RESUMEN_METRICS.map(m => m.key)).toEqual([
      'glucosa', 'colesterol', 'Presion Arterial Sistolica', 'IMC'
    ])
  })

  it('sin datos: cada anillo queda sin valor y sin rango', async () => {
    const { rings, load, loaded } = useResumenSalud()
    await load()
    expect(loaded.value).toBe(true)
    expect(rings.value).toHaveLength(4)
    expect(rings.value.every(r => r.value === null)).toBe(true)
  })

  it('toma el último valor de cada métrica y le aplica su rango', async () => {
    svc.getMine.mockResolvedValue({
      data: {
        analisisSangre: [
          { tipo: 'Glucosa', valor: 88, fechaCreacion: '2026-01-01T00:00:00Z' },
          { tipo: 'Glucosa', valor: 140, fechaCreacion: '2026-03-01T00:00:00Z' }
        ],
        signosVitales: [
          { tipo: 'IMC', valor: 22.5, fechaCreacion: '2026-02-01T00:00:00Z' }
        ]
      }
    })
    svc.getRangos.mockResolvedValue({
      data: [
        { nombre: 'Glucosa', valorInferiorNumerico: 70, valorSuperiorNumerico: 100 },
        { nombre: 'IMC', valorInferiorNumerico: 18.5, valorSuperiorNumerico: 24.9 }
      ]
    })
    const { rings, load } = useResumenSalud()
    await load()

    const glucosa = rings.value.find(r => r.key === 'glucosa')
    expect(glucosa).toMatchObject({ value: 140, min: 70, max: 100, unit: 'mg/dL' })

    const imc = rings.value.find(r => r.key === 'IMC')
    expect(imc).toMatchObject({ value: 22.5, min: 18.5, max: 24.9 })
  })

  it('no rompe si getMine falla', async () => {
    svc.getMine.mockRejectedValueOnce(new Error('boom'))
    const { rings, load } = useResumenSalud()
    await load()
    expect(rings.value).toHaveLength(4)
  })
})
