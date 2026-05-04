<template>
  <div class="historia-page">
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
          id="identificacion-tab"
          ref="tabIdentificacion"
          :class="['tab-btn', { active: activeSection === 'identificacion' }]"
          @click="activeSection = 'identificacion'"
          @keydown="onTabKeydown($event, 'identificacion')"
          role="tab"
          :aria-selected="activeSection === 'identificacion'"
          aria-controls="identificacion-panel"
          :tabindex="activeSection === 'identificacion' ? 0 : -1"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
          {{$t('identification')}}
        </button>

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
      </div>

      <transition name="fade-slide" mode="out-in">
        <div
          v-if="activeSection === 'identificacion'"
          key="identificacion"
          id="identificacion-panel"
          class="tab-panel"
          role="tabpanel"
          aria-labelledby="identificacion-tab"
          tabindex="-1"
        >
          <div class="panel-card">
            <div class="panel-header">
              <h2>{{$t('basic_identification')}}</h2>
              <p class="panel-subtitle">{{$t('personal_info')}}</p>
            </div>

            <div class="form-grid">
              <div class="form-group full-width">
                <label for="ident-nombre" class="form-label">{{$t('full_name')}} <span class="required">*</span></label>
                <input id="ident-nombre" type="text" v-model="identNombre" :placeholder="$t('full_name')" class="form-input" required aria-required="true" autocomplete="name" />
              </div>

              <div class="form-group">
                <label for="ident-nif" class="form-label">{{$t('nif_nie')}} <span class="required">*</span></label>
                <input id="ident-nif" type="text" v-model="identNif" :placeholder="$t('nif_nie')" class="form-input" required aria-required="true" autocomplete="off" />
              </div>

              <div class="form-group">
                <label for="ident-fecha" class="form-label">{{$t('birth_date')}}</label>
                <input id="ident-fecha" type="date" v-model="identFechaNacimiento" :placeholder="$t('birth_date')" class="form-input" autocomplete="bday" />
              </div>

              <div class="form-group">
                <label for="ident-telefono" class="form-label">{{$t('phone')}}</label>
                <input id="ident-telefono" type="tel" v-model="identTelefono" :placeholder="$t('phone')" class="form-input" autocomplete="tel" />
              </div>

              <div class="form-group">
                <label for="ident-email" class="form-label">{{$t('email_optional')}}</label>
                <input id="ident-email" type="email" v-model="identEmail" :placeholder="$t('email_optional')" class="form-input" autocomplete="email" />
              </div>
            </div>

            <div class="info-box">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              <span>{{$t('data_secure')}}</span>
            </div>

            <div class="form-actions">
              <button type="button" @click="saveIdentificacion" :disabled="savingIdent" class="btn-primary">
                <svg v-if="!savingIdent" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
                  <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                  <polyline points="17 21 17 13 7 13 7 21"></polyline>
                  <polyline points="7 3 7 8 15 8"></polyline>
                </svg>
                <div v-else class="spinner-small"></div>
                {{ savingIdent ? $t('saving') : $t('save_identification') }}
              </button>
            </div>

            <div v-if="msgIdent" class="alert alert-success" role="status" aria-live="polite">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              {{$t('saved')}}
            </div>
          </div>
        </div>

        <div
          v-else-if="activeSection === 'antecedentes'"
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
      </transition>
    </section>
  </div>
</template>

<script>
import svc from '@/services/archivoClinicoService'
import AntecedentesSection from '@/components/AntecedentesSection.vue'
import AlergiasSection from '@/components/AlergiasSection.vue'
import AnalisisSangreSection from '@/components/AnalisisSangreSection.vue'

export default {
  name: 'HistoriaClinicaView',
  components: { AntecedentesSection, AlergiasSection, AnalisisSangreSection },
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
      tabOrder: ['identificacion', 'antecedentes', 'alergias', 'analisis', 'archivos'],
      items: [],
      file: null,
      uploading: false,
      removingId: null,
      error: null,
      isDragging: false,
      identNombre: '',
      identNif: '',
      identFechaNacimiento: '',
      identTelefono: '',
      identEmail: '',
      savingIdent: false,
      msgIdent: '',
      activeSection: 'identificacion'
    }
  },
  created() {
    this.load()
    this.loadHistoria()
  },
  methods: {
    getTabRefName(section) {
      const map = {
        identificacion: 'tabIdentificacion',
        antecedentes: 'tabAntecedentes',
        alergias: 'tabAlergias',
        analisis: 'tabAnalisis',
        archivos: 'tabArchivos'
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

    async loadHistoria() {
      try {
        const historiaSvc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await historiaSvc.getMine()
        const dto = res.data || {}
        try {
          const id = dto.identificacionJson
            ? (typeof dto.identificacionJson === 'string' ? JSON.parse(dto.identificacionJson) : dto.identificacionJson)
            : {}
          this.identNombre = id.nombre || ''
          this.identNif = id.nif || ''
          this.identFechaNacimiento = id.fechaNacimiento || ''
          this.identTelefono = id.contacto || ''
          this.identEmail = id.email || ''
        } catch (e) {
          // Ignore malformed JSON from backend identification payload.
        }
      } catch (e) {
        console.error(this.$t('error_loading_history'), e)
      }
    },

    async saveIdentificacion() {
      this.savingIdent = true
      try {
        const historiaSvc = await import('@/services/historiaClinicaService').then(m => m.default)
        const payload = {
          nombre: this.identNombre,
          nif: this.identNif,
          fechaNacimiento: this.identFechaNacimiento,
          contacto: this.identTelefono,
          email: this.identEmail
        }
        await historiaSvc.updateIdentificacion(JSON.stringify(payload))
        await this.loadHistoria()
        this.msgIdent = this.$t('saved')
        setTimeout(() => {
          this.msgIdent = ''
        }, 3000)
      } catch (e) {
        this.error = this.$t('error_saving_identification')
      } finally {
        this.savingIdent = false
      }
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
.historia-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid var(--primary-color);
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-hover));
  border-radius: 12px;
  color: var(--text-inverse);
  flex-shrink: 0;
}

.page-header h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
}

.subtitle {
  margin: 0.25rem 0 0 0;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

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
  outline: 3px solid var(--focus-color, #005fcc);
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

.panel-card {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: var(--shadow-md);
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

.panel-subtitle {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

@media (min-width: 768px) {
  .form-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.required {
  color: var(--danger-active);
  font-size: 1.2em;
  font-weight: 800;
}

.form-input {
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--border);
  border-radius: 8px;
  font-size: 0.875rem;
  transition: all 0.2s ease;
  background: var(--card-bg);
}

.form-input:hover {
  border-color: var(--primary-color);
}

.form-input:focus-visible {
  outline: 3px solid var(--focus-color, #005fcc);
  outline-offset: 2px;
  border-color: var(--primary-color);
  box-shadow: var(--focus-ring-info);
}

.info-box {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  background: var(--surface-info-bg);
  border: 1px solid var(--surface-info-border);
  border-radius: 8px;
  margin-bottom: 2rem;
}

.info-box svg {
  color: var(--primary-color);
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.info-box span {
  font-size: 0.875rem;
  color: var(--surface-info-text);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.875rem 1.5rem;
  background: var(--primary-color);
  color: var(--text-inverse);
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: var(--shadow-sm);
}

.btn-primary:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.btn-primary:focus-visible,
.btn-icon:focus-visible,
.file-drop-zone:focus-visible {
  outline: 3px solid var(--focus-color, #005fcc);
  outline-offset: 2px;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.alert-success {
  background: var(--alert-success-bg);
  color: var(--alert-success-text);
  border: 1px solid var(--alert-success-border);
}

.alert-danger {
  background: var(--alert-danger-bg);
  color: var(--alert-danger-text);
  border: 1px solid var(--alert-danger-border);
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

.btn-icon:hover:not(:disabled) {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: var(--text-inverse);
}

.btn-icon.btn-danger:hover:not(:disabled) {
  background: var(--danger-color);
  border-color: var(--danger-color);
  color: var(--text-inverse);
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

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .header-icon {
    width: 48px;
    height: 48px;
  }

  .page-header h1 {
    font-size: 1.5rem;
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
  .fade-slide-leave-active,
  .form-input {
    transition: none;
  }
}
</style>
