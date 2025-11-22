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
        <h1>Mis Solicitudes</h1>
        <p class="subtitle">Gestiona las solicitudes de acceso a tu historia clínica</p>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Cargando solicitudes...</p>
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
      <h3>No hay solicitudes</h3>
      <p>No tienes solicitudes pendientes en este momento</p>
    </div>

    <!-- Solicitudes recibidas -->
    <div v-if="!loading && solicitudes.length > 0" class="solicitudes-section">
      <h2 class="section-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
        </svg>
        Solicitudes Recibidas
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
                <span class="info-label">Médico</span>
                <span class="info-value">{{ s.medico?.nombre || '-' }}</span>
              </div>
            </div>

            <div class="info-row">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <div class="info-content">
                <span class="info-label">Paciente</span>
                <span class="info-value">{{ s.paciente?.nombre || '-' }}</span>
              </div>
            </div>
          </div>

          <div v-if="s.estado === 'PENDIENTE'" class="card-actions">
            <button @click="askConfirm(s.id, 'ACEPTADA')" class="btn-accept">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              Aceptar
            </button>
            <button @click="askConfirm(s.id, 'RECHAZADA')" class="btn-reject">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
              Rechazar
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
        Solicitudes Enviadas
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
                <span class="info-label">Paciente</span>
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
          <h3>Confirmar acción</h3>
        </div>
        <p class="modal-text">¿Estás seguro de que deseas <strong>{{ confirmLabel }}</strong> esta solicitud?</p>
        <div class="modal-actions">
          <button @click="cambiarEstadoConfirmed" :class="confirmAction === 'ACEPTADA' ? 'btn-confirm-accept' : 'btn-confirm-reject'">
            Sí, {{ confirmLabel }}
          </button>
          <button @click="cancelConfirm" class="btn-cancel">Cancelar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import authService from '../services/authService'

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
        const roles = (claims && (claims.authorities || claims.roles || claims.scope)) || []
        const isMedico = Array.isArray(roles)
          ? roles.some(r => String(r).toUpperCase().includes('MEDICO'))
          : (typeof roles === 'string' && String(roles).toUpperCase().includes('MEDICO'))
        if (isMedico) {
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
  border-bottom: 2px solid var(--primary-color, #0284c7);
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--primary-color, #0284c7), var(--primary-hover, #0369a1));
  border-radius: 12px;
  color: white;
  flex-shrink: 0;
}

.page-header h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary, #262626);
}

.subtitle {
  margin: 0.25rem 0 0 0;
  color: var(--text-secondary, #737373);
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
  border: 4px solid var(--border, #e5e5e5);
  border-top-color: var(--primary-color, #0284c7);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-container p {
  color: var(--text-secondary, #737373);
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
  background: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.alert-success {
  background: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-state svg {
  color: var(--text-secondary, #737373);
  opacity: 0.3;
  margin-bottom: 1.5rem;
}

.empty-state h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
}

.empty-state p {
  margin: 0;
  color: var(--text-secondary, #737373);
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
  color: var(--text-primary, #262626);
  margin: 0 0 1.5rem 0;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--border, #e5e5e5);
}

.section-title svg {
  color: var(--primary-color, #0284c7);
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
  background: white;
  border: 1px solid var(--border, #e5e5e5);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s ease;
}

.solicitud-card:hover {
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: var(--bg-light, #fafafa);
  border-bottom: 1px solid var(--border, #e5e5e5);
}

.card-date {
  font-size: 0.75rem;
  color: var(--text-secondary, #737373);
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
  background: #fef3c7;
  color: #92400e;
}

.status-accepted {
  background: #d1fae5;
  color: #065f46;
}

.status-rejected {
  background: #fee2e2;
  color: #991b1b;
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
  color: var(--primary-color, #0284c7);
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
  color: var(--text-secondary, #737373);
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.info-value {
  font-size: 0.875rem;
  color: var(--text-primary, #262626);
  font-weight: 500;
  word-break: break-word;
}

/* Card actions */
.card-actions {
  display: flex;
  gap: 0.5rem;
  padding: 1rem;
  border-top: 1px solid var(--border, #e5e5e5);
  background: var(--bg-light, #fafafa);
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
  background: var(--success-color, #16a34a);
  color: white;
}

.btn-accept:hover {
  background: #15803d;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
}

.btn-reject {
  background: var(--danger-color, #dc2626);
  color: white;
}

.btn-reject:hover {
  background: #b91c1c;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
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
  background: white;
  border-radius: 12px;
  padding: 2rem;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
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
  color: var(--success-color, #16a34a);
}

.icon-danger {
  color: var(--danger-color, #dc2626);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
}

.modal-text {
  text-align: center;
  color: var(--text-secondary, #737373);
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
  background: var(--success-color, #16a34a);
  color: white;
}

.btn-confirm-accept:hover {
  background: #15803d;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
}

.btn-confirm-reject {
  background: var(--danger-color, #dc2626);
  color: white;
}

.btn-confirm-reject:hover {
  background: #b91c1c;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
}

.btn-cancel {
  background: var(--bg-light, #fafafa);
  color: var(--text-primary, #262626);
  border: 1.5px solid var(--border, #e5e5e5);
}

.btn-cancel:hover {
  background: var(--border, #e5e5e5);
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
