import * as d3 from 'd3'
import { getThemeColor } from '@/utils/themeColors'

/**
 * Dibuja un gráfico de dos líneas temporales combinadas en los mismos ejes
 * (p. ej. presión arterial sistólica y diastólica).
 * @param {HTMLElement} container - Contenedor DOM
 * @param {Array} seriesA - Array de { date, value } para la primera serie
 * @param {Array} seriesB - Array de { date, value } para la segunda serie
 * @param {Object} options - Opciones de configuración
 */
export function drawDualLineChart(container, seriesA, seriesB, options = {}) {
  const colorA = options.colorA || getThemeColor('--chart-default')
  const colorB = options.colorB || getThemeColor('--chart-emphasis')

  const {
    labelA = '',
    labelB = '',
    unit = '',
    width: customWidth = null,
    height = 300,
    ariaLabel = 'Gráfico de dos líneas combinadas',
    emptyMessage = 'No hay datos disponibles.'
  } = options

  container.innerHTML = ''

  const dataA = seriesA || []
  const dataB = seriesB || []

  if (dataA.length === 0 && dataB.length === 0) {
    const msg = document.createElement('div')
    msg.className = 'chart-empty'
    msg.textContent = emptyMessage
    container.appendChild(msg)
    return
  }

  const width = customWidth || Math.min(700, container.clientWidth || 700)
  const margin = { top: 20, right: 20, bottom: 30, left: 50 }
  const innerW = width - margin.left - margin.right
  const innerH = height - margin.top - margin.bottom

  const svg = d3.select(container)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .attr('role', 'img')
    .attr('aria-label', ariaLabel)

  const allPoints = [...dataA, ...dataB]
  const x = d3.scaleTime()
    .domain(d3.extent(allPoints, d => d.date))
    .range([0, innerW])

  const dataMin = d3.min(allPoints, d => d.value)
  const dataMax = d3.max(allPoints, d => d.value)
  const y = d3.scaleLinear()
    .domain([(dataMin != null) ? dataMin * 0.9 : 0, (dataMax != null) ? dataMax * 1.1 : 1])
    .nice()
    .range([innerH, 0])

  const g = svg.append('g')
    .attr('transform', `translate(${margin.left},${margin.top})`)

  g.append('g')
    .attr('transform', `translate(0,${innerH})`)
    .call(d3.axisBottom(x)
      .ticks(Math.min(6, allPoints.length || 1))
      .tickFormat(d3.timeFormat('%d/%m %H:%M')))

  g.append('g').call(d3.axisLeft(y))

  const line = d3.line()
    .x(d => x(d.date))
    .y(d => y(d.value))
    .curve(d3.curveMonotoneX)

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

  function drawSeries (data, color, seriesLabel) {
    if (!data.length) return

    g.append('path')
      .datum(data)
      .attr('fill', 'none')
      .attr('stroke', color)
      .attr('stroke-width', 2)
      .attr('d', line)

    g.selectAll(null)
      .data(data)
      .enter()
      .append('circle')
      .attr('cx', d => x(d.date))
      .attr('cy', d => y(d.value))
      .attr('r', 3.5)
      .attr('fill', color)
      .style('cursor', 'pointer')
      .on('mouseover', function (event, d) {
        tooltip
          .style('visibility', 'visible')
          .html(`<strong>${seriesLabel}</strong><br/>${d.value} ${unit}<br/>${d.date.toLocaleString()}`)
      })
      .on('mousemove', function (event) {
        tooltip.style('top', (event.pageY - 60) + 'px').style('left', (event.pageX + 10) + 'px')
      })
      .on('mouseout', function () {
        tooltip.style('visibility', 'hidden')
      })
  }

  drawSeries(dataA, colorA, labelA)
  drawSeries(dataB, colorB, labelB)

  // Leyenda
  const legend = d3.select(container)
    .append('div')
    .attr('class', 'chart-legend')

  ;[[colorA, labelA], [colorB, labelB]].forEach(([color, text]) => {
    if (!text) return
    const item = legend.append('div').attr('class', 'chart-legend-item')
    item.append('span').attr('class', 'chart-legend-dot').style('background', color)
    item.append('span').attr('class', 'chart-legend-label').text(text)
  })
}
