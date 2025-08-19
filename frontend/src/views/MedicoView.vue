<template>
  <div class="page">
    <h1>Zona Médica</h1>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="!alive">Comprobando acceso…</p>
    <p v-else>Acceso verificado para rol MEDICO.</p>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  name: 'MedicoView',
  data() { return { alive: false, error: '' } },
  async created() {
    try {
      const token = localStorage.getItem('authToken')
      const base = process.env.VUE_APP_API_URL || 'http://localhost:8081'
      const { data } = await axios.get(base + '/medico/ping', { headers: { Authorization: `Bearer ${token}` } })
      this.alive = data === 'OK-MEDICO'
    } catch (e) {
      this.error = 'Sin permisos o no autenticado'
    }
  }
}
</script>

<style scoped>
.error { color: #b00020 }
</style>
