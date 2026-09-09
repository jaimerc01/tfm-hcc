<template>
  <section class="chart-section health-rings" :aria-label="$t('health_rings_title')">
    <div class="chart-header">
      <h3>
        <AppIcon name="activity" size="lg" />
        {{ $t('health_rings_title') }}
      </h3>
    </div>
    <p class="health-rings__subtitle">{{ $t('health_rings_subtitle') }}</p>

    <p v-if="loading" class="health-rings__loading">{{ $t('loading_info') }}</p>

    <div v-else-if="cardVisible" class="health-rings__body">
      <div
        ref="chart"
        class="health-rings__chart"
        role="img"
        :aria-label="ariaLabel"
      ></div>

      <ul class="health-rings__legend">
        <li
          v-for="(ring, i) in ringsView"
          :key="`slot-${i}`"
          class="health-rings__legend-item"
          :class="{ 'is-active': activeRing === i }"
          @mouseenter="setActive(i)"
          @mouseleave="setActive(null)"
        >
          <span
            class="health-rings__swatch"
            :style="{ background: ring.swatchColor }"
            aria-hidden="true"
          ></span>
          <select
            class="health-rings__legend-select"
            :value="ring.key"
            :aria-label="$t('health_rings_change_metric', { n: i + 1 })"
            @change="changeMetric(i, $event.target.value)"
          >
            <option v-for="opt in optionsForSlot(i)" :key="opt.key" :value="opt.key">
              {{ $t(opt.labelKey) }}
            </option>
          </select>
          <span v-if="ring.rangeText" class="health-rings__legend-range">{{ ring.rangeText }}</span>
          <span class="health-rings__legend-value">
            <template v-if="ring.hasData">
              {{ ring.value }} <span class="health-rings__unit">{{ ring.unit }}</span>
            </template>
            <template v-else>{{ $t('health_rings_metric_no_data') }}</template>
          </span>
          <span class="health-rings__status" :class="`health-rings__status--${ring.statusKind}`">
            <AppIcon v-if="ring.statusKind === 'in'" name="check" size="sm" />
            <AppIcon v-else-if="ring.statusKind === 'out'" name="alert-circle" size="sm" />
            {{ ring.statusText }}
          </span>
        </li>
      </ul>
    </div>

    <p v-else class="chart-empty">{{ $t('health_rings_no_data') }}</p>
  </section>
</template>

<script>
import AppIcon from './AppIcon.vue'
import { getThemeColor } from '@/utils/themeColors'
import { useResumenSalud, RESUMEN_METRICS, DEFAULT_RING_KEYS } from '@/composables/useResumenSalud'
import { useChart } from '@/composables/useChart'

const STORAGE_KEY = 'hcc:health-rings-keys'
const SLOT_COUNT = 4
// El color de cada anillo lo pone su posición (azul/verde alternos), no la métrica.
const SLOT_COLOR_TOKENS = ['--chart-series-blue', '--chart-series-green']

// Lee los cuatro huecos guardados; si no hay nada válido usa los de por defecto.
function readStoredKeys() {
  const valid = new Set(RESUMEN_METRICS.map(m => m.key))
  try {
    const raw = JSON.parse(window.localStorage.getItem(STORAGE_KEY))
    if (Array.isArray(raw)) {
      const deduped = [...new Set(raw.filter(k => valid.has(k)))].slice(0, SLOT_COUNT)
      if (deduped.length === SLOT_COUNT) return deduped
    }
  } catch (e) {
    // localStorage no disponible o valor corrupto: se usan los anillos por defecto.
  }
  return [...DEFAULT_RING_KEYS]
}

export default {
  name: 'HealthRingsCard',
  components: { AppIcon },
  setup() {
    const { metrics, loading, load } = useResumenSalud()
    const { drawHealthRingsChart } = useChart()
    return { metrics, loading, load, drawHealthRingsChart }
  },
  data() {
    return {
      selectedKeys: readStoredKeys(),
      activeRing: null,
      chartHandle: null
    }
  },
  computed: {
    metricByKey() {
      const map = {}
      this.metrics.forEach(m => { map[m.key] = m })
      return map
    },
    ringsView() {
      return this.selectedKeys.map((key, i) => {
        const meta = RESUMEN_METRICS.find(m => m.key === key) || { key, labelKey: key }
        const m = this.metricByKey[key] || { key, labelKey: meta.labelKey, unit: '', value: null, min: null, max: null }
        const colorToken = SLOT_COLOR_TOKENS[i % SLOT_COLOR_TOKENS.length]
        const hasData = m.value != null
        const hasRange = m.min != null && m.max != null
        let statusKind = 'none'
        if (hasData && hasRange) {
          statusKind = (m.value >= m.min && m.value <= m.max) ? 'in' : 'out'
        }
        const statusText = statusKind === 'in'
          ? this.$t('health_rings_in_range')
          : statusKind === 'out'
            ? this.$t('health_rings_out_of_range')
            : this.$t('health_rings_no_reference')
        const swatchColor = statusKind === 'out'
          ? getThemeColor('--chart-out-of-range')
          : statusKind === 'in'
            ? getThemeColor(colorToken)
            : getThemeColor('--chart-label-muted')
        return {
          key,
          labelKey: meta.labelKey,
          colorToken,
          unit: m.unit,
          value: m.value,
          min: m.min,
          max: m.max,
          hasData,
          hasRange,
          statusKind,
          statusText,
          swatchColor,
          rangeText: hasRange ? `${m.min}–${m.max} ${m.unit}`.trim() : ''
        }
      })
    },
    // Se muestra el bloque en cuanto el paciente tiene datos de algún parámetro.
    cardVisible() {
      return this.metrics.some(m => m.value != null)
    },
    inRangeCount() {
      return this.ringsView.filter(r => r.statusKind === 'in').length
    },
    withStatusCount() {
      return this.ringsView.filter(r => r.statusKind !== 'none').length
    },
    ariaLabel() {
      if (!this.withStatusCount) return this.$t('health_rings_subtitle')
      return this.$t('health_rings_summary', {
        count: this.inRangeCount,
        total: this.withStatusCount
      })
    }
  },
  watch: {
    metrics: {
      handler() {
        this.$nextTick(() => this.render())
      },
      deep: true
    }
  },
  async mounted() {
    await this.load()
    this.$nextTick(() => this.render())
  },
  beforeUnmount() {
    const container = this.$refs.chart
    if (container) container.innerHTML = ''
  },
  methods: {
    persistKeys() {
      try {
        window.localStorage.setItem(STORAGE_KEY, JSON.stringify(this.selectedKeys))
      } catch (e) {
        // Sin persistencia: la elección solo dura esta sesión.
      }
    },
    // Opciones del selector de un hueco: todas las métricas que no estén ya
    // elegidas en OTRO hueco (la del hueco actual sí se mantiene).
    optionsForSlot(slot) {
      const takenElsewhere = new Set(this.selectedKeys.filter((_, i) => i !== slot))
      return RESUMEN_METRICS.filter(m => !takenElsewhere.has(m.key))
    },
    changeMetric(slot, key) {
      if (!key || this.selectedKeys[slot] === key) return
      this.selectedKeys.splice(slot, 1, key)
      this.persistKeys()
      this.$nextTick(() => this.render())
    },
    render() {
      const container = this.$refs.chart
      if (!container || !this.cardVisible) return

      this.chartHandle = this.drawHealthRingsChart(
        container,
        this.ringsView.map(r => ({
          label: this.$t(r.labelKey),
          value: r.hasData ? r.value : null,
          min: r.min,
          max: r.max,
          unit: r.unit,
          colorToken: r.colorToken
        })),
        {
          centerPrimary: this.withStatusCount ? `${this.inRangeCount}/${this.withStatusCount}` : '—',
          centerSecondary: this.$t('health_rings_in_range_short'),
          ariaLabel: this.ariaLabel,
          emptyMessage: this.$t('health_rings_no_data'),
          onHoverRing: (i) => { this.activeRing = i }
        }
      )
    },
    setActive(i) {
      this.activeRing = i
      if (this.chartHandle) this.chartHandle.setHighlight(i)
    }
  }
}
</script>

<style scoped>
@import '@/styles/charts.css';
</style>
