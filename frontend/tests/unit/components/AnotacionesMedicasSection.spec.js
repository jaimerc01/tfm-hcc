import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const authService = vi.hoisted(() => ({ listarMisAnotaciones: vi.fn() }))
vi.mock('@/services/authService', () => ({ default: authService }))

import AnotacionesMedicasSection from '@/components/AnotacionesMedicasSection.vue'

const anotacion = (over = {}) => ({
  id: 'a1',
  medicoNif: '11111111H',
  medicoNombre: 'Dra. Ana López',
  mensaje: 'Revisar la tensión',
  createdAt: '2026-01-10T09:00:00Z',
  ...over
})

beforeEach(() => {
  vi.clearAllMocks()
  authService.listarMisAnotaciones.mockResolvedValue([])
})

const factory = () => mount(AnotacionesMedicasSection)

describe('AnotacionesMedicasSection', () => {
  it('carga y muestra las anotaciones recibidas', async () => {
    authService.listarMisAnotaciones.mockResolvedValue([anotacion(), anotacion({ id: 'a2', mensaje: 'Segunda nota' })])
    const w = factory()
    await flushPromises()
    expect(w.text()).toContain('Revisar la tensión')
    expect(w.text()).toContain('Dra. Ana López')
    expect(w.find('.badge-info').text()).toBe('2')
  })

  it('muestra el empty state cuando no hay anotaciones', async () => {
    const w = factory()
    await flushPromises()
    expect(w.find('.empty-state-small').exists()).toBe(true)
  })

  it('deriva la lista de médicos disponibles de las anotaciones cargadas', async () => {
    authService.listarMisAnotaciones.mockResolvedValue([
      anotacion(),
      anotacion({ id: 'a2', medicoNif: '22222222J', medicoNombre: 'Dr. Beltrán' }),
      anotacion({ id: 'a3' })
    ])
    const w = factory()
    await flushPromises()
    expect(w.vm.medicosDisponibles).toHaveLength(2)
  })

  it('applyFilters vuelve a pedir las anotaciones con el filtro de médico y las fechas', async () => {
    const w = factory()
    await flushPromises()
    w.vm.filtroMedicoNif = '11111111H'
    w.vm.filtroDesde = '2026-01-01'
    w.vm.filtroHasta = '2026-01-31'
    await w.vm.applyFilters()
    expect(authService.listarMisAnotaciones).toHaveBeenLastCalledWith({
      medicoNif: '11111111H',
      desde: '2026-01-01T00:00:00',
      hasta: '2026-01-31T23:59:59'
    })
  })

  it('resetFilters limpia los filtros y recarga', async () => {
    const w = factory()
    await flushPromises()
    w.vm.filtroMedicoNif = '11111111H'
    await w.vm.resetFilters()
    expect(w.vm.filtroMedicoNif).toBe('')
    expect(authService.listarMisAnotaciones).toHaveBeenLastCalledWith({ medicoNif: undefined, desde: undefined, hasta: undefined })
  })

  it('muestra un error si la carga falla', async () => {
    authService.listarMisAnotaciones.mockRejectedValueOnce(new Error('boom'))
    const w = factory()
    await flushPromises()
    expect(w.find('.alert-danger').exists()).toBe(true)
  })

  it('formatDateTime tolera nulos y fechas inválidas', () => {
    const w = factory()
    expect(w.vm.formatDateTime(null)).toBe('')
    expect(w.vm.formatDateTime('nope')).toBe('nope')
  })
})
