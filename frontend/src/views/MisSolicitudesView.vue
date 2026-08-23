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

    <!-- Empty state -->
    <div v-if="!loading && solicitudes.length === 0 && (!solicitudesEnviadas || solicitudesEnviadas.length === 0)" class="empty-state">
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
        <p class="modal-text">¿Estás seguro de que deseas <strong>{{ confirmLabel }}</strong> esta solicitud?</p>
          <p class="modal-text">{{ $t('confirm_request_action', { action: confirmLabel }) }}</p>
        <div class="modal-actions">
          <button @click="cambiarEstadoConfirmed" :class="confirmAction === 'ACEPTADA' ? 'btn-confirm-accept' : 'btn-confirm-reject'">
            {{ $t('yes_do_action', { action: confirmLabel }) }}
          </button>
          <button @click="cancelConfirm" class="btn-cancel">Cancelar</button>
                  <button @click="cancelConfirm" class="btn-cancel">{{ $t('cancel') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import authService from '../services/authService'
import { ROLES, hasRole } from '../utils/roles'

export default {
  name: 'MisSolicitudesView',
  data() {
    return {
      solicitudes: [],
  solicitudesEnviadas: [],
      loading: false,
      error: '',
      messages: {},
      messagesError: {},
      // confirm dialog state
      confirmOpen: false,
      confirmTarget: null,
      confirmAction: null,
      confirmLabel: ''
    }
  },
  created() {
    this.cargarSolicitudes()
  },
  methods: {
    async cargarSolicitudes() {
      this.loading = true
      this.error = ''
      try {
        const token = localStorage.getItem('authToken')
        const base = process.env.VUE_APP_API_URL || 'http://localhost:8081'
        const resp = await axios.get(`${base}/usuario/solicitudes`, { headers: { Authorization: `Bearer ${token}` } })
        this.solicitudes = Array.isArray(resp.data) ? resp.data : []
        // if user is a medico, also load solicitudes they have sent
        const claims = authService.getCurrentUser()
        if (hasRole(claims, ROLES.MEDICO)) {
          try {
            const r2 = await axios.get(`${base}/medico/solicitudes-asignacion/enviadas`, { headers: { Authorization: `Bearer ${token}` } })
            this.solicitudesEnviadas = Array.isArray(r2.data) ? r2.data : []
          } catch (e) {
            // non-blocking: ignore sent list errors but keep a console message
            // eslint-disable-next-line no-console
            console.warn('No se pudieron cargar solicitudes enviadas', e)
            this.solicitudesEnviadas = []
          }
        }
      } catch (e) {
        this.error = (e && e.response && (e.response.data || e.response.statusText)) || 'Error cargando solicitudes'
        this.solicitudes = []
      } finally {
        this.loading = false
      }
    },

    formatDate(v) {
      if (!v) return null
      try {
        const date = new Date(v)
        return date.toLocaleDateString('es-ES', { 
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
        'PENDIENTE': 'Pendiente',
        'ACEPTADA': 'Aceptada',
        'RECHAZADA': 'Rechazada'
      }
      return labelMap[estado] || estado
    },

    // Directly call the API to change estado
    async cambiarEstado(id, nuevoEstado) {
      // reset messages for this id (Vue 3: use direct assignment)
      this.messages[id] = ''
      this.messagesError[id] = false
      try {
        const token = localStorage.getItem('authToken')
        const base = process.env.VUE_APP_API_URL || 'http://localhost:8081'
        await axios.put(`${base}/usuario/solicitudes/${id}`, { estado: nuevoEstado }, { headers: { Authorization: `Bearer ${token}` } })
  this.messages[id] = `Solicitud ${nuevoEstado.toLowerCase()} correctamente`
  this.messagesError[id] = false
        await this.cargarSolicitudes()
      } catch (e) {
  const msg = (e && e.response && (e.response.data || e.response.statusText)) || 'Error al actualizar estado'
  this.messages[id] = msg
  this.messagesError[id] = true
      }
    },

    // Open confirm modal
    askConfirm(id, action) {
      this.confirmTarget = id
      this.confirmAction = action
      this.confirmLabel = action === 'ACEPTADA' ? 'aceptar' : 'rechazar'
      this.confirmOpen = true
    },

    // Confirmed by user -> call cambiarEstado
    async cambiarEstadoConfirmed() {
      const id = this.confirmTarget
      const action = this.confirmAction
      this.confirmOpen = false
      try {
        await this.cambiarEstado(id, action)
      } finally {
        this.confirmTarget = null
        this.confirmAction = null
        this.confirmLabel = ''
      }
    },

    cancelConfirm() {
      this.confirmOpen = false
      this.confirmTarget = null
      this.confirmAction = null
      this.confirmLabel = ''
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

/* Header */
.page-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid var(--primary-color);
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-hover));
  border-radius: 12px;
  color: var(--text-inverse);
  flex-shrink: 0;
}

.page-header h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
}

.subtitle {
  margin: 0.25rem 0 0 0;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* Loading */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  gap: 1rem;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid var(--border);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-container p {
  color: var(--text-secondary);
  margin: 0;
}

/* Alerts */
.alert {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.alert svg {
  flex-shrink: 0;
}

.alert-danger {
  background: var(--alert-danger-bg);
  color: var(--alert-danger-text);
  border: 1px solid var(--alert-danger-border);
}

.alert-success {
  background: var(--alert-success-bg);
  color: var(--alert-success-text);
  border: 1px solid var(--alert-success-border);
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-state svg {
  color: var(--text-secondary);
  opacity: 0.3;
  margin-bottom: 1.5rem;
}

.empty-state h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.875rem;
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
  background: var(--success-color);
  color: var(--text-inverse);
}

.btn-accept:hover {
  background: var(--success-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.btn-reject {
  background: var(--danger-color);
  color: var(--text-inverse);
}

.btn-reject:hover {
  background: var(--danger-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: var(--surface-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 2rem;
  max-width: 400px;
  width: 90%;
  box-shadow: var(--shadow-xl);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.modal-header svg {
  flex-shrink: 0;
}

.icon-success {
  color: var(--success-color);
}

.icon-danger {
  color: var(--danger-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
}

.modal-text {
  text-align: center;
  color: var(--text-secondary);
  margin-bottom: 2rem;
  font-size: 1rem;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  flex-direction: column;
}

@media (min-width: 400px) {
  .modal-actions {
    flex-direction: row;
  }
}

.btn-confirm-accept,
.btn-confirm-reject,
.btn-cancel {
  flex: 1;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-confirm-accept {
  background: var(--success-color);
  color: var(--text-inverse);
}

.btn-confirm-accept:hover {
  background: var(--success-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.btn-confirm-reject {
  background: var(--danger-color);
  color: var(--text-inverse);
}

.btn-confirm-reject:hover {
  background: var(--danger-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.btn-cancel {
  background: var(--bg-light);
  color: var(--text-primary);
  border: 1.5px solid var(--border);
}

.btn-cancel:hover {
  background: var(--border);
}

/* Responsive */
@media (max-width: 768px) {
  .solicitudes-page {
    padding: 1.5rem 1rem;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .header-icon {
    width: 48px;
    height: 48px;
  }

  .page-header h1 {
    font-size: 1.5rem;
  }

  .card-actions {
    flex-direction: column;
  }
}
</style>
