import * as d3 from 'd3'
import { getThemeColor } from '@/utils/themeColors'
import { isOutOfRecommendedRange } from './chartUtils'

/**
 * Gráfico de línea temporal simple
 */
export function drawTimeSeriesChart(container, data, options = {}) {
  const defaultColor = getThemeColor('--chart-emphasis')
  const recommendedBandColor = getThemeColor('--chart-recommended-band')
  const outOfRangeColor = getThemeColor('--chart-out-of-range')
  const gridColor = getThemeColor('--chart-grid')
  const pointRingColor = getThemeColor('--chart-point-ring')
  const crosshairColor = getThemeColor('--chart-crosshair')

  const {
    label = '',
    color = defaultColor,
    recommendedMin = null,
    recommendedMax = null,
    width: customWidth = null,
    height = 220,
    ariaLabel = 'Gráfico de serie temporal'
  } = options

  // Limpiar contenedor
  container.innerHTML = ''
  container.style.position = 'relative'

  // Validar datos
  if (!data || data.length === 0) {
    const msg = document.createElement('div')
    msg.className = 'chart-empty'
    msg.textContent = label ? `No hay datos históricos de ${label}.` : 'No hay datos disponibles.'
    container.appendChild(msg)
    return
  }

  // Configuración de dimensiones
  const width = customWidth || Math.min(700, container.clientWidth || 700)
  const margin = { top: 10, right: 20, bottom: 30, left: 50 }
  const innerW = width - margin.left - margin.right
  const innerH = height - margin.top - margin.bottom

  // Crear SVG
  const svg = d3.select(container)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .attr('role', 'img')
    .attr('aria-label', ariaLabel)

  const gradientId = `ts-area-gradient-${Math.random().toString(36).slice(2)}`
  svg.append('defs')
    .append('linearGradient')
    .attr('id', gradientId)
    .attr('x1', '0').attr('y1', '0')
    .attr('x2', '0').attr('y2', '1')
    .selectAll('stop')
    .data([
      { offset: '0%', opacity: 0.28 },
      { offset: '100%', opacity: 0.02 }
    ])
    .enter()
    .append('stop')
    .attr('offset', d => d.offset)
    .attr('stop-color', color)
    .attr('stop-opacity', d => d.opacity)

  // Escala X (tiempo)
  const x = d3.scaleTime()
    .domain(d3.extent(data, d => d.date))
    .range([0, innerW])

  // Escala Y (valores)
  const dataMin = d3.min(data, d => d.value)
  const dataMax = d3.max(data, d => d.value)
  let domainMin = (dataMin != null) ? dataMin * 0.9 : 0
  let domainMax = (dataMax != null) ? dataMax * 1.1 : 1

  // Extender dominio con rangos recomendados si existen
  if (recommendedMin != null) domainMin = Math.min(domainMin, recommendedMin)
  if (recommendedMax != null) domainMax = Math.max(domainMax, recommendedMax)

  const y = d3.scaleLinear()
    .domain([domainMin, domainMax])
    .nice()
    .range([innerH, 0])

  // Grupo principal
  const g = svg.append('g')
    .attr('transform', `translate(${margin.left},${margin.top})`)

  // Cuadrícula horizontal recesiva
  g.append('g')
    .attr('class', 'chart-grid-lines')
    .call(d3.axisLeft(y).tickSize(-innerW).tickFormat(''))
    .call(sel => sel.select('.domain').remove())
    .call(sel => sel.selectAll('line').attr('stroke', gridColor).attr('stroke-dasharray', '2,3'))

  // Eje X
  g.append('g')
    .attr('transform', `translate(0,${innerH})`)
    .call(d3.axisBottom(x)
      .ticks(Math.min(6, data.length))
      .tickFormat(d3.timeFormat('%d/%m %H:%M')))

  // Eje Y
  g.append('g')
    .call(d3.axisLeft(y))

  // Banda de rango recomendado (si existe)
  if (recommendedMin != null && recommendedMax != null) {
    const yTop = y(recommendedMax)
    const yBottom = y(recommendedMin)
    const rectY = Math.min(yTop, yBottom)
    const rectH = Math.abs(yBottom - yTop)

    g.append('rect')
      .attr('x', 0)
      .attr('y', rectY)
      .attr('width', innerW)
      .attr('height', rectH)
      .attr('fill', recommendedBandColor)
      .attr('opacity', 0.35)
      .attr('aria-hidden', 'true')
  }

  // Área de degradado bajo la línea
  const area = d3.area()
    .x(d => x(d.date))
    .y0(innerH)
    .y1(d => y(d.value))
    .curve(d3.curveMonotoneX)

  g.append('path')
    .datum(data)
    .attr('fill', `url(#${gradientId})`)
    .attr('d', area)
    .attr('aria-hidden', 'true')

  // Línea
  const line = d3.line()
    .x(d => x(d.date))
    .y(d => y(d.value))
    .curve(d3.curveMonotoneX)

  g.append('path')
    .datum(data)
    .attr('fill', 'none')
    .attr('stroke', color)
    .attr('stroke-width', 2.5)
    .attr('stroke-linecap', 'round')
    .attr('stroke-linejoin', 'round')
    .attr('d', line)

  // Puntos
  const dots = g.selectAll('.dot')
    .data(data)
    .enter()
    .append('circle')
    .attr('class', 'dot')
    .attr('cx', d => x(d.date))
    .attr('cy', d => y(d.value))
    .attr('r', 4)
    .attr('fill', d => isOutOfRecommendedRange(d.value, recommendedMin, recommendedMax) ? outOfRangeColor : color)
    .attr('stroke', pointRingColor)
    .attr('stroke-width', 1.5)

  // El hover se gestiona desde el overlay de más abajo (pintado encima de los
  // puntos), que muestra un tooltip + crosshair siguiendo al punto más cercano
  // -- un listener puesto directamente en los círculos nunca lo recibiría.

  // Crosshair interactivo
  const crosshair = g.append('line')
    .attr('class', 'chart-crosshair-line')
    .attr('y1', 0)
    .attr('y2', innerH)
    .attr('stroke', crosshairColor)
    .style('display', 'none')

  const tooltip = d3.select(container)
    .append('div')
    .attr('class', 'chart-tooltip')

  const bisectDate = d3.bisector(d => d.date).left
  let activeDot = null

  g.append('rect')
    .attr('class', 'chart-overlay')
    .attr('width', innerW)
    .attr('height', innerH)
    .attr('fill', 'transparent')
    .style('cursor', 'crosshair')
    .on('mousemove', function (event) {
      const [mx] = d3.pointer(event, this)
      const x0 = x.invert(mx)
      let i = bisectDate(data, x0, 1)
      i = Math.min(i, data.length - 1)
      const d0 = data[i - 1]
      const d1 = data[i]
      const d = (d1 && (x0 - d0.date > d1.date - x0)) ? d1 : d0
      if (!d) return

      if (activeDot !== d) {
        dots.filter(dd => dd === activeDot).attr('r', 4)
        dots.filter(dd => dd === d).attr('r', 6)
        activeDot = d
      }

      crosshair
        .attr('x1', x(d.date))
        .attr('x2', x(d.date))
        .style('display', null)

      tooltip
        .style('visibility', 'visible')
        .html(`<strong>${label}</strong>${d.value}<br/>${d.date.toLocaleString('es-ES', {
          year: 'numeric',
          month: 'short',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        })}`)
        .style('top', `${y(d.value) + margin.top - 60}px`)
        .style('left', `${x(d.date) + margin.left + 12}px`)
    })
    .on('mouseleave', function () {
      dots.filter(dd => dd === activeDot).attr('r', 4)
      activeDot = null
      crosshair.style('display', 'none')
      tooltip.style('visibility', 'hidden')
    })
}
