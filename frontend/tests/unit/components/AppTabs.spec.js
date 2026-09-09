import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AppTabs from '@/components/AppTabs.vue'

const TABS = [
  { id: 'general', label: 'General' },
  { id: 'alergias', label: 'Alergias' },
  { id: 'analisis', label: 'Análisis' }
]

const montar = (modelValue = 'general') =>
  mount(AppTabs, { props: { tabs: TABS, modelValue, ariaLabel: 'Secciones del historial' } })

describe('AppTabs', () => {
  it('marca como activa la pestaña indicada por modelValue', () => {
    const w = montar('alergias')
    const activa = w.get('#tab-alergias')
    expect(activa.classes()).toContain('active')
    expect(activa.attributes('aria-selected')).toBe('true')
    expect(activa.attributes('tabindex')).toBe('0')
    expect(w.get('#tab-general').attributes('tabindex')).toBe('-1')
  })

  it('al pulsar una pestaña emite update:modelValue con su id', async () => {
    const w = montar('general')
    await w.get('#tab-analisis').trigger('click')
    expect(w.emitted('update:modelValue')[0]).toEqual(['analisis'])
  })

  describe('navegación por teclado', () => {
    it('ArrowRight pasa a la siguiente pestaña y ArrowLeft a la anterior (con envoltura)', async () => {
      const w = montar('general')
      await w.get('#tab-general').trigger('keydown', { key: 'ArrowRight' })
      expect(w.emitted('update:modelValue')[0]).toEqual(['alergias'])

      await w.get('#tab-general').trigger('keydown', { key: 'ArrowLeft' })
      expect(w.emitted('update:modelValue')[1]).toEqual(['analisis'])
    })

    it('Home salta a la primera pestaña y End a la última', async () => {
      const w = montar('alergias')
      await w.get('#tab-alergias').trigger('keydown', { key: 'Home' })
      expect(w.emitted('update:modelValue')[0]).toEqual(['general'])

      await w.get('#tab-alergias').trigger('keydown', { key: 'End' })
      expect(w.emitted('update:modelValue')[1]).toEqual(['analisis'])
    })

    it('otras teclas no hacen nada', async () => {
      const w = montar('general')
      await w.get('#tab-general').trigger('keydown', { key: 'Enter' })
      expect(w.emitted('update:modelValue')).toBeUndefined()
    })

    it('mueve el foco al botón de la pestaña destino', async () => {
      const w = montar('general', { attachTo: document.body })
      const attached = mount(AppTabs, {
        props: { tabs: TABS, modelValue: 'general', ariaLabel: 'x' },
        attachTo: document.body
      })
      await attached.get('#tab-general').trigger('keydown', { key: 'ArrowRight' })
      await attached.vm.$nextTick()
      expect(document.activeElement).toBe(attached.get('#tab-alergias').element)
      attached.unmount()
      w.unmount()
    })
  })

  describe('desplazamiento horizontal', () => {
    it('muestra los botones de scroll según el estado de desbordamiento', async () => {
      const w = montar()
      const nav = w.get({ ref: 'tabsNav' }).element
      Object.defineProperty(nav, 'scrollLeft', { value: 50, writable: true, configurable: true })
      Object.defineProperty(nav, 'clientWidth', { value: 100, configurable: true })
      Object.defineProperty(nav, 'scrollWidth', { value: 400, configurable: true })

      w.vm.updateScrollState()
      await w.vm.$nextTick()

      expect(w.vm.canScrollLeft).toBe(true)
      expect(w.vm.canScrollRight).toBe(true)
      expect(w.find('.tabs-scroll-btn').exists()).toBe(true)
    })

    it('scrollLeft y scrollRight desplazan el contenedor', () => {
      const w = montar()
      const nav = w.get({ ref: 'tabsNav' }).element
      nav.scrollBy = vi.fn()

      w.vm.scrollLeft()
      w.vm.scrollRight()

      expect(nav.scrollBy).toHaveBeenNthCalledWith(1, { left: -200, behavior: 'smooth' })
      expect(nav.scrollBy).toHaveBeenNthCalledWith(2, { left: 200, behavior: 'smooth' })
    })

    it('updateScrollState no falla si el contenedor aún no existe', () => {
      const w = montar()
      w.unmount()
      expect(() => w.vm.updateScrollState()).not.toThrow()
    })
  })

  it('recalcula el estado de scroll cuando cambia la lista de pestañas', async () => {
    const w = montar()
    const spy = vi.spyOn(w.vm, 'updateScrollState')
    await w.setProps({ tabs: [...TABS, { id: 'nuevo', label: 'Nuevo' }] })
    await w.vm.$nextTick()
    expect(spy).toHaveBeenCalled()
  })

  it('quita el listener de resize al desmontarse', () => {
    const remove = vi.spyOn(window, 'removeEventListener')
    montar().unmount()
    expect(remove).toHaveBeenCalledWith('resize', expect.any(Function))
  })
})
