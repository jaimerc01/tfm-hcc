<template>
  <nav v-if="totalPages > 1" class="pagination" :aria-label="ariaLabel">
    <button
      type="button"
      class="btn-secondary btn-sm"
      @click="$emit('prev')"
      :disabled="page === 1"
      :aria-label="$t('pagination_previous')"
    >
      {{ $t('pagination_previous') }}
    </button>
    <span class="pagination-status" aria-live="polite">{{ $t('pagination_page_of', { page, total: totalPages }) }}</span>
    <button
      type="button"
      class="btn-secondary btn-sm"
      @click="$emit('next')"
      :disabled="page === totalPages"
      :aria-label="$t('pagination_next')"
    >
      {{ $t('pagination_next') }}
    </button>
  </nav>
</template>

<script>
export default {
  name: 'AppPagination',
  props: {
    page: { type: Number, required: true },
    totalPages: { type: Number, required: true },
    ariaLabel: { type: String, required: true }
  },
  emits: ['prev', 'next']
}
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}

.pagination-status {
  font-size: 0.875rem;
  color: var(--text-secondary);
}
</style>
