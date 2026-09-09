<template>
  <AppModal :label="tituloModal" @close="$emit('close')">
    <template #header>
      <h3>{{ tituloModal }}</h3>
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
          <label class="form-label" for="proponer-descripcion">{{ valorLabel }}</label>
          <textarea id="proponer-descripcion" v-model="form.descripcion" class="form-input" rows="3" required></textarea>
        </div>
      </template>

      <!-- Alergia -->
      <template v-if="dominio === 'ALERGIA' && operacion !== 'DELETE'">
        <div class="form-group">
          <label class="form-label" for="proponer-alergia">{{ valorLabel }}</label>
          <input id="proponer-alergia" v-model="form.descripcion" class="form-input" type="text" required />
        </div>
      </template>

      <!-- Medición -->
      <template v-if="esMedicion && operacion !== 'DELETE'">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="proponer-parametro">{{ $t('parameter_required') }}</label>
            <!-- Alta: se elige de los parámetros que ya maneja la aplicación.
                 Edición: el parámetro es fijo, solo se propone otro valor. -->
            <select
              v-if="operacion === 'CREATE'"
              id="proponer-parametro"
              v-model="form.parametro"
              class="form-input"
              required
              @change="onParametroChange"
            >
              <option value="" disabled>{{ $t('analysis_validation_select_param') }}</option>
              <option v-for="a in analytesDominio" :key="a.key" :value="a.key">{{ $t(a.labelKey) }}</option>
            </select>
            <input
              v-else
              id="proponer-parametro"
              class="form-input"
              type="text"
              :value="parametroLabel"
              readonly
            />
          </div>
          <div class="form-group">
            <label class="form-label" for="proponer-valor">{{ $t('value_required') }}</label>
            <input id="proponer-valor" v-model="form.valor" class="form-input" type="text" inputmode="decimal" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="proponer-unidad">{{ $t('field_unit') }}</label>
            <input id="proponer-unidad" :value="form.unidad" class="form-input" type="text" readonly />
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
import { DEFAULT_ANALYTES as BLOOD_ANALYTES } from '@/composables/useAnalisisSangre'
import { DEFAULT_ANALYTES as VITAL_ANALYTES } from '@/composables/useSignosVitales'
import { DEFAULT_ANALYTES as URINE_ANALYTES } from '@/composables/useAnalisisOrina'

const MEDICION_DOMINIOS = ['ANALISIS_SANGRE', 'SIGNOS_VITALES', 'ANALISIS_ORINA']

// Parámetros que la aplicación reconoce para cada dominio de medición.
function analytesForDominio(dominio) {
  if (dominio === 'ANALISIS_SANGRE') return BLOOD_ANALYTES
  if (dominio === 'SIGNOS_VITALES') return VITAL_ANALYTES
  if (dominio === 'ANALISIS_ORINA') return URINE_ANALYTES
  return []
}

// Formatea una fecha al valor que espera un <input type="datetime-local">
// (hora local, sin zona, con precisión de minutos).
function toLocalDatetimeInput(value) {
  const d = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function findAnalyte(dominio, raw) {
  const norm = String(raw || '').trim().toLowerCase()
  if (!norm) return null
  return analytesForDominio(dominio).find(a =>
    a.key.toLowerCase() === norm ||
    (a.aliases || []).some(al => String(al).toLowerCase() === norm)
  ) || null
}

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
    const analyteActual = this.operacion === 'UPDATE' ? findAnalyte(this.dominio, entrada.tipo) : null
    // Medición: en una edición se parte de la fecha de la fila; en un alta, de
    // la fecha actual. En ambos casos el campo es editable.
    let fecha = ''
    if (MEDICION_DOMINIOS.includes(this.dominio)) {
      fecha = this.operacion === 'UPDATE'
        ? toLocalDatetimeInput(entrada.createdAt)
        : toLocalDatetimeInput(new Date())
    }
    return {
      form: {
        categoria: entrada.categoria || 'PERSONAL',
        descripcion: this.operacion === 'UPDATE' ? (entrada.descripcion || '') : '',
        parametro: this.operacion === 'UPDATE' ? (entrada.tipo || '') : '',
        valor: this.operacion === 'UPDATE' ? (entrada.valor || '') : '',
        unidad: this.operacion === 'UPDATE'
          ? (entrada.unidad || (analyteActual && analyteActual.unit) || '')
          : '',
        fecha,
        motivo: ''
      }
    }
  },
  computed: {
    // El título y la etiqueta del campo dependen de la operación: un alta no es
    // una modificación, así que no debe hablar de "cambio" ni de "valor propuesto".
    tituloModal() {
      return this.$t(`propose_change_title_${this.operacion}`)
    },
    valorLabel() {
      return this.operacion === 'CREATE'
        ? this.$t('propose_change_new_value')
        : this.$t('propose_change_proposed_value')
    },
    esMedicion() {
      return MEDICION_DOMINIOS.includes(this.dominio)
    },
    analytesDominio() {
      return analytesForDominio(this.dominio)
    },
    parametroLabel() {
      const a = findAnalyte(this.dominio, this.form.parametro)
      return a ? this.$t(a.labelKey) : this.form.parametro
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
    onParametroChange() {
      const a = this.analytesDominio.find(x => x.key === this.form.parametro)
      this.form.unidad = (a && a.unit) || ''
    },
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
