import { describe, it, expect, vi, beforeEach } from 'vitest'

const auth = vi.hoisted(() => ({ isAuthenticated: vi.fn(), getCurrentUser: vi.fn() }))
vi.mock('@/services/authService', () => ({ default: auth }))

// Evita montar las vistas reales al resolver rutas en el test.
vi.mock('@/views/HomeView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/auth/LoginView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/auth/RegisterView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/auth/GoogleCallbackView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/auth/ForgotPasswordView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/auth/ResetPasswordView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/dashboard/DashboardView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/HistoriaClinicaView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/PoliticaPrivacidadView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/PoliticaCookiesView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/DatosUsuarioView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/MedicoView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/medico/PacienteHistorialView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/AdminView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/AdminMedicosView.vue', () => ({ default: { template: '<div/>' } }))
vi.mock('@/views/MisSolicitudesView.vue', () => ({ default: { template: '<div/>' } }))

import router from '@/router'

beforeEach(() => {
  vi.clearAllMocks()
  auth.isAuthenticated.mockReturnValue(false)
  auth.getCurrentUser.mockReturnValue(null)
})

const go = async (path) => {
  await router.push(path).catch(() => {})
  await router.isReady().catch(() => {})
  return router.currentRoute.value
}

describe('router guard', () => {
  it('redirige a Login una ruta protegida si no hay sesión', async () => {
    const r = await go('/dashboard')
    expect(r.name).toBe('Login')
  })

  it('permite una ruta protegida con sesión', async () => {
    auth.isAuthenticated.mockReturnValue(true)
    const r = await go('/dashboard')
    expect(r.name).toBe('Dashboard')
  })

  it('redirige a Dashboard una ruta de invitado si ya hay sesión', async () => {
    auth.isAuthenticated.mockReturnValue(true)
    const r = await go('/login')
    expect(r.name).toBe('Dashboard')
  })

  it('permite una ruta de invitado sin sesión', async () => {
    const r = await go('/register')
    expect(r.name).toBe('Register')
  })

  it('redirige a Dashboard si falta el rol requerido', async () => {
    auth.isAuthenticated.mockReturnValue(true)
    auth.getCurrentUser.mockReturnValue({ authorities: ['ROLE_PACIENTE'] })
    const r = await go('/medico')
    expect(r.name).toBe('Dashboard')
  })

  it('permite la ruta si el usuario tiene el rol requerido', async () => {
    auth.isAuthenticated.mockReturnValue(true)
    auth.getCurrentUser.mockReturnValue({ authorities: ['ROLE_MEDICO'] })
    const r = await go('/medico')
    expect(r.name).toBe('Medico')
  })

  it('deja pasar rutas públicas sin meta', async () => {
    const r = await go('/privacidad')
    expect(r.name).toBe('PoliticaPrivacidad')
  })

  it('permite recuperar contraseña sin sesión', async () => {
    const r = await go('/recuperar-password')
    expect(r.name).toBe('RecuperarPassword')
  })

  it('permite abrir el enlace de restablecimiento con su token sin sesión', async () => {
    const r = await go('/restablecer-password?token=abc')
    expect(r.name).toBe('RestablecerPassword')
    expect(r.query.token).toBe('abc')
  })

  it('redirige el flujo de recuperación a Dashboard si ya hay sesión', async () => {
    auth.isAuthenticated.mockReturnValue(true)
    const r = await go('/recuperar-password')
    expect(r.name).toBe('Dashboard')
  })
})
