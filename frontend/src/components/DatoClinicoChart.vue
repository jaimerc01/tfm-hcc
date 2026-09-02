<template>
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
        <div class="control-group" v-if="!selectedCombo">
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
            <option v-for="c in combos" :key="c.id" :value="c.id">{{ $t(c.labelKey) }}</option>
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

    <div class="chart-alt-actions" v-if="!selectedCombo">
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

    <div v-if="!selectedCombo && showChartDataTable" id="analysis-chart-data-table" class="results-table-container">
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
      <svg class="field-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      <span>{{ getChartTypeInfo() }}</span>
    </div>
  </div>
</template>

<script>
import { useChart } from '@/composables/useChart'
import { getThemeColor } from '@/utils/themeColors'
import { getAnalyteLabel, formatDate, mapTipoToKey } from '@/utils/datosClinicos'

const CHART_UI = Object.freeze({
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
    hematocrito: 60,
    'Frecuencia Cardiaca': 220,
    'Presion Arterial Sistolica': 200,
    'Presion Arterial Diastolica': 150,
    IMC: 50,
    'PH Orina': 14
  }
})

export default {
  name: 'DatoClinicoChart',
  props: {
    entries: { type: Array, required: true },
    analytes: { type: Array, required: true },
    // Vistas combinadas opcionales que cruzan varios analitos a la vez
    // (p. ej. circular LDL/HDL o dos líneas para presión arterial).
    // Cada combo: { id, labelKey, type: 'pie' | 'dual-line', analyteKeys: [key1, key2] }
    combos: { type: Array, default: () => [] }
  },
  setup() {
    const {
      drawTimeSeriesChart,
      drawInteractiveTimeSeriesChart,
      drawBarChart,
      drawGaugeChart,
      drawComparisonPieChart,
      drawDualLineChart,
      prepareChartData
    } = useChart()
    return {
      drawTimeSeriesChart,
      drawInteractiveTimeSeriesChart,
      drawBarChart,
      drawGaugeChart,
      drawComparisonPieChart,
      drawDualLineChart,
      prepareChartData
    }
  },
  data() {
    return {
      chartType: 'interactive', // Tipo de gráfico por defecto
      chartParam: this.analytes.length ? this.analytes[0].key : '',
      showChartDataTable: false
    }
  },
  computed: {
    defaultParamKey() {
      return this.analytes.length ? this.analytes[0].key : ''
    },
    selectedCombo() {
      return this.combos.find(c => c.id === this.chartParam) || null
    },
    selectedChartAnalyte() {
      return this.analytes.find(x => x.key === this.chartParam) || null
    },
    chartCurrentMetricLabel() {
      if (this.selectedCombo) return this.$t(this.selectedCombo.labelKey)
      return this.selectedChartAnalyte ? this.getAnalyteLabel(this.selectedChartAnalyte) : this.$t('parameter')
    },
    chartEntries() {
      if (!Array.isArray(this.entries)) return []
      const param = this.chartParam || this.defaultParamKey
      return this.entries
        .filter(e => e && (e.key === param || mapTipoToKey(e.label || e.tipo || e.key, this.analytes) === param))
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
      if (this.selectedCombo) {
        const [keyA, keyB] = this.selectedCombo.analyteKeys
        const labelA = this.analyteLabelForKey(keyA)
        const labelB = this.analyteLabelForKey(keyB)
        return this.$t('analysis_chart_combo_summary', { labelA, labelB })
      }

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
    }
  },
  watch: {
    entries: {
      handler() {
        this.drawChart()
      },
      deep: true
    }
  },
  mounted() {
    this.drawChart()
  },
  beforeUnmount() {
    // Se limpia en beforeUnmount (no en unmounted): Vue ya ha puesto a null las
    // refs de plantilla cuando se ejecuta unmounted, así que allí no habría contenedor.
    const container = this.$refs.chart
    if (container) {
      container.innerHTML = ''
    }
  },
  methods: {
    getAnalyteLabel(analyte) {
      return getAnalyteLabel(analyte, this.$t)
    },
    formatDate(iso) {
      return formatDate(iso)
    },
    renderChartEmpty(container, message) {
      container.innerHTML = ''
      const empty = document.createElement('div')
      empty.className = 'chart-empty'
      empty.textContent = message
      container.appendChild(empty)
    },
    analyteLabelForKey(key) {
      const analyte = this.analytes.find(a => a.key === key)
      return analyte ? this.getAnalyteLabel(analyte) : key
    },
    // Entradas que pertenecen a un analito concreto, ordenadas cronológicamente
    entriesForKey(key) {
      if (!Array.isArray(this.entries)) return []
      return this.entries
        .filter(e => e && (e.key === key || mapTipoToKey(e.label || e.tipo || e.key, this.analytes) === key))
        .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
    },
    // D3 chart rendering: gráficos combinados que cruzan varios analitos (circular, dos líneas)
    drawComboChart(container, combo) {
      const [keyA, keyB] = combo.analyteKeys
      const labelA = this.analyteLabelForKey(keyA)
      const labelB = this.analyteLabelForKey(keyB)

      if (combo.type === 'pie') {
        const latestA = this.entriesForKey(keyA).slice(-1)[0]
        const latestB = this.entriesForKey(keyB).slice(-1)[0]
        const analyteA = this.analytes.find(a => a.key === keyA) || {}
        const analyteB = this.analytes.find(a => a.key === keyB) || {}

        const toDatum = (entry, label, analyte) => {
          if (!entry) return null
          const raw = String(entry.value || entry.valor || '').replace(',', '.')
          const num = Number(raw)
          return Number.isFinite(num) ? { label, value: num, unit: analyte.unit || entry.unit || '' } : null
        }

        const data = [toDatum(latestA, labelA, analyteA), toDatum(latestB, labelB, analyteB)].filter(Boolean)

        if (data.length === 0) {
          this.renderChartEmpty(container, this.$t('analysis_chart_no_metric_data'))
          return
        }

        this.drawComparisonPieChart(container, data, {
          ariaLabel: this.$t('analysis_chart_aria_label', { label: this.chartCurrentMetricLabel })
        })
        return
      }

      if (combo.type === 'dual-line') {
        const seriesA = this.prepareChartData(this.entries, keyA)
        const seriesB = this.prepareChartData(this.entries, keyB)

        if (seriesA.length === 0 && seriesB.length === 0) {
          this.renderChartEmpty(container, this.$t('analysis_chart_no_historical_data', { label: this.chartCurrentMetricLabel }))
          return
        }

        const analyteA = this.analytes.find(a => a.key === keyA) || {}

        this.drawDualLineChart(container, seriesA, seriesB, {
          labelA,
          labelB,
          unit: analyteA.unit || '',
          colorA: getThemeColor('--chart-default'),
          colorB: getThemeColor('--chart-emphasis'),
          height: CHART_UI.CHART_HEIGHT_INTERACTIVE,
          ariaLabel: this.$t('analysis_chart_aria_label', { label: this.chartCurrentMetricLabel })
        })
      }
    },
    // D3 chart rendering: time series for the selected clinical parameter (historical values)
    drawChart() {
      const container = this.$refs.chart
      if (!container || !this.entries) return

      if (this.selectedCombo) {
        this.drawComboChart(container, this.selectedCombo)
        return
      }

      // Preparar datos para el parámetro seleccionado
      const param = this.chartParam || this.defaultParamKey
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
        const gaugeMin = CHART_UI.GAUGE_DEFAULT_MIN
        const gaugeMax = CHART_UI.GAUGE_MAX_BY_PARAM[param] || CHART_UI.GAUGE_DEFAULT_MAX

        this.drawGaugeChart(container, value, {
          ...baseOptions,
          min: gaugeMin,
          max: gaugeMax,
          unit: analyteDef.unit || '',
          height: CHART_UI.CHART_HEIGHT_GAUGE
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
      switch (this.chartType) {
        case 'line':
          this.drawTimeSeriesChart(container, chartData, baseOptions)
          break

        case 'interactive':
          this.drawInteractiveTimeSeriesChart(container, chartData, {
            ...baseOptions,
            height: CHART_UI.CHART_HEIGHT_INTERACTIVE
          })
          break

        case 'bar':
          this.drawBarChart(container, chartData, {
            ...baseOptions,
            height: CHART_UI.CHART_HEIGHT_BAR
          })
          break

        default:
          this.drawTimeSeriesChart(container, chartData, baseOptions)
      }
    },
    getChartTypeInfo() {
      if (this.selectedCombo) {
        return this.selectedCombo.type === 'pie'
          ? this.$t('analysis_chart_info_pie_comparison')
          : this.$t('analysis_chart_info_dual_line')
      }
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

.chart-summary {
  margin: 0.75rem 0 0;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.chart-alt-actions {
  margin-top: 0.75rem;
}

.chart-alt-actions button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
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

.results-table-container {
  overflow-x: auto;
  margin-bottom: 1rem;
}

.results-table tbody tr {
  transition: background-color 0.15s ease;
}

.results-table tbody tr:hover {
  background: var(--bg-light);
}

@media (prefers-reduced-motion: reduce) {
  .results-table tbody tr {
    transition: none;
    animation: none;
  }
}

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

  .control-group {
    width: 100%;
  }

  .control-group .form-label {
    min-width: 120px;
  }

  .control-group .chart-select {
    flex: 1;
  }

  .results-table {
    font-size: 0.8125rem;
  }

  .results-table th,
  .results-table td {
    padding: 0.5rem;
  }
}
</style>
