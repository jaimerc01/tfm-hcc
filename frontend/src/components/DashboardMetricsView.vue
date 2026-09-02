<template>
  <div class="metrics-dashboard">
    <h2 class="dashboard-title">{{ $t('health_metrics_panel') }}</h2>
    
    <!-- Grid de Gauges para valores actuales -->
    <div class="gauges-section">
      <h3>{{ $t('current_status') }}</h3>
      <div class="gauges-grid">
        <div class="gauge-container" ref="gaugeGlucosa"></div>
        <div class="gauge-container" ref="gaugeHemoglobina"></div>
        <div class="gauge-container" ref="gaugeColesterol"></div>
        <div class="gauge-container" ref="gaugeTrigliceridos"></div>
        <div class="gauge-container" ref="gaugeCreatinina"></div>
        <div class="gauge-container" ref="gaugeHematocrito"></div>
      </div>
    </div>

    <!-- Selector de tipo de gráfico -->
    <div class="chart-section">
      <div class="chart-header">
        <h3>{{ $t('historical_evolution') }}</h3>
        <div class="chart-controls">
          <div class="control-group">
            <label class="form-label">{{ $t('chart_type') }}</label>
            <select v-model="chartType" @change="updateChart" class="form-input chart-select">
              <option value="line">{{ $t('line_simple') }}</option>
              <option value="interactive">{{ $t('line_interactive') }}</option>
              <option value="bar">{{ $t('bar_chart') }}</option>
            </select>
          </div>
          <div class="control-group">
            <label class="form-label">{{ $t('parameter') }}</label>
            <select v-model="selectedMetric" @change="updateChart" class="form-input chart-select">
              <option value="glucosa">{{ $t('glucose') }}</option>
              <option value="hemoglobina">{{ $t('hemoglobin') }}</option>
              <option value="colesterol">{{ $t('total_cholesterol') }}</option>
              <option value="trigliceridos">{{ $t('triglycerides') }}</option>
              <option value="creatinina">{{ $t('creatinine') }}</option>
              <option value="hematocrito">{{ $t('hematocrit') }}</option>
            </select>
          </div>
        </div>
      </div>
      <div class="chart-container" ref="mainChart"></div>
    </div>

    <!-- Información sobre rangos -->
    <div class="info-box" v-if="selectedMetricData">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      <div>
        <strong>{{ $t('recommended_range', { metric: selectedMetricLabel }) }}</strong>
        <span v-if="selectedMetricData.recommendedMin && selectedMetricData.recommendedMax">
          {{ selectedMetricData.recommendedMin }} - {{ selectedMetricData.recommendedMax }} {{ selectedMetricData.unit }}
        </span>
        <span v-else>{{ $t('not_defined') }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { useChart } from '@/composables/useChart'
import { getThemeColor } from '@/utils/themeColors'

const DASHBOARD_METRICS_UI = Object.freeze({
  INTERACTIVE_CHART_HEIGHT: 400,
  CHART_EMPTY_CLASS: 'chart-empty'
})

export default {
  name: 'DashboardMetricsView',
  
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
    const metricColors = {
      glucosa: getThemeColor('--chart-series-green'),
      hemoglobina: getThemeColor('--chart-series-magenta'),
      colesterol: getThemeColor('--chart-series-cyan'),
      trigliceridos: getThemeColor('--chart-series-slate'),
      creatinina: getThemeColor('--chart-series-blue'),
      hematocrito: getThemeColor('--chart-series-purple')
    }

    return {
      chartType: 'interactive',
      selectedMetric: 'glucosa',
      entries: [],
      metrics: {
        glucosa: {
          key: 'glucosa',
          labelKey: 'glucose',
          aliases: ['glucosa', 'glucose'],
          unit: 'mg/dL',
          color: metricColors.glucosa,
          recommendedMin: null,
          recommendedMax: null,
          gaugeMin: 0,
          gaugeMax: 300
        },
        hemoglobina: {
          key: 'hemoglobina',
          labelKey: 'hemoglobin',
          aliases: ['hemoglobina', 'hemoglobin'],
          unit: 'g/dL',
          color: metricColors.hemoglobina,
          recommendedMin: null,
          recommendedMax: null,
          gaugeMin: 0,
          gaugeMax: 20
        },
        colesterol: {
          key: 'colesterol',
          labelKey: 'total_cholesterol',
          aliases: ['colesterol', 'colesterol total', 'colesteroltotal', 'total cholesterol'],
          unit: 'mg/dL',
          color: metricColors.colesterol,
          recommendedMin: null,
          recommendedMax: null,
          gaugeMin: 0,
          gaugeMax: 400
        },
        trigliceridos: {
          key: 'trigliceridos',
          labelKey: 'triglycerides',
          aliases: ['trigliceridos', 'triglicéridos', 'triglycerides'],
          unit: 'mg/dL',
          color: metricColors.trigliceridos,
          recommendedMin: null,
          recommendedMax: null,
          gaugeMin: 0,
          gaugeMax: 500
        },
        creatinina: {
          key: 'creatinina',
          labelKey: 'creatinine',
          aliases: ['creatinina', 'creatinine'],
          unit: 'mg/dL',
          color: metricColors.creatinina,
          recommendedMin: null,
          recommendedMax: null,
          gaugeMin: 0,
          gaugeMax: 3
        },
        hematocrito: {
          key: 'hematocrito',
          labelKey: 'hematocrit',
          aliases: ['hematocrito', 'hematocrit'],
          unit: '%',
          color: metricColors.hematocrito,
          recommendedMin: null,
          recommendedMax: null,
          gaugeMin: 0,
          gaugeMax: 60
        }
      }
    }
  },

  computed: {
    selectedMetricData() {
      return this.metrics[this.selectedMetric]
    },
    selectedMetricLabel() {
      return this.getMetricLabel(this.selectedMetricData)
    }
  },

  async mounted() {
    await this.loadRangos()
    await this.loadData()
    this.drawAllGauges()
    this.updateChart()
  },

  methods: {
    getMetricLabel(metric) {
      if (!metric) return ''
      return this.$t(metric.labelKey || metric.key)
    },
    renderChartEmpty(container, message) {
      container.innerHTML = ''
      const empty = document.createElement('div')
      empty.className = DASHBOARD_METRICS_UI.CHART_EMPTY_CLASS
      empty.textContent = message
      container.appendChild(empty)
    },
    async loadRangos() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getRangos()
        const rangos = res.data || []
        
        // Actualizar rangos en cada métrica
        rangos.forEach(rango => {
          const nombreNormalizado = this.normalizarNombre(rango.nombre)
          
          // Buscar la métrica correspondiente
          const metricKey = Object.keys(this.metrics).find(key => {
            const metric = this.metrics[key]
            const keyNormalizado = this.normalizarNombre(metric.key)
            const aliasMatch = (metric.aliases || []).some(alias => this.normalizarNombre(alias) === nombreNormalizado)
            return keyNormalizado === nombreNormalizado || aliasMatch
          })
          
          if (metricKey && rango.valorInferiorNumerico !== null && rango.valorSuperiorNumerico !== null) {
            this.metrics[metricKey].recommendedMin = rango.valorInferiorNumerico
            this.metrics[metricKey].recommendedMax = rango.valorSuperiorNumerico
          }
        })
      } catch (e) {
        console.error(this.$t('dashboard_metrics_error_loading_ranges'), e)
      }
    },
    
    normalizarNombre(nombre) {
      if (!nombre) return ''
      return String(nombre)
        .normalize('NFD')
        .replace(/\p{Diacritic}/gu, '')
        .toLowerCase()
        .replace(/\s+/g, '')
    },
    
    async loadData() {
      try {
        // Aquí cargarías los datos del servidor
        // Por ahora usamos datos de ejemplo
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getMine()
        const dto = res.data || {}
        
        if (dto.analisisSangre) {
          const parsed = typeof dto.analisisSangre === 'string' 
            ? JSON.parse(dto.analisisSangre) 
            : dto.analisisSangre
          
          if (Array.isArray(parsed)) {
            this.entries = parsed.map(p => ({
              key: p.key || this.mapTipoToKey(p.tipo),
              label: p.label || p.tipo,
              value: p.value || p.valor,
              createdAt: p.createdAt || p.fechaCreacion,
              rango: p.rango
            }))
          }
        }
      } catch (e) {
        console.error(this.$t('dashboard_metrics_error_loading_data'), e)
      }
    },

    mapTipoToKey(tipo) {
      if (!tipo) return null
      const norm = this.normalizarNombre(String(tipo).replace(/\s*\(.*\)/, '').trim())
      const metric = Object.values(this.metrics).find(m => {
        const keyMatch = this.normalizarNombre(m.key) === norm
        const aliasMatch = (m.aliases || []).some(alias => this.normalizarNombre(alias) === norm)
        return keyMatch || aliasMatch
      })
      return metric ? metric.key : norm
    },

    getLatestValue(metricKey) {
      const metricEntries = this.entries
        .filter(e => e.key === metricKey)
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      
      if (metricEntries.length === 0) return null
      
      const latest = metricEntries[0]
      return parseFloat(String(latest.value).replace(',', '.'))
    },

    drawAllGauges() {
      Object.keys(this.metrics).forEach(key => {
        const metric = this.metrics[key]
        const container = this.$refs[`gauge${key.charAt(0).toUpperCase() + key.slice(1)}`]
        
        if (!container) return
        
        const value = this.getLatestValue(key)
        
        this.drawGaugeChart(container, value, {
          label: this.getMetricLabel(metric),
          unit: metric.unit,
          min: metric.gaugeMin,
          max: metric.gaugeMax,
          recommendedMin: metric.recommendedMin,
          recommendedMax: metric.recommendedMax,
          ariaLabel: this.$t('dashboard_metric_gauge_aria', { metric: this.getMetricLabel(metric) })
        })
      })
    },

    updateChart() {
      const container = this.$refs.mainChart
      if (!container) return

      const metric = this.selectedMetricData
      const data = this.prepareChartData(this.entries, metric.key)

      if (data.length === 0) {
        this.renderChartEmpty(container, this.$t('dashboard_metric_no_data'))
        return
      }

      const options = {
        label: this.getMetricLabel(metric),
        color: metric.color,
        recommendedMin: metric.recommendedMin,
        recommendedMax: metric.recommendedMax,
        ariaLabel: this.$t('dashboard_metric_chart_aria', { metric: this.getMetricLabel(metric) })
      }

      switch (this.chartType) {
        case 'line':
          this.drawTimeSeriesChart(container, data, options)
          break
        case 'interactive':
          this.drawInteractiveTimeSeriesChart(container, data, { ...options, height: DASHBOARD_METRICS_UI.INTERACTIVE_CHART_HEIGHT })
          break
        case 'bar':
          this.drawBarChart(container, data, options)
          break
      }
    }
  }
}
</script>

<style scoped>
@import '@/styles/charts.css';

.metrics-dashboard {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding: 2rem;
}

.dashboard-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.gauges-section h3,
.chart-section h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 1rem 0;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.info-box {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: var(--surface-info-bg);
  border: 1px solid var(--surface-info-border);
  border-radius: 8px;
  color: var(--surface-info-text);
}

.info-box svg {
  flex-shrink: 0;
  color: var(--surface-info-icon);
}

.info-box strong {
  display: block;
  margin-bottom: 0.25rem;
}
</style>
