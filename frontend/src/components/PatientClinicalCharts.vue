<template>
  <div class="patient-charts">
    <div class="patient-charts__nav" role="tablist" :aria-label="$t('historical_evolution')">
      <button
        v-for="d in dominiosConDatos"
        :key="d.id"
        type="button"
        role="tab"
        :class="['patient-charts__tab', { 'is-active': dominioActivo === d.id }]"
        :aria-selected="dominioActivo === d.id"
        @click="dominioActivo = d.id"
      >
        {{ $t(d.labelKey) }}
      </button>
    </div>

    <template v-for="d in dominiosConDatos" :key="`panel-${d.id}`">
      <DatoClinicoChart
        v-if="dominioActivo === d.id"
        :entries="d.entries"
        :analytes="d.analytes"
      />
    </template>

    <p v-if="!dominiosConDatos.length" class="empty-hint">{{ $t('no_chart_data') }}</p>
  </div>
</template>

<script>
import DatoClinicoChart from './DatoClinicoChart.vue'
import historiaClinicaService from '@/services/historiaClinicaService'
import { applyRangos, mapHistorialEntries } from '@/utils/datosClinicos'
import { DEFAULT_ANALYTES as BLOOD_ANALYTES } from '@/composables/useAnalisisSangre'
import { DEFAULT_ANALYTES as VITAL_ANALYTES } from '@/composables/useSignosVitales'
import { DEFAULT_ANALYTES as URINE_ANALYTES } from '@/composables/useAnalisisOrina'

export default {
  name: 'PatientClinicalCharts',
  components: { DatoClinicoChart },
  props: {
    analisisSangre: { type: [Array, String], default: () => [] },
    signosVitales: { type: [Array, String], default: () => [] },
    analisisOrina: { type: [Array, String], default: () => [] }
  },
  data() {
    return {
      bloodAnalytes: BLOOD_ANALYTES.map(a => ({ ...a })),
      vitalAnalytes: VITAL_ANALYTES.map(a => ({ ...a })),
      urineAnalytes: URINE_ANALYTES.map(a => ({ ...a })),
      dominioActivo: 'sangre'
    }
  },
  computed: {
    dominios() {
      return [
        { id: 'sangre', labelKey: 'analysis', analytes: this.bloodAnalytes, raw: this.analisisSangre },
        { id: 'signos', labelKey: 'vital_signs', analytes: this.vitalAnalytes, raw: this.signosVitales },
        { id: 'orina', labelKey: 'urine_analysis', analytes: this.urineAnalytes, raw: this.analisisOrina }
      ].map(d => ({ ...d, entries: mapHistorialEntries(d.raw, d.analytes) }))
    },
    dominiosConDatos() {
      return this.dominios.filter(d => d.entries.length > 0)
    }
  },
  watch: {
    dominiosConDatos(lista) {
      if (lista.length && !lista.some(d => d.id === this.dominioActivo)) {
        this.dominioActivo = lista[0].id
      }
    }
  },
  async created() {
    try {
      const res = await historiaClinicaService.getRangos()
      const rangos = res.data || []
      applyRangos(this.bloodAnalytes, rangos)
      applyRangos(this.vitalAnalytes, rangos)
      applyRangos(this.urineAnalytes, rangos)
    } catch (e) {
      // Los rangos son opcionales: la gráfica se muestra igualmente sin la banda recomendada.
    }
  }
}
</script>

<style scoped>
.patient-charts {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.patient-charts__nav {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.patient-charts__tab {
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--card-bg);
  color: var(--text-secondary);
  padding: 0.4rem 0.9rem;
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
}

.patient-charts__tab.is-active {
  background: var(--primary-color);
  color: var(--on-primary);
  border-color: var(--primary-color);
}

.patient-charts__tab:focus-visible {
  outline: var(--focus-outline);
  outline-offset: var(--focus-outline-offset);
}
</style>
