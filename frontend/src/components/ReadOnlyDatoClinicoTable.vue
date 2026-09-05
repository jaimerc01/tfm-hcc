<template>
  <div v-if="entries && entries.length">
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
            </tr>
          </thead>
          <tbody>
            <tr v-for="(e, idx) in pagedEntries" :key="e.id || idx">
              <td>{{ e.tipo }}</td>
              <td>{{ e.valor }} <span class="unit-small">{{ e.unidad }}</span></td>
              <td>{{ formatDate(e.createdAt) }}</td>
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
    </template>
    <p v-else class="empty-hint">{{ $t('no_results_for_filters') }}</p>
  </div>
  <p v-else class="empty-hint">{{ $t('no_results_registered', { domain: domainLabel }) }}</p>
</template>

<script>
import AppPagination from './AppPagination.vue'

const PAGE_SIZE = 10
let uidCounter = 0

export default {
  name: 'ReadOnlyDatoClinicoTable',
  components: { AppPagination },
  props: {
    entries: { type: Array, default: () => [] },
    domainLabel: { type: String, required: true }
  },
  data() {
    uidCounter += 1
    return {
      uid: `readonly-dato-clinico-${uidCounter}`,
      page: 1,
      filtroParametro: '',
      filtroDesde: '',
      filtroHasta: '',
      ordenFecha: 'desc'
    }
  },
  computed: {
    parametrosDisponibles() {
      const tipos = new Set()
      ;(this.entries || []).forEach(e => {
        if (e.tipo) tipos.add(e.tipo)
      })
      return Array.from(tipos).sort((a, b) => a.localeCompare(b))
    },
    hasActiveFilters() {
      return Boolean(this.filtroParametro || this.filtroDesde || this.filtroHasta) || this.ordenFecha !== 'desc'
    },
    filteredEntries() {
      let list = (this.entries || []).slice()

      if (this.filtroParametro) {
        list = list.filter(e => e.tipo === this.filtroParametro)
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
    pagedEntries() {
      const start = (this.page - 1) * PAGE_SIZE
      return this.filteredEntries.slice(start, start + PAGE_SIZE)
    }
  },
  watch: {
    entries() {
      this.page = 1
    },
    filteredEntries() {
      if (this.page > this.totalPages) this.page = 1
    }
  },
  methods: {
    formatDate(iso) {
      if (!iso) return ''
      try {
        return new Date(iso).toLocaleString()
      } catch (e) {
        return iso
      }
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
}

.results-table td {
  padding: 0.75rem 1rem;
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
