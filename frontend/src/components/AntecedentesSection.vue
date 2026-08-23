<template>
  <section class="antecedentes-section" :aria-busy="saving ? 'true' : 'false'" aria-labelledby="antecedentes-heading">
    <h2 id="antecedentes-heading" class="sr-only">{{ $t('antecedentes_personales_familiares') }}</h2>
    <div class="info-box">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      <span>
        <strong>{{ $t('describe_antecedentes') }}</strong> {{ $t('familia_personal') }}
      </span>
    </div>

    <form class="add-form" @submit.prevent="addAntecedente">
      <div class="add-form-row">
        <div class="form-group">
          <label class="form-label" for="antecedente-categoria">{{ $t('antecedente_category_label') }}</label>
          <select id="antecedente-categoria" v-model="categoria" class="form-input">
            <option value="PERSONAL">{{ $t('category_personal') }}</option>
            <option value="FAMILIAR">{{ $t('category_familiar') }}</option>
          </select>
        </div>

        <div class="form-group grow">
          <label class="form-label" for="antecedente-descripcion">{{ $t('antecedente_description_label') }}</label>
          <input
            id="antecedente-descripcion"
            type="text"
            v-model="descripcion"
            class="form-input"
            maxlength="500"
            :placeholder="$t('antecedente_placeholder')"
            required
            aria-required="true"
            :aria-invalid="error ? 'true' : 'false'" />
        </div>

        <button
          type="submit"
          class="btn-primary add-btn"
          :disabled="saving || !descripcion.trim()"
          :aria-disabled="saving || !descripcion.trim() ? 'true' : 'false'">
          <div v-if="saving" class="spinner-small"></div>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
            <path d="M12 5v14"></path>
            <path d="M5 12h14"></path>
          </svg>
          {{ saving ? $t('saving') : $t('add_antecedente') }}
        </button>
      </div>
    </form>

    <div v-if="error" class="alert alert-danger" role="alert" aria-live="assertive">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      {{ error }}
    </div>

    <div v-if="msg" class="alert alert-success" role="status" aria-live="polite">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <polyline points="20 6 9 17 4 12"></polyline>
      </svg>
      {{ msg }}
    </div>

    <!-- Antecedentes agrupados por categoría -->
    <div v-if="groupedAntecedentes.length" class="antecedentes-groups">
      <div v-for="group in groupedAntecedentes" :key="group.key" class="section">
        <div class="section-title">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
          {{ group.label }}
          <span class="badge badge-info">{{ group.items.length }}</span>
        </div>

        <div class="antecedentes-grid">
          <div v-for="a in group.items" :key="a.id" class="antecedente-card">
            <template v-if="editingId === a.id">
              <div class="form-group">
                <label class="sr-only" :for="'edit-cat-' + a.id">{{ $t('antecedente_category_label') }}</label>
                <select :id="'edit-cat-' + a.id" v-model="editCategoria" class="form-input">
                  <option value="PERSONAL">{{ $t('category_personal') }}</option>
                  <option value="FAMILIAR">{{ $t('category_familiar') }}</option>
                </select>
              </div>
              <div class="form-group">
                <label class="sr-only" :for="'edit-desc-' + a.id">{{ $t('antecedente_description_label') }}</label>
                <input :id="'edit-desc-' + a.id" type="text" v-model="editDescripcion" class="form-input" maxlength="500" />
              </div>
              <div class="antecedente-actions">
                <button type="button" class="btn-primary" @click="saveEdit(a)" :disabled="editSaving || !editDescripcion.trim()">
                  <div v-if="editSaving" class="spinner-small"></div>
                  {{ editSaving ? $t('saving') : $t('save_changes') }}
                </button>
                <button type="button" class="btn-secondary" @click="cancelEdit">{{ $t('cancel') }}</button>
              </div>
            </template>
            <template v-else>
              <div class="antecedente-header">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="antecedente-icon">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                  <polyline points="14 2 14 8 20 8"></polyline>
                </svg>
                <h3>{{ a.descripcion }}</h3>
              </div>
              <div class="antecedente-body">
                <div class="antecedente-meta">
                  <span v-if="a.createdAt" class="badge badge-date">{{ formatDateTime(a.createdAt) }}</span>
                </div>
              </div>
              <div class="antecedente-actions">
                <button
                  type="button"
                  class="btn-icon"
                  @click="startEdit(a)"
                  :title="$t('edit')"
                  :aria-label="$t('edit')">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                  </svg>
                </button>
                <button
                  type="button"
                  class="btn-icon btn-danger"
                  @click="openDelete(a)"
                  :title="$t('delete_antecedente')"
                  :aria-label="$t('delete_antecedente_item_aria', { antecedente: a.descripcion })">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                  </svg>
                </button>
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- Modal de confirmación para borrar antecedente -->
      <AppModal v-if="showDelete && deleteAntecedenteItem" :label="$t('delete_antecedente')" @close="closeDelete">
        <template #header>
          <div style="display:flex; flex-direction:column; align-items:center; gap:8px;">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color:var(--danger-color);">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
            <h3 style="margin:0; text-align:center;">{{ $t('delete_antecedente') }}</h3>
          </div>
        </template>
        <div class="modal-body" style="text-align:center;">
          <p>{{ $t('delete_antecedente_confirm') }}</p>
          <p v-if="deleteError" class="modal-error" role="alert" aria-live="assertive">{{ deleteError }}</p>
        </div>
        <template #footer>
          <div style="display:flex; justify-content:center; gap:12px;">
            <button type="button" class="btn-primary" @click="confirmDelete" :aria-label="$t('confirm')">
              {{ $t('confirm') }}
            </button>
            <button type="button" class="btn-secondary" @click="closeDelete">{{ $t('cancel') }}</button>
          </div>
        </template>
      </AppModal>
    </div>

    <!-- Empty state -->
    <div v-else class="empty-state-small">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
        <circle cx="9" cy="7" r="4"></circle>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
      </svg>
      <p>{{ $t('no_antecedentes_registered') }}</p>
      <span>{{ $t('add_first_antecedente_hint') }}</span>
    </div>
  </section>
</template>

<script>
import AppModal from './Modal.vue'

export default {
  name: 'AntecedentesSection',
  components: { AppModal },
  data() {
    return {
      categoria: 'PERSONAL',
      descripcion: '',
      antecedentes: [],
      saving: false,
      msg: '',
      error: null,
      editingId: null,
      editCategoria: '',
      editDescripcion: '',
      editSaving: false,
      showDelete: false,
      deleteError: '',
      deleteAntecedenteItem: null,
      successTimer: null
    }
  },
  created() { this.load() },
  computed: {
    groupedAntecedentes() {
      const personal = this.antecedentes.filter(a => a.categoria === 'PERSONAL')
      const familiar = this.antecedentes.filter(a => a.categoria === 'FAMILIAR')
      const groups = []
      if (personal.length) groups.push({ key: 'PERSONAL', label: this.$t('category_personal'), items: personal })
      if (familiar.length) groups.push({ key: 'FAMILIAR', label: this.$t('category_familiar'), items: familiar })
      return groups
    }
  },
  methods: {
    async load() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getMine()
        const dto = res.data || {}
        this.antecedentes = Array.isArray(dto.antecedentes) ? dto.antecedentes : []
      } catch (e) { console.error(this.$t('error_loading_antecedentes'), e); this.error = this.$t('error_loading_antecedentes') }
    },

    async addAntecedente() {
      const texto = this.descripcion.trim()
      if (!texto) return
      this.saving = true
      this.error = null
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.crearAntecedente({ categoria: this.categoria, descripcion: texto })
        this.descripcion = ''
        await this.load()
        this.msg = this.$t('saved_antecedentes')
        if (this.successTimer) clearTimeout(this.successTimer)
        this.successTimer = setTimeout(() => {
          this.msg = ''
          this.successTimer = null
        }, 3000)
      } catch (e) { this.error = this.$t('error_saving_antecedentes') } finally { this.saving = false }
    },

    startEdit(a) {
      this.editingId = a.id
      this.editCategoria = a.categoria
      this.editDescripcion = a.descripcion
    },
    cancelEdit() {
      this.editingId = null
      this.editCategoria = ''
      this.editDescripcion = ''
    },
    async saveEdit(a) {
      const texto = this.editDescripcion.trim()
      if (!texto) return
      this.editSaving = true
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.editarAntecedente(a.id, { categoria: this.editCategoria, descripcion: texto })
        await this.load()
        this.cancelEdit()
      } catch (e) {
        this.error = this.$t('error_editing_antecedente')
      } finally {
        this.editSaving = false
      }
    },

    openDelete(antecedente) {
      this.deleteAntecedenteItem = antecedente
      this.showDelete = true
      this.deleteError = ''
    },
    closeDelete() {
      this.showDelete = false
      this.deleteAntecedenteItem = null
      this.deleteError = ''
    },
    async confirmDelete() {
      if (!this.deleteAntecedenteItem) return
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.deleteAntecedente(this.deleteAntecedenteItem.id)
        await this.load()
        this.closeDelete()
      } catch (e) {
        this.deleteError = this.$t('error_deleting_antecedente')
      }
    },

    formatDateTime(dt) {
      if (!dt) return ''
      const d = new Date(dt)
      if (isNaN(d)) return dt
      return d.toLocaleString()
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
.antecedentes-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.add-form-row {
  display: flex;
  gap: 0.75rem;
  align-items: flex-end;
  flex-wrap: wrap;
}

.add-form-row .form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.add-form-row .form-group.grow {
  flex: 1 1 260px;
}

.add-btn {
  flex-shrink: 0;
}

.antecedentes-groups {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.antecedentes-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

@media (min-width: 768px) {
  .antecedentes-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.antecedente-card {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-left: 4px solid var(--primary-color);
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  transition: all 0.2s ease;
}

.antecedente-card:hover {
  box-shadow: var(--shadow-soft);
  transform: translateY(-2px);
}

.antecedente-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.antecedente-icon {
  color: var(--primary-color);
  flex-shrink: 0;
}

.antecedente-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  word-break: break-word;
  white-space: pre-line;
}

.antecedente-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.antecedente-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
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
  outline: 3px solid var(--focus-color);
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
  .antecedente-card,
  .spinner-small,
  .btn-primary,
  .btn-icon {
    transition: none;
    animation: none;
    transform: none;
  }
}
</style>
