import { describe, it, expect, vi, beforeEach } from 'vitest'

const { client, create } = vi.hoisted(() => {
  const client = {
    get: vi.fn(() => Promise.resolve({ data: {} })),
    post: vi.fn(() => Promise.resolve({ data: {} })),
    put: vi.fn(() => Promise.resolve({ data: {} })),
    delete: vi.fn(() => Promise.resolve({ data: {} })),
    interceptors: { request: { use: vi.fn() } }
  }
  return { client, create: vi.fn(() => client) }
})
vi.mock('axios', () => ({ default: { create } }))

import notiService, { fetchUnreadCount, markNotificationRead, deleteNotification } from '@/services/notiService'

describe('notiService', () => {
  beforeEach(() => { client.get.mockClear(); client.post.mockClear(); client.put.mockClear(); client.delete.mockClear() })

  it('listMyNotifications usa page/size por defecto', () => {
    notiService.listMyNotifications()
    expect(client.get).toHaveBeenCalledWith('/notificaciones', { params: { page: 0, size: 5 } })
  })

  it('listMyNotifications acepta page/size explícitos', () => {
    notiService.listMyNotifications(2, 20)
    expect(client.get).toHaveBeenCalledWith('/notificaciones', { params: { page: 2, size: 20 } })
  })

  it('markAllRead hace POST /notificaciones/marcar-leidas', () => {
    notiService.markAllRead()
    expect(client.post).toHaveBeenCalledWith('/notificaciones/marcar-leidas')
  })

  it('fetchUnreadCount hace GET /notificaciones/no-leidas', () => {
    fetchUnreadCount()
    expect(client.get).toHaveBeenCalledWith('/notificaciones/no-leidas')
  })

  it('markNotificationRead hace PUT a la ruta con id', () => {
    markNotificationRead(8)
    expect(client.put).toHaveBeenCalledWith('/notificaciones/8/leida')
  })

  it('deleteNotification hace DELETE a la ruta con id', () => {
    deleteNotification(8)
    expect(client.delete).toHaveBeenCalledWith('/notificaciones/8')
  })
})
