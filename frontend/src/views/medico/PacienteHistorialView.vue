<template>
  <div class="page-container historia-page">
    <div class="page-header">
      <div class="header-icon">
        <AppIcon name="file-text" size="2xl" />
      </div>
      <div>
        <h1>{{ $t('patient_historial_title', { name: nombrePaciente }) }}</h1>
        <p class="subtitle">{{ $t('dni') }}: {{ nif }}</p>
      </div>
      <router-link :to="{ name: 'Medico' }" class="back-link">
        <AppIcon name="arrow-left" size="md" />
        {{ $t('back') }}
      </router-link>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>{{ $t('loading_info') }}</p>
    </div>

    <div v-else-if="error" class="alert alert-danger" role="alert">
      <AppIcon name="alert-circle" size="lg" />
      {{ error }}
    </div>

    <div v-else class="historial-content">
      <AppTabs :tabs="tabs" v-model="activeTab" :aria-label="$t('historial_tabs_aria')" />

      <!-- Antecedentes -->
      <section v-show="activeTab === 'antecedentes'" id="tabpanel-antecedentes" role="tabpanel" aria-labelledby="tab-antecedentes" tabindex="-1" class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('backgrounds') }}</h2>
        </div>
        <div v-if="groupedAntecedentes.length" class="antecedentes-groups">
          <div v-for="group in groupedAntecedentes" :key="group.key" class="section">
            <div class="section-title">
              {{ group.label }}
              <span class="badge badge-info">{{ group.items.length }}</span>
            </div>
            <div class="cards-grid">
              <div v-for="a in group.items" :key="a.id" class="read-card">
                <h3>{{ a.descripcion }}</h3>
                <span v-if="a.createdAt" class="badge badge-date">{{ formatDateTime(a.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
        <p v-else class="empty-hint">{{ $t('no_antecedentes_registered') }}</p>
      </section>

      <!-- Alergias -->
      <section v-show="activeTab === 'alergias'" id="tabpanel-alergias" role="tabpanel" aria-labelledby="tab-alergias" tabindex="-1" class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('allergies') }}</h2>
        </div>
        <div v-if="historial.alergias && historial.alergias.length">
          <div class="cards-grid">
            <div v-for="al in pagedAlergias" :key="al.id" class="read-card allergy">
              <h3>{{ al.descripcion }}</h3>
              <span v-if="al.createdAt" class="badge badge-date">{{ formatDateTime(al.createdAt) }}</span>
            </div>
          </div>
          <AppPagination
            :page="alergiasPage"
            :total-pages="alergiasTotalPages"
            :aria-label="$t('pagination_nav_aria', { domain: $t('allergies') })"
            @prev="goToPrevAlergiasPage"
            @next="goToNextAlergiasPage"
          />
        </div>
        <p v-else class="empty-hint">{{ $t('no_allergies_registered') }}</p>
      </section>

      <!-- Análisis de sangre -->
      <section v-show="activeTab === 'analisis-sangre'" id="tabpanel-analisis-sangre" role="tabpanel" aria-labelledby="tab-analisis-sangre" tabindex="-1" class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('tab_blood_analysis') }}</h2>
        </div>
        <ReadOnlyDatoClinicoTable :entries="historial.analisisSangre" :domain-label="$t('domain_blood_analysis')" />
      </section>

      <!-- Signos vitales -->
      <section v-show="activeTab === 'signos-vitales'" id="tabpanel-signos-vitales" role="tabpanel" aria-labelledby="tab-signos-vitales" tabindex="-1" class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('vital_signs') }}</h2>
        </div>
        <ReadOnlyDatoClinicoTable :entries="historial.signosVitales" :domain-label="$t('domain_vital_signs')" />
      </section>

      <!-- Análisis de orina -->
      <section v-show="activeTab === 'analisis-orina'" id="tabpanel-analisis-orina" role="tabpanel" aria-labelledby="tab-analisis-orina" tabindex="-1" class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('urine_analysis') }}</h2>
        </div>
        <ReadOnlyDatoClinicoTable :entries="historial.analisisOrina" :domain-label="$t('domain_urine_analysis')" />
      </section>

      <!-- Gráficas (CU-15) -->
      <section v-show="activeTab === 'evolucion'" id="tabpanel-evolucion" role="tabpanel" aria-labelledby="tab-evolucion" tabindex="-1" class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('historical_evolution') }}</h2>
          <p class="panel-subtitle">{{ $t('patient_charts_subtitle') }}</p>
        </div>
        <PatientClinicalCharts
          :analisis-sangre="historial.analisisSangre"
          :signos-vitales="historial.signosVitales"
          :analisis-orina="historial.analisisOrina"
        />
      </section>

      <!-- Línea de tiempo clínica anotada -->
      <section v-show="activeTab === 'linea-tiempo'" id="tabpanel-linea-tiempo" role="tabpanel" aria-labelledby="tab-linea-tiempo" tabindex="-1" class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('clinical_timeline_title') }}</h2>
          <p class="panel-subtitle">{{ $t('clinical_timeline_subtitle') }}</p>
        </div>
        <ClinicalTimelineCard
          :analisis-sangre="historial.analisisSangre"
          :signos-vitales="historial.signosVitales"
          :analisis-orina="historial.analisisOrina"
          :antecedentes="historial.antecedentes"
        />
      </section>

      <!-- Documentos (CU-16) -->
      <section v-show="activeTab === 'documentos'" id="tabpanel-documentos" role="tabpanel" aria-labelledby="tab-documentos" tabindex="-1" class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('files') }}</h2>
          <p class="panel-subtitle">{{ $t('patient_files_subtitle') }}</p>
        </div>

        <form class="archivo-form" @submit.prevent="subirArchivo">
          <FileDropZone
            ref="dropZoneRef"
            :model-value="archivoSeleccionado"
            input-id="archivo-input"
            @update:model-value="onArchivoSeleccionado"
          />
          <div class="form-actions form-actions--right">
            <button type="submit" class="btn-primary" :disabled="!archivoSeleccionado || subiendoArchivo">
              <div v-if="subiendoArchivo" class="spinner-small"></div>
              {{ subiendoArchivo ? $t('uploading') : $t('upload_file') }}
            </button>
          </div>
          <div v-if="archivoError" class="alert alert-danger" role="alert">{{ archivoError }}</div>
          <div v-if="archivoSuccess" class="alert alert-success" role="status" aria-live="polite">{{ $t('file_uploaded_success') }}</div>
        </form>

        <div v-if="archivos.length">
          <div class="cards-grid">
            <div v-for="f in pagedArchivos" :key="f.id" class="read-card">
              <h3>{{ f.nombreOriginal }}</h3>
              <span v-if="f.fechaCreacion" class="badge badge-date">{{ formatDateTime(f.fechaCreacion) }}</span>
              <button type="button" class="btn-secondary" @click="descargarArchivo(f)">{{ $t('download') }}</button>
            </div>
          </div>
          <AppPagination
            :page="archivosPage"
            :total-pages="archivosTotalPages"
            :aria-label="$t('pagination_nav_aria', { domain: $t('files') })"
            @prev="goToPrevArchivosPage"
            @next="goToNextArchivosPage"
          />
        </div>
        <p v-else class="empty-hint">{{ $t('no_files') }}</p>
      </section>

      <!-- Anotación médica -->
      <section v-show="activeTab === 'anotacion'" id="tabpanel-anotacion" role="tabpanel" aria-labelledby="tab-anotacion" tabindex="-1" class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('write_annotation') }}</h2>
          <p class="panel-subtitle">{{ $t('write_annotation_subtitle') }}</p>
        </div>

        <form class="anotacion-form" @submit.prevent="enviarAnotacion">
          <div class="form-group">
            <label class="form-label" for="anotacion-mensaje">{{ $t('annotation_message_label') }}</label>
            <textarea
              id="anotacion-mensaje"
              v-model="anotacionMensaje"
              class="form-input"
              rows="4"
              maxlength="2000"
              :placeholder="$t('annotation_message_placeholder')"
              required
            ></textarea>
          </div>

          <div v-if="anotacionError" class="alert alert-danger" role="alert" aria-live="assertive">
            <AppIcon name="alert-circle" size="lg" />
            {{ anotacionError }}
          </div>

          <div v-if="anotacionSuccess" class="alert alert-success" role="status" aria-live="polite">
            <AppIcon name="check" size="lg" />
            {{ $t('annotation_sent_success') }}
          </div>

          <div class="form-actions form-actions--right">
            <button type="submit" class="btn-primary" :disabled="anotacionSaving || !anotacionMensaje.trim()">
              <div v-if="anotacionSaving" class="spinner-small"></div>
              {{ anotacionSaving ? $t('saving') : $t('send_annotation') }}
            </button>
          </div>
        </form>

        <div class="anotaciones-previas">
          <h3 class="anotaciones-previas__title">{{ $t('my_annotations_for_patient') }}</h3>
          <div v-if="anotaciones.length">
            <div class="cards-grid">
              <div v-for="a in pagedAnotaciones" :key="a.id" class="read-card annotation">
                <p class="annotation-text">{{ a.mensaje }}</p>
                <span v-if="a.createdAt" class="badge badge-date">{{ formatDateTime(a.createdAt) }}</span>
              </div>
            </div>
            <AppPagination
              :page="anotacionesPage"
              :total-pages="anotacionesTotalPages"
              :aria-label="$t('pagination_nav_aria', { domain: $t('write_annotation') })"
              @prev="goToPrevAnotacionesPage"
              @next="goToNextAnotacionesPage"
            />
          </div>
          <p v-else class="empty-hint">{{ $t('no_annotations_written') }}</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import medicoPacienteService from '@/services/medicoPacienteService'
import ReadOnlyDatoClinicoTable from '@/components/ReadOnlyDatoClinicoTable.vue'
import PatientClinicalCharts from '@/components/PatientClinicalCharts.vue'
import ClinicalTimelineCard from '@/components/ClinicalTimelineCard.vue'
import AppIcon from '@/components/AppIcon.vue'
import AppTabs from '@/components/AppTabs.vue'
import AppPagination from '@/components/AppPagination.vue'
import FileDropZone from '@/components/FileDropZone.vue'

const PAGE_SIZE = 10

export default {
  name: 'PacienteHistorialView',
  components: { ReadOnlyDatoClinicoTable, PatientClinicalCharts, ClinicalTimelineCard, AppIcon, AppTabs, AppPagination, FileDropZone },
  data() {
    return {
      historial: { antecedentes: [], alergias: [], analisisSangre: [], signosVitales: [], analisisOrina: [] },
      loading: true,
      error: null,
      activeTab: 'antecedentes',
      alergiasPage: 1,
      anotacionMensaje: '',
      anotacionSaving: false,
      anotacionError: null,
      anotacionSuccess: false,
      anotaciones: [],
      anotacionesPage: 1,
      archivos: [],
      archivosPage: 1,
      archivoSeleccionado: null,
      subiendoArchivo: false,
      archivoError: null,
      archivoSuccess: false
    }
  },
  computed: {
    nif() {
      return this.$route.params.nif
    },
    nombrePaciente() {
      return this.$route.query.nombre || this.nif
    },
    tabs() {
      return [
        { id: 'antecedentes', label: this.$t('backgrounds') },
        { id: 'alergias', label: this.$t('allergies') },
        { id: 'analisis-sangre', label: this.$t('tab_blood_analysis') },
        { id: 'signos-vitales', label: this.$t('vital_signs') },
        { id: 'analisis-orina', label: this.$t('urine_analysis') },
        { id: 'evolucion', label: this.$t('historical_evolution') },
        { id: 'linea-tiempo', label: this.$t('clinical_timeline') },
        { id: 'documentos', label: this.$t('files') },
        { id: 'anotacion', label: this.$t('write_annotation') }
      ]
    },
    groupedAntecedentes() {
      const antecedentes = this.historial.antecedentes || []
      const personal = antecedentes.filter(a => a.categoria === 'PERSONAL')
      const familiar = antecedentes.filter(a => a.categoria === 'FAMILIAR')
      const groups = []
      if (personal.length) groups.push({ key: 'PERSONAL', label: this.$t('category_personal'), items: personal })
      if (familiar.length) groups.push({ key: 'FAMILIAR', label: this.$t('category_familiar'), items: familiar })
      return groups
    },
    alergiasTotalPages() {
      return Math.max(1, Math.ceil((this.historial.alergias || []).length / PAGE_SIZE))
    },
    pagedAlergias() {
      const start = (this.alergiasPage - 1) * PAGE_SIZE
      return (this.historial.alergias || []).slice(start, start + PAGE_SIZE)
    },
    archivosTotalPages() {
      return Math.max(1, Math.ceil(this.archivos.length / PAGE_SIZE))
    },
    pagedArchivos() {
      const start = (this.archivosPage - 1) * PAGE_SIZE
      return this.archivos.slice(start, start + PAGE_SIZE)
    },
    anotacionesTotalPages() {
      return Math.max(1, Math.ceil(this.anotaciones.length / PAGE_SIZE))
    },
    pagedAnotaciones() {
      const start = (this.anotacionesPage - 1) * PAGE_SIZE
      return this.anotaciones.slice(start, start + PAGE_SIZE)
    }
  },
  created() {
    this.load()
    this.cargarArchivos()
    this.cargarAnotaciones()
  },
  methods: {
    async load() {
      this.loading = true
      this.error = null
      try {
        const { data } = await medicoPacienteService.obtenerHistorialPaciente(this.nif)
        this.historial = data || {}
        this.alergiasPage = 1
      } catch (e) {
        this.error = this.$t('error_loading_patient_historial')
      } finally {
        this.loading = false
      }
    },
    async cargarArchivos() {
      try {
        const { data } = await medicoPacienteService.listarArchivosPaciente(this.nif)
        this.archivos = Array.isArray(data) ? data : []
        this.archivosPage = 1
      } catch (e) {
        this.archivos = []
      }
    },
    async cargarAnotaciones() {
      try {
        const { data } = await medicoPacienteService.listarAnotacionesPaciente(this.nif)
        this.anotaciones = Array.isArray(data) ? data : []
        this.anotacionesPage = 1
      } catch (e) {
        this.anotaciones = []
      }
    },
    goToPrevAlergiasPage() {
      if (this.alergiasPage > 1) this.alergiasPage -= 1
    },
    goToNextAlergiasPage() {
      if (this.alergiasPage < this.alergiasTotalPages) this.alergiasPage += 1
    },
    goToPrevArchivosPage() {
      if (this.archivosPage > 1) this.archivosPage -= 1
    },
    goToNextArchivosPage() {
      if (this.archivosPage < this.archivosTotalPages) this.archivosPage += 1
    },
    goToPrevAnotacionesPage() {
      if (this.anotacionesPage > 1) this.anotacionesPage -= 1
    },
    goToNextAnotacionesPage() {
      if (this.anotacionesPage < this.anotacionesTotalPages) this.anotacionesPage += 1
    },
    onArchivoSeleccionado(file) {
      this.archivoSeleccionado = file
      this.archivoError = null
      this.archivoSuccess = false
    },
    async subirArchivo() {
      if (!this.archivoSeleccionado) return
      this.subiendoArchivo = true
      this.archivoError = null
      this.archivoSuccess = false
      try {
        await medicoPacienteService.subirArchivoPaciente(this.nif, this.archivoSeleccionado)
        this.archivoSeleccionado = null
        this.$refs.dropZoneRef?.reset()
        this.archivoSuccess = true
        await this.cargarArchivos()
      } catch (e) {
        const status = e?.response?.status
        this.archivoError = status === 403
          ? this.$t('error_annotation_forbidden')
          : this.$t('error_uploading_file')
      } finally {
        this.subiendoArchivo = false
      }
    },
    async descargarArchivo(f) {
      try {
        const { data, headers } = await medicoPacienteService.descargarArchivoPaciente(this.nif, f.id)
        const blob = new Blob([data], { type: headers['content-type'] || 'application/octet-stream' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = f.nombreOriginal || 'documento'
        a.click()
        URL.revokeObjectURL(url)
      } catch (e) {
        this.archivoError = this.$t('error_downloading_file')
      }
    },
    formatDateTime(dt) {
      if (!dt) return ''
      const d = new Date(dt)
      if (isNaN(d)) return dt
      return d.toLocaleString()
    },
    async enviarAnotacion() {
      const mensaje = this.anotacionMensaje.trim()
      if (!mensaje) return
      this.anotacionSaving = true
      this.anotacionError = null
      this.anotacionSuccess = false
      try {
        await medicoPacienteService.crearAnotacion(this.nif, mensaje)
        this.anotacionMensaje = ''
        this.anotacionSuccess = true
        await this.cargarAnotaciones()
      } catch (e) {
        const status = e?.response?.status
        if (status === 403) {
          this.anotacionError = this.$t('error_annotation_forbidden')
        } else if (status === 404) {
          this.anotacionError = this.$t('error_annotation_patient_not_found')
        } else {
          this.anotacionError = this.$t('error_sending_annotation')
        }
      } finally {
        this.anotacionSaving = false
      }
    }
  }
}
</script>

<style scoped>
.page-header h1 {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  margin-left: auto;
  padding: 0.625rem 1rem;
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text-primary);
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 600;
  flex-shrink: 0;
}

.back-link:hover {
  background: var(--bg-light);
}

.historial-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.anotacion-form,
.archivo-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.anotacion-form textarea.form-input {
  resize: vertical;
  min-height: 96px;
}

.form-actions--right {
  display: flex;
  justify-content: flex-end;
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

.panel-header {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border);
}

.panel-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
}

.antecedentes-groups {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.cards-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

@media (min-width: 768px) {
  .cards-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.read-card {
  background: var(--bg-light);
  border: 1px solid var(--border);
  border-left: 4px solid var(--primary-color);
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.read-card.allergy {
  border-left-color: var(--warning-color);
}

.read-card h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  word-break: break-word;
}

.anotaciones-previas {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border);
}

.anotaciones-previas__title {
  margin: 0 0 1rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.annotation-text {
  margin: 0;
  font-size: 0.9375rem;
  color: var(--text-primary);
  white-space: pre-line;
  word-break: break-word;
}

.loading-container {
  text-align: center;
  padding: 3rem;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .page-header {
    flex-wrap: wrap;
  }

  .back-link {
    margin-left: 0;
  }

  .panel-card {
    padding: 1.5rem 1rem;
  }
}
</style>
