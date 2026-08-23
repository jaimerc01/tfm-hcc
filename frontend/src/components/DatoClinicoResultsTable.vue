<template>
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
                @click="removeEntry(idx)"
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
        @click="clearAll"
        :disabled="saving"
        :aria-label="$t('analysis_clear_all_aria', { count: entries.length, domain: domainLabel })"
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
    <p>{{ $t('no_results_registered', { domain: domainLabel }) }}</p>
    <span>{{ $t('add_first_result_hint') }}</span>
  </div>
</template>

<script>
import { getAnalyteLabel, formatDate } from '@/utils/datosClinicos'

export default {
  name: 'DatoClinicoResultsTable',
  props: {
    entries: { type: Array, required: true },
    analytes: { type: Array, required: true },
    saving: { type: Boolean, default: false },
    // Etiqueta traducida del dominio clínico (p. ej. "análisis de sangre", "signos vitales"),
    // usada en los textos ARIA para que reflejen el tipo de dato que se está eliminando.
    domainLabel: { type: String, required: true }
  },
  emits: ['delete-entry', 'clear-all'],
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
    }
  }
}
</script>

<style scoped>
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
