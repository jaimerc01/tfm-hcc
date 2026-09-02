import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const auth = vi.hoisted(() => ({
  fetchMyData: vi.fn(),
  changePassword: vi.fn(() => Promise.resolve()),
  getTotpStatus: vi.fn(() => Promise.resolve(false)),
  setupTotp: vi.fn(),
  confirmTotp: vi.fn(() => Promise.resolve()),
  disableTotp: vi.fn(() => Promise.resolve()),
  limitarTratamiento: vi.fn(() => Promise.resolve()),
  reanudarTratamiento: vi.fn(() => Promise.resolve()),
  exportMyData: vi.fn(),
  deleteAccount: vi.fn(() => Promise.resolve()),
  updateMyData: vi.fn(),
  getCurrentUser: vi.fn(() => ({ sub: '12345678Z' })),
  _clearAuth: vi.fn()
}))
vi.mock('@/services/authService', () => ({ default: auth }))
vi.mock('qrcode', () => ({ default: { toDataURL: vi.fn(() => Promise.resolve('data:image/png;base64,QR')) } }))

const push = vi.hoisted(() => vi.fn())
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))

import DatosUsuarioView from '@/views/DatosUsuarioView.vue'

const stubs = { RouterLink: { template: '<a><slot /></a>', props: ['to'] } }
const baseUser = { nombre: 'Ana', apellido1: 'López', apellido2: '', nif: '12345678Z', email: 'ana@x.com', telefono: '600123123', fechaNacimiento: '1990-01-01T00:00:00', estadoCuenta: 'ACTIVO' }

beforeEach(() => {
  vi.clearAllMocks()
  auth.fetchMyData.mockResolvedValue({ ...baseUser })
  auth.getTotpStatus.mockResolvedValue(false)
  auth.getCurrentUser.mockReturnValue({ sub: '12345678Z' })
})

const factory = async () => {
  const w = mount(DatosUsuarioView, { global: { stubs } })
  await flushPromises()
  return w
}

describe('DatosUsuarioView', () => {
  it('carga los datos del usuario y el estado TOTP al montarse', async () => {
    const w = await factory()
    expect(auth.fetchMyData).toHaveBeenCalled()
    expect(auth.getTotpStatus).toHaveBeenCalled()
    expect(w.vm.user.nombre).toBe('Ana')
  })

  it('muestra error si la carga de datos falla', async () => {
    auth.fetchMyData.mockRejectedValueOnce(new Error('sin conexión'))
    const w = await factory()
    expect(w.vm.error).toBe('sin conexión')
  })

  it('formatDate y formatDateTime toleran valores nulos', async () => {
    const w = await factory()
    expect(w.vm.formatDate(null)).toBe('')
    expect(w.vm.formatDateTime(null)).toBe('')
    expect(w.vm.formatDate('1990-01-01')).toBeTruthy()
  })

  describe('cambio de contraseña', () => {
    it('rechaza si las contraseñas nuevas no coinciden', async () => {
      const w = await factory()
      w.vm.pw = { current: 'a', new1: 'b', new2: 'c' }
      await w.vm.submitPw()
      expect(w.vm.pwError).toBeTruthy()
      expect(auth.changePassword).not.toHaveBeenCalled()
    })

    it('cambia la contraseña y redirige a Login', async () => {
      vi.useFakeTimers()
      const w = await factory()
      w.vm.pw = { current: 'old', new1: 'nueva1', new2: 'nueva1' }
      await w.vm.submitPw()
      await flushPromises()
      expect(auth.changePassword).toHaveBeenCalledWith('old', 'nueva1')
      expect(w.vm.pwSuccess).toBe(true)
      vi.advanceTimersByTime(800)
      expect(push).toHaveBeenCalledWith({ name: 'Login' })
      vi.useRealTimers()
    })

    it('muestra el error del backend', async () => {
      auth.changePassword.mockRejectedValueOnce(new Error('contraseña incorrecta'))
      const w = await factory()
      w.vm.pw = { current: 'x', new1: 'nueva1', new2: 'nueva1' }
      await w.vm.submitPw()
      await flushPromises()
      expect(w.vm.pwError).toBe('contraseña incorrecta')
    })

    it('cancelPw limpia el formulario', async () => {
      const w = await factory()
      w.vm.showPwForm = true
      w.vm.pw = { current: 'x', new1: 'y', new2: 'z' }
      w.vm.cancelPw()
      expect(w.vm.showPwForm).toBe(false)
      expect(w.vm.pw).toEqual({ current: '', new1: '', new2: '' })
    })
  })

  describe('TOTP', () => {
    it('startTotpSetup obtiene el secreto y genera el QR', async () => {
      auth.setupTotp.mockResolvedValueOnce({ otpauthUri: 'otpauth://totp/x' })
      const w = await factory()
      await w.vm.startTotpSetup()
      expect(w.vm.showTotpSetup).toBe(true)
      expect(w.vm.totpQrDataUrl).toBe('data:image/png;base64,QR')
    })

    it('startTotpSetup gestiona el error', async () => {
      auth.setupTotp.mockRejectedValueOnce(new Error('fallo'))
      const w = await factory()
      await w.vm.startTotpSetup()
      expect(w.vm.totpSetupError).toBe('fallo')
    })

    it('confirmTotpSetup activa el segundo factor', async () => {
      const w = await factory()
      w.vm.totpConfirmCode = '123456'
      await w.vm.confirmTotpSetup()
      expect(auth.confirmTotp).toHaveBeenCalledWith('123456')
      expect(w.vm.totpEnabled).toBe(true)
    })

    it('confirmTotpDisable desactiva el segundo factor', async () => {
      const w = await factory()
      w.vm.totpDisableCode = '654321'
      await w.vm.confirmTotpDisable()
      expect(auth.disableTotp).toHaveBeenCalledWith('654321')
      expect(w.vm.totpEnabled).toBe(false)
    })

    it('loadTotpStatus no bloquea la pantalla si falla', async () => {
      auth.getTotpStatus.mockRejectedValueOnce(new Error('x'))
      const w = await factory()
      expect(w.vm.totpEnabled).toBe(false)
      expect(w.vm.totpStatusLoading).toBe(false)
    })
  })

  describe('limitación del tratamiento (art. 18 RGPD)', () => {
    it('enableProcessingRestriction suspende la cuenta', async () => {
      const w = await factory()
      await w.vm.enableProcessingRestriction()
      expect(auth.limitarTratamiento).toHaveBeenCalled()
      expect(w.vm.user.estadoCuenta).toBe('SUSPENDIDO')
    })

    it('disableProcessingRestriction reactiva la cuenta', async () => {
      const w = await factory()
      w.vm.user.estadoCuenta = 'SUSPENDIDO'
      await w.vm.disableProcessingRestriction()
      expect(auth.reanudarTratamiento).toHaveBeenCalled()
      expect(w.vm.user.estadoCuenta).toBe('ACTIVO')
    })

    it('muestra error si la limitación falla', async () => {
      auth.limitarTratamiento.mockRejectedValueOnce(new Error('conflicto'))
      const w = await factory()
      await w.vm.enableProcessingRestriction()
      expect(w.vm.processingRestrictionError).toBe('conflicto')
    })
  })

  describe('exportación de datos (art. 20 RGPD)', () => {
    it('descarga un JSON con los datos', async () => {
      auth.exportMyData.mockResolvedValueOnce({ nombre: 'Ana' })
      const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
      const w = await factory()
      w.vm.reauth.password = 'secret'
      await w.vm.exportData()
      await flushPromises()
      expect(auth.exportMyData).toHaveBeenCalledWith('secret')
      expect(clickSpy).toHaveBeenCalled()
      expect(w.vm.exportSuccess).toBeTruthy()
      clickSpy.mockRestore()
    })

    it('muestra error si la exportación falla', async () => {
      auth.exportMyData.mockRejectedValueOnce(new Error('reauth requerida'))
      const w = await factory()
      await w.vm.exportData()
      expect(w.vm.exportError).toBe('reauth requerida')
    })
  })

  describe('eliminación de cuenta', () => {
    it('confirmDelete borra la cuenta y redirige a Login', async () => {
      const w = await factory()
      w.vm.reauth.password = 'secret'
      await w.vm.confirmDelete()
      await flushPromises()
      expect(auth.deleteAccount).toHaveBeenCalledWith('secret')
      expect(push).toHaveBeenCalledWith({ name: 'Login' })
    })

    it('muestra error si el borrado falla', async () => {
      auth.deleteAccount.mockRejectedValueOnce(new Error('contraseña requerida'))
      const w = await factory()
      await w.vm.confirmDelete()
      expect(w.vm.deleteError).toBe('contraseña requerida')
    })

    it('onDeleteModalKeydown con Escape cierra el modal', async () => {
      const w = await factory()
      w.vm.showDelete = true
      w.vm.onDeleteModalKeydown({ key: 'Escape', preventDefault: vi.fn() })
      expect(w.vm.showDelete).toBe(false)
    })
  })

  describe('edición de datos', () => {
    it('startEdit precarga el formulario con la fecha recortada', async () => {
      const w = await factory()
      w.vm.startEdit()
      expect(w.vm.editMode).toBe(true)
      expect(w.vm.form.nif).toBe('12345678Z')
      expect(w.vm.form.fechaNacimiento).toBe('1990-01-01')
    })

    it('submitEdit valida nombre, NIF y email', async () => {
      const w = await factory()
      w.vm.startEdit()
      w.vm.form.nombre = ''
      await w.vm.submitEdit()
      expect(w.vm.editError).toBeTruthy()

      w.vm.form.nombre = 'Ana'
      w.vm.form.nif = 'malo'
      await w.vm.submitEdit()
      expect(w.vm.editError).toBeTruthy()

      w.vm.form.nif = '12345678Z'
      w.vm.form.email = 'no-es-email'
      await w.vm.submitEdit()
      expect(w.vm.editError).toBeTruthy()
    })

    it('submitEdit correcto actualiza el usuario', async () => {
      vi.useFakeTimers()
      auth.updateMyData.mockResolvedValueOnce({ ...baseUser, nombre: 'Ana María' })
      const w = await factory()
      w.vm.startEdit()
      w.vm.form.nombre = 'Ana María'
      await w.vm.submitEdit()
      await flushPromises()
      expect(auth.updateMyData).toHaveBeenCalled()
      expect(w.vm.user.nombre).toBe('Ana María')
      expect(w.vm.editSuccess).toBe(true)
      vi.useRealTimers()
    })

    it('submitEdit fuerza logout si cambia el NIF', async () => {
      vi.useFakeTimers()
      auth.updateMyData.mockResolvedValueOnce({ ...baseUser, nif: '87654321X' })
      const w = await factory()
      w.vm.startEdit()
      w.vm.form.nif = '87654321X'
      await w.vm.submitEdit()
      await flushPromises()
      expect(w.vm.nifChanged).toBe(true)
      expect(auth._clearAuth).toHaveBeenCalled()
      vi.advanceTimersByTime(900)
      expect(push).toHaveBeenCalledWith({ name: 'Login' })
      vi.useRealTimers()
    })

    it('cancelEdit sale del modo edición', async () => {
      const w = await factory()
      w.vm.startEdit()
      w.vm.cancelEdit()
      expect(w.vm.editMode).toBe(false)
    })
  })
})
