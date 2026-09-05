import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const auth = vi.hoisted(() => ({ misLogs: vi.fn() }))
vi.mock('@/services/authService', () => ({ default: auth }))

import AccessLogSection from '@/components/AccessLogSection.vue'

const acceso = (over = {}) => ({ timestamp: '2024-01-01T10:00:00', ruta: '/historia-clinica', metodo: 'GET', estado: 200, ip: '127.0.0.1', ...over })

beforeEach(() => {
  vi.clearAllMocks()
  auth.misLogs.mockResolvedValue([])
})

const factory = async () => {
  const w = mount(AccessLogSection)
  await flushPromises()
  return w
}

describe('AccessLogSection', () => {
  it('carga el registro de accesos al crearse', async () => {
    auth.misLogs.mockResolvedValue([acceso()])
    const w = await factory()
    expect(auth.misLogs).toHaveBeenCalled()
    expect(w.text()).toContain('/historia-clinica')
  })

  it('muestra el estado vacío si no hay accesos', async () => {
    const w = await factory()
    expect(w.find('.empty-hint').exists()).toBe(true)
  })

  it('muestra un error si la carga falla', async () => {
    auth.misLogs.mockRejectedValueOnce(new Error('boom'))
    const w = await factory()
    expect(w.vm.error).toBe('boom')
  })

  it('applyFilters recarga con las fechas indicadas', async () => {
    const w = await factory()
    w.vm.filtroDesde = '2024-01-01'
    w.vm.filtroHasta = '2024-01-31'
    w.vm.applyFilters()
    await flushPromises()
    expect(auth.misLogs).toHaveBeenLastCalledWith({ desde: '2024-01-01T00:00:00', hasta: '2024-01-31T23:59:59' })
  })

  it('resetFilters limpia las fechas y recarga', async () => {
    const w = await factory()
    w.vm.filtroDesde = '2024-01-01'
    w.vm.filtroHasta = '2024-01-31'
    w.vm.resetFilters()
    await flushPromises()
    expect(w.vm.filtroDesde).toBe('')
    expect(w.vm.filtroHasta).toBe('')
    expect(auth.misLogs).toHaveBeenLastCalledWith({ desde: undefined, hasta: undefined })
  })

  describe('paginación', () => {
    it('no muestra controles de paginación con una sola página', async () => {
      auth.misLogs.mockResolvedValue([acceso(), acceso()])
      const w = await factory()
      expect(w.find('.pagination').exists()).toBe(false)
    })

    it('pagina de 10 en 10 y deshabilita los botones en los extremos', async () => {
      const accesos = Array.from({ length: 25 }, (_, i) => acceso({ ruta: `/ruta-${i}` }))
      auth.misLogs.mockResolvedValue(accesos)
      const w = await factory()

      expect(w.vm.totalPages).toBe(3)
      expect(w.vm.pagedAccesos).toHaveLength(10)
      expect(w.text()).toContain('Página 1 de 3')

      const [prevBtn, nextBtn] = w.findAll('.pagination button')
      expect(prevBtn.attributes('disabled')).toBeDefined()
      expect(nextBtn.attributes('disabled')).toBeUndefined()

      await nextBtn.trigger('click')
      expect(w.vm.page).toBe(2)
      await nextBtn.trigger('click')
      expect(w.vm.page).toBe(3)
      expect(w.vm.pagedAccesos).toHaveLength(5)
      expect(nextBtn.attributes('disabled')).toBeDefined()

      await prevBtn.trigger('click')
      expect(w.vm.page).toBe(2)
    })

    it('vuelve a la página 1 al recargar tras aplicar filtros', async () => {
      const accesos = Array.from({ length: 15 }, (_, i) => acceso({ ruta: `/ruta-${i}` }))
      auth.misLogs.mockResolvedValue(accesos)
      const w = await factory()
      w.vm.page = 2
      w.vm.applyFilters()
      await flushPromises()
      expect(w.vm.page).toBe(1)
    })
  })
})
