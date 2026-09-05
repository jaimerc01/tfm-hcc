import * as d3 from 'd3'
import { getThemeColor } from '@/utils/themeColors'
import { isOutOfRecommendedRange, shouldAnimateChart } from './chartUtils'

/**
 * Línea de tiempo clínica anotada: la evolución de un parámetro cuantitativo
 * (línea con área de degradado, banda de rango recomendado y puntos fuera de
 * rango en ámbar) con marcadores verticales numerados sobre los eventos
 * clínicos (antecedentes) situados en su fecha. Pensada para que un médico vea
 * si una tendencia cambia después de un evento registrado.
 *
 * @param {HTMLElement} container
 * @param {Array<{date:Date, value:number}>} series
 * @param {Array<{date:Date, kind:'personal'|'familiar', label:string}>} events
 * @param {Object} options
 *   - label {string}
 *   - unit {string}
 *   - recommendedMin {number|null}
 *   - recommendedMax {number|null}
 *   - width {number|null}
 *   - height {number}
 *   - ariaLabel {string}
 *   - emptyMessage {string}
 *   - onHoverEvent {(index:number|null)=>void}
 * @returns {{ setActiveEvent: (index:number|null)=>void }}
 */
export function drawAnnotatedTimelineChart(container, series, events, options = {}) {
  const lineColor = getThemeColor('--chart-default')
  const recommendedBandColor = getThemeColor('--chart-recommended-band')
  const outOfRangeColor = getThemeColor('--chart-out-of-range')
  const gridColor = getThemeColor('--chart-grid')
  const pointRingColor = getThemeColor('--chart-point-ring')
  const eventColor = getThemeColor('--chart-series-slate')

  const {
    label = '',
    unit = '',
    recommendedMin = null,
    recommendedMax = null,
    width: customWidth = null,
    height = 340,
    ariaLabel = 'Línea de tiempo clínica',
    emptyMessage = 'No hay datos disponibles.',
    onHoverEvent = null
  } = options

  container.innerHTML = ''
  container.style.position = 'relative'

  const data = (series || []).filter(d => d && d.date instanceof Date && Number.isFinite(d.value))
  if (!data.length) {
    const msg = document.createElement('div')
    msg.className = 'chart-empty'
    msg.textContent = emptyMessage
    container.appendChild(msg)
    return { setActiveEvent() {} }
  }

  const evts = (events || [])
    .filter(e => e && e.date instanceof Date && !Number.isNaN(e.date.getTime()))
    .slice()
    .sort((a, b) => a.date - b.date)

  const width = customWidth || Math.min(820, container.clientWidth || 820)
  const laneH = 54
  const margin = { top: 10 + laneH, right: 22, bottom: 30, left: 50 }
  const innerW = width - margin.left - margin.right
  const innerH = height - margin.top - margin.bottom

  const svg = d3.select(container)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .attr('role', 'img')
    .attr('aria-label', ariaLabel)

  const gradientId = `atl-area-${Math.random().toString(36).slice(2)}`
  svg.append('defs')
    .append('linearGradient')
    .attr('id', gradientId)
    .attr('x1', '0').attr('y1', '0')
    .attr('x2', '0').attr('y2', '1')
    .selectAll('stop')
    .data([
      { offset: '0%', opacity: 0.26 },
      { offset: '100%', opacity: 0.02 }
    ])
    .enter()
    .append('stop')
    .attr('offset', d => d.offset)
    .attr('stop-color', lineColor)
    .attr('stop-opacity', d => d.opacity)

  // El dominio X abarca tanto las medidas como los eventos, para que un evento
  // anterior a la primera medida (p. ej. un diagnóstico) siga siendo visible.
  const allDates = data.map(d => d.date).concat(evts.map(e => e.date))
  const x = d3.scaleTime()
    .domain([d3.min(allDates), d3.max(allDates)])
    .range([0, innerW])

  const dataMin = d3.min(data, d => d.value)
  const dataMax = d3.max(data, d => d.value)
  let domainMin = dataMin * 0.9
  let domainMax = dataMax * 1.1
  if (recommendedMin != null) domainMin = Math.min(domainMin, recommendedMin)
  if (recommendedMax != null) domainMax = Math.max(domainMax, recommendedMax)

  const y = d3.scaleLinear().domain([domainMin, domainMax]).nice().range([innerH, 0])

  const g = svg.append('g').attr('transform', `translate(${margin.left},${margin.top})`)

  // Cuadrícula horizontal recesiva.
  g.append('g')
    .attr('class', 'chart-grid-lines')
    .call(d3.axisLeft(y).tickSize(-innerW).tickFormat(''))
    .call(sel => sel.select('.domain').remove())
    .call(sel => sel.selectAll('line').attr('stroke', gridColor).attr('stroke-dasharray', '2,3'))

  g.append('g')
    .attr('transform', `translate(0,${innerH})`)
    .call(d3.axisBottom(x).ticks(Math.min(6, data.length)).tickFormat(d3.timeFormat('%d/%m/%y')))

  g.append('g').call(d3.axisLeft(y))

  // Banda de rango recomendado.
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
      .attr('aria-hidden', 'true')
  }

  // Reparto de las chinchetas en la banda superior: cuando varias caen juntas
  // en el eje X se escalonan en varias filas y se abren un poco en abanico para
  // que ninguna quede totalmente tapada.
  const pinR = 10
  const staggerRows = 3
  const layout = []
  let clusterStart = 0
  evts.forEach((e, i) => {
    const px = x(e.date)
    if (i > 0 && (px - x(evts[i - 1].date)) >= pinR * 1.9) clusterStart = i
    const posInCluster = i - clusterStart
    const row = posInCluster % staggerRows
    const fan = Math.floor(posInCluster / staggerRows)
    layout.push({
      cx: px + fan * pinR * 0.9,
      cy: -laneH + 14 + row * (pinR * 1.15)
    })
  })

  // Guías verticales de los eventos (por debajo de la línea de datos).
  const guides = g.append('g').attr('class', 'atl-guides')
  evts.forEach((e, i) => {
    guides.append('line')
      .attr('class', 'atl-guide')
      .attr('data-event', i)
      .attr('x1', layout[i].cx)
      .attr('x2', x(e.date))
      .attr('y1', layout[i].cy)
      .attr('y2', innerH)
      .attr('stroke', eventColor)
      .attr('stroke-dasharray', '3,3')
      .attr('opacity', 0.3)
  })

  // Área + línea.
  const area = d3.area()
    .x(d => x(d.date))
    .y0(innerH)
    .y1(d => y(d.value))
    .curve(d3.curveMonotoneX)
  g.append('path').datum(data).attr('fill', `url(#${gradientId})`).attr('d', area).attr('aria-hidden', 'true')

  const line = d3.line().x(d => x(d.date)).y(d => y(d.value)).curve(d3.curveMonotoneX)
  const linePath = g.append('path')
    .datum(data)
    .attr('fill', 'none')
    .attr('stroke', lineColor)
    .attr('stroke-width', 2.5)
    .attr('stroke-linecap', 'round')
    .attr('stroke-linejoin', 'round')
    .attr('d', line)

  if (shouldAnimateChart()) {
    let total = 0
    try {
      total = typeof linePath.node().getTotalLength === 'function' ? linePath.node().getTotalLength() : 0
    } catch (e) {
      total = 0
    }
    if (total) {
      linePath
        .attr('stroke-dasharray', `${total} ${total}`)
        .attr('stroke-dashoffset', total)
        .transition()
        .duration(700)
        .ease(d3.easeCubicOut)
        .attr('stroke-dashoffset', 0)
        .on('end', () => linePath.attr('stroke-dasharray', null))
    }
  }

  g.selectAll('.atl-dot')
    .data(data)
    .enter()
    .append('circle')
    .attr('class', 'atl-dot')
    .attr('cx', d => x(d.date))
    .attr('cy', d => y(d.value))
    .attr('r', 4)
    .attr('fill', d => isOutOfRecommendedRange(d.value, recommendedMin, recommendedMax) ? outOfRangeColor : lineColor)
    .attr('stroke', pointRingColor)
    .attr('stroke-width', 1.5)
    .append('title')
    .text(d => `${label ? label + ': ' : ''}${d.value} ${unit} — ${d.date.toLocaleDateString('es-ES')}`.trim())

  // Chinchetas numeradas de los eventos en la banda superior.
  const pins = g.append('g').attr('class', 'atl-pins')
  evts.forEach((e, i) => {
    const { cx, cy } = layout[i]

    const pin = pins.append('g')
      .attr('class', 'atl-pin')
      .attr('data-event', i)
      .style('cursor', 'default')
      .on('mouseenter', () => { setActiveEvent(i); if (onHoverEvent) onHoverEvent(i) })
      .on('mouseleave', () => { setActiveEvent(null); if (onHoverEvent) onHoverEvent(null) })

    pin.append('circle')
      .attr('cx', cx)
      .attr('cy', cy)
      .attr('r', pinR)
      .attr('fill', e.kind === 'familiar' ? pointRingColor : eventColor)
      .attr('stroke', eventColor)
      .attr('stroke-width', 1.5)

    pin.append('text')
      .attr('x', cx)
      .attr('y', cy)
      .attr('text-anchor', 'middle')
      .attr('dominant-baseline', 'central')
      .style('font-size', '11px')
      .style('font-weight', '700')
      .style('fill', e.kind === 'familiar' ? eventColor : pointRingColor)
      .text(i + 1)

    pin.append('title').text(e.label)
  })

  function setActiveEvent(index) {
    guides.selectAll('.atl-guide')
      .attr('opacity', function () {
        if (index == null) return 0.35
        return Number(this.getAttribute('data-event')) === index ? 0.9 : 0.15
      })
    pins.selectAll('.atl-pin')
      .attr('opacity', function () {
        if (index == null) return 1
        return Number(this.getAttribute('data-event')) === index ? 1 : 0.4
      })
  }

  return { setActiveEvent }
}
