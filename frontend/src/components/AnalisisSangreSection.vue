<template>
  <section class="analisis-section" :aria-busy="saving ? 'true' : 'false'" aria-labelledby="analisis-heading">
    <h2 id="analisis-heading" class="sr-only">{{ $t('analysis') }}</h2>
    <div class="info-box">
      <AppIcon name="info" size="lg" />
      <span>
        <strong>{{ $t('register_blood_analysis') }}</strong> {{ $t('track_health') }}
        {{ $t('select_param_value_date') }}
      </span>
    </div>

    <DatoClinicoChart :entries="entries" :analytes="analytes" :combos="chartCombos" />

    <DatoClinicoForm ref="addForm" :analytes="analytes" :saving="saving" :domain-label="$t('domain_blood_analysis')" @submit="handleFormSubmit" />

    <DatoClinicoResultsTable
      :entries="entries"
      :analytes="analytes"
      :saving="saving"
      :domain-label="$t('domain_blood_analysis')"
      @delete-entry="removeEntry"
      @clear-all="clearAll"
    />

    <div v-if="msg" class="alert alert-success" role="status" aria-live="polite">
      <AppIcon name="check" size="lg" />
      {{ msg }}
    </div>

    <div v-if="error" class="alert alert-danger" role="alert" aria-live="assertive">
      <AppIcon name="alert-circle" size="lg" />
      {{ error }}
    </div>

    <div :class="['toast-notification', showToast ? 'show' : '']" role="status" aria-live="polite" aria-atomic="true">
      <AppIcon name="check" size="lg" />
      {{ showToastMessage }}
    </div>

    <!-- Delete Confirmation Modal -->
    <AppModal v-if="showDeleteModal" :label="$t('confirm_delete')" @close="closeDeleteModal">
      <template #header>
        <AppIcon name="alert-triangle" size="3xl" style="color: var(--danger-color)" />
        <h3>{{ $t('confirm_delete') }}</h3>
      </template>
      <p class="modal-text">{{ $t('delete_result_confirm') }}</p>
      <template #footer>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeDeleteModal">
            {{ $t('cancel') }}
          </button>
          <button type="button" class="btn-danger" @click="confirmDelete">
            <AppIcon name="trash" size="md" />
            {{ $t('yes_delete') }}
          </button>
        </div>
      </template>
    </AppModal>

    <!-- Clear All Confirmation Modal -->
    <AppModal v-if="showClearAllModal" :label="$t('confirm_bulk_delete')" @close="closeClearAllModal">
      <template #header>
        <AppIcon name="alert-triangle" size="3xl" style="color: var(--danger-color)" />
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
            <AppIcon v-else name="trash" size="md" />
            {{ saving ? $t('deleting') : $t('yes_delete_all') }}
          </button>
        </div>
      </template>
    </AppModal>
  </section>
</template>

<script>
import AppModal from './Modal.vue'
import AppIcon from './AppIcon.vue'
import DatoClinicoChart from './DatoClinicoChart.vue'
import DatoClinicoForm from './DatoClinicoForm.vue'
import DatoClinicoResultsTable from './DatoClinicoResultsTable.vue'
import { useAnalisisSangre } from '@/composables/useAnalisisSangre'

const ANALYSIS_UI = Object.freeze({
  MESSAGE_CLEAR_MS: 3000,
  TOAST_MS: 2000
})

export default {
  name: 'AnalisisSangreSection',
  components: { AppModal, AppIcon, DatoClinicoChart, DatoClinicoForm, DatoClinicoResultsTable },
  setup() {
    const { analytes, entries, saving, load, loadRangos, addEntry, deleteEntryById, clearAllEntries } = useAnalisisSangre()
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
        { id: 'combo-colesterol-ldl-hdl', labelKey: 'combo_cholesterol_ldl_hdl', type: 'pie', analyteKeys: ['Colesterol LDL', 'Colesterol HDL'] }
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
        setTimeout(() => this.msg = '', ANALYSIS_UI.MESSAGE_CLEAR_MS)
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
        setTimeout(() => this.msg = '', ANALYSIS_UI.MESSAGE_CLEAR_MS)
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
        setTimeout(() => this.msg = '', ANALYSIS_UI.MESSAGE_CLEAR_MS)
      } catch (e) {
        console.error(this.$t('analysis_error_clear_all'), e)
        this.error = `${this.$t('analysis_error_clear_all')}: ${e.message || this.$t('error')}`
        setTimeout(() => this.error = '', 5000)
      }
    },

    showTemporaryToast(message) {
      this.showToastMessage = message || this.$t('analysis_toast_saved')
      this.showToast = true
      setTimeout(() => this.showToast = false, ANALYSIS_UI.TOAST_MS)
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
