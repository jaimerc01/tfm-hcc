import { describe, it, expect, vi, beforeEach } from 'vitest'

const state = vi.hoisted(() => ({ interceptor: null }))
const { client, create } = vi.hoisted(() => {
  const client = {
    get: vi.fn(() => Promise.resolve({ data: [] })),
    post: vi.fn(),
    put: vi.fn(() => Promise.resolve({ data: {} })),
    delete: vi.fn(),
    interceptors: { request: { use: vi.fn() } }
  }
  return { client, create: vi.fn(() => client) }
})
vi.mock('axios', () => ({ default: { create } }))

import svc from '@/services/solicitudService'
state.interceptor = client.interceptors.request.use.mock.calls[0]?.[0]

describe('solicitudService', () => {
  beforeEach(() => { client.get.mockClear(); client.put.mockClear() })

  it('registra el interceptor de token', () => {
    expect(state.interceptor).toBeTypeOf('function')
  })

  it('el interceptor añade Authorization solo si hay token', () => {
    localStorage.removeItem('authToken')
    expect(state.interceptor({ headers: {} }).headers.Authorization).toBeUndefined()
    localStorage.setItem('authToken', 'jwt')
    expect(state.interceptor({ headers: {} }).headers.Authorization).toBe('Bearer jwt')
  })

  it('listarRecibidas hace GET /usuario/solicitudes', () => {
    svc.listarRecibidas()
    expect(client.get).toHaveBeenCalledWith('/usuario/solicitudes')
  })

  it('listarEnviadas hace GET /medico/solicitudes-asignacion/enviadas', () => {
    svc.listarEnviadas()
    expect(client.get).toHaveBeenCalledWith('/medico/solicitudes-asignacion/enviadas')
  })

  it('actualizarEstado hace PUT a la ruta con id y el estado en el cuerpo', () => {
    svc.actualizarEstado(9, 'ACEPTADA')
    expect(client.put).toHaveBeenCalledWith('/usuario/solicitudes/9', { estado: 'ACEPTADA' })
  })
})
