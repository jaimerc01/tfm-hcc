<template>
  <div class="page-container historia-page">
    <div class="page-header">
      <div class="header-icon">
        <AppIcon name="file-text" size="2xl" />
      </div>
      <div>
        <h1>{{$t('clinical_history')}}</h1>
        <p class="subtitle">{{$t('manage_medical_info')}}</p>
      </div>
    </div>

    <section class="tabs-section">
      <div class="tabs-nav" role="tablist" :aria-label="$t('clinical_history')">
        <button
          type="button"
          id="antecedentes-tab"
          ref="tabAntecedentes"
          :class="['tab-btn', { active: activeSection === 'antecedentes' }]"
          @click="activeSection = 'antecedentes'"
          @keydown="onTabKeydown($event, 'antecedentes')"
          role="tab"
          :aria-selected="activeSection === 'antecedentes'"
          aria-controls="antecedentes-panel"
          :tabindex="activeSection === 'antecedentes' ? 0 : -1"
        >
          <AppIcon name="users" size="md" />
          {{$t('backgrounds')}}
        </button>

        <button
          type="button"
          id="alergias-tab"
          ref="tabAlergias"
          :class="['tab-btn', { active: activeSection === 'alergias' }]"
          @click="activeSection = 'alergias'"
          @keydown="onTabKeydown($event, 'alergias')"
          role="tab"
          :aria-selected="activeSection === 'alergias'"
          aria-controls="alergias-panel"
          :tabindex="activeSection === 'alergias' ? 0 : -1"
        >
          <AppIcon name="alert-circle" size="md" />
          {{$t('allergies')}}
        </button>

        <button
          type="button"
          id="analisis-tab"
          ref="tabAnalisis"
          :class="['tab-btn', { active: activeSection === 'analisis' }]"
          @click="activeSection = 'analisis'"
          @keydown="onTabKeydown($event, 'analisis')"
          role="tab"
          :aria-selected="activeSection === 'analisis'"
          aria-controls="analisis-panel"
          :tabindex="activeSection === 'analisis' ? 0 : -1"
        >
          <AppIcon name="test-tube" size="md" />
          {{$t('analysis')}}
        </button>

        <button
          type="button"
          id="signos-vitales-tab"
          ref="tabSignosVitales"
          :class="['tab-btn', { active: activeSection === 'signos-vitales' }]"
          @click="activeSection = 'signos-vitales'"
          @keydown="onTabKeydown($event, 'signos-vitales')"
          role="tab"
          :aria-selected="activeSection === 'signos-vitales'"
          aria-controls="signos-vitales-panel"
          :tabindex="activeSection === 'signos-vitales' ? 0 : -1"
        >
          <AppIcon name="activity" size="md" />
          {{$t('vital_signs')}}
        </button>

        <button
          type="button"
          id="analisis-orina-tab"
          ref="tabAnalisisOrina"
          :class="['tab-btn', { active: activeSection === 'analisis-orina' }]"
          @click="activeSection = 'analisis-orina'"
          @keydown="onTabKeydown($event, 'analisis-orina')"
          role="tab"
          :aria-selected="activeSection === 'analisis-orina'"
          aria-controls="analisis-orina-panel"
          :tabindex="activeSection === 'analisis-orina' ? 0 : -1"
        >
          <AppIcon name="flask" size="md" />
          {{$t('urine_analysis')}}
        </button>

        <button
          type="button"
          id="archivos-tab"
          ref="tabArchivos"
          :class="['tab-btn', { active: activeSection === 'archivos' }]"
          @click="activeSection = 'archivos'"
          @keydown="onTabKeydown($event, 'archivos')"
          role="tab"
          :aria-selected="activeSection === 'archivos'"
          aria-controls="archivos-panel"
          :tabindex="activeSection === 'archivos' ? 0 : -1"
        >
          <AppIcon name="file" size="md" />
          {{$t('files')}}
        </button>

        <button
          type="button"
          id="anotaciones-tab"
          ref="tabAnotaciones"
          :class="['tab-btn', { active: activeSection === 'anotaciones' }]"
          @click="activeSection = 'anotaciones'"
          @keydown="onTabKeydown($event, 'anotaciones')"
          role="tab"
          :aria-selected="activeSection === 'anotaciones'"
          aria-controls="anotaciones-panel"
          :tabindex="activeSection === 'anotaciones' ? 0 : -1"
        >
          <AppIcon name="note" size="md" />
          {{$t('medical_annotations')}}
        </button>
      </div>

      <transition name="fade-slide" mode="out-in">
        <div
          v-if="activeSection === 'antecedentes'"
          key="antecedentes"
          id="antecedentes-panel"
          class="tab-panel"
          role="tabpanel"
          aria-labelledby="antecedentes-tab"
          tabindex="-1"
        >
          <AntecedentesSection />
        </div>

        <div
          v-else-if="activeSection === 'alergias'"
          key="alergias"
          id="alergias-panel"
          class="tab-panel"
          role="tabpanel"
          aria-labelledby="alergias-tab"
          tabindex="-1"
        >
          <AlergiasSection />
        </div>

        <div
          v-else-if="activeSection === 'analisis'"
          key="analisis"
          id="analisis-panel"
          class="tab-panel"
          role="tabpanel"
          aria-labelledby="analisis-tab"
          tabindex="-1"
        >
          <AnalisisSangreSection />
        </div>

        <div
          v-else-if="activeSection === 'signos-vitales'"
          key="signos-vitales"
          id="signos-vitales-panel"
          class="tab-panel"
          role="tabpanel"
          aria-labelledby="signos-vitales-tab"
          tabindex="-1"
        >
          <SignosVitalesSection />
        </div>

        <div
          v-else-if="activeSection === 'analisis-orina'"
          key="analisis-orina"
          id="analisis-orina-panel"
          class="tab-panel"
          role="tabpanel"
          aria-labelledby="analisis-orina-tab"
          tabindex="-1"
        >
          <AnalisisOrinaSection />
        </div>

        <div
          v-else-if="activeSection === 'archivos'"
          key="archivos"
          id="archivos-panel"
          class="tab-panel"
          role="tabpanel"
          aria-labelledby="archivos-tab"
          tabindex="-1"
        >
          <div class="panel-card">
            <div class="panel-header">
              <h2>{{$t('files')}}</h2>
              <p class="panel-subtitle">{{$t('manage_medical_info')}}</p>
            </div>

            <div class="upload-section">
              <form @submit.prevent="onUpload">
                <FileDropZone ref="dropZoneRef" v-model="file" input-id="fileInput" />

                <button type="submit" :disabled="!file || uploading" class="btn-primary upload-btn">
                  <AppIcon v-if="!uploading" name="upload" size="sm" />
                  <div v-else class="spinner-small"></div>
                  {{ uploading ? $t('uploading') : $t('upload_file') }}
                </button>
              </form>

              <div v-if="error" class="alert alert-danger" role="alert" aria-live="assertive">
                <AppIcon name="alert-circle" size="sm" />
                {{ error }}
              </div>
            </div>

            <div v-if="items.length" class="files-list">
              <div class="list-header">
                <h4>{{$t('your_files')}}</h4>
                <span class="file-count">{{ items.length }} {{$t('file_count')}}{{ items.length !== 1 ? 's' : '' }}</span>
              </div>

              <div class="files-grid">
                <div v-for="it in items" :key="it.id" class="file-card">
                  <div class="file-icon-wrapper">
                    <AppIcon name="file" size="xl" />
                  </div>
                  <div class="file-info">
                    <div class="file-name">{{ it.nombreOriginal }}</div>
                    <div class="file-size">{{ formatSize(it.sizeBytes) }}</div>
                  </div>
                  <div class="file-actions">
                    <button class="btn-icon" @click="download(it)" :title="$t('download')" :aria-label="$t('download')">
                      <AppIcon name="download" size="sm" />
                    </button>
                    <button class="btn-icon btn-danger" @click="askDelete(it)" :disabled="removingId === it.id" :title="$t('delete')" :aria-label="$t('delete')">
                      <AppIcon v-if="removingId !== it.id" name="trash" size="sm" />
                      <div v-else class="spinner-small"></div>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="empty-files">
              <AppIcon name="file" size="4xl" :stroke-width="1.5" />
              <p>{{$t('no_files')}}</p>
              <span>{{$t('upload_first')}}</span>
            </div>

            <AppModal v-if="deleteTarget" :label="$t('delete')" @close="cancelDelete">
              <template #header>
                <div class="modal-icon-header">
                  <AppIcon name="trash" size="2xl" class="modal-icon modal-icon--danger" />
                  <h3>{{$t('delete')}}</h3>
                </div>
              </template>
              <p class="modal-confirm-text">{{ $t('confirm_delete_file', { name: deleteTarget.nombreOriginal }) }}</p>
              <template #footer>
                <div class="modal-footer-actions">
                  <button type="button" class="btn-danger" :disabled="removingId === deleteTarget.id" @click="confirmDelete" :aria-label="$t('delete')">
                    <div v-if="removingId === deleteTarget.id" class="spinner-small"></div>
                    {{$t('delete')}}
                  </button>
                  <button type="button" class="btn-secondary" :disabled="removingId === deleteTarget.id" @click="cancelDelete">{{$t('cancel')}}</button>
                </div>
              </template>
            </AppModal>
          </div>
        </div>

        <div
          v-else-if="activeSection === 'anotaciones'"
          key="anotaciones"
          id="anotaciones-panel"
          class="tab-panel"
          role="tabpanel"
          aria-labelledby="anotaciones-tab"
          tabindex="-1"
        >
          <div class="panel-card">
            <div class="panel-header">
              <h2>{{$t('medical_annotations')}}</h2>
              <p class="panel-subtitle">{{$t('medical_annotations_subtitle')}}</p>
            </div>
            <AnotacionesMedicasSection />
          </div>
        </div>
      </transition>
    </section>
  </div>
</template>

<script>
import svc from '@/services/archivoClinicoService'
import AntecedentesSection from '@/components/AntecedentesSection.vue'
import AlergiasSection from '@/components/AlergiasSection.vue'
import AnalisisSangreSection from '@/components/AnalisisSangreSection.vue'
import SignosVitalesSection from '@/components/SignosVitalesSection.vue'
import AnalisisOrinaSection from '@/components/AnalisisOrinaSection.vue'
import AnotacionesMedicasSection from '@/components/AnotacionesMedicasSection.vue'
import AppIcon from '@/components/AppIcon.vue'
import FileDropZone from '@/components/FileDropZone.vue'
import AppModal from '@/components/Modal.vue'

export default {
  name: 'HistoriaClinicaView',
  components: { AntecedentesSection, AlergiasSection, AnalisisSangreSection, SignosVitalesSection, AnalisisOrinaSection, AnotacionesMedicasSection, AppIcon, FileDropZone, AppModal },
  watch: {
    activeSection() {
      this.$nextTick(() => {
        const panel = document.getElementById(`${this.activeSection}-panel`)
        if (panel) panel.focus()
      })
    }
  },
  data() {
    return {
      tabOrder: ['antecedentes', 'alergias', 'analisis', 'signos-vitales', 'analisis-orina', 'archivos', 'anotaciones'],
      items: [],
      file: null,
      uploading: false,
      removingId: null,
      error: null,
      activeSection: 'antecedentes',
      deleteTarget: null
    }
  },
  created() {
    this.load()
  },
  methods: {
    getTabRefName(section) {
      const map = {
        antecedentes: 'tabAntecedentes',
        alergias: 'tabAlergias',
        analisis: 'tabAnalisis',
        'signos-vitales': 'tabSignosVitales',
        'analisis-orina': 'tabAnalisisOrina',
        archivos: 'tabArchivos',
        anotaciones: 'tabAnotaciones'
      }
      return map[section]
    },

    focusTab(section) {
      const refName = this.getTabRefName(section)
      const tab = this.$refs[refName]
      if (tab && typeof tab.focus === 'function') tab.focus()
    },

    onTabKeydown(event, currentSection) {
      const idx = this.tabOrder.indexOf(currentSection)
      if (idx < 0) return

      let targetIdx = idx
      if (event.key === 'ArrowRight') targetIdx = (idx + 1) % this.tabOrder.length
      if (event.key === 'ArrowLeft') targetIdx = (idx - 1 + this.tabOrder.length) % this.tabOrder.length
      if (event.key === 'Home') targetIdx = 0
      if (event.key === 'End') targetIdx = this.tabOrder.length - 1

      if (targetIdx !== idx) {
        event.preventDefault()
        this.activeSection = this.tabOrder[targetIdx]
        this.$nextTick(() => this.focusTab(this.tabOrder[targetIdx]))
      }
    },

    async load() {
      try {
        this.items = await svc.list()
      } catch (e) {
        this.error = this.$t('error_loading_files')
      }
    },

    async onUpload() {
      if (!this.file) return
      this.uploading = true
      this.error = null
      try {
        await svc.upload(this.file)
        this.file = null
        this.$refs.dropZoneRef?.reset()
        await this.load()
      } catch (e) {
        this.error = this.$t('error_uploading_file')
      } finally {
        this.uploading = false
      }
    },

    askDelete(it) {
      this.deleteTarget = it
    },

    cancelDelete() {
      if (this.removingId) return
      this.deleteTarget = null
    },

    async confirmDelete() {
      const it = this.deleteTarget
      if (!it) return
      this.removingId = it.id
      try {
        await svc.remove(it.id)
        this.items = this.items.filter(x => x.id !== it.id)
        this.deleteTarget = null
      } catch (e) {
        this.error = this.$t('error_deleting_file')
        this.deleteTarget = null
      } finally {
        this.removingId = null
      }
    },

    async download(it) {
      try {
        const { blob, filename } = await svc.download(it.id)
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = filename
        a.click()
        URL.revokeObjectURL(url)
      } catch (e) {
        this.error = this.$t('error_downloading_file')
      }
    },

    formatSize(bytes) {
      if (!bytes && bytes !== 0) return ''
      const units = ['B', 'KB', 'MB', 'GB']
      let b = Number(bytes)
      let i = 0
      while (b >= 1024 && i < units.length - 1) {
        b /= 1024
        i++
      }
      return `${b.toFixed(1)} ${units[i]}`
    }
  }
}
</script>

<style scoped>
.tabs-section {
  margin-bottom: 2rem;
}

.tabs-nav {
  display: flex;
  gap: 0.5rem;
  border-bottom: 2px solid var(--border);
  overflow-x: auto;
  padding-bottom: 0;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.875rem 1.25rem;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.tab-btn:hover {
  color: var(--primary-color);
  background: var(--bg-light);
}

.tab-btn:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 2px;
}

.tab-btn.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}

.tab-btn svg {
  flex-shrink: 0;
}

.tab-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.panel-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border);
}

.panel-header h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
}

/* Layout only -- colour, hover and disabled come from the shared button
   system in styles/shared.css (every button in the app is green). */
.btn-primary {
  padding: 0.875rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
}

.btn-primary:focus-visible,
.btn-icon:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 2px;
}

.spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid var(--white-alpha-30);
  border-top-color: var(--text-inverse);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.alert {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.alert svg {
  flex-shrink: 0;
}

.upload-section {
  margin-bottom: 2rem;
}

.upload-section form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.upload-btn {
  width: 100%;
  max-width: 300px;
  margin: 0 auto;
  display: flex;
}

.files-list {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 1px solid var(--border);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.list-header h4 {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
}

.file-count {
  font-size: 0.875rem;
  color: var(--text-secondary);
  background: var(--bg-light);
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
}

.files-grid {
  display: grid;
  gap: 1rem;
}

.file-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--card-bg);
  transition: all 0.2s ease;
}

.file-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.file-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: var(--bg-light);
  border-radius: 8px;
  color: var(--primary-color);
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  word-break: break-word;
  margin-bottom: 0.25rem;
}

.file-size {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.file-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid var(--border);
  background: var(--card-bg);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-secondary);
}

.btn-icon:hover:not(:disabled),
.btn-icon.btn-danger:hover:not(:disabled) {
  background: var(--button-color);
  border-color: var(--button-color);
  color: var(--on-button);
}

.btn-icon:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-files {
  text-align: center;
  padding: 3rem 2rem;
}

.empty-files svg {
  color: var(--text-secondary);
  opacity: 0.3;
  margin-bottom: 1rem;
}

.empty-files p {
  margin: 0 0 0.5rem 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-files span {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

/* Confirmación de borrado (mismo patrón que AdminMedicosView) */
.modal-icon-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
}

.modal-icon-header h3 {
  margin: 0;
  text-align: center;
}

.modal-icon--danger {
  color: var(--danger-color);
}

.modal-confirm-text {
  text-align: center;
  color: var(--text-secondary);
}

.modal-footer-actions {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
}

@media (max-width: 768px) {
  .historia-page {
    padding: 1.5rem 1rem;
  }

  .panel-card {
    padding: 1.5rem 1rem;
  }

  .tabs-nav {
    gap: 0.25rem;
  }

  .tab-btn {
    padding: 0.75rem 1rem;
    font-size: 0.8125rem;
  }

  .file-card {
    flex-wrap: wrap;
  }

  .file-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (prefers-reduced-motion: reduce) {
  .spinner-small,
  .tab-panel {
    animation: none;
  }

  .tab-btn,
  .btn-primary,
  .btn-icon,
  .file-card,
  .fade-slide-enter-active,
  .fade-slide-leave-active {
    transition: none;
  }
}
</style>
