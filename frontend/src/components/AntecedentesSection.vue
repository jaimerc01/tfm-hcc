<template>
  <div class="section-panel card" role="tabpanel">
    <h3>Antecedentes personales y familiares</h3>
  <label class="visually-hidden" for="antecedentes-text">Antecedentes personales y familiares</label>
  <textarea id="antecedentes-text" v-model="antecedentesFamiliares" @input="onInputChange" rows="6" placeholder='Describe aquí, con tus palabras, enfermedades importantes en ti o en tu familia (por ejemplo: diabetes en padre, hipertensión, etc.)' aria-describedby="antecedentes-hint"></textarea>
    <p id="antecedentes-hint" class="hint">Ejemplo: "Padre: diabetes tipo 2; Madre: hipertensión. Yo: asma en la infancia". No hace falta usar términos médicos exactos.</p>

    <div class="form-actions">
      <button @click="saveAntecedentes" :disabled="savingAntecedentes">{{ savingAntecedentes ? 'Guardando…' : 'Guardar antecedentes' }}</button>
      <span v-if="msgAnte" class="success">{{ msgAnte }}</span>
    </div>
    <div class="counter">{{ wordCount }} palabras · {{ charCount }} caracteres ({{ remainingChars }} restantes)</div>
    <div :class="['toast', showToast ? 'show' : '']">Guardado</div>
  </div>
</template>

<script>
export default {
  name: 'AntecedentesSection',
  data() {
    return {
      antecedentesFamiliares: '',
      savingAntecedentes: false,
      msgAnte: '',
      error: null,
      charLimit: 2000,
      autosaveTimer: null,
      autosaveDelay: 1500,
      showToast: false
    }
  },
  created() { this.load() },
  computed: {
    charCount() { return this.antecedentesFamiliares ? this.antecedentesFamiliares.length : 0 },
    wordCount() { return this.antecedentesFamiliares ? this.antecedentesFamiliares.trim().split(/\s+/).filter(Boolean).length : 0 },
    remainingChars() { return this.charLimit - this.charCount }
  },
  methods: {
    async load() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getMine()
        const dto = res.data || {}
        this.antecedentesFamiliares = dto.antecedentesFamiliares || ''
      } catch (e) { console.error('No se pudo cargar antecedentes', e); this.error = 'No se pudo cargar antecedentes' }
    },

    async saveAntecedentes() {
      this.savingAntecedentes = true
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.updateAntecedentes(this.antecedentesFamiliares)
        await this.load()
        this.msgAnte = 'Antecedentes guardados.'
        this.showTemporaryToast()
        setTimeout(() => this.msgAnte = '', 3000)
      } catch (e) { this.error = 'Error guardando antecedentes' } finally { this.savingAntecedentes = false }
    },

    onInputChange() {
      if (this.antecedentesFamiliares && this.antecedentesFamiliares.length > this.charLimit) {
        this.antecedentesFamiliares = this.antecedentesFamiliares.slice(0, this.charLimit)
      }
      if (this.autosaveTimer) clearTimeout(this.autosaveTimer)
      this.autosaveTimer = setTimeout(() => { this.saveAntecedentes().catch(()=>{}); this.autosaveTimer = null }, this.autosaveDelay)
    },

    showTemporaryToast() {
      this.showToast = true
      setTimeout(() => this.showToast = false, 2000)
    }
  }
}
</script>

<style scoped>
.counter { font-size: 0.85rem; color: var(--muted, #666); margin-top: 0.25rem }
.toast { position: fixed; right: 1rem; bottom: 1rem; background: var(--accent, #007bff); color: white; padding: 0.5rem 1rem; border-radius: 6px; box-shadow: 0 6px 18px rgba(0,0,0,0.15); opacity: 0; transform: translateY(8px); transition: opacity .25s ease, transform .25s ease }
.toast.show { opacity: 1; transform: translateY(0) }
</style>
