import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const svc = vi.hoisted(() => ({
  listMyNotifications: vi.fn(),
  markAllRead: vi.fn(() => Promise.resolve()),
  markNotificationRead: vi.fn(() => Promise.resolve()),
  deleteNotification: vi.fn(() => Promise.resolve()),
  fetchUnreadCount: vi.fn(() => Promise.resolve({ data: { noLeidas: 0 } }))
}))
vi.mock('@/services/notiService', () => ({
  default: { listMyNotifications: svc.listMyNotifications, markAllRead: svc.markAllRead },
  markNotificationRead: svc.markNotificationRead,
  deleteNotification: svc.deleteNotification,
  fetchUnreadCount: svc.fetchUnreadCount
}))

const push = vi.fn()
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))

import NotificationsDropdown from '@/components/NotificationsDropdown.vue'

const notif = (over = {}) => ({ id: 1, mensaje: 'Hola', fechaCreacion: '2026-01-01T00:00:00Z', leida: false, ...over })

beforeEach(() => {
  vi.clearAllMocks()
  svc.listMyNotifications.mockResolvedValue({ data: { items: [], total: 0 } })
  svc.fetchUnreadCount.mockResolvedValue({ data: { noLeidas: 0 } })
})

const factory = async () => {
  const w = mount(NotificationsDropdown)
  await flushPromises()
  return w
}

describe('NotificationsDropdown', () => {
  it('carga la primera página al montarse', async () => {
    await factory()
    expect(svc.listMyNotifications).toHaveBeenCalledWith(0, 5)
    expect(svc.fetchUnreadCount).toHaveBeenCalled()
  })

  it('el badge muestra el número de no leídas de la lista', async () => {
    svc.listMyNotifications.mockResolvedValue({ data: { items: [notif(), notif({ id: 2, leida: true })], total: 2 } })
    const w = await factory()
    expect(w.find('.notif__badge').text()).toBe('1')
  })

  it('el badge cae al contador del servidor si no hay lista', async () => {
    svc.fetchUnreadCount.mockResolvedValue({ data: { noLeidas: 3 } })
    const w = await factory()
    expect(w.find('.notif__badge').text()).toBe('3')
  })

  it('toggle abre y cierra el menú', async () => {
    const w = await factory()
    expect(w.find('.notif__menu').exists()).toBe(false)
    await w.find('.notif__button').trigger('click')
    expect(w.find('.notif__menu').exists()).toBe(true)
  })

  it('muestra el estado vacío cuando no hay notificaciones', async () => {
    const w = await factory()
    await w.find('.notif__button').trigger('click')
    expect(w.find('.notif__empty').exists()).toBe(true)
  })

  it('click en una notificación no leída la marca como leída y navega si tiene enlace', async () => {
    svc.listMyNotifications.mockResolvedValue({ data: { items: [notif({ enlace: '/historia' })], total: 1 } })
    const w = await factory()
    await w.find('.notif__button').trigger('click')
    await w.find('.notif__message').trigger('click')
    await flushPromises()
    expect(svc.markNotificationRead).toHaveBeenCalledWith(1)
    expect(push).toHaveBeenCalledWith({ path: '/historia' })
  })

  it('borrar una notificación la quita de la lista', async () => {
    svc.listMyNotifications.mockResolvedValue({ data: { items: [notif(), notif({ id: 2 })], total: 2 } })
    const w = await factory()
    await w.find('.notif__button').trigger('click')
    await w.find('.notif__delete').trigger('click')
    await flushPromises()
    expect(svc.deleteNotification).toHaveBeenCalledWith(1)
    expect(w.findAll('.notif__menu li')).toHaveLength(1)
  })

  it('marcar todas como leídas llama al servicio y recarga', async () => {
    svc.listMyNotifications.mockResolvedValue({ data: { items: [notif()], total: 1 } })
    const w = await factory()
    await w.find('.notif__button').trigger('click')
    await w.find('.notif__footer button').trigger('click')
    await flushPromises()
    expect(svc.markAllRead).toHaveBeenCalled()
    expect(svc.listMyNotifications).toHaveBeenCalledTimes(2)
  })

  it('loadMore pide la siguiente página cuando faltan elementos', async () => {
    svc.listMyNotifications.mockResolvedValueOnce({ data: { items: [notif()], total: 3 } })
    const w = await factory()
    await w.find('.notif__button').trigger('click')
    svc.listMyNotifications.mockResolvedValueOnce({ data: { items: [notif({ id: 2 })], total: 3 } })
    await w.vm.loadMore()
    expect(svc.listMyNotifications).toHaveBeenLastCalledWith(1, 5)
  })

  it('formatDate devuelve "" sin fecha', async () => {
    const w = await factory()
    expect(w.vm.formatDate(null)).toBe('')
  })

  it('no rompe si la carga de notificaciones falla', async () => {
    svc.listMyNotifications.mockRejectedValueOnce(new Error('boom'))
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
    const w = await factory()
    expect(w.exists()).toBe(true)
    spy.mockRestore()
  })
})
