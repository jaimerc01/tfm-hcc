import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { drawTimeSeriesChart } from '@/composables/charts/timeSeriesChart'
import { drawInteractiveTimeSeriesChart } from '@/composables/charts/interactiveTimeSeriesChart'
import { drawBarChart } from '@/composables/charts/barChart'
import { drawGaugeChart } from '@/composables/charts/gaugeChart'
import { drawComparisonPieChart } from '@/composables/charts/pieChart'
import { drawDualLineChart } from '@/composables/charts/dualLineChart'
import { drawHealthRingsChart, ringStatus } from '@/composables/charts/healthRingsChart'
import { drawAnnotatedTimelineChart } from '@/composables/charts/annotatedTimelineChart'

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

  describe('valores fuera de rango en amarillo', () => {
    afterEach(() => {
      document.documentElement.style.removeProperty('--chart-out-of-range')
    })

    it('pinta de amarillo solo los puntos fuera del rango recomendado', () => {
      document.documentElement.style.setProperty('--chart-out-of-range', 'rgb(255, 200, 0)')
      const el = container()
      // 90 y 100 dentro de [70,105]; 110 por encima.
      drawTimeSeriesChart(el, series, { color: 'rgb(0, 0, 255)', recommendedMin: 70, recommendedMax: 105 })
      const fills = [...el.querySelectorAll('circle')].map(c => c.getAttribute('fill'))
      expect(fills).toEqual(['rgb(0, 0, 255)', 'rgb(255, 200, 0)', 'rgb(0, 0, 255)'])
    })

    it('sin rango recomendado, todos los puntos usan el color normal', () => {
      document.documentElement.style.setProperty('--chart-out-of-range', 'rgb(255, 200, 0)')
      const el = container()
      drawTimeSeriesChart(el, series, { color: 'rgb(0, 0, 255)' })
      const fills = [...el.querySelectorAll('circle')].map(c => c.getAttribute('fill'))
      expect(fills.every(f => f === 'rgb(0, 0, 255)')).toBe(true)
    })
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

  it('pinta de amarillo los puntos fuera del rango recomendado', () => {
    document.documentElement.style.setProperty('--chart-out-of-range', 'rgb(255, 200, 0)')
    const el = container()
    drawInteractiveTimeSeriesChart(el, series, { color: 'rgb(0, 0, 255)', recommendedMin: 70, recommendedMax: 105 })
    const fills = [...el.querySelectorAll('.dot')].map(c => c.getAttribute('fill'))
    expect(fills).toEqual(['rgb(0, 0, 255)', 'rgb(255, 200, 0)', 'rgb(0, 0, 255)'])
    document.documentElement.style.removeProperty('--chart-out-of-range')
  })
})

describe('drawBarChart', () => {
  // Cada barra es un <path class="bar"> relleno con un gradiente (url(#id)); el color
  // real vive en el <stop> de ese gradiente, no en el atributo fill de la barra.
  const gradientColorOf = (el, bar) => {
    const fill = bar.getAttribute('fill')
    const id = fill.slice(fill.indexOf('#') + 1, -1)
    return el.querySelector(`#${id} stop`).getAttribute('stop-color')
  }

  it('renderiza una barra por dato', () => {
    const el = container()
    drawBarChart(el, series, { label: 'Glucosa' })
    expect(el.querySelectorAll('path.bar').length).toBe(3)
  })

  it('mensaje vacío sin datos', () => {
    const el = container()
    drawBarChart(el, [], {})
    expect(el.querySelector('.chart-empty')).toBeTruthy()
  })

  it('pinta de amarillo las barras fuera del rango recomendado', () => {
    document.documentElement.style.setProperty('--chart-out-of-range', 'rgb(255, 200, 0)')
    const el = container()
    drawBarChart(el, series, { color: 'rgb(0, 0, 255)', recommendedMin: 70, recommendedMax: 105 })
    const colors = [...el.querySelectorAll('path.bar')].map(bar => gradientColorOf(el, bar))
    expect(colors).toEqual(['rgb(0, 0, 255)', 'rgb(255, 200, 0)', 'rgb(0, 0, 255)'])
    document.documentElement.style.removeProperty('--chart-out-of-range')
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

describe('ringStatus', () => {
  it('dentro del rango: anillo cerrado', () => {
    expect(ringStatus({ value: 90, min: 70, max: 100 })).toEqual({ kind: 'in', fraction: 1 })
  })

  it('por encima del rango: fuera, con fracción proporcional', () => {
    expect(ringStatus({ value: 200, min: 70, max: 100 })).toEqual({ kind: 'out', fraction: 0.5 })
  })

  it('por debajo del rango: fuera, con fracción proporcional', () => {
    expect(ringStatus({ value: 35, min: 70, max: 100 })).toEqual({ kind: 'out', fraction: 0.5 })
  })

  it('sin valor o sin rango: estado "none"', () => {
    expect(ringStatus({ value: null, min: 70, max: 100 }).kind).toBe('none')
    expect(ringStatus({ value: 90, min: null, max: null }).kind).toBe('none')
  })
})

describe('drawHealthRingsChart', () => {
  const rings = [
    { label: 'Glucosa', value: 90, min: 70, max: 100, unit: 'mg/dL', colorToken: '--chart-series-blue' },
    { label: 'Colesterol', value: 260, min: 100, max: 200, unit: 'mg/dL', colorToken: '--chart-series-green' }
  ]

  it('dibuja un anillo (grupo) por métrica más el centro', () => {
    const el = container()
    drawHealthRingsChart(el, rings, { centerPrimary: '1/2' })
    expect(el.querySelector('svg')).toBeTruthy()
    expect(el.querySelectorAll('g.health-ring')).toHaveLength(2)
    expect(el.querySelector('text').textContent).toBe('1/2')
  })

  it('pinta en ámbar el anillo fuera de rango', () => {
    document.documentElement.style.setProperty('--chart-out-of-range', 'rgb(255, 200, 0)')
    document.documentElement.style.setProperty('--chart-series-blue', 'rgb(0, 0, 255)')
    const el = container()
    drawHealthRingsChart(el, rings, {})
    const fills = [...el.querySelectorAll('.health-ring__value')].map(p => p.getAttribute('fill'))
    expect(fills).toEqual(['rgb(0, 0, 255)', 'rgb(255, 200, 0)'])
    document.documentElement.style.removeProperty('--chart-out-of-range')
    document.documentElement.style.removeProperty('--chart-series-blue')
  })

  it('mensaje vacío sin anillos y devuelve un handle inerte', () => {
    const el = container()
    const handle = drawHealthRingsChart(el, [], { emptyMessage: 'nada' })
    expect(el.querySelector('.chart-empty')).toBeTruthy()
    expect(() => handle.setHighlight(0)).not.toThrow()
  })

  it('el handle setHighlight atenúa los demás anillos', () => {
    const el = container()
    const handle = drawHealthRingsChart(el, rings, {})
    handle.setHighlight(0)
    const opacities = [...el.querySelectorAll('g.health-ring')].map(g => g.style.opacity)
    expect(opacities[0]).toBe('1')
    expect(opacities[1]).toBe('0.3')
  })
})

describe('drawAnnotatedTimelineChart', () => {
  const events = [
    { date: new Date('2026-01-15'), kind: 'personal', label: 'Diagnóstico X' },
    { date: new Date('2026-02-20'), kind: 'familiar', label: 'Antecedente familiar Y' }
  ]

  it('renderiza la serie y una chincheta por evento', () => {
    const el = container()
    drawAnnotatedTimelineChart(el, series, events, { label: 'Glucosa', unit: 'mg/dL', recommendedMin: 70, recommendedMax: 105 })
    expect(el.querySelector('svg')).toBeTruthy()
    expect(el.querySelectorAll('.atl-pin')).toHaveLength(2)
    expect(el.querySelectorAll('.atl-guide')).toHaveLength(2)
    expect(el.querySelectorAll('.atl-dot')).toHaveLength(3)
  })

  it('funciona sin eventos', () => {
    const el = container()
    drawAnnotatedTimelineChart(el, series, [], { label: 'Glucosa' })
    expect(el.querySelector('svg')).toBeTruthy()
    expect(el.querySelectorAll('.atl-pin')).toHaveLength(0)
  })

  it('mensaje vacío sin serie', () => {
    const el = container()
    drawAnnotatedTimelineChart(el, [], events, { emptyMessage: 'sin datos' })
    expect(el.querySelector('.chart-empty')).toBeTruthy()
  })

  it('pinta de ámbar los puntos fuera del rango recomendado', () => {
    document.documentElement.style.setProperty('--chart-out-of-range', 'rgb(255, 200, 0)')
    document.documentElement.style.setProperty('--chart-default', 'rgb(0, 0, 255)')
    const el = container()
    drawAnnotatedTimelineChart(el, series, [], { recommendedMin: 70, recommendedMax: 105 })
    const fills = [...el.querySelectorAll('.atl-dot')].map(c => c.getAttribute('fill'))
    expect(fills).toEqual(['rgb(0, 0, 255)', 'rgb(255, 200, 0)', 'rgb(0, 0, 255)'])
    document.documentElement.style.removeProperty('--chart-out-of-range')
    document.documentElement.style.removeProperty('--chart-default')
  })

  it('el handle setActiveEvent resalta la guía elegida', () => {
    const el = container()
    const handle = drawAnnotatedTimelineChart(el, series, events, {})
    handle.setActiveEvent(0)
    const guideOpacities = [...el.querySelectorAll('.atl-guide')].map(g => g.getAttribute('opacity'))
    expect(guideOpacities[0]).toBe('0.9')
    expect(guideOpacities[1]).toBe('0.15')
  })
})
