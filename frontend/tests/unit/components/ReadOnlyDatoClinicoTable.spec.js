import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ReadOnlyDatoClinicoTable from '@/components/ReadOnlyDatoClinicoTable.vue'

const entry = (over = {}) => ({
  id: over.id ?? Math.random().toString(36).slice(2),
  tipo: 'Glucosa',
  valor: '95',
  unidad: 'mg/dL',
  createdAt: '2026-01-15T10:00:00',
  ...over
})

const factory = (entries = [], props = {}) => mount(ReadOnlyDatoClinicoTable, {
  props: { entries, domainLabel: 'análisis de sangre', ...props }
})

describe('ReadOnlyDatoClinicoTable', () => {
  it('muestra el estado vacío cuando no hay entradas', () => {
    const w = factory([])
    expect(w.find('.empty-hint').text()).toContain('No hay resultados de análisis de sangre registrados')
    expect(w.find('table').exists()).toBe(false)
    expect(w.find('.filters-form').exists()).toBe(false)
  })

  it('renderiza la tabla y los filtros cuando hay entradas', () => {
    const w = factory([entry()])
    expect(w.find('.filters-form').exists()).toBe(true)
    expect(w.findAll('tbody tr')).toHaveLength(1)
  })

  it('ofrece en el selector de parámetro solo los tipos presentes, ordenados', () => {
    const w = factory([
      entry({ tipo: 'Glucosa' }),
      entry({ tipo: 'Colesterol' }),
      entry({ tipo: 'Glucosa' })
    ])
    expect(w.vm.parametrosDisponibles).toEqual(['Colesterol', 'Glucosa'])
    const options = w.findAll('.filters-form select')[0].findAll('option').map(o => o.text())
    expect(options).toEqual(['Todos los parámetros', 'Colesterol', 'Glucosa'])
  })

  it('filtra por parámetro', async () => {
    const w = factory([
      entry({ tipo: 'Glucosa', valor: '95' }),
      entry({ tipo: 'Colesterol', valor: '180' })
    ])
    w.vm.filtroParametro = 'Colesterol'
    await w.vm.$nextTick()
    expect(w.findAll('tbody tr')).toHaveLength(1)
    expect(w.text()).toContain('180')
    expect(w.text()).not.toContain('95')
  })

  it('filtra por rango de fechas (desde / hasta, inclusivo)', async () => {
    const w = factory([
      entry({ id: 'a', createdAt: '2026-01-01T09:00:00' }),
      entry({ id: 'b', createdAt: '2026-02-15T09:00:00' }),
      entry({ id: 'c', createdAt: '2026-03-20T09:00:00' })
    ])
    w.vm.filtroDesde = '2026-02-01'
    w.vm.filtroHasta = '2026-03-20'
    await w.vm.$nextTick()
    expect(w.vm.filteredEntries.map(e => e.id)).toEqual(['c', 'b'])
  })

  it('ordena por fecha descendente por defecto y permite ascendente', async () => {
    const w = factory([
      entry({ id: 'vieja', createdAt: '2026-01-01T09:00:00' }),
      entry({ id: 'nueva', createdAt: '2026-06-01T09:00:00' })
    ])
    expect(w.vm.filteredEntries.map(e => e.id)).toEqual(['nueva', 'vieja'])
    w.vm.ordenFecha = 'asc'
    await w.vm.$nextTick()
    expect(w.vm.filteredEntries.map(e => e.id)).toEqual(['vieja', 'nueva'])
  })

  it('muestra un mensaje específico cuando los filtros no dejan resultados', async () => {
    const w = factory([entry({ tipo: 'Glucosa' })])
    w.vm.filtroParametro = 'Glucosa'
    w.vm.filtroDesde = '2030-01-01'
    await w.vm.$nextTick()
    expect(w.find('table').exists()).toBe(false)
    expect(w.find('.empty-hint').text()).toContain('No hay resultados para los filtros seleccionados')
  })

  it('el botón de limpiar filtros se habilita solo con filtros activos y los resetea', async () => {
    const w = factory([entry(), entry({ id: '2', createdAt: '2026-02-01T09:00:00' })])
    const clearBtn = w.find('.filters-actions button')
    expect(clearBtn.attributes('disabled')).toBeDefined()

    w.vm.filtroParametro = 'Glucosa'
    w.vm.ordenFecha = 'asc'
    await w.vm.$nextTick()
    expect(clearBtn.attributes('disabled')).toBeUndefined()

    await clearBtn.trigger('click')
    expect(w.vm.filtroParametro).toBe('')
    expect(w.vm.filtroDesde).toBe('')
    expect(w.vm.filtroHasta).toBe('')
    expect(w.vm.ordenFecha).toBe('desc')
  })

  it('pagina sobre las entradas filtradas y vuelve a la página 1 al cambiar el filtro', async () => {
    const many = Array.from({ length: 25 }, (_, i) => entry({
      id: `e${i}`,
      tipo: i % 2 === 0 ? 'Glucosa' : 'Colesterol',
      createdAt: `2026-01-${String(i + 1).padStart(2, '0')}T09:00:00`
    }))
    const w = factory(many)
    expect(w.vm.totalPages).toBe(3)
    w.vm.page = 3
    await w.vm.$nextTick()

    w.vm.filtroParametro = 'Glucosa'
    await w.vm.$nextTick()
    expect(w.vm.page).toBe(1)
    expect(w.vm.totalPages).toBe(2)
  })

  it('formatDate tolera nulos y valores no fecha', () => {
    const w = factory([entry()])
    expect(w.vm.formatDate(null)).toBe('')
    expect(w.vm.formatDate('')).toBe('')
  })

  it('usa ids únicos para cada instancia montada a la vez', () => {
    const w1 = factory([entry()])
    const w2 = factory([entry()])
    expect(w1.vm.uid).not.toBe(w2.vm.uid)
  })
})
