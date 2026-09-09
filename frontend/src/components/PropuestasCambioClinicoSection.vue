<template>
  <section class="propuestas-section panel-card" aria-labelledby="propuestas-heading">
    <div class="panel-header">
      <h2 id="propuestas-heading">{{ $t('change_proposals') }}</h2>
      <p class="panel-subtitle">{{ $t('change_proposals_subtitle') }}</p>
    </div>

    <div v-if="loading" class="empty-hint">{{ $t('loading_info') }}</div>

    <template v-else>
      <p v-if="!pendientes.length && !historico.length" class="empty-hint">
        {{ $t('no_change_proposals') }}
      </p>

      <!-- Pendientes de confirmar -->
      <div v-if="pendientes.length" class="propuestas-group">
        <div class="alert alert-warning" role="alert">
          {{ $t('change_proposals_pending_banner', { count: pendientes.length }) }}
        </div>

        <div class="cards-grid">
          <PropuestaCambioCard v-for="p in pendientes" :key="p.id" :propuesta="p" show-doctor accent>
            <template #actions>
              <button type="button" class="btn-primary" :disabled="working" @click="pedirConfirmacion(p, true)">
                {{ $t('change_proposal_accept') }}
              </button>
              <button type="button" class="btn-secondary" :disabled="working" @click="pedirConfirmacion(p, false)">
                {{ $t('change_proposal_reject') }}
              </button>
            </template>
          </PropuestaCambioCard>
        </div>
      </div>

      <!-- Histórico de propuestas ya resueltas -->
      <div v-if="historico.length" class="propuestas-group">
        <h3 class="propuestas-group__title">{{ $t('change_proposals_history_heading') }}</h3>

        <div class="cards-grid">
          <PropuestaCambioCard v-for="p in historico" :key="p.id" :propuesta="p" show-doctor show-status />
        </div>
      </div>
    </template>

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
import PropuestaCambioCard from './PropuestaCambioCard.vue'

const ESTADO_PENDIENTE = 'PENDIENTE'

export default {
  name: 'PropuestasCambioClinicoSection',
  components: { AppModal, PropuestaCambioCard },
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
  computed: {
    pendientes() {
      return this.propuestas.filter(p => p.estado === ESTADO_PENDIENTE)
    },
    historico() {
      return this.propuestas.filter(p => p.estado !== ESTADO_PENDIENTE)
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
        this.$emit('count-changed', this.pendientes.length)
      }
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
.propuestas-group {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.propuestas-group__title {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
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
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
</style>
