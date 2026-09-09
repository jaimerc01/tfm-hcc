<template>
  <div class="notifications" @click.stop>
    <button
      class="notif__button"
      @click="toggle"
      :aria-expanded="open"
      :aria-label="unreadCount > 0 ? $t('notifications_unread_badge_aria', { count: unreadCount }) : $t('notifications')"
    >
      <AppIcon name="bell" size="lg" />
      <span v-if="unreadCount > 0" class="notif__badge" aria-hidden="true">{{ unreadCount }}</span>
    </button>

    <div v-if="open" class="notif__menu">
      <div class="notif__header">
        <AppIcon name="bell" size="md" class="notif__header-icon" />
        <span class="notif__header-title">{{ $t('notifications') }}</span>
        <span v-if="unreadCount > 0" class="badge badge-info notif__header-count">{{ unreadCount }}</span>
      </div>

      <ul v-if="notifications.length" class="notif__list">
        <li v-for="n in notifications" :key="n.id" class="notif__item" :class="{ 'notif__item--unread': !n.leida }">
          <div
            class="notif__content"
            role="button"
            tabindex="0"
            @click.stop.prevent="onNotificationClick(n)"
            @keydown.enter="onNotificationClick(n)"
            @keydown.space.prevent="onNotificationClick(n)"
          >
            <AppIcon name="bell" size="sm" class="notif__item-icon" />
            <span class="notif__item-body">
              <span class="notif__message">{{ n.mensaje }}</span>
              <span class="notif__time">{{ formatDate(n.fechaCreacion) }}</span>
            </span>
          </div>
          <button
            type="button"
            class="btn-icon btn-danger notif__delete"
            @click.stop.prevent="onDelete(n)"
            :title="$t('delete_notification_aria')"
            :aria-label="$t('delete_notification_aria')"
          >
            <AppIcon name="trash" size="sm" />
          </button>
        </li>
      </ul>

      <div v-else class="notif__empty">
        <AppIcon name="bell" size="3xl" />
        <p>{{ $t('no_notifications') }}</p>
        <span>{{ $t('no_notifications_hint') }}</span>
      </div>

      <div class="notif__footer">
        <button type="button" class="btn-secondary btn-sm" @click="markAllRead" :disabled="unreadCount === 0">
          {{ $t('mark_all_read') }}
        </button>
        <button
          v-if="notifications.length < total"
          type="button"
          class="btn-secondary btn-sm"
          @click="loadMore"
          :disabled="loading"
        >
          {{ loading ? $t('loading') : $t('load_more') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import notiService, { markNotificationRead, deleteNotification, fetchUnreadCount } from '@/services/notiService'
import AppIcon from '@/components/AppIcon.vue'

export default {
  name: 'NotificationsDropdown',
  components: { AppIcon },
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
      try { return new Date(d).toLocaleString() } catch (e) { return d }
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
.notifications {
  position: relative;
  display: inline-flex;
  align-items: center;
}

/* ---------- Bell trigger ---------- */
/* Same colour/hover language as the other nav items (.nav__link in Navbar.vue):
   the bell sits on the same blue bar, so it must use the same tokens instead of
   the dark-surface tokens the previous version borrowed from an unrelated menu. */
.notif__button {
  position: relative;
  display: inline-flex;
  align-items: center;
  background: transparent;
  border: none;
  color: var(--white-alpha-90);
  padding: 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.notif__button:hover {
  color: var(--text-inverse);
  background: var(--primary-hover);
}

.notif__button:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 2px;
}

/* Unread counter: part of the amber/gold family reserved app-wide for alerts
   and error states, never a button colour (see styles/variables.css). The
   border matches the navbar background so the badge reads as a cutout. */
.notif__badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 1.15rem;
  height: 1.15rem;
  padding: 0 0.3rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--danger-color);
  color: var(--text-inverse);
  border: 2px solid var(--primary-color);
  border-radius: 999px;
  font-size: 0.65rem;
  font-weight: 700;
  line-height: 1;
}

/* ---------- Dropdown panel ---------- */
/* Light card surface, same recipe as .card / .modal (components.css): tinted
   background, hairline border, 12px radius, popover shadow. */
.notif__menu {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  width: 320px;
  max-width: calc(100vw - 2rem);
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
  color: var(--text-primary);
  border: 1px solid var(--border);
  border-radius: 12px;
  box-shadow: var(--shadow-popover);
  overflow: hidden;
  z-index: 2000;
}

.notif__header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: var(--bg-light);
  border-bottom: 1px solid var(--border);
}

.notif__header-icon {
  color: var(--primary-color);
  flex-shrink: 0;
}

.notif__header-title {
  flex: 1;
  font-weight: 600;
  color: var(--text-primary);
}

.notif__header-count {
  flex-shrink: 0;
}

.notif__list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 320px;
  overflow-y: auto;
}

.notif__item {
  display: flex;
  align-items: stretch;
  gap: 0.35rem;
  padding: 0.6rem 0.75rem;
  border-bottom: 1px solid var(--border);
  border-left: 3px solid transparent;
}

.notif__item:last-child {
  border-bottom: none;
}

/* Unread = "needs attention", same treatment as summary-tile--attention in
   DashboardView.vue: gold left accent + soft gold tint, not a colour invented
   for this component. */
.notif__item--unread {
  background: var(--danger-soft-bg);
  border-left-color: var(--danger-color);
}

.notif__content {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: flex-start;
  gap: 0.6rem;
  padding: 0.25rem;
  margin: -0.25rem;
  border-radius: 6px;
  cursor: pointer;
}

.notif__content:hover {
  background: var(--bg-light);
}

.notif__content:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 2px;
}

.notif__item-icon {
  flex-shrink: 0;
  margin-top: 0.15rem;
  color: var(--text-secondary);
}

.notif__item--unread .notif__item-icon {
  color: var(--danger-color);
}

.notif__item-body {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  min-width: 0;
}

.notif__message {
  font-size: 0.875rem;
  color: var(--text-primary);
  word-break: break-word;
}

.notif__item--unread .notif__message {
  font-weight: 600;
}

.notif__time {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.notif__delete {
  flex-shrink: 0;
  align-self: center;
}

.notif__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  padding: 2rem 1rem;
  text-align: center;
  color: var(--text-secondary);
}

.notif__empty :deep(svg) {
  opacity: 0.35;
}

.notif__empty p {
  margin: 0;
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--text-primary);
}

.notif__empty span {
  font-size: 0.8rem;
}

.notif__footer {
  display: flex;
  gap: 0.5rem;
  padding: 0.6rem 0.75rem;
  background: var(--bg-light);
  border-top: 1px solid var(--border);
}

.notif__footer .btn-secondary {
  flex: 1;
}

@media (max-width: 480px) {
  .notif__menu {
    position: fixed;
    left: 1rem;
    right: 1rem;
    top: 60px;
    width: auto;
    max-width: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .notif__button,
  .notif__content {
    transition: none;
  }
}
</style>
