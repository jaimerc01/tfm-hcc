<template>
  <div class="page-container historia-page">
    <div class="page-header">
      <div class="header-icon">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
        </svg>
      </div>
      <div>
        <h1>{{ $t('patient_historial_title', { name: nombrePaciente }) }}</h1>
        <p class="subtitle">{{ $t('dni') }}: {{ nif }}</p>
      </div>
      <router-link :to="{ name: 'Medico' }" class="back-link">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
          <line x1="19" y1="12" x2="5" y2="12"></line>
          <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
        {{ $t('back') }}
      </router-link>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>{{ $t('loading_info') }}</p>
    </div>

    <div v-else-if="error" class="alert alert-danger" role="alert">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      {{ error }}
    </div>

    <div v-else class="historial-content">
      <!-- Antecedentes -->
      <section class="panel-card">
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
      <section class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('allergies') }}</h2>
        </div>
        <div v-if="historial.alergias && historial.alergias.length" class="cards-grid">
          <div v-for="al in historial.alergias" :key="al.id" class="read-card allergy">
            <h3>{{ al.descripcion }}</h3>
            <span v-if="al.createdAt" class="badge badge-date">{{ formatDateTime(al.createdAt) }}</span>
          </div>
        </div>
        <p v-else class="empty-hint">{{ $t('no_allergies_registered') }}</p>
      </section>

      <!-- Análisis de sangre -->
      <section class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('analysis') }}</h2>
        </div>
        <ReadOnlyDatoClinicoTable :entries="historial.analisisSangre" :domain-label="$t('domain_blood_analysis')" />
      </section>

      <!-- Signos vitales -->
      <section class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('vital_signs') }}</h2>
        </div>
        <ReadOnlyDatoClinicoTable :entries="historial.signosVitales" :domain-label="$t('domain_vital_signs')" />
      </section>

      <!-- Análisis de orina -->
      <section class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('urine_analysis') }}</h2>
        </div>
        <ReadOnlyDatoClinicoTable :entries="historial.analisisOrina" :domain-label="$t('domain_urine_analysis')" />
      </section>

      <!-- Gráficas (CU-15) -->
      <section class="panel-card">
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

      <!-- Documentos (CU-16) -->
      <section class="panel-card">
        <div class="panel-header">
          <h2>{{ $t('files') }}</h2>
          <p class="panel-subtitle">{{ $t('patient_files_subtitle') }}</p>
        </div>

        <form class="archivo-form" @submit.prevent="subirArchivo">
          <label class="form-label" for="archivo-input">{{ $t('upload_file') }}</label>
          <input id="archivo-input" ref="archivoInput" type="file" class="form-input" @change="onArchivoSeleccionado" />
          <div class="form-actions form-actions--right">
            <button type="submit" class="btn-primary" :disabled="!archivoSeleccionado || subiendoArchivo">
              <div v-if="subiendoArchivo" class="spinner-small"></div>
              {{ subiendoArchivo ? $t('uploading') : $t('upload_file') }}
            </button>
          </div>
          <div v-if="archivoError" class="alert alert-danger" role="alert">{{ archivoError }}</div>
          <div v-if="archivoSuccess" class="alert alert-success" role="status" aria-live="polite">{{ $t('file_uploaded_success') }}</div>
        </form>

        <div v-if="archivos.length" class="cards-grid">
          <div v-for="f in archivos" :key="f.id" class="read-card">
            <h3>{{ f.nombreOriginal }}</h3>
            <span v-if="f.fechaCreacion" class="badge badge-date">{{ formatDateTime(f.fechaCreacion) }}</span>
            <button type="button" class="btn-secondary" @click="descargarArchivo(f)">{{ $t('download') }}</button>
          </div>
        </div>
        <p v-else class="empty-hint">{{ $t('no_files') }}</p>
      </section>

      <!-- Anotación médica -->
      <section class="panel-card">
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
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ anotacionError }}
          </div>

          <div v-if="anotacionSuccess" class="alert alert-success" role="status" aria-live="polite">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" focusable="false">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
            {{ $t('annotation_sent_success') }}
          </div>

          <div class="form-actions form-actions--right">
            <button type="submit" class="btn-primary" :disabled="anotacionSaving || !anotacionMensaje.trim()">
              <div v-if="anotacionSaving" class="spinner-small"></div>
              {{ anotacionSaving ? $t('saving') : $t('send_annotation') }}
            </button>
          </div>
        </form>
      </section>
    </div>
  </div>
</template>

<script>
import medicoPacienteService from '@/services/medicoPacienteService'
import ReadOnlyDatoClinicoTable from '@/components/ReadOnlyDatoClinicoTable.vue'
import PatientClinicalCharts from '@/components/PatientClinicalCharts.vue'

export default {
  name: 'PacienteHistorialView',
  components: { ReadOnlyDatoClinicoTable, PatientClinicalCharts },
  data() {
    return {
      historial: { antecedentes: [], alergias: [], analisisSangre: [], signosVitales: [], analisisOrina: [] },
      loading: true,
      error: null,
      anotacionMensaje: '',
      anotacionSaving: false,
      anotacionError: null,
      anotacionSuccess: false,
      archivos: [],
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
    groupedAntecedentes() {
      const antecedentes = this.historial.antecedentes || []
      const personal = antecedentes.filter(a => a.categoria === 'PERSONAL')
      const familiar = antecedentes.filter(a => a.categoria === 'FAMILIAR')
      const groups = []
      if (personal.length) groups.push({ key: 'PERSONAL', label: this.$t('category_personal'), items: personal })
      if (familiar.length) groups.push({ key: 'FAMILIAR', label: this.$t('category_familiar'), items: familiar })
      return groups
    }
  },
  created() {
    this.load()
    this.cargarArchivos()
  },
  methods: {
    async load() {
      this.loading = true
      this.error = null
      try {
        const { data } = await medicoPacienteService.obtenerHistorialPaciente(this.nif)
        this.historial = data || {}
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
      } catch (e) {
        this.archivos = []
      }
    },
    onArchivoSeleccionado(e) {
      this.archivoSeleccionado = e.target.files && e.target.files[0] ? e.target.files[0] : null
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
        if (this.$refs.archivoInput) this.$refs.archivoInput.value = ''
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

.anotacion-form {
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
