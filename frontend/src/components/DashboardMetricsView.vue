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
        <strong>{{ $t('recommended_range', { metric: selectedMetricData.label }) }}</strong>
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
    return {
      chartType: 'interactive',
      selectedMetric: 'glucosa',
      entries: [],
      metrics: {
        glucosa: {
          key: 'glucosa',
          label: 'Glucosa',
          unit: 'mg/dL',
          color: '#c73333',
          recommendedMin: null,
          recommendedMax: null,
          gaugeMin: 0,
          gaugeMax: 300
        },
        hemoglobina: {
          key: 'hemoglobina',
          label: 'Hemoglobina',
          unit: 'g/dL',
          color: '#dc2626',
          recommendedMin: null,
          recommendedMax: null,
          gaugeMin: 0,
          gaugeMax: 20
        },
        colesterol: {
          key: 'colesterol',
          label: 'Colesterol Total',
          unit: 'mg/dL',
          color: '#ea580c',
          recommendedMin: null,
          recommendedMax: null,
          gaugeMin: 0,
          gaugeMax: 400
        },
        trigliceridos: {
          key: 'trigliceridos',
          label: 'Triglicéridos',
          unit: 'mg/dL',
          color: '#d97706',
          recommendedMin: null,
          recommendedMax: null,
          gaugeMin: 0,
          gaugeMax: 500
        },
        creatinina: {
          key: 'creatinina',
          label: 'Creatinina',
          unit: 'mg/dL',
          color: '#0284c7',
          recommendedMin: null,
          recommendedMax: null,
          gaugeMin: 0,
          gaugeMax: 3
        },
        hematocrito: {
          key: 'hematocrito',
          label: 'Hematocrito',
          unit: '%',
          color: '#7c3aed',
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
    }
  },

  async mounted() {
    await this.loadRangos()
    await this.loadData()
    this.drawAllGauges()
    this.updateChart()
  },

  methods: {
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
            const labelNormalizado = this.normalizarNombre(metric.label)
            const keyNormalizado = this.normalizarNombre(metric.key)
            return labelNormalizado === nombreNormalizado || keyNormalizado === nombreNormalizado
          })
          
          if (metricKey && rango.valorInferiorNumerico !== null && rango.valorSuperiorNumerico !== null) {
            this.metrics[metricKey].recommendedMin = rango.valorInferiorNumerico
            this.metrics[metricKey].recommendedMax = rango.valorSuperiorNumerico
            console.log(`Rango cargado para ${this.metrics[metricKey].label}: ${rango.valorInferiorNumerico} - ${rango.valorSuperiorNumerico}`)
          }
        })
      } catch (e) {
        console.error('Error cargando rangos:', e)
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
        console.error('Error cargando datos', e)
      }
    },

    mapTipoToKey(tipo) {
      if (!tipo) return null
      const norm = String(tipo).toLowerCase().replace(/\s*\(.*\)/, '').trim()
      const map = {
        glucosa: 'glucosa',
        hemoglobina: 'hemoglobina',
        'colesterol total': 'colesterol',
        triglicéridos: 'trigliceridos',
        trigliceridos: 'trigliceridos',
        creatinina: 'creatinina',
        hematocrito: 'hematocrito'
      }
      return map[norm] || norm
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
          label: metric.label,
          unit: metric.unit,
          min: metric.gaugeMin,
          max: metric.gaugeMax,
          recommendedMin: metric.recommendedMin,
          recommendedMax: metric.recommendedMax,
          ariaLabel: `Medidor de ${metric.label}`
        })
      })
    },

    updateChart() {
      const container = this.$refs.mainChart
      if (!container) return

      const metric = this.selectedMetricData
      const data = this.prepareChartData(this.entries, metric.key)

      if (data.length === 0) {
        container.innerHTML = '<div class="chart-empty">No hay datos disponibles para esta métrica</div>'
        return
      }

      const options = {
        label: metric.label,
        color: metric.color,
        recommendedMin: metric.recommendedMin,
        recommendedMax: metric.recommendedMax,
        ariaLabel: `Gráfico de ${metric.label}`
      }

      switch (this.chartType) {
        case 'line':
          this.drawTimeSeriesChart(container, data, options)
          break
        case 'interactive':
          this.drawInteractiveTimeSeriesChart(container, data, { ...options, height: 400 })
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
  color: var(--text-primary, #262626);
  margin: 0;
}

.gauges-section h3,
.chart-section h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
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
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  color: #1e40af;
}

.info-box svg {
  flex-shrink: 0;
  color: #3b82f6;
}

.info-box strong {
  display: block;
  margin-bottom: 0.25rem;
}
</style>
