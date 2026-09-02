import { describe, it, expect, vi, beforeEach } from 'vitest'
import { makeJwt } from '../../helpers/jwt'

const { client, create } = vi.hoisted(() => {
  const client = {
    get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn(),
    interceptors: { request: { use: vi.fn() } }
  }
  return { client, create: vi.fn(() => client) }
})
vi.mock('axios', () => ({ default: { create } }))

import authService from '@/services/authService'

const err = (status, data, headers) => ({ response: { status, data, headers } })

beforeEach(() => {
  localStorage.clear()
  client.get.mockReset(); client.post.mockReset(); client.put.mockReset(); client.delete.mockReset()
})

describe('authService.login', () => {
  it('guarda el token y su expiración a partir del claim exp del JWT', async () => {
    const exp = Math.floor(Date.now() / 1000) + 3600
    const token = makeJwt({ exp })
    client.post.mockResolvedValueOnce({ data: { token, expirationTime: 1000 }, headers: {} })

    const res = await authService.login({ nif: 'x', password: 'y' })

    expect(res.token).toBe(token)
    expect(localStorage.getItem('authToken')).toBe(token)
    expect(localStorage.getItem('tokenExp')).toBe(String(exp * 1000))
  })

  it('usa expirationTime relativo si el JWT no trae exp', async () => {
    const token = makeJwt({ sub: 'u' })
    client.post.mockResolvedValueOnce({ data: { token, expirationTime: 5000 }, headers: {} })
    const before = Date.now()
    await authService.login({})
    const stored = Number(localStorage.getItem('tokenExp'))
    expect(stored).toBeGreaterThanOrEqual(before + 5000)
  })

  it('devuelve requiresTwoFactor con challengeId sin guardar token', async () => {
    client.post.mockResolvedValueOnce({ data: { requiresTwoFactor: true, challengeId: 'c1' }, headers: {} })
    const res = await authService.login({})
    expect(res).toEqual({ requiresTwoFactor: true, challengeId: 'c1' })
    expect(localStorage.getItem('authToken')).toBeNull()
  })

  it('lanza si requiresTwoFactor sin challengeId', async () => {
    client.post.mockResolvedValueOnce({ data: { requiresTwoFactor: true }, headers: {} })
    await expect(authService.login({})).rejects.toThrow()
  })

  it('lanza si la respuesta no trae token', async () => {
    client.post.mockResolvedValueOnce({ data: {}, headers: {} })
    await expect(authService.login({})).rejects.toThrow()
  })

  it('lanza si la respuesta es HTML', async () => {
    client.post.mockResolvedValueOnce({ data: { token: 'x' }, headers: { 'content-type': 'text/html' } })
    await expect(authService.login({})).rejects.toThrow()
  })

  it('traduce el error 401 a credenciales inválidas', async () => {
    client.post.mockRejectedValueOnce(err(401))
    await expect(authService.login({})).rejects.toThrow()
  })

  it('trata el timeout (ECONNABORTED) como error genérico', async () => {
    client.post.mockRejectedValueOnce({ code: 'ECONNABORTED', message: 'timeout of 10000ms' })
    await expect(authService.login({})).rejects.toThrow()
  })

  it('trata un 500 como error genérico', async () => {
    client.post.mockRejectedValueOnce(err(500))
    await expect(authService.login({})).rejects.toThrow()
  })
})

describe('authService.loginTwoFactor', () => {
  it('guarda el token cuando el código es correcto', async () => {
    const token = makeJwt({ exp: Math.floor(Date.now() / 1000) + 60 })
    client.post.mockResolvedValueOnce({ data: { token } })
    await authService.loginTwoFactor('c1', '123456')
    expect(localStorage.getItem('authToken')).toBe(token)
  })

  it('lanza si no hay token', async () => {
    client.post.mockResolvedValueOnce({ data: {} })
    await expect(authService.loginTwoFactor('c1', '000000')).rejects.toThrow()
  })

  it('traduce el 401 a código inválido', async () => {
    client.post.mockRejectedValueOnce(err(401))
    await expect(authService.loginTwoFactor('c1', '000000')).rejects.toThrow()
  })
})

describe('authService token helpers', () => {
  it('logout limpia authToken y tokenExp', async () => {
    localStorage.setItem('authToken', 'x')
    localStorage.setItem('tokenExp', '123')
    await authService.logout()
    expect(localStorage.getItem('authToken')).toBeNull()
    expect(localStorage.getItem('tokenExp')).toBeNull()
  })

  it('getToken ignora los valores literales "undefined"/"null"', () => {
    localStorage.setItem('authToken', 'undefined')
    expect(authService.getToken()).toBeNull()
    localStorage.setItem('authToken', 'abc')
    expect(authService.getToken()).toBe('abc')
  })

  it('getCurrentUser decodifica los claims del JWT', () => {
    const token = makeJwt({ sub: 'u1', authorities: ['ROLE_MEDICO'] })
    localStorage.setItem('authToken', token)
    expect(authService.getCurrentUser()).toMatchObject({ sub: 'u1', authorities: ['ROLE_MEDICO'] })
  })

  it('getCurrentUser devuelve null si no hay token o es inválido', () => {
    expect(authService.getCurrentUser()).toBeNull()
    localStorage.setItem('authToken', 'no-es-jwt')
    expect(authService.getCurrentUser()).toBeNull()
  })

  it('isAuthenticated true con token no expirado', () => {
    localStorage.setItem('authToken', makeJwt({ exp: Math.floor(Date.now() / 1000) + 3600 }))
    expect(authService.isAuthenticated()).toBe(true)
  })

  it('isAuthenticated false y limpia sesión con token expirado', () => {
    localStorage.setItem('authToken', makeJwt({ exp: Math.floor(Date.now() / 1000) - 10 }))
    expect(authService.isAuthenticated()).toBe(false)
    expect(localStorage.getItem('authToken')).toBeNull()
  })

  it('isAuthenticated usa tokenExp persistido si el JWT no trae exp', () => {
    localStorage.setItem('authToken', makeJwt({ sub: 'u' }))
    localStorage.setItem('tokenExp', String(Date.now() + 10000))
    expect(authService.isAuthenticated()).toBe(true)
  })

  it('isAuthenticated asume válido si no puede determinar exp', () => {
    localStorage.setItem('authToken', makeJwt({ sub: 'u' }))
    expect(authService.isAuthenticated()).toBe(true)
  })
})

describe('authService.fetchMyName', () => {
  it('devuelve el nombre si la respuesta es string', async () => {
    client.get.mockResolvedValueOnce({ data: 'Ana López' })
    await expect(authService.fetchMyName()).resolves.toBe('Ana López')
  })
  it('devuelve null si la respuesta no es string', async () => {
    client.get.mockResolvedValueOnce({ data: { x: 1 } })
    await expect(authService.fetchMyName()).resolves.toBeNull()
  })
  it('devuelve null ante 401', async () => {
    client.get.mockRejectedValueOnce(err(401))
    await expect(authService.fetchMyName()).resolves.toBeNull()
  })
  it('propaga otros errores', async () => {
    client.get.mockRejectedValueOnce(err(500))
    await expect(authService.fetchMyName()).rejects.toBeTruthy()
  })
})

describe('authService.signup', () => {
  it('devuelve data en éxito', async () => {
    client.post.mockResolvedValueOnce({ data: { id: 1 } })
    await expect(authService.signup({})).resolves.toEqual({ id: 1 })
  })
  it('traduce el 409 a usuario ya existente', async () => {
    client.post.mockRejectedValueOnce(err(409))
    await expect(authService.signup({})).rejects.toThrow()
  })
})

describe('authService.exchangeGoogleCode', () => {
  it('guarda el token en éxito', async () => {
    const token = makeJwt({ exp: Math.floor(Date.now() / 1000) + 60 })
    client.post.mockResolvedValueOnce({ data: { token } })
    await authService.exchangeGoogleCode('code')
    expect(localStorage.getItem('authToken')).toBe(token)
  })
  it('devuelve requiresTwoFactor', async () => {
    client.post.mockResolvedValueOnce({ data: { requiresTwoFactor: true, challengeId: 'c' } })
    await expect(authService.exchangeGoogleCode('code')).resolves.toEqual({ requiresTwoFactor: true, challengeId: 'c' })
  })
  it('traduce el 401 a error de login de Google', async () => {
    client.post.mockRejectedValueOnce(err(401))
    await expect(authService.exchangeGoogleCode('code')).rejects.toThrow()
  })
})

describe('authService datos de usuario', () => {
  it('fetchMyData devuelve data / null en 401', async () => {
    client.get.mockResolvedValueOnce({ data: { nombre: 'A' } })
    await expect(authService.fetchMyData()).resolves.toEqual({ nombre: 'A' })
    client.get.mockRejectedValueOnce(err(401))
    await expect(authService.fetchMyData()).resolves.toBeNull()
  })

  it('updateMyData traduce 400 y 401', async () => {
    client.put.mockResolvedValueOnce({ data: { ok: true } })
    await expect(authService.updateMyData({})).resolves.toEqual({ ok: true })
    client.put.mockRejectedValueOnce(err(400, 'campo inválido'))
    await expect(authService.updateMyData({})).rejects.toThrow('campo inválido')
    client.put.mockRejectedValueOnce(err(401))
    await expect(authService.updateMyData({})).rejects.toThrow()
  })

  it('changePassword invalida la sesión local tras éxito', async () => {
    localStorage.setItem('authToken', 'x')
    client.put.mockResolvedValueOnce({ data: { id: 3 } })
    await authService.changePassword('old', 'new')
    expect(localStorage.getItem('authToken')).toBeNull()
  })

  it('changePassword propaga el mensaje del backend', async () => {
    client.put.mockRejectedValueOnce({ response: { data: 'contraseña actual incorrecta' } })
    await expect(authService.changePassword('a', 'b')).rejects.toThrow('contraseña actual incorrecta')
  })

  it('deleteAccount exige contraseña cuando el backend pide reauth', async () => {
    client.delete.mockRejectedValueOnce(err(401, null, { 'x-reauth-required': 'true' }))
    await expect(authService.deleteAccount()).rejects.toThrow()
  })

  it('deleteAccount limpia la sesión en éxito', async () => {
    localStorage.setItem('authToken', 'x')
    client.delete.mockResolvedValueOnce({})
    await expect(authService.deleteAccount('pw')).resolves.toBe(true)
    expect(localStorage.getItem('authToken')).toBeNull()
  })

  it('exportMyData devuelve data y maneja reauth', async () => {
    client.get.mockResolvedValueOnce({ data: { export: 1 } })
    await expect(authService.exportMyData('pw')).resolves.toEqual({ export: 1 })
    client.get.mockRejectedValueOnce(err(401, null, { 'x-reauth-required': 'true' }))
    await expect(authService.exportMyData()).rejects.toThrow()
  })
})

describe('authService RGPD limitación de tratamiento', () => {
  it('limitarTratamiento devuelve true y traduce conflictos', async () => {
    client.post.mockResolvedValueOnce({})
    await expect(authService.limitarTratamiento()).resolves.toBe(true)
    client.post.mockRejectedValueOnce(err(409))
    await expect(authService.limitarTratamiento()).rejects.toThrow()
  })
  it('reanudarTratamiento devuelve true y traduce 401', async () => {
    client.post.mockResolvedValueOnce({})
    await expect(authService.reanudarTratamiento()).resolves.toBe(true)
    client.post.mockRejectedValueOnce(err(401))
    await expect(authService.reanudarTratamiento()).rejects.toThrow()
  })
})

describe('authService TOTP', () => {
  it('getTotpStatus devuelve booleano', async () => {
    client.get.mockResolvedValueOnce({ data: true })
    await expect(authService.getTotpStatus()).resolves.toBe(true)
  })
  it('setupTotp devuelve data', async () => {
    client.post.mockResolvedValueOnce({ data: { secret: 's' } })
    await expect(authService.setupTotp()).resolves.toEqual({ secret: 's' })
  })
  it('confirmTotp traduce 400 a código inválido', async () => {
    client.post.mockRejectedValueOnce(err(400))
    await expect(authService.confirmTotp('000000')).rejects.toThrow()
  })
  it('confirmTotp devuelve true en éxito', async () => {
    client.post.mockResolvedValueOnce({})
    await expect(authService.confirmTotp('123456')).resolves.toBe(true)
  })
  it('disableTotp traduce 409 a estado inválido', async () => {
    client.post.mockRejectedValueOnce(err(409))
    await expect(authService.disableTotp('123456')).rejects.toThrow()
  })
})

describe('authService.redirectToGoogleLogin', () => {
  it('redirige a la URL de login de Google del backend', () => {
    const assign = vi.fn()
    Object.defineProperty(window, 'location', { value: { assign }, writable: true })
    authService.redirectToGoogleLogin()
    expect(assign).toHaveBeenCalledWith(expect.stringContaining('/authentication/google/login'))
  })
})

describe('authService.listarMisMedicos / desasignarMedico', () => {
  it('listarMisMedicos pide /relaciones/mis-medicos y devuelve la lista', async () => {
    client.get.mockResolvedValueOnce({ data: [{ nif: '11111111H', nombre: 'Ana' }] })
    const res = await authService.listarMisMedicos()
    expect(client.get).toHaveBeenCalledWith('/relaciones/mis-medicos')
    expect(res).toEqual([{ nif: '11111111H', nombre: 'Ana' }])
  })

  it('listarMisMedicos devuelve [] si la respuesta no es un array', async () => {
    client.get.mockResolvedValueOnce({ data: null })
    await expect(authService.listarMisMedicos()).resolves.toEqual([])
  })

  it('desasignarMedico hace DELETE sobre /relaciones/mis-medicos con el nif codificado', async () => {
    client.delete.mockResolvedValueOnce({})
    await authService.desasignarMedico('11111111/H')
    expect(client.delete).toHaveBeenCalledWith('/relaciones/mis-medicos/11111111%2FH')
  })

  it('desasignarMedico traduce el 404 a "relación no encontrada"', async () => {
    client.delete.mockRejectedValueOnce(err(404))
    await expect(authService.desasignarMedico('11111111H')).rejects.toThrow()
  })
})

describe('authService.listarMisAnotaciones', () => {
  it('sin filtros pide /usuario/anotaciones con params vacíos y devuelve la lista', async () => {
    client.get.mockResolvedValueOnce({ data: [{ id: '1', mensaje: 'hola' }] })
    const res = await authService.listarMisAnotaciones()
    expect(client.get).toHaveBeenCalledWith('/usuario/anotaciones', { params: {} })
    expect(res).toEqual([{ id: '1', mensaje: 'hola' }])
  })

  it('propaga los filtros de médico y fechas como params', async () => {
    client.get.mockResolvedValueOnce({ data: [] })
    await authService.listarMisAnotaciones({ medicoNif: '11111111H', desde: '2024-01-01T00:00:00', hasta: '2024-12-31T23:59:59' })
    expect(client.get).toHaveBeenCalledWith('/usuario/anotaciones', {
      params: { medicoNif: '11111111H', desde: '2024-01-01T00:00:00', hasta: '2024-12-31T23:59:59' }
    })
  })

  it('devuelve lista vacía si la respuesta no es un array', async () => {
    client.get.mockResolvedValueOnce({ data: null })
    await expect(authService.listarMisAnotaciones()).resolves.toEqual([])
  })

  it('lanza error traducido ante un 401', async () => {
    client.get.mockRejectedValueOnce(err(401))
    await expect(authService.listarMisAnotaciones()).rejects.toThrow()
  })
})

describe('authService.solicitarResetPassword', () => {
  it('hace POST con el correo en el cuerpo', async () => {
    client.post.mockResolvedValueOnce({})
    await authService.solicitarResetPassword('ana@example.com')
    expect(client.post).toHaveBeenCalledWith('/authentication/password-reset/request', { email: 'ana@example.com' })
  })

  it('resuelve igual ante un 4xx (respuesta neutra)', async () => {
    client.post.mockRejectedValueOnce(err(400))
    await expect(authService.solicitarResetPassword('x')).resolves.toBe(true)
  })

  it('lanza ante un error de servidor', async () => {
    client.post.mockRejectedValueOnce(err(500))
    await expect(authService.solicitarResetPassword('x')).rejects.toThrow()
  })
})

describe('authService.confirmarResetPassword', () => {
  it('hace POST con token y nueva contraseña', async () => {
    client.post.mockResolvedValueOnce({})
    await authService.confirmarResetPassword('tok', 'nuevaClaveSegura')
    expect(client.post).toHaveBeenCalledWith('/authentication/password-reset/confirm', { token: 'tok', nuevaPassword: 'nuevaClaveSegura' })
  })

  it('traduce el 400 a enlace no válido', async () => {
    client.post.mockRejectedValueOnce(err(400))
    await expect(authService.confirmarResetPassword('tok', 'nuevaClaveSegura')).rejects.toThrow()
  })

  it('lanza un error genérico ante otros fallos', async () => {
    client.post.mockRejectedValueOnce(err(500))
    await expect(authService.confirmarResetPassword('tok', 'nuevaClaveSegura')).rejects.toThrow()
  })
})
