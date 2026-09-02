import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const svc = vi.hoisted(() => ({
  getMine: vi.fn(),
  crearAntecedente: vi.fn(() => Promise.resolve()),
  editarAntecedente: vi.fn(() => Promise.resolve()),
  deleteAntecedente: vi.fn(() => Promise.resolve())
}))
vi.mock('@/services/historiaClinicaService', () => ({ default: svc }))

import AntecedentesSection from '@/components/AntecedentesSection.vue'

const ant = (over = {}) => ({ id: 1, categoria: 'PERSONAL', descripcion: 'Hipertensión', createdAt: '2026-01-01T00:00:00Z', ...over })

beforeEach(() => {
  vi.clearAllMocks()
  svc.getMine.mockResolvedValue({ data: { antecedentes: [] } })
})

const factory = async () => {
  const w = mount(AntecedentesSection)
  await flushPromises()
  return w
}

describe('AntecedentesSection', () => {
  it('empty state cuando no hay antecedentes', async () => {
    const w = await factory()
    expect(w.find('.empty-state-small').exists()).toBe(true)
  })

  it('agrupa por categoría PERSONAL y FAMILIAR', async () => {
    svc.getMine.mockResolvedValue({ data: { antecedentes: [ant(), ant({ id: 2, categoria: 'FAMILIAR', descripcion: 'Diabetes' })] } })
    const w = await factory()
    const titles = w.findAll('.section-title').map(t => t.text())
    expect(titles.join(' ')).toMatch(/[Pp]ersonal/)
    expect(titles.join(' ')).toMatch(/[Ff]amiliar/)
    expect(w.vm.groupedAntecedentes).toHaveLength(2)
  })

  it('crea un antecedente con la categoría seleccionada', async () => {
    const w = await factory()
    await w.find('#antecedente-categoria').setValue('FAMILIAR')
    await w.find('#antecedente-descripcion').setValue('  Cáncer  ')
    await w.find('form').trigger('submit')
    await flushPromises()
    expect(svc.crearAntecedente).toHaveBeenCalledWith({ categoria: 'FAMILIAR', descripcion: 'Cáncer' })
  })

  it('no crea si la descripción está vacía', async () => {
    const w = await factory()
    await w.find('form').trigger('submit')
    expect(svc.crearAntecedente).not.toHaveBeenCalled()
  })

  it('muestra error si la creación falla', async () => {
    svc.crearAntecedente.mockRejectedValueOnce(new Error('500'))
    const w = await factory()
    await w.find('#antecedente-descripcion').setValue('X')
    await w.find('form').trigger('submit')
    await flushPromises()
    expect(w.find('.alert-danger').exists()).toBe(true)
  })

  it('startEdit rellena los campos de edición y saveEdit persiste', async () => {
    svc.getMine.mockResolvedValue({ data: { antecedentes: [ant()] } })
    const w = await factory()
    w.vm.startEdit(ant())
    expect(w.vm.editingId).toBe(1)
    expect(w.vm.editDescripcion).toBe('Hipertensión')
    w.vm.editDescripcion = 'Hipertensión arterial'
    await w.vm.saveEdit(ant())
    await flushPromises()
    expect(svc.editarAntecedente).toHaveBeenCalledWith(1, { categoria: 'PERSONAL', descripcion: 'Hipertensión arterial' })
    expect(w.vm.editingId).toBeNull()
  })

  it('saveEdit no hace nada con descripción vacía', async () => {
    const w = await factory()
    w.vm.startEdit(ant())
    w.vm.editDescripcion = '   '
    await w.vm.saveEdit(ant())
    expect(svc.editarAntecedente).not.toHaveBeenCalled()
  })

  it('cancelEdit limpia el estado de edición', async () => {
    const w = await factory()
    w.vm.startEdit(ant())
    w.vm.cancelEdit()
    expect(w.vm.editingId).toBeNull()
    expect(w.vm.editDescripcion).toBe('')
  })

  it('confirmDelete borra por id y recarga', async () => {
    svc.getMine.mockResolvedValue({ data: { antecedentes: [ant()] } })
    const w = await factory()
    w.vm.openDelete(ant())
    expect(w.vm.showDelete).toBe(true)
    await w.vm.confirmDelete()
    await flushPromises()
    expect(svc.deleteAntecedente).toHaveBeenCalledWith(1)
    expect(w.vm.showDelete).toBe(false)
  })

  it('confirmDelete muestra error en el modal si falla', async () => {
    svc.deleteAntecedente.mockRejectedValueOnce(new Error('500'))
    const w = await factory()
    w.vm.openDelete(ant())
    await w.vm.confirmDelete()
    await flushPromises()
    expect(w.vm.deleteError).toBeTruthy()
  })

  it('muestra error si la carga inicial falla', async () => {
    svc.getMine.mockRejectedValueOnce(new Error('boom'))
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
    const w = await factory()
    expect(w.vm.error).toBeTruthy()
    spy.mockRestore()
  })

  it('formatDateTime maneja fecha nula e inválida', async () => {
    const w = await factory()
    expect(w.vm.formatDateTime(null)).toBe('')
    expect(w.vm.formatDateTime('nope')).toBe('nope')
  })
})
