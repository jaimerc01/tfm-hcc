import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const auth = vi.hoisted(() => ({ confirmarResetPassword: vi.fn() }))
vi.mock('@/services/authService', () => ({ default: auth }))

const push = vi.hoisted(() => vi.fn())
const route = vi.hoisted(() => ({ query: {} }))
vi.mock('vue-router', () => ({ useRouter: () => ({ push }), useRoute: () => route }))

import ResetPasswordView from '@/views/auth/ResetPasswordView.vue'

const stubs = { RouterLink: { template: '<a><slot /></a>', props: ['to'] } }

beforeEach(() => {
  vi.clearAllMocks()
  vi.useFakeTimers()
  route.query = { token: 'tok-123' }
  auth.confirmarResetPassword.mockResolvedValue(true)
})

const factory = async () => {
  const w = mount(ResetPasswordView, { global: { stubs } })
  await flushPromises()
  return w
}

describe('ResetPasswordView', () => {
  it('sin token muestra el aviso de enlace no válido', async () => {
    route.query = {}
    const w = await factory()
    expect(w.text()).toContain(w.vm.$t('reset_password_link_invalid'))
    expect(w.find('form').exists()).toBe(false)
  })

  it('rechaza contraseñas cortas o que no coinciden sin llamar al servicio', async () => {
    const w = await factory()
    w.vm.password = 'corta'
    w.vm.password2 = 'corta'
    await w.vm.handleSubmit()
    expect(auth.confirmarResetPassword).not.toHaveBeenCalled()

    w.vm.password = 'claveLargaSegura'
    w.vm.password2 = 'otraCosa'
    await w.vm.handleSubmit()
    expect(auth.confirmarResetPassword).not.toHaveBeenCalled()
    expect(w.vm.fieldErrors.password2).toBeTruthy()
  })

  it('envía token y contraseña, marca hecho y redirige al login', async () => {
    const w = await factory()
    w.vm.password = 'claveLargaSegura'
    w.vm.password2 = 'claveLargaSegura'
    await w.vm.handleSubmit()
    await flushPromises()
    expect(auth.confirmarResetPassword).toHaveBeenCalledWith('tok-123', 'claveLargaSegura')
    expect(w.vm.done).toBe(true)
    vi.runAllTimers()
    expect(push).toHaveBeenCalledWith({ name: 'Login' })
  })

  it('muestra el error del backend si el enlace no es válido', async () => {
    auth.confirmarResetPassword.mockRejectedValueOnce(new Error('enlace no válido'))
    const w = await factory()
    w.vm.password = 'claveLargaSegura'
    w.vm.password2 = 'claveLargaSegura'
    await w.vm.handleSubmit()
    await flushPromises()
    expect(w.vm.done).toBe(false)
    expect(w.vm.error).toBe('enlace no válido')
  })
})
