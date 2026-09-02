import { describe, it, expect, beforeEach } from 'vitest'
import { drawTimeSeriesChart } from '@/composables/charts/timeSeriesChart'
import { drawInteractiveTimeSeriesChart } from '@/composables/charts/interactiveTimeSeriesChart'
import { drawBarChart } from '@/composables/charts/barChart'
import { drawGaugeChart } from '@/composables/charts/gaugeChart'
import { drawComparisonPieChart } from '@/composables/charts/pieChart'
import { drawDualLineChart } from '@/composables/charts/dualLineChart'

// jsdom no implementa layout: forzamos getBBox para que los ejes de D3 no fallen.
beforeEach(() => {
  if (!SVGElement.prototype.getBBox) {
    SVGElement.prototype.getBBox = () => ({ x: 0, y: 0, width: 100, height: 20 })
  }
})

const container = () => {
  const el = document.createElement('div')
  document.body.appendChild(el)
  return el
}

const series = [
  { date: new Date('2026-01-01'), value: 90 },
  { date: new Date('2026-02-01'), value: 110 },
  { date: new Date('2026-03-01'), value: 100 }
]

describe('drawTimeSeriesChart', () => {
  it('renderiza un SVG con línea y puntos', () => {
    const el = container()
    drawTimeSeriesChart(el, series, { label: 'Glucosa', recommendedMin: 70, recommendedMax: 110, ariaLabel: 'graf' })
    const svg = el.querySelector('svg')
    expect(svg).toBeTruthy()
    expect(svg.getAttribute('aria-label')).toBe('graf')
    expect(el.querySelectorAll('circle').length).toBe(3)
    expect(el.querySelector('path')).toBeTruthy()
  })

  it('muestra el mensaje vacío sin datos', () => {
    const el = container()
    drawTimeSeriesChart(el, [], { label: 'Glucosa' })
    expect(el.querySelector('.chart-empty')).toBeTruthy()
    expect(el.querySelector('svg')).toBeFalsy()
  })
})

describe('drawInteractiveTimeSeriesChart', () => {
  it('renderiza un SVG', () => {
    const el = container()
    drawInteractiveTimeSeriesChart(el, series, { label: 'Glucosa', height: 300 })
    expect(el.querySelector('svg')).toBeTruthy()
  })

  it('mensaje vacío sin datos', () => {
    const el = container()
    drawInteractiveTimeSeriesChart(el, [], {})
    expect(el.querySelector('.chart-empty')).toBeTruthy()
  })
})

describe('drawBarChart', () => {
  it('renderiza una barra por dato', () => {
    const el = container()
    drawBarChart(el, series, { label: 'Glucosa' })
    expect(el.querySelectorAll('rect.bar, rect').length).toBeGreaterThanOrEqual(3)
  })

  it('mensaje vacío sin datos', () => {
    const el = container()
    drawBarChart(el, [], {})
    expect(el.querySelector('.chart-empty')).toBeTruthy()
  })
})

describe('drawGaugeChart', () => {
  it('renderiza un SVG para un valor numérico', () => {
    const el = container()
    drawGaugeChart(el, 95, { label: 'Glucosa', min: 0, max: 300, recommendedMin: 70, recommendedMax: 110 })
    expect(el.querySelector('svg')).toBeTruthy()
  })

  it('gestiona un valor nulo sin lanzar', () => {
    const el = container()
    expect(() => drawGaugeChart(el, null, { label: 'Glucosa', min: 0, max: 300 })).not.toThrow()
  })
})

describe('drawComparisonPieChart', () => {
  it('renderiza sectores para varios valores', () => {
    const el = container()
    drawComparisonPieChart(el, [
      { label: 'LDL', value: 120, unit: 'mg/dL' },
      { label: 'HDL', value: 50, unit: 'mg/dL' }
    ], { ariaLabel: 'pie' })
    expect(el.querySelector('svg')).toBeTruthy()
    expect(el.querySelectorAll('path').length).toBeGreaterThanOrEqual(2)
  })
})

describe('drawDualLineChart', () => {
  it('renderiza dos líneas', () => {
    const el = container()
    drawDualLineChart(el, series, series.map(s => ({ ...s, value: s.value - 30 })), { labelA: 'Sistólica', labelB: 'Diastólica', unit: 'mmHg' })
    expect(el.querySelector('svg')).toBeTruthy()
    expect(el.querySelectorAll('path').length).toBeGreaterThanOrEqual(2)
  })

  it('mensaje vacío si ambas series están vacías', () => {
    const el = container()
    drawDualLineChart(el, [], [], { labelA: 'A', labelB: 'B' })
    expect(el.querySelector('.chart-empty')).toBeTruthy()
  })
})
