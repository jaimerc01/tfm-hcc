import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import AppFooter from '@/components/AppFooter.vue'

const stubs = { RouterLink: { template: '<a><slot /></a>', props: ['to'] } }

describe('AppFooter', () => {
  it('muestra los enlaces de privacidad y cookies traducidos', () => {
    const w = mount(AppFooter, { global: { stubs, mocks: { $router: { push: vi.fn() } } } })
    expect(w.text().toLowerCase()).toContain('privacidad')
    expect(w.text().toLowerCase()).toContain('cookies')
    expect(w.find('.app-footer__button').exists()).toBe(true)
  })

  it('el enlace de privacidad apunta a la ruta PoliticaPrivacidad', () => {
    const w = mount(AppFooter, { global: { stubs, mocks: { $router: { push: vi.fn() } } } })
    expect(w.findComponent(stubs.RouterLink).props('to')).toEqual({ name: 'PoliticaPrivacidad' })
  })

  it('el botón de cookies navega a la ruta PoliticaCookies', async () => {
    const push = vi.fn()
    const w = mount(AppFooter, { global: { stubs, mocks: { $router: { push } } } })
    await w.find('.app-footer__button').trigger('click')
    expect(push).toHaveBeenCalledWith({ name: 'PoliticaCookies' })
  })
})
