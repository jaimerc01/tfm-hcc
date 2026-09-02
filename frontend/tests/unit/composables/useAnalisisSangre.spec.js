import { describe, it, expect, vi, beforeEach } from 'vitest'

const svc = vi.hoisted(() => ({
  getMine: vi.fn(),
  getRangos: vi.fn(),
  añadirAnalisisSangre: vi.fn(() => Promise.resolve()),
  deleteDatoClinico: vi.fn(() => Promise.resolve())
}))
vi.mock('@/services/historiaClinicaService', () => ({ default: svc }))

import { useAnalisisSangre } from '@/composables/useAnalisisSangre'

beforeEach(() => {
  vi.clearAllMocks()
  svc.getMine.mockResolvedValue({ data: {} })
  svc.getRangos.mockResolvedValue({ data: [] })
})

describe('useAnalisisSangre.load', () => {
  it('deja entries vacío si el DTO no trae analisisSangre', async () => {
    const { load, entries } = useAnalisisSangre()
    await load()
    expect(entries.value).toEqual([])
  })

  it('parsea analisisSangre como string JSON y mapea tipo -> key', async () => {
    svc.getMine.mockResolvedValue({
      data: { analisisSangre: JSON.stringify([{ tipo: 'Glucosa', valor: 95, fechaCreacion: '2026-01-01T00:00:00Z' }]) }
    })
    const { load, entries } = useAnalisisSangre()
    await load()
    expect(entries.value).toHaveLength(1)
    expect(entries.value[0]).toMatchObject({ key: 'glucosa', value: '95', unit: 'mg/dL' })
  })

  it('acepta analisisSangre como array ya parseado', async () => {
    svc.getMine.mockResolvedValue({ data: { analisisSangre: [{ key: 'hemoglobina', value: 14 }] } })
    const { load, entries } = useAnalisisSangre()
    await load()
    expect(entries.value[0]).toMatchObject({ key: 'hemoglobina', value: '14', unit: 'g/dL' })
  })

  it('deja entries vacío si el JSON no es un array', async () => {
    svc.getMine.mockResolvedValue({ data: { analisisSangre: '{"x":1}' } })
    const { load, entries } = useAnalisisSangre()
    await load()
    expect(entries.value).toEqual([])
  })

  it('deja entries vacío si el JSON está corrupto', async () => {
    svc.getMine.mockResolvedValue({ data: { analisisSangre: '[[[' } })
    const { load, entries } = useAnalisisSangre()
    await load()
    expect(entries.value).toEqual([])
  })

  it('aplica el rango embebido en la entrada al analito', async () => {
    svc.getMine.mockResolvedValue({
      data: { analisisSangre: [{ key: 'glucosa', value: 90, rango: { valorInferiorNumerico: 70, valorSuperiorNumerico: 110 } }] }
    })
    const { load, analytes } = useAnalisisSangre()
    await load()
    const glu = analytes.value.find(a => a.key === 'glucosa')
    expect(glu.recommendedMin).toBe(70)
    expect(glu.recommendedMax).toBe(110)
  })
})

describe('useAnalisisSangre.loadRangos', () => {
  it('actualiza recommendedMin/Max por nombre normalizado o alias', async () => {
    svc.getRangos.mockResolvedValue({
      data: [{ nombre: 'Glucosa', valorInferiorNumerico: 70, valorSuperiorNumerico: 100 }]
    })
    const { loadRangos, analytes } = useAnalisisSangre()
    await loadRangos()
    const glu = analytes.value.find(a => a.key === 'glucosa')
    expect(glu.recommendedMin).toBe(70)
    expect(glu.recommendedMax).toBe(100)
  })

  it('ignora rangos con valores null', async () => {
    svc.getRangos.mockResolvedValue({ data: [{ nombre: 'Glucosa', valorInferiorNumerico: null, valorSuperiorNumerico: null }] })
    const { loadRangos, analytes } = useAnalisisSangre()
    await loadRangos()
    expect(analytes.value.find(a => a.key === 'glucosa').recommendedMin).toBeNull()
  })
})

describe('useAnalisisSangre.addEntry', () => {
  it('envía la medición y recarga', async () => {
    const { addEntry, saving } = useAnalisisSangre()
    await addEntry({ tipo: 'Glucosa', valor: 100 })
    expect(svc.añadirAnalisisSangre).toHaveBeenCalledWith([{ tipo: 'Glucosa', valor: 100 }])
    expect(svc.getMine).toHaveBeenCalled()
    expect(saving.value).toBe(false)
  })

  it('recarga y propaga el error si el guardado falla', async () => {
    svc.añadirAnalisisSangre.mockRejectedValueOnce(new Error('500'))
    const { addEntry, saving } = useAnalisisSangre()
    await expect(addEntry({})).rejects.toThrow('500')
    expect(svc.getMine).toHaveBeenCalled()
    expect(saving.value).toBe(false)
  })
})

describe('useAnalisisSangre.deleteEntryById', () => {
  it('borra por id y recarga', async () => {
    const { deleteEntryById } = useAnalisisSangre()
    await deleteEntryById(7)
    expect(svc.deleteDatoClinico).toHaveBeenCalledWith(7)
    expect(svc.getMine).toHaveBeenCalled()
  })

  it('sin id solo recarga', async () => {
    const { deleteEntryById } = useAnalisisSangre()
    await deleteEntryById(null)
    expect(svc.deleteDatoClinico).not.toHaveBeenCalled()
    expect(svc.getMine).toHaveBeenCalled()
  })
})

describe('useAnalisisSangre.clearAllEntries', () => {
  it('sin entradas con id, solo vacía la lista', async () => {
    const { clearAllEntries, entries } = useAnalisisSangre()
    entries.value = [{ value: '1' }]
    await clearAllEntries()
    expect(entries.value).toEqual([])
    expect(svc.deleteDatoClinico).not.toHaveBeenCalled()
  })

  it('borra en paralelo todas las entradas con id y recarga', async () => {
    svc.getMine.mockResolvedValue({ data: { analisisSangre: [{ id: 1, key: 'glucosa', value: 90 }, { id: 2, key: 'glucosa', value: 95 }] } })
    const c = useAnalisisSangre()
    await c.load()
    await c.clearAllEntries()
    expect(svc.deleteDatoClinico).toHaveBeenCalledTimes(2)
  })
})
