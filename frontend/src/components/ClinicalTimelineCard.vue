<template>
  <div class="chart-section clinical-timeline">
    <div class="chart-header">
      <h3>
        <AppIcon name="activity" size="lg" />
        {{ $t('clinical_timeline_title') }}
      </h3>
      <div class="chart-controls" v-if="analyteOptions.length">
        <div class="control-group">
          <label class="form-label" for="timeline-param-select">{{ $t('clinical_timeline_param_label') }}</label>
          <select
            id="timeline-param-select"
            v-model="selectedKey"
            class="form-input chart-select"
            @change="render"
          >
            <option v-for="a in analyteOptions" :key="a.key" :value="a.key">{{ $t(a.labelKey) }}</option>
          </select>
        </div>
      </div>
    </div>

    <div
      ref="chart"
      class="chart-container"
      role="img"
      :aria-label="ariaLabel"
    ></div>

    <div class="clinical-timeline__events">
      <h4>{{ $t('clinical_timeline_events_heading') }}</h4>
      <ol v-if="events.length" class="clinical-timeline__event-list">
        <li
          v-for="(ev, i) in events"
          :key="ev.id || i"
          class="clinical-timeline__event"
          :class="{ 'is-active': activeEvent === i }"
          @mouseenter="setActiveEvent(i)"
          @mouseleave="setActiveEvent(null)"
        >
          <span class="clinical-timeline__event-num" aria-hidden="true">{{ i + 1 }}</span>
          <span class="clinical-timeline__event-body">
            <span class="clinical-timeline__event-meta">
              <AppIcon :name="ev.kind === 'familiar' ? 'users' : 'user'" size="sm" />
              {{ ev.kind === 'familiar' ? $t('clinical_timeline_event_familiar') : $t('clinical_timeline_event_personal') }}
              <span class="clinical-timeline__event-date">
                {{ $t('clinical_timeline_registered_on', { date: formatDate(ev.rawDate) }) }}
              </span>
            </span>
            <span class="clinical-timeline__event-text">{{ ev.label }}</span>
          </span>
        </li>
      </ol>
      <p v-else class="empty-hint">{{ $t('clinical_timeline_no_events') }}</p>
    </div>
  </div>
</template>

<script>
import AppIcon from './AppIcon.vue'
import historiaClinicaService from '@/services/historiaClinicaService'
import { useChart } from '@/composables/useChart'
import { applyRangos, mapHistorialEntries, getAnalyteLabel } from '@/utils/datosClinicos'
import { DEFAULT_ANALYTES as BLOOD_ANALYTES } from '@/composables/useAnalisisSangre'
import { DEFAULT_ANALYTES as VITAL_ANALYTES } from '@/composables/useSignosVitales'
import { DEFAULT_ANALYTES as URINE_ANALYTES } from '@/composables/useAnalisisOrina'

export default {
  name: 'ClinicalTimelineCard',
  components: { AppIcon },
  props: {
    analisisSangre: { type: [Array, String], default: () => [] },
    signosVitales: { type: [Array, String], default: () => [] },
    analisisOrina: { type: [Array, String], default: () => [] },
    antecedentes: { type: Array, default: () => [] }
  },
  setup() {
    const { drawAnnotatedTimelineChart, prepareChartData } = useChart()
    return { drawAnnotatedTimelineChart, prepareChartData }
  },
  data() {
    return {
      analytes: [
        ...BLOOD_ANALYTES.map(a => ({ ...a, domain: 'sangre' })),
        ...VITAL_ANALYTES.map(a => ({ ...a, domain: 'signos' })),
        ...URINE_ANALYTES.map(a => ({ ...a, domain: 'orina' }))
      ],
      selectedKey: '',
      activeEvent: null,
      chartHandle: null
    }
  },
  computed: {
    entriesByDomain() {
      return {
        sangre: mapHistorialEntries(this.analisisSangre, BLOOD_ANALYTES),
        signos: mapHistorialEntries(this.signosVitales, VITAL_ANALYTES),
        orina: mapHistorialEntries(this.analisisOrina, URINE_ANALYTES)
      }
    },
    allEntries() {
      const e = this.entriesByDomain
      return [...e.sangre, ...e.signos, ...e.orina]
    },
    analyteOptions() {
      // Solo los parámetros que tienen al menos un registro.
      const withData = new Set(this.allEntries.map(x => x.key))
      return this.analytes.filter(a => withData.has(a.key))
    },
    selectedAnalyte() {
      return this.analytes.find(a => a.key === this.selectedKey) || null
    },
    events() {
      return (this.antecedentes || [])
        .map((a, idx) => {
          const rawDate = a.createdAt || a.fechaCreacion || null
          const date = rawDate ? new Date(rawDate) : null
          return {
            id: a.id || `ant-${idx}`,
            rawDate,
            date: (date && !Number.isNaN(date.getTime())) ? date : null,
            kind: a.categoria === 'FAMILIAR' ? 'familiar' : 'personal',
            label: a.descripcion || ''
          }
        })
        .filter(e => e.date && e.label)
        .sort((a, b) => a.date - b.date)
    },
    ariaLabel() {
      const label = this.selectedAnalyte
        ? getAnalyteLabel(this.selectedAnalyte, this.$t)
        : this.$t('clinical_timeline_title')
      return this.$t('clinical_timeline_aria', { label, count: this.events.length })
    }
  },
  watch: {
    analyteOptions() {
      this.ensureSelection()
    },
    allEntries() {
      this.$nextTick(() => this.render())
    },
    events() {
      this.$nextTick(() => this.render())
    }
  },
  async created() {
    try {
      const res = await historiaClinicaService.getRangos()
      applyRangos(this.analytes, res.data || [])
    } catch (e) {
      // Los rangos son opcionales: la banda recomendada simplemente no se dibuja.
    }
    this.ensureSelection()
  },
  mounted() {
    this.$nextTick(() => this.render())
  },
  beforeUnmount() {
    const container = this.$refs.chart
    if (container) container.innerHTML = ''
  },
  methods: {
    formatDate(iso) {
      if (!iso) return ''
      const d = new Date(iso)
      return Number.isNaN(d.getTime()) ? String(iso) : d.toLocaleDateString('es-ES')
    },
    ensureSelection() {
      const options = this.analyteOptions
      if (!options.length) {
        this.selectedKey = ''
        return
      }
      if (!options.some(a => a.key === this.selectedKey)) {
        this.selectedKey = options[0].key
      }
      this.$nextTick(() => this.render())
    },
    render() {
      const container = this.$refs.chart
      if (!container) return

      const analyte = this.selectedAnalyte
      if (!analyte) {
        container.innerHTML = ''
        const empty = document.createElement('div')
        empty.className = 'chart-empty'
        empty.textContent = this.$t('clinical_timeline_no_data')
        container.appendChild(empty)
        return
      }

      const series = this.prepareChartData(this.allEntries, analyte.key)
      const label = getAnalyteLabel(analyte, this.$t)

      this.chartHandle = this.drawAnnotatedTimelineChart(
        container,
        series,
        this.events.map(e => ({ date: e.date, kind: e.kind, label: e.label })),
        {
          label,
          unit: analyte.unit || '',
          recommendedMin: analyte.recommendedMin,
          recommendedMax: analyte.recommendedMax,
          ariaLabel: this.ariaLabel,
          emptyMessage: this.$t('clinical_timeline_no_historical', { label }),
          onHoverEvent: (i) => { this.activeEvent = i }
        }
      )
    },
    setActiveEvent(i) {
      this.activeEvent = i
      if (this.chartHandle && this.chartHandle.setActiveEvent) {
        this.chartHandle.setActiveEvent(i)
      }
    }
  }
}
</script>

<style scoped>
@import '@/styles/charts.css';
</style>
