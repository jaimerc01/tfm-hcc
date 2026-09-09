import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import DatoClinicoResultsTable from '@/components/DatoClinicoResultsTable.vue'
import ReadOnlyDatoClinicoTable from '@/components/ReadOnlyDatoClinicoTable.vue'

const analytes = [
  { key: 'glucosa', labelKey: 'glucose', unit: 'mg/dL' },
  { key: 'colesterol', labelKey: 'total_cholesterol', unit: 'mg/dL' }
]

describe('DatoClinicoResultsTable', () => {
  const factory = (entries) => mount(DatoClinicoResultsTable, {
    props: { entries, analytes, domainLabel: 'análisis de sangre' }
  })

  it('muestra el empty state cuando no hay entradas', () => {
    const w = factory([])
    expect(w.find('.empty-state-small').exists()).toBe(true)
    expect(w.find('table').exists()).toBe(false)
    expect(w.find('.filters-form').exists()).toBe(false)
  })

  it('renderiza una fila por entrada con el label del analito traducido', () => {
    const w = factory([{ id: 'a', key: 'glucosa', value: '95', unit: 'mg/dL', createdAt: '2026-01-01T10:00:00Z' }])
    expect(w.findAll('tbody tr')).toHaveLength(1)
    expect(w.find('.param-cell strong').text()).toBe('Glucosa')
    expect(w.find('.value-cell').text()).toContain('95')
    expect(w.find('.filters-form').exists()).toBe(true)
  })

  it('getEntryLabel cae a label/tipo/key si no hay analito', () => {
    const w = factory([{ id: 'a', tipo: 'Desconocido', value: '1' }])
    expect(w.find('.param-cell strong').text()).toBe('Desconocido')
  })

  it('emite delete-entry con el id de la entrada al pulsar la papelera', async () => {
    const w = factory([{ id: 'abc', key: 'glucosa', value: '95' }])
    await w.find('tbody .btn-danger').trigger('click')
    expect(w.emitted('delete-entry')[0]).toEqual(['abc'])
  })

  it('emite clear-all al pulsar el botón de borrar todo', async () => {
    const w = factory([{ id: 'a', key: 'glucosa', value: '95' }])
    await w.find('.form-actions .btn-danger').trigger('click')
    expect(w.emitted('clear-all')).toBeTruthy()
  })

  it('deshabilita el botón de borrar todo cuando saving', () => {
    const w = mount(DatoClinicoResultsTable, { props: { entries: [{ id: 'a', key: 'glucosa', value: '1' }], analytes, domainLabel: 'x', saving: true } })
    expect(w.find('.form-actions .btn-danger').attributes('disabled')).toBeDefined()
  })

  const conjunto = () => [
    { id: 'g1', key: 'glucosa', value: '90', createdAt: '2026-01-10T09:00:00' },
    { id: 'g2', key: 'glucosa', value: '110', createdAt: '2026-06-15T09:00:00' },
    { id: 'c1', key: 'colesterol', value: '180', createdAt: '2026-03-01T09:00:00' }
  ]

  it('el selector de parámetro ofrece los analitos presentes, ordenados y traducidos', () => {
    const w = factory(conjunto())
    expect(w.vm.parametrosDisponibles).toEqual(['Colesterol Total', 'Glucosa'])
  })

  it('filtra por parámetro', async () => {
    const w = factory(conjunto())
    w.vm.filtroParametro = 'Colesterol Total'
    await w.vm.$nextTick()
    expect(w.findAll('tbody tr')).toHaveLength(1)
    expect(w.text()).toContain('180')
  })

  it('filtra por rango de fechas inclusivo', async () => {
    const w = factory(conjunto())
    w.vm.filtroDesde = '2026-02-01'
    w.vm.filtroHasta = '2026-06-15'
    await w.vm.$nextTick()
    expect(w.vm.filteredEntries.map(e => e.id)).toEqual(['g2', 'c1'])
  })

  it('ordena por fecha: descendente por defecto, ascendente opcional', async () => {
    const w = factory(conjunto())
    expect(w.vm.filteredEntries.map(e => e.id)).toEqual(['g2', 'c1', 'g1'])
    w.vm.ordenFecha = 'asc'
    await w.vm.$nextTick()
    expect(w.vm.filteredEntries.map(e => e.id)).toEqual(['g1', 'c1', 'g2'])
  })

  it('muestra un mensaje propio cuando ningún resultado pasa los filtros', async () => {
    const w = factory(conjunto())
    w.vm.filtroDesde = '2030-01-01'
    await w.vm.$nextTick()
    expect(w.find('table').exists()).toBe(false)
    expect(w.find('.empty-hint').text()).toContain('No hay resultados para los filtros seleccionados')
  })

  it('el botón de limpiar filtros se habilita con filtros activos y los restablece', async () => {
    const w = factory(conjunto())
    const clearBtn = w.find('.filters-actions button')
    expect(clearBtn.attributes('disabled')).toBeDefined()
    w.vm.filtroParametro = 'Glucosa'
    await w.vm.$nextTick()
    expect(clearBtn.attributes('disabled')).toBeUndefined()
    await clearBtn.trigger('click')
    expect(w.vm.filtroParametro).toBe('')
    expect(w.vm.ordenFecha).toBe('desc')
  })

  it('al filtrar vuelve a una página válida', async () => {
    const many = Array.from({ length: 25 }, (_, i) => ({
      id: `e${i}`,
      key: i % 2 === 0 ? 'glucosa' : 'colesterol',
      value: String(i),
      createdAt: `2026-01-${String(i + 1).padStart(2, '0')}T09:00:00`
    }))
    const w = factory(many)
    w.vm.page = 3
    await w.vm.$nextTick()
    w.vm.filtroParametro = 'Glucosa'
    await w.vm.$nextTick()
    expect(w.vm.page).toBeLessThanOrEqual(w.vm.totalPages)
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
