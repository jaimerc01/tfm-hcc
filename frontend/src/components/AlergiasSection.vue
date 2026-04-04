<template>
  <div class="alergias-section" role="tabpanel">
    <div class="info-box">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      <span>
        <strong>{{ $t('register_allergies') }}</strong> {{ $t('doctor_consider') }}
        {{ $t('allergy_line_instruction') }}
      </span>
    </div>

    <div class="form-group full-width">
      <label class="form-label" for="alergias-text">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
          <line x1="12" y1="9" x2="12" y2="13"></line>
          <line x1="12" y1="17" x2="12.01" y2="17"></line>
        </svg>
        {{ $t('add_new_allergy') }}
      </label>
      <textarea 
        id="alergias-text" 
        v-model="alergias" 
        class="form-input"
        rows="4" 
        :placeholder="$t('allergy_placeholder')"
        aria-describedby="alergias-hint"></textarea>
      <p id="alergias-hint" class="form-help">
        {{ $t('allergy_hint') }}
      </p>
    </div>

    <div v-if="msgAler" class="alert alert-success" role="status" aria-live="polite">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12"></polyline>
      </svg>
      {{ msgAler }}
    </div>

    <div class="form-actions form-actions--right">
      <button 
        type="button"
        class="btn-primary"
        @click="saveAlergias" 
        :disabled="savingAlergias || !alergias.trim()">
        <div v-if="savingAlergias" class="spinner-small"></div>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 5v14"></path>
          <path d="M5 12h14"></path>
        </svg>
        {{ savingAlergias ? $t('saving') : $t('save_allergy') }}
      </button>
    </div>

    <!-- Lista de alergias registradas -->
    <div v-if="alergiasList && alergiasList.length" class="section">
      <div class="section-title">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 11H3v2h6m-6-5h6m-6 8h6m4-7h8m-8-3h8m-8 6h8m-8 3h8"></path>
        </svg>
        {{ $t('allergies_registered') }}
        <span class="badge badge-info">{{ alergiasList.length }}</span>
      </div>

      <div class="allergies-grid">
        <div v-for="a in alergiasList" :key="a.id" class="allergy-card">
          <div class="allergy-header">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="allergy-icon">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            <h4 v-html="(a.observacion || a.valor || a.tipo).replace(/\n/g, '<br>')"></h4>
          </div>
          <div class="allergy-body">
            <div class="allergy-meta">
              <span class="badge badge-warning">{{ a.tipo }}</span>
              <span v-if="a.createdAt" class="badge badge-date">{{ formatDateTime(a.createdAt) }}</span>
            </div>
          </div>
          <div class="allergy-actions">
            <button 
              type="button"
              class="btn-icon btn-danger"
              @click="openDelete(a)"
              :aria-label="`Eliminar alergia ${a.observacion || a.valor || a.tipo}`">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
            </button>
          </div>
        </div>
        <!-- Modal de confirmación para borrar alergia -->
        <AppModal v-if="showDelete && deleteAlergia" :label="$t('delete_allergy')" @close="closeDelete">
          <template #header>
            <div style="display:flex; flex-direction:column; align-items:center; gap:8px;">
              <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color:#d32f2f;">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
              <h3 style="margin:0; text-align:center;">{{ $t('delete_allergy') }}</h3>
            </div>
          </template>
          <div class="modal-body" style="text-align:center;">
            <p>{{ $t('delete_allergy_confirm') }}</p>
          </div>
          <template #footer>
            <div style="display:flex; justify-content:center; gap:12px;">
              <button type="button" class="btn-primary" @click="confirmDelete" :aria-label="$t('confirm')">
                {{ $t('confirm')}}
              </button>
              <button type="button" class="btn-secondary" @click="closeDelete">{{ $t('cancel') }}</button>
            </div>
          </template>
        </AppModal>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else class="empty-state-small">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      <p>{{ $t('no_allergies_registered') }}</p>
      <span>{{ $t('add_first_allergy_hint') }}</span>
    </div>
  </div>
</template>

<script>
import AppModal from './Modal.vue';
export default {
  name: 'AlergiasSection',
  components: { AppModal },
  data() {
    return {
      alergias: '',
      alergiasList: [],
      savingAlergias: false,
      msgAler: '',
      error: null,
      showDelete: false,
      deleteError: '',
      deleteAlergia: null
    }
  },
  created() { this.load() },
  methods: {
    async load() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getMine()
        const dto = res.data || {}
        console.log('DEBUG: Respuesta completa de getMine:', dto)
        console.log('DEBUG: datosClinicos:', dto.datosClinicos)
        console.log('DEBUG: analisisSangre:', dto.analisisSangre)
        this.alergias = dto.alergiasJson || ''
        this.alergiasList = Array.isArray(dto.datosClinicos) ? dto.datosClinicos.filter(d => (d.tipo || '').toUpperCase().includes('ALERGIA')) : []
        console.log('DEBUG: alergiasList filtrada:', this.alergiasList)
      } catch (e) { console.error(this.$t('error_loading_allergies'), e); this.error = this.$t('error_loading_allergies') }
    },

    async saveAlergias() {
      this.savingAlergias = true
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.updateAlergias(this.alergias)
        await this.load()
        this.msgAler = this.$t('allergies_saved')
        // clear textarea after saving to indicate stored
        this.alergias = ''
        setTimeout(() => this.msgAler = '', 3000)
      } catch (e) { this.error = this.$t('error_saving_allergies') } finally { this.savingAlergias = false }
    },

    openDelete(alergia) {
      this.deleteAlergia = alergia;
      this.showDelete = true;
      this.deleteError = '';
    },
    closeDelete() {
      this.showDelete = false;
      this.deleteAlergia = null;
      this.deleteError = '';
    },
    async confirmDelete() {
      if (!this.deleteAlergia) return;
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.deleteDatoClinico(this.deleteAlergia.id)
        await this.load()
        this.closeDelete()
      } catch (e) {
        this.deleteError = this.$t('error_deleting_allergy')
      }
    },

    formatDateTime(dt) {
      if (!dt) return '';
      const d = new Date(dt);
      if (isNaN(d)) return dt;
      return d.toLocaleString();
    }
  }
}
</script>

<style scoped>
.alergias-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-label svg {
  color: var(--warning-color, #d97706);
}

.form-help {
  font-size: 0.8125rem;
  color: var(--text-secondary, #737373);
  margin-top: 0.5rem;
  margin-bottom: 0;
}

/* Allergies Grid */
.allergies-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

@media (min-width: 768px) {
  .allergies-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.allergy-card {
  background: white;
  border: 1px solid var(--border, #e5e5e5);
  border-left: 4px solid var(--warning-color, #d97706);
  border-radius: 8px;
  padding: 1rem;
  transition: all 0.2s ease;
}

.allergy-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.allergy-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.allergy-icon {
  color: var(--warning-color, #d97706);
  flex-shrink: 0;
}

.allergy-header h4 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
  word-break: break-word;
}

.allergy-body {
  margin-bottom: 0.75rem;
}

.allergy-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.allergy-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 0.75rem;
  border-top: 1px solid var(--border, #e5e5e5);
}

/* Empty state small variant */
.empty-state-small {
  text-align: center;
  padding: 3rem 2rem;
  background: var(--bg-light, #fafafa);
  border-radius: 12px;
  border: 1px dashed var(--border, #e5e5e5);
}

.empty-state-small svg {
  color: var(--text-secondary, #737373);
  opacity: 0.3;
  margin-bottom: 1rem;
}

.empty-state-small p {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
}

.empty-state-small span {
  font-size: 0.875rem;
  color: var(--text-secondary, #737373);
}
</style>
