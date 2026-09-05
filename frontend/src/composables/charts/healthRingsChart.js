import * as d3 from 'd3'
import { getThemeColor } from '@/utils/themeColors'
import { shouldAnimateChart } from './chartUtils'

/**
 * Devuelve el estado de un anillo a partir de su último valor y su rango.
 * @param {{value:number|null, min:number|null, max:number|null, unit:string}} ring
 * @returns {{kind:'in'|'out'|'none', fraction:number}}
 *   - fraction: cuánto se "cierra" el anillo (0..1). En rango => 1; fuera de
 *     rango => proporción a lo cerca que queda del límite superado.
 */
export function ringStatus(ring) {
  const { min, max } = ring
  if (ring.value == null || ring.value === '') return { kind: 'none', fraction: 0 }
  const v = Number(ring.value)
  if (!Number.isFinite(v)) return { kind: 'none', fraction: 0 }
  if (min == null || max == null) return { kind: 'none', fraction: 0 }
  if (v >= min && v <= max) return { kind: 'in', fraction: 1 }
  let fraction
  if (v < min) fraction = min > 0 ? Math.max(0, Math.min(1, v / min)) : 1
  else fraction = v > 0 ? Math.max(0, Math.min(1, max / v)) : 0
  return { kind: 'out', fraction }
}

/**
 * Anillos de salud: varios anillos concéntricos, uno por métrica clínica.
 * Cada anillo se "cierra" del todo cuando el último valor de esa métrica está
 * dentro de su rango recomendado; si está fuera, se llena solo en proporción a
 * lo cerca que queda del rango y se pinta con el ámbar de aviso (regla 60-30-10).
 *
 * @param {HTMLElement} container
 * @param {Array<{label:string,value:number|null,min:number|null,max:number|null,unit:string,colorToken:string}>} rings
 *   Anillos de fuera hacia dentro.
 * @param {Object} options
 *   - centerPrimary {string}   Texto grande del centro (p. ej. "3/4").
 *   - centerSecondary {string} Texto pequeño bajo el anterior.
 *   - ariaLabel {string}
 *   - emptyMessage {string}
 *   - onHoverRing {(index:number|null)=>void}
 * @returns {{ setHighlight: (index:number|null)=>void }}
 */
export function drawHealthRingsChart(container, rings, options = {}) {
  const trackColor = getThemeColor('--chart-gauge-bg')
  const outOfRangeColor = getThemeColor('--chart-out-of-range')
  const mutedColor = getThemeColor('--chart-label-muted')
  const strongColor = getThemeColor('--chart-label-strong')

  const {
    centerPrimary = '',
    centerSecondary = '',
    ariaLabel = 'Anillos de salud',
    emptyMessage = 'No hay datos disponibles.',
    onHoverRing = null
  } = options

  container.innerHTML = ''

  const list = Array.isArray(rings) ? rings : []
  if (!list.length) {
    const msg = document.createElement('div')
    msg.className = 'chart-empty'
    msg.textContent = emptyMessage
    container.appendChild(msg)
    return { setHighlight() {} }
  }

  const width = Math.max(240, Math.min(container.clientWidth || 300, 300))
  const height = width
  const compact = width < 280
  const thickness = compact ? 12 : 16
  const gap = compact ? 5 : 7
  const outerR = width / 2 - 6

  const svg = d3.select(container)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .attr('viewBox', `0 0 ${width} ${height}`)
    .attr('role', 'img')
    .attr('aria-label', ariaLabel)

  const g = svg.append('g').attr('transform', `translate(${width / 2},${height / 2})`)

  const animate = shouldAnimateChart()
  const ringGroups = []

  list.forEach((ring, i) => {
    const rOuter = outerR - i * (thickness + gap)
    const rInner = rOuter - thickness
    const ringColor = ring.colorToken ? getThemeColor(ring.colorToken) : strongColor

    const grp = g.append('g').attr('class', 'health-ring').attr('data-ring', i)

    const trackArc = d3.arc()
      .innerRadius(rInner)
      .outerRadius(rOuter)
      .startAngle(0)
      .endAngle(2 * Math.PI)
    grp.append('path').attr('d', trackArc).attr('fill', trackColor)

    const st = ringStatus(ring)
    const valueColor = st.kind === 'out'
      ? outOfRangeColor
      : st.kind === 'in' ? ringColor : mutedColor
    const targetAngle = 2 * Math.PI * (st.kind === 'none' ? 0 : st.fraction)

    if (targetAngle > 0) {
      const valArc = d3.arc()
        .innerRadius(rInner)
        .outerRadius(rOuter)
        .cornerRadius(thickness / 2)
        .startAngle(0)
      const path = grp.append('path')
        .attr('class', 'health-ring__value')
        .attr('fill', valueColor)
        .attr('d', valArc({ endAngle: targetAngle }))

      if (animate) {
        path.transition()
          .duration(900)
          .ease(d3.easeCubicOut)
          .attrTween('d', function () {
            const interp = d3.interpolate(0, targetAngle)
            return t => valArc({ endAngle: interp(t) })
          })
      }
    }

    // Corona transparente para captar el ratón sobre todo el anillo.
    const hitArc = d3.arc()
      .innerRadius(Math.max(0, rInner - gap / 2))
      .outerRadius(rOuter + gap / 2)
      .startAngle(0)
      .endAngle(2 * Math.PI)
    grp.append('path')
      .attr('d', hitArc)
      .attr('fill', 'transparent')
      .on('mouseenter', () => { setHighlight(i); if (onHoverRing) onHoverRing(i) })
      .on('mouseleave', () => { setHighlight(null); if (onHoverRing) onHoverRing(null) })
      .append('title')
      .text(ring.label)

    ringGroups.push(grp)
  })

  if (centerPrimary) {
    g.append('text')
      .attr('text-anchor', 'middle')
      .attr('dy', centerSecondary ? '-0.05em' : '0.35em')
      .style('font-size', compact ? '22px' : '26px')
      .style('font-weight', '700')
      .style('fill', strongColor)
      .text(centerPrimary)
  }
  if (centerSecondary) {
    g.append('text')
      .attr('text-anchor', 'middle')
      .attr('dy', centerPrimary ? '1.5em' : '0.35em')
      .style('font-size', '11px')
      .style('fill', mutedColor)
      .text(centerSecondary)
  }

  function setHighlight(index) {
    ringGroups.forEach((grp, i) => {
      grp.style('opacity', index == null || i === index ? 1 : 0.3)
    })
  }

  return { setHighlight }
}
