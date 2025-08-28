<template>
  <div class="section-panel card" role="tabpanel">
    <h3>Análisis de sangre</h3>
    <p class="hint">Añade resultados individuales de tu analítica: selecciona el parámetro, escribe el valor y pulsa "Añadir".</p>

  <!-- Selector para elegir parámetro a visualizar -->
  <div class="chart-controls">
    <label class="form-label">Mostrar parámetro</label>
    <select v-model="chartParam" @change="drawChart">
      <option v-for="a in analytes" :key="a.key" :value="a.key">{{ a.label }}</option>
    </select>
  </div>

  <!-- D3 chart placeholder -->
  <div ref="chart" class="chart" aria-hidden="false"></div>

    <div class="form-row controls">
      <div class="col-param">
        <label class="form-label">Parámetro</label>
        <select v-model="selected">
          <option v-for="a in analytes" :key="a.key" :value="a.key">{{ a.label }}</option>
        </select>
      </div>
      <div class="col-value">
        <label class="form-label">Valor</label>
        <input class="value-input" :class="{invalid: !validation.isValid && String(value).trim() !== ''}" type="text" v-model="value" placeholder="Ej: 95" />
        <div class="hint small" v-if="!validation.isValid && String(value).trim() !== ''">{{ validation.message }}</div>
        <div class="hint small" v-else>Rango: {{ analytes.find(x => x.key === selected).min }}–{{ analytes.find(x => x.key === selected).max }} {{ unitForSelected }}</div>
      </div>
      <div class="col-unit">
        <label class="form-label">Unidad</label>
        <div class="unit-display">{{ unitForSelected }}</div>
      </div>
      <div class="col-date">
        <label class="form-label">Fecha y hora</label>
        <input type="datetime-local" v-model="inputDate" />
        <div class="hint small">Selecciona fecha y hora del registro (por defecto ahora)</div>
      </div>
      <div class="actions-col">
        <label class="form-label">&nbsp;</label>
        <button @click="addEntry" :disabled="!canAdd">Añadir</button>
      </div>
    </div>

    <div v-if="entries.length" class="entries">
      <h4>Resultados añadidos</h4>
      <table class="results">
        <thead><tr><th>Parámetro</th><th>Valor</th><th>Fecha</th><th></th></tr></thead>
        <tbody>
          <tr v-for="(e, idx) in entries" :key="idx">
            <td>{{ e.label }}</td>
            <td>{{ e.value }} {{ e.unit }}</td>
            <td>{{ formatDate(e.createdAt) }}</td>
            <td><button @click="removeEntry(idx)">Eliminar</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="form-actions">
      <button @click="saveAnalisis" :disabled="saving || entries.length===0">{{ saving ? 'Guardando…' : 'Guardar resultados' }}</button>
      <button @click="clearAll" :disabled="saving && entries.length===0">Borrar todo</button>
      <span v-if="msg" class="success">{{ msg }}</span>
    </div>

    <div :class="['toast', showToast ? 'show' : '']">Guardado</div>
  </div>
</template>

<script>
import * as d3 from 'd3'

export default {
  name: 'AnalisisSangreSection',
  data() {
    return {
      analytes: [
        { key: 'glucosa', label: 'Glucosa', unit: 'mg/dL', min: 0, max: 1000, decimals: 0 },
        { key: 'hemoglobina', label: 'Hemoglobina', unit: 'g/dL', min: 0, max: 25, decimals: 1 },
        { key: 'colesterol', label: 'Colesterol total', unit: 'mg/dL', min: 0, max: 1000, decimals: 0 },
        { key: 'trigliceridos', label: 'Triglicéridos', unit: 'mg/dL', min: 0, max: 2000, decimals: 0 },
        { key: 'creatinina', label: 'Creatinina', unit: 'mg/dL', min: 0, max: 50, decimals: 2 },
        { key: 'hematocrito', label: 'Hematocrito', unit: '%', min: 0, max: 100, decimals: 1 }
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
      showToast: false
    }
  },
  created() { this.load(); this.inputDate = this.localNowForInput() },
  mounted() {
    // initial draw after component mounted
    this.drawChart()
  },
  unmounted() {
    this.clearChart()
  },
  computed: {
    unitForSelected() {
      const a = this.analytes.find(x => x.key === this.selected)
      return a ? a.unit : ''
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
      if (a.min != null && num < a.min) return { isValid: false, message: `Valor mínimo ${a.min} ${a.unit}` }
      if (a.max != null && num > a.max) return { isValid: false, message: `Valor máximo ${a.max} ${a.unit}` }
      return { isValid: true, message: '' , value: num }
    }
  },
  methods: {
    // Map server 'tipo' (e.g. "Glucosa", "Hemoglobina") to a local analyte key.
    // Normalizes accents/case/whitespace and matches against known analytes.
    mapTipoToKey(tipo) {
      if (!tipo) return null
      const norm = String(tipo).normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase().replace(/\s+/g, '')
      const map = {
        glucosa: 'glucosa',
        hemoglobina: 'hemoglobina',
        'colesteroltotal': 'colesterol',
        colesterol: 'colesterol',
        triglicéridos: 'trigliceridos',
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
              // Map server DTO shape { tipo, valor, unidad, createdAt, id } to component shape
              this.entries = parsed.map(p => {
                const mappedKey = p.key || this.mapTipoToKey(p.tipo)
                const analyte = this.analytes.find(x => x.key === mappedKey)
                return {
                  id: p.id || null,
                  key: mappedKey || p.key || null,
                  label: p.label || p.tipo || p.key || (analyte ? analyte.label : ''),
                  // backend may send 'valor' (string/number) so normalize to string for display
                  value: (p.value !== undefined && p.value !== null) ? String(p.value) : (p.valor !== undefined && p.valor !== null) ? String(p.valor) : '',
                  unit: analyte ? analyte.unit : (p.unit || p.unidad || ''),
                  createdAt: p.createdAt || p.fechaCreacion || null
                }
              })
              // draw chart after loading entries
              this.drawChart()
            }
          } catch (ignore) {
            // ignore malformed content and start empty
            this.entries = []
          }
        }
      } catch (e) { console.error('No se pudo cargar análisis', e); this.error = 'No se pudo cargar análisis' }
    },

    addEntry() {
      if (!this.canAdd) return
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

      this.entries.push({ key: a.key, label: a.label, value: formatted, unit: a.unit, createdAt })
      this.value = ''
      this.inputDate = this.localNowForInput()
  this.drawChart()
    },

    async removeEntry(i) {
      const e = this.entries[i]
      if (e && e.id) {
        try {
          const svc = await import('@/services/historiaClinicaService').then(m => m.default)
          await svc.deleteDatoClinico(e.id)
          // refresh from server to keep consistency
          await this.load()
        } catch (err) {
          console.error('Error borrando dato en servidor', err)
          this.error = 'No se pudo eliminar en el servidor'
        }
      } else {
        this.entries.splice(i, 1)
      }
      this.drawChart()
    },

    async saveAnalisis() {
      this.saving = true
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        // send structured data as JSON string in the text/plain body
        const payload = this.entries.length ? JSON.stringify(this.entries) : ''
        await svc.updateAnalisisSangre(payload)
        this.msg = 'Resultados guardados.'
        this.showTemporaryToast()
        setTimeout(() => this.msg = '', 3000)
      } catch (e) { this.error = 'Error guardando análisis' } finally { this.saving = false }
    },

    async clearAll() {
      this.entries = []
  this.clearChart()
      try {
        const svc = await import('@/services/historiaClinicaService').then(m => m.default)
        await svc.updateAnalisisSangre('')
        this.msg = 'Contenido eliminado.'
        this.showTemporaryToast()
        setTimeout(() => this.msg = '', 3000)
      } catch (e) { this.error = 'Error borrando análisis' }
    },

    showTemporaryToast() {
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
      this.clearChart()
      const container = this.$refs.chart
      if (!container || !this.entries) return

      // select entries that correspond to the chosen parameter (by key or label)
      const param = (this.chartParam || 'glucosa').toLowerCase()
      const selectedEntries = this.entries.filter(e => {
        if (!e) return false
        if (e.key && String(e.key).toLowerCase() === param) return true
        return String(e.label || '').toLowerCase().includes(param)
      }).map(e => {
        return {
          date: e.createdAt ? new Date(e.createdAt) : null,
          value: (e.value !== undefined && e.value !== null) ? Number(String(e.value).replace(',', '.')) : (e.valor !== undefined && e.valor !== null) ? Number(String(e.valor).replace(',', '.')) : NaN
        }
      }).filter(d => d.date instanceof Date && !Number.isNaN(d.value))

      if (selectedEntries.length === 0) {
        // show a friendly message
        const msg = document.createElement('div')
        msg.className = 'chart-empty'
        const label = (this.analytes.find(a => a.key === this.chartParam) || {}).label || this.chartParam
        msg.textContent = `No hay datos históricos de ${label}.`
        container.appendChild(msg)
        return
      }

      // sort by date
      selectedEntries.sort((a, b) => a.date - b.date)

      const width = Math.min(700, container.clientWidth || 700)
      const margin = { top: 10, right: 20, bottom: 30, left: 50 }
      const height = 200
      const innerW = width - margin.left - margin.right
      const innerH = height - margin.top - margin.bottom

      const svg = d3.select(container).append('svg')
        .attr('width', width)
        .attr('height', height)
        .attr('role', 'img')
        .attr('aria-label', 'Histórico de glucosa')

      const x = d3.scaleTime()
        .domain(d3.extent(selectedEntries, d => d.date))
        .range([0, innerW])

      // compute y domain, extending it with analyte recommended min/max if available
      const dataMin = d3.min(selectedEntries, d => d.value)
      const dataMax = d3.max(selectedEntries, d => d.value)
      const analyteDef = this.analytes.find(a => a.key === (this.chartParam || this.selected))
      let domainMin = (dataMin != null) ? dataMin * 0.9 : 0
      let domainMax = (dataMax != null) ? dataMax * 1.1 : 1
      if (analyteDef) {
        if (analyteDef.min != null) domainMin = Math.min(domainMin, analyteDef.min)
        if (analyteDef.max != null) domainMax = Math.max(domainMax, analyteDef.max)
      }
      const y = d3.scaleLinear()
        .domain([domainMin, domainMax])
        .nice()
        .range([innerH, 0])

      const g = svg.append('g').attr('transform', `translate(${margin.left},${margin.top})`)

      // axes
      g.append('g')
        .attr('transform', `translate(0,${innerH})`)
        .call(d3.axisBottom(x).ticks(Math.min(6, selectedEntries.length)).tickFormat(d3.timeFormat('%d/%m %H:%M')))

      g.append('g').call(d3.axisLeft(y))

      // recommended interval band (if analyte has min/max)
      if (analyteDef && analyteDef.min != null && analyteDef.max != null) {
        const yTop = y(analyteDef.max)
        const yBottom = y(analyteDef.min)
        const rectY = Math.min(yTop, yBottom)
        const rectH = Math.abs(yBottom - yTop)
        g.append('rect')
          .attr('x', 0)
          .attr('y', rectY)
          .attr('width', innerW)
          .attr('height', rectH)
          .attr('fill', '#d2f0d9')
          .attr('opacity', 0.35)
          .attr('aria-hidden', 'true')
      }

      // line
      const line = d3.line()
        .x(d => x(d.date))
        .y(d => y(d.value))
        .curve(d3.curveMonotoneX)

      g.append('path')
        .datum(selectedEntries)
        .attr('fill', 'none')
        .attr('stroke', '#c73333')
        .attr('stroke-width', 2)
        .attr('d', line)

      // points
      g.selectAll('circle').data(selectedEntries).enter().append('circle')
        .attr('cx', d => x(d.date))
        .attr('cy', d => y(d.value))
        .attr('r', 3.5)
        .attr('fill', '#c73333')
        .append('title')
        .text(d => `${d.value} — ${d.date.toLocaleString()}`)
    },

    clearChart() {
      const container = this.$refs.chart
      if (container) container.innerHTML = ''
    },
    formatDate(iso) {
      if (!iso) return ''
      try {
        const d = new Date(iso)
        return d.toLocaleString()
      } catch (e) { return iso }
    }
  }
}
</script>

<style scoped>
.form-row.controls { display: grid; grid-template-columns: 1fr 1fr 120px 220px 120px; gap: 0.5rem; align-items: end; }
 .form-row.controls .value-input, .form-row.controls select, .form-row.controls input[type="datetime-local"] { width: 100%; box-sizing: border-box }
.unit-display { padding: .5rem; background: #f8f9fa; border: 1px solid #ddd; border-radius: 4px }
.entries { margin-top: .75rem }
.results { width: 100%; border-collapse: collapse }
.results th, .results td { padding: .4rem .6rem; border-bottom: 1px solid #eee; text-align: left }
.form-actions { margin-top: .75rem }
.toast { position: fixed; right: 1rem; bottom: 1rem; background: var(--accent, #007bff); color: white; padding: 0.5rem 1rem; border-radius: 6px; box-shadow: 0 6px 18px rgba(0,0,0,0.15); opacity: 0; transform: translateY(8px); transition: opacity .25s ease, transform .25s ease }
.toast.show { opacity: 1; transform: translateY(0) }
.results td:nth-child(3) { white-space: nowrap; color: #666; font-size: .9rem }
.chart-controls { display: flex; gap: .5rem; align-items: end; margin-bottom: .5rem }
.chart-controls select { width: 220px }
.chart-empty { color: #666; padding: .75rem; background: #fafafa; border: 1px dashed #ddd; border-radius: 6px }
</style>
