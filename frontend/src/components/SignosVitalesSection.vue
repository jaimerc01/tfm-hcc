<template>
  <section class="analisis-section" :aria-busy="saving ? 'true' : 'false'" aria-labelledby="signos-vitales-heading">
    <h2 id="signos-vitales-heading" class="sr-only">{{ $t('vital_signs') }}</h2>
    <div class="info-box">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      <span>
        <strong>{{ $t('register_vital_signs') }}</strong> {{ $t('track_health') }}
        {{ $t('select_param_value_date') }}
      </span>
    </div>

    <DatoClinicoChart :entries="entries" :analytes="analytes" :combos="chartCombos" />

    <DatoClinicoForm ref="addForm" :analytes="analytes" :saving="saving" :domain-label="$t('domain_vital_signs')" @submit="handleFormSubmit" />

    <DatoClinicoResultsTable
      :entries="entries"
      :analytes="analytes"
      :saving="saving"
      :domain-label="$t('domain_vital_signs')"
      @delete-entry="removeEntry"
      @clear-all="clearAll"
    />

    <div v-if="msg" class="alert alert-success" role="status" aria-live="polite">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12"></polyline>
      </svg>
      {{ msg }}
    </div>

    <div v-if="error" class="alert alert-danger" role="alert" aria-live="assertive">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      {{ error }}
    </div>

    <div :class="['toast-notification', showToast ? 'show' : '']" role="status" aria-live="polite" aria-atomic="true">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12"></polyline>
      </svg>
      {{ showToastMessage }}
    </div>

    <!-- Delete Confirmation Modal -->
    <AppModal v-if="showDeleteModal" :label="$t('confirm_delete')" @close="closeDeleteModal">
      <template #header>
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--danger-color)">
          <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
          <line x1="12" y1="9" x2="12" y2="13"></line>
          <line x1="12" y1="17" x2="12.01" y2="17"></line>
        </svg>
        <h3>{{ $t('confirm_delete') }}</h3>
      </template>
      <p class="modal-text">{{ $t('delete_result_confirm') }}</p>
      <template #footer>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeDeleteModal">
            {{ $t('cancel') }}
          </button>
          <button type="button" class="btn-danger" @click="confirmDelete">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
            {{ $t('yes_delete') }}
          </button>
        </div>
      </template>
    </AppModal>

    <!-- Clear All Confirmation Modal -->
    <AppModal v-if="showClearAllModal" :label="$t('confirm_bulk_delete')" @close="closeClearAllModal">
      <template #header>
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--danger-color)">
          <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
          <line x1="12" y1="9" x2="12" y2="13"></line>
          <line x1="12" y1="17" x2="12.01" y2="17"></line>
        </svg>
        <h3>{{ $t('confirm_bulk_delete') }}</h3>
      </template>
      <p class="modal-text">
        {{ $t('bulk_delete_confirm', { count: entries.length }) }}
      </p>
      <template #footer>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeClearAllModal">
            {{ $t('cancel') }}
          </button>
          <button type="button" class="btn-danger" @click="confirmClearAll" :disabled="saving">
            <div v-if="saving" class="spinner-small"></div>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
            {{ saving ? $t('deleting') : $t('yes_delete_all') }}
          </button>
        </div>
      </template>
    </AppModal>
  </section>
</template>

<script>
import AppModal from './Modal.vue'
import DatoClinicoChart from './DatoClinicoChart.vue'
import DatoClinicoForm from './DatoClinicoForm.vue'
import DatoClinicoResultsTable from './DatoClinicoResultsTable.vue'
import { useSignosVitales } from '@/composables/useSignosVitales'

const SIGNOS_VITALES_UI = Object.freeze({
  MESSAGE_CLEAR_MS: 3000,
  TOAST_MS: 2000
})

export default {
  name: 'SignosVitalesSection',
  components: { AppModal, DatoClinicoChart, DatoClinicoForm, DatoClinicoResultsTable },
  setup() {
    const { analytes, entries, saving, load, loadRangos, addEntry, deleteEntryById, clearAllEntries } = useSignosVitales()
    return { analytes, entries, saving, load, loadRangos, addEntry, deleteEntryById, clearAllEntries }
  },
  data() {
    return {
      msg: '',
      error: null,
      showToast: false,
      showToastMessage: '',
      showDeleteModal: false,
      deleteIndex: null,
      showClearAllModal: false
    }
  },
  computed: {
    chartCombos() {
      return [
        { id: 'combo-presion-arterial', labelKey: 'combo_blood_pressure', type: 'dual-line', analyteKeys: ['Presion Arterial Sistolica', 'Presion Arterial Diastolica'] }
      ]
    }
  },
  async created() {
    this.showToastMessage = this.$t('analysis_toast_saved')
    try {
      await this.loadRangos()
    } catch (e) {
      console.error(this.$t('analysis_error_loading_ranges'), e)
      // No bloqueamos la carga del componente si fallan los rangos
    }
    try {
      await this.load()
    } catch (e) {
      console.error(this.$t('analysis_error_loading'), e)
      this.error = this.$t('analysis_error_loading')
    }
  },
  methods: {
    async handleFormSubmit(entryPayload) {
      try {
        await this.addEntry(entryPayload)
        this.$refs.addForm.resetFields()
        this.msg = this.$t('analysis_result_added')
        this.showTemporaryToast(this.$t('analysis_toast_saved'))
        setTimeout(() => this.msg = '', SIGNOS_VITALES_UI.MESSAGE_CLEAR_MS)
      } catch (e) {
        console.error(this.$t('analysis_error_saving'), e)
        this.error = this.$t('analysis_error_saving')
      }
    },

    removeEntry(idx) {
      this.deleteIndex = idx
      this.showDeleteModal = true
    },

    closeDeleteModal() {
      this.showDeleteModal = false
      this.deleteIndex = null
    },

    async confirmDelete() {
      if (this.deleteIndex === null) return

      try {
        const entry = this.entries[this.deleteIndex]
        await this.deleteEntryById(entry && entry.id)
        this.closeDeleteModal()
        this.msg = this.$t('analysis_result_deleted')
        this.showTemporaryToast(this.$t('analysis_toast_deleted'))
        setTimeout(() => this.msg = '', SIGNOS_VITALES_UI.MESSAGE_CLEAR_MS)
      } catch (err) {
        console.error(this.$t('analysis_error_deleting'), err)
        this.error = this.$t('analysis_error_deleting')
        this.closeDeleteModal()
      }
    },

    clearAll() {
      this.showClearAllModal = true
    },

    closeClearAllModal() {
      this.showClearAllModal = false
    },

    async confirmClearAll() {
      try {
        await this.clearAllEntries()
        this.showClearAllModal = false
        this.msg = this.$t('analysis_results_deleted')
        this.showTemporaryToast(this.$t('analysis_toast_deleted'))
        setTimeout(() => this.msg = '', SIGNOS_VITALES_UI.MESSAGE_CLEAR_MS)
      } catch (e) {
        console.error(this.$t('analysis_error_clear_all'), e)
        this.error = `${this.$t('analysis_error_clear_all')}: ${e.message || this.$t('error')}`
        setTimeout(() => this.error = '', 5000)
      }
    },

    showTemporaryToast(message) {
      this.showToastMessage = message || this.$t('analysis_toast_saved')
      this.showToast = true
      setTimeout(() => this.showToast = false, SIGNOS_VITALES_UI.TOAST_MS)
    }
  }
}
</script>

<style scoped>
.analisis-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.modal button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

</style>
