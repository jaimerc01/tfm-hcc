<template>
  <div class="antecedentes-section">
    <div class="info-box">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      <span>
        <strong>{{ $t('describe_antecedentes') }}</strong> {{ $t('familia_personal') }}
        {{ $t('example') }}
      </span>
    </div>

    <div class="form-group full-width">
      <label class="form-label" for="antecedentes-text">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
          <circle cx="9" cy="7" r="4"></circle>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
        </svg>
        {{ $t('antecedentes_personales_familiares') }}
      </label>
      <textarea 
        id="antecedentes-text" 
        v-model="antecedentesFamiliares" 
        @input="onInputChange" 
        class="form-input textarea-large"
        rows="8" 
        :placeholder="$t('placeholder_antecedentes')"
        aria-describedby="antecedentes-counter"
        :maxlength="charLimit"></textarea>
      <div id="antecedentes-counter" class="char-counter">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
          <polyline points="9 11 12 14 22 4"></polyline>
          <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path>
        </svg>
        {{ $t('antecedentes_counter', { words: wordCount, chars: charCount, limit: charLimit }) }}
        <span v-if="remainingChars < 200" :class="remainingChars < 50 ? 'text-danger' : 'text-warning'">
          {{ $t('antecedentes_remaining', { remaining: remainingChars }) }}
        </span>
      </div>
    </div>

    <div v-if="error" class="alert alert-danger" role="alert" aria-live="assertive">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      {{ error }}
    </div>

    <div v-if="msgAnte" class="alert alert-success" role="status" aria-live="polite">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <polyline points="20 6 9 17 4 12"></polyline>
      </svg>
      {{ msgAnte }}
    </div>

    <div class="form-actions form-actions--right">
      <button 
        type="button"
        class="btn-primary"
        @click="saveAntecedentes" 
        :disabled="savingAntecedentes"
        :aria-disabled="savingAntecedentes ? 'true' : 'false'">
        <div v-if="savingAntecedentes" class="spinner-small"></div>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
          <polyline points="17 21 17 13 7 13 7 21"></polyline>
          <polyline points="7 3 7 8 15 8"></polyline>
        </svg>
        {{ savingAntecedentes ? $t('saving') : $t('save_antecedentes') }}
      </button>
    </div>

    <div :class="['toast-notification', showToast ? 'show' : '']" role="status" aria-live="polite" aria-atomic="true">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <polyline points="20 6 9 17 4 12"></polyline>
      </svg>
      {{ $t('saved_antecedentes') }}
    </div>
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
      } catch (e) { console.error(this.$t('error_loading_antecedentes'), e); this.error = this.$t('error_loading_antecedentes') }
    },

    async saveAntecedentes() {
      this.savingAntecedentes = true
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.updateAntecedentes(this.antecedentesFamiliares)
        await this.load()
        this.msgAnte = this.$t('saved_antecedentes')
        this.showTemporaryToast()
        setTimeout(() => this.msgAnte = '', 3000)
      } catch (e) { this.error = this.$t('error_saving_antecedentes') } finally { this.savingAntecedentes = false }
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
  },
  beforeUnmount() {
    if (this.autosaveTimer) {
      clearTimeout(this.autosaveTimer)
      this.autosaveTimer = null
    }
  }
}
</script>

<style scoped>
.antecedentes-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.textarea-large {
  min-height: 200px;
  resize: vertical;
  font-family: inherit;
  line-height: 1.6;
}

.char-counter {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin-top: 0.5rem;
}

.char-counter svg {
  color: var(--success-color);
  flex-shrink: 0;
}

.text-warning {
  color: var(--warning-color);
  font-weight: 600;
}

.text-danger {
  color: var(--danger-color);
  font-weight: 600;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-label svg {
  color: var(--primary-color);
}

.form-input:focus-visible,
.btn-primary:focus-visible {
  outline: 3px solid var(--focus-color, #005fcc);
  outline-offset: 2px;
}

.alert-danger {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 8px;
  background: var(--alert-danger-bg);
  color: var(--alert-danger-text);
  border: 1px solid var(--alert-danger-border);
}

.toast-notification {
  position: fixed;
  right: 2rem;
  bottom: 2rem;
  background: var(--success-color);
  color: var(--text-inverse);
  padding: 1rem 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow-toast);
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 500;
  z-index: 10000;
}

.toast-notification.show {
  opacity: 1;
  transform: translateY(0);
}

.toast-notification svg {
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .toast-notification {
    right: 1rem;
    bottom: 1rem;
    left: 1rem;
    width: auto;
  }
}

@media (prefers-reduced-motion: reduce) {
  .toast-notification,
  .spinner-small,
  .btn-primary {
    transition: none;
    animation: none;
    transform: none;
  }
}
</style>
