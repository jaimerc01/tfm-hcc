import * as d3 from 'd3'
import { getThemeColor } from '@/utils/themeColors'

const SLICE_COLOR_TOKENS = ['--chart-default', '--chart-emphasis']

/**
 * Dibuja un gráfico circular (donut) comparando la proporción entre varios valores
 * (p. ej. el último colesterol LDL registrado frente al último HDL).
 * @param {HTMLElement} container - Contenedor DOM
 * @param {Array} data - Array de { label, value, unit, color? }
 * @param {Object} options - Opciones de configuración
 */
export function drawComparisonPieChart(container, data, options = {}) {
  const {
    width: customWidth = null,
    height = 280,
    ariaLabel = 'Gráfico circular comparativo',
    emptyMessage = 'No hay datos disponibles.'
  } = options

  container.innerHTML = ''

  const validData = (data || []).filter(d => d && Number.isFinite(d.value) && d.value >= 0)
  const total = d3.sum(validData, d => d.value)

  if (validData.length === 0 || total <= 0) {
    const msg = document.createElement('div')
    msg.className = 'chart-empty'
    msg.textContent = emptyMessage
    container.appendChild(msg)
    return
  }

  const colors = validData.map((d, i) => d.color || getThemeColor(SLICE_COLOR_TOKENS[i % SLICE_COLOR_TOKENS.length]))

  const width = customWidth || Math.min(320, container.clientWidth || 320)
  const radius = Math.min(width, height) / 2 - 10

  const svg = d3.select(container)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .attr('role', 'img')
    .attr('aria-label', ariaLabel)

  const g = svg.append('g')
    .attr('transform', `translate(${width / 2},${height / 2})`)

  const pieLayout = d3.pie().value(d => d.value).sort(null)
  const arcGen = d3.arc().innerRadius(radius * 0.55).outerRadius(radius)
  const arcHover = d3.arc().innerRadius(radius * 0.55).outerRadius(radius + 6)

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

  const arcs = g.selectAll('path')
    .data(pieLayout(validData))
    .enter()
    .append('path')
    .attr('fill', (d, i) => colors[i])
    .style('cursor', 'pointer')
    .each(function (d) { this._current = { startAngle: d.startAngle, endAngle: d.startAngle } })
    .on('mouseover', function (event, d) {
      d3.select(this).transition().duration(150).attr('d', arcHover)
      const pct = Math.round((d.data.value / total) * 100)
      tooltip
        .style('visibility', 'visible')
        .html(`<strong>${d.data.label}</strong><br/>${d.data.value} ${d.data.unit || ''} (${pct}%)`)
    })
    .on('mousemove', function (event) {
      tooltip.style('top', (event.pageY - 60) + 'px').style('left', (event.pageX + 10) + 'px')
    })
    .on('mouseout', function () {
      d3.select(this).transition().duration(150).attr('d', arcGen)
      tooltip.style('visibility', 'hidden')
    })

  arcs.transition()
    .duration(800)
    .attrTween('d', function (d) {
      const interpolate = d3.interpolate(this._current, d)
      this._current = interpolate(0)
      return t => arcGen(interpolate(t))
    })

  // Leyenda con etiqueta, valor y porcentaje
  const legend = d3.select(container)
    .append('div')
    .attr('class', 'chart-legend')

  validData.forEach((d, i) => {
    const pct = Math.round((d.value / total) * 100)
    const item = legend.append('div').attr('class', 'chart-legend-item')
    item.append('span').attr('class', 'chart-legend-dot').style('background', colors[i])
    item.append('span').attr('class', 'chart-legend-label').text(d.label)
    item.append('span').attr('class', 'chart-legend-value').text(`${d.value} ${d.unit || ''} (${pct}%)`)
  })
}
