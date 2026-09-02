import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AdminView from '@/views/AdminView.vue'
import HomeView from '@/views/HomeView.vue'
import PoliticaCookiesView from '@/views/PoliticaCookiesView.vue'
import PoliticaPrivacidadView from '@/views/PoliticaPrivacidadView.vue'

const stubs = { RouterLink: { template: '<a><slot /></a>', props: ['to'] } }

describe('vistas estáticas', () => {
  it('AdminView muestra el mensaje de acceso y el error si lo hay', async () => {
    const w = mount(AdminView, { global: { stubs } })
    expect(w.find('h1').text()).toBeTruthy()
    expect(w.find('.error').exists()).toBe(false)
    w.vm.error = 'fallo'
    await w.vm.$nextTick()
    expect(w.find('.error').text()).toBe('fallo')
  })

  it('HomeView muestra título, subtítulo y enlace a login', () => {
    const w = mount(HomeView, { global: { stubs } })
    expect(w.find('h1').text()).toBeTruthy()
    expect(w.findComponent(stubs.RouterLink).props('to')).toBe('/login')
  })

  it('PoliticaCookiesView renderiza las tres secciones', () => {
    const w = mount(PoliticaCookiesView)
    expect(w.findAll('section')).toHaveLength(3)
    expect(w.find('h1').text()).toBeTruthy()
  })

  it('PoliticaPrivacidadView renderiza todas las secciones RGPD', () => {
    const w = mount(PoliticaPrivacidadView)
    expect(w.findAll('section').length).toBeGreaterThanOrEqual(10)
    expect(w.text().toLowerCase()).toContain('privacidad')
  })
})
