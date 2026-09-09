import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { i18n } from '../setup'

vi.mock('@/components/Navbar.vue', () => ({ default: { name: 'AppNavbar', template: '<nav class="navbar-stub" />' } }))
vi.mock('@/components/AppFooter.vue', () => ({ default: { name: 'AppFooter', template: '<footer class="footer-stub" />' } }))

import App from '@/App.vue'

const stubs = { RouterView: { template: '<div class="router-view-stub" />' } }
const mountApp = (meta = {}) => mount(App, {
  global: { stubs, mocks: { $route: { meta } } }
})

beforeEach(() => {
  vi.clearAllMocks()
  localStorage.clear()
  i18n.global.locale.value = 'es'
})

describe('App', () => {
  it('muestra navbar y footer en rutas normales', () => {
    const w = mountApp({})
    expect(w.find('.navbar-stub').exists()).toBe(true)
    expect(w.find('.footer-stub').exists()).toBe(true)
    expect(w.find('.language-switch').exists()).toBe(false)
  })

  it('oculta navbar/footer y muestra el selector de idioma en rutas de invitado', () => {
    const w = mountApp({ requiresGuest: true })
    expect(w.find('.navbar-stub').exists()).toBe(false)
    expect(w.find('.language-switch').exists()).toBe(true)
  })

  it('setLanguage cambia el locale, lo persiste y recarga', async () => {
    const reload = vi.fn()
    vi.stubGlobal('location', { ...window.location, reload })
    const w = mountApp({ requiresGuest: true })
    const glBtn = w.findAll('.language-switch__option').find(b => b.text().toLowerCase().includes('gal') || b.text().toLowerCase().includes('gall'))
    await glBtn.trigger('click')
    expect(localStorage.getItem('app-locale')).toBe('gl')
    expect(reload).toHaveBeenCalled()
    vi.unstubAllGlobals()
  })

  it('setLanguage no hace nada si el idioma ya está activo', async () => {
    const reload = vi.fn()
    vi.stubGlobal('location', { ...window.location, reload })
    const w = mountApp({ requiresGuest: true })
    const esBtn = w.findAll('.language-switch__option').find(b => b.text().toLowerCase().includes('esp'))
    await esBtn.trigger('click')
    expect(reload).not.toHaveBeenCalled()
    vi.unstubAllGlobals()
  })
})
