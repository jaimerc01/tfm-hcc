import * as d3 from 'd3'
import { getThemeColor } from '@/utils/themeColors'

/**
 * Gráfico de línea temporal simple
 */
export function drawTimeSeriesChart(container, data, options = {}) {
  const defaultColor = getThemeColor('--chart-emphasis')
  const recommendedBandColor = getThemeColor('--chart-recommended-band')

  const {
    label = '',
    color = defaultColor,
    recommendedMin = null,
    recommendedMax = null,
    width: customWidth = null,
    height = 200,
    ariaLabel = 'Gráfico de serie temporal'
  } = options

  // Limpiar contenedor
  container.innerHTML = ''

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

  // Línea
  const line = d3.line()
    .x(d => x(d.date))
    .y(d => y(d.value))
    .curve(d3.curveMonotoneX)

  g.append('path')
    .datum(data)
    .attr('fill', 'none')
    .attr('stroke', color)
    .attr('stroke-width', 2)
    .attr('d', line)

  // Puntos
  g.selectAll('circle')
    .data(data)
    .enter()
    .append('circle')
    .attr('cx', d => x(d.date))
    .attr('cy', d => y(d.value))
    .attr('r', 3.5)
    .attr('fill', color)
    .append('title')
    .text(d => `${d.value} — ${d.date.toLocaleString()}`)
}
