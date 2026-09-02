import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const svc = vi.hoisted(() => ({
  getMine: vi.fn(),
  crearAlergia: vi.fn(() => Promise.resolve()),
  deleteAlergia: vi.fn(() => Promise.resolve())
}))
vi.mock('@/services/historiaClinicaService', () => ({ default: svc }))

import AlergiasSection from '@/components/AlergiasSection.vue'

beforeEach(() => {
  vi.clearAllMocks()
  svc.getMine.mockResolvedValue({ data: { alergias: [] } })
})

const factory = () => mount(AlergiasSection)

describe('AlergiasSection', () => {
  it('carga la lista de alergias al crearse', async () => {
    svc.getMine.mockResolvedValue({ data: { alergias: [{ id: 1, descripcion: 'Polen', createdAt: '2026-01-01T00:00:00Z' }] } })
    const w = factory()
    await flushPromises()
    expect(w.text()).toContain('Polen')
    expect(w.find('.badge-info').text()).toBe('1')
  })

  it('muestra el empty state si no hay alergias', async () => {
    const w = factory()
    await flushPromises()
    expect(w.find('.empty-state-small').exists()).toBe(true)
  })

  it('muestra un error si la carga falla', async () => {
    svc.getMine.mockRejectedValueOnce(new Error('boom'))
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
    const w = factory()
    await flushPromises()
    expect(w.find('.alert-danger').exists()).toBe(true)
    spy.mockRestore()
  })

  it('no envía si la descripción está vacía', async () => {
    const w = factory()
    await flushPromises()
    await w.find('form').trigger('submit')
    expect(svc.crearAlergia).not.toHaveBeenCalled()
  })

  it('crea una alergia, limpia el input y muestra mensaje de éxito', async () => {
    vi.useFakeTimers()
    const w = factory()
    await flushPromises()
    await w.find('#alergia-input').setValue('  Frutos secos  ')
    await w.find('form').trigger('submit')
    await flushPromises()
    expect(svc.crearAlergia).toHaveBeenCalledWith({ descripcion: 'Frutos secos' })
    expect(w.vm.descripcion).toBe('')
    expect(w.vm.msg).toBeTruthy()
    vi.advanceTimersByTime(3000)
    expect(w.vm.msg).toBe('')
    vi.useRealTimers()
  })

  it('muestra error si la creación falla', async () => {
    svc.crearAlergia.mockRejectedValueOnce(new Error('500'))
    const w = factory()
    await flushPromises()
    await w.find('#alergia-input').setValue('X')
    await w.find('form').trigger('submit')
    await flushPromises()
    expect(w.find('.alert-danger').exists()).toBe(true)
  })

  it('abre y cierra el modal de borrado', async () => {
    svc.getMine.mockResolvedValue({ data: { alergias: [{ id: 5, descripcion: 'Polen' }] } })
    const w = factory()
    await flushPromises()
    await w.find('.btn-danger').trigger('click')
    expect(w.vm.showDelete).toBe(true)
    expect(w.find('[role="dialog"]').exists()).toBe(true)
    await w.find('.btn-secondary').trigger('click')
    expect(w.vm.showDelete).toBe(false)
  })

  it('confirma el borrado y recarga', async () => {
    svc.getMine.mockResolvedValue({ data: { alergias: [{ id: 5, descripcion: 'Polen' }] } })
    const w = factory()
    await flushPromises()
    await w.find('.btn-danger').trigger('click')
    await w.find('[role="dialog"] .btn-primary').trigger('click')
    await flushPromises()
    expect(svc.deleteAlergia).toHaveBeenCalledWith(5)
    expect(w.vm.showDelete).toBe(false)
  })

  it('muestra error en el modal si el borrado falla', async () => {
    svc.getMine.mockResolvedValue({ data: { alergias: [{ id: 5, descripcion: 'Polen' }] } })
    svc.deleteAlergia.mockRejectedValueOnce(new Error('500'))
    const w = factory()
    await flushPromises()
    await w.find('.btn-danger').trigger('click')
    await w.find('[role="dialog"] .btn-primary').trigger('click')
    await flushPromises()
    expect(w.vm.deleteError).toBeTruthy()
  })

  it('formatDateTime devuelve "" sin fecha y el original si es inválida', () => {
    const w = factory()
    expect(w.vm.formatDateTime(null)).toBe('')
    expect(w.vm.formatDateTime('no-fecha')).toBe('no-fecha')
  })
})
