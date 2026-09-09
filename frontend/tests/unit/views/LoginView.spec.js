import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const authComposable = vi.hoisted(() => ({ login: vi.fn(), loginTwoFactor: vi.fn() }))
vi.mock('@/composables/useAuth', () => ({ useAuth: () => authComposable }))

const authService = vi.hoisted(() => ({ redirectToGoogleLogin: vi.fn() }))
vi.mock('@/services/authService', () => ({ default: authService }))

const push = vi.hoisted(() => vi.fn())
const replace = vi.hoisted(() => vi.fn())
const route = vi.hoisted(() => ({ query: {} }))
vi.mock('vue-router', () => ({ useRouter: () => ({ push, replace }), useRoute: () => route }))

import LoginView from '@/views/auth/LoginView.vue'

const stubs = { RouterLink: { template: '<a><slot /></a>', props: ['to'] } }

beforeEach(() => {
  vi.clearAllMocks()
  route.query = {}
})

const factory = async () => {
  const w = mount(LoginView, { global: { stubs } })
  await flushPromises()
  return w
}

describe('LoginView', () => {
  it('normalizeNif pasa a mayúsculas y quita espacios', async () => {
    const w = await factory()
    w.vm.credentials.nif = ' 12345678z '
    w.vm.normalizeNif()
    expect(w.vm.credentials.nif).toBe('12345678Z')
  })

  it('validateNifField marca NIF inválido', async () => {
    const w = await factory()
    w.vm.credentials.nif = 'malo'
    expect(w.vm.validateNifField()).toBe(false)
    expect(w.vm.fieldErrors.nif).toBeTruthy()
  })

  it('validatePasswordField exige contraseña', async () => {
    const w = await factory()
    expect(w.vm.validatePasswordField()).toBe(false)
    w.vm.credentials.password = 'x'
    expect(w.vm.validatePasswordField()).toBe(true)
  })

  it('handleLogin no llama al servicio si la validación falla', async () => {
    const w = await factory()
    await w.vm.handleLogin()
    expect(authComposable.login).not.toHaveBeenCalled()
  })

  it('handleLogin correcto navega a /dashboard', async () => {
    authComposable.login.mockResolvedValueOnce({})
    const w = await factory()
    w.vm.credentials.nif = '12345678Z'
    w.vm.credentials.password = 'secret'
    await w.vm.handleLogin()
    await flushPromises()
    expect(push).toHaveBeenCalledWith('/dashboard')
  })

  it('handleLogin con requiresTwoFactor cambia al paso twoFactor', async () => {
    authComposable.login.mockResolvedValueOnce({ requiresTwoFactor: true, challengeId: 'c1' })
    const w = await factory()
    w.vm.credentials.nif = '12345678Z'
    w.vm.credentials.password = 'secret'
    await w.vm.handleLogin()
    await flushPromises()
    expect(w.vm.step).toBe('twoFactor')
  })

  it('handleLogin muestra el error del backend', async () => {
    authComposable.login.mockRejectedValueOnce(new Error('Credenciales inválidas'))
    const w = await factory()
    w.vm.credentials.nif = '12345678Z'
    w.vm.credentials.password = 'secret'
    await w.vm.handleLogin()
    await flushPromises()
    expect(w.vm.error).toBe('Credenciales inválidas')
  })

  it('handleGoogleLogin redirige al backend', async () => {
    const w = await factory()
    w.vm.handleGoogleLogin()
    expect(authService.redirectToGoogleLogin).toHaveBeenCalled()
  })

  it('handleTwoFactorSubmit valida el formato de 6 dígitos', async () => {
    const w = await factory()
    w.vm.step = 'twoFactor'
    w.vm.twoFactorCode = '12'
    await w.vm.handleTwoFactorSubmit()
    expect(w.vm.twoFactorError).toBeTruthy()
    expect(authComposable.loginTwoFactor).not.toHaveBeenCalled()
  })

  it('handleTwoFactorSubmit correcto navega a /dashboard', async () => {
    authComposable.loginTwoFactor.mockResolvedValueOnce({ token: 't' })
    const w = await factory()
    w.vm.step = 'twoFactor'
    w.vm.twoFactorCode = '123456'
    await w.vm.handleTwoFactorSubmit()
    await flushPromises()
    expect(push).toHaveBeenCalledWith('/dashboard')
  })

  it('handleTwoFactorSubmit erróneo limpia el código y muestra error', async () => {
    authComposable.loginTwoFactor.mockRejectedValueOnce(new Error('Código incorrecto'))
    const w = await factory()
    w.vm.step = 'twoFactor'
    w.vm.twoFactorCode = '123456'
    await w.vm.handleTwoFactorSubmit()
    await flushPromises()
    expect(w.vm.twoFactorError).toBe('Código incorrecto')
    expect(w.vm.twoFactorCode).toBe('')
  })

  it('backToCredentials vuelve al paso de credenciales', async () => {
    const w = await factory()
    w.vm.step = 'twoFactor'
    w.vm.backToCredentials()
    expect(w.vm.step).toBe('credentials')
  })

  it('onMounted con ?error muestra el mensaje de Google y limpia la query', async () => {
    route.query = { error: 'google_account_not_found' }
    const w = await factory()
    expect(w.vm.error).toBeTruthy()
    expect(replace).toHaveBeenCalledWith({ name: 'Login' })
  })

  it('onMounted con ?challengeId entra directo al paso twoFactor', async () => {
    route.query = { challengeId: 'c9' }
    const w = await factory()
    expect(w.vm.step).toBe('twoFactor')
  })

  it('buildDescribedBy compone los ids de ayuda y error', async () => {
    const w = await factory()
    expect(w.vm.buildDescribedBy('nif', true)).toContain('nif-error')
    expect(w.vm.buildDescribedBy('password', false)).toBeUndefined()
  })

  it('muestra un enlace para volver a la pantalla de inicio', async () => {
    const w = await factory()
    const backLink = w.findComponent('.back-home-link')
    expect(backLink.exists()).toBe(true)
    expect(backLink.props('to')).toBe('/')
    expect(backLink.text()).toContain('Volver al inicio')
  })
})
