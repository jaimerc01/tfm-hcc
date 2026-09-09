import { describe, it, expect, vi, beforeEach } from 'vitest'

const svc = vi.hoisted(() => ({
  getMine: vi.fn(),
  getRangos: vi.fn(),
  añadirAnalisisOrina: vi.fn(() => Promise.resolve()),
  deleteDatoClinico: vi.fn(() => Promise.resolve())
}))
vi.mock('@/services/historiaClinicaService', () => ({ default: svc }))

import { useAnalisisOrina } from '@/composables/useAnalisisOrina'

beforeEach(() => {
  vi.clearAllMocks()
  svc.getMine.mockResolvedValue({ data: {} })
  svc.getRangos.mockResolvedValue({ data: [] })
})

describe('useAnalisisOrina', () => {
  it('expone el analito de pH de orina', () => {
    const { analytes } = useAnalisisOrina()
    expect(analytes.value[0]).toMatchObject({ key: 'PH Orina', unit: 'pH' })
  })

  it('load parsea analisisOrina (string JSON)', async () => {
    svc.getMine.mockResolvedValue({ data: { analisisOrina: JSON.stringify([{ tipo: 'PH Orina', valor: 6.5 }]) } })
    const { load, entries } = useAnalisisOrina()
    await load()
    expect(entries.value[0]).toMatchObject({ key: 'PH Orina', value: '6.5' })
  })

  it('load deja entries vacío si no es array', async () => {
    svc.getMine.mockResolvedValue({ data: { analisisOrina: '5' } })
    const { load, entries } = useAnalisisOrina()
    await load()
    expect(entries.value).toEqual([])
  })

  it('loadRangos aplica alias ("ph")', async () => {
    svc.getRangos.mockResolvedValue({ data: [{ nombre: 'ph', valorInferiorNumerico: 4.5, valorSuperiorNumerico: 8 }] })
    const { loadRangos, analytes } = useAnalisisOrina()
    await loadRangos()
    expect(analytes.value[0].recommendedMin).toBe(4.5)
  })

  it('addEntry / deleteEntryById / clearAllEntries', async () => {
    const c = useAnalisisOrina()
    await c.addEntry({ tipo: 'PH Orina', valor: 6 })
    expect(svc.añadirAnalisisOrina).toHaveBeenCalled()
    await c.deleteEntryById(2)
    expect(svc.deleteDatoClinico).toHaveBeenCalledWith(2)
    svc.getMine.mockResolvedValue({ data: { analisisOrina: [{ id: 9, key: 'PH Orina', value: 6 }] } })
    await c.load()
    await c.clearAllEntries()
    expect(svc.deleteDatoClinico).toHaveBeenCalledWith(9)
  })

  it('addEntry recarga y propaga error', async () => {
    svc.añadirAnalisisOrina.mockRejectedValueOnce(new Error('x'))
    const c = useAnalisisOrina()
    await expect(c.addEntry({})).rejects.toThrow('x')
  })

  it('deleteEntryById recarga y propaga si el borrado falla', async () => {
    svc.deleteDatoClinico.mockRejectedValueOnce(new Error('boom'))
    const c = useAnalisisOrina()
    await expect(c.deleteEntryById(3)).rejects.toThrow('boom')
    expect(svc.getMine).toHaveBeenCalled()
  })

  it('clearAllEntries sin entradas con id solo vacía la lista', async () => {
    const c = useAnalisisOrina()
    c.entries.value = [{ value: '1' }]
    await c.clearAllEntries()
    expect(c.entries.value).toEqual([])
    expect(svc.deleteDatoClinico).not.toHaveBeenCalled()
  })

  it('clearAllEntries recarga y propaga si un borrado falla', async () => {
    svc.getMine.mockResolvedValue({ data: { analisisOrina: [{ id: 1, key: 'PH Orina', value: 6 }] } })
    svc.deleteDatoClinico.mockRejectedValueOnce(new Error('nope'))
    const c = useAnalisisOrina()
    await c.load()
    await expect(c.clearAllEntries()).rejects.toThrow('nope')
    expect(c.saving.value).toBe(false)
  })
})
