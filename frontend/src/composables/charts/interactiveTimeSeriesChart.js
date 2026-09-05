import * as d3 from 'd3'
import { getThemeColor } from '@/utils/themeColors'
import { isOutOfRecommendedRange } from './chartUtils'

/**
 * Dibuja un gráfico de series de tiempo interactivo con zoom y brush
 * @param {HTMLElement} container - Contenedor DOM
 * @param {Array} data - Array de { date, value }
 * @param {Object} options - Opciones de configuración
 */
export function drawInteractiveTimeSeriesChart(container, data, options = {}) {
  const defaultColor = getThemeColor('--chart-default')
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
    height = 400,
    ariaLabel = 'Gráfico de series de tiempo interactivo'
  } = options

  container.innerHTML = ''
  container.style.position = 'relative'

  if (!data || data.length === 0) {
    const msg = document.createElement('div')
    msg.className = 'chart-empty'
    msg.textContent = label ? `No hay datos históricos de ${label}.` : 'No hay datos disponibles.'
    container.appendChild(msg)
    return
  }

  const width = customWidth || Math.min(900, container.clientWidth || 900)
  const margin = { top: 20, right: 30, bottom: 100, left: 50 }
  const margin2 = { top: height - 70, right: 30, bottom: 30, left: 50 }
  const innerW = width - margin.left - margin.right
  const innerH = height - margin.top - margin.bottom
  const brushHeight = height - margin2.top - margin2.bottom

  // SVG principal
  const svg = d3.select(container)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .attr('role', 'img')
    .attr('aria-label', ariaLabel)

  const gradientId = `its-area-gradient-${Math.random().toString(36).slice(2)}`
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

  // Clip path para el área del gráfico
  const clipId = `clip-${Math.random().toString(36).slice(2)}`
  svg.append('defs')
    .append('clipPath')
    .attr('id', clipId)
    .append('rect')
    .attr('width', innerW)
    .attr('height', innerH)

  // Escalas principales
  const x = d3.scaleTime()
    .domain(d3.extent(data, d => d.date))
    .range([0, innerW])

  const dataMin = d3.min(data, d => d.value)
  const dataMax = d3.max(data, d => d.value)
  let domainMin = (dataMin != null) ? dataMin * 0.9 : 0
  let domainMax = (dataMax != null) ? dataMax * 1.1 : 1
  if (recommendedMin != null) domainMin = Math.min(domainMin, recommendedMin)
  if (recommendedMax != null) domainMax = Math.max(domainMax, recommendedMax)

  const y = d3.scaleLinear()
    .domain([domainMin, domainMax])
    .nice()
    .range([innerH, 0])

  // Escalas para el brush
  const x2 = d3.scaleTime()
    .domain(x.domain())
    .range([0, innerW])

  const y2 = d3.scaleLinear()
    .domain(y.domain())
    .range([brushHeight, 0])

  // Grupo principal
  const focus = svg.append('g')
    .attr('transform', `translate(${margin.left},${margin.top})`)

  // Grupo para el brush
  const context = svg.append('g')
    .attr('transform', `translate(${margin2.left},${margin2.top})`)

  // Cuadrícula horizontal recesiva
  focus.append('g')
    .attr('class', 'chart-grid-lines')
    .call(d3.axisLeft(y).tickSize(-innerW).tickFormat(''))
    .call(sel => sel.select('.domain').remove())
    .call(sel => sel.selectAll('line').attr('stroke', gridColor).attr('stroke-dasharray', '2,3'))

  // Banda de rango recomendado
  if (recommendedMin != null && recommendedMax != null) {
    const yTop = y(recommendedMax)
    const yBottom = y(recommendedMin)
    focus.append('rect')
      .attr('class', 'recommended-band')
      .attr('x', 0)
      .attr('y', Math.min(yTop, yBottom))
      .attr('width', innerW)
      .attr('height', Math.abs(yBottom - yTop))
      .attr('fill', recommendedBandColor)
      .attr('opacity', 0.35)
  }

  // Área de degradado bajo la línea
  const area = d3.area()
    .x(d => x(d.date))
    .y0(innerH)
    .y1(d => y(d.value))
    .curve(d3.curveMonotoneX)

  focus.append('path')
    .datum(data)
    .attr('class', 'area')
    .attr('clip-path', `url(#${clipId})`)
    .attr('fill', `url(#${gradientId})`)
    .attr('d', area)
    .attr('aria-hidden', 'true')

  // Línea principal
  const line = d3.line()
    .x(d => x(d.date))
    .y(d => y(d.value))
    .curve(d3.curveMonotoneX)

  focus.append('path')
    .datum(data)
    .attr('class', 'line')
    .attr('clip-path', `url(#${clipId})`)
    .attr('fill', 'none')
    .attr('stroke', color)
    .attr('stroke-width', 2.5)
    .attr('stroke-linecap', 'round')
    .attr('stroke-linejoin', 'round')
    .attr('d', line)

  // Puntos (el hover se gestiona desde el overlay de más abajo, que capta el
  // ratón sobre todo el área del gráfico: al pintarse encima en el orden del
  // DOM, un listener puesto directamente en los círculos nunca llegaría a
  // recibir el evento)
  const dots = focus.selectAll('.dot')
    .data(data)
    .enter()
    .append('circle')
    .attr('class', 'dot')
    .attr('clip-path', `url(#${clipId})`)
    .attr('cx', d => x(d.date))
    .attr('cy', d => y(d.value))
    .attr('r', 4)
    .attr('fill', d => isOutOfRecommendedRange(d.value, recommendedMin, recommendedMax) ? outOfRangeColor : color)
    .attr('stroke', pointRingColor)
    .attr('stroke-width', 1.5)

  // Tooltip
  const tooltip = d3.select(container)
    .append('div')
    .attr('class', 'chart-tooltip')

  // Crosshair vertical guía
  const crosshair = focus.append('line')
    .attr('class', 'chart-crosshair-line')
    .attr('y1', 0)
    .attr('y2', innerH)
    .attr('stroke', crosshairColor)
    .style('display', 'none')

  let activeDot = null

  function showTooltipFor(d, cx, cy) {
    if (activeDot !== d) {
      dots.filter(dd => dd === activeDot).attr('r', 4)
      dots.filter(dd => dd === d).attr('r', 6)
      activeDot = d
    }
    crosshair.attr('x1', cx).attr('x2', cx).style('display', null)
    tooltip
      .style('visibility', 'visible')
      .html(`<strong>${label}</strong>${d.value}<br/>${d.date.toLocaleString('es-ES', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })}`)
      .style('top', `${cy + margin.top - 60}px`)
      .style('left', `${cx + margin.left + 12}px`)
  }

  function hideTooltip() {
    dots.filter(dd => dd === activeDot).attr('r', 4)
    activeDot = null
    crosshair.style('display', 'none')
    tooltip.style('visibility', 'hidden')
  }

  // Overlay: capta el ratón sobre todo el área del gráfico y busca el punto
  // más cercano (patrón habitual de crosshair en D3, en vez de depender de
  // acertar justo sobre un punto, a menudo pequeño).
  const bisectDate = d3.bisector(dd => dd.date).left

  focus.append('rect')
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
      const d = (d1 && d0 && (x0 - d0.date > d1.date - x0)) ? d1 : (d0 || d1)
      if (!d) return
      showTooltipFor(d, x(d.date), y(d.value))
    })
    .on('mouseleave', hideTooltip)

  // Ejes principales
  focus.append('g')
    .attr('class', 'axis axis--x')
    .attr('transform', `translate(0,${innerH})`)
    .call(d3.axisBottom(x).ticks(6).tickFormat(d3.timeFormat('%d/%m')))

  focus.append('g')
    .call(d3.axisLeft(y))

  // Línea del brush (contexto)
  const line2 = d3.line()
    .x(d => x2(d.date))
    .y(d => y2(d.value))
    .curve(d3.curveMonotoneX)

  context.append('path')
    .datum(data)
    .attr('fill', 'none')
    .attr('stroke', color)
    .attr('stroke-width', 1.5)
    .attr('stroke-linecap', 'round')
    .attr('stroke-linejoin', 'round')
    .attr('d', line2)

  // Eje X del brush
  context.append('g')
    .attr('transform', `translate(0,${brushHeight})`)
    .call(d3.axisBottom(x2).ticks(4).tickFormat(d3.timeFormat('%d/%m')))

  // Brush para selección de rango
  const brush = d3.brushX()
    .extent([[0, 0], [innerW, brushHeight]])
    .on('brush end', brushed)

  context.append('g')
    .attr('class', 'brush')
    .call(brush)

  function brushed(event) {
    if (!event.selection) return

    const [x0, x1] = event.selection.map(x2.invert)
    x.domain([x0, x1])

    focus.select('.area')
      .attr('d', area)

    focus.select('.line')
      .attr('d', line)

    dots
      .attr('cx', d => x(d.date))
      .attr('cy', d => y(d.value))

    focus.select('.axis--x')
      .call(d3.axisBottom(x).ticks(6).tickFormat(d3.timeFormat('%d/%m')))
  }
}
