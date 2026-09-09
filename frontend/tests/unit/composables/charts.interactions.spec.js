import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { drawTimeSeriesChart } from '@/composables/charts/timeSeriesChart'
import { drawInteractiveTimeSeriesChart } from '@/composables/charts/interactiveTimeSeriesChart'
import { drawComparisonPieChart } from '@/composables/charts/pieChart'
import { drawDualLineChart } from '@/composables/charts/dualLineChart'

/**
 * Cubre los manejadores de ratón (tooltip, crosshair, resaltado de puntos) de los gráficos
 * D3. jsdom no calcula layout, así que `d3.pointer` devuelve coordenadas nulas, pero eso basta
 * para ejecutar el código de los callbacks y comprobar sus efectos sobre el DOM.
 */

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

const fireMouse = (node, type) => node.dispatchEvent(new MouseEvent(type, { bubbles: true, clientX: 40, clientY: 20 }))

describe('timeSeriesChart — interacción con el ratón', () => {
  afterEach(() => document.querySelectorAll('.chart-tooltip').forEach(t => t.remove()))

  it('mousemove sobre el overlay muestra el tooltip y agranda el punto activo', () => {
    const el = container()
    drawTimeSeriesChart(el, series, { label: 'Glucosa' })
    const overlay = el.querySelector('.chart-overlay')
    expect(overlay).toBeTruthy()

    fireMouse(overlay, 'mousemove')

    const tooltip = document.querySelector('.chart-tooltip')
    expect(tooltip.style.visibility).toBe('visible')
    expect(tooltip.innerHTML).toContain('Glucosa')
    expect([...el.querySelectorAll('circle')].some(c => c.getAttribute('r') === '6')).toBe(true)
  })

  it('mouseleave oculta el tooltip y el crosshair', () => {
    const el = container()
    drawTimeSeriesChart(el, series, { label: 'Glucosa' })
    const overlay = el.querySelector('.chart-overlay')

    fireMouse(overlay, 'mousemove')
    fireMouse(overlay, 'mouseleave')

    expect(document.querySelector('.chart-tooltip').style.visibility).toBe('hidden')
    expect([...el.querySelectorAll('circle')].every(c => c.getAttribute('r') === '4')).toBe(true)
  })
})

describe('interactiveTimeSeriesChart — interacción con el ratón', () => {
  afterEach(() => document.querySelectorAll('.chart-tooltip').forEach(t => t.remove()))

  it('mousemove sobre el overlay muestra el tooltip', () => {
    const el = container()
    drawInteractiveTimeSeriesChart(el, series, { label: 'Glucosa', height: 300 })
    const overlay = el.querySelector('.chart-overlay')

    fireMouse(overlay, 'mousemove')

    expect(document.querySelector('.chart-tooltip').style.visibility).toBe('visible')
  })

  it('mouseleave oculta el tooltip', () => {
    const el = container()
    drawInteractiveTimeSeriesChart(el, series, { label: 'Glucosa' })
    const overlay = el.querySelector('.chart-overlay')

    fireMouse(overlay, 'mousemove')
    fireMouse(overlay, 'mouseleave')

    expect(document.querySelector('.chart-tooltip').style.visibility).toBe('hidden')
  })
})

describe('dualLineChart — interacción con el ratón', () => {
  afterEach(() => document.querySelectorAll('.chart-tooltip').forEach(t => t.remove()))

  it('mouseover sobre un punto muestra el tooltip con las dos etiquetas', () => {
    const el = container()
    drawDualLineChart(el, series, series.map(s => ({ ...s, value: s.value - 30 })),
      { labelA: 'Sistólica', labelB: 'Diastólica', unit: 'mmHg' })
    const punto = el.querySelector('circle')

    fireMouse(punto, 'mouseover')
    expect(document.querySelector('.chart-tooltip').style.visibility).toBe('visible')

    fireMouse(punto, 'mouseout')
    expect(document.querySelector('.chart-tooltip').style.visibility).toBe('hidden')
  })
})

describe('drawComparisonPieChart', () => {
  afterEach(() => document.querySelectorAll('.chart-tooltip').forEach(t => t.remove()))

  it('muestra el mensaje vacío cuando no hay valores positivos', () => {
    const el = container()
    drawComparisonPieChart(el, [{ label: 'LDL', value: 0 }], { emptyMessage: 'sin datos' })
    expect(el.querySelector('.chart-empty')).toBeTruthy()
    expect(el.querySelector('svg')).toBeFalsy()
  })

  it('ignora entradas con valores no numéricos o negativos', () => {
    const el = container()
    drawComparisonPieChart(el, [
      { label: 'LDL', value: 120 },
      { label: 'Roto', value: -5 },
      { label: 'NaN', value: Number.NaN }
    ], {})
    expect(el.querySelectorAll('path.arc, path').length).toBeGreaterThanOrEqual(1)
  })

  it('mouseover sobre un sector muestra el porcentaje en el tooltip', () => {
    const el = container()
    drawComparisonPieChart(el, [
      { label: 'LDL', value: 75, unit: 'mg/dL' },
      { label: 'HDL', value: 25, unit: 'mg/dL' }
    ], {})
    const sector = el.querySelector('svg path')

    fireMouse(sector, 'mouseover')
    const tooltip = document.querySelector('.chart-tooltip')
    expect(tooltip.style.visibility).toBe('visible')
    expect(tooltip.innerHTML).toMatch(/%/)

    fireMouse(sector, 'mousemove')
    fireMouse(sector, 'mouseout')
    expect(document.querySelector('.chart-tooltip').style.visibility).toBe('hidden')
  })
})
