<template>
  <div class="page-container historia-page">
    <div class="page-header">
      <div class="header-icon">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
          <line x1="16" y1="13" x2="8" y2="13"></line>
          <line x1="16" y1="17" x2="8" y2="17"></line>
          <polyline points="10 9 9 9 8 9"></polyline>
        </svg>
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
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
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
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
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
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
            <path d="M9 2v6"></path>
            <path d="M15 2v6"></path>
            <path d="M12 17v5"></path>
            <path d="M5 8h14"></path>
            <path d="M6 11V8h12v3"></path>
          </svg>
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
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
          </svg>
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
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
            <path d="M9 2v7.31"></path>
            <path d="M14.69 2v7.31"></path>
            <path d="M9 9.31h5.69l4.36 10.15a2 2 0 0 1-1.84 2.79H6.48a2 2 0 0 1-1.84-2.79z"></path>
            <path d="M7 16h10"></path>
          </svg>
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
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
            <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path>
            <polyline points="13 2 13 9 20 9"></polyline>
          </svg>
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
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
            <path d="M4 5h11l5 5v9a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1z"></path>
            <path d="M14 5v6h6"></path>
            <path d="M8 15h8M8 18h5"></path>
          </svg>
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
                <div
                  class="file-drop-zone"
                  :class="{ 'drag-over': isDragging }"
                  role="button"
                  tabindex="0"
                  :aria-label="$t('upload_here')"
                  @dragover="onDragOver"
                  @dragleave="onDragLeave"
                  @drop="onDrop"
                  @keydown.enter.prevent="openFilePicker"
                  @keydown.space.prevent="openFilePicker"
                >
                  <input type="file" id="fileInput" ref="fileInputRef" @change="onFileChange" class="file-input-hidden" />
                  <label for="fileInput" class="file-drop-label" tabindex="-1">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="upload-icon" aria-hidden="true" focusable="false">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                      <polyline points="17 8 12 3 7 8"></polyline>
                      <line x1="12" y1="3" x2="12" y2="15"></line>
                    </svg>
                    <div class="upload-text">
                      <span v-if="!file" class="upload-primary">{{$t('upload_here')}}</span>
                      <span v-else class="file-selected-name">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
                          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                          <polyline points="14 2 14 8 20 8"></polyline>
                        </svg>
                        {{ file.name }}
                      </span>
                      <span class="upload-secondary">{{$t('file_types')}}</span>
                    </div>
                  </label>
                </div>

                <button type="submit" :disabled="!file || uploading" class="btn-primary upload-btn">
                  <svg v-if="!uploading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="17 8 12 3 7 8"></polyline>
                    <line x1="12" y1="3" x2="12" y2="15"></line>
                  </svg>
                  <div v-else class="spinner-small"></div>
                  {{ uploading ? $t('uploading') : $t('upload_file') }}
                </button>
              </form>

              <div v-if="error" class="alert alert-danger" role="alert" aria-live="assertive">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="8" x2="12" y2="12"></line>
                  <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
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
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                      <polyline points="14 2 14 8 20 8"></polyline>
                    </svg>
                  </div>
                  <div class="file-info">
                    <div class="file-name">{{ it.nombreOriginal }}</div>
                    <div class="file-size">{{ formatSize(it.sizeBytes) }}</div>
                  </div>
                  <div class="file-actions">
                    <button class="btn-icon" @click="download(it)" :title="$t('download')" :aria-label="$t('download')">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                        <polyline points="7 10 12 15 17 10"></polyline>
                        <line x1="12" y1="15" x2="12" y2="3"></line>
                      </svg>
                    </button>
                    <button class="btn-icon btn-danger" @click="remove(it)" :disabled="removingId === it.id" :title="$t('delete')" :aria-label="$t('delete')">
                      <svg v-if="removingId !== it.id" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
                        <polyline points="3 6 5 6 21 6"></polyline>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                      </svg>
                      <div v-else class="spinner-small"></div>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="empty-files">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true" focusable="false">
                <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path>
                <polyline points="13 2 13 9 20 9"></polyline>
              </svg>
              <p>{{$t('no_files')}}</p>
              <span>{{$t('upload_first')}}</span>
            </div>
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

export default {
  name: 'HistoriaClinicaView',
  components: { AntecedentesSection, AlergiasSection, AnalisisSangreSection, SignosVitalesSection, AnalisisOrinaSection, AnotacionesMedicasSection },
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
      isDragging: false,
      activeSection: 'antecedentes'
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

    openFilePicker() {
      const input = this.$refs.fileInputRef
      if (input && typeof input.click === 'function') input.click()
    },

    async load() {
      try {
        this.items = await svc.list()
      } catch (e) {
        this.error = this.$t('error_loading_files')
      }
    },

    onFileChange(e) {
      this.file = e.target.files && e.target.files[0] ? e.target.files[0] : null
    },

    onDragOver(e) {
      e.preventDefault()
      e.stopPropagation()
      this.isDragging = true
    },

    onDragLeave(e) {
      e.preventDefault()
      e.stopPropagation()
      this.isDragging = false
    },

    onDrop(e) {
      e.preventDefault()
      e.stopPropagation()
      this.isDragging = false

      const files = e.dataTransfer.files
      if (files && files[0]) {
        this.file = files[0]
      }
    },

    async onUpload() {
      if (!this.file) return
      this.uploading = true
      this.error = null
      try {
        await svc.upload(this.file)
        this.file = null
        await this.load()
      } catch (e) {
        this.error = this.$t('error_uploading_file')
      } finally {
        this.uploading = false
      }
    },

    async remove(it) {
      this.removingId = it.id
      try {
        await svc.remove(it.id)
        this.items = this.items.filter(x => x.id !== it.id)
      } catch (e) {
        this.error = this.$t('error_deleting_file')
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
.btn-icon:focus-visible,
.file-drop-zone:focus-visible {
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

.file-drop-zone {
  border: 2px dashed var(--border);
  border-radius: 12px;
  padding: 3rem 2rem;
  text-align: center;
  transition: all 0.2s ease;
  background: var(--bg-light);
  margin-bottom: 1rem;
}

.file-drop-zone:hover,
.file-drop-zone.drag-over {
  border-color: var(--primary-color);
  background: var(--surface-info-bg);
}

.file-input-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
  border: 0;
}

.file-drop-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
}

.upload-icon {
  color: var(--primary-color);
}

.upload-text {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.upload-primary {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.file-selected-name {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--success-color);
}

.file-selected-name svg {
  color: var(--success-color);
}

.upload-secondary {
  font-size: 0.875rem;
  color: var(--text-secondary);
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

  .file-drop-zone {
    padding: 2rem 1rem;
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
  .file-drop-zone,
  .file-card,
  .fade-slide-enter-active,
  .fade-slide-leave-active {
    transition: none;
  }
}
</style>
