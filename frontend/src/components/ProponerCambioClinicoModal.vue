<template>
  <AppModal :label="$t('propose_change_title')" @close="$emit('close')">
    <template #header>
      <h3>{{ $t('propose_change_title') }}</h3>
    </template>

    <p class="modal-subtitle">{{ $t('propose_change_subtitle') }}</p>

    <form class="proponer-form" @submit.prevent="onSubmit">
      <!-- Valor actual (edición y borrado) -->
      <div v-if="operacion !== 'CREATE'" class="current-value">
        <span class="field-label">{{ $t('propose_change_current_value') }}</span>
        <p class="current-value__text">{{ descripcionActual }}</p>
      </div>

      <!-- Antecedente -->
      <template v-if="dominio === 'ANTECEDENTE' && operacion !== 'DELETE'">
        <div class="form-group">
          <label class="form-label" for="proponer-categoria">{{ $t('antecedente_category_label') }}</label>
          <select id="proponer-categoria" v-model="form.categoria" class="form-input" required>
            <option value="PERSONAL">{{ $t('category_personal') }}</option>
            <option value="FAMILIAR">{{ $t('category_familiar') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label" for="proponer-descripcion">{{ $t('propose_change_proposed_value') }}</label>
          <textarea id="proponer-descripcion" v-model="form.descripcion" class="form-input" rows="3" required></textarea>
        </div>
      </template>

      <!-- Alergia -->
      <template v-if="dominio === 'ALERGIA' && operacion !== 'DELETE'">
        <div class="form-group">
          <label class="form-label" for="proponer-alergia">{{ $t('propose_change_proposed_value') }}</label>
          <input id="proponer-alergia" v-model="form.descripcion" class="form-input" type="text" required />
        </div>
      </template>

      <!-- Medición -->
      <template v-if="esMedicion && operacion !== 'DELETE'">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="proponer-parametro">{{ $t('parameter_required') }}</label>
            <input id="proponer-parametro" v-model="form.parametro" class="form-input" type="text" required />
          </div>
          <div class="form-group">
            <label class="form-label" for="proponer-valor">{{ $t('value_required') }}</label>
            <input id="proponer-valor" v-model="form.valor" class="form-input" type="text" inputmode="decimal" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="proponer-unidad">{{ $t('field_unit') }}</label>
            <input id="proponer-unidad" v-model="form.unidad" class="form-input" type="text" />
          </div>
          <div class="form-group">
            <label class="form-label" for="proponer-fecha">{{ $t('date_required') }}</label>
            <input id="proponer-fecha" v-model="form.fecha" class="form-input" type="datetime-local" />
          </div>
        </div>
      </template>

      <!-- Motivo (obligatorio siempre) -->
      <div class="form-group">
        <label class="form-label" for="proponer-motivo">{{ $t('propose_change_reason_label') }}</label>
        <textarea
          id="proponer-motivo"
          v-model="form.motivo"
          class="form-input"
          rows="2"
          :placeholder="$t('propose_change_reason_placeholder')"
          maxlength="1000"
          required
        ></textarea>
      </div>
    </form>

    <template #footer>
      <div class="modal-actions">
        <button type="button" class="btn-secondary" @click="$emit('close')">{{ $t('cancel') }}</button>
        <button type="button" class="btn-primary" :disabled="!valido || saving" @click="onSubmit">
          <span v-if="saving" class="spinner-small"></span>
          {{ $t('propose_change_send') }}
        </button>
      </div>
    </template>
  </AppModal>
</template>

<script>
import AppModal from './Modal.vue'

export default {
  name: 'ProponerCambioClinicoModal',
  components: { AppModal },
  props: {
    dominio: { type: String, required: true },
    operacion: { type: String, required: true },
    entrada: { type: Object, default: null },
    saving: { type: Boolean, default: false }
  },
  emits: ['submit', 'close'],
  data() {
    const entrada = this.entrada || {}
    return {
      form: {
        categoria: entrada.categoria || 'PERSONAL',
        descripcion: this.operacion === 'UPDATE' ? (entrada.descripcion || '') : '',
        parametro: this.operacion === 'UPDATE' ? (entrada.tipo || '') : '',
        valor: this.operacion === 'UPDATE' ? (entrada.valor || '') : '',
        unidad: this.operacion === 'UPDATE' ? (entrada.unidad || '') : '',
        fecha: '',
        motivo: ''
      }
    }
  },
  computed: {
    esMedicion() {
      return ['ANALISIS_SANGRE', 'SIGNOS_VITALES', 'ANALISIS_ORINA'].includes(this.dominio)
    },
    descripcionActual() {
      const e = this.entrada || {}
      if (this.dominio === 'ANTECEDENTE') return `${e.categoria || ''}: ${e.descripcion || ''}`.trim()
      if (this.dominio === 'ALERGIA') return e.descripcion || ''
      return `${e.tipo || ''}: ${e.valor || ''} ${e.unidad || ''}`.trim()
    },
    valido() {
      if (!this.form.motivo.trim()) return false
      if (this.operacion === 'DELETE') return true
      if (this.dominio === 'ANTECEDENTE') return !!this.form.descripcion.trim()
      if (this.dominio === 'ALERGIA') return !!this.form.descripcion.trim()
      return !!this.form.parametro.trim() && !!this.form.valor.trim()
    }
  },
  methods: {
    onSubmit() {
      if (!this.valido || this.saving) return
      const payload = {
        dominio: this.dominio,
        operacion: this.operacion,
        idRecursoObjetivo: this.operacion === 'CREATE' ? null : (this.entrada && this.entrada.id) || null,
        motivo: this.form.motivo.trim()
      }
      if (this.operacion !== 'DELETE') {
        if (this.dominio === 'ANTECEDENTE') {
          payload.antecedente = { categoria: this.form.categoria, descripcion: this.form.descripcion.trim() }
        } else if (this.dominio === 'ALERGIA') {
          payload.alergia = { descripcion: this.form.descripcion.trim() }
        } else {
          payload.medicion = {
            label: this.form.parametro.trim(),
            value: this.form.valor.trim(),
            unit: this.form.unidad.trim(),
            createdAt: this.form.fecha ? new Date(this.form.fecha).toISOString() : null
          }
        }
      }
      this.$emit('submit', payload)
    }
  }
}
</script>

<style scoped>
.modal-subtitle {
  margin: 0 0 1rem;
  font-size: 0.875rem;
  color: var(--text-secondary);
}
.proponer-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}
.field-label,
.form-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.025em;
}
.current-value {
  background: var(--bg-light);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 0.75rem 1rem;
}
.current-value__text {
  margin: 0.25rem 0 0;
  color: var(--text-primary);
  word-break: break-word;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
.spinner-small {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid var(--white-alpha-30);
  border-top-color: var(--text-inverse);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  vertical-align: middle;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
