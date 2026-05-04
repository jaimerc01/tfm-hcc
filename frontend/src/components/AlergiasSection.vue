<template>
  <section class="alergias-section" :aria-busy="savingAlergias ? 'true' : 'false'" aria-labelledby="alergias-heading">
    <h2 id="alergias-heading" class="sr-only">{{ $t('allergies') }}</h2>
    <div class="info-box">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
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
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
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
        required
        aria-required="true"
        :aria-invalid="error ? 'true' : 'false'"
        :aria-describedby="error ? 'alergias-hint alergias-error' : 'alergias-hint'"></textarea>
      <p id="alergias-hint" class="form-help">
        {{ $t('allergy_hint') }}
      </p>
    </div>

    <div v-if="error" id="alergias-error" class="alert alert-danger" role="alert" aria-live="assertive">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      {{ error }}
    </div>

    <p v-if="savingAlergias" class="sr-only" role="status" aria-live="polite">
      {{ $t('saving') }}
    </p>

    <div v-if="msgAler" class="alert alert-success" role="status" aria-live="polite">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <polyline points="20 6 9 17 4 12"></polyline>
      </svg>
      {{ msgAler }}
    </div>

    <div class="form-actions form-actions--right">
      <button 
        type="button"
        class="btn-primary"
        @click="saveAlergias" 
        :disabled="savingAlergias || !alergias.trim()"
        :aria-disabled="savingAlergias || !alergias.trim() ? 'true' : 'false'">
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
            <h3>{{ allergyText(a) }}</h3>
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
              :title="$t('delete_allergy')"
              :aria-label="$t('delete_allergy_item_aria', { allergy: allergyText(a) })">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
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
              <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color:var(--danger-color);">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
              <h3 style="margin:0; text-align:center;">{{ $t('delete_allergy') }}</h3>
            </div>
          </template>
          <div class="modal-body" style="text-align:center;">
            <p>{{ $t('delete_allergy_confirm') }}</p>
            <p v-if="deleteError" class="modal-error" role="alert" aria-live="assertive">{{ deleteError }}</p>
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
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      <p>{{ $t('no_allergies_registered') }}</p>
      <span>{{ $t('add_first_allergy_hint') }}</span>
    </div>
  </section>
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
      deleteAlergia: null,
      successTimer: null
    }
  },
  created() { this.load() },
  methods: {
    allergyText(a) {
      return String((a && (a.observacion || a.valor || a.tipo)) || '')
    },

    async load() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getMine()
        const dto = res.data || {}
        this.alergias = dto.alergiasJson || ''
        this.alergiasList = Array.isArray(dto.datosClinicos) ? dto.datosClinicos.filter(d => (d.tipo || '').toUpperCase().includes('ALERGIA')) : []
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
        if (this.successTimer) clearTimeout(this.successTimer)
        this.successTimer = setTimeout(() => {
          this.msgAler = ''
          this.successTimer = null
        }, 3000)
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
  },
  beforeUnmount() {
    if (this.successTimer) {
      clearTimeout(this.successTimer)
      this.successTimer = null
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
  color: var(--warning-color);
}

.form-help {
  font-size: 0.8125rem;
  color: var(--text-secondary);
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
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-left: 4px solid var(--warning-color);
  border-radius: 8px;
  padding: 1rem;
  transition: all 0.2s ease;
}

.allergy-card:hover {
  box-shadow: var(--shadow-soft);
  transform: translateY(-2px);
}

.allergy-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.allergy-icon {
  color: var(--warning-color);
  flex-shrink: 0;
}

.allergy-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  word-break: break-word;
  white-space: pre-line;
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
  border-top: 1px solid var(--border);
}

.modal-error {
  color: var(--danger-color);
  font-weight: 600;
  margin-top: 0.75rem;
}

.form-input:focus-visible,
.btn-primary:focus-visible,
.btn-secondary:focus-visible,
.btn-icon:focus-visible {
  outline: 3px solid var(--focus-color, #005fcc);
  outline-offset: 2px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
  border: 0;
}

/* Empty state small variant */
.empty-state-small {
  text-align: center;
  padding: 3rem 2rem;
  background: var(--bg-light);
  border-radius: 12px;
  border: 1px dashed var(--border);
}

.empty-state-small svg {
  color: var(--text-secondary);
  opacity: 0.3;
  margin-bottom: 1rem;
}

.empty-state-small p {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state-small span {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

@media (prefers-reduced-motion: reduce) {
  .allergy-card,
  .spinner-small,
  .btn-primary,
  .btn-icon {
    transition: none;
    animation: none;
    transform: none;
  }
}
</style>
