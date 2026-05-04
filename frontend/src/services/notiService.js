import axios from 'axios'

const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8081'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
})

api.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('authToken')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

export default {
  listMyNotifications(page = 0, size = 5) { return api.get('/usuario/notificaciones', { params: { page, size } }) },
  markAllRead() { return api.post('/usuario/notificaciones/marcar-todas') }
}

export function fetchUnreadCount() { return api.get('/usuario/notificaciones/no-leidas') }

// mark single notification as read
export function markNotificationRead(id) { return api.put(`/usuario/notificaciones/${id}/leida`) }

export function deleteNotification(id) { return api.delete(`/usuario/notificaciones/${id}`) }

