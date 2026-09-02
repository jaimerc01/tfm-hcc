import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const svc = vi.hoisted(() => ({
  listarRecibidas: vi.fn(),
  listarEnviadas: vi.fn(),
  actualizarEstado: vi.fn(() => Promise.resolve({ data: {} }))
}))
vi.mock('@/services/solicitudService', () => ({ default: svc }))

const auth = vi.hoisted(() => ({
  getCurrentUser: vi.fn(() => ({ authorities: ['ROLE_PACIENTE'] })),
  listarMisMedicos: vi.fn(),
  desasignarMedico: vi.fn(() => Promise.resolve(true))
}))
vi.mock('@/services/authService', () => ({ default: auth }))

import MisSolicitudesView from '@/views/MisSolicitudesView.vue'

beforeEach(() => {
  vi.clearAllMocks()
  svc.listarRecibidas.mockResolvedValue({ data: [] })
  svc.listarEnviadas.mockResolvedValue({ data: [] })
  auth.getCurrentUser.mockReturnValue({ authorities: ['ROLE_PACIENTE'] })
  auth.listarMisMedicos.mockResolvedValue([])
})

const factory = async () => {
  const w = mount(MisSolicitudesView)
  await flushPromises()
  return w
}

describe('MisSolicitudesView', () => {
  it('carga las solicitudes recibidas a través del servicio', async () => {
    svc.listarRecibidas.mockResolvedValueOnce({ data: [{ id: 1, estado: 'PENDIENTE' }] })
    const w = await factory()
    expect(svc.listarRecibidas).toHaveBeenCalled()
    expect(w.vm.solicitudes).toHaveLength(1)
  })

  it('si es médico también carga las solicitudes enviadas', async () => {
    auth.getCurrentUser.mockReturnValue({ authorities: ['ROLE_MEDICO'] })
    svc.listarEnviadas.mockResolvedValueOnce({ data: [{ id: 9 }] })
    const w = await factory()
    expect(w.vm.solicitudesEnviadas).toEqual([{ id: 9 }])
  })

  it('un paciente no llama a listarEnviadas', async () => {
    await factory()
    expect(svc.listarEnviadas).not.toHaveBeenCalled()
  })

  it('la carga de enviadas fallida no bloquea la vista', async () => {
    auth.getCurrentUser.mockReturnValue({ authorities: ['ROLE_MEDICO'] })
    svc.listarEnviadas.mockRejectedValueOnce(new Error('x'))
    const spy = vi.spyOn(console, 'warn').mockImplementation(() => {})
    const w = await factory()
    expect(w.vm.solicitudesEnviadas).toEqual([])
    spy.mockRestore()
  })

  it('muestra error traducido si la carga principal falla sin cuerpo', async () => {
    svc.listarRecibidas.mockRejectedValueOnce(new Error('network'))
    const w = await factory()
    expect(w.vm.error).toBe('Error al cargar las solicitudes')
  })

  it('usa el mensaje del backend si la respuesta de error lo trae', async () => {
    svc.listarRecibidas.mockRejectedValueOnce({ response: { data: 'No autorizado' } })
    const w = await factory()
    expect(w.vm.error).toBe('No autorizado')
  })

  it('getStatusClass y getStatusLabel mapean los estados (label traducido)', async () => {
    const w = await factory()
    expect(w.vm.getStatusClass('ACEPTADA')).toBe('status-accepted')
    expect(w.vm.getStatusClass('DESCONOCIDO')).toBe('status-pending')
    expect(w.vm.getStatusLabel('RECHAZADA')).toBe('Rechazada')
    expect(w.vm.getStatusLabel('OTRO')).toBe('OTRO')
  })

  it('confirmActionLabel devuelve el verbo traducido según la acción', async () => {
    const w = await factory()
    w.vm.confirmAction = 'ACEPTADA'
    expect(w.vm.confirmActionLabel).toBe('aceptar')
    w.vm.confirmAction = 'RECHAZADA'
    expect(w.vm.confirmActionLabel).toBe('rechazar')
  })

  it('formatDate devuelve null sin valor y una fecha localizada con valor', async () => {
    const w = await factory()
    expect(w.vm.formatDate(null)).toBeNull()
    expect(w.vm.formatDate('2026-01-15T10:30:00Z')).toBeTruthy()
  })

  it('askConfirm abre el modal guardando id y acción', async () => {
    const w = await factory()
    w.vm.askConfirm(5, 'ACEPTADA')
    expect(w.vm.confirmOpen).toBe(true)
    expect(w.vm.confirmTarget).toBe(5)
    expect(w.vm.confirmAction).toBe('ACEPTADA')
  })

  it('cambiarEstadoConfirmed actualiza el estado vía servicio y recarga', async () => {
    const w = await factory()
    w.vm.askConfirm(5, 'ACEPTADA')
    await w.vm.cambiarEstadoConfirmed()
    await flushPromises()
    expect(svc.actualizarEstado).toHaveBeenCalledWith(5, 'ACEPTADA')
    expect(w.vm.messages[5]).toBe('Solicitud actualizada correctamente')
    expect(w.vm.confirmTarget).toBeNull()
  })

  it('cambiarEstado registra el error por id si el servicio falla', async () => {
    svc.actualizarEstado.mockRejectedValueOnce({ response: { data: 'Conflicto' } })
    const w = await factory()
    await w.vm.cambiarEstado(7, 'RECHAZADA')
    expect(w.vm.messages[7]).toBe('Conflicto')
    expect(w.vm.messagesError[7]).toBe(true)
  })

  it('cambiarEstado usa el mensaje traducido si el error no trae cuerpo', async () => {
    svc.actualizarEstado.mockRejectedValueOnce(new Error('x'))
    const w = await factory()
    await w.vm.cambiarEstado(7, 'ACEPTADA')
    expect(w.vm.messages[7]).toBe('Error al actualizar el estado de la solicitud')
  })

  it('cancelConfirm limpia el estado del modal', async () => {
    const w = await factory()
    w.vm.askConfirm(5, 'ACEPTADA')
    w.vm.cancelConfirm()
    expect(w.vm.confirmOpen).toBe(false)
    expect(w.vm.confirmAction).toBeNull()
  })

  it('carga la lista de médicos asignados al crearse', async () => {
    auth.listarMisMedicos.mockResolvedValueOnce([{ nif: '11111111H', nombre: 'Ana' }])
    const w = await factory()
    expect(auth.listarMisMedicos).toHaveBeenCalled()
    expect(w.vm.misMedicos).toHaveLength(1)
    expect(w.text()).toContain('Ana')
  })

  it('un fallo al cargar los médicos no bloquea la vista', async () => {
    auth.listarMisMedicos.mockRejectedValueOnce(new Error('x'))
    const w = await factory()
    expect(w.vm.misMedicos).toEqual([])
  })

  it('confirmUnassign finaliza la relación vía servicio y recarga la lista', async () => {
    auth.listarMisMedicos.mockResolvedValueOnce([{ nif: '11111111H', nombre: 'Ana' }])
    const w = await factory()
    w.vm.askUnassign({ nif: '11111111H', nombre: 'Ana' })
    expect(w.vm.unassignTarget).toBeTruthy()
    await w.vm.confirmUnassign()
    await flushPromises()
    expect(auth.desasignarMedico).toHaveBeenCalledWith('11111111H')
    expect(w.vm.unassignTarget).toBeNull()
    expect(w.vm.unassignError).toBe(false)
  })

  it('confirmUnassign muestra el error si el servicio falla', async () => {
    auth.desasignarMedico.mockRejectedValueOnce(new Error('No existe una relación'))
    const w = await factory()
    w.vm.askUnassign({ nif: '11111111H', nombre: 'Ana' })
    await w.vm.confirmUnassign()
    expect(w.vm.unassignError).toBe(true)
    expect(w.vm.unassignMsg).toBe('No existe una relación')
  })

  it('nombreMedico compone nombre y apellidos con fallback al NIF', async () => {
    const w = await factory()
    expect(w.vm.nombreMedico({ nombre: 'Ana', apellido1: 'Gil', nif: 'X' })).toBe('Ana Gil')
    expect(w.vm.nombreMedico({ nif: '11111111H' })).toBe('11111111H')
  })
})
