<template>
  <div class="page">
    <h1>Historia clínica</h1>

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

export default {
  name: 'HistoriaClinicaView',
  data() {
    return {
      items: [],
      file: null,
      uploading: false,
      removingId: null,
      error: null,
    }
  },
  created() { this.load() },
  methods: {
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
</script>

<style scoped>
.upload { margin: 1rem 0; }
.error { color: #b00020; }
.list ul { list-style: none; padding: 0; }
.item { display: flex; align-items: center; justify-content: space-between; padding: .5rem 0; border-bottom: 1px solid #eee; }
.meta small { color: #666; margin-left: .5rem; }
.actions a { margin-right: .75rem; }
</style>
