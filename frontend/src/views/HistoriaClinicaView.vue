<template>
  <div class="page">
    <h1>Historia clínica</h1>

    <section class="subsections">
      <div class="subnav" role="tablist" aria-label="Secciones de historia clínica">
        <button :class="{active: activeSection==='identificacion'}" @click="activeSection='identificacion'" role="tab" :aria-selected="activeSection==='identificacion'">Identificación básica</button>
        <button :class="{active: activeSection==='antecedentes'}" @click="activeSection='antecedentes'" role="tab" :aria-selected="activeSection==='antecedentes'">Antecedentes personales y familiares</button>
        <button :class="{active: activeSection==='alergias'}" @click="activeSection='alergias'" role="tab" :aria-selected="activeSection==='alergias'">Alergias e intolerancias</button>
        <button :class="{active: activeSection==='analisis'}" @click="activeSection='analisis'" role="tab" :aria-selected="activeSection==='analisis'">Análisis de sangre</button>
      </div>

      <transition name="fade-slide" mode="out-in">
        <div v-if="activeSection==='identificacion'" key="identificacion" class="section-panel card simple-form" role="tabpanel">
          <h3>Identificación básica</h3>
          <label class="form-label">Nombre completo</label>
          <input type="text" v-model="identNombre" placeholder="Nombre y apellidos" />

          <div class="form-row">
            <div>
              <label class="form-label">NIF / NIE</label>
              <input type="text" v-model="identNif" placeholder="12345678A" />
            </div>
            <div>
              <label class="form-label">Fecha de nacimiento</label>
              <input type="date" v-model="identFechaNacimiento" />
            </div>
          </div>

          <div class="form-row">
            <div>
              <label class="form-label">Teléfono</label>
              <input type="tel" v-model="identTelefono" placeholder="600 000 000" />
            </div>
            <div>
              <label class="form-label">Email (opcional)</label>
              <input type="email" v-model="identEmail" placeholder="tu@correo.com" />
            </div>
          </div>

          <p class="hint">Los datos se guardan y se usan sólo para identificar tu historial; escribe lo que aparece en tus documentos.</p>
          <div class="form-actions form-actions--right">
            <button @click="saveIdentificacion" :disabled="savingIdent">{{ savingIdent ? 'Guardando…' : 'Guardar identificación' }}</button>
            <span v-if="msgIdent" class="success">{{ msgIdent }}</span>
          </div>
        </div>

        <div v-else-if="activeSection==='antecedentes'" key="antecedentes">
          <AntecedentesSection />
        </div>

        <div v-else-if="activeSection==='alergias'" key="alergias">
          <AlergiasSection />
        </div>

        <div v-else-if="activeSection==='analisis'" key="analisis">
          <AnalisisSangreSection />
        </div>
      </transition>
    </section>

  

    <section class="upload">
      <form @submit.prevent="onUpload">
        <input type="file" @change="onFileChange" />
        <button type="submit" :disabled="!file || uploading">{{ uploading ? 'Subiendo…' : 'Subir' }}</button>
      </form>
      <p v-if="error" class="error">{{ error }}</p>
    </section>

    <section class="list" v-if="items.length">
      <h2>Mis archivos</h2>
      <ul>
        <li v-for="it in items" :key="it.id" class="item">
          <div class="meta">
            <strong>{{ it.nombreOriginal }}</strong>
            <small>({{ formatSize(it.sizeBytes) }})</small>
          </div>
          <div class="actions">
            <button @click="download(it)">Descargar</button>
            <button @click="remove(it)" :disabled="removingId===it.id">Eliminar</button>
          </div>
        </li>
      </ul>
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

/* transition for panels */
</script>

<style scoped>
.list ul { list-style: none; padding: 0; }
.item { display: flex; align-items: center; justify-content: space-between; padding: .5rem 0; border-bottom: 1px solid #eee; }
.meta small { color: #666; margin-left: .5rem; }
.actions a { margin-right: .75rem; }
</style>
