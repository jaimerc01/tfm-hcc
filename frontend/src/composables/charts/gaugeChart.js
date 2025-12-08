import * as d3 from 'd3'

/**
 * Dibuja un gauge (medidor circular) para mostrar el valor actual vs rango
 * @param {HTMLElement} container - Contenedor DOM
 * @param {number} value - Valor actual
 * @param {Object} options - Opciones de configuración
 */
export function drawGaugeChart(container, value, options = {}) {
  const {
    label = '',
    min = 0,
    max = 100,
    recommendedMin = null,
    recommendedMax = null,
    unit = '',
    width: customWidth = null,
    height = 200,
    ariaLabel = 'Medidor de valor'
  } = options

  container.innerHTML = ''

  if (value == null || isNaN(value)) {
    const msg = document.createElement('div')
    msg.className = 'chart-empty'
    msg.textContent = 'No hay valor disponible'
    container.appendChild(msg)
    return
  }

  const width = customWidth || Math.min(300, container.clientWidth || 300)
  const radius = Math.min(width, height) / 2 - 10
  const arcWidth = 20

  const svg = d3.select(container)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .attr('role', 'img')
    .attr('aria-label', ariaLabel)

  const g = svg.append('g')
    .attr('transform', `translate(${width / 2},${height / 2})`)

  // Escala angular
  const angle = d3.scaleLinear()
    .domain([min, max])
    .range([-Math.PI * 0.75, Math.PI * 0.75])
    .clamp(true)

  // Arco de fondo (gris claro)
  const backgroundArc = d3.arc()
    .innerRadius(radius - arcWidth)
    .outerRadius(radius)
    .startAngle(-Math.PI * 0.75)
    .endAngle(Math.PI * 0.75)

  g.append('path')
    .attr('d', backgroundArc)
    .attr('fill', '#e5e7eb')

  // Dibujar zona verde del rango recomendado si existe
  if (recommendedMin != null && recommendedMax != null) {
    const minAngle = angle(recommendedMin)
    const maxAngle = angle(recommendedMax)
    const innerR = radius - arcWidth
    const outerR = radius
    
    // Arco verde del rango recomendado
    const recommendedArc = d3.arc()
      .innerRadius(innerR)
      .outerRadius(outerR)
      .startAngle(minAngle)
      .endAngle(maxAngle)
    
    g.append('path')
      .attr('d', recommendedArc)
      .attr('fill', '#16a34a')
      .attr('opacity', 0.3)
      .attr('stroke', '#16a34a')
      .attr('stroke-width', 2)
      .style('pointer-events', 'none')
      .raise() // Mover al frente para que se vea encima
  }

  // Determinar color según el rango
  let fillColor = '#0284c7'
  let statusText = ''
  
  if (recommendedMin != null && recommendedMax != null) {
    if (value < recommendedMin) {
      fillColor = '#dc2626' // Rojo si está fuera de rango
      statusText = 'Bajo'
    } else if (value > recommendedMax) {
      fillColor = '#dc2626' // Rojo si está fuera de rango
      statusText = 'Alto'
    } else {
      fillColor = '#16a34a' // Verde si está en rango
      statusText = 'Normal'
    }
  }

  // Arco de valor con animación
  const valueArc = d3.arc()
    .innerRadius(radius - arcWidth)
    .outerRadius(radius)
    .startAngle(-Math.PI * 0.75)

  const arcPath = g.append('path')
    .datum({ endAngle: -Math.PI * 0.75 })
    .attr('d', valueArc)
    .attr('fill', fillColor)

  arcPath.transition()
    .duration(1000)
    .attrTween('d', function(d) {
      const interpolate = d3.interpolate(d.endAngle, angle(value))
      return function(t) {
        d.endAngle = interpolate(t)
        return valueArc(d)
      }
    })

  // Texto del valor
  g.append('text')
    .attr('text-anchor', 'middle')
    .attr('dy', '0.5em')
    .style('font-size', '32px')
    .style('font-weight', 'bold')
    .style('fill', fillColor)
    .text(value)

  // Unidad
  g.append('text')
    .attr('text-anchor', 'middle')
    .attr('dy', '2.5em')
    .style('font-size', '14px')
    .style('fill', '#6b7280')
    .text(unit)

  // Label
  if (label) {
    g.append('text')
      .attr('text-anchor', 'middle')
      .attr('dy', '-2em')
      .style('font-size', '16px')
      .style('font-weight', '600')
      .style('fill', '#374151')
      .text(label)
  }

  // Texto informativo del estado (ya definido en statusText)
  if (statusText) {
    g.append('text')
      .attr('text-anchor', 'middle')
      .attr('dy', '4em')
      .style('font-size', '14px')
      .style('font-weight', '700')
      .style('fill', fillColor)
      .text(statusText)
  }
}
