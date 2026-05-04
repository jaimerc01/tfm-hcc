import * as d3 from 'd3'
import { getThemeColor } from '@/utils/themeColors'

/**
 * Dibuja un gráfico de barras con comparación
 * @param {HTMLElement} container - Contenedor DOM
 * @param {Array} data - Array de { date, value, label }
 * @param {Object} options - Opciones de configuración
 */
export function drawBarChart(container, data, options = {}) {
  const defaultColor = getThemeColor('--chart-default')
  const recommendedBandColor = getThemeColor('--chart-recommended-band')

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

  const svg = d3.select(container)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .attr('role', 'img')
    .attr('aria-label', ariaLabel)

  const g = svg.append('g')
    .attr('transform', `translate(${margin.left},${margin.top})`)

  // Escalas
  const x = d3.scaleBand()
    .domain(data.map((d, i) => i))
    .range([0, innerW])
    .padding(0.2)

  const dataMax = d3.max(data, d => d.value)
  let domainMin = 0
  let domainMax = (dataMax != null) ? dataMax * 1.1 : 1
  if (recommendedMax != null) domainMax = Math.max(domainMax, recommendedMax)

  const y = d3.scaleLinear()
    .domain([domainMin, domainMax])
    .nice()
    .range([innerH, 0])

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
    .style('position', 'absolute')
    .style('visibility', 'hidden')
    .style('background', 'rgba(0, 0, 0, 0.8)')
    .style('color', 'white')
    .style('padding', '8px 12px')
    .style('border-radius', '6px')
    .style('font-size', '13px')
    .style('pointer-events', 'none')
    .style('z-index', '1000')

  // Barras
  g.selectAll('.bar')
    .data(data)
    .enter()
    .append('rect')
    .attr('class', 'bar')
    .attr('x', (d, i) => x(i))
    .attr('y', innerH)
    .attr('width', x.bandwidth())
    .attr('height', 0)
    .attr('fill', color)
    .style('cursor', 'pointer')
    .on('mouseover', function(event, d) {
      d3.select(this).attr('opacity', 0.7)
      tooltip
        .style('visibility', 'visible')
        .html(`<strong>${label}</strong><br/>${d.value}<br/>${d.date.toLocaleDateString('es-ES', { 
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
      d3.select(this).attr('opacity', 1)
      tooltip.style('visibility', 'hidden')
    })
    .transition()
    .duration(800)
    .attr('y', d => y(d.value))
    .attr('height', d => innerH - y(d.value))

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
