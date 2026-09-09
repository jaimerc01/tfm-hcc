import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'

const draws = vi.hoisted(() => ({
  drawTimeSeriesChart: vi.fn(),
  drawInteractiveTimeSeriesChart: vi.fn(),
  drawBarChart: vi.fn(),
  drawGaugeChart: vi.fn(),
  drawComparisonPieChart: vi.fn(),
  drawDualLineChart: vi.fn(),
  prepareChartData: vi.fn(() => [])
}))
vi.mock('@/composables/useChart', () => ({ useChart: () => draws }))

import DatoClinicoChart from '@/components/DatoClinicoChart.vue'

const analytes = [
  { key: 'glucosa', labelKey: 'glucose', unit: 'mg/dL', recommendedMin: 70, recommendedMax: 110 },
  { key: 'colesterol', labelKey: 'total_cholesterol', unit: 'mg/dL', recommendedMin: null, recommendedMax: null }
]
const entry = (over = {}) => ({ key: 'glucosa', value: '95', unit: 'mg/dL', createdAt: '2026-01-02T00:00:00Z', ...over })

const factory = (props = {}) => mount(DatoClinicoChart, {
  props: { entries: [], analytes, combos: [], ...props }
})

beforeEach(() => vi.clearAllMocks())

describe('DatoClinicoChart', () => {
  it('preselecciona el primer analito', () => {
    expect(factory().vm.chartParam).toBe('glucosa')
  })

  it('chartEntries filtra por la key seleccionada y ordena por fecha', () => {
    const w = factory({ entries: [entry({ createdAt: '2026-03-01' }), entry({ createdAt: '2026-01-01' }), entry({ key: 'colesterol' })] })
    expect(w.vm.chartEntries).toHaveLength(2)
    expect(new Date(w.vm.chartEntries[0].createdAt) < new Date(w.vm.chartEntries[1].createdAt)).toBe(true)
  })

  it('latestChartEntryNumericValue parsea coma decimal', () => {
    const w = factory({ entries: [entry({ value: '5,5' })] })
    expect(w.vm.latestChartEntryNumericValue).toBe(5.5)
  })

  it('latestOutOfRecommendedRange detecta valor fuera de rango', () => {
    const w = factory({ entries: [entry({ value: '200' })] })
    expect(w.vm.latestOutOfRecommendedRange).toBe(true)
  })

  it('latestOutOfRecommendedRange es false sin rangos recomendados', async () => {
    const w = factory({ entries: [entry({ key: 'colesterol', value: '999' })] })
    w.vm.chartParam = 'colesterol'
    await w.vm.$nextTick()
    expect(w.vm.latestOutOfRecommendedRange).toBe(false)
  })

  it('chartSummaryText usa el texto de "sin datos" cuando no hay entradas', () => {
    expect(factory().vm.chartSummaryText).toBeTruthy()
  })

  it('chartRows invierte el orden y añade statusText', () => {
    draws.prepareChartData.mockReturnValue([])
    const w = factory({ entries: [entry({ value: '95', createdAt: '2026-01-01' }), entry({ value: '200', createdAt: '2026-02-01' })] })
    const rows = w.vm.chartRows
    expect(rows[0].value).toBe('200')
    expect(rows[0].statusText).toBeTruthy()
  })

  describe('drawChart dispatch', () => {
    it('tipo interactive -> drawInteractiveTimeSeriesChart', () => {
      draws.prepareChartData.mockReturnValue([{ date: new Date(), value: 95 }])
      factory({ entries: [entry()] })
      expect(draws.drawInteractiveTimeSeriesChart).toHaveBeenCalled()
    })

    it('tipo line -> drawTimeSeriesChart', async () => {
      draws.prepareChartData.mockReturnValue([{ date: new Date(), value: 95 }])
      const w = factory({ entries: [entry()] })
      draws.drawTimeSeriesChart.mockClear()
      w.vm.chartType = 'line'
      w.vm.drawChart()
      expect(draws.drawTimeSeriesChart).toHaveBeenCalled()
    })

    it('tipo bar -> drawBarChart', () => {
      draws.prepareChartData.mockReturnValue([{ date: new Date(), value: 95 }])
      const w = factory({ entries: [entry()] })
      draws.drawBarChart.mockClear()
      w.vm.chartType = 'bar'
      w.vm.drawChart()
      expect(draws.drawBarChart).toHaveBeenCalled()
    })

    it('tipo gauge usa el último valor -> drawGaugeChart', () => {
      const w = factory({ entries: [entry()] })
      w.vm.chartType = 'gauge'
      w.vm.drawChart()
      expect(draws.drawGaugeChart).toHaveBeenCalled()
    })

    it('gauge sin entradas muestra el mensaje vacío', () => {
      const w = factory({ entries: [] })
      w.vm.chartType = 'gauge'
      w.vm.drawChart()
      expect(w.vm.$refs.chart.querySelector('.chart-empty')).toBeTruthy()
    })

    it('sin datos temporales muestra el mensaje vacío', () => {
      draws.prepareChartData.mockReturnValue([])
      const w = factory({ entries: [entry()] })
      w.vm.drawChart()
      expect(w.vm.$refs.chart.querySelector('.chart-empty')).toBeTruthy()
    })
  })

  describe('combos', () => {
    const combos = [{ id: 'combo-pie', labelKey: 'combo_cholesterol_ldl_hdl', type: 'pie', analyteKeys: ['glucosa', 'colesterol'] }]

    it('selectedCombo se resuelve por id y dibuja el combo', async () => {
      const w = factory({ combos, entries: [entry(), entry({ key: 'colesterol', value: '180' })] })
      w.vm.chartParam = 'combo-pie'
      await w.vm.$nextTick()
      expect(w.vm.selectedCombo).toEqual(combos[0])
      w.vm.drawChart()
      expect(draws.drawComparisonPieChart).toHaveBeenCalled()
    })

    it('combo dual-line llama a drawDualLineChart', async () => {
      draws.prepareChartData.mockReturnValue([{ date: new Date(), value: 1 }])
      const dl = [{ id: 'c-dl', labelKey: 'combo_blood_pressure', type: 'dual-line', analyteKeys: ['glucosa', 'colesterol'] }]
      const w = factory({ combos: dl, entries: [entry()] })
      w.vm.chartParam = 'c-dl'
      await w.vm.$nextTick()
      w.vm.drawChart()
      expect(draws.drawDualLineChart).toHaveBeenCalled()
    })
  })

  it('getChartTypeInfo devuelve texto por tipo y por combo', async () => {
    const w = factory()
    expect(w.vm.getChartTypeInfo()).toBeTruthy()
    w.vm.chartType = 'gauge'
    expect(w.vm.getChartTypeInfo()).toBeTruthy()
  })

  it('el botón alterna la tabla de datos accesible', async () => {
    const w = factory({ entries: [entry()] })
    expect(w.vm.showChartDataTable).toBe(false)
    await w.find('.chart-alt-actions button').trigger('click')
    expect(w.vm.showChartDataTable).toBe(true)
  })

  it('redibuja al cambiar entries (watch profundo)', async () => {
    const w = factory({ entries: [] })
    draws.drawInteractiveTimeSeriesChart.mockClear()
    draws.prepareChartData.mockReturnValue([{ date: new Date(), value: 1 }])
    await w.setProps({ entries: [entry()] })
    expect(draws.drawInteractiveTimeSeriesChart).toHaveBeenCalled()
  })

  it('limpia el contenedor del gráfico al desmontar (beforeUnmount)', () => {
    const w = factory({ entries: [entry()] })
    const el = w.vm.$refs.chart
    el.innerHTML = '<svg></svg>'
    w.unmount()
    expect(el.innerHTML).toBe('')
  })
})
