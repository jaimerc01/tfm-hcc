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

    <div v-else-if="hasAnyData" class="health-rings__body">
      <div
        ref="chart"
        class="health-rings__chart"
        role="img"
        :aria-label="ariaLabel"
      ></div>

      <ul class="health-rings__legend">
        <li
          v-for="(ring, i) in ringsView"
          :key="ring.key"
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
          <span class="health-rings__legend-text">
            <span class="health-rings__legend-name">{{ $t(ring.labelKey) }}</span>
            <span v-if="ring.rangeText" class="health-rings__legend-range">{{ ring.rangeText }}</span>
          </span>
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
import { useResumenSalud } from '@/composables/useResumenSalud'
import { useChart } from '@/composables/useChart'

export default {
  name: 'HealthRingsCard',
  components: { AppIcon },
  setup() {
    const { rings, loading, load } = useResumenSalud()
    const { drawHealthRingsChart } = useChart()
    return { rings, loading, load, drawHealthRingsChart }
  },
  data() {
    return {
      activeRing: null,
      chartHandle: null
    }
  },
  computed: {
    ringsView() {
      return this.rings.map(r => {
        const hasData = r.value != null
        const hasRange = r.min != null && r.max != null
        let statusKind = 'none'
        if (hasData && hasRange) {
          statusKind = (r.value >= r.min && r.value <= r.max) ? 'in' : 'out'
        }
        const statusText = statusKind === 'in'
          ? this.$t('health_rings_in_range')
          : statusKind === 'out'
            ? this.$t('health_rings_out_of_range')
            : this.$t('health_rings_no_reference')
        const swatchColor = statusKind === 'out'
          ? getThemeColor('--chart-out-of-range')
          : statusKind === 'in'
            ? getThemeColor(r.colorToken)
            : getThemeColor('--chart-label-muted')
        return {
          ...r,
          hasData,
          hasRange,
          statusKind,
          statusText,
          swatchColor,
          rangeText: hasRange ? `${r.min}–${r.max} ${r.unit}`.trim() : ''
        }
      })
    },
    hasAnyData() {
      return this.ringsView.some(r => r.hasData)
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
    rings: {
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
    render() {
      const container = this.$refs.chart
      if (!container || !this.hasAnyData) return

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
