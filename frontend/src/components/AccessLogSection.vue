<template>
  <section class="access-log-section" :aria-busy="loading ? 'true' : 'false'" aria-labelledby="access-log-heading">
    <h2 id="access-log-heading" class="sr-only">{{ $t('access_log_section_title') }}</h2>

    <div class="info-box">
      <AppIcon name="info" size="lg" />
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
      <AppIcon name="alert-circle" size="lg" />
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
          <tr v-for="(a, idx) in pagedAccesos" :key="`${a.timestamp || 'no-date'}-${idx}`">
            <td>{{ formatDateTime(a.timestamp) }}</td>
            <td>{{ a.ruta }}</td>
            <td>{{ a.metodo }}</td>
            <td>{{ a.estado }}</td>
            <td>{{ a.ip }}</td>
          </tr>
        </tbody>
      </table>

      <AppPagination
        :page="page"
        :total-pages="totalPages"
        :aria-label="$t('pagination_nav_aria', { domain: $t('access_log_section_title') })"
        @prev="goToPrevPage"
        @next="goToNextPage"
      />
    </div>

    <p v-else class="empty-hint">{{ $t('no_access_log_registered') }}</p>
  </section>
</template>

<script>
import authService from '@/services/authService'
import AppIcon from './AppIcon.vue'
import AppPagination from './AppPagination.vue'

const PAGE_SIZE = 10

export default {
  name: 'AccessLogSection',
  components: { AppIcon, AppPagination },
  data() {
    return {
      accesos: [],
      loading: false,
      error: null,
      filtroDesde: '',
      filtroHasta: '',
      page: 1
    }
  },
  computed: {
    totalPages() {
      return Math.max(1, Math.ceil(this.accesos.length / PAGE_SIZE))
    },
    pagedAccesos() {
      const start = (this.page - 1) * PAGE_SIZE
      return this.accesos.slice(start, start + PAGE_SIZE)
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
        this.page = 1
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
    goToPrevPage() {
      if (this.page > 1) this.page -= 1
    },
    goToNextPage() {
      if (this.page < this.totalPages) this.page += 1
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
