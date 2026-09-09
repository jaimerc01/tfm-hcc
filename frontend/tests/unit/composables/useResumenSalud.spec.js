import { describe, it, expect, vi, beforeEach } from 'vitest'

const svc = vi.hoisted(() => ({
  getMine: vi.fn(),
  getRangos: vi.fn()
}))
vi.mock('@/services/historiaClinicaService', () => ({ default: svc }))

import { useResumenSalud, RESUMEN_METRICS, DEFAULT_RING_KEYS } from '@/composables/useResumenSalud'

beforeEach(() => {
  vi.clearAllMocks()
  svc.getMine.mockResolvedValue({ data: {} })
  svc.getRangos.mockResolvedValue({ data: [] })
})

describe('useResumenSalud', () => {
  it('cataloga todos los parámetros rastreables con su dominio', () => {
    const keys = RESUMEN_METRICS.map(m => m.key)
    expect(keys).toContain('glucosa')
    expect(keys).toContain('Frecuencia Cardiaca')
    expect(keys).toContain('PH Orina')
    expect(RESUMEN_METRICS.every(m => ['sangre', 'signos', 'orina'].includes(m.domain))).toBe(true)
  })

  it('los cuatro anillos por defecto son los de una revisión básica', () => {
    expect(DEFAULT_RING_KEYS).toEqual(['glucosa', 'colesterol', 'Presion Arterial Sistolica', 'IMC'])
  })

  it('sin datos: cada métrica queda sin valor y sin rango', async () => {
    const { metrics, load, loaded } = useResumenSalud()
    await load()
    expect(loaded.value).toBe(true)
    expect(metrics.value).toHaveLength(RESUMEN_METRICS.length)
    expect(metrics.value.every(m => m.value === null)).toBe(true)
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
        ],
        analisisOrina: [
          { tipo: 'PH Orina', valor: 6.2, fechaCreacion: '2026-02-01T00:00:00Z' }
        ]
      }
    })
    svc.getRangos.mockResolvedValue({
      data: [
        { nombre: 'Glucosa', valorInferiorNumerico: 70, valorSuperiorNumerico: 100 },
        { nombre: 'IMC', valorInferiorNumerico: 18.5, valorSuperiorNumerico: 24.9 }
      ]
    })
    const { metrics, load } = useResumenSalud()
    await load()

    expect(metrics.value.find(m => m.key === 'glucosa')).toMatchObject({ value: 140, min: 70, max: 100, unit: 'mg/dL' })
    expect(metrics.value.find(m => m.key === 'IMC')).toMatchObject({ value: 22.5, min: 18.5, max: 24.9 })
    expect(metrics.value.find(m => m.key === 'PH Orina')).toMatchObject({ value: 6.2 })
  })

  it('no rompe si getMine falla', async () => {
    svc.getMine.mockRejectedValueOnce(new Error('boom'))
    const { metrics, load } = useResumenSalud()
    await load()
    expect(metrics.value).toHaveLength(RESUMEN_METRICS.length)
  })
})
