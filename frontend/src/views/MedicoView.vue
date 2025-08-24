<template>
  <div class="page">
    <h1>Zona Médica</h1>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="!alive && !isMedico">Comprobando acceso…</p>

    <div v-else>
      <p v-if="isMedico">Acceso verificado para rol MEDICO.</p>

      <!-- Search form visible for médicos -->
      <form @submit.prevent="buscarPaciente" class="search-form">
        <div class="form-group">
          <label for="dni">DNI del paciente</label>
          <input id="dni" v-model="dni" required />
        </div>
        <div class="form-group">
          <label for="fechaNacimiento">Fecha de nacimiento</label>
          <input id="fechaNacimiento" type="date" v-model="fechaNacimiento" required />
        </div>
        <button type="submit">Buscar paciente</button>
        <p v-if="searchError" class="error">{{ searchError }}</p>
      </form>

      <div v-if="paciente">
        <h2>Datos del paciente</h2>
        <ul>
          <li><b>Nombre:</b> {{ paciente.nombre }}</li>
          <li><b>Apellidos:</b> {{ paciente.apellido1 }} {{ paciente.apellido2 }}</li>
          <li><b>DNI:</b> {{ paciente.nif }}</li>
          <li><b>Fecha nacimiento:</b> {{ paciente.fechaNacimiento }}</li>
        </ul>
        <button @click="solicitarAsignacion" class="asignar-btn">Solicitar asignación</button>
        <p v-if="asignacionMsg" :class="{ error: asignacionError, success: !asignacionError }">{{ asignacionMsg }}</p>
      </div>

      <div class="pendientes" v-if="solicitudesPendientes && solicitudesPendientes.length">
        <h3>Solicitudes pendientes</h3>
        <ul>
          <li v-for="s in solicitudesPendientes" :key="s.id">
            Paciente: {{ s.paciente?.nif || (s.paciente && s.paciente.nif) }} — Estado: {{ s.estado }} — Fecha: {{ s.fechaCreacion }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import authService from '@/services/authService'
import { validateNIF } from '@/utils/validateNIF'

export default {
  name: 'MedicoView',
  data() {
    return {
      alive: false,
      error: '',
  solicitudesPendientes: [],
      dni: '',
      fechaNacimiento: '',
      searchError: '',
      paciente: null,
      asignacionMsg: '',
      asignacionError: false
    }
  },
  computed: {
    isMedico() {
      const claims = authService.getCurrentUser() || {}
      const roles = claims.authorities || claims.roles || []
      return Array.isArray(roles) && roles.some(r => String(r).toUpperCase().includes('MEDICO'))
    }
  },
  async created() {
    try {
      const token = localStorage.getItem('authToken')
      const base = process.env.VUE_APP_API_URL || 'http://localhost:8081'
      const { data } = await axios.get(base + '/medico/ping', { headers: { Authorization: `Bearer ${token}` } })
      this.alive = data === 'OK-MEDICO'
    } catch (e) {
      // keep error but still allow search UI if user has MEDICO role
      this.error = 'Sin permisos o no autenticado'
    }
    // Load pending solicitudes if current user has MEDICO role (show even if ping failed)
    if (this.isMedico) await this.cargarSolicitudesPendientes()
  },
  methods: {
    async buscarPaciente() {
      this.searchError = ''
      this.paciente = null
      if (!validateNIF(this.dni)) {
        this.searchError = 'DNI no válido.'
        return
      }
      if (!this.fechaNacimiento) {
        this.searchError = 'Debe introducir la fecha de nacimiento.'
        return
      }
      try {
        const token = localStorage.getItem('authToken')
        const base = process.env.VUE_APP_API_URL || 'http://localhost:8081'
        const { data } = await axios.get(
          `${base}/medico/paciente/buscar`,
          {
            params: { dni: this.dni, fechaNacimiento: this.fechaNacimiento },
            headers: { Authorization: `Bearer ${token}` }
          }
        )
        this.paciente = data
        if (!data || !data.nif) {
          this.searchError = 'No se encontró ningún paciente con esos datos.'
          this.paciente = null
        }
      } catch (e) {
        this.searchError = 'No se encontró ningún paciente con esos datos.'
        this.paciente = null
      }
    },
    async cargarSolicitudesPendientes() {
      try {
        const token = localStorage.getItem('authToken')
        const base = process.env.VUE_APP_API_URL || 'http://localhost:8081'
        const { data } = await axios.get(`${base}/medico/solicitud/pendientes`, { headers: { Authorization: `Bearer ${token}` } })
        this.solicitudesPendientes = Array.isArray(data) ? data : []
      } catch (e) {
        this.solicitudesPendientes = []
      }
    },
    async solicitarAsignacion() {
      this.asignacionMsg = ''
      this.asignacionError = false
      try {
        const token = localStorage.getItem('authToken')
        const base = process.env.VUE_APP_API_URL || 'http://localhost:8081'
        const nifPaciente = this.paciente.nif
        // Do not send nifMedico; backend should determine medico from the auth token
        await axios.get(
          `${base}/medico/solicitud/asignacion`,
          {
            params: { nifPaciente },
            headers: { Authorization: `Bearer ${token}` }
          }
        )
  this.asignacionMsg = 'Solicitud enviada correctamente.'
  this.asignacionError = false
  await this.cargarSolicitudesPendientes()
      } catch (e) {
        const backendMsg = e && e.response && e.response.data && (e.response.data.message || e.response.data.error)
        this.asignacionMsg = backendMsg || 'No se pudo enviar la solicitud.'
        this.asignacionError = true
      }
    }
  }
}
</script>

<style scoped>
.error { color: #b00020 }
.search-form {
  margin: 2rem 0;
  padding: 1rem;
  background: #f8f8f8;
  border-radius: 8px;
  max-width: 400px;
}
.form-group {
  margin-bottom: 1rem;
}
.form-group label {
  display: block;
  margin-bottom: 0.3rem;
}
.form-group input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}
button[type="submit"] {
  background: #1976d2;
  color: #fff;
  border: none;
  padding: 0.5rem 1.2rem;
  border-radius: 4px;
  cursor: pointer;
}
button[type="submit"]:hover {
  background: #125ea2;
}
.asignar-btn {
  margin-top: 1rem;
  background: #388e3c;
  color: #fff;
  border: none;
  padding: 0.5rem 1.2rem;
  border-radius: 4px;
  cursor: pointer;
}
.asignar-btn:hover {
  background: #256029;
}
.success {
  color: #388e3c;
  margin-top: 0.5rem;
}
</style>
