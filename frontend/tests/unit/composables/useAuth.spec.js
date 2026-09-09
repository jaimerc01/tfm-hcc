import { describe, it, expect, vi, beforeEach } from 'vitest'

const mock = vi.hoisted(() => ({
  login: vi.fn(),
  loginTwoFactor: vi.fn(),
  logout: vi.fn(),
  getCurrentUser: vi.fn(() => null),
  isAuthenticated: vi.fn(() => false)
}))
vi.mock('@/services/authService', () => ({ default: mock }))

import { useAuth } from '@/composables/useAuth'

describe('useAuth', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mock.getCurrentUser.mockReturnValue(null)
    mock.isAuthenticated.mockReturnValue(false)
  })

  it('login actualiza el usuario a partir de los claims', async () => {
    mock.login.mockResolvedValueOnce({ token: 't' })
    mock.getCurrentUser.mockReturnValue({ sub: 'u1' })
    const { login, user } = useAuth()
    const res = await login({ nif: 'x' })
    expect(res).toEqual({ token: 't' })
    expect(user.value).toEqual({ sub: 'u1' })
  })

  it('login con requiresTwoFactor no toca el usuario', async () => {
    mock.login.mockResolvedValueOnce({ requiresTwoFactor: true, challengeId: 'c' })
    const { login } = useAuth()
    const res = await login({})
    expect(res.requiresTwoFactor).toBe(true)
    expect(mock.getCurrentUser).not.toHaveBeenCalled()
  })

  it('loginTwoFactor refresca el usuario', async () => {
    mock.loginTwoFactor.mockResolvedValueOnce({ token: 't' })
    mock.getCurrentUser.mockReturnValue({ sub: 'u2' })
    const { loginTwoFactor, user } = useAuth()
    await loginTwoFactor('c', '123456')
    expect(user.value).toEqual({ sub: 'u2' })
  })

  it('logout limpia el usuario', async () => {
    mock.logout.mockResolvedValueOnce()
    const { logout, user } = useAuth()
    await logout()
    expect(user.value).toBeNull()
  })

  it('logout captura errores del servicio', async () => {
    mock.logout.mockRejectedValueOnce(new Error('boom'))
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
    const { logout } = useAuth()
    await expect(logout()).resolves.toBeUndefined()
    spy.mockRestore()
  })

  it('checkAuth delega en el servicio', () => {
    mock.isAuthenticated.mockReturnValue(true)
    mock.getCurrentUser.mockReturnValue({ sub: 'u3' })
    const { checkAuth, user } = useAuth()
    expect(checkAuth()).toBe(true)
    expect(user.value).toEqual({ sub: 'u3' })
  })
})
