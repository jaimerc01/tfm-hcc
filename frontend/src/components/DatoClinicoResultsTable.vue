<template>
  <div v-if="entries.length" class="section section-card results-section">
    <div class="section-title">
      <AppIcon name="list" size="lg" />
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
          <tr v-for="(e, localIdx) in pagedEntries" :key="pageStart + localIdx">
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
                @click="removeEntry(pageStart + localIdx)"
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
    return {
      page: 1
    }
  },
  computed: {
    totalPages() {
      return Math.max(1, Math.ceil(this.entries.length / PAGE_SIZE))
    },
    pageStart() {
      return (this.page - 1) * PAGE_SIZE
    },
    pagedEntries() {
      return this.entries.slice(this.pageStart, this.pageStart + PAGE_SIZE)
    }
  },
  watch: {
    // Si se borra el último elemento de la última página, o se borran todos de golpe
    // con "clearAll", vuelve a una página válida en vez de dejar la tabla en blanco.
    totalPages(newTotalPages) {
      if (this.page > newTotalPages) {
        this.page = newTotalPages
      }
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
    removeEntry(idx) {
      this.$emit('delete-entry', idx)
    },
    clearAll() {
      this.$emit('clear-all')
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
</style>
