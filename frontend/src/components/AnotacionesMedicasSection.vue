<template>
  <section class="anotaciones-section" :aria-busy="loading ? 'true' : 'false'" aria-labelledby="anotaciones-heading">
    <h2 id="anotaciones-heading" class="sr-only">{{ $t('medical_annotations') }}</h2>

    <div class="info-box">
      <AppIcon name="info" size="lg" />
      <span>{{ $t('medical_annotations_help') }}</span>
    </div>

    <form class="filters-form" @submit.prevent="applyFilters">
      <div class="form-group">
        <label class="form-label" for="anotaciones-filtro-medico">{{ $t('filter_by_doctor') }}</label>
        <select id="anotaciones-filtro-medico" v-model="filtroMedicoNif" class="form-input">
          <option value="">{{ $t('all_doctors') }}</option>
          <option v-for="medico in medicosDisponibles" :key="medico.nif" :value="medico.nif">
            {{ medico.nombre }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label class="form-label" for="anotaciones-filtro-desde">{{ $t('filter_from_date') }}</label>
        <input id="anotaciones-filtro-desde" type="date" v-model="filtroDesde" class="form-input" />
      </div>
      <div class="form-group">
        <label class="form-label" for="anotaciones-filtro-hasta">{{ $t('filter_to_date') }}</label>
        <input id="anotaciones-filtro-hasta" type="date" v-model="filtroHasta" class="form-input" />
      </div>
      <div class="form-actions filters-actions">
        <button type="submit" class="btn-primary" :disabled="loading">{{ $t('apply_filters') }}</button>
        <button type="button" class="btn-secondary" @click="resetFilters" :disabled="loading">{{ $t('clear_filters') }}</button>
      </div>
    </form>

    <div v-if="error" class="alert alert-danger" role="alert" aria-live="assertive">
      <AppIcon name="alert-circle" size="lg" />
      {{ error }}
    </div>

    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
    </div>

    <div v-else-if="anotaciones.length" class="section">
      <div class="section-title">
        <AppIcon name="note" size="lg" />
        {{ $t('medical_annotations') }}
        <span class="badge badge-info">{{ anotaciones.length }}</span>
      </div>

      <div class="anotaciones-list">
        <article v-for="anotacion in anotaciones" :key="anotacion.id" class="anotacion-card">
          <header class="anotacion-header">
            <span class="anotacion-medico">{{ anotacion.medicoNombre || anotacion.medicoNif }}</span>
            <span v-if="anotacion.createdAt" class="badge badge-date">{{ formatDateTime(anotacion.createdAt) }}</span>
          </header>
          <p class="anotacion-mensaje">{{ anotacion.mensaje }}</p>
        </article>
      </div>
    </div>

    <div v-else class="empty-state-small">
      <AppIcon name="note" size="3xl" />
      <p>{{ $t('no_annotations_registered') }}</p>
      <span>{{ $t('no_annotations_hint') }}</span>
    </div>
  </section>
</template>

<script>
import authService from '@/services/authService'
import AppIcon from './AppIcon.vue'

export default {
  name: 'AnotacionesMedicasSection',
  components: { AppIcon },
  data() {
    return {
      anotaciones: [],
      loading: false,
      error: null,
      filtroMedicoNif: '',
      filtroDesde: '',
      filtroHasta: ''
    }
  },
  computed: {
    medicosDisponibles() {
      const porNif = new Map()
      this.anotaciones.forEach(a => {
        if (a.medicoNif && !porNif.has(a.medicoNif)) {
          porNif.set(a.medicoNif, { nif: a.medicoNif, nombre: a.medicoNombre || a.medicoNif })
        }
      })
      return Array.from(porNif.values())
    }
  },
  created() {
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      this.error = null
      try {
        this.anotaciones = await authService.listarMisAnotaciones({
          medicoNif: this.filtroMedicoNif || undefined,
          desde: this.filtroDesde ? `${this.filtroDesde}T00:00:00` : undefined,
          hasta: this.filtroHasta ? `${this.filtroHasta}T23:59:59` : undefined
        })
      } catch (e) {
        this.error = e.message || this.$t('error_loading_annotations')
        this.anotaciones = []
      } finally {
        this.loading = false
      }
    },
    applyFilters() {
      this.load()
    },
    resetFilters() {
      this.filtroMedicoNif = ''
      this.filtroDesde = ''
      this.filtroHasta = ''
      this.load()
    },
    formatDateTime(dt) {
      if (!dt) return ''
      const d = new Date(dt)
      if (isNaN(d)) return dt
      return d.toLocaleString()
    }
  }
}
</script>

<style scoped>
.anotaciones-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.filters-form {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: flex-end;
  padding: 1rem;
  background: var(--bg-light);
  border-radius: 8px;
}

.filters-form .form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  min-width: 180px;
}

.filters-actions {
  display: flex;
  gap: 0.5rem;
}

.anotaciones-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.anotacion-card {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-left: 4px solid var(--primary-color);
  border-radius: 8px;
  padding: 1rem;
}

.anotacion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.anotacion-medico {
  font-weight: 600;
  color: var(--text-primary);
}

.anotacion-mensaje {
  margin: 0;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 640px) {
  .filters-form {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
