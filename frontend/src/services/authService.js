import axios from 'axios'

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
        throw new Error('Ha ocurrido un error. Por favor, inténtelo de nuevo')
      }

      const { token, expirationTime } = (response && response.data) ? response.data : {}
      if (!token) {
        throw new Error('Ha ocurrido un error. Por favor, inténtelo de nuevo')
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
        throw new Error('Ha ocurrido un error. Por favor, inténtelo de nuevo')
      } else if (error.response?.status === 401) {
        throw new Error('Credenciales incorrectas')
      } else if (error.response?.status >= 500) {
        throw new Error('Ha ocurrido un error. Por favor, inténtelo de nuevo')
      } else {
        throw new Error(error.response?.data?.message || 'Ha ocurrido un error. Por favor, inténtelo de nuevo')
      }
    }
  }

  async logout() {
    try {
      // Llama al endpoint si existe; si no, simplemente limpia el estado local
      await this.apiClient.post('/auth/logout').catch(() => {})
    } catch (error) {
      console.error('Error al cerrar sesión:', error)
    } finally {
      // Limpiar datos locales siempre
      localStorage.removeItem('authToken')
      localStorage.removeItem('tokenExp')
    }
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
  const response = await this.apiClient.get('/usuario/me')
      // Expected shape: { string }
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
}

export default new AuthService()
