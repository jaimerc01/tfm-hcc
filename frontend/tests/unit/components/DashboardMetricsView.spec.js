import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const draws = vi.hoisted(() => ({
  drawTimeSeriesChart: vi.fn(),
  drawInteractiveTimeSeriesChart: vi.fn(),
  drawBarChart: vi.fn(),
  drawGaugeChart: vi.fn(),
  prepareChartData: vi.fn(() => [])
}))
vi.mock('@/composables/useChart', () => ({ useChart: () => draws }))

const svc = vi.hoisted(() => ({ getMine: vi.fn(), getRangos: vi.fn() }))
vi.mock('@/services/historiaClinicaService', () => ({ default: svc }))

import DashboardMetricsView from '@/components/DashboardMetricsView.vue'

beforeEach(() => {
  vi.clearAllMocks()
  svc.getMine.mockResolvedValue({ data: {} })
  svc.getRangos.mockResolvedValue({ data: [] })
})

const factory = async () => {
  const w = mount(DashboardMetricsView)
  await flushPromises()
  return w
}

describe('DashboardMetricsView', () => {
  it('al montarse carga rangos, datos y pinta 6 gauges', async () => {
    await factory()
    expect(svc.getRangos).toHaveBeenCalled()
    expect(svc.getMine).toHaveBeenCalled()
    expect(draws.drawGaugeChart).toHaveBeenCalledTimes(6)
  })

  it('loadRangos asigna rangos recomendados por alias', async () => {
    svc.getRangos.mockResolvedValue({ data: [{ nombre: 'Glucosa', valorInferiorNumerico: 70, valorSuperiorNumerico: 100 }] })
    const w = await factory()
    expect(w.vm.metrics.glucosa.recommendedMin).toBe(70)
    expect(w.vm.metrics.glucosa.recommendedMax).toBe(100)
  })

  it('parsea analisisSangre (string JSON) en entries', async () => {
    svc.getMine.mockResolvedValue({ data: { analisisSangre: JSON.stringify([{ tipo: 'Glucosa', valor: '95', fechaCreacion: '2026-01-01' }]) } })
    const w = await factory()
    expect(w.vm.entries[0]).toMatchObject({ key: 'glucosa', value: '95' })
  })

  it('getLatestValue devuelve el valor más reciente parseado', async () => {
    svc.getMine.mockResolvedValue({ data: { analisisSangre: [
      { key: 'glucosa', value: '90', createdAt: '2026-01-01' },
      { key: 'glucosa', value: '110', createdAt: '2026-02-01' }
    ] } })
    const w = await factory()
    expect(w.vm.getLatestValue('glucosa')).toBe(110)
  })

  it('getLatestValue devuelve null si no hay entradas de esa métrica', async () => {
    const w = await factory()
    expect(w.vm.getLatestValue('creatinina')).toBeNull()
  })

  it('mapTipoToKey resuelve alias y descarta paréntesis', async () => {
    const w = await factory()
    expect(w.vm.mapTipoToKey('Colesterol total (suero)')).toBe('colesterol')
    expect(w.vm.mapTipoToKey(null)).toBeNull()
  })

  it('updateChart con datos usa el dibujante según chartType', async () => {
    draws.prepareChartData.mockReturnValue([{ date: new Date(), value: 95 }])
    const w = await factory()
    draws.drawInteractiveTimeSeriesChart.mockClear()
    w.vm.updateChart()
    expect(draws.drawInteractiveTimeSeriesChart).toHaveBeenCalled()
    w.vm.chartType = 'bar'
    w.vm.updateChart()
    expect(draws.drawBarChart).toHaveBeenCalled()
    w.vm.chartType = 'line'
    w.vm.updateChart()
    expect(draws.drawTimeSeriesChart).toHaveBeenCalled()
  })

  it('updateChart sin datos pinta el mensaje vacío', async () => {
    draws.prepareChartData.mockReturnValue([])
    const w = await factory()
    w.vm.updateChart()
    expect(w.vm.$refs.mainChart.querySelector('.chart-empty')).toBeTruthy()
  })

  it('selectedMetricLabel refleja la métrica seleccionada', async () => {
    const w = await factory()
    w.vm.selectedMetric = 'hemoglobina'
    await w.vm.$nextTick()
    expect(w.vm.selectedMetricLabel).toBe('Hemoglobina')
  })

  it('el info-box muestra el rango recomendado o "no definido"', async () => {
    const w = await factory()
    expect(w.find('.info-box').text().toLowerCase()).toMatch(/no definido|not defined|—/i)
  })

  it('no rompe si falla la carga de rangos o de datos', async () => {
    svc.getRangos.mockRejectedValueOnce(new Error('r'))
    svc.getMine.mockRejectedValueOnce(new Error('d'))
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
    const w = await factory()
    expect(w.exists()).toBe(true)
    spy.mockRestore()
  })
})
