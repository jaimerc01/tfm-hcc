import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const auth = vi.hoisted(() => ({ solicitarResetPassword: vi.fn() }))
vi.mock('@/services/authService', () => ({ default: auth }))

import ForgotPasswordView from '@/views/auth/ForgotPasswordView.vue'

const stubs = { RouterLink: { template: '<a><slot /></a>', props: ['to'] } }

beforeEach(() => {
  vi.clearAllMocks()
  auth.solicitarResetPassword.mockResolvedValue(true)
})

const factory = async () => {
  const w = mount(ForgotPasswordView, { global: { stubs } })
  await flushPromises()
  return w
}

describe('ForgotPasswordView', () => {
  it('no llama al servicio si el correo no es válido', async () => {
    const w = await factory()
    w.vm.email = 'sin-arroba'
    await w.vm.handleSubmit()
    expect(auth.solicitarResetPassword).not.toHaveBeenCalled()
    expect(w.vm.fieldError).toBeTruthy()
  })

  it('envía la solicitud y muestra el mensaje neutro', async () => {
    const w = await factory()
    w.vm.email = 'ana@example.com'
    await w.vm.handleSubmit()
    await flushPromises()
    expect(auth.solicitarResetPassword).toHaveBeenCalledWith('ana@example.com')
    expect(w.vm.sent).toBe(true)
    expect(w.text()).toContain(w.vm.$t('forgot_password_sent'))
  })

  it('muestra el error si la solicitud falla', async () => {
    auth.solicitarResetPassword.mockRejectedValueOnce(new Error('boom'))
    const w = await factory()
    w.vm.email = 'ana@example.com'
    await w.vm.handleSubmit()
    await flushPromises()
    expect(w.vm.sent).toBe(false)
    expect(w.vm.fieldError).toBe('boom')
  })
})
