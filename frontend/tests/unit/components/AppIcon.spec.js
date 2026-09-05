import { describe, it, expect, vi, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AppIcon from '@/components/AppIcon.vue'

const mountIcon = props => mount(AppIcon, { props })

afterEach(() => {
  vi.restoreAllMocks()
})

describe('AppIcon', () => {
  it('renderiza un <svg> con las clases base y de tamaño por defecto (lg)', () => {
    const w = mountIcon({ name: 'trash' })
    const svg = w.find('svg')
    expect(svg.exists()).toBe(true)
    expect(svg.classes()).toContain('app-icon')
    expect(svg.classes()).toContain('app-icon--lg')
  })

  it('resuelve el nombre semántico al glifo de lucide correspondiente', () => {
    // lucide expone su nombre de icono como clase en el <svg> raíz
    expect(mountIcon({ name: 'trash' }).find('svg').classes()).toContain('lucide-trash-2')
    expect(mountIcon({ name: 'alert-circle' }).find('svg').classes()).toContain('lucide-circle-alert')
    expect(mountIcon({ name: 'bar-chart' }).find('svg').classes()).toContain('lucide-chart-column-big')
  })

  it('aplica la clase del tamaño indicado', () => {
    expect(mountIcon({ name: 'check', size: 'xs' }).find('svg').classes()).toContain('app-icon--xs')
    expect(mountIcon({ name: 'check', size: '3xl' }).find('svg').classes()).toContain('app-icon--3xl')
  })

  it('por defecto es decorativo: aria-hidden y sin role', () => {
    const svg = mountIcon({ name: 'info' }).find('svg')
    expect(svg.attributes('aria-hidden')).toBe('true')
    expect(svg.attributes('role')).toBeUndefined()
    expect(svg.attributes('aria-label')).toBeUndefined()
  })

  it('con label se anuncia como imagen informativa', () => {
    const svg = mountIcon({ name: 'info', label: 'Información importante' }).find('svg')
    expect(svg.attributes('role')).toBe('img')
    expect(svg.attributes('aria-label')).toBe('Información importante')
    expect(svg.attributes('aria-hidden')).toBeUndefined()
  })

  it('spin añade la clase de animación', () => {
    expect(mountIcon({ name: 'spinner', spin: true }).find('svg').classes()).toContain('app-icon--spin')
    expect(mountIcon({ name: 'spinner' }).find('svg').classes()).not.toContain('app-icon--spin')
  })

  it('propaga strokeWidth al <svg>', () => {
    expect(mountIcon({ name: 'upload', strokeWidth: 1.5 }).find('svg').attributes('stroke-width')).toBe('1.5')
  })

  it('con un nombre desconocido no renderiza nada y avisa por consola', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => {})
    const w = mountIcon({ name: 'no-existe' })
    expect(w.find('svg').exists()).toBe(false)
    expect(warn).toHaveBeenCalledWith(expect.stringContaining('no-existe'))
  })

  it('el validador de la prop size rechaza valores fuera de la escala', () => {
    const { validator } = AppIcon.props.size
    expect(validator('lg')).toBe(true)
    expect(validator('gigante')).toBe(false)
  })
})
