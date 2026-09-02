import { describe, it, expect, vi, beforeEach } from 'vitest'

const { client, create } = vi.hoisted(() => {
  const client = {
    get: vi.fn(() => Promise.resolve({ data: [] })),
    post: vi.fn(() => Promise.resolve({ data: {} })),
    put: vi.fn(() => Promise.resolve({ data: {} })),
    delete: vi.fn(() => Promise.resolve()),
    interceptors: { request: { use: vi.fn() } }
  }
  return { client, create: vi.fn(() => client) }
})
vi.mock('axios', () => ({ default: { create } }))

import svc from '@/services/medicoService'

describe('medicoService', () => {
  beforeEach(() => {
    client.get.mockClear(); client.post.mockClear(); client.put.mockClear(); client.delete.mockClear()
  })

  it('listar devuelve data de GET /admin/medicos', async () => {
    client.get.mockResolvedValueOnce({ data: [{ id: 1 }] })
    await expect(svc.listar()).resolves.toEqual([{ id: 1 }])
    expect(client.get).toHaveBeenCalledWith('/admin/medicos')
  })

  it('checkByNif pasa el nif como query param', () => {
    svc.checkByNif('12345678Z')
    expect(client.get).toHaveBeenCalledWith('/admin/usuarios/by-nif', { params: { nif: '12345678Z' } })
  })

  it('crear hace POST y devuelve data', async () => {
    client.post.mockResolvedValueOnce({ data: { id: 5 } })
    await expect(svc.crear({ nombre: 'A' })).resolves.toEqual({ id: 5 })
    expect(client.post).toHaveBeenCalledWith('/admin/medicos', { nombre: 'A' })
  })

  it('actualizar hace PUT a la ruta con id', async () => {
    client.put.mockResolvedValueOnce({ data: { id: 2 } })
    await expect(svc.actualizar(2, { nombre: 'B' })).resolves.toEqual({ id: 2 })
    expect(client.put).toHaveBeenCalledWith('/admin/medicos/2', { nombre: 'B' })
  })

  it('eliminar hace DELETE a la ruta con id', async () => {
    await svc.eliminar(3)
    expect(client.delete).toHaveBeenCalledWith('/admin/medicos/3')
  })

  it('setPerfilMedico hace PUT con query param asignar', async () => {
    await svc.setPerfilMedico(4, true)
    expect(client.put).toHaveBeenCalledWith('/admin/medicos/4/perfil-medico', null, { params: { asignar: true } })
  })
})
