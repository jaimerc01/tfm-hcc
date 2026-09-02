<template>
  <div class="solicitudes-page">
    <div class="page-header">
      <div class="header-icon">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 11l3 3L22 4"></path>
          <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path>
        </svg>
      </div>
      <div>
        <h1>{{ $t('my_requests') }}</h1>
        <p class="subtitle">{{ $t('manage_requests') }}</p>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>{{ $t('loading_requests') }}</p>
    </div>

    <!-- Error state -->
    <div v-if="error" class="alert alert-danger">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      {{ error }}
    </div>

    <!-- Mis médicos asignados -->
    <div v-if="!loading && misMedicos.length > 0" class="solicitudes-section">
      <h2 class="section-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        {{ $t('my_doctors') }}
      </h2>

      <div class="solicitudes-grid">
        <div v-for="m in misMedicos" :key="m.nif" class="solicitud-card">
          <div class="card-body">
            <div class="info-row">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <div class="info-content">
                <span class="info-label">{{ $t('doctor') }}</span>
                <span class="info-value">{{ nombreMedico(m) }}</span>
                <span v-if="m.especialidad" class="info-value">{{ m.especialidad }}</span>
              </div>
            </div>
          </div>
          <div class="card-actions">
            <button @click="askUnassign(m)" class="btn-reject">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
              {{ $t('unassign_doctor') }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="unassignMsg" :class="['alert', unassignError ? 'alert-danger' : 'alert-success']">
        {{ unassignMsg }}
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!loading && misMedicos.length === 0 && solicitudes.length === 0 && (!solicitudesEnviadas || solicitudesEnviadas.length === 0)" class="empty-state">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M9 11l3 3L22 4"></path>
        <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path>
      </svg>
      <h3>{{ $t('no_requests') }}</h3>
      <p>{{ $t('no_pending_requests') }}</p>
    </div>

    <!-- Solicitudes recibidas -->
    <div v-if="!loading && solicitudes.length > 0" class="solicitudes-section">
      <h2 class="section-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
        </svg>
        {{ $t('received_requests') }}
      </h2>
      
      <div class="solicitudes-grid">
        <div v-for="s in solicitudes" :key="s.id" class="solicitud-card">
          <div class="card-header">
            <span :class="['status-badge', getStatusClass(s.estado)]">
              {{ getStatusLabel(s.estado) }}
            </span>
            <span class="card-date">{{ formatDate(s.fechaCreacion) }}</span>
          </div>

          <div class="card-body">
            <div class="info-row">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <div class="info-content">
                <span class="info-label">{{ $t('doctor') }}</span>
                <span class="info-value">{{ s.medico?.nombre || '-' }}</span>
              </div>
            </div>

            <div class="info-row">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <div class="info-content">
                <span class="info-label">{{ $t('patient') }}</span>
                <span class="info-value">{{ s.paciente?.nombre || '-' }}</span>
              </div>
            </div>
          </div>

          <div v-if="s.estado === 'PENDIENTE'" class="card-actions">
            <button @click="askConfirm(s.id, 'ACEPTADA')" class="btn-accept">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              {{ $t('accept') }}
            </button>
            <button @click="askConfirm(s.id, 'RECHAZADA')" class="btn-reject">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
              {{ $t('reject') }}
            </button>
          </div>

          <div v-if="messages[s.id]" :class="['alert', messagesError[s.id] ? 'alert-danger' : 'alert-success']">
            {{ messages[s.id] }}
          </div>
        </div>
      </div>
    </div>

    <!-- Solicitudes enviadas -->
    <div v-if="!loading && solicitudesEnviadas && solicitudesEnviadas.length > 0" class="solicitudes-section">
      <h2 class="section-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="22" y1="2" x2="11" y2="13"></line>
          <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
        </svg>
        {{ $t('sent_requests') }}
      </h2>
      
      <div class="solicitudes-grid">
        <div v-for="s in solicitudesEnviadas" :key="'env-'+s.id" class="solicitud-card">
          <div class="card-header">
            <span :class="['status-badge', getStatusClass(s.estado)]">
              {{ getStatusLabel(s.estado) }}
            </span>
            <span class="card-date">{{ formatDate(s.fechaCreacion) }}</span>
          </div>

          <div class="card-body">
            <div class="info-row">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <div class="info-content">
                <span class="info-label">{{ $t('patient') }}</span>
                <span class="info-value">{{ s.paciente?.nombre || '-' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirm modal: desasignar médico -->
    <div v-if="unassignTarget" class="modal-overlay" @click="cancelUnassign">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="icon-danger">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          <h3>{{ $t('confirm_action') }}</h3>
        </div>
        <p class="modal-text">{{ $t('unassign_doctor_confirm', { nombre: nombreMedico(unassignTarget) }) }}</p>
        <div class="modal-actions">
          <button class="modal-btn modal-btn--confirm btn-confirm-reject" :disabled="unassigning" @click="confirmUnassign">
            {{ unassigning ? $t('loading_requests') : $t('unassign_doctor') }}
          </button>
          <button class="modal-btn modal-btn--cancel" :disabled="unassigning" @click="cancelUnassign">{{ $t('cancel') }}</button>
        </div>
      </div>
    </div>

    <!-- Confirm modal -->
    <div v-if="confirmOpen" class="modal-overlay" @click="cancelConfirm">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :class="confirmAction === 'ACEPTADA' ? 'icon-success' : 'icon-danger'">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          <h3>{{ $t('confirm_action') }}</h3>
        </div>
        <p class="modal-text">{{ $t('confirm_request_action', { action: confirmActionLabel }) }}</p>
        <div class="modal-actions">
          <button
            @click="cambiarEstadoConfirmed"
            class="modal-btn modal-btn--confirm"
            :class="confirmAction === 'ACEPTADA' ? 'btn-confirm-accept' : 'btn-confirm-reject'"
          >
            {{ $t('yes_do_action', { action: confirmActionLabel }) }}
          </button>
          <button @click="cancelConfirm" class="modal-btn modal-btn--cancel">{{ $t('cancel') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import solicitudService from '@/services/solicitudService'
import authService from '@/services/authService'
import { ROLES, hasRole } from '@/utils/roles'

export default {
  name: 'MisSolicitudesView',
  data() {
    return {
      solicitudes: [],
      solicitudesEnviadas: [],
      misMedicos: [],
      loading: false,
      error: '',
      messages: {},
      messagesError: {},
      // confirm dialog state
      confirmOpen: false,
      confirmTarget: null,
      confirmAction: null,
      // desasignar médico
      unassignTarget: null,
      unassigning: false,
      unassignMsg: '',
      unassignError: false
    }
  },
  computed: {
    // Etiqueta del verbo de la acción a confirmar ("aceptar" / "rechazar"),
    // traducida al idioma activo para los textos del modal de confirmación.
    confirmActionLabel() {
      return this.confirmAction === 'ACEPTADA' ? this.$t('action_accept') : this.$t('action_reject')
    }
  },
  created() {
    this.cargarSolicitudes()
    this.cargarMisMedicos()
  },
  methods: {
    async cargarMisMedicos() {
      try {
        this.misMedicos = await authService.listarMisMedicos()
      } catch (e) {
        // La lista de médicos es secundaria: no bloquea la pantalla de solicitudes.
        this.misMedicos = []
      }
    },

    nombreMedico(m) {
      return [m.nombre, m.apellido1, m.apellido2].filter(Boolean).join(' ') || m.nif
    },

    askUnassign(medico) {
      this.unassignTarget = medico
      this.unassignMsg = ''
      this.unassignError = false
    },

    cancelUnassign() {
      if (this.unassigning) return
      this.unassignTarget = null
    },

    async confirmUnassign() {
      const medico = this.unassignTarget
      if (!medico) return
      this.unassigning = true
      try {
        await authService.desasignarMedico(medico.nif)
        this.unassignTarget = null
        this.unassignMsg = this.$t('unassign_doctor_success')
        this.unassignError = false
        await this.cargarMisMedicos()
      } catch (e) {
        this.unassignError = true
        this.unassignMsg = e.message || this.$t('error_unassigning_doctor')
      } finally {
        this.unassigning = false
      }
    },

    async cargarSolicitudes() {
      this.loading = true
      this.error = ''
      try {
        const { data } = await solicitudService.listarRecibidas()
        this.solicitudes = Array.isArray(data) ? data : []
        // Si además es médico, cargar también las solicitudes que ha enviado él.
        if (hasRole(authService.getCurrentUser(), ROLES.MEDICO)) {
          try {
            const { data: enviadas } = await solicitudService.listarEnviadas()
            this.solicitudesEnviadas = Array.isArray(enviadas) ? enviadas : []
          } catch (e) {
            // No bloquea la pantalla: la lista de enviadas es secundaria.
            console.warn('No se pudieron cargar solicitudes enviadas', e)
            this.solicitudesEnviadas = []
          }
        }
      } catch (e) {
        this.error = (e && e.response && (e.response.data || e.response.statusText)) || this.$t('error_loading_requests')
        this.solicitudes = []
      } finally {
        this.loading = false
      }
    },

    formatDate(v) {
      if (!v) return null
      try {
        const date = new Date(v)
        return date.toLocaleDateString(this.$i18n.locale === 'gl' ? 'gl-ES' : 'es-ES', {
          year: 'numeric', 
          month: 'short', 
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (e) {
        return v
      }
    },

    getStatusClass(estado) {
      const statusMap = {
        'PENDIENTE': 'status-pending',
        'ACEPTADA': 'status-accepted',
        'RECHAZADA': 'status-rejected'
      }
      return statusMap[estado] || 'status-pending'
    },

    getStatusLabel(estado) {
      const labelMap = {
        'PENDIENTE': 'request_status_pending',
        'ACEPTADA': 'request_status_accepted',
        'RECHAZADA': 'request_status_rejected'
      }
      return labelMap[estado] ? this.$t(labelMap[estado]) : estado
    },

    async cambiarEstado(id, nuevoEstado) {
      this.messages[id] = ''
      this.messagesError[id] = false
      try {
        await solicitudService.actualizarEstado(id, nuevoEstado)
        this.messages[id] = this.$t('request_state_updated')
        this.messagesError[id] = false
        await this.cargarSolicitudes()
      } catch (e) {
        this.messages[id] = (e && e.response && (e.response.data || e.response.statusText)) || this.$t('error_updating_request_state')
        this.messagesError[id] = true
      }
    },

    askConfirm(id, action) {
      this.confirmTarget = id
      this.confirmAction = action
      this.confirmOpen = true
    },

    async cambiarEstadoConfirmed() {
      const id = this.confirmTarget
      const action = this.confirmAction
      this.confirmOpen = false
      try {
        await this.cambiarEstado(id, action)
      } finally {
        this.confirmTarget = null
        this.confirmAction = null
      }
    },

    cancelConfirm() {
      this.confirmOpen = false
      this.confirmTarget = null
      this.confirmAction = null
    }
  }
}
</script>

<style scoped>
.solicitudes-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.alert {
  margin-bottom: 1.5rem;
}

/* Sections */
.solicitudes-section {
  margin-bottom: 3rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 1.5rem 0;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--border);
}

.section-title svg {
  color: var(--primary-color);
}

/* Grid */
.solicitudes-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .solicitudes-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .solicitudes-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

/* Cards */
.solicitud-card {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s ease;
}

.solicitud-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: var(--bg-light);
  border-bottom: 1px solid var(--border);
}

.card-date {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

/* Status badges */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.status-pending {
  background: var(--badge-warning-bg);
  color: var(--badge-warning-text);
}

.status-accepted {
  background: var(--success-light);
  color: var(--success-active);
}

.status-rejected {
  background: var(--badge-danger-bg);
  color: var(--badge-danger-text);
}

/* Card body */
.card-body {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.info-row svg {
  color: var(--primary-color);
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
  min-width: 0;
}

.info-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.info-value {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 500;
  word-break: break-word;
}

/* Card actions */
.card-actions {
  display: flex;
  gap: 0.5rem;
  padding: 1rem;
  border-top: 1px solid var(--border);
  background: var(--bg-light);
}

.btn-accept,
.btn-reject {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.625rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-accept {
  background: var(--button-color);
  color: var(--on-button);
}

.btn-accept:hover {
  background: var(--button-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.btn-reject {
  background: var(--button-color);
  color: var(--on-button);
}

.btn-reject:hover {
  background: var(--button-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

/* Modal */
.modal-header svg {
  flex-shrink: 0;
}

.icon-success {
  color: var(--success-color);
}

.icon-danger {
  color: var(--danger-color);
}

/* Responsive */
@media (max-width: 768px) {
  .solicitudes-page {
    padding: 1.5rem 1rem;
  }

  .card-actions {
    flex-direction: column;
  }
}
</style>
