<template>
  <div class="page">
    <h1>Mis solicitudes</h1>

    <p v-if="loading">Cargando solicitudes...</p>
    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="!loading && solicitudes.length === 0">
      <p>No tienes solicitudes.</p>
    </div>

    <ul v-if="!loading && solicitudes.length">
      <li v-for="s in solicitudes" :key="s.id" class="solicitud-item">
        <div class="solicitud-main">
          <div>
            <b>De médico:</b> {{ s.medico?.nombre || '-' }} ({{ s.medico?.nif || '-' }})
          </div>
          <div>
            <b>Paciente:</b> {{ s.paciente?.nombre || '-' }} ({{ s.paciente?.nif || '-' }})
          </div>
          <div>
            <b>Estado:</b> {{ s.estado }}
          </div>
          <div>
            <b>Creada:</b> {{ formatDate(s.fechaCreacion) || '-' }}
          </div>
        </div>

        <div class="solicitud-actions" v-if="s.estado === 'PENDIENTE'">
          <button @click="askConfirm(s.id, 'ACEPTADA')" class="accept">Aceptar</button>
          <button @click="askConfirm(s.id, 'RECHAZADA')" class="reject">Rechazar</button>
        </div>

        <p v-if="messages[s.id]" :class="{ error: messagesError[s.id], success: !messagesError[s.id] }">{{ messages[s.id] }}</p>
      </li>
    </ul>

    <div v-if="!loading && solicitudesEnviadas && solicitudesEnviadas.length">
      <h2>Solicitudes enviadas</h2>
      <ul>
        <li v-for="s in solicitudesEnviadas" :key="'env-'+s.id" class="solicitud-item">
          <div class="solicitud-main">
            <div>
              <b>De médico:</b> {{ s.medico?.nombre || '-' }} ({{ s.medico?.nif || '-' }})
            </div>
            <div>
              <b>Paciente:</b> {{ s.paciente?.nombre || '-' }} ({{ s.paciente?.nif || '-' }})
            </div>
            <div>
              <b>Estado:</b> {{ s.estado }}
            </div>
            <div>
              <b>Creada:</b> {{ formatDate(s.fechaCreacion) || '-' }}
            </div>
          </div>
        </li>
      </ul>
    </div>

    <!-- Confirm modal -->
    <div v-if="confirmOpen" class="modal-overlay">
      <div class="modal">
        <p>¿Estás seguro que deseas <strong>{{ confirmLabel }}</strong> esta solicitud?</p>
        <div class="modal-actions">
          <button @click="cambiarEstadoConfirmed" class="accept">Sí, {{ confirmLabel }}</button>
          <button @click="cancelConfirm" class="cancel">Cancelar</button>
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
        const resp = await axios.get(`${base}/usuario/solicitud/mis`, { headers: { Authorization: `Bearer ${token}` } })
        this.solicitudes = Array.isArray(resp.data) ? resp.data : []
        // if user is a medico, also load solicitudes they have sent
        const claims = authService.getCurrentUser()
        const roles = (claims && (claims.authorities || claims.roles || claims.scope)) || []
        const isMedico = Array.isArray(roles)
          ? roles.some(r => String(r).toUpperCase().includes('MEDICO'))
          : (typeof roles === 'string' && String(roles).toUpperCase().includes('MEDICO'))
        if (isMedico) {
          try {
            const r2 = await axios.get(`${base}/medico/solicitud/enviadas`, { headers: { Authorization: `Bearer ${token}` } })
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
        return new Date(v).toLocaleString()
      } catch (e) {
        return v
      }
    },

    // Directly call the API to change estado
    async cambiarEstado(id, nuevoEstado) {
      // reset messages for this id (Vue 3: use direct assignment)
      this.messages[id] = ''
      this.messagesError[id] = false
      try {
        const token = localStorage.getItem('authToken')
        const base = process.env.VUE_APP_API_URL || 'http://localhost:8081'
        await axios.post(`${base}/usuario/solicitud/${id}/estado`, { estado: nuevoEstado }, { headers: { Authorization: `Bearer ${token}` } })
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
.solicitud-item { border: 1px solid #eee; padding: 0.8rem; margin-bottom: 0.8rem; border-radius: 6px }
.solicitud-main { display:flex; gap:1rem; flex-wrap:wrap }
.accept { background:#388e3c;color:#fff;border:none;padding:0.4rem 0.8rem;border-radius:4px;margin-right:0.5rem }
.reject { background:#b00020;color:#fff;border:none;padding:0.4rem 0.8rem;border-radius:4px }

/* modal styles */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display:flex; align-items:center; justify-content:center; z-index: 1000 }
.modal { background: #fff; padding: 1rem 1.2rem; border-radius: 8px; min-width: 280px; box-shadow: 0 6px 18px rgba(0,0,0,0.2) }
.modal-actions { display:flex; gap:0.5rem; justify-content:flex-end; margin-top:0.8rem }
.cancel { background: #ccc; border: none; padding: 0.4rem 0.8rem; border-radius:4px }
</style>
