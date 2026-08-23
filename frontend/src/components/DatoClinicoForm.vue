<template>
  <div class="add-entry-section">
    <h3 class="section-subtitle">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 5v14"></path>
        <path d="M5 12h14"></path>
      </svg>
      {{ $t('add_new_result') }}
    </h3>

    <div class="form-grid-analisis">
      <div class="form-group">
        <label class="form-label" for="param-select">{{ $t('parameter_required') }} <span class="required">*</span></label>
        <select id="param-select" v-model="selected" class="form-input">
          <option v-for="a in analytes" :key="a.key" :value="a.key">{{ getAnalyteLabel(a) }}</option>
        </select>
      </div>

      <div class="form-group">
        <label class="form-label" for="value-input">{{ $t('value_required') }} <span class="required">*</span></label>
        <div class="input-with-unit">
          <input
            id="value-input"
            ref="valueInput"
            class="form-input"
            :class="{'input-error': !validation.isValid && String(value).trim() !== ''}"
            type="text"
            v-model="value"
            :placeholder="$t('analysis_value_placeholder')"
            inputmode="decimal"
            pattern="[0-9]+([.,][0-9]+)?"
            :aria-invalid="!validation.isValid && String(value).trim() !== '' ? 'true' : 'false'"
            :aria-describedby="!validation.isValid && String(value).trim() !== '' ? 'validation-error' : 'range-hint'" />
          <span class="unit-badge">{{ unitForSelected }}</span>
        </div>
        <div v-if="!validation.isValid && String(value).trim() !== ''" id="validation-error" class="form-error">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          {{ validation.message }}
        </div>
        <div v-else id="range-hint" class="form-help">{{ rangeHintText }}</div>
      </div>

      <div class="form-group">
        <label class="form-label" for="date-input">{{ $t('date_required') }} <span class="required">*</span></label>
        <input id="date-input" type="datetime-local" v-model="inputDate" class="form-input" />
        <p class="form-help">{{ $t('analysis_datetime_help') }}</p>
      </div>

      <div class="form-group form-actions-inline">
        <div class="form-label-spacer" aria-hidden="true"></div>
        <button
          type="button"
          class="btn-primary btn-add"
          @click="handleSubmit"
          :disabled="!canAdd || saving"
          :aria-label="saving ? $t('analysis_add_aria_saving', { domain: domainLabel }) : $t('analysis_add_aria', { domain: domainLabel })"
          :aria-describedby="!validation.isValid && String(value).trim() !== '' ? 'validation-error' : 'range-hint'">
          <div v-if="saving" class="spinner-small"></div>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 5v14"></path>
            <path d="M5 12h14"></path>
          </svg>
          {{ saving ? $t('saving') : $t('add_result') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { getAnalyteLabel } from '@/utils/datosClinicos'

export default {
  name: 'DatoClinicoForm',
  props: {
    analytes: { type: Array, required: true },
    saving: { type: Boolean, default: false },
    // Etiqueta traducida del dominio clínico (p. ej. "análisis de sangre", "signos vitales"),
    // usada en los textos ARIA para que reflejen el tipo de dato que se está añadiendo.
    domainLabel: { type: String, required: true }
  },
  emits: ['submit'],
  data() {
    return {
      selected: this.analytes.length ? this.analytes[0].key : '',
      value: '',
      // datetime-local input value (local time, e.g. '2025-08-28T15:30')
      inputDate: ''
    }
  },
  created() {
    this.inputDate = this.localNowForInput()
  },
  computed: {
    unitForSelected() {
      const a = this.analytes.find(x => x.key === this.selected)
      return a ? a.unit : ''
    },
    rangeHintText() {
      const a = this.analytes.find(x => x.key === this.selected)
      if (!a) return ''

      // Si hay rangos recomendados del servidor, mostrarlos
      if (a.recommendedMin !== null && a.recommendedMax !== null) {
        return this.$t('analysis_range_recommended', {
          min: a.recommendedMin,
          max: a.recommendedMax,
          unit: a.unit
        })
      }

      // Si no hay rangos recomendados, mostrar mensaje genérico
      return this.$t('analysis_range_enter_value', { unit: a.unit })
    },
    canAdd() { return this.value !== null && String(this.value).trim() !== '' && this.validation.isValid },
    validation() {
      const a = this.analytes.find(x => x.key === this.selected)
      const raw = String(this.value).trim()
      if (!a) return { isValid: false, message: this.$t('analysis_validation_select_param') }
      if (raw === '') return { isValid: false, message: this.$t('analysis_validation_enter_value') }
      // allow comma or dot as decimal separator
      const normalized = raw.replace(',', '.')
      const num = Number(normalized)
      if (Number.isNaN(num)) return { isValid: false, message: this.$t('analysis_validation_non_numeric') }
      if (a.validationMin != null && num < a.validationMin) {
        return { isValid: false, message: this.$t('analysis_validation_min', { min: a.validationMin, unit: a.unit }) }
      }
      if (a.validationMax != null && num > a.validationMax) {
        return { isValid: false, message: this.$t('analysis_validation_max', { max: a.validationMax, unit: a.unit }) }
      }
      return { isValid: true, message: '', value: num }
    }
  },
  methods: {
    getAnalyteLabel(analyte) {
      return getAnalyteLabel(analyte, this.$t)
    },
    focusFirstInvalidField() {
      if (!this.validation.isValid && this.$refs.valueInput) {
        this.$refs.valueInput.focus()
      }
    },
    localNowForInput() {
      const d = new Date()
      // get local iso without seconds fraction to match input step
      const pad = n => String(n).padStart(2, '0')
      const yyyy = d.getFullYear()
      const mm = pad(d.getMonth() + 1)
      const dd = pad(d.getDate())
      const hh = pad(d.getHours())
      const min = pad(d.getMinutes())
      return `${yyyy}-${mm}-${dd}T${hh}:${min}`
    },
    handleSubmit() {
      if (!this.canAdd) {
        this.focusFirstInvalidField()
        return
      }

      const a = this.analytes.find(x => x.key === this.selected)
      const num = this.validation.value
      const formatted = (a.decimals != null) ? num.toFixed(a.decimals) : String(num)
      let createdAt = new Date().toISOString()
      try {
        if (this.inputDate && String(this.inputDate).trim() !== '') {
          const dt = new Date(this.inputDate)
          if (!Number.isNaN(dt.getTime())) createdAt = dt.toISOString()
        }
      } catch (e) { /* ignore and use now */ }

      // No enviamos 'label' (texto traducido al idioma activo): el backend lo usaría tal cual como
      // 'tipo' persistido, dependiente de idioma. Enviando solo 'key' (identificador estable),
      // el backend cae a usar la key como tipo, y la traducción se resuelve siempre en el cliente.
      this.$emit('submit', { key: a.key, value: formatted, unit: a.unit, createdAt })
    },
    resetFields() {
      this.value = ''
      this.inputDate = this.localNowForInput()
    }
  }
}
</script>

<style scoped>
.add-entry-section {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: var(--shadow-md);
}

.section-subtitle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 1.5rem 0;
}

.section-subtitle svg {
  color: var(--success-color);
}

.form-grid-analisis {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .form-grid-analisis {
    grid-template-columns: 1fr 1fr 1fr auto;
    align-items: start;
  }
}

.form-actions-inline {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.form-label-spacer {
  min-height: 1.5rem;
  margin-bottom: 0.5rem;
}

.btn-add {
  width: 100%;
  white-space: nowrap;
}

@media (min-width: 768px) {
  .btn-add {
    width: auto;
    min-width: 180px;
  }
}

/* Input with unit badge */
.input-with-unit {
  position: relative;
  display: flex;
  align-items: center;
}

.input-with-unit input {
  padding-right: 4.5rem;
}

.unit-badge {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 1rem;
  background: var(--bg-light);
  border-left: 1px solid var(--border);
  border-radius: 0 8px 8px 0;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--text-secondary);
  min-width: 60px;
}

.input-error {
  border-color: var(--danger-color) !important;
}

.input-error:focus {
  box-shadow: var(--focus-ring-danger) !important;
}

.btn-primary:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}
</style>
