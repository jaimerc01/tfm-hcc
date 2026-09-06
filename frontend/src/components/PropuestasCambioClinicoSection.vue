<template>
  <section v-if="loading || propuestas.length" class="propuestas-section panel-card" aria-labelledby="propuestas-heading">
    <div class="panel-header">
      <h2 id="propuestas-heading">{{ $t('change_proposals') }}</h2>
      <p class="panel-subtitle">{{ $t('change_proposals_subtitle') }}</p>
    </div>

    <div v-if="loading" class="empty-hint">{{ $t('loading_info') }}</div>

    <div v-else>
      <div class="alert alert-warning" role="alert">
        {{ $t('change_proposals_pending_banner', { count: propuestas.length }) }}
      </div>

      <div class="cards-grid">
        <article v-for="p in propuestas" :key="p.id" class="propuesta-card">
          <header class="propuesta-card__header">
            <span class="badge badge-info">{{ $t('change_proposal_domain_' + p.dominio) }}</span>
            <span class="badge badge-op">{{ $t('change_proposal_operation_' + p.operacion) }}</span>
            <span v-if="p.fechaCreacion" class="propuesta-card__date">{{ formatDateTime(p.fechaCreacion) }}</span>
          </header>

          <p class="propuesta-card__doctor">
            {{ $t('change_proposal_from_doctor', { name: nombreMedico(p.medico) }) }}
          </p>

          <dl class="propuesta-card__values">
            <template v-if="p.operacion !== 'CREATE' && p.descripcionActual">
              <dt>{{ $t('propose_change_current_value') }}</dt>
              <dd>{{ p.descripcionActual }}</dd>
            </template>
            <template v-if="p.operacion !== 'DELETE'">
              <dt>{{ $t('propose_change_proposed_value') }}</dt>
              <dd>{{ valorPropuesto(p) }}</dd>
            </template>
            <dt>{{ $t('change_proposal_reason') }}</dt>
            <dd>{{ p.motivo }}</dd>
          </dl>

          <footer class="propuesta-card__actions">
            <button type="button" class="btn-primary" :disabled="working" @click="pedirConfirmacion(p, true)">
              {{ $t('change_proposal_accept') }}
            </button>
            <button type="button" class="btn-secondary" :disabled="working" @click="pedirConfirmacion(p, false)">
              {{ $t('change_proposal_reject') }}
            </button>
          </footer>
        </article>
      </div>
    </div>

    <div v-if="mensaje" :class="['alert', mensajeError ? 'alert-danger' : 'alert-success']" role="status" aria-live="polite">
      {{ mensaje }}
    </div>

    <AppModal v-if="confirmando" :label="$t('confirm_action')" @close="cancelarConfirmacion">
      <template #header>
        <h3>{{ $t('confirm_action') }}</h3>
      </template>
      <p class="modal-text">
        {{ pendiente.aceptar ? $t('change_proposal_accept_confirm') : $t('change_proposal_reject_confirm') }}
      </p>
      <template #footer>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="cancelarConfirmacion">{{ $t('cancel') }}</button>
          <button type="button" class="btn-primary" :disabled="working" @click="confirmar">
            {{ pendiente.aceptar ? $t('change_proposal_accept') : $t('change_proposal_reject') }}
          </button>
        </div>
      </template>
    </AppModal>
  </section>
</template>

<script>
import historiaClinicaService from '@/services/historiaClinicaService'
import AppModal from './Modal.vue'

export default {
  name: 'PropuestasCambioClinicoSection',
  components: { AppModal },
  emits: ['applied', 'count-changed'],
  data() {
    return {
      propuestas: [],
      loading: true,
      working: false,
      mensaje: '',
      mensajeError: false,
      confirmando: false,
      pendiente: { propuesta: null, aceptar: true }
    }
  },
  created() {
    this.cargar()
  },
  methods: {
    async cargar() {
      this.loading = true
      try {
        const { data } = await historiaClinicaService.listarPropuestasCambio()
        this.propuestas = Array.isArray(data) ? data : []
      } catch (e) {
        this.propuestas = []
      } finally {
        this.loading = false
        this.$emit('count-changed', this.propuestas.length)
      }
    },
    nombreMedico(medico) {
      if (!medico) return '—'
      return [medico.nombre, medico.apellido1, medico.apellido2].filter(Boolean).join(' ') || medico.nif || '—'
    },
    valorPropuesto(p) {
      if (p.dominio === 'ANTECEDENTE' && p.antecedente) {
        return `${p.antecedente.categoria || ''}: ${p.antecedente.descripcion || ''}`.trim()
      }
      if (p.dominio === 'ALERGIA' && p.alergia) return p.alergia.descripcion || ''
      if (p.medicion) {
        return `${p.medicion.label || ''}: ${p.medicion.value || ''} ${p.medicion.unit || ''}`.trim()
      }
      return '—'
    },
    formatDateTime(dt) {
      if (!dt) return ''
      const d = new Date(dt)
      return isNaN(d) ? dt : d.toLocaleString()
    },
    pedirConfirmacion(propuesta, aceptar) {
      this.pendiente = { propuesta, aceptar }
      this.confirmando = true
    },
    cancelarConfirmacion() {
      if (this.working) return
      this.confirmando = false
    },
    async confirmar() {
      const { propuesta, aceptar } = this.pendiente
      if (!propuesta) return
      this.working = true
      this.mensaje = ''
      try {
        await historiaClinicaService.responderPropuestaCambio(propuesta.id, aceptar)
        this.mensaje = aceptar ? this.$t('change_proposal_accepted') : this.$t('change_proposal_rejected')
        this.mensajeError = false
        this.confirmando = false
        await this.cargar()
        if (aceptar) this.$emit('applied')
      } catch (e) {
        this.mensaje = this.$t('change_proposal_error')
        this.mensajeError = true
        this.confirmando = false
      } finally {
        this.working = false
      }
    }
  }
}
</script>

<style scoped>
.propuestas-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.panel-header {
  margin-bottom: 0.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border);
}
.panel-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
}
.panel-subtitle {
  margin: 0.25rem 0 0;
  font-size: 0.875rem;
  color: var(--text-secondary);
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
.propuesta-card {
  background: var(--bg-light);
  border: 1px solid var(--border);
  border-left: 4px solid var(--warning-color);
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.propuesta-card__header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.badge-op {
  background: var(--badge-warning-bg);
  color: var(--badge-warning-text);
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
}
.propuesta-card__date {
  margin-left: auto;
  font-size: 0.75rem;
  color: var(--text-secondary);
}
.propuesta-card__doctor {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}
.propuesta-card__values {
  margin: 0;
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0.25rem 0.75rem;
}
.propuesta-card__values dt {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
}
.propuesta-card__values dd {
  margin: 0;
  font-size: 0.875rem;
  color: var(--text-primary);
  word-break: break-word;
}
.propuesta-card__actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.25rem;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
</style>
