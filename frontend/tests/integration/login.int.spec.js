import { describe, it, expect, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

import { useIntegrationServer, server, API, backend } from './server'
import { makeRouter } from './router'
import { http, HttpResponse } from 'msw'

// Vistas y lógica REALES: LoginView + useAuth + authService + vue-router.
// Solo se intercepta la red (MSW). Ningún mock de módulos.
import LoginView from '@/views/auth/LoginView.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'

useIntegrationServer()

beforeEach(() => {
  localStorage.clear()
})

async function montarLogin(router) {
  const w = mount(LoginView, {
    global: {
      plugins: [router],
      stubs: { RouterLink: { template: '<a><slot/></a>', props: ['to'] } }
    }
  })
  await flushPromises()
  return w
}

describe('integración: flujo de login', () => {
  it('credenciales válidas → guarda el token y navega a /dashboard', async () => {
    const router = makeRouter([], '/login')
    await router.isReady()
    const w = await montarLogin(router)

    await w.find('#nif').setValue('12345678Z')
    await w.find('#password').setValue('Secreto123')
    await w.find('form').trigger('submit')
    await flushPromises()

    expect(localStorage.getItem('authToken')).toBe(backend.tokenValido)
    expect(router.currentRoute.value.path).toBe('/dashboard')
  })

  it('el backend responde 401 → muestra error y no navega', async () => {
    server.use(http.post(`${API}/authentication/login`, () => new HttpResponse(null, { status: 401 })))

    const router = makeRouter([], '/login')
    await router.isReady()
    const w = await montarLogin(router)

    await w.find('#nif').setValue('12345678Z')
    await w.find('#password').setValue('loquesea1')
    await w.find('form').trigger('submit')
    await flushPromises()

    expect(localStorage.getItem('authToken')).toBeNull()
    expect(router.currentRoute.value.path).toBe('/login')
    expect(w.find('.error-message').exists()).toBe(true)
  })

  it('usuario con 2FA → pasa al paso del segundo factor y lo completa', async () => {
    server.use(http.post(`${API}/authentication/login`, () =>
      HttpResponse.json({ requiresTwoFactor: true, challengeId: 'challenge-1' })))

    const router = makeRouter([], '/login')
    await router.isReady()
    const w = await montarLogin(router)

    await w.find('#nif').setValue('12345678Z')
    await w.find('#password').setValue('Secreto123')
    await w.find('form').trigger('submit')
    await flushPromises()

    expect(w.vm.step).toBe('twoFactor')
    expect(router.currentRoute.value.path).toBe('/login')

    await w.find('#two-factor-code').setValue('123456')
    await w.find('form').trigger('submit')
    await flushPromises()

    expect(localStorage.getItem('authToken')).toBe(backend.tokenValido)
    expect(router.currentRoute.value.path).toBe('/dashboard')
  })

  it('tras el login, DashboardView pide el nombre al backend y lo muestra', async () => {
    localStorage.setItem('authToken', backend.tokenValido)
    const router = makeRouter([], '/dashboard')
    await router.isReady()

    const w = mount(DashboardView, {
      global: { plugins: [router], stubs: { RouterLink: true } }
    })
    await flushPromises()

    expect(w.text()).toContain('Ana López')
  })

  it('un fallo de red en el login se traduce a un mensaje de error', async () => {
    server.use(http.post(`${API}/authentication/login`, () => HttpResponse.error()))

    const router = makeRouter([], '/login')
    await router.isReady()
    const w = await montarLogin(router)

    await w.find('#nif').setValue('12345678Z')
    await w.find('#password').setValue('Secreto123')
    await w.find('form').trigger('submit')
    await flushPromises()

    expect(w.find('.error-message').exists()).toBe(true)
    expect(router.currentRoute.value.path).toBe('/login')
  })
})
