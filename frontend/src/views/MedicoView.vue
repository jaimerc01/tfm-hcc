<template>
  <div class="page-container medico-page">
    <div class="page-header">
      <div class="header-icon">
        <AppIcon name="users" size="2xl" />
      </div>
      <div>
        <h1>{{ $t('medical_zone') }}</h1>
        <p class="subtitle">{{ $t('dashboard_access_medico_desc') }}</p>
      </div>
    </div>

    <div v-if="error" class="alert alert-danger" role="alert">
      <AppIcon name="alert-circle" size="lg" />
      {{ error }}
    </div>

    <div v-else-if="!alive && !isMedico" class="loading-container">
      <div class="spinner"></div>
      <p>{{ $t('checking_access') }}</p>
    </div>

    <template v-else>
      <div v-if="isMedico" class="alert alert-success" role="status">
        <AppIcon name="check-circle" size="lg" />
        {{ $t('access_verified') }}
      </div>

      <!-- Búsqueda de paciente -->
      <div class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('search_patient') }}</h2>
          <p class="panel-subtitle">{{ $t('search_patient_hint') }}</p>
        </div>

        <form @submit.prevent="buscarPaciente">
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label" for="dni">{{ $t('patient_dni_label') }}</label>
              <input id="dni" v-model="dni" class="form-input" required />
            </div>
            <div class="form-group">
              <label class="form-label" for="fechaNacimiento">{{ $t('birth_date_label') }}</label>
              <input id="fechaNacimiento" type="date" v-model="fechaNacimiento" class="form-input" required />
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-primary">
              <AppIcon name="search" size="md" />
              {{ $t('search_patient_button') }}
            </button>
          </div>
        </form>

        <div v-if="searchError" class="alert alert-danger" role="alert">
          <AppIcon name="alert-circle" size="lg" />
          {{ searchError }}
        </div>
      </div>

      <!-- Paciente encontrado -->
      <div v-if="paciente" class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('patient_data') }}</h2>
        </div>

        <div class="data-grid">
          <div class="data-item">
            <div class="data-label">{{ $t('name') }}</div>
            <div class="data-value">{{ paciente.nombre }}</div>
          </div>
          <div class="data-item">
            <div class="data-label">{{ $t('surnames') }}</div>
            <div class="data-value">{{ paciente.apellido1 }} {{ paciente.apellido2 }}</div>
          </div>
          <div class="data-item">
            <div class="data-label">{{ $t('dni') }}</div>
            <div class="data-value">{{ paciente.nif }}</div>
          </div>
          <div class="data-item">
            <div class="data-label">{{ $t('birth_date') }}</div>
            <div class="data-value">{{ formatDate(paciente.fechaNacimiento) }}</div>
          </div>
        </div>

        <div class="form-actions">
          <button @click="solicitarAsignacion" class="btn-primary">
            <AppIcon name="user-plus" size="md" />
            {{ $t('assign_request') }}
          </button>
        </div>

        <div v-if="asignacionMsg" :class="['alert', asignacionError ? 'alert-danger' : 'alert-success']" role="status">
          <AppIcon :name="asignacionError ? 'alert-circle' : 'check'" size="lg" />
          {{ asignacionMsg }}
        </div>
      </div>

      <!-- Solicitudes pendientes -->
      <div v-if="solicitudesPendientes && solicitudesPendientes.length" class="solicitudes-section">
        <h2 class="section-title">
          <AppIcon name="bookmark" size="lg" />
          {{ $t('pending_requests') }}
        </h2>

        <div class="solicitudes-grid">
          <div v-for="s in solicitudesPendientes" :key="s.id" class="solicitud-card">
            <div class="card-header">
              <span class="status-badge status-pending">{{ $t('request_status_pending') }}</span>
              <span class="card-date">{{ formatDate(s.fechaCreacion) }}</span>
            </div>
            <div class="card-body">
              <div class="info-row">
                <AppIcon name="user" size="sm" />
                <div class="info-content">
                  <span class="info-label">{{ $t('patient') }}</span>
                  <span class="info-value">{{ s.paciente?.nif || (s.paciente && s.paciente.nif) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Mis pacientes -->
      <div v-if="isMedico" class="solicitudes-section">
        <h2 class="section-title">
          <AppIcon name="users" size="lg" />
          {{ $t('my_patients') }}
        </h2>

        <div v-if="misPacientes.length" class="filters-form">
          <div class="form-group">
            <label class="form-label" for="filtro-pacientes">{{ $t('filter_patients_label') }}</label>
            <input
              id="filtro-pacientes"
              v-model="filtroPacientes"
              type="text"
              class="form-input"
              :placeholder="$t('filter_patients_placeholder')"
            />
          </div>
          <span class="filter-count">{{ $t('filter_patients_count', { count: pacientesFiltrados.length, total: misPacientes.length }) }}</span>
          <button v-if="filtroPacientes" type="button" class="btn-secondary" @click="filtroPacientes = ''">
            <AppIcon name="x" size="sm" />
            {{ $t('clear_filters') }}
          </button>
        </div>

        <div v-if="pacientesFiltrados.length" class="solicitudes-grid">
          <div v-for="p in pacientesFiltrados" :key="p.nif" class="solicitud-card">
            <div class="card-body">
              <div class="info-row">
                <AppIcon name="user" size="sm" />
                <div class="info-content">
                  <span class="info-label">{{ $t('patient') }}</span>
                  <span class="info-value">{{ nombreCompleto(p) }}</span>
                  <span class="info-value">{{ $t('dni') }}: {{ p.nif }}</span>
                </div>
              </div>
            </div>
            <div class="card-actions">
              <router-link
                :to="{ name: 'MedicoPacienteHistorial', params: { nif: p.nif }, query: { nombre: nombreCompleto(p) } }"
                class="btn-accept">
                <AppIcon name="file-text" size="sm" />
                {{ $t('view_history') }}
              </router-link>
              <button type="button" class="btn-reject" @click="pedirDesasignar(p)">
                <AppIcon name="x" size="sm" />
                {{ $t('unassign_patient') }}
              </button>
            </div>
          </div>
        </div>

        <div v-else-if="misPacientes.length && !loadingPacientes" class="empty-state">
          <AppIcon name="search" size="4xl" />
          <h3>{{ $t('no_patients_match_filter') }}</h3>
          <p>{{ $t('no_patients_match_filter_hint') }}</p>
        </div>

        <div v-else-if="!loadingPacientes" class="empty-state">
          <AppIcon name="users" size="4xl" />
          <h3>{{ $t('no_patients_assigned') }}</h3>
          <p>{{ $t('no_patients_assigned_hint') }}</p>
        </div>

        <div v-if="desasignarMsg" :class="['alert', desasignarError ? 'alert-danger' : 'alert-success']" role="status">
          <AppIcon :name="desasignarError ? 'alert-circle' : 'check'" size="lg" />
          {{ desasignarMsg }}
        </div>
      </div>
    </template>

    <!-- Confirmar desasignación -->
    <div v-if="desasignarTarget" class="modal-overlay" @click="cancelarDesasignar">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <AppIcon name="alert-triangle" size="3xl" class="icon-danger" />
          <h3>{{ $t('confirm_action') }}</h3>
        </div>
        <p class="modal-text">
          {{ $t('unassign_patient_confirm', { nombre: nombreCompleto(desasignarTarget) }) }}
        </p>
        <div class="modal-actions">
          <button type="button" class="modal-btn modal-btn--confirm" :disabled="desasignando" @click="confirmarDesasignar">
            {{ desasignando ? $t('saving') : $t('unassign_patient') }}
          </button>
          <button type="button" class="modal-btn modal-btn--cancel" :disabled="desasignando" @click="cancelarDesasignar">
            {{ $t('cancel') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import medicoPacienteService from '@/services/medicoPacienteService'
import { validateNIF } from '@/utils/validateNIF'
import { useRole } from '@/composables/useRole'
import AppIcon from '@/components/AppIcon.vue'

export default {
  name: 'MedicoView',
  components: { AppIcon },
  setup() {
    const { isMedico } = useRole()
    return { isMedico }
  },
  data() {
    return {
      error: '',
      solicitudesPendientes: [],
      misPacientes: [],
      filtroPacientes: '',
      loadingPacientes: false,
      dni: '',
      fechaNacimiento: '',
      paciente: null,
      asignacionMsg: '',
      asignacionError: false,
      searchError: '',
      alive: true,
      desasignarTarget: null,
      desasignando: false,
      desasignarMsg: '',
      desasignarError: false
    }
  },
  computed: {
    // Filtro en cliente por nombre o DNI: la lista de pacientes asignados ya
    // viene completa del backend, no hace falta ida y vuelta al servidor.
    pacientesFiltrados() {
      const q = this.filtroPacientes.trim().toLowerCase()
      if (!q) return this.misPacientes
      return this.misPacientes.filter(p => {
        const nombre = this.nombreCompleto(p).toLowerCase()
        const nif = (p.nif || '').toLowerCase()
        return nombre.includes(q) || nif.includes(q)
      })
    }
  },
  async created() {
    // Load pending solicitudes and assigned patients if current user has MEDICO role
    if (this.isMedico) {
      await Promise.all([this.cargarSolicitudesPendientes(), this.cargarMisPacientes()])
    }
  },
  methods: {
    async buscarPaciente() {
      this.searchError = ''
      this.paciente = null
      if (!validateNIF(this.dni)) {
        this.searchError = this.$t('invalid_nif')
        return
      }
      if (!this.fechaNacimiento) {
        this.searchError = this.$t('birth_date_required')
        return
      }
      try {
        const { data } = await medicoPacienteService.buscarPaciente(this.dni, this.fechaNacimiento)
        this.paciente = data
        if (!data || !data.nif) {
          this.searchError = this.$t('patient_not_found')
          this.paciente = null
        }
      } catch (e) {
        this.searchError = this.$t('patient_not_found')
        this.paciente = null
      }
    },
    async cargarSolicitudesPendientes() {
      try {
        const { data } = await medicoPacienteService.listarSolicitudesPendientes()
        this.solicitudesPendientes = Array.isArray(data) ? data : []
      } catch (e) {
        this.solicitudesPendientes = []
      }
    },
    async cargarMisPacientes() {
      this.loadingPacientes = true
      try {
        const { data } = await medicoPacienteService.listarMisPacientes()
        this.misPacientes = Array.isArray(data) ? data : []
      } catch (e) {
        this.error = this.$t('error_loading_patients')
        this.misPacientes = []
      } finally {
        this.loadingPacientes = false
      }
    },
    nombreCompleto(p) {
      return [p.nombre, p.apellido1, p.apellido2].filter(Boolean).join(' ')
    },
    formatDate(v) {
      if (!v) return ''
      const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(v)
      if (!match) return v
      const [, year, month, day] = match
      return `${day}/${month}/${year}`
    },
    async solicitarAsignacion() {
      this.asignacionMsg = ''
      this.asignacionError = false
      try {
        const nifPaciente = this.paciente.nif
        await medicoPacienteService.crearSolicitudAsignacion(nifPaciente)
        this.asignacionMsg = this.$t('request_sent_success')
        this.asignacionError = false
        await this.cargarSolicitudesPendientes()
      } catch (e) {
        const backendMsg = e && e.response && e.response.data && (e.response.data.message || e.response.data.error)
        this.asignacionMsg = backendMsg || this.$t('error_sending_request')
        this.asignacionError = true
      }
    },
    pedirDesasignar(p) {
      this.desasignarTarget = p
      this.desasignarMsg = ''
      this.desasignarError = false
    },
    cancelarDesasignar() {
      if (this.desasignando) return
      this.desasignarTarget = null
    },
    async confirmarDesasignar() {
      const p = this.desasignarTarget
      if (!p) return
      this.desasignando = true
      try {
        await medicoPacienteService.desasignarPaciente(p.nif)
        this.desasignarTarget = null
        this.desasignarMsg = this.$t('unassign_patient_success')
        this.desasignarError = false
        await this.cargarMisPacientes()
      } catch (e) {
        this.desasignarError = true
        this.desasignarMsg = e?.response?.status === 404
          ? this.$t('error_relationship_not_found')
          : this.$t('error_unassigning_patient')
      } finally {
        this.desasignando = false
      }
    }
  }
}
</script>

<style scoped>
.medico-page .alert {
  margin-bottom: 1.5rem;
}

.medico-page .panel-card {
  margin-bottom: 2rem;
}

/* Data Grid - specific to this view (same pattern as DatosUsuarioView) */
.data-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

@media (min-width: 640px) {
  .data-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.data-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  background: var(--bg-light);
  border-radius: 8px;
  border: 1px solid var(--border);
}

.data-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-value {
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--text-primary);
  word-break: break-word;
}

/* Solicitudes / pacientes en pastillas (mismo patrón que MisSolicitudesView) */
.solicitudes-section {
  margin-bottom: 3rem;
}

/* Filtro de "Mis pacientes" (mismo patrón que AccessLogSection) */
.filters-form {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: flex-end;
  padding: 1rem;
  background: var(--bg-light);
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.filters-form .form-group {
  min-width: 220px;
  flex: 1 1 220px;
}

.filter-count {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  white-space: nowrap;
  padding-bottom: 0.65rem;
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
  text-decoration: none;
  transition: all 0.2s ease;
  background: var(--button-color);
  color: var(--on-button);
}

.btn-accept:hover,
.btn-reject:hover {
  background: var(--button-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.modal-header svg {
  flex-shrink: 0;
}

.icon-danger {
  color: var(--danger-color);
}

@media (max-width: 768px) {
  .card-actions {
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .filters-form {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-count {
    padding-bottom: 0;
  }
}
</style>
