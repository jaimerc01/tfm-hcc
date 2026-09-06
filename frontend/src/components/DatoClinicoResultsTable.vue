<template>
  <div v-if="entries.length" class="section section-card results-section">
    <div class="section-title">
      <AppIcon name="list" size="lg" />
      {{ $t('registered_results') }}
      <span class="badge badge-info">{{ entries.length }}</span>
    </div>

    <form class="filters-form" @submit.prevent>
      <div class="form-group">
        <label class="form-label" :for="`${uid}-parametro`">{{ $t('filter_by_parameter') }}</label>
        <select :id="`${uid}-parametro`" v-model="filtroParametro" class="form-input">
          <option value="">{{ $t('all_parameters') }}</option>
          <option v-for="p in parametrosDisponibles" :key="p" :value="p">{{ p }}</option>
        </select>
      </div>
      <div class="form-group">
        <label class="form-label" :for="`${uid}-desde`">{{ $t('filter_from_date') }}</label>
        <input :id="`${uid}-desde`" type="date" v-model="filtroDesde" class="form-input" />
      </div>
      <div class="form-group">
        <label class="form-label" :for="`${uid}-hasta`">{{ $t('filter_to_date') }}</label>
        <input :id="`${uid}-hasta`" type="date" v-model="filtroHasta" class="form-input" />
      </div>
      <div class="form-group">
        <label class="form-label" :for="`${uid}-orden`">{{ $t('sort_by_date') }}</label>
        <select :id="`${uid}-orden`" v-model="ordenFecha" class="form-input">
          <option value="desc">{{ $t('sort_date_desc') }}</option>
          <option value="asc">{{ $t('sort_date_asc') }}</option>
        </select>
      </div>
      <div class="form-actions filters-actions">
        <button type="button" class="btn-secondary" :disabled="!hasActiveFilters" @click="resetFilters">
          {{ $t('clear_filters') }}
        </button>
      </div>
    </form>

    <template v-if="filteredEntries.length">
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
            <tr v-for="(e, localIdx) in pagedEntries" :key="e.id || pageStart + localIdx">
              <td>
                <div class="param-cell">
                  <AppIcon name="more-vertical" size="sm" />
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
                  @click="removeEntry(e)"
                  :aria-label="$t('analysis_delete_entry_aria', { label: getEntryLabel(e), date: formatDate(e.createdAt) })"
                  class="btn-icon btn-danger">
                  <AppIcon name="trash" size="sm" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AppPagination
        :page="page"
        :total-pages="totalPages"
        :aria-label="$t('pagination_nav_aria', { domain: domainLabel })"
        @prev="goToPrevPage"
        @next="goToNextPage"
      />

      <div class="form-actions">
        <button
          type="button"
          @click="clearAll"
          :disabled="saving"
          :aria-label="$t('analysis_clear_all_aria', { count: entries.length, domain: domainLabel })"
          class="btn-danger">
          <AppIcon name="trash" size="md" />
          {{ $t('analysis_clear_all_button') }}
        </button>
      </div>
    </template>

    <p v-else class="empty-hint">{{ $t('no_results_for_filters') }}</p>
  </div>

  <!-- Empty state -->
  <div v-else class="empty-state-small">
    <AppIcon name="file-text" size="3xl" />
    <p>{{ $t('no_results_registered', { domain: domainLabel }) }}</p>
    <span>{{ $t('add_first_result_hint') }}</span>
  </div>
</template>

<script>
import { getAnalyteLabel, formatDate } from '@/utils/datosClinicos'
import AppIcon from './AppIcon.vue'
import AppPagination from './AppPagination.vue'

const PAGE_SIZE = 10
let uidCounter = 0

export default {
  name: 'DatoClinicoResultsTable',
  components: { AppIcon, AppPagination },
  props: {
    entries: { type: Array, required: true },
    analytes: { type: Array, required: true },
    saving: { type: Boolean, default: false },
    // Etiqueta traducida del dominio clínico (p. ej. "análisis de sangre", "signos vitales"),
    // usada en los textos ARIA para que reflejen el tipo de dato que se está eliminando.
    domainLabel: { type: String, required: true }
  },
  emits: ['delete-entry', 'clear-all'],
  data() {
    uidCounter += 1
    return {
      uid: `dato-clinico-results-${uidCounter}`,
      page: 1,
      filtroParametro: '',
      filtroDesde: '',
      filtroHasta: '',
      ordenFecha: 'desc'
    }
  },
  computed: {
    parametrosDisponibles() {
      const labels = new Set()
      this.entries.forEach(e => {
        const label = this.getEntryLabel(e)
        if (label) labels.add(label)
      })
      return Array.from(labels).sort((a, b) => a.localeCompare(b))
    },
    hasActiveFilters() {
      return Boolean(this.filtroParametro || this.filtroDesde || this.filtroHasta) || this.ordenFecha !== 'desc'
    },
    filteredEntries() {
      let list = this.entries.slice()

      if (this.filtroParametro) {
        list = list.filter(e => this.getEntryLabel(e) === this.filtroParametro)
      }
      if (this.filtroDesde) {
        const desde = new Date(`${this.filtroDesde}T00:00:00`).getTime()
        list = list.filter(e => e.createdAt && new Date(e.createdAt).getTime() >= desde)
      }
      if (this.filtroHasta) {
        const hasta = new Date(`${this.filtroHasta}T23:59:59`).getTime()
        list = list.filter(e => e.createdAt && new Date(e.createdAt).getTime() <= hasta)
      }

      list.sort((a, b) => {
        const ta = a.createdAt ? new Date(a.createdAt).getTime() : 0
        const tb = b.createdAt ? new Date(b.createdAt).getTime() : 0
        return this.ordenFecha === 'asc' ? ta - tb : tb - ta
      })
      return list
    },
    totalPages() {
      return Math.max(1, Math.ceil(this.filteredEntries.length / PAGE_SIZE))
    },
    pageStart() {
      return (this.page - 1) * PAGE_SIZE
    },
    pagedEntries() {
      return this.filteredEntries.slice(this.pageStart, this.pageStart + PAGE_SIZE)
    }
  },
  watch: {
    // Si al borrar, filtrar o vaciar la lista la página actual queda fuera de rango,
    // vuelve a la primera en vez de dejar la tabla en blanco.
    filteredEntries() {
      if (this.page > this.totalPages) this.page = 1
    }
  },
  methods: {
    getEntryLabel(entry) {
      if (!entry) return ''
      if (entry.key) {
        const analyte = this.analytes.find(a => a.key === entry.key)
        if (analyte) return getAnalyteLabel(analyte, this.$t)
      }
      return entry.label || entry.tipo || entry.key || ''
    },
    formatDate(iso) {
      return formatDate(iso)
    },
    removeEntry(entry) {
      this.$emit('delete-entry', entry?.id ?? null)
    },
    clearAll() {
      this.$emit('clear-all')
    },
    resetFilters() {
      this.filtroParametro = ''
      this.filtroDesde = ''
      this.filtroHasta = ''
      this.ordenFecha = 'desc'
      this.page = 1
    },
    goToPrevPage() {
      if (this.page > 1) this.page -= 1
    },
    goToNextPage() {
      if (this.page < this.totalPages) this.page += 1
    }
  }
}
</script>

<style scoped>
.filters-form {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: flex-end;
  padding: 1rem;
  margin-bottom: 1rem;
  background: var(--bg-light);
  border-radius: 8px;
}

.filters-form .form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  min-width: 180px;
}

.filters-actions {
  display: flex;
  gap: 0.5rem;
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

.actions-col {
  text-align: right;
  width: 80px;
}

.btn-icon:focus-visible,
.btn-danger:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .results-table tbody tr {
    transition: none;
    animation: none;
  }
}

@media (max-width: 768px) {
  .results-table {
    font-size: 0.8125rem;
  }

  .results-table th,
  .results-table td {
    padding: 0.5rem;
  }
}

@media (max-width: 640px) {
  .filters-form {
    flex-direction: column;
    align-items: stretch;
  }

  .filters-form .form-group {
    min-width: 0;
  }
}
</style>
