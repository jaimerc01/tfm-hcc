<template>
  <div class="notifications" @click.stop>
    <button class="notif__button" @click="toggle" :aria-expanded="open">
      🔔 <span v-if="unreadCount>0" class="notif__badge">{{ unreadCount }}</span>
    </button>

    <div v-if="open" class="notif__menu">
      <div class="notif__header">Notificaciones</div>
      <ul>
        <li v-for="n in notifications" :key="n.id" :class="{ 'notif--unread': !n.leida }">
          <div class="notif__message" @click.stop.prevent="onNotificationClick(n)">{{ n.mensaje }}</div>
          <div class="notif__time">{{ formatDate(n.fechaCreacion) }}</div>
          <button class="notif__delete" @click.stop.prevent="onDelete(n)" aria-label="Eliminar">✕</button>
        </li>
        <li v-if="notifications.length===0" class="notif__empty">No tienes notificaciones</li>
      </ul>
      <div class="notif__footer">
        <button @click="markAllRead">Marcar todas como leídas</button>
        <button v-if="notifications.length < total" @click="loadMore" :disabled="loading">{{ loading ? 'Cargando...' : 'Cargar más' }}</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import notiService, { markNotificationRead, deleteNotification, fetchUnreadCount } from '@/services/notiService'

export default {
  name: 'NotificationsDropdown',
  setup() {
    const open = ref(false)
  const notifications = ref([])
  const page = ref(0)
  const size = 5
  const total = ref(0)
  const loading = ref(false)

  const router = useRouter()

  const load = async (p = 0) => {
      try {
        loading.value = true
        const res = await notiService.listMyNotifications(p, size)
        const body = res.data || { items: [], total: 0 }
        if (p === 0) {
          notifications.value = body.items || []
        } else {
          notifications.value = notifications.value.concat(body.items || [])
        }
        total.value = body.total || 0
        page.value = p
      } catch (e) {
        console.error('No se pudieron cargar notificaciones', e)
      } finally { loading.value = false }
    }

    onMounted(() => load(0))

    // Load only the unread count initially to render badge quickly
    const loadUnreadCount = async () => {
      try {
        const r = await fetchUnreadCount()
        const body = r.data || { noLeidas: 0 }
        // Update local unread flag counts by not fetching full list
        // We won't alter notifications array here; badge value is computed from server count
        // but keep a small local representation: set total to at least unread count
        total.value = Math.max(total.value || 0, body.noLeidas || 0)
      } catch (e) { console.error('Error al obtener contador de notificaciones', e) }
    }

    onMounted(() => loadUnreadCount())

    const toggle = () => { open.value = !open.value }
    const unreadCount = computed(() => {
      if (notifications.value && notifications.value.length) return notifications.value.filter(n => !n.leida).length
      return total.value || 0
    })

    const markAllRead = async () => {
      try {
        await notiService.markAllRead()
  notifications.value.forEach(n => n.leida = true)
  // optionally refresh counts from server
  // reload first page to get up-to-date state
  await load(0)
      } catch (e) {
        console.error('Error marcando leídas', e)
      }
    }

    const formatDate = (d) => {
      if (!d) return ''
      try { return new Date(d).toLocaleString() } catch(e) { return d }
    }

    const onNotificationClick = async (n) => {
      if (!n) return
      try {
        if (!n.leida) {
          await markNotificationRead(n.id)
          n.leida = true
        }
      } catch (e) { console.error('Error marcando notificación leída', e) }
      if (n.enlace) {
        router.push({ path: n.enlace })
      }
      open.value = false
    }

    const onDelete = async (n) => {
      if (!n) return
      try {
        await deleteNotification(n.id)
        notifications.value = notifications.value.filter(x => x.id !== n.id)
        total.value = Math.max(0, total.value - 1)
      } catch (e) { console.error('Error eliminando notificación', e) }
    }

    const loadMore = async () => {
      if (notifications.value.length >= total.value) return
      await load(page.value + 1)
    }

  return { open, notifications, toggle, unreadCount, markAllRead, formatDate, onNotificationClick, onDelete, loadMore, loading, total }
  }
}
</script>

<style scoped>
.notifications { position: relative; display: inline-block; }
.notif__button { background: transparent; border: none; color: #ecf0f1; font-size: 1.1rem; cursor: pointer; }
.notif__badge { background: #e74c3c; color: #fff; padding: 2px 6px; border-radius: 999px; margin-left: 4px; font-size: 0.75rem; }
.notif__menu { position: absolute; right: 0; top: 36px; background: #34495e; color: #fff; min-width: 260px; border-radius: 6px; box-shadow: 0 4px 12px rgba(0,0,0,0.2); z-index: 2000; }
.notif__header { padding: 0.5rem 0.75rem; border-bottom: 1px solid rgba(255,255,255,0.06); font-weight: 600; }
.notif__menu ul { list-style: none; margin: 0; padding: 0; max-height: 260px; overflow:auto; }
.notif__menu li { padding: 0.5rem 0.75rem; border-bottom: 1px solid rgba(255,255,255,0.03); }
.notif--unread { background: rgba(0,0,0,0.08); }
.notif__message { font-size: 0.95rem; }
.notif__time { font-size: 0.75rem; color: #bdc3c7; }
.notif__empty { padding: 0.5rem 0.75rem; color: #bdc3c7 }
.notif__footer { padding: 0.5rem; text-align: right; }
.notif__footer button { background: transparent; border: 1px solid rgba(255,255,255,0.08); color: #fff; padding: 0.3rem 0.5rem; border-radius: 4px; cursor: pointer; }
.notif__delete { background: transparent; border: none; color: #bdc3c7; cursor: pointer; float: right; }
.notif__delete:hover { color: #fff; }
</style>
