import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import DatoClinicoResultsTable from '@/components/DatoClinicoResultsTable.vue'
import ReadOnlyDatoClinicoTable from '@/components/ReadOnlyDatoClinicoTable.vue'

const analytes = [{ key: 'glucosa', labelKey: 'glucose', unit: 'mg/dL' }]

describe('DatoClinicoResultsTable', () => {
  const factory = (entries) => mount(DatoClinicoResultsTable, {
    props: { entries, analytes, domainLabel: 'análisis de sangre' }
  })

  it('muestra el empty state cuando no hay entradas', () => {
    const w = factory([])
    expect(w.find('.empty-state-small').exists()).toBe(true)
    expect(w.find('table').exists()).toBe(false)
  })

  it('renderiza una fila por entrada con el label del analito traducido', () => {
    const w = factory([{ key: 'glucosa', value: '95', unit: 'mg/dL', createdAt: '2026-01-01T10:00:00Z' }])
    expect(w.findAll('tbody tr')).toHaveLength(1)
    expect(w.find('.param-cell strong').text()).toBe('Glucosa')
    expect(w.find('.value-cell').text()).toContain('95')
  })

  it('getEntryLabel cae a label/tipo/key si no hay analito', () => {
    const w = factory([{ tipo: 'Desconocido', value: '1' }])
    expect(w.find('.param-cell strong').text()).toBe('Desconocido')
  })

  it('emite delete-entry con el índice al pulsar la papelera', async () => {
    const w = factory([{ key: 'glucosa', value: '95' }])
    await w.find('tbody .btn-danger').trigger('click')
    expect(w.emitted('delete-entry')[0]).toEqual([0])
  })

  it('emite clear-all al pulsar el botón de borrar todo', async () => {
    const w = factory([{ key: 'glucosa', value: '95' }])
    await w.find('.form-actions .btn-danger').trigger('click')
    expect(w.emitted('clear-all')).toBeTruthy()
  })

  it('deshabilita el botón de borrar todo cuando saving', () => {
    const w = mount(DatoClinicoResultsTable, { props: { entries: [{ key: 'glucosa', value: '1' }], analytes, domainLabel: 'x', saving: true } })
    expect(w.find('.form-actions .btn-danger').attributes('disabled')).toBeDefined()
  })
})

describe('ReadOnlyDatoClinicoTable', () => {
  it('muestra el hint vacío sin entradas', () => {
    const w = mount(ReadOnlyDatoClinicoTable, { props: { entries: [], domainLabel: 'signos vitales' } })
    expect(w.find('.empty-hint').exists()).toBe(true)
  })

  it('renderiza tipo, valor+unidad y fecha formateada', () => {
    const w = mount(ReadOnlyDatoClinicoTable, {
      props: { entries: [{ id: 1, tipo: 'Glucosa', valor: '95', unidad: 'mg/dL', createdAt: '2026-01-01T10:00:00Z' }], domainLabel: 'x' }
    })
    const cells = w.findAll('tbody td')
    expect(cells[0].text()).toBe('Glucosa')
    expect(cells[1].text()).toContain('95')
    expect(cells[2].text()).toBe(new Date('2026-01-01T10:00:00Z').toLocaleString())
  })

  it('formatDate devuelve "" sin iso', () => {
    const w = mount(ReadOnlyDatoClinicoTable, { props: { entries: [], domainLabel: 'x' } })
    expect(w.vm.formatDate(null)).toBe('')
  })
})
