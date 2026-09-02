import { describe, it, expect } from 'vitest'
import { prepareChartData } from '@/composables/charts/chartUtils'
import { useChart } from '@/composables/useChart'

describe('prepareChartData', () => {
  it('devuelve [] para entradas no válidas', () => {
    expect(prepareChartData(null, 'glucosa')).toEqual([])
    expect(prepareChartData('x', 'glucosa')).toEqual([])
  })

  it('filtra por key exacta (case-insensitive)', () => {
    const entries = [
      { key: 'glucosa', value: '90', createdAt: '2026-01-02' },
      { key: 'hemoglobina', value: '14', createdAt: '2026-01-01' }
    ]
    const out = prepareChartData(entries, 'GLUCOSA')
    expect(out).toHaveLength(1)
    expect(out[0].value).toBe(90)
  })

  it('filtra por label que contiene la clave', () => {
    const entries = [{ label: 'Glucosa en ayunas', value: '100', createdAt: '2026-01-01' }]
    expect(prepareChartData(entries, 'glucosa')).toHaveLength(1)
  })

  it('convierte comas decimales y ordena por fecha ascendente', () => {
    const entries = [
      { key: 'x', value: '1,5', createdAt: '2026-03-01' },
      { key: 'x', value: '0,5', createdAt: '2026-01-01' }
    ]
    const out = prepareChartData(entries, 'x')
    expect(out.map(d => d.value)).toEqual([0.5, 1.5])
    expect(out[0].date.getTime()).toBeLessThan(out[1].date.getTime())
  })

  it('usa el campo valor si no hay value', () => {
    const entries = [{ key: 'x', valor: '7', createdAt: '2026-01-01' }]
    expect(prepareChartData(entries, 'x')[0].value).toBe(7)
  })

  it('descarta entradas sin fecha o con valor no numérico', () => {
    const entries = [
      { key: 'x', value: 'abc', createdAt: '2026-01-01' },
      { key: 'x', value: '5', createdAt: null },
      { key: 'x', value: '9', createdAt: '2026-01-02' }
    ]
    const out = prepareChartData(entries, 'x')
    expect(out).toEqual([{ date: new Date('2026-01-02'), value: 9 }])
  })

  it('ignora entradas null en la lista', () => {
    expect(prepareChartData([null, { key: 'x', value: '1', createdAt: '2026-01-01' }], 'x')).toHaveLength(1)
  })
})

describe('useChart', () => {
  it('re-exporta todas las funciones de gráfico', () => {
    const c = useChart()
    expect(Object.keys(c).sort()).toEqual([
      'drawBarChart',
      'drawComparisonPieChart',
      'drawDualLineChart',
      'drawGaugeChart',
      'drawInteractiveTimeSeriesChart',
      'drawTimeSeriesChart',
      'prepareChartData'
    ])
    for (const fn of Object.values(c)) expect(fn).toBeTypeOf('function')
  })
})
