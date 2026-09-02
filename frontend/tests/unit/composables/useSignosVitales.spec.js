import { describe, it, expect, vi, beforeEach } from 'vitest'

const svc = vi.hoisted(() => ({
  getMine: vi.fn(),
  getRangos: vi.fn(),
  añadirSignosVitales: vi.fn(() => Promise.resolve()),
  deleteDatoClinico: vi.fn(() => Promise.resolve())
}))
vi.mock('@/services/historiaClinicaService', () => ({ default: svc }))

import { useSignosVitales } from '@/composables/useSignosVitales'

beforeEach(() => {
  vi.clearAllMocks()
  svc.getMine.mockResolvedValue({ data: {} })
  svc.getRangos.mockResolvedValue({ data: [] })
})

describe('useSignosVitales', () => {
  it('expone los analitos por defecto de signos vitales', () => {
    const { analytes } = useSignosVitales()
    expect(analytes.value.map(a => a.key)).toContain('Frecuencia Cardiaca')
    expect(analytes.value.map(a => a.key)).toContain('IMC')
  })

  it('load parsea signosVitales y mapea el tipo', async () => {
    svc.getMine.mockResolvedValue({ data: { signosVitales: [{ tipo: 'Frecuencia Cardiaca', valor: 70 }] } })
    const { load, entries } = useSignosVitales()
    await load()
    expect(entries.value[0]).toMatchObject({ key: 'Frecuencia Cardiaca', value: '70', unit: 'lpm' })
  })

  it('load deja entries vacío sin signosVitales', async () => {
    const { load, entries } = useSignosVitales()
    await load()
    expect(entries.value).toEqual([])
  })

  it('loadRangos actualiza los rangos recomendados', async () => {
    svc.getRangos.mockResolvedValue({ data: [{ nombre: 'IMC', valorInferiorNumerico: 18.5, valorSuperiorNumerico: 25 }] })
    const { loadRangos, analytes } = useSignosVitales()
    await loadRangos()
    expect(analytes.value.find(a => a.key === 'IMC').recommendedMax).toBe(25)
  })

  it('addEntry envía y recarga; deleteEntryById borra por id', async () => {
    const c = useSignosVitales()
    await c.addEntry({ tipo: 'IMC', valor: 22 })
    expect(svc.añadirSignosVitales).toHaveBeenCalledWith([{ tipo: 'IMC', valor: 22 }])
    await c.deleteEntryById(4)
    expect(svc.deleteDatoClinico).toHaveBeenCalledWith(4)
  })

  it('clearAllEntries borra las entradas con id', async () => {
    svc.getMine.mockResolvedValue({ data: { signosVitales: [{ id: 1, key: 'IMC', value: 22 }] } })
    const c = useSignosVitales()
    await c.load()
    await c.clearAllEntries()
    expect(svc.deleteDatoClinico).toHaveBeenCalledWith(1)
  })

  it('addEntry recarga y propaga error; deleteEntryById también', async () => {
    svc.añadirSignosVitales.mockRejectedValueOnce(new Error('a'))
    const c = useSignosVitales()
    await expect(c.addEntry({})).rejects.toThrow('a')
    expect(c.saving.value).toBe(false)
    svc.deleteDatoClinico.mockRejectedValueOnce(new Error('b'))
    await expect(c.deleteEntryById(5)).rejects.toThrow('b')
  })

  it('clearAllEntries sin entradas con id solo vacía la lista', async () => {
    const c = useSignosVitales()
    c.entries.value = [{ value: '1' }]
    await c.clearAllEntries()
    expect(c.entries.value).toEqual([])
  })

  it('clearAllEntries propaga si un borrado falla', async () => {
    svc.getMine.mockResolvedValue({ data: { signosVitales: [{ id: 9, key: 'IMC', value: 22 }] } })
    svc.deleteDatoClinico.mockRejectedValueOnce(new Error('nope'))
    const c = useSignosVitales()
    await c.load()
    await expect(c.clearAllEntries()).rejects.toThrow('nope')
  })

  it('load ignora signosVitales con JSON corrupto', async () => {
    svc.getMine.mockResolvedValue({ data: { signosVitales: '[[[' } })
    const c = useSignosVitales()
    await c.load()
    expect(c.entries.value).toEqual([])
  })
})
