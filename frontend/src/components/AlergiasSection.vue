<template>
  <div class="section-panel card" role="tabpanel">
    <h3>Alergias e intolerancias</h3>
  <label class="visually-hidden" for="alergias-text">Alergias e intolerancias</label>
  <textarea id="alergias-text" v-model="alergias" rows="4" placeholder='Ejemplo: Penicilina — reacción: erupción — gravedad: moderada. Separa alergias por línea.' aria-describedby="alergias-hint"></textarea>
  <p id="alergias-hint" class="hint">Puedes escribir cada alergia en una línea: sustancia — reacción — gravedad.</p>
    <div class="form-actions">
      <button @click="saveAlergias" :disabled="savingAlergias">{{ savingAlergias ? 'Guardando…' : 'Guardar alergias' }}</button>
      <span v-if="msgAler" class="success">{{ msgAler }}</span>
    </div>
    <div v-if="alergiasList && alergiasList.length" class="existing-allergies">
      <h4>Listado de alergias registradas</h4>
      <ul>
        <li v-for="a in alergiasList" :key="a.id">
          <strong>{{ a.observacion || a.tipo }}</strong>
          <div class="meta small">Tipo: {{ a.tipo }}</div>
          <div class="actions"><button @click="removeAlergia(a.id)">Eliminar</button></div>
        </li>
      </ul>
    </div>
    
  </div>
</template>

<script>
export default {
  name: 'AlergiasSection',
  data() {
    return {
      alergias: '',
      alergiasList: [],
      savingAlergias: false,
      msgAler: '',
      error: null
    }
  },
  created() { this.load() },
  methods: {
    async load() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getMine()
        const dto = res.data || {}
        this.alergias = dto.alergiasJson || ''
        this.alergiasList = Array.isArray(dto.datosClinicos) ? dto.datosClinicos.filter(d => (d.tipo || '').toUpperCase().includes('ALERGIA')) : []
      } catch (e) { console.error('No se pudo cargar alergias', e); this.error = 'No se pudo cargar alergias' }
    },

    async saveAlergias() {
      this.savingAlergias = true
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.updateAlergias(this.alergias)
        await this.load()
        this.msgAler = 'Alergias guardadas.'
        // clear textarea after saving to indicate stored
        this.alergias = ''
        setTimeout(() => this.msgAler = '', 3000)
      } catch (e) { this.error = 'Error guardando alergias' } finally { this.savingAlergias = false }
    },

    async removeAlergia(id) {
      if (!confirm('Eliminar esta alergia?')) return
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.deleteDatoClinico(id)
        await this.load()
      } catch (e) { this.error = 'No se pudo eliminar alergia' }
    }
  }
}
</script>

<style scoped>
/* reuse .toast if needed in parent */
/* align save button and hint */
</style>
