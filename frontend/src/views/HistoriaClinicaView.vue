<template>
  <div class="historia-page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-icon">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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

    <!-- Tabs Navigation -->
    <section class="tabs-section">
      <div class="tabs-nav" role="tablist" aria-label="Secciones de historia clínica">
        <button 
          type="button"
          id="identificacion-tab"
          :class="['tab-btn', {active: activeSection==='identificacion'}]" 
          @click="activeSection='identificacion'" 
          role="tab" 
          :aria-selected="activeSection==='identificacion'"
          :aria-controls="activeSection==='identificacion' ? 'identificacion-panel' : null">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
          {{$t('identification')}}
        </button>
        <button 
          type="button"
          id="antecedentes-tab"
          :class="['tab-btn', {active: activeSection==='antecedentes'}]" 
          @click="activeSection='antecedentes'" 
          role="tab" 
          :aria-selected="activeSection==='antecedentes'"
          :aria-controls="activeSection==='antecedentes' ? 'antecedentes-panel' : null">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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
          :class="['tab-btn', {active: activeSection==='alergias'}]" 
          @click="activeSection='alergias'" 
          role="tab" 
          :aria-selected="activeSection==='alergias'"
          :aria-controls="activeSection==='alergias' ? 'alergias-panel' : null">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          {{$t('allergies')}}
        </button>
        <button 
          type="button"
          id="analisis-tab"
          :class="['tab-btn', {active: activeSection==='analisis'}]" 
          @click="activeSection='analisis'" 
          role="tab" 
          :aria-selected="activeSection==='analisis'"
          :aria-controls="activeSection==='analisis' ? 'analisis-panel' : null">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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
          :class="['tab-btn', {active: activeSection==='archivos'}]" 
          @click="activeSection='archivos'" 
          role="tab" 
          :aria-selected="activeSection==='archivos'"
          :aria-controls="activeSection==='archivos' ? 'archivos-panel' : null">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path>
            <polyline points="13 2 13 9 20 9"></polyline>
          </svg>
          {{$t('files')}}
        </button>
      </div>


      <!-- Tab Panels -->
      <transition name="fade-slide" mode="out-in">
        <!-- Identificación Panel -->
        <div v-if="activeSection==='identificacion'" key="identificacion" id="identificacion-panel" class="tab-panel" role="tabpanel" aria-labelledby="identificacion-tab">
          <div class="panel-card">
            <div class="panel-header">
              <h3>{{$t('basic_identification')}}</h3>
              <p class="panel-subtitle">{{$t('personal_info')}}</p>
            </div>

            <div class="form-grid">
              <div class="form-group full-width">
                <label class="form-label">{{$t('full_name')}} <span class="required">*</span></label>
                <input type="text" v-model="identNombre" :placeholder="$t('full_name')" class="form-input" />
              </div>

              <div class="form-group">
                <label class="form-label">{{$t('nif_nie')}} <span class="required">*</span></label>
                <input type="text" v-model="identNif" :placeholder="$t('nif_nie')" class="form-input" />
              </div>

              <div class="form-group">
                <label class="form-label">{{$t('birth_date')}}</label>
                <input type="date" v-model="identFechaNacimiento" :placeholder="$t('birth_date')" class="form-input" />
              </div>

              <div class="form-group">
                <label class="form-label">{{$t('phone')}}</label>
                <input type="tel" v-model="identTelefono" :placeholder="$t('phone')" class="form-input" />
              </div>

              <div class="form-group">
                <label class="form-label">{{$t('email_optional')}}</label>
                <input type="email" v-model="identEmail" :placeholder="$t('email_optional')" class="form-input" />
              </div>
            </div>

            <div class="info-box">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              <span>{{$t('data_secure')}}</span>
            </div>

            <div class="form-actions">
              <button 
                type="button"
                @click="saveIdentificacion" 
                :disabled="savingIdent"
                class="btn-primary">
                <svg v-if="!savingIdent" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                  <polyline points="17 21 17 13 7 13 7 21"></polyline>
                  <polyline points="7 3 7 8 15 8"></polyline>
                </svg>
                <div v-else class="spinner-small"></div>
                {{ savingIdent ? $t('saving') : $t('save_identification') }}
              </button>
            </div>

              <div v-if="msgIdent" class="alert alert-success">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                {{$t('saved')}}
              </div>
          </div>
        </div>

        <!-- Other Sections -->
        <div v-else-if="activeSection==='antecedentes'" key="antecedentes" id="antecedentes-panel" class="tab-panel" role="tabpanel" aria-labelledby="antecedentes-tab">
          <AntecedentesSection />
        </div>

        <div v-else-if="activeSection==='alergias'" key="alergias" id="alergias-panel" class="tab-panel" role="tabpanel" aria-labelledby="alergias-tab">
          <AlergiasSection />
        </div>

        <div v-else-if="activeSection==='analisis'" key="analisis" id="analisis-panel" class="tab-panel" role="tabpanel" aria-labelledby="analisis-tab">
          <AnalisisSangreSection />
        </div>

        <!-- Archivos Panel -->
        <div v-else-if="activeSection==='archivos'" key="archivos" id="archivos-panel" class="tab-panel" role="tabpanel" aria-labelledby="archivos-tab">
          <div class="panel-card">
            <div class="panel-header">
                <h3>{{$t('files')}}</h3>
                <p class="panel-subtitle">{{$t('manage_medical_info')}}</p>
            </div>

            <!-- Upload Area -->
            <div class="upload-section">
              <form @submit.prevent="onUpload">
                <div 
                  class="file-drop-zone" 
                  :class="{'drag-over': isDragging}"
                  @dragover="onDragOver" 
                  @dragleave="onDragLeave" 
                  @drop="onDrop">
                  <input 
                    type="file" 
                    id="fileInput" 
                    @change="onFileChange" 
                    class="file-input-hidden" />
                  <label for="fileInput" class="file-drop-label">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="upload-icon">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                      <polyline points="17 8 12 3 7 8"></polyline>
                      <line x1="12" y1="3" x2="12" y2="15"></line>
                    </svg>
                    <div class="upload-text">
                      <span v-if="!file" class="upload-primary">{{$t('upload_here')}}</span>
                      <span v-else class="file-selected-name">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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
                  <svg v-if="!uploading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="17 8 12 3 7 8"></polyline>
                    <line x1="12" y1="3" x2="12" y2="15"></line>
                  </svg>
                  <div v-else class="spinner-small"></div>
                  {{ uploading ? $t('uploading') : $t('upload_file') }}
                </button>
              </form>

              <div v-if="error" class="alert alert-danger">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="8" x2="12" y2="12"></line>
                  <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                {{ error }}
              </div>
            </div>

            <!-- Files List -->
            <div v-if="items.length" class="files-list">
              <div class="list-header">
                <h4>{{$t('your_files')}}</h4>
                <span class="file-count">{{ items.length }} {{$t('file_count')}}{{ items.length !== 1 ? 's' : '' }}</span>
              </div>
              
              <div class="files-grid">
                <div v-for="it in items" :key="it.id" class="file-card">
                  <div class="file-icon-wrapper">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                      <polyline points="14 2 14 8 20 8"></polyline>
                    </svg>
                  </div>
                  <div class="file-info">
                    <div class="file-name">{{ it.nombreOriginal }}</div>
                    <div class="file-size">{{ formatSize(it.sizeBytes) }}</div>
                  </div>
                  <div class="file-actions">
                    <button class="btn-icon" @click="download(it)" :title="$t('download')">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                        <polyline points="7 10 12 15 17 10"></polyline>
                        <line x1="12" y1="15" x2="12" y2="3"></line>
                      </svg>
                    </button>
                    <button class="btn-icon btn-danger" @click="remove(it)" :disabled="removingId===it.id" :title="$t('delete')">
                      <svg v-if="removingId !== it.id" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3 6 5 6 21 6"></polyline>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                      </svg>
                      <div v-else class="spinner-small"></div>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Empty State -->
            <div v-else class="empty-files">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
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
  data() {
    return {
      items: [],
      file: null,
      uploading: false,
      removingId: null,
      error: null,
      isDragging: false,
      // Identification fields (user-friendly)
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
    async loadHistoria() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getMine()
        const dto = res.data || {}
        // If backend returns identification JSON, try to parse and populate fields; otherwise leave blank
        try {
          const id = dto.identificacionJson ? (typeof dto.identificacionJson === 'string' ? JSON.parse(dto.identificacionJson) : dto.identificacionJson) : {}
          this.identNombre = id.nombre || ''
          this.identNif = id.nif || ''
          this.identFechaNacimiento = id.fechaNacimiento || ''
          this.identTelefono = id.contacto || ''
          this.identEmail = id.email || ''
        } catch (e) { /* ignore parse errors */ }
        // children components handle antecedentes and alergias UI/state
      } catch (e) { console.error('No se pudo cargar historia', e) }
    },

    async saveIdentificacion() {
      this.savingIdent = true
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const payload = {
          nombre: this.identNombre,
          nif: this.identNif,
          fechaNacimiento: this.identFechaNacimiento,
          contacto: this.identTelefono,
          email: this.identEmail
        }
        await svc.updateIdentificacion(JSON.stringify(payload))
        await this.loadHistoria()
        this.msgIdent = 'Identificación guardada.'
        setTimeout(() => this.msgIdent = '', 3000)
      } catch (e) { this.error = 'Error guardando identificación' } finally { this.savingIdent = false }
    },

    async load() {
      try {
        this.items = await svc.list()
      } catch (e) {
        this.error = 'No se pudieron cargar los archivos'
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
        this.error = 'Error al subir archivo'
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
        this.error = 'No se pudo eliminar'
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
        this.error = 'No se pudo descargar'
      }
    },

    formatSize(bytes) {
      if (!bytes && bytes !== 0) return ''
      const units = ['B','KB','MB','GB']
      let b = Number(bytes), i = 0
      while (b >= 1024 && i < units.length-1) { b /= 1024; i++ }
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

/* Page Header */
.page-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid var(--primary-color, #0284c7);
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--primary-color, #0284c7), var(--primary-hover, #0369a1));
  border-radius: 12px;
  color: white;
  flex-shrink: 0;
}

.page-header h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary, #262626);
}

.subtitle {
  margin: 0.25rem 0 0 0;
  color: var(--text-secondary, #737373);
  font-size: 0.875rem;
}

/* Tabs Navigation */
.tabs-section {
  margin-bottom: 2rem;
}

.tabs-nav {
  display: flex;
  gap: 0.5rem;
  border-bottom: 2px solid var(--border, #e5e5e5);
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
  color: var(--text-secondary, #737373);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.tab-btn:hover {
  color: var(--primary-color, #0284c7);
  background: var(--bg-light, #fafafa);
}

.tab-btn.active {
  color: var(--primary-color, #0284c7);
  border-bottom-color: var(--primary-color, #0284c7);
}

.tab-btn svg {
  flex-shrink: 0;
}

/* Tab Panel */
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

/* Panel Card */
.panel-card {
  background: white;
  border: 1px solid var(--border, #e5e5e5);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
}

.panel-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border, #e5e5e5);
}

.panel-header h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
}

.panel-subtitle {
  margin: 0;
  color: var(--text-secondary, #737373);
  font-size: 0.875rem;
}

/* Form Grid */
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
  color: var(--text-primary, #262626);
}

.required {
  color: var(--danger-color, #dc2626);
}

.form-input {
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--border, #e5e5e5);
  border-radius: 8px;
  font-size: 0.875rem;
  transition: all 0.2s ease;
  background: white;
}

.form-input:hover {
  border-color: var(--primary-color, #0284c7);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color, #0284c7);
  box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.1);
}

/* Info Box */
.info-box {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  margin-bottom: 2rem;
}

.info-box svg {
  color: var(--primary-color, #0284c7);
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.info-box span {
  font-size: 0.875rem;
  color: #1e40af;
}

/* Form Actions */
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
  background: var(--primary-color, #0284c7);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
}

.btn-primary:hover:not(:disabled) {
  background: var(--primary-hover, #0369a1);
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Alerts */
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
  background: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.alert-danger {
  background: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

/* Upload Section */
.upload-section {
  margin-bottom: 2rem;
}

.file-drop-zone {
  border: 2px dashed var(--border, #e5e5e5);
  border-radius: 12px;
  padding: 3rem 2rem;
  text-align: center;
  transition: all 0.2s ease;
  background: var(--bg-light, #fafafa);
  margin-bottom: 1rem;
}

.file-drop-zone:hover,
.file-drop-zone.drag-over {
  border-color: var(--primary-color, #0284c7);
  background: #eff6ff;
}

.file-input-hidden {
  display: none;
}

.file-drop-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
}

.upload-icon {
  color: var(--primary-color, #0284c7);
}

.upload-text {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.upload-primary {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
}

.file-selected-name {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--success-color, #16a34a);
}

.file-selected-name svg {
  color: var(--success-color, #16a34a);
}

.upload-secondary {
  font-size: 0.875rem;
  color: var(--text-secondary, #737373);
}

.upload-btn {
  width: 100%;
  max-width: 300px;
  margin: 0 auto;
  display: flex;
}

/* Files List */
.files-list {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 1px solid var(--border, #e5e5e5);
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
  color: var(--text-primary, #262626);
}

.file-count {
  font-size: 0.875rem;
  color: var(--text-secondary, #737373);
  background: var(--bg-light, #fafafa);
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
  border: 1px solid var(--border, #e5e5e5);
  border-radius: 8px;
  background: white;
  transition: all 0.2s ease;
}

.file-card:hover {
  box-shadow: 0 2px 4px 0 rgb(0 0 0 / 0.1);
  transform: translateY(-1px);
}

.file-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: var(--bg-light, #fafafa);
  border-radius: 8px;
  color: var(--primary-color, #0284c7);
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
  word-break: break-word;
  margin-bottom: 0.25rem;
}

.file-size {
  font-size: 0.75rem;
  color: var(--text-secondary, #737373);
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
  border: 1px solid var(--border, #e5e5e5);
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-secondary, #737373);
}

.btn-icon:hover:not(:disabled) {
  background: var(--primary-color, #0284c7);
  border-color: var(--primary-color, #0284c7);
  color: white;
}

.btn-icon.btn-danger:hover:not(:disabled) {
  background: var(--danger-color, #dc2626);
  border-color: var(--danger-color, #dc2626);
  color: white;
}

.btn-icon:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Empty State */
.empty-files {
  text-align: center;
  padding: 3rem 2rem;
}

.empty-files svg {
  color: var(--text-secondary, #737373);
  opacity: 0.3;
  margin-bottom: 1rem;
}

.empty-files p {
  margin: 0 0 0.5rem 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
}

.empty-files span {
  font-size: 0.875rem;
  color: var(--text-secondary, #737373);
}

/* Responsive */
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

/* Transitions */
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
</style>
