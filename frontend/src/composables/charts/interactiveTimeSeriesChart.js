import * as d3 from 'd3'
import { getThemeColor } from '@/utils/themeColors'

/**
 * Dibuja un gráfico de series de tiempo interactivo con zoom y brush
 * @param {HTMLElement} container - Contenedor DOM
 * @param {Array} data - Array de { date, value }
 * @param {Object} options - Opciones de configuración
 */
export function drawInteractiveTimeSeriesChart(container, data, options = {}) {
  const defaultColor = getThemeColor('--chart-default')
  const recommendedBandColor = getThemeColor('--chart-recommended-band')

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

  // Clip path para el área del gráfico
  svg.append('defs')
    .append('clipPath')
    .attr('id', `clip-${Date.now()}`)
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

  // Banda de rango recomendado
  if (recommendedMin != null && recommendedMax != null) {
    const yTop = y(recommendedMax)
    const yBottom = y(recommendedMin)
    focus.append('rect')
      .attr('x', 0)
      .attr('y', Math.min(yTop, yBottom))
      .attr('width', innerW)
      .attr('height', Math.abs(yBottom - yTop))
      .attr('fill', recommendedBandColor)
      .attr('opacity', 0.35)
  }

  // Línea principal
  const line = d3.line()
    .x(d => x(d.date))
    .y(d => y(d.value))
    .curve(d3.curveMonotoneX)

  focus.append('path')
    .datum(data)
    .attr('class', 'line')
    .attr('fill', 'none')
    .attr('stroke', color)
    .attr('stroke-width', 2)
    .attr('d', line)

  // Puntos con hover
  const dots = focus.selectAll('.dot')
    .data(data)
    .enter()
    .append('circle')
    .attr('class', 'dot')
    .attr('cx', d => x(d.date))
    .attr('cy', d => y(d.value))
    .attr('r', 4)
    .attr('fill', color)
    .style('cursor', 'pointer')

  // Tooltip
  const tooltip = d3.select(container)
    .append('div')
    .attr('class', 'chart-tooltip')
    .style('position', 'absolute')
    .style('visibility', 'hidden')
    .style('background', 'rgba(0, 0, 0, 0.8)')
    .style('color', 'white')
    .style('padding', '8px 12px')
    .style('border-radius', '6px')
    .style('font-size', '13px')
    .style('pointer-events', 'none')
    .style('z-index', '1000')

  dots
    .on('mouseover', function(event, d) {
      d3.select(this).attr('r', 6)
      tooltip
        .style('visibility', 'visible')
        .html(`<strong>${label}</strong><br/>${d.value}<br/>${d.date.toLocaleString('es-ES', { 
          year: 'numeric', 
          month: 'short', 
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        })}`)
    })
    .on('mousemove', function(event) {
      tooltip
        .style('top', (event.pageY - 60) + 'px')
        .style('left', (event.pageX + 10) + 'px')
    })
    .on('mouseout', function() {
      d3.select(this).attr('r', 4)
      tooltip.style('visibility', 'hidden')
    })

  // Ejes principales
  focus.append('g')
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
    
    focus.select('.line')
      .attr('d', line)
    
    dots
      .attr('cx', d => x(d.date))
      .attr('cy', d => y(d.value))
    
    focus.select('.axis--x')
      .call(d3.axisBottom(x).ticks(6).tickFormat(d3.timeFormat('%d/%m')))
  }
}
