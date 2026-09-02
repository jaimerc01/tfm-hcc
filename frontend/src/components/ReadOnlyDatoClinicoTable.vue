<template>
  <div v-if="entries && entries.length" class="results-table-container">
    <table class="results-table">
      <thead>
        <tr>
          <th>{{ $t('parameter_col') }}</th>
          <th>{{ $t('value_col') }}</th>
          <th>{{ $t('date_col') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(e, idx) in entries" :key="e.id || idx">
          <td>{{ e.tipo }}</td>
          <td>{{ e.valor }} <span class="unit-small">{{ e.unidad }}</span></td>
          <td>{{ formatDate(e.createdAt) }}</td>
        </tr>
      </tbody>
    </table>
  </div>
  <p v-else class="empty-hint">{{ $t('no_results_registered', { domain: domainLabel }) }}</p>
</template>

<script>
export default {
  name: 'ReadOnlyDatoClinicoTable',
  props: {
    entries: { type: Array, default: () => [] },
    domainLabel: { type: String, required: true }
  },
  methods: {
    formatDate(iso) {
      if (!iso) return ''
      try {
        return new Date(iso).toLocaleString()
      } catch (e) {
        return iso
      }
    }
  }
}
</script>

<style scoped>
.results-table-container {
  overflow-x: auto;
}

.results-table td {
  padding: 0.75rem 1rem;
}
</style>
