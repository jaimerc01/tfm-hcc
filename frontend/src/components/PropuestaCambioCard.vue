<template>
  <article class="propuesta-card" :class="{ 'propuesta-card--accent': accent }">
    <header class="propuesta-card__header">
      <span class="badge badge-info">{{ $t('change_proposal_domain_' + propuesta.dominio) }}</span>
      <span class="badge badge-op">{{ $t('change_proposal_operation_' + propuesta.operacion) }}</span>
      <span
        v-if="showStatus && propuesta.estado"
        :class="['badge', 'badge-status', 'badge-status--' + propuesta.estado]"
      >{{ $t('change_proposal_status_' + propuesta.estado) }}</span>
      <span v-if="propuesta.fechaCreacion" class="propuesta-card__date">
        {{ formatDateTime(propuesta.fechaCreacion) }}
      </span>
    </header>

    <p v-if="showDoctor" class="propuesta-card__doctor">
      {{ $t('change_proposal_from_doctor', { name: nombreMedico }) }}
    </p>

    <dl class="propuesta-card__values">
      <template v-if="propuesta.operacion !== 'CREATE' && propuesta.descripcionActual">
        <dt>{{ $t('propose_change_current_value') }}</dt>
        <dd>{{ propuesta.descripcionActual }}</dd>
      </template>
      <template v-if="propuesta.operacion !== 'DELETE'">
        <dt>{{ propuesta.operacion === 'CREATE' ? $t('propose_change_new_value') : $t('propose_change_proposed_value') }}</dt>
        <dd>{{ valorPropuesto }}</dd>
      </template>
      <dt>{{ $t('change_proposal_reason') }}</dt>
      <dd>{{ propuesta.motivo }}</dd>
    </dl>

    <p v-if="propuesta.fechaResolucion" class="propuesta-card__resolved">
      {{ $t('change_proposal_resolved_at', { date: formatDateTime(propuesta.fechaResolucion) }) }}
    </p>

    <footer v-if="$slots.actions" class="propuesta-card__actions">
      <slot name="actions" />
    </footer>
  </article>
</template>

<script>
/**
 * Tarjeta de una propuesta de cambio clínico. La usan tanto el paciente
 * (sección «Cambios propuestos», con acciones para pendientes e histórico de
 * resueltas) como el médico (pestaña «Propuestas enviadas» del historial del
 * paciente). Es solo presentación: las acciones llegan por el slot `actions`.
 */
export default {
  name: 'PropuestaCambioCard',
  props: {
    propuesta: {
      type: Object,
      required: true
    },
    /** Muestra la línea «Propuesto por el Dr./Dra. …». */
    showDoctor: {
      type: Boolean,
      default: false
    },
    /** Muestra la pastilla de estado (pendiente / aceptada / rechazada / anulada). */
    showStatus: {
      type: Boolean,
      default: false
    },
    /** Resalta la tarjeta con el borde de aviso (propuestas pendientes de confirmar). */
    accent: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    nombreMedico() {
      const medico = this.propuesta.medico
      if (!medico) return '—'
      return [medico.nombre, medico.apellido1, medico.apellido2].filter(Boolean).join(' ') || medico.nif || '—'
    },
    valorPropuesto() {
      const p = this.propuesta
      if (p.dominio === 'ANTECEDENTE' && p.antecedente) {
        return `${p.antecedente.categoria || ''}: ${p.antecedente.descripcion || ''}`.trim()
      }
      if (p.dominio === 'ALERGIA' && p.alergia) return p.alergia.descripcion || ''
      if (p.medicion) {
        return `${p.medicion.label || ''}: ${p.medicion.value || ''} ${p.medicion.unit || ''}`.trim()
      }
      return '—'
    }
  },
  methods: {
    formatDateTime(dt) {
      if (!dt) return ''
      const d = new Date(dt)
      return isNaN(d) ? dt : d.toLocaleString()
    }
  }
}
</script>

<style scoped>
.propuesta-card {
  background: var(--bg-light);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.propuesta-card--accent {
  border-left: 4px solid var(--warning-color);
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
.badge-status--ACEPTADA {
  background: var(--success-light);
  color: var(--success-active);
}
.badge-status--RECHAZADA,
.badge-status--ANULADA {
  background: var(--badge-danger-bg);
  color: var(--badge-danger-text);
}
.badge-status--PENDIENTE {
  background: var(--badge-warning-bg);
  color: var(--badge-warning-text);
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
.propuesta-card__resolved {
  margin: 0;
  font-size: 0.75rem;
  color: var(--text-secondary);
}
.propuesta-card__actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.25rem;
}
</style>
