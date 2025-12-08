<template>
  <div class="analisis-section" role="tabpanel">
    <div class="info-box">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 16v-4"></path>
        <path d="M12 8h.01"></path>
      </svg>
      <span>
        <strong>Registra tus análisis de sangre</strong> para hacer seguimiento de tu salud.
        Selecciona el parámetro, introduce el valor y la fecha, y pulsa "Añadir resultado".
      </span>
    </div>

    <!-- Chart Section -->
    <div class="chart-section">
      <div class="chart-header">
        <h4>
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="20" x2="18" y2="10"></line>
            <line x1="12" y1="20" x2="12" y2="4"></line>
            <line x1="6" y1="20" x2="6" y2="14"></line>
          </svg>
          Evolución Histórica
        </h4>
        <div class="chart-controls">
          <div class="control-group">
            <label class="form-label" for="chart-type-select">Tipo de gráfico:</label>
            <select id="chart-type-select" v-model="chartType" @change="drawChart" class="form-input chart-select">
              <option value="line">Línea simple</option>
              <option value="interactive">Línea interactiva (zoom)</option>
              <option value="bar">Barras</option>
              <option value="gauge">Medidor actual</option>
            </select>
          </div>
          <div class="control-group">
            <label class="form-label" for="chart-param-select">Parámetro:</label>
            <select id="chart-param-select" v-model="chartParam" @change="drawChart" class="form-input chart-select">
              <option v-for="a in analytes" :key="a.key" :value="a.key">{{ a.label }}</option>
            </select>
          </div>
        </div>
      </div>
      <div ref="chart" class="chart-container" aria-hidden="false"></div>
      
      <!-- Chart info helper -->
      <div class="chart-info" v-if="chartType">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"></circle>
          <path d="M12 16v-4"></path>
          <path d="M12 8h.01"></path>
        </svg>
        <span>{{ getChartTypeInfo() }}</span>
      </div>
    </div>

    <!-- Add Entry Form -->
    <div class="add-entry-section">
      <h4 class="section-subtitle">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 5v14"></path>
          <path d="M5 12h14"></path>
        </svg>
        Añadir Nuevo Resultado
      </h4>

      <div class="form-grid-analisis">
        <div class="form-group">
          <label class="form-label" for="param-select">Parámetro <span class="required">*</span></label>
          <select id="param-select" v-model="selected" class="form-input">
            <option v-for="a in analytes" :key="a.key" :value="a.key">{{ a.label }}</option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label" for="value-input">Valor <span class="required">*</span></label>
          <div class="input-with-unit">
            <input 
              id="value-input"
              class="form-input" 
              :class="{'input-error': !validation.isValid && String(value).trim() !== ''}" 
              type="text" 
              v-model="value" 
              placeholder="Ej: 95" 
              :aria-describedby="!validation.isValid && String(value).trim() !== '' ? 'validation-error' : 'range-hint'" />
            <span class="unit-badge">{{ unitForSelected }}</span>
          </div>
          <div v-if="!validation.isValid && String(value).trim() !== ''" id="validation-error" class="form-error">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ validation.message }}
          </div>
          <div v-else id="range-hint" class="form-help">{{ rangeHintText }}</div>
        </div>

        <div class="form-group">
          <label class="form-label" for="date-input">Fecha y Hora <span class="required">*</span></label>
          <input id="date-input" type="datetime-local" v-model="inputDate" class="form-input" />
          <p class="form-help">Fecha y hora del análisis</p>
        </div>

        <div class="form-group form-actions-inline">
          <label class="form-label">&nbsp;</label>
          <button 
            type="button"
            class="btn-primary btn-add"
            @click="addEntry" 
            :disabled="!canAdd || saving"
            :aria-label="saving ? 'Guardando análisis de sangre...' : 'Añadir análisis de sangre a la historia clínica'"
            :aria-describedby="!validation.isValid ? 'validation-error' : 'range-hint'">
            <div v-if="saving" class="spinner-small"></div>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 5v14"></path>
              <path d="M5 12h14"></path>
            </svg>
            {{ saving ? 'Guardando...' : 'Añadir resultado' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Results List -->
    <div v-if="entries.length" class="section results-section">
      <div class="section-title">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 11H3v2h6m-6-5h6m-6 8h6m4-7h8m-8-3h8m-8 6h8m-8 3h8"></path>
        </svg>
        Resultados Registrados
        <span class="badge badge-info">{{ entries.length }}</span>
      </div>

      <div class="results-table-container">
        <table class="results-table">
          <thead>
            <tr>
              <th>Parámetro</th>
              <th>Valor</th>
              <th>Fecha</th>
              <th class="actions-col">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(e, idx) in entries" :key="idx">
              <td>
                <div class="param-cell">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="1"></circle>
                    <circle cx="12" cy="5" r="1"></circle>
                    <circle cx="12" cy="19" r="1"></circle>
                  </svg>
                  <strong>{{ e.label }}</strong>
                </div>
              </td>
              <td>
                <span class="value-cell">{{ e.value }} <span class="unit-small">{{ e.unit }}</span></span>
              </td>
              <td class="date-cell">{{ formatDate(e.createdAt) }}</td>
              <td class="actions-col">
                <button 
                  type="button"
                  @click="removeEntry(idx)"
                  :aria-label="`Eliminar resultado de ${e.label} del ${formatDate(e.createdAt)}`"
                  class="btn-icon btn-danger">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                  </svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="form-actions">
        <button 
          type="button"
          @click="clearAll" 
          :disabled="saving"
          :aria-label="`Borrar todos los ${entries.length} resultados de análisis de sangre`"
          class="btn-danger">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          </svg>
          Borrar todos los resultados
        </button>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else class="empty-state-small">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
        <polyline points="14 2 14 8 20 8"></polyline>
        <line x1="16" y1="13" x2="8" y2="13"></line>
        <line x1="16" y1="17" x2="8" y2="17"></line>
        <polyline points="10 9 9 9 8 9"></polyline>
      </svg>
      <p>No hay resultados de análisis registrados</p>
      <span>Añade tu primer resultado usando el formulario de arriba</span>
    </div>

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

    <div :class="['toast-notification', showToast ? 'show' : '']">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12"></polyline>
      </svg>
      {{ showToastMessage }}
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" role="dialog" aria-modal="true" @click.self="closeDeleteModal">
      <div class="modal">
        <div class="modal-header">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--danger-color)">
            <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
          <h3>Confirmar eliminación</h3>
        </div>
        <p class="modal-text">¿Estás seguro de que quieres eliminar este resultado? Esta acción no se puede deshacer.</p>
        
        <div class="modal-actions">
          <button 
            type="button"
            class="btn-secondary"
            @click="closeDeleteModal">
            Cancelar
          </button>
          <button 
            type="button"
            class="btn-danger"
            @click="confirmDelete">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
            Sí, eliminar
          </button>
        </div>
      </div>
    </div>

    <!-- Clear All Confirmation Modal -->
    <div v-if="showClearAllModal" class="modal-overlay" role="dialog" aria-modal="true" @click.self="closeClearAllModal">
      <div class="modal">
        <div class="modal-header">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--danger-color)">
            <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
          <h3>Confirmar eliminación masiva</h3>
        </div>
        <p class="modal-text">
          ¿Estás seguro de que quieres eliminar <strong>todos los {{ entries.length }} resultados</strong>? 
          Esta acción no se puede deshacer y se eliminarán permanentemente todos los análisis de sangre registrados.
        </p>
        
        <div class="modal-actions">
          <button 
            type="button"
            class="btn-secondary"
            @click="closeClearAllModal">
            Cancelar
          </button>
          <button 
            type="button"
            class="btn-danger"
            @click="confirmClearAll"
            :disabled="saving">
            <div v-if="saving" class="spinner-small"></div>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
            {{ saving ? 'Eliminando...' : 'Sí, eliminar todo' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useChart } from '@/composables/useChart'

export default {
  name: 'AnalisisSangreSection',
  setup() {
    const { 
      drawTimeSeriesChart, 
      drawInteractiveTimeSeriesChart,
      drawBarChart,
      drawGaugeChart,
      prepareChartData 
    } = useChart()
    return { 
      drawTimeSeriesChart,
      drawInteractiveTimeSeriesChart,
      drawBarChart,
      drawGaugeChart,
      prepareChartData 
    }
  },
  data() {
    return {
      chartType: 'interactive', // Tipo de gráfico por defecto
      analytes: [
        { 
          key: 'glucosa', 
          label: 'Glucosa', 
          unit: 'mg/dL', 
          // Rangos de validación del formulario (amplios)
          validationMin: 0, 
          validationMax: 1000, 
          decimals: 0,
          // Rangos recomendados (se actualizarán desde servidor)
          recommendedMin: null,
          recommendedMax: null
        },
        { 
          key: 'hemoglobina', 
          label: 'Hemoglobina', 
          unit: 'g/dL', 
          validationMin: 0, 
          validationMax: 25, 
          decimals: 1,
          recommendedMin: null,
          recommendedMax: null
        },
        { 
          key: 'colesterol', 
          label: 'Colesterol total', 
          unit: 'mg/dL', 
          validationMin: 0, 
          validationMax: 500, // Aumentado para permitir valores altos como 250-300
          decimals: 0,
          recommendedMin: null,
          recommendedMax: null
        },
        { 
          key: 'trigliceridos', 
          label: 'Triglicéridos', 
          unit: 'mg/dL', 
          validationMin: 0, 
          validationMax: 2000, 
          decimals: 0,
          recommendedMin: null,
          recommendedMax: null
        },
        { 
          key: 'creatinina', 
          label: 'Creatinina', 
          unit: 'mg/dL', 
          validationMin: 0, 
          validationMax: 50, 
          decimals: 2,
          recommendedMin: null,
          recommendedMax: null
        },
        { 
          key: 'hematocrito', 
          label: 'Hematocrito', 
          unit: '%', 
          validationMin: 0, 
          validationMax: 100, 
          decimals: 1,
          recommendedMin: null,
          recommendedMax: null
        }
      ],
  selected: 'glucosa',
  value: '',
  // datetime-local input value (local time, e.g. '2025-08-28T15:30')
  inputDate: '',
      entries: [],
  chartParam: 'glucosa',
      saving: false,
      msg: '',
      error: null,
      showToast: false,
      showToastMessage: 'Guardado correctamente',
      showDeleteModal: false,
      deleteIndex: null,
      showClearAllModal: false
    }
  },
  async created() { 
    await this.loadRangos()
    await this.load()
    this.inputDate = this.localNowForInput() 
  },
  mounted() {
    // initial draw after component mounted
    this.drawChart()
  },
  unmounted() {
    // Limpiar el contenedor del gráfico al desmontar el componente
    const container = this.$refs.chart
    if (container) {
      container.innerHTML = ''
    }
  },
  computed: {
    unitForSelected() {
      const a = this.analytes.find(x => x.key === this.selected)
      return a ? a.unit : ''
    },
    rangeHintText() {
      const a = this.analytes.find(x => x.key === this.selected)
      if (!a) return ''
      
      // Si hay rangos recomendados del servidor, mostrarlos
      if (a.recommendedMin !== null && a.recommendedMax !== null) {
        return `Rango recomendado: ${a.recommendedMin}–${a.recommendedMax} ${a.unit}`
      }
      
      // Si no hay rangos recomendados, mostrar mensaje genérico
      return `Introduce el valor del análisis en ${a.unit}`
    },
  canAdd() { return this.value !== null && String(this.value).trim() !== '' && this.validation.isValid },
    validation() {
      const a = this.analytes.find(x => x.key === this.selected)
      const raw = String(this.value).trim()
      if (!a) return { isValid: false, message: 'Seleccione un parámetro' }
      if (raw === '') return { isValid: false, message: 'Introduce un valor' }
      // allow comma or dot as decimal separator
      const normalized = raw.replace(',', '.')
      const num = Number(normalized)
      if (Number.isNaN(num)) return { isValid: false, message: 'Valor no numérico' }
      if (a.validationMin != null && num < a.validationMin) return { isValid: false, message: `Valor mínimo ${a.validationMin} ${a.unit}` }
      if (a.validationMax != null && num > a.validationMax) return { isValid: false, message: `Valor máximo ${a.validationMax} ${a.unit}` }
      return { isValid: true, message: '' , value: num }
    }
  },
  methods: {
    /**
     * Carga los rangos de referencia desde el servidor y actualiza los analytes
     */
    async loadRangos() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getRangos()
        const rangos = res.data || []
        
        // Actualizar rangos en cada analyte
        rangos.forEach(rango => {
          // Normalizar nombre del rango para hacer match con los analytes
          const nombreNormalizado = this.normalizarNombre(rango.nombre)
          
          // Buscar el analyte correspondiente
          const analyte = this.analytes.find(a => {
            const labelNormalizado = this.normalizarNombre(a.label)
            const keyNormalizado = this.normalizarNombre(a.key)
            return labelNormalizado === nombreNormalizado || keyNormalizado === nombreNormalizado
          })
          
          if (analyte && rango.valorInferiorNumerico !== null && rango.valorSuperiorNumerico !== null) {
            console.log(`DEBUG Rango recibido para ${rango.nombre}:`, {
              valorInferior: rango.valorInferior,
              valorSuperior: rango.valorSuperior,
              valorInferiorNumerico: rango.valorInferiorNumerico,
              valorSuperiorNumerico: rango.valorSuperiorNumerico
            })
            analyte.recommendedMin = rango.valorInferiorNumerico
            analyte.recommendedMax = rango.valorSuperiorNumerico
            console.log(`Rango cargado para ${analyte.label}: min=${analyte.recommendedMin}, max=${analyte.recommendedMax}`)
          }
        })
      } catch (e) {
        console.error('Error cargando rangos:', e)
        // No bloqueamos la carga del componente si fallan los rangos
      }
    },
    
    /**
     * Normaliza un nombre para hacer comparaciones
     */
    normalizarNombre(nombre) {
      if (!nombre) return ''
      return String(nombre)
        .normalize('NFD')
        .replace(/\p{Diacritic}/gu, '')
        .toLowerCase()
        .replace(/\s+/g, '')
    },
    
    // Map server 'tipo' (e.g. "Glucosa", "Hemoglobina") to a local analyte key.
    // Normalizes accents/case/whitespace and matches against known analytes.
    mapTipoToKey(tipo) {
      if (!tipo) return null
      const norm = this.normalizarNombre(tipo)
      const map = {
        glucosa: 'glucosa',
        hemoglobina: 'hemoglobina',
        'colesteroltotal': 'colesterol',
        colesterol: 'colesterol',
        trigliceridos: 'trigliceridos',
        creatinina: 'creatinina',
        hematocrito: 'hematocrito'
      }
      return map[norm] || norm
    },
    async load() {
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const res = await svc.getMine()
        const dto = res.data || {}
        
        // backend may return a JSON array for analisisSangre; try to parse
        if (dto.analisisSangre) {
          try {
            const parsed = typeof dto.analisisSangre === 'string' ? JSON.parse(dto.analisisSangre) : dto.analisisSangre
            if (Array.isArray(parsed)) {
              // Map server DTO shape { tipo, valor, unidad, createdAt, id, rango } to component shape
              this.entries = parsed.map(p => {
                const mappedKey = p.key || this.mapTipoToKey(p.tipo)
                const analyte = this.analytes.find(x => x.key === mappedKey)
                
                // Actualizar los rangos de los analytes con datos del servidor
                if (p.rango && analyte) {
                  this.updateAnalyteRanges(analyte, p.rango)
                }
                
                return {
                  id: p.id || null,
                  key: mappedKey || p.key || null,
                  label: p.label || p.tipo || p.key || (analyte ? analyte.label : ''),
                  // backend may send 'valor' (string/number) so normalize to string for display
                  value: (p.value !== undefined && p.value !== null) ? String(p.value) : (p.valor !== undefined && p.valor !== null) ? String(p.valor) : '',
                  unit: analyte ? analyte.unit : (p.unit || p.unidad || ''),
                  createdAt: p.createdAt || p.fechaCreacion || null,
                  rango: p.rango || null // Guardar información del rango
                }
              })
            } else {
              // Si no es un array, inicializar como vacío
              this.entries = []
            }
          } catch (ignore) {
            // ignore malformed content and start empty
            this.entries = []
          }
        } else {
          // Si no hay analisisSangre, inicializar vacío
          this.entries = []
        }
        
        // Asegurar que el gráfico se redibuja después de actualizar entries
        await this.$nextTick()
        this.drawChart()
      } catch (e) { 
        console.error('No se pudo cargar análisis', e)
        this.error = 'No se pudo cargar análisis'
        this.entries = []
        await this.$nextTick()
        this.drawChart()
      }
    },

    /**
     * Actualiza los rangos de un analyte con datos del servidor
     */
    updateAnalyteRanges(analyte, rangoData) {
      if (!rangoData) return
      
      // Actualizar rangos recomendados con valores del servidor
      if (rangoData.valorInferiorNumerico !== undefined && rangoData.valorInferiorNumerico !== null) {
        analyte.recommendedMin = rangoData.valorInferiorNumerico
      }
      if (rangoData.valorSuperiorNumerico !== undefined && rangoData.valorSuperiorNumerico !== null) {
        analyte.recommendedMax = rangoData.valorSuperiorNumerico
      }
      
      // Guardar referencia al rango para uso posterior
      analyte.rangoData = rangoData
    },

    async addEntry() {
      if (!this.canAdd) return
      
      this.saving = true
      try {
        const a = this.analytes.find(x => x.key === this.selected)
        // format numeric value according to decimals
        const num = this.validation.value
        const formatted = (a.decimals != null) ? num.toFixed(a.decimals) : String(num)
        // use provided inputDate if valid, otherwise now
        let createdAt = new Date().toISOString()
        try {
          if (this.inputDate && String(this.inputDate).trim() !== '') {
            const dt = new Date(this.inputDate)
            if (!Number.isNaN(dt.getTime())) createdAt = dt.toISOString()
          }
        } catch (e) { /* ignore and use now */ }

        const newEntry = { key: a.key, label: a.label, value: formatted, unit: a.unit, createdAt }
        
        // Añadir solo la nueva entrada al servidor (POST añade sin eliminar existentes)
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        const payload = JSON.stringify([newEntry])
        await svc.añadirAnalisisSangre(payload)
        
        // Reload from server to get the updated data
        await this.load()
        
        // Clear form
        this.value = ''
        this.inputDate = this.localNowForInput()
        
        // Show success message
        this.msg = 'Resultado añadido y guardado.'
        this.showTemporaryToast('Guardado correctamente')
        setTimeout(() => this.msg = '', 3000)
        
      } catch (e) {
        console.error('Error guardando análisis', e)
        this.error = 'Error guardando el resultado'
        // Reload to restore state
        await this.load()
      } finally {
        this.saving = false
      }
    },

    removeEntry(i) {
      this.deleteIndex = i
      this.showDeleteModal = true
    },

    closeDeleteModal() {
      this.showDeleteModal = false
      this.deleteIndex = null
    },

    async confirmDelete() {
      if (this.deleteIndex === null) return
      
      try {
        const i = this.deleteIndex
        const entry = this.entries[i]
        
        // Si el entry tiene ID, usar el endpoint DELETE específico
        if (entry && entry.id) {
          const svc = await import('@/services/historiaClinicaService').then(m => m.default)
          await svc.deleteDatoClinico(entry.id)
        } else {
          // Si no tiene ID (entrada local no guardada aún), solo remover del array
          console.warn('Entry sin ID, solo se eliminará localmente')
        }
        
        // Reload from server to ensure consistency
        await this.load()
        
        // Close modal
        this.closeDeleteModal()
        
        // Show success message
        this.msg = 'Resultado eliminado correctamente.'
        this.showTemporaryToast('Eliminado correctamente')
        setTimeout(() => this.msg = '', 3000)
      } catch (err) {
        console.error('Error eliminando resultado', err)
        this.error = 'No se pudo eliminar el resultado'
        // Reload to restore state
        await this.load()
        this.closeDeleteModal()
      }
    },

    clearAll() {
      // Verificar que hay entradas para eliminar
      if (!this.entries || this.entries.length === 0) {
        this.msg = 'No hay resultados para eliminar.'
        setTimeout(() => this.msg = '', 3000)
        return
      }
      
      // Mostrar modal de confirmación
      this.showClearAllModal = true
    },
    
    closeClearAllModal() {
      this.showClearAllModal = false
    },
    
    async confirmClearAll() {
      
      this.saving = true
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        
        // Eliminar cada entrada individualmente usando su ID
        const entriesToDelete = this.entries.filter(e => e && e.id)
        
        if (entriesToDelete.length === 0) {
          console.warn('No hay entradas con ID para eliminar')
          this.entries = []
          this.drawChart() // Redibujar gráfico vacío
          return
        }
        
        const deletePromises = entriesToDelete.map(e => svc.deleteDatoClinico(e.id))
        await Promise.all(deletePromises)
        
        // Limpiar array local y redibujar gráfico
        this.entries = []
        this.drawChart() // Redibujar gráfico vacío
        
        // Recargar desde servidor para asegurar consistencia
        await this.load()
        
        this.showClearAllModal = false
        this.msg = 'Todos los resultados eliminados correctamente.'
        this.showTemporaryToast('Eliminado correctamente')
        setTimeout(() => this.msg = '', 3000)
      } catch (e) { 
        console.error('Error borrando análisis', e)
        this.error = 'Error borrando análisis: ' + (e.message || 'Error desconocido')
        setTimeout(() => this.error = '', 5000)
        // Recargar para restaurar estado
        await this.load()
      } finally {
        this.saving = false
      }
    },

    showTemporaryToast(message = 'Guardado correctamente') {
      this.showToastMessage = message
      this.showToast = true
      setTimeout(() => this.showToast = false, 2000)
    }
    ,
    localNowForInput() {
      const d = new Date()
      // get local iso without seconds fraction to match input step
      const pad = n => String(n).padStart(2, '0')
      const yyyy = d.getFullYear()
      const mm = pad(d.getMonth() + 1)
      const dd = pad(d.getDate())
      const hh = pad(d.getHours())
      const min = pad(d.getMinutes())
      return `${yyyy}-${mm}-${dd}T${hh}:${min}`
    },
    // D3 chart rendering: time series for glucosa (historical values)
    drawChart() {
      const container = this.$refs.chart
      if (!container || !this.entries) return

      // Preparar datos para el parámetro seleccionado
      const param = this.chartParam || 'glucosa'
      const analyteDef = this.analytes.find(a => a.key === param) || {}
      const label = analyteDef.label || param

      // Opciones comunes para todos los gráficos
      const baseOptions = {
        label,
        color: '#c73333',
        recommendedMin: analyteDef.recommendedMin,
        recommendedMax: analyteDef.recommendedMax,
        ariaLabel: `Histórico de ${label}`
      }

      // Para el gauge, usar el último valor
      if (this.chartType === 'gauge') {
        const latestEntry = this.entries
          .filter(e => e.key === param || (e.label && e.label.toLowerCase().includes(param.toLowerCase())))
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0]
        
        if (!latestEntry) {
          container.innerHTML = '<div class="chart-empty">No hay datos disponibles para esta métrica</div>'
          return
        }

        const value = parseFloat(String(latestEntry.value || latestEntry.valor || '0').replace(',', '.'))
        
        // Determinar rango del gauge basado en el analito
        let gaugeMin = 0
        let gaugeMax = 100
        
        switch(param) {
          case 'glucosa':
            gaugeMax = 300
            break
          case 'hemoglobina':
            gaugeMax = 20
            break
          case 'colesterol':
            gaugeMax = 400
            break
          case 'trigliceridos':
            gaugeMax = 500
            break
          case 'creatinina':
            gaugeMax = 3
            break
          case 'hematocrito':
            gaugeMax = 60
            break
        }

        this.drawGaugeChart(container, value, {
          ...baseOptions,
          min: gaugeMin,
          max: gaugeMax,
          unit: analyteDef.unit || '',
          height: 250
        })
        return
      }

      // Para otros tipos de gráfico, preparar datos temporales
      const chartData = this.prepareChartData(this.entries, param)

      if (chartData.length === 0) {
        container.innerHTML = `<div class="chart-empty">No hay datos históricos de ${label}.</div>`
        return
      }

      // Dibujar según el tipo seleccionado
      switch(this.chartType) {
        case 'line':
          this.drawTimeSeriesChart(container, chartData, baseOptions)
          break
        
        case 'interactive':
          this.drawInteractiveTimeSeriesChart(container, chartData, {
            ...baseOptions,
            height: 400
          })
          break
        
        case 'bar':
          this.drawBarChart(container, chartData, {
            ...baseOptions,
            height: 350
          })
          break
        
        default:
          this.drawTimeSeriesChart(container, chartData, baseOptions)
      }
    },
    formatDate(iso) {
      if (!iso) return ''
      try {
        const d = new Date(iso)
        return d.toLocaleString()
      } catch (e) { return iso }
    },

    getChartTypeInfo() {
      const info = {
        line: 'Gráfico de línea simple para ver la tendencia general.',
        interactive: 'Gráfico interactivo con zoom. Usa el selector inferior para explorar períodos específicos.',
        bar: 'Gráfico de barras para comparar valores entre mediciones.',
        gauge: 'Medidor que muestra el último valor registrado y su posición en el rango normal.'
      }
      return info[this.chartType] || ''
    }
  }
}
</script>

<style scoped>
@import '@/styles/charts.css';

.analisis-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* Controles de gráfico mejorados */
.control-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: nowrap;
}

.control-group .form-label {
  white-space: nowrap;
  margin: 0;
}

/* Información del tipo de gráfico */
.chart-info {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  font-size: 0.875rem;
  color: #0c4a6e;
  line-height: 1.5;
}

.chart-info svg {
  flex-shrink: 0;
  margin-top: 0.125rem;
  color: #0284c7;
}

@media (max-width: 768px) {
  .chart-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .control-group {
    width: 100%;
  }
  
  .control-group .form-label {
    min-width: 120px;
  }
  
  .control-group .chart-select {
    flex: 1;
  }
}

/* Add Entry Section */
.add-entry-section {
  background: white;
  border: 1px solid var(--border, #e5e5e5);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
}

.section-subtitle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
  margin: 0 0 1.5rem 0;
}

.section-subtitle svg {
  color: var(--success-color, #16a34a);
}

.form-grid-analisis {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .form-grid-analisis {
    grid-template-columns: 1fr 1fr 1fr auto;
    align-items: start;
  }
}

.form-actions-inline {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.btn-add {
  width: 100%;
  white-space: nowrap;
}

@media (min-width: 768px) {
  .btn-add {
    width: auto;
    min-width: 180px;
  }
}

/* Input with unit badge */
.input-with-unit {
  position: relative;
  display: flex;
  align-items: center;
}

.input-with-unit input {
  padding-right: 4.5rem;
}

.unit-badge {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 1rem;
  background: var(--bg-light, #fafafa);
  border-left: 1px solid var(--border, #e5e5e5);
  border-radius: 0 8px 8px 0;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--text-secondary, #737373);
  min-width: 60px;
}

.input-error {
  border-color: var(--danger-color, #dc2626) !important;
}

.input-error:focus {
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1) !important;
}

/* Results Section */
.results-section {
  background: white;
  border: 1px solid var(--border, #e5e5e5);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
}

.results-table-container {
  overflow-x: auto;
  margin-bottom: 1rem;
}

.results-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.results-table thead {
  background: var(--bg-light, #fafafa);
  border-bottom: 2px solid var(--border, #e5e5e5);
}

.results-table th {
  padding: 0.75rem 1rem;
  text-align: left;
  font-weight: 600;
  color: var(--text-secondary, #737373);
  font-size: 0.8125rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.results-table tbody tr {
  border-bottom: 1px solid var(--border, #e5e5e5);
  transition: background-color 0.15s ease;
}

.results-table tbody tr:hover {
  background: var(--bg-light, #fafafa);
}

.results-table td {
  padding: 1rem;
  color: var(--text-primary, #262626);
}

.param-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.param-cell svg {
  color: var(--primary-color, #0284c7);
  flex-shrink: 0;
}

.value-cell {
  font-weight: 600;
  color: var(--text-primary, #262626);
}

.unit-small {
  font-weight: 400;
  color: var(--text-secondary, #737373);
  font-size: 0.8125rem;
}

.date-cell {
  white-space: nowrap;
  color: var(--text-secondary, #737373);
  font-size: 0.8125rem;
}

.actions-col {
  text-align: right;
  width: 80px;
}

/* Empty state */
.empty-state-small {
  text-align: center;
  padding: 3rem 2rem;
  background: var(--bg-light, #fafafa);
  border-radius: 12px;
  border: 1px dashed var(--border, #e5e5e5);
}

.empty-state-small svg {
  color: var(--text-secondary, #737373);
  opacity: 0.3;
  margin-bottom: 1rem;
}

.empty-state-small p {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
}

.empty-state-small span {
  font-size: 0.875rem;
  color: var(--text-secondary, #737373);
}

/* Toast notification */
.toast-notification {
  position: fixed;
  right: 2rem;
  bottom: 2rem;
  background: var(--success-color, #16a34a);
  color: white;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 500;
  z-index: 10000;
}

.toast-notification.show {
  opacity: 1;
  transform: translateY(0);
}

.toast-notification svg {
  flex-shrink: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .chart-controls {
    width: 100%;
  }
  
  .chart-select {
    flex: 1;
    min-width: 0;
  }
  
  .results-table {
    font-size: 0.8125rem;
  }
  
  .results-table th,
  .results-table td {
    padding: 0.5rem;
  }
  
  .toast-notification {
    right: 1rem;
    bottom: 1rem;
    left: 1rem;
    width: auto;
  }
}
</style>
