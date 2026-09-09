import * as d3 from 'd3'
import { getThemeColor } from '@/utils/themeColors'
import { isOutOfRecommendedRange } from './chartUtils'

/**
 * Dibuja un gráfico de barras con comparación
 * @param {HTMLElement} container - Contenedor DOM
 * @param {Array} data - Array de { date, value, label }
 * @param {Object} options - Opciones de configuración
 */
export function drawBarChart(container, data, options = {}) {
  const defaultColor = getThemeColor('--chart-default')
  const recommendedBandColor = getThemeColor('--chart-recommended-band')
  const outOfRangeColor = getThemeColor('--chart-out-of-range')
  const gridColor = getThemeColor('--chart-grid')

  const {
    label = '',
    color = defaultColor,
    recommendedMin = null,
    recommendedMax = null,
    width: customWidth = null,
    height = 300,
    ariaLabel = 'Gráfico de barras'
  } = options

  container.innerHTML = ''

  if (!data || data.length === 0) {
    const msg = document.createElement('div')
    msg.className = 'chart-empty'
    msg.textContent = label ? `No hay datos de ${label}.` : 'No hay datos disponibles.'
    container.appendChild(msg)
    return
  }

  const width = customWidth || Math.min(700, container.clientWidth || 700)
  const margin = { top: 20, right: 30, bottom: 60, left: 50 }
  const innerW = width - margin.left - margin.right
  const innerH = height - margin.top - margin.bottom
  const barRadius = 5

  const svg = d3.select(container)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .attr('role', 'img')
    .attr('aria-label', ariaLabel)

  const barGradientId = `bar-gradient-${Math.random().toString(36).slice(2)}`
  const outOfRangeGradientId = `bar-gradient-oor-${Math.random().toString(36).slice(2)}`
  const defs = svg.append('defs')
  ;[[barGradientId, color], [outOfRangeGradientId, outOfRangeColor]].forEach(([id, barColor]) => {
    defs.append('linearGradient')
      .attr('id', id)
      .attr('x1', '0').attr('y1', '0')
      .attr('x2', '0').attr('y2', '1')
      .selectAll('stop')
      .data([
        { offset: '0%', opacity: 0.85 },
        { offset: '100%', opacity: 1 }
      ])
      .enter()
      .append('stop')
      .attr('offset', d => d.offset)
      .attr('stop-color', barColor)
      .attr('stop-opacity', d => d.opacity)
  })

  const g = svg.append('g')
    .attr('transform', `translate(${margin.left},${margin.top})`)

  // Escalas
  const x = d3.scaleBand()
    .domain(data.map((d, i) => i))
    .range([0, innerW])
    .padding(0.3)

  const dataMax = d3.max(data, d => d.value)
  let domainMin = 0
  let domainMax = (dataMax != null) ? dataMax * 1.1 : 1
  if (recommendedMax != null) domainMax = Math.max(domainMax, recommendedMax)

  const y = d3.scaleLinear()
    .domain([domainMin, domainMax])
    .nice()
    .range([innerH, 0])

  // Cuadrícula horizontal recesiva
  g.append('g')
    .attr('class', 'chart-grid-lines')
    .call(d3.axisLeft(y).tickSize(-innerW).tickFormat(''))
    .call(sel => sel.select('.domain').remove())
    .call(sel => sel.selectAll('line').attr('stroke', gridColor).attr('stroke-dasharray', '2,3'))

  // Banda de rango recomendado
  if (recommendedMin != null && recommendedMax != null) {
    const yTop = y(recommendedMax)
    const yBottom = y(recommendedMin)
    g.append('rect')
      .attr('x', 0)
      .attr('y', Math.min(yTop, yBottom))
      .attr('width', innerW)
      .attr('height', Math.abs(yBottom - yTop))
      .attr('fill', recommendedBandColor)
      .attr('opacity', 0.35)
  }

  // Tooltip
  const tooltip = d3.select(container)
    .append('div')
    .attr('class', 'chart-tooltip')

  // Barras (con esquinas superiores redondeadas mediante un path)
  function barPath(xPos, yPos, w, h, r) {
    const radius = Math.min(r, w / 2, h)
    if (h <= 0) return `M${xPos},${yPos + h} h${w} v0 h-${w} Z`
    return `M${xPos},${yPos + h}
      L${xPos},${yPos + radius}
      Q${xPos},${yPos} ${xPos + radius},${yPos}
      L${xPos + w - radius},${yPos}
      Q${xPos + w},${yPos} ${xPos + w},${yPos + radius}
      L${xPos + w},${yPos + h}
      Z`
  }

  g.selectAll('.bar')
    .data(data)
    .enter()
    .append('path')
    .attr('class', 'bar')
    .attr('d', d => barPath(x(data.indexOf(d)), innerH, x.bandwidth(), 0, barRadius))
    .attr('fill', d => `url(#${isOutOfRecommendedRange(d.value, recommendedMin, recommendedMax) ? outOfRangeGradientId : barGradientId})`)
    .style('cursor', 'pointer')
    .on('mouseover', function(event, d) {
      tooltip
        .style('visibility', 'visible')
        .html(`<strong>${label}</strong>${d.value}<br/>${d.date.toLocaleDateString('es-ES', {
          year: 'numeric',
          month: 'short',
          day: 'numeric'
        })}`)
    })
    .on('mousemove', function(event) {
      tooltip
        .style('top', (event.pageY - 60) + 'px')
        .style('left', (event.pageX + 10) + 'px')
    })
    .on('mouseout', function() {
      tooltip.style('visibility', 'hidden')
    })
    .transition()
    .duration(800)
    .attrTween('d', function (d) {
      const i = data.indexOf(d)
      const targetY = y(d.value)
      const targetH = innerH - targetY
      const interpolateH = d3.interpolate(0, targetH)
      return t => {
        const h = interpolateH(t)
        return barPath(x(i), innerH - h, x.bandwidth(), h, barRadius)
      }
    })

  // Ejes
  g.append('g')
    .attr('transform', `translate(0,${innerH})`)
    .call(d3.axisBottom(x).tickFormat((d, i) => {
      const date = data[i].date
      return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' })
    }))
    .selectAll('text')
    .attr('transform', 'rotate(-45)')
    .style('text-anchor', 'end')

  g.append('g')
    .call(d3.axisLeft(y))
}
