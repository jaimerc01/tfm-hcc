import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises, RouterLinkStub } from '@vue/test-utils'

const auth = vi.hoisted(() => ({ getCurrentUser: vi.fn(), fetchMyName: vi.fn() }))
const solicitud = vi.hoisted(() => ({ listarRecibidas: vi.fn() }))
const noti = vi.hoisted(() => ({ fetchUnreadCount: vi.fn() }))
const historia = vi.hoisted(() => ({ getMine: vi.fn(), getRangos: vi.fn() }))

vi.mock('@/services/authService', () => ({ default: auth }))
vi.mock('@/services/solicitudService', () => ({ default: solicitud }))
vi.mock('@/services/notiService', () => ({ fetchUnreadCount: noti.fetchUnreadCount }))
vi.mock('@/services/historiaClinicaService', () => ({ default: historia }))

import DashboardView from '@/views/dashboard/DashboardView.vue'

const roleClaims = roles => ({ sub: '12345678Z', authorities: roles.map(r => `ROLE_${r}`) })

beforeEach(() => {
  vi.clearAllMocks()
  auth.getCurrentUser.mockReturnValue(roleClaims(['PACIENTE']))
  auth.fetchMyName.mockResolvedValue(null)
  solicitud.listarRecibidas.mockResolvedValue({ data: [] })
  noti.fetchUnreadCount.mockResolvedValue({ data: { noLeidas: 0 } })
  historia.getMine.mockResolvedValue({ data: {} })
  historia.getRangos.mockResolvedValue({ data: [] })
})

const factory = async () => {
  const w = mount(DashboardView, {
    global: { stubs: { RouterLink: RouterLinkStub } }
  })
  await flushPromises()
  return w
}

describe('DashboardView', () => {
  it('usa el nombre del backend cuando está disponible', async () => {
    auth.fetchMyName.mockResolvedValue('Ana López')
    const w = await factory()
    expect(w.text()).toContain('Ana López')
  })

  it('cae al claim del JWT si el backend no devuelve nombre', async () => {
    auth.getCurrentUser.mockReturnValue({ ...roleClaims(['PACIENTE']), nombre: 'Pepe' })
    const w = await factory()
    expect(w.vm.welcomeName).toBe('Pepe')
  })

  it('muestra el subtítulo genérico si no hay nombre', async () => {
    auth.getCurrentUser.mockReturnValue({ authorities: ['ROLE_PACIENTE'] })
    const w = await factory()
    expect(w.vm.welcomeName).toBe('')
    expect(w.find('.subtitle').exists()).toBe(true)
    expect(w.find('.dash-hello').exists()).toBe(false)
  })

  it('mantiene el nombre del JWT si el backend falla al dar el nombre', async () => {
    auth.getCurrentUser.mockReturnValue({ ...roleClaims(['PACIENTE']), nombre: 'Pepe' })
    auth.fetchMyName.mockRejectedValueOnce(new Error('boom'))
    const w = await factory()
    expect(w.vm.welcomeName).toBe('Pepe')
  })

  it('cuenta las solicitudes pendientes recibidas', async () => {
    solicitud.listarRecibidas.mockResolvedValue({
      data: [
        { id: 1, estado: 'PENDIENTE' },
        { id: 2, estado: 'ACEPTADA' },
        { id: 3, estado: 'PENDIENTE' }
      ]
    })
    const w = await factory()
    expect(w.vm.pendingRequests).toBe(2)
  })

  it('muestra el contador de notificaciones sin leer', async () => {
    // El backend responde { noLeidas: N }, no el número suelto (regresión: ver
    // NotificacionControllerImpl#contarNotificacionesNoLeidas).
    noti.fetchUnreadCount.mockResolvedValue({ data: { noLeidas: 4 } })
    const w = await factory()
    expect(w.vm.unreadNotifications).toBe(4)
  })

  it('oculta el resumen si todas las llamadas fallan', async () => {
    solicitud.listarRecibidas.mockRejectedValue(new Error('down'))
    noti.fetchUnreadCount.mockRejectedValue(new Error('down'))
    const w = await factory()
    expect(w.vm.showSummary).toBe(false)
    expect(w.find('.dash-summary').exists()).toBe(false)
  })

  it('el paciente ve la tarjeta de solicitudes pero no la de zona médica ni administración', async () => {
    const w = await factory()
    const names = w.vm.accessItems.map(i => i.name)
    expect(names).toContain('HistoriaClinica')
    expect(names).toContain('MisSolicitudes')
    expect(names).toContain('DatosUsuario')
    expect(names).not.toContain('Medico')
    expect(names).not.toContain('Admin')
  })

  it('el médico ve la tarjeta de zona médica', async () => {
    auth.getCurrentUser.mockReturnValue(roleClaims(['MEDICO']))
    const w = await factory()
    const names = w.vm.accessItems.map(i => i.name)
    expect(names).toContain('Medico')
    expect(names).not.toContain('MisSolicitudes')
  })

  it('el administrador ve la tarjeta de administración pero no una propia de gestión de médicos', async () => {
    auth.getCurrentUser.mockReturnValue(roleClaims(['ADMINISTRADOR']))
    const w = await factory()
    const names = w.vm.accessItems.map(i => i.name)
    expect(names).toContain('Admin')
    expect(names).not.toContain('AdminMedicos')
  })

  it('el administrador no tiene tile de notificaciones ni de solicitudes: el resumen no se pide ni se muestra', async () => {
    auth.getCurrentUser.mockReturnValue(roleClaims(['ADMINISTRADOR']))
    const w = await factory()
    expect(noti.fetchUnreadCount).not.toHaveBeenCalled()
    expect(solicitud.listarRecibidas).not.toHaveBeenCalled()
    expect(w.vm.showSummary).toBe(false)
    expect(w.find('.dash-summary').exists()).toBe(false)
  })
})
