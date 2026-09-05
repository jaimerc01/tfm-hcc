<template>
  <div class="dashboard">
    <header class="page-header">
      <div class="header-icon" aria-hidden="true">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 10.5 12 3l9 7.5" />
          <path d="M5 9.5V21h14V9.5" />
          <path d="M9.5 21v-6h5v6" />
        </svg>
      </div>
      <div>
        <h1>{{ $t('dashboard_title') }}</h1>
        <p class="subtitle">
          <span v-if="welcomeName" class="dash-hello">{{ $t('dashboard_hello', { name: welcomeName }) }}</span>
          {{ $t('dashboard_subtitle') }}
        </p>
      </div>
    </header>

    <section v-if="showSummary" class="dash-summary" :aria-label="$t('dashboard_summary_aria')">
      <router-link
        v-if="isPaciente || isMedico"
        :to="{ name: 'MisSolicitudes' }"
        class="summary-tile"
        :class="{ 'summary-tile--attention': pendingRequests > 0 }"
      >
        <span class="summary-tile__value">{{ pendingRequests }}</span>
        <span class="summary-tile__label">{{ $t('dashboard_pending_requests') }}</span>
      </router-link>

      <div v-if="!isAdmin" class="summary-tile">
        <span class="summary-tile__value">{{ unreadNotifications }}</span>
        <span class="summary-tile__label">{{ $t('dashboard_unread_notifications') }}</span>
      </div>
    </section>

    <section v-if="isPaciente" class="dash-health" :aria-label="$t('health_rings_title')">
      <HealthRingsCard />
    </section>

    <section class="dash-access" :aria-label="$t('dashboard_quick_access')">
      <h2 class="dash-section-title">{{ $t('dashboard_quick_access') }}</h2>
      <ul class="access-grid">
        <li v-for="item in accessItems" :key="item.name" class="access-card">
          <router-link :to="{ name: item.name }" class="access-card__link">
            <span class="circle-icon access-card__icon" aria-hidden="true">
              <svg v-if="item.icon === 'history'" viewBox="0 0 24 24" focusable="false">
                <path d="M4 5h11l5 5v9a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1z" />
                <path d="M14 5v6h6" />
                <path d="M8 15h8M8 18h5" />
              </svg>
              <svg v-else-if="item.icon === 'user'" viewBox="0 0 24 24" focusable="false">
                <circle cx="12" cy="8" r="4" />
                <path d="M5 21c0-3.9 3.1-7 7-7s7 3.1 7 7" />
              </svg>
              <svg v-else-if="item.icon === 'requests'" viewBox="0 0 24 24" focusable="false">
                <path d="M9 11l3 3L22 4" />
                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
              </svg>
              <svg v-else-if="item.icon === 'medico'" viewBox="0 0 24 24" focusable="false">
                <path d="M6 3v6a5 5 0 0 0 10 0V3" />
                <path d="M6 3H4M16 3h2" />
                <path d="M11 19a4 4 0 0 0 8 0v-2" />
                <circle cx="19" cy="15" r="2" />
              </svg>
              <svg v-else-if="item.icon === 'admin'" viewBox="0 0 24 24" focusable="false">
                <path d="M12 3l8 4v5c0 5-3.4 8-8 9-4.6-1-8-4-8-9V7z" />
                <path d="M9.5 12l1.8 1.8L15 10" />
              </svg>
              <svg v-else viewBox="0 0 24 24" focusable="false">
                <circle cx="9" cy="8" r="3.5" />
                <path d="M3.5 20c0-3 2.5-5.5 5.5-5.5s5.5 2.5 5.5 5.5" />
                <path d="M16 7.5a3 3 0 0 1 0 6" />
                <path d="M18 14.5c2.5 0 4.5 2 4.5 4.5" />
              </svg>
            </span>
            <span class="access-card__title">{{ $t(item.labelKey) }}</span>
            <span class="access-card__desc">{{ $t(item.descKey) }}</span>
          </router-link>
        </li>
      </ul>
    </section>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRole } from '@/composables/useRole'
import authService from '@/services/authService'
import solicitudService from '@/services/solicitudService'
import { fetchUnreadCount } from '@/services/notiService'
import HealthRingsCard from '@/components/HealthRingsCard.vue'

const PENDING_STATE = 'PENDIENTE'

export default {
  name: 'DashboardView',
  components: { HealthRingsCard },
  setup() {
    const { isPaciente, isMedico, isAdmin } = useRole()

    const welcomeName = ref('')
    const pendingRequests = ref(0)
    const unreadNotifications = ref(0)
    const summaryLoaded = ref(false)

    // Tarjetas de acceso rápido: las comunes a cualquier usuario más las
    // específicas del rol. El orden refleja el uso previsto de cada perfil.
    const accessItems = computed(() => {
      const items = [
        { name: 'HistoriaClinica', icon: 'history', labelKey: 'medical_history', descKey: 'dashboard_access_history_desc' }
      ]
      if (isPaciente.value) {
        items.push({ name: 'MisSolicitudes', icon: 'requests', labelKey: 'my_requests', descKey: 'dashboard_access_requests_desc' })
      }
      if (isMedico.value) {
        items.push({ name: 'Medico', icon: 'medico', labelKey: 'medical_zone', descKey: 'dashboard_access_medico_desc' })
      }
      if (isAdmin.value) {
        // Gestionar médicos es una subsección de Administración (ver AdminView),
        // no una tarjeta propia en el acceso rápido.
        items.push({ name: 'Admin', icon: 'admin', labelKey: 'admin', descKey: 'dashboard_access_admin_desc' })
      }
      items.push({ name: 'DatosUsuario', icon: 'user', labelKey: 'user_data', descKey: 'dashboard_access_user_desc' })
      return items
    })

    const showSummary = computed(() => summaryLoaded.value)

    const loadWelcome = async () => {
      const claims = authService.getCurrentUser()
      if (claims) {
        const maybeName = claims.nombre || claims.name || claims.sub || ''
        if (maybeName) welcomeName.value = maybeName
      }
      try {
        const name = await authService.fetchMyName()
        if (name) welcomeName.value = name
      } catch (e) {
        // El nombre real es opcional: si el backend falla se mantiene el del JWT
        // (o el subtítulo genérico). No se interrumpe la pantalla por esto.
      }
    }

    // El resumen es informativo: si una llamada falla no se bloquea la pantalla,
    // simplemente se oculta el bloque de contadores.
    const loadSummary = async () => {
      const tasks = []

      if (isPaciente.value || isMedico.value) {
        tasks.push(
          solicitudService.listarRecibidas()
            .then(({ data }) => {
              const list = Array.isArray(data) ? data : []
              pendingRequests.value = list.filter(s => s.estado === PENDING_STATE).length
            })
        )
      }

      // El administrador no es destinatario de ningún tipo de notificación del
      // sistema (ver NotificacionFacadeImpl): no tiene sentido pedirle el
      // contador ni mostrarle el tile correspondiente.
      if (!isAdmin.value) {
        tasks.push(
          fetchUnreadCount().then(({ data }) => {
            unreadNotifications.value = Number(data?.noLeidas) || 0
          })
        )
      }

      const results = await Promise.allSettled(tasks)
      summaryLoaded.value = results.some(r => r.status === 'fulfilled')
    }

    onMounted(async () => {
      await loadWelcome()
      await loadSummary()
    })

    return {
      welcomeName,
      pendingRequests,
      unreadNotifications,
      showSummary,
      accessItems,
      isPaciente,
      isMedico,
      isAdmin
    }
  }
}
</script>

<style scoped>
.dashboard {
  max-width: 1100px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

/* ---------- Header ---------- */
.dash-hello {
  display: block;
  color: var(--text-primary);
  font-weight: 600;
  font-size: 1.05rem;
}

/* ---------- Summary ---------- */
.dash-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2.5rem;
}

.summary-tile {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 1.25rem 1.5rem;
  background: var(--card-bg);
  border: 1px solid var(--border-medium);
  border-left: 4px solid var(--primary-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  text-decoration: none;
}

a.summary-tile {
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
}

a.summary-tile:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

a.summary-tile:focus-visible {
  outline: var(--focus-outline);
  outline-offset: 2px;
}

.summary-tile--attention {
  border-left-color: var(--danger-color);
}

/* Cuando solo se muestra un tile (p. ej. un administrador con perfil médico
   adicional, que ve "Solicitudes pendientes" pero no "Notificaciones sin
   leer"), la única columna de auto-fit se estira a 1fr y ocupa toda la fila.
   Limitar su ancho evita ese estirado sin afectar al caso habitual de dos
   tiles, donde no aplica :only-child. */
.dash-summary .summary-tile:only-child {
  max-width: 340px;
}

.summary-tile__value {
  font-size: 1.9rem;
  font-weight: 700;
  color: var(--text-primary);
}

.summary-tile--attention .summary-tile__value {
  color: var(--danger-active);
}

.summary-tile__label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

/* ---------- Health rings ---------- */
.dash-health {
  margin-bottom: 2.5rem;
}

/* ---------- Quick access ---------- */
.dash-section-title {
  margin: 0 0 1.25rem;
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--primary-color);
}

.access-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 1.25rem;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

.access-card__link {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  height: 100%;
  padding: 1.5rem;
  background: var(--card-bg);
  border: 1px solid var(--border-medium);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  text-decoration: none;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
}

.access-card__link:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.access-card__link:focus-visible {
  outline: var(--focus-outline);
  outline-offset: 2px;
}

.access-card__icon {
  margin-bottom: 0.25rem;
}

.access-card__title {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.access-card__desc {
  font-size: 0.92rem;
  line-height: 1.5;
  color: var(--text-secondary);
}

/* ---------- Responsive ---------- */
@media (max-width: 768px) {
  .dashboard {
    padding: 1.5rem 1rem;
  }

}

@media (prefers-reduced-motion: reduce) {
  a.summary-tile,
  .access-card__link {
    transition: none;
  }

  a.summary-tile:hover,
  .access-card__link:hover {
    transform: none;
  }
}
</style>
