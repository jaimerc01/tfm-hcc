import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const svc = vi.hoisted(() => ({ listar: vi.fn() }))
vi.mock('@/services/medicoService', () => ({ default: svc }))

import AdminView from '@/views/AdminView.vue'

beforeEach(() => {
  vi.clearAllMocks()
  svc.listar.mockResolvedValue([])
})

const factory = async () => {
  const w = mount(AdminView)
  await flushPromises()
  return w
}

describe('AdminView', () => {
  it('muestra el título de la zona de administración', async () => {
    const w = await factory()
    expect(w.text()).toContain('Administración')
  })

  it('muestra la gestión de médicos directamente, sin un paso adicional', async () => {
    const w = await factory()
    expect(w.text()).toContain('Gestión de Médicos')
    expect(svc.listar).toHaveBeenCalled()
  })
})
