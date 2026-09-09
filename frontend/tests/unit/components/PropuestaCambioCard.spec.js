import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import PropuestaCambioCard from '@/components/PropuestaCambioCard.vue'

const base = (over = {}) => ({
  id: 'p1',
  dominio: 'ANALISIS_SANGRE',
  operacion: 'UPDATE',
  estado: 'PENDIENTE',
  motivo: 'Corrección del valor',
  descripcionActual: 'Glucosa: 250 mg/dL',
  medicion: { label: 'Glucosa', value: '95', unit: 'mg/dL' },
  medico: { nombre: 'Ana', apellido1: 'López' },
  fechaCreacion: '2026-09-01T10:00:00Z',
  ...over
})

describe('PropuestaCambioCard', () => {
  it('muestra dominio, operación, valor actual, valor propuesto y motivo', () => {
    const w = mount(PropuestaCambioCard, { props: { propuesta: base() } })
    expect(w.text()).toContain('Análisis de sangre')
    expect(w.text()).toContain('Edición')
    expect(w.text()).toContain('Glucosa: 250 mg/dL')
    expect(w.text()).toContain('Glucosa: 95 mg/dL')
    expect(w.text()).toContain('Corrección del valor')
  })

  it('oculta la pastilla de estado y el médico salvo que se pidan por props', () => {
    const w = mount(PropuestaCambioCard, { props: { propuesta: base() } })
    expect(w.find('.badge-status').exists()).toBe(false)
    expect(w.find('.propuesta-card__doctor').exists()).toBe(false)
  })

  it('con show-status y show-doctor muestra el estado y el nombre del médico', () => {
    const w = mount(PropuestaCambioCard, {
      props: {
        propuesta: base({ estado: 'ACEPTADA', fechaResolucion: '2026-09-02T09:00:00Z' }),
        showStatus: true,
        showDoctor: true
      }
    })
    expect(w.find('.badge-status--ACEPTADA').text()).toBe('Aceptada')
    expect(w.text()).toContain('Ana López')
    expect(w.text()).toContain('Resuelta el')
  })

  it('en un alta no muestra el valor actual y titula el valor como descripción', () => {
    const w = mount(PropuestaCambioCard, {
      props: { propuesta: base({ operacion: 'CREATE', descripcionActual: null }) }
    })
    expect(w.text()).not.toContain('Glucosa: 250 mg/dL')
    expect(w.text()).toContain('Descripción')
  })

  it('renderiza el slot de acciones', () => {
    const w = mount(PropuestaCambioCard, {
      props: { propuesta: base() },
      slots: { actions: '<button>Aceptar y aplicar</button>' }
    })
    expect(w.find('.propuesta-card__actions button').text()).toBe('Aceptar y aplicar')
  })
})
