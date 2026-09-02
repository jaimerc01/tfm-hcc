<template>
  <section class="access-log-section" :aria-busy="loading ? 'true' : 'false'" aria-labelledby="access-log-heading">
    <h2 id="access-log-heading" class="sr-only">{{ $t('access_log_section_title') }}</h2>

    <div class="info-box">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      <span>{{ $t('access_log_help') }}</span>
    </div>

    <form class="filters-form" @submit.prevent="applyFilters">
      <div class="form-group">
        <label class="form-label" for="access-log-filtro-desde">{{ $t('filter_from_date') }}</label>
        <input id="access-log-filtro-desde" type="date" v-model="filtroDesde" class="form-input" />
      </div>
      <div class="form-group">
        <label class="form-label" for="access-log-filtro-hasta">{{ $t('filter_to_date') }}</label>
        <input id="access-log-filtro-hasta" type="date" v-model="filtroHasta" class="form-input" />
      </div>
      <div class="form-actions filters-actions">
        <button type="submit" class="btn-primary" :disabled="loading">{{ $t('apply_filters') }}</button>
        <button type="button" class="btn-secondary" @click="resetFilters" :disabled="loading">{{ $t('clear_filters') }}</button>
      </div>
    </form>

    <div v-if="error" class="alert alert-danger" role="alert" aria-live="assertive">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      {{ error }}
    </div>

    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
    </div>

    <div v-else-if="accesos.length" class="results-table-container">
      <table class="results-table">
        <thead>
          <tr>
            <th>{{ $t('date_col') }}</th>
            <th>{{ $t('access_log_route_col') }}</th>
            <th>{{ $t('access_log_method_col') }}</th>
            <th>{{ $t('access_log_status_col') }}</th>
            <th>{{ $t('access_log_origin_col') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(a, idx) in accesos" :key="`${a.timestamp || 'no-date'}-${idx}`">
            <td>{{ formatDateTime(a.timestamp) }}</td>
            <td>{{ a.ruta }}</td>
            <td>{{ a.metodo }}</td>
            <td>{{ a.estado }}</td>
            <td>{{ a.ip }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-else class="empty-hint">{{ $t('no_access_log_registered') }}</p>
  </section>
</template>

<script>
import authService from '@/services/authService'

export default {
  name: 'AccessLogSection',
  data() {
    return {
      accesos: [],
      loading: false,
      error: null,
      filtroDesde: '',
      filtroHasta: ''
    }
  },
  created() {
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      this.error = null
      try {
        this.accesos = await authService.misLogs({
          desde: this.filtroDesde ? `${this.filtroDesde}T00:00:00` : undefined,
          hasta: this.filtroHasta ? `${this.filtroHasta}T23:59:59` : undefined
        })
      } catch (e) {
        this.error = e.message || this.$t('error_loading_access_log')
        this.accesos = []
      } finally {
        this.loading = false
      }
    },
    applyFilters() {
      this.load()
    },
    resetFilters() {
      this.filtroDesde = ''
      this.filtroHasta = ''
      this.load()
    },
    formatDateTime(dt) {
      if (!dt) return ''
      const d = new Date(dt)
      if (isNaN(d)) return dt
      return d.toLocaleString()
    }
  }
}
</script>

<style scoped>
.access-log-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.filters-form {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: flex-end;
  padding: 1rem;
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
}

@media (max-width: 640px) {
  .filters-form {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
