<template>
  <section class="alergias-section" :aria-busy="saving ? 'true' : 'false'" aria-labelledby="alergias-heading">
    <h2 id="alergias-heading" class="sr-only">{{ $t('allergies') }}</h2>
    <div class="info-box">
      <AppIcon name="info" size="lg" />
      <span>
        <strong>{{ $t('register_allergies') }}</strong> {{ $t('doctor_consider') }}
      </span>
    </div>

    <form class="add-form" @submit.prevent="addAlergia">
      <div class="form-group full-width">
        <label class="form-label" for="alergia-input">
          <AppIcon name="alert-triangle" size="sm" />
          {{ $t('add_new_allergy') }}
        </label>
        <div class="input-row">
          <input
            id="alergia-input"
            type="text"
            v-model="descripcion"
            class="form-input"
            maxlength="500"
            :placeholder="$t('allergy_placeholder')"
            required
            aria-required="true"
            :aria-invalid="error ? 'true' : 'false'"
            :aria-describedby="error ? 'alergias-hint alergias-error' : 'alergias-hint'" />
          <button
            type="submit"
            class="btn-primary"
            :disabled="saving || !descripcion.trim()"
            :aria-disabled="saving || !descripcion.trim() ? 'true' : 'false'">
            <div v-if="saving" class="spinner-small"></div>
            <AppIcon v-else name="plus" size="md" />
            {{ saving ? $t('saving') : $t('add_allergy') }}
          </button>
        </div>
        <p id="alergias-hint" class="form-help">
          {{ $t('allergy_hint') }}
        </p>
      </div>
    </form>

    <div v-if="error" id="alergias-error" class="alert alert-danger" role="alert" aria-live="assertive">
      <AppIcon name="alert-circle" size="lg" />
      {{ error }}
    </div>

    <div v-if="msg" class="alert alert-success" role="status" aria-live="polite">
      <AppIcon name="check" size="lg" />
      {{ msg }}
    </div>

    <!-- Lista de alergias registradas -->
    <div v-if="alergiasList && alergiasList.length" class="section">
      <div class="section-title">
        <AppIcon name="list" size="lg" />
        {{ $t('allergies_registered') }}
        <span class="badge badge-info">{{ alergiasList.length }}</span>
      </div>

      <div class="allergies-grid">
        <div v-for="a in alergiasList" :key="a.id" class="allergy-card">
          <div class="allergy-header">
            <AppIcon name="alert-circle" size="lg" class="allergy-icon" />
            <h3 class="entry-title">{{ a.descripcion }}</h3>
          </div>
          <div class="allergy-body">
            <div class="allergy-meta">
              <span v-if="a.createdAt" class="badge badge-date">{{ formatDateTime(a.createdAt) }}</span>
            </div>
          </div>
          <div class="allergy-actions">
            <button
              type="button"
              class="btn-icon btn-danger"
              @click="openDelete(a)"
              :title="$t('delete_allergy')"
              :aria-label="$t('delete_allergy_item_aria', { allergy: a.descripcion })">
              <AppIcon name="trash" size="sm" />
            </button>
          </div>
        </div>
        <!-- Modal de confirmación para borrar alergia -->
        <AppModal v-if="showDelete && deleteAlergiaItem" :label="$t('delete_allergy')" @close="closeDelete">
          <template #header>
            <div style="display:flex; flex-direction:column; align-items:center; gap:8px;">
              <AppIcon name="trash" size="2xl" style="color:var(--danger-color);" />
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
      <AppIcon name="alert-circle" size="3xl" />
      <p>{{ $t('no_allergies_registered') }}</p>
      <span>{{ $t('add_first_allergy_hint') }}</span>
    </div>
  </section>
</template>

<script>
import AppModal from './Modal.vue';
import AppIcon from './AppIcon.vue';
export default {
  name: 'AlergiasSection',
  components: { AppModal, AppIcon },
  data() {
    return {
      descripcion: '',
      alergiasList: [],
      saving: false,
      msg: '',
      error: null,
      showDelete: false,
      deleteError: '',
      deleteAlergiaItem: null,
      successTimer: null
    }
  },
  created() { this.load() },
  methods: {
    async load() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getMine()
        const dto = res.data || {}
        this.alergiasList = Array.isArray(dto.alergias) ? dto.alergias : []
      } catch (e) { console.error(this.$t('error_loading_allergies'), e); this.error = this.$t('error_loading_allergies') }
    },

    async addAlergia() {
      const texto = this.descripcion.trim()
      if (!texto) return
      this.saving = true
      this.error = null
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.crearAlergia({ descripcion: texto })
        this.descripcion = ''
        await this.load()
        this.msg = this.$t('allergies_saved')
        if (this.successTimer) clearTimeout(this.successTimer)
        this.successTimer = setTimeout(() => {
          this.msg = ''
          this.successTimer = null
        }, 3000)
      } catch (e) { this.error = this.$t('error_saving_allergies') } finally { this.saving = false }
    },

    openDelete(alergia) {
      this.deleteAlergiaItem = alergia;
      this.showDelete = true;
      this.deleteError = '';
    },
    closeDelete() {
      this.showDelete = false;
      this.deleteAlergiaItem = null;
      this.deleteError = '';
    },
    async confirmDelete() {
      if (!this.deleteAlergiaItem) return;
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.deleteAlergia(this.deleteAlergiaItem.id)
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

.input-row {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.input-row .form-input {
  flex: 1 1 260px;
}

.input-row .btn-primary {
  flex-shrink: 0;
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

.form-input:focus-visible,
.btn-primary:focus-visible,
.btn-secondary:focus-visible,
.btn-icon:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 2px;
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
