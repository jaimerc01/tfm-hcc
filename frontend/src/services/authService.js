import axios from 'axios'
import { tService } from './serviceI18n'

const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8081'

class AuthService {
  constructor() {
    this.apiClient = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000, // 10 segundos timeout
      responseType: 'json', // Preferir JSON
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    // Interceptor para añadir token automáticamente
    this.apiClient.interceptors.request.use((config) => {
      const token = localStorage.getItem('authToken')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    })
  }

  async login(credentials) {
    try {
      const response = await this.apiClient.post('/authentication/login', credentials)

      // Validar que la respuesta sea JSON y contenga token
      const contentType = response.headers?.['content-type'] || response.headers?.get?.('content-type') || ''
      if (contentType && contentType.includes('text/html')) {
        throw new Error(tService('auth_generic_retry'))
      }

      const { token, expirationTime } = (response && response.data) ? response.data : {}
      if (!token) {
        throw new Error(tService('auth_generic_retry'))
      }

      // Guardar token y expiración en localStorage
      if (token) {
        localStorage.setItem('authToken', token)
      }
      // Calcular expiración absoluta: preferir 'exp' del JWT; si no, usar ahora + duration recibido
      try {
        const payload = token.split('.')[1]
        const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
        const jwtExpSec = decoded?.exp
        if (jwtExpSec && Number.isFinite(jwtExpSec)) {
          localStorage.setItem('tokenExp', String(jwtExpSec * 1000))
        } else if (typeof expirationTime === 'number' && Number.isFinite(expirationTime)) {
          localStorage.setItem('tokenExp', String(Date.now() + Number(expirationTime)))
        }
      } catch (_) {
        if (typeof expirationTime === 'number' && Number.isFinite(expirationTime)) {
          localStorage.setItem('tokenExp', String(Date.now() + Number(expirationTime)))
        }
      }

      return { token, expirationTime }
    } catch (error) {
      // Manejar diferentes tipos de errores
      if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        throw new Error(tService('auth_generic_retry'))
      } else if (error.response?.status === 401) {
        throw new Error(tService('auth_invalid_credentials'))
      } else if (error.response?.status >= 500) {
        throw new Error(tService('auth_generic_retry'))
      } else {
        throw new Error(error.response?.data?.message || tService('auth_generic_retry'))
      }
    }
  }

  async logout() {
    // Logout en JWT es stateless - solo limpiamos el token local
    // No hay necesidad de llamar al backend ya que el token expirará naturalmente
    localStorage.removeItem('authToken')
    localStorage.removeItem('tokenExp')
  }

  async refreshToken() {
    try {
      const response = await this.apiClient.post('/auth/refresh')
      const { token, expirationTime } = (response && response.data) ? response.data : {}
      if (token) {
        localStorage.setItem('authToken', token)
        // Normalizar expiración como en login
        try {
          const payload = token.split('.')[1]
          const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
          const jwtExpSec = decoded?.exp
          if (jwtExpSec && Number.isFinite(jwtExpSec)) {
            localStorage.setItem('tokenExp', String(jwtExpSec * 1000))
          } else if (typeof expirationTime === 'number' && Number.isFinite(expirationTime)) {
            localStorage.setItem('tokenExp', String(Date.now() + Number(expirationTime)))
          }
        } catch (_) {
          if (typeof expirationTime === 'number' && Number.isFinite(expirationTime)) {
            localStorage.setItem('tokenExp', String(Date.now() + Number(expirationTime)))
          }
        }
      }
      return token
    } catch (error) {
      this.logout()
      throw error
    }
  }

  async fetchMyName() {
    try {
      const response = await this.apiClient.get('/usuario/nombre')
      // Expected shape: string (nombre completo del usuario)
      if (response?.data && typeof response.data === 'string') {
        return response.data
      }
      return null
    } catch (error) {
      if (error.response?.status === 401) return null
      throw error
    }
  }

  getCurrentUser() {
    const token = this.getToken()
    if (!token) return null
    try {
      const payload = token.split('.')[1]
      const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
      return decoded || null
    } catch (_e) {
      return null
    }
  }

  getToken() {
    const token = localStorage.getItem('authToken')
    return (token && token !== 'undefined' && token !== 'null') ? token : null
  }

  isAuthenticated() {
    const token = this.getToken()
    if (!token) return false
    // Obtener exp en ms (JWT exp o tokenExp persistido)
    const expMs = this._getExpMsFromToken(token)
    if (typeof expMs === 'number') {
      const valid = Date.now() < expMs
      if (!valid) {
        this._clearAuth()
      }
      return valid
    }
    // Si no podemos determinar exp, asumimos válido, pero no limpiamos
    return true
  }

  // Helpers privados
  _getExpMsFromToken(token) {
    try {
      const payload = token.split('.')[1]
      const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
      const jwtExpSec = decoded?.exp
      if (jwtExpSec && Number.isFinite(jwtExpSec)) {
        return jwtExpSec * 1000
      }
  } catch (_) { void 0 }
    const expStr = localStorage.getItem('tokenExp')
    if (expStr) {
      const expMs = Number(expStr)
      if (!Number.isNaN(expMs)) return expMs
    }
    return null
  }

  _clearAuth() {
  try { localStorage.removeItem('authToken') } catch (_) { void 0 }
  try { localStorage.removeItem('tokenExp') } catch (_) { void 0 }
  }

  // Login con Google: solo para cuentas ya registradas por el formulario tradicional.
  redirectToGoogleLogin() {
    window.location.assign(`${API_BASE_URL}/authentication/google/login`)
  }

  async exchangeGoogleCode(code) {
    try {
      const response = await this.apiClient.post('/authentication/google/token', { code })
      const { token, expirationTime } = (response && response.data) ? response.data : {}
      if (!token) {
        throw new Error(tService('auth_generic_retry'))
      }

      localStorage.setItem('authToken', token)

      try {
        const payload = token.split('.')[1]
        const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
        const jwtExpSec = decoded?.exp
        if (jwtExpSec && Number.isFinite(jwtExpSec)) {
          localStorage.setItem('tokenExp', String(jwtExpSec * 1000))
        } else if (typeof expirationTime === 'number' && Number.isFinite(expirationTime)) {
          localStorage.setItem('tokenExp', String(Date.now() + Number(expirationTime)))
        }
      } catch (_) {
        if (typeof expirationTime === 'number' && Number.isFinite(expirationTime)) {
          localStorage.setItem('tokenExp', String(Date.now() + Number(expirationTime)))
        }
      }

      return { token, expirationTime }
    } catch (error) {
      if (error?.response?.status === 401) {
        throw new Error(tService('google_login_error'))
      }
      throw new Error(error.response?.data?.message || tService('auth_generic_retry'))
    }
  }

  async signup(user) {
    // Espera un objeto con los campos necesarios por backend (nombre, apellido1, apellido2?, email, password, fechaNacimiento, nif, telefono, especialidad)
    try {
      const response = await this.apiClient.post('/authentication/signup', user)
      return response?.data || null
    } catch (error) {
      if (error.response?.status === 409) {
        throw new Error(tService('user_exists'))
      }
      throw new Error(error.response?.data?.message || tService('error_creating_user'))
    }
  }

  async fetchMyData() {
    try {
      const response = await this.apiClient.get('/usuario/me')
      return response?.data || null
    } catch (e) {
      if (e.response?.status === 401) return null
      throw e
    }
  }

  async updateMyData(partial) {
    try {
      const response = await this.apiClient.put('/usuario/me', partial)
      return response?.data || null
    } catch (e) {
      if (e.response?.status === 400) throw new Error(e.response.data || tService('invalid_data'))
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      throw new Error(tService('error_updating_data'))
    }
  }

  async changePassword(currentPassword, newPassword) {
    const payload = { currentPassword, newPassword }
    try {
  const resp = await this.apiClient.put('/usuario/password', payload)
  // Invalidate local session immediately
  this._clearAuth()
  return resp?.data?.id || true
    } catch (e) {
      if (e.response?.data) throw new Error(e.response.data)
      throw new Error(tService('error_changing_password'))
    }
  }

  async deleteAccount() {
    try {
      await this.apiClient.delete('/usuario/me')
      this._clearAuth()
      return true
    } catch (e) {
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      throw new Error(e.response?.data || tService('error_deleting_account'))
    }
  }
}

export default new AuthService()
