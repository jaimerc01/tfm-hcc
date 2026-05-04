<template>
  <section class="analisis-section" :aria-busy="saving ? 'true' : 'false'" aria-labelledby="analisis-heading">
    <h2 id="analisis-heading" class="sr-only">{{ $t('analysis') }}</h2>
    <div class="info-box">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      <span>
        <strong>{{ $t('register_blood_analysis') }}</strong> {{ $t('track_health') }}
        {{ $t('select_param_value_date') }}
      </span>
    </div>

    <!-- Chart Section -->
    <div class="chart-section">
      <div class="chart-header">
        <h3>
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="20" x2="18" y2="10"></line>
            <line x1="12" y1="20" x2="12" y2="4"></line>
            <line x1="6" y1="20" x2="6" y2="14"></line>
          </svg>
          {{ $t('historical_evolution') }}
        </h3>
        <div class="chart-controls">
          <div class="control-group">
            <label class="form-label" for="chart-type-select">{{ $t('chart_type') }}</label>
            <select id="chart-type-select" v-model="chartType" @change="drawChart" class="form-input chart-select">
              <option value="line">{{ $t('line_simple') }}</option>
              <option value="interactive">{{ $t('line_interactive') }}</option>
              <option value="bar">{{ $t('bar_chart') }}</option>
              <option value="gauge">{{ $t('gauge_chart') }}</option>
            </select>
          </div>
          <div class="control-group">
            <label class="form-label" for="chart-param-select">{{ $t('parameter') }}</label>
            <select id="chart-param-select" v-model="chartParam" @change="drawChart" class="form-input chart-select">
              <option v-for="a in analytes" :key="a.key" :value="a.key">{{ getAnalyteLabel(a) }}</option>
            </select>
          </div>
        </div>
      </div>
      <div
        ref="chart"
        class="chart-container"
        role="img"
        :aria-label="$t('analysis_chart_aria_label', { label: chartCurrentMetricLabel })"
      ></div>

      <p id="analysis-chart-summary" class="chart-summary" role="status" aria-live="polite">
        {{ chartSummaryText }}
      </p>

      <div class="chart-alt-actions">
        <button
          type="button"
          class="btn-secondary"
          :aria-expanded="showChartDataTable ? 'true' : 'false'"
          aria-controls="analysis-chart-data-table"
          @click="showChartDataTable = !showChartDataTable"
        >
          {{ showChartDataTable ? $t('analysis_hide_data_table') : $t('analysis_show_data_table') }}
        </button>
      </div>

      <div v-if="showChartDataTable" id="analysis-chart-data-table" class="results-table-container">
        <table class="results-table">
          <thead>
            <tr>
              <th>{{ $t('date_col') }}</th>
              <th>{{ $t('value_col') }}</th>
              <th>{{ $t('status') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in chartRows" :key="`${row.createdAt || 'no-date'}-${idx}`">
              <td class="date-cell">{{ formatDate(row.createdAt) }}</td>
              <td>{{ row.value }} <span class="unit-small">{{ row.unit }}</span></td>
              <td>{{ row.statusText }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Chart info helper -->
      <div class="chart-info" v-if="chartType">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
          <circle cx="12" cy="12" r="10"></circle>
          <path d="M12 16v-4"></path>
          <path d="M12 8h.01"></path>
        </svg>
        <span>{{ getChartTypeInfo() }}</span>
      </div>
    </div>

    <!-- Add Entry Form -->
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
            @click="addEntry" 
            :disabled="!canAdd || saving"
            :aria-label="saving ? $t('analysis_add_aria_saving') : $t('analysis_add_aria')"
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

    <!-- Results List -->
    <div v-if="entries.length" class="section results-section">
      <div class="section-title">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 11H3v2h6m-6-5h6m-6 8h6m4-7h8m-8-3h8m-8 6h8m-8 3h8"></path>
        </svg>
        {{ $t('registered_results') }}
        <span class="badge badge-info">{{ entries.length }}</span>
      </div>

      <div class="results-table-container">
        <table class="results-table">
          <thead>
            <tr>
              <th>{{ $t('parameter_col') }}</th>
              <th>{{ $t('value_col') }}</th>
              <th>{{ $t('date_col') }}</th>
              <th class="actions-col">{{ $t('actions_col') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(e, idx) in entries" :key="idx">
              <td>
                <div class="param-cell">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="1"></circle>
                    <circle cx="12" cy="5" r="1"></circle>
                    <circle cx="12" cy="19" r="1"></circle>
                  </svg>
                  <strong>{{ getEntryLabel(e) }}</strong>
                </div>
              </td>
              <td>
                <span class="value-cell">{{ e.value }} <span class="unit-small">{{ e.unit }}</span></span>
              </td>
              <td class="date-cell">{{ formatDate(e.createdAt) }}</td>
              <td class="actions-col">
                <button 
                  type="button"
                  @click="removeEntry(idx, $event)"
                  :aria-label="$t('analysis_delete_entry_aria', { label: getEntryLabel(e), date: formatDate(e.createdAt) })"
                  class="btn-icon btn-danger">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                  </svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="form-actions">
        <button 
          type="button"
          @click="clearAll($event)" 
          :disabled="saving"
          :aria-label="$t('analysis_clear_all_aria', { count: entries.length })"
          class="btn-danger">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          </svg>
          {{ $t('analysis_clear_all_button') }}
        </button>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else class="empty-state-small">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
        <polyline points="14 2 14 8 20 8"></polyline>
        <line x1="16" y1="13" x2="8" y2="13"></line>
        <line x1="16" y1="17" x2="8" y2="17"></line>
        <polyline points="10 9 9 9 8 9"></polyline>
      </svg>
      <p>{{ $t('no_blood_analysis_results') }}</p>
      <span>{{ $t('add_first_result_hint') }}</span>
    </div>

    <div v-if="msg" class="alert alert-success" role="status" aria-live="polite">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12"></polyline>
      </svg>
      {{ msg }}
    </div>

    <div v-if="error" class="alert alert-danger" role="alert" aria-live="assertive">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      {{ error }}
    </div>

    <div :class="['toast-notification', showToast ? 'show' : '']" role="status" aria-live="polite" aria-atomic="true">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12"></polyline>
      </svg>
      {{ showToastMessage }}
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" role="dialog" aria-modal="true" @click.self="closeDeleteModal">
      <div
        ref="deleteModal"
        class="modal"
        tabindex="-1"
        @keydown="onModalKeydown('delete', $event)"
      >
        <div class="modal-header">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--danger-color)">
            <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
          <h3>{{ $t('confirm_delete') }}</h3>
        </div>
        <p class="modal-text">{{ $t('delete_result_confirm') }}</p>
        
        <div class="modal-actions">
          <button 
            ref="deleteCancelButton"
            type="button"
            class="btn-secondary"
            @click="closeDeleteModal">
            {{ $t('cancel') }}
          </button>
          <button 
            ref="deleteConfirmButton"
            type="button"
            class="btn-danger"
            @click="confirmDelete">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
            {{ $t('yes_delete') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Clear All Confirmation Modal -->
    <div v-if="showClearAllModal" class="modal-overlay" role="dialog" aria-modal="true" @click.self="closeClearAllModal">
      <div
        ref="clearAllModal"
        class="modal"
        tabindex="-1"
        @keydown="onModalKeydown('clear', $event)"
      >
        <div class="modal-header">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--danger-color)">
            <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
          <h3>{{ $t('confirm_bulk_delete') }}</h3>
        </div>
        <p class="modal-text">
          {{ $t('bulk_delete_confirm', { count: entries.length }) }}
        </p>
        
        <div class="modal-actions">
          <button 
            ref="clearCancelButton"
            type="button"
            class="btn-secondary"
            @click="closeClearAllModal">
            {{ $t('cancel') }}
          </button>
          <button 
            ref="clearConfirmButton"
            type="button"
            class="btn-danger"
            @click="confirmClearAll"
            :disabled="saving">
            <div v-if="saving" class="spinner-small"></div>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
            {{ saving ? $t('deleting') : $t('yes_delete_all') }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import { useChart } from '@/composables/useChart'
import { getThemeColor } from '@/utils/themeColors'

const ANALYSIS_UI = Object.freeze({
  MESSAGE_CLEAR_MS: 3000,
  TOAST_MS: 2000,
  CHART_HEIGHT_INTERACTIVE: 400,
  CHART_HEIGHT_BAR: 350,
  CHART_HEIGHT_GAUGE: 250,
  GAUGE_DEFAULT_MIN: 0,
  GAUGE_DEFAULT_MAX: 100,
  GAUGE_MAX_BY_PARAM: {
    glucosa: 300,
    hemoglobina: 20,
    colesterol: 400,
    trigliceridos: 500,
    creatinina: 3,
    hematocrito: 60
  }
})

export default {
  name: 'AnalisisSangreSection',
  setup() {
    const { 
      drawTimeSeriesChart, 
      drawInteractiveTimeSeriesChart,
      drawBarChart,
      drawGaugeChart,
      prepareChartData 
    } = useChart()
    return { 
      drawTimeSeriesChart,
      drawInteractiveTimeSeriesChart,
      drawBarChart,
      drawGaugeChart,
      prepareChartData 
    }
  },
  data() {
    return {
      chartType: 'interactive', // Tipo de gráfico por defecto
      analytes: [
        { 
          key: 'glucosa', 
          labelKey: 'glucose',
          aliases: ['glucosa', 'glucose'],
          unit: 'mg/dL', 
          // Rangos de validación del formulario (amplios)
          validationMin: 0, 
          validationMax: 1000, 
          decimals: 0,
          // Rangos recomendados (se actualizarán desde servidor)
          recommendedMin: null,
          recommendedMax: null
        },
        { 
          key: 'hemoglobina', 
          labelKey: 'hemoglobin',
          aliases: ['hemoglobina', 'hemoglobin'],
          unit: 'g/dL', 
          validationMin: 0, 
          validationMax: 25, 
          decimals: 1,
          recommendedMin: null,
          recommendedMax: null
        },
        { 
          key: 'colesterol', 
          labelKey: 'total_cholesterol',
          aliases: ['colesterol', 'colesterol total', 'colesteroltotal', 'total cholesterol'],
          unit: 'mg/dL', 
          validationMin: 0, 
          validationMax: 500, // Aumentado para permitir valores altos como 250-300
          decimals: 0,
          recommendedMin: null,
          recommendedMax: null
        },
        { 
          key: 'trigliceridos', 
          labelKey: 'triglycerides',
          aliases: ['trigliceridos', 'triglicéridos', 'triglycerides'],
          unit: 'mg/dL', 
          validationMin: 0, 
          validationMax: 2000, 
          decimals: 0,
          recommendedMin: null,
          recommendedMax: null
        },
        { 
          key: 'creatinina', 
          labelKey: 'creatinine',
          aliases: ['creatinina', 'creatinine'],
          unit: 'mg/dL', 
          validationMin: 0, 
          validationMax: 50, 
          decimals: 2,
          recommendedMin: null,
          recommendedMax: null
        },
        { 
          key: 'hematocrito', 
          labelKey: 'hematocrit',
          aliases: ['hematocrito', 'hematocrit'],
          unit: '%', 
          validationMin: 0, 
          validationMax: 100, 
          decimals: 1,
          recommendedMin: null,
          recommendedMax: null
        }
      ],
  selected: 'glucosa',
  value: '',
  // datetime-local input value (local time, e.g. '2025-08-28T15:30')
  inputDate: '',
      entries: [],
  chartParam: 'glucosa',
      saving: false,
      msg: '',
      error: null,
      showToast: false,
      showToastMessage: '',
      showDeleteModal: false,
      deleteIndex: null,
      showClearAllModal: false,
      showChartDataTable: false,
      deleteTriggerEl: null,
      clearAllTriggerEl: null
    }
  },
  async created() { 
    this.showToastMessage = this.$t('analysis_toast_saved')
    await this.loadRangos()
    await this.load()
    this.inputDate = this.localNowForInput() 
  },
  mounted() {
    // initial draw after component mounted
    this.drawChart()
  },
  unmounted() {
    // Limpiar el contenedor del gráfico al desmontar el componente
    const container = this.$refs.chart
    if (container) {
      container.innerHTML = ''
    }
  },
  computed: {
    selectedChartAnalyte() {
      return this.analytes.find(x => x.key === this.chartParam) || null
    },
    chartCurrentMetricLabel() {
      return this.selectedChartAnalyte ? this.getAnalyteLabel(this.selectedChartAnalyte) : this.$t('parameter')
    },
    chartEntries() {
      if (!Array.isArray(this.entries)) return []
      const param = this.chartParam || 'glucosa'
      return this.entries
        .filter(e => e && (e.key === param || this.mapTipoToKey(e.label || e.tipo || e.key) === param))
        .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
    },
    latestChartEntry() {
      if (!this.chartEntries.length) return null
      return this.chartEntries[this.chartEntries.length - 1]
    },
    latestChartEntryNumericValue() {
      if (!this.latestChartEntry) return null
      const raw = String(this.latestChartEntry.value || this.latestChartEntry.valor || '').replace(',', '.')
      const parsed = Number(raw)
      return Number.isFinite(parsed) ? parsed : null
    },
    latestOutOfRecommendedRange() {
      if (!this.selectedChartAnalyte) return false
      if (this.latestChartEntryNumericValue === null) return false
      const min = this.selectedChartAnalyte.recommendedMin
      const max = this.selectedChartAnalyte.recommendedMax
      if (min === null || max === null) return false
      return this.latestChartEntryNumericValue < min || this.latestChartEntryNumericValue > max
    },
    chartSummaryText() {
      const label = this.chartCurrentMetricLabel
      const type = this.$t(`analysis_chart_type_${this.chartType || 'interactive'}`)
      if (!this.chartEntries.length) {
        return this.$t('analysis_chart_summary_empty', { label, type })
      }

      const latest = this.latestChartEntry
      const latestValue = this.latestChartEntryNumericValue
      const unit = (this.selectedChartAnalyte && this.selectedChartAnalyte.unit) || latest.unit || ''
      const date = this.formatDate(latest.createdAt)
      const status = this.latestOutOfRecommendedRange
        ? this.$t('analysis_chart_status_out_of_range')
        : this.$t('analysis_chart_status_in_range')

      return this.$t('analysis_chart_summary_with_data', {
        label,
        type,
        count: this.chartEntries.length,
        latestValue: latestValue ?? latest.value,
        unit,
        latestDate: date,
        status
      })
    },
    chartRows() {
      const analyte = this.selectedChartAnalyte
      const min = analyte ? analyte.recommendedMin : null
      const max = analyte ? analyte.recommendedMax : null
      return this.chartEntries
        .slice()
        .reverse()
        .map(entry => {
          const raw = String(entry.value || entry.valor || '').replace(',', '.')
          const num = Number(raw)
          let statusText = this.$t('analysis_chart_status_no_reference')
          if (Number.isFinite(num) && min !== null && max !== null) {
            statusText = num < min || num > max
              ? this.$t('analysis_chart_status_out_of_range')
              : this.$t('analysis_chart_status_in_range')
          }
          return {
            ...entry,
            statusText
          }
        })
    },
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
      return { isValid: true, message: '' , value: num }
    }
  },
  methods: {
    focusFirstInvalidField() {
      if (!this.validation.isValid && this.$refs.valueInput) {
        this.$refs.valueInput.focus()
      }
    },
    focusModalInitial(type) {
      this.$nextTick(() => {
        if (type === 'delete' && this.$refs.deleteCancelButton) {
          this.$refs.deleteCancelButton.focus()
          return
        }
        if (type === 'clear' && this.$refs.clearCancelButton) {
          this.$refs.clearCancelButton.focus()
          return
        }
        const fallback = type === 'delete' ? this.$refs.deleteModal : this.$refs.clearAllModal
        if (fallback) fallback.focus()
      })
    },
    restoreTriggerFocus(type) {
      const trigger = type === 'delete' ? this.deleteTriggerEl : this.clearAllTriggerEl
      if (trigger && typeof trigger.focus === 'function') {
        this.$nextTick(() => trigger.focus())
      }
      if (type === 'delete') this.deleteTriggerEl = null
      if (type === 'clear') this.clearAllTriggerEl = null
    },
    onModalKeydown(type, event) {
      if (event.key === 'Escape') {
        event.preventDefault()
        if (type === 'delete') this.closeDeleteModal()
        if (type === 'clear') this.closeClearAllModal()
        return
      }
      if (event.key !== 'Tab') return

      const focusableRefs = type === 'delete'
        ? [this.$refs.deleteCancelButton, this.$refs.deleteConfirmButton]
        : [this.$refs.clearCancelButton, this.$refs.clearConfirmButton]
      const focusable = focusableRefs.filter(Boolean)
      if (!focusable.length) return

      const first = focusable[0]
      const last = focusable[focusable.length - 1]
      const active = document.activeElement

      if (event.shiftKey && active === first) {
        event.preventDefault()
        last.focus()
      } else if (!event.shiftKey && active === last) {
        event.preventDefault()
        first.focus()
      }
    },
    getAnalyteLabel(analyte) {
      if (!analyte) return ''
      return this.$t(analyte.labelKey || analyte.key)
    },
    getEntryLabel(entry) {
      if (!entry) return ''
      if (entry.key) {
        const analyte = this.analytes.find(a => a.key === entry.key)
        if (analyte) return this.getAnalyteLabel(analyte)
      }
      return entry.label || entry.tipo || entry.key || ''
    },
    /**
     * Carga los rangos de referencia desde el servidor y actualiza los analytes
     */
    async loadRangos() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getRangos()
        const rangos = res.data || []
        
        // Actualizar rangos en cada analyte
        rangos.forEach(rango => {
          // Normalizar nombre del rango para hacer match con los analytes
          const nombreNormalizado = this.normalizarNombre(rango.nombre)
          
          // Buscar el analyte correspondiente
          const analyte = this.analytes.find(a => {
            const keyNormalizado = this.normalizarNombre(a.key)
            const aliasMatch = (a.aliases || []).some(alias => this.normalizarNombre(alias) === nombreNormalizado)
            return keyNormalizado === nombreNormalizado || aliasMatch
          })
          
          if (analyte && rango.valorInferiorNumerico !== null && rango.valorSuperiorNumerico !== null) {
            analyte.recommendedMin = rango.valorInferiorNumerico
            analyte.recommendedMax = rango.valorSuperiorNumerico
          }
        })
      } catch (e) {
        console.error(this.$t('analysis_error_loading_ranges'), e)
        // No bloqueamos la carga del componente si fallan los rangos
      }
    },
    
    /**
     * Normaliza un nombre para hacer comparaciones
     */
    normalizarNombre(nombre) {
      if (!nombre) return ''
      return String(nombre)
        .normalize('NFD')
        .replace(/\p{Diacritic}/gu, '')
        .toLowerCase()
        .replace(/\s+/g, '')
    },
    
    // Map server 'tipo' (e.g. "Glucosa", "Hemoglobina") to a local analyte key.
    // Normalizes accents/case/whitespace and matches against known analytes.
    mapTipoToKey(tipo) {
      if (!tipo) return null
      const norm = this.normalizarNombre(tipo)
      const analyte = this.analytes.find(a => {
        const keyMatch = this.normalizarNombre(a.key) === norm
        const aliasMatch = (a.aliases || []).some(alias => this.normalizarNombre(alias) === norm)
        return keyMatch || aliasMatch
      })
      return analyte ? analyte.key : norm
    },
    async load() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getMine()
        const dto = res.data || {}
        
        // backend may return a JSON array for analisisSangre; try to parse
        if (dto.analisisSangre) {
          try {
            const parsed = typeof dto.analisisSangre === 'string' ? JSON.parse(dto.analisisSangre) : dto.analisisSangre
            if (Array.isArray(parsed)) {
              // Map server DTO shape { tipo, valor, unidad, createdAt, id, rango } to component shape
              this.entries = parsed.map(p => {
                const mappedKey = p.key || this.mapTipoToKey(p.tipo)
                const analyte = this.analytes.find(x => x.key === mappedKey)
                
                // Actualizar los rangos de los analytes con datos del servidor
                if (p.rango && analyte) {
                  this.updateAnalyteRanges(analyte, p.rango)
                }
                
                return {
                  id: p.id || null,
                  key: mappedKey || p.key || null,
                  label: p.label || p.tipo || p.key || '',
                  // backend may send 'valor' (string/number) so normalize to string for display
                  value: (p.value !== undefined && p.value !== null) ? String(p.value) : (p.valor !== undefined && p.valor !== null) ? String(p.valor) : '',
                  unit: analyte ? analyte.unit : (p.unit || p.unidad || ''),
                  createdAt: p.createdAt || p.fechaCreacion || null,
                  rango: p.rango || null // Guardar información del rango
                }
              })
            } else {
              // Si no es un array, inicializar como vacío
              this.entries = []
            }
          } catch (ignore) {
            // ignore malformed content and start empty
            this.entries = []
          }
        } else {
          // Si no hay analisisSangre, inicializar vacío
          this.entries = []
        }
        
        // Asegurar que el gráfico se redibuja después de actualizar entries
        await this.$nextTick()
        this.drawChart()
      } catch (e) { 
        console.error(this.$t('analysis_error_loading'), e)
        this.error = this.$t('analysis_error_loading')
        this.entries = []
        await this.$nextTick()
        this.drawChart()
      }
    },

    /**
     * Actualiza los rangos de un analyte con datos del servidor
     */
    updateAnalyteRanges(analyte, rangoData) {
      if (!rangoData) return
      
      // Actualizar rangos recomendados con valores del servidor
      if (rangoData.valorInferiorNumerico !== undefined && rangoData.valorInferiorNumerico !== null) {
        analyte.recommendedMin = rangoData.valorInferiorNumerico
      }
      if (rangoData.valorSuperiorNumerico !== undefined && rangoData.valorSuperiorNumerico !== null) {
        analyte.recommendedMax = rangoData.valorSuperiorNumerico
      }
      
      // Guardar referencia al rango para uso posterior
      analyte.rangoData = rangoData
    },

    async addEntry() {
      if (!this.canAdd) {
        this.focusFirstInvalidField()
        return
      }
      
      this.saving = true
      try {
        const a = this.analytes.find(x => x.key === this.selected)
        // format numeric value according to decimals
        const num = this.validation.value
        const formatted = (a.decimals != null) ? num.toFixed(a.decimals) : String(num)
        // use provided inputDate if valid, otherwise now
        let createdAt = new Date().toISOString()
        try {
          if (this.inputDate && String(this.inputDate).trim() !== '') {
            const dt = new Date(this.inputDate)
            if (!Number.isNaN(dt.getTime())) createdAt = dt.toISOString()
          }
        } catch (e) { /* ignore and use now */ }

        const newEntry = { key: a.key, label: this.getAnalyteLabel(a), value: formatted, unit: a.unit, createdAt }
        
        // Añadir solo la nueva entrada al servidor (POST añade sin eliminar existentes)
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const payload = JSON.stringify([newEntry])
        await svc.añadirAnalisisSangre(payload)
        
        // Reload from server to get the updated data
        await this.load()
        
        // Clear form
        this.value = ''
        this.inputDate = this.localNowForInput()
        
        // Show success message
        this.msg = this.$t('analysis_result_added')
        this.showTemporaryToast(this.$t('analysis_toast_saved'))
        setTimeout(() => this.msg = '', ANALYSIS_UI.MESSAGE_CLEAR_MS)
        
      } catch (e) {
        console.error(this.$t('analysis_error_saving'), e)
        this.error = this.$t('analysis_error_saving')
        // Reload to restore state
        await this.load()
      } finally {
        this.saving = false
      }
    },

    removeEntry(i, event) {
      this.deleteTriggerEl = event && event.currentTarget ? event.currentTarget : null
      this.deleteIndex = i
      this.showDeleteModal = true
      this.focusModalInitial('delete')
    },

    closeDeleteModal() {
      this.showDeleteModal = false
      this.deleteIndex = null
      this.restoreTriggerFocus('delete')
    },

    async confirmDelete() {
      if (this.deleteIndex === null) return
      
      try {
        const i = this.deleteIndex
        const entry = this.entries[i]
        
        // Si el entry tiene ID, usar el endpoint DELETE específico
        if (entry && entry.id) {
          const svc = await import('@/services/historiaClinicaService').then(m => m.default)
          await svc.deleteDatoClinico(entry.id)
        }
        
        // Reload from server to ensure consistency
        await this.load()
        
        // Close modal
        this.closeDeleteModal()
        
        // Show success message
        this.msg = this.$t('analysis_result_deleted')
        this.showTemporaryToast(this.$t('analysis_toast_deleted'))
        setTimeout(() => this.msg = '', ANALYSIS_UI.MESSAGE_CLEAR_MS)
      } catch (err) {
        console.error(this.$t('analysis_error_deleting'), err)
        this.error = this.$t('analysis_error_deleting')
        // Reload to restore state
        await this.load()
        this.closeDeleteModal()
      }
    },

    clearAll(event) {
      // Verificar que hay entradas para eliminar
      if (!this.entries || this.entries.length === 0) {
        this.msg = this.$t('analysis_no_results_to_delete')
        setTimeout(() => this.msg = '', ANALYSIS_UI.MESSAGE_CLEAR_MS)
        return
      }
      
      // Mostrar modal de confirmación
      this.clearAllTriggerEl = event && event.currentTarget ? event.currentTarget : null
      this.showClearAllModal = true
      this.focusModalInitial('clear')
    },
    
    closeClearAllModal() {
      this.showClearAllModal = false
      this.restoreTriggerFocus('clear')
    },
    
    async confirmClearAll() {
      
      this.saving = true
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        
        // Eliminar cada entrada individualmente usando su ID
        const entriesToDelete = this.entries.filter(e => e && e.id)
        
        if (entriesToDelete.length === 0) {
          this.entries = []
          this.drawChart() // Redibujar gráfico vacío
          return
        }
        
        const deletePromises = entriesToDelete.map(e => svc.deleteDatoClinico(e.id))
        await Promise.all(deletePromises)
        
        // Limpiar array local y redibujar gráfico
        this.entries = []
        this.drawChart() // Redibujar gráfico vacío
        
        // Recargar desde servidor para asegurar consistencia
        await this.load()
        
        this.showClearAllModal = false
        this.restoreTriggerFocus('clear')
        this.msg = this.$t('analysis_results_deleted')
        this.showTemporaryToast(this.$t('analysis_toast_deleted'))
        setTimeout(() => this.msg = '', ANALYSIS_UI.MESSAGE_CLEAR_MS)
      } catch (e) { 
        console.error(this.$t('analysis_error_clear_all'), e)
        this.error = `${this.$t('analysis_error_clear_all')}: ${e.message || this.$t('error')}`
        setTimeout(() => this.error = '', 5000)
        // Recargar para restaurar estado
        await this.load()
      } finally {
        this.saving = false
      }
    },

    showTemporaryToast(message) {
      this.showToastMessage = message || this.$t('analysis_toast_saved')
      this.showToast = true
      setTimeout(() => this.showToast = false, ANALYSIS_UI.TOAST_MS)
    }
    ,
    renderChartEmpty(container, message) {
      container.innerHTML = ''
      const empty = document.createElement('div')
      empty.className = 'chart-empty'
      empty.textContent = message
      container.appendChild(empty)
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
    // D3 chart rendering: time series for glucosa (historical values)
    drawChart() {
      const container = this.$refs.chart
      if (!container || !this.entries) return

      // Preparar datos para el parámetro seleccionado
      const param = this.chartParam || 'glucosa'
      const analyteDef = this.analytes.find(a => a.key === param) || {}
      const label = analyteDef.key ? this.getAnalyteLabel(analyteDef) : param

      // Opciones comunes para todos los gráficos
      const baseOptions = {
        label,
        color: getThemeColor('--chart-emphasis'),
        recommendedMin: analyteDef.recommendedMin,
        recommendedMax: analyteDef.recommendedMax,
        ariaLabel: this.$t('analysis_chart_aria_label', { label })
      }

      // Para el gauge, usar el último valor
      if (this.chartType === 'gauge') {
        const latestEntry = this.entries
          .filter(e => e.key === param || (e.label && e.label.toLowerCase().includes(param.toLowerCase())))
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0]
        
        if (!latestEntry) {
          this.renderChartEmpty(container, this.$t('analysis_chart_no_metric_data'))
          return
        }

        const value = parseFloat(String(latestEntry.value || latestEntry.valor || '0').replace(',', '.'))
        
        // Determinar rango del gauge basado en el analito
        const gaugeMin = ANALYSIS_UI.GAUGE_DEFAULT_MIN
        const gaugeMax = ANALYSIS_UI.GAUGE_MAX_BY_PARAM[param] || ANALYSIS_UI.GAUGE_DEFAULT_MAX

        this.drawGaugeChart(container, value, {
          ...baseOptions,
          min: gaugeMin,
          max: gaugeMax,
          unit: analyteDef.unit || '',
          height: ANALYSIS_UI.CHART_HEIGHT_GAUGE
        })
        return
      }

      // Para otros tipos de gráfico, preparar datos temporales
      const chartData = this.prepareChartData(this.entries, param)

      if (chartData.length === 0) {
        this.renderChartEmpty(container, this.$t('analysis_chart_no_historical_data', { label }))
        return
      }

      // Dibujar según el tipo seleccionado
      switch(this.chartType) {
        case 'line':
          this.drawTimeSeriesChart(container, chartData, baseOptions)
          break
        
        case 'interactive':
          this.drawInteractiveTimeSeriesChart(container, chartData, {
            ...baseOptions,
            height: ANALYSIS_UI.CHART_HEIGHT_INTERACTIVE
          })
          break
        
        case 'bar':
          this.drawBarChart(container, chartData, {
            ...baseOptions,
            height: ANALYSIS_UI.CHART_HEIGHT_BAR
          })
          break
        
        default:
          this.drawTimeSeriesChart(container, chartData, baseOptions)
      }
    },
    formatDate(iso) {
      if (!iso) return ''
      try {
        const d = new Date(iso)
        return d.toLocaleString()
      } catch (e) { return iso }
    },

    getChartTypeInfo() {
      const info = {
        line: this.$t('analysis_chart_info_line'),
        interactive: this.$t('analysis_chart_info_interactive'),
        bar: this.$t('analysis_chart_info_bar'),
        gauge: this.$t('analysis_chart_info_gauge')
      }
      return info[this.chartType] || ''
    }
  }
}
</script>

<style scoped>
@import '@/styles/charts.css';

.analisis-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.chart-summary {
  margin: 0.75rem 0 0;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.chart-alt-actions {
  margin-top: 0.75rem;
}

/* Controles de gráfico mejorados */
.control-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: nowrap;
}

.control-group .form-label {
  white-space: nowrap;
  margin: 0;
}

/* Información del tipo de gráfico */
.chart-info {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  background: var(--primary-100);
  border: 1px solid var(--primary-light);
  border-radius: 8px;
  font-size: 0.875rem;
  color: var(--primary-active);
  line-height: 1.5;
}

.chart-info svg {
  flex-shrink: 0;
  margin-top: 0.125rem;
  color: var(--primary-color);
}

@media (max-width: 768px) {
  .chart-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .control-group {
    width: 100%;
  }
  
  .control-group .form-label {
    min-width: 120px;
  }
  
  .control-group .chart-select {
    flex: 1;
  }
}

/* Add Entry Section */
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

/* Results Section */
.results-section {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: var(--shadow-md);
}

.results-table-container {
  overflow-x: auto;
  margin-bottom: 1rem;
}

.results-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.results-table thead {
  background: var(--bg-light);
  border-bottom: 2px solid var(--border);
}

.results-table th {
  padding: 0.75rem 1rem;
  text-align: left;
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.8125rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.results-table tbody tr {
  border-bottom: 1px solid var(--border);
  transition: background-color 0.15s ease;
}

.results-table tbody tr:hover {
  background: var(--bg-light);
}

.results-table td {
  padding: 1rem;
  color: var(--text-primary);
}

.param-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.param-cell svg {
  color: var(--primary-color);
  flex-shrink: 0;
}

.value-cell {
  font-weight: 600;
  color: var(--text-primary);
}

.unit-small {
  font-weight: 400;
  color: var(--text-secondary);
  font-size: 0.8125rem;
}

.date-cell {
  white-space: nowrap;
  color: var(--text-secondary);
  font-size: 0.8125rem;
}

.actions-col {
  text-align: right;
  width: 80px;
}

/* Empty state */
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

/* Toast notification */
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

.modal button:focus-visible,
.chart-alt-actions button:focus-visible,
.btn-icon:focus-visible,
.btn-danger:focus-visible,
.btn-secondary:focus-visible,
.btn-primary:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .toast-notification,
  .results-table tbody tr {
    transition: none;
    animation: none;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .chart-controls {
    width: 100%;
  }
  
  .chart-select {
    flex: 1;
    min-width: 0;
  }
  
  .results-table {
    font-size: 0.8125rem;
  }
  
  .results-table th,
  .results-table td {
    padding: 0.5rem;
  }
  
  .toast-notification {
    right: 1rem;
    bottom: 1rem;
    left: 1rem;
    width: auto;
  }
}
</style>
