<template>
  <div class="page">
    <h1>{{$t('medical_zone')}}</h1>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="!alive && !isMedico">{{$t('checking_access')}}</p>

    <div v-else>
      <p v-if="isMedico">{{$t('access_verified')}}</p>

      <!-- Search form visible for médicos -->
      <form @submit.prevent="buscarPaciente" class="search-form">
        <div class="form-group">
          <label for="dni">{{$t('patient_dni_label')}}</label>
          <input id="dni" v-model="dni" required />
        </div>
        <div class="form-group">
          <label for="fechaNacimiento">{{$t('birth_date_label')}}</label>
          <input id="fechaNacimiento" type="date" v-model="fechaNacimiento" required />
        </div>
        <button type="submit">{{$t('search_patient_button')}}</button>
        <p v-if="searchError" class="error">{{ searchError }}</p>
      </form>

      <div v-if="paciente">
        <h2>{{$t('patient_data')}}</h2>
        <ul>
          <li><b>{{$t('name')}}:</b> {{ paciente.nombre }}</li>
          <li><b>{{$t('surnames')}}:</b> {{ paciente.apellido1 }} {{ paciente.apellido2 }}</li>
          <li><b>{{$t('dni')}}:</b> {{ paciente.nif }}</li>
          <li><b>{{$t('birth_date')}}:</b> {{ formatDate(paciente.fechaNacimiento) }}</li>
        </ul>
        <button @click="solicitarAsignacion" class="asignar-btn">{{$t('assign_request')}}</button>
        <p v-if="asignacionMsg" :class="{ error: asignacionError, success: !asignacionError }">{{ asignacionMsg }}</p>
      </div>

      <div class="pacientes" v-if="isMedico">
        <h3>{{$t('my_patients')}}</h3>
        <div v-if="misPacientes.length" class="pacientes-grid">
          <div v-for="p in misPacientes" :key="p.nif" class="paciente-card">
            <div class="paciente-info">
              <strong>{{ p.nombre }} {{ p.apellido1 }} {{ p.apellido2 }}</strong>
              <span>{{$t('dni')}}: {{ p.nif }}</span>
            </div>
            <div class="paciente-actions">
              <router-link
                :to="{ name: 'MedicoPacienteHistorial', params: { nif: p.nif }, query: { nombre: nombreCompleto(p) } }"
                class="ver-historial-btn">
                {{$t('view_history')}}
              </router-link>
              <button type="button" class="desasignar-btn" @click="pedirDesasignar(p)">
                {{$t('unassign_patient')}}
              </button>
            </div>
          </div>
        </div>
        <p v-else-if="!loadingPacientes">{{$t('no_patients_assigned')}}</p>
        <p v-if="desasignarMsg" :class="{ error: desasignarError, success: !desasignarError }">{{ desasignarMsg }}</p>
      </div>

      <div v-if="desasignarTarget" class="modal-overlay" @click="cancelarDesasignar">
        <div class="modal" @click.stop>
          <div class="modal-header">
            <h3>{{$t('confirm_action')}}</h3>
          </div>
          <p class="modal-text">
            {{$t('unassign_patient_confirm', { nombre: nombreCompleto(desasignarTarget) })}}
          </p>
          <div class="modal-actions">
            <button type="button" class="modal-btn modal-btn--confirm" :disabled="desasignando" @click="confirmarDesasignar">
              {{ desasignando ? $t('saving') : $t('unassign_patient') }}
            </button>
            <button type="button" class="modal-btn modal-btn--cancel" :disabled="desasignando" @click="cancelarDesasignar">
              {{$t('cancel')}}
            </button>
          </div>
        </div>
      </div>

      <div class="pendientes" v-if="solicitudesPendientes && solicitudesPendientes.length">
        <h3>{{$t('pending_requests')}}</h3>
        <ul>
          <li v-for="s in solicitudesPendientes" :key="s.id">
            {{$t('patient')}}: {{ s.paciente?.nif || (s.paciente && s.paciente.nif) }} — {{$t('state')}}: {{ s.estado }} — {{$t('date')}}: {{ formatDate(s.fechaCreacion) }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import medicoPacienteService from '@/services/medicoPacienteService'
import { validateNIF } from '@/utils/validateNIF'
import { useRole } from '@/composables/useRole'

export default {
  name: 'MedicoView',
  setup() {
    const { isMedico } = useRole()
    return { isMedico }
  },
  data() {
    return {
      error: '',
      solicitudesPendientes: [],
      misPacientes: [],
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

.page {
  padding: 2rem 1.5rem 2rem 3rem;
}

.search-form {
  margin: 2rem 0;
  padding: 1rem;
  background: var(--bg-light);
  border-radius: 8px;
  width: fit-content;
}
.form-group {
  margin-bottom: 1rem;
}
.form-group label {
  display: block;
  margin-bottom: 0.3rem;
}
.form-group input {
  width: 240px;
  padding: 0.5rem;
  border: 1px solid var(--border-medium);
  border-radius: 4px;
}
button[type="submit"] {
  background: var(--primary-color);
  color: var(--text-inverse);
  border: none;
  padding: 0.5rem 1.2rem;
  border-radius: 4px;
  cursor: pointer;
}
button[type="submit"]:hover {
  background: var(--primary-hover);
}
.asignar-btn {
  margin-top: 1rem;
  background: var(--success-color);
  color: var(--text-inverse);
  border: none;
  padding: 0.5rem 1.2rem;
  border-radius: 4px;
  cursor: pointer;
}
.asignar-btn:hover {
  background: var(--success-hover);
}
.success { margin-top: 0.5rem; }

.pacientes {
  margin: 2rem 0;
}

.pacientes-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
  margin-top: 1rem;
}

@media (min-width: 640px) {
  .pacientes-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.paciente-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-left: 4px solid var(--primary-color);
  border-radius: 8px;
  padding: 1rem;
}

.paciente-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.paciente-info span {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.ver-historial-btn {
  flex-shrink: 0;
  background: var(--primary-color);
  color: var(--text-inverse);
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  font-size: 0.875rem;
  white-space: nowrap;
}

.ver-historial-btn:hover {
  background: var(--primary-hover);
}

.paciente-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex-shrink: 0;
}

.desasignar-btn {
  background: transparent;
  color: var(--button-color);
  border: 1px solid var(--button-color);
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  white-space: nowrap;
}

.desasignar-btn:hover {
  background: var(--button-color);
  color: var(--on-button);
}

.modal-text {
  margin: 1rem 0;
}
</style>
