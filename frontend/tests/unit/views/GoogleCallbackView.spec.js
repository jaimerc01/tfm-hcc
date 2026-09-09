import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const auth = vi.hoisted(() => ({ exchangeGoogleCode: vi.fn() }))
vi.mock('@/services/authService', () => ({ default: auth }))

const replace = vi.hoisted(() => vi.fn())
const route = vi.hoisted(() => ({ query: {} }))
vi.mock('vue-router', () => ({ useRouter: () => ({ replace }), useRoute: () => route }))

import GoogleCallbackView from '@/views/auth/GoogleCallbackView.vue'

beforeEach(() => {
  vi.clearAllMocks()
  route.query = {}
})

describe('GoogleCallbackView', () => {
  it('sin code redirige a Login con error', async () => {
    mount(GoogleCallbackView)
    await flushPromises()
    expect(replace).toHaveBeenCalledWith({ name: 'Login', query: { error: 'google_auth_failed' } })
  })

  it('con code válido intercambia y va a Dashboard', async () => {
    route.query = { code: 'abc' }
    auth.exchangeGoogleCode.mockResolvedValueOnce({ token: 't' })
    mount(GoogleCallbackView)
    await flushPromises()
    expect(auth.exchangeGoogleCode).toHaveBeenCalledWith('abc')
    expect(replace).toHaveBeenCalledWith({ name: 'Dashboard' })
  })

  it('si requiere 2FA redirige a Login con challengeId', async () => {
    route.query = { code: 'abc' }
    auth.exchangeGoogleCode.mockResolvedValueOnce({ requiresTwoFactor: true, challengeId: 'c1' })
    mount(GoogleCallbackView)
    await flushPromises()
    expect(replace).toHaveBeenCalledWith({ name: 'Login', query: { challengeId: 'c1' } })
  })

  it('si el intercambio lanza redirige a Login con error', async () => {
    route.query = { code: 'abc' }
    auth.exchangeGoogleCode.mockRejectedValueOnce(new Error('nope'))
    mount(GoogleCallbackView)
    await flushPromises()
    expect(replace).toHaveBeenCalledWith({ name: 'Login', query: { error: 'google_auth_failed' } })
  })
})
