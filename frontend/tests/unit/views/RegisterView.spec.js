import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const authService = vi.hoisted(() => ({ signup: vi.fn() }))
vi.mock('@/services/authService', () => ({ default: authService }))

const push = vi.hoisted(() => vi.fn())
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))

import RegisterView from '@/views/auth/RegisterView.vue'

const stubs = {
  RouterLink: { template: '<a><slot /></a>', props: ['to'] }
}

beforeEach(() => vi.clearAllMocks())

const factory = () => mount(RegisterView, { global: { stubs } })

const fillValid = (vm) => {
  vm.form.nombre = 'Ana'
  vm.form.apellido1 = 'López'
  vm.form.fechaNacimiento = '1990-01-01'
  vm.form.nif = '12345678Z'
  vm.form.email = 'ana@example.com'
  vm.form.password = 'secret123'
  vm.form.password2 = 'secret123'
}

describe('RegisterView', () => {
  it('normalizeNif normaliza el NIF', () => {
    const w = factory()
    w.vm.form.nif = ' 12345678z '
    w.vm.normalizeNif()
    expect(w.vm.form.nif).toBe('12345678Z')
  })

  it('valida campos obligatorios vacíos', () => {
    const w = factory()
    expect(w.vm.validateNombreField()).toBe(false)
    expect(w.vm.validateApellido1Field()).toBe(false)
    expect(w.vm.validateFechaNacimientoField()).toBe(false)
    expect(w.vm.validateNifField()).toBe(false)
    expect(w.vm.validateEmailField()).toBe(false)
    expect(w.vm.validatePasswordField()).toBe(false)
  })

  it('valida NIF con formato incorrecto', () => {
    const w = factory()
    w.vm.form.nif = 'malo'
    expect(w.vm.validateNifField()).toBe(false)
    expect(w.vm.fieldErrors.nif).toBeTruthy()
  })

  it('valida longitud mínima de contraseña', () => {
    const w = factory()
    w.vm.form.password = '1234567'
    expect(w.vm.validatePasswordField()).toBe(false)
    w.vm.form.password = '12345678'
    expect(w.vm.validatePasswordField()).toBe(true)
  })

  it('valida coincidencia de contraseñas', () => {
    const w = factory()
    w.vm.form.password = 'secret123'
    w.vm.form.password2 = 'secret124'
    expect(w.vm.validatePassword2Field()).toBe(false)
    w.vm.form.password2 = 'secret123'
    expect(w.vm.validatePassword2Field()).toBe(true)
  })

  it('handleSignup no llama al servicio si el formulario es inválido', async () => {
    const w = factory()
    await w.vm.handleSignup()
    expect(authService.signup).not.toHaveBeenCalled()
  })

  it('handleSignup envía el payload sin password2 y con fecha ISO', async () => {
    authService.signup.mockResolvedValueOnce({ id: 1 })
    vi.useFakeTimers()
    const w = factory()
    fillValid(w.vm)
    await w.vm.handleSignup()
    await flushPromises()
    const payload = authService.signup.mock.calls[0][0]
    expect(payload.password2).toBeUndefined()
    expect(payload.fechaNacimiento).toBe(new Date('1990-01-01').toISOString())
    expect(w.vm.success).toBe(true)
    vi.advanceTimersByTime(1200)
    expect(push).toHaveBeenCalledWith({ name: 'Login' })
    vi.useRealTimers()
  })

  it('handleSignup muestra el error del backend', async () => {
    authService.signup.mockRejectedValueOnce(new Error('El usuario ya existe'))
    const w = factory()
    fillValid(w.vm)
    await w.vm.handleSignup()
    await flushPromises()
    expect(w.vm.error).toBe('El usuario ya existe')
  })

  it('goLogin navega a Login', () => {
    const w = factory()
    w.vm.goLogin()
    expect(push).toHaveBeenCalledWith({ name: 'Login' })
  })

  it('buildDescribedBy incluye help y error', () => {
    const w = factory()
    w.vm.fieldErrors.nif = 'err'
    expect(w.vm.buildDescribedBy('nif', true)).toBe('nif-help nif-error')
  })
})
