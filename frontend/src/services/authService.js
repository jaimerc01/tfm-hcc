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

  // Guarda el token y calcula su expiración absoluta en localStorage.
  // Compartido por login(), loginTwoFactor() y exchangeGoogleCode(): en los tres
  // casos el backend devuelve el mismo par {token, expirationTime}.
  _storeToken(token, expirationTime) {
    localStorage.setItem('authToken', token)
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
  }

  async login(credentials) {
    try {
      const response = await this.apiClient.post('/authentication/login', credentials)

      // Validar que la respuesta sea JSON y contenga token
      const contentType = response.headers?.['content-type'] || response.headers?.get?.('content-type') || ''
      if (contentType && contentType.includes('text/html')) {
        throw new Error(tService('auth_generic_retry'))
      }

      const { token, expirationTime, requiresTwoFactor, challengeId } = (response && response.data) ? response.data : {}

      // El NIF y la contraseña son correctos, pero el usuario tiene activado el
      // segundo factor: todavía no hay token, hay que completar loginTwoFactor().
      if (requiresTwoFactor) {
        if (!challengeId) {
          throw new Error(tService('auth_generic_retry'))
        }
        return { requiresTwoFactor: true, challengeId }
      }

      if (!token) {
        throw new Error(tService('auth_generic_retry'))
      }

      this._storeToken(token, expirationTime)

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

  // Segundo paso del login cuando el usuario tiene activado el segundo factor (TOTP).
  async loginTwoFactor(challengeId, code) {
    try {
      const response = await this.apiClient.post('/authentication/login/2fa', { challengeId, code })
      const { token, expirationTime } = (response && response.data) ? response.data : {}
      if (!token) {
        throw new Error(tService('auth_generic_retry'))
      }

      this._storeToken(token, expirationTime)

      return { token, expirationTime }
    } catch (error) {
      if (error.response?.status === 401) {
        throw new Error(tService('two_factor_code_invalid'))
      }
      throw new Error(tService('auth_generic_retry'))
    }
  }

  async logout() {
    // Logout en JWT es stateless - solo limpiamos el token local
    // No hay necesidad de llamar al backend ya que el token expirará naturalmente
    localStorage.removeItem('authToken')
    localStorage.removeItem('tokenExp')
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
      const { token, expirationTime, requiresTwoFactor, challengeId } = (response && response.data) ? response.data : {}

      // El NIF/contraseña de Google eran válidos, pero el usuario tiene activado el
      // segundo factor: todavía no hay token, hay que completar loginTwoFactor().
      if (requiresTwoFactor) {
        if (!challengeId) {
          throw new Error(tService('auth_generic_retry'))
        }
        return { requiresTwoFactor: true, challengeId }
      }

      if (!token) {
        throw new Error(tService('auth_generic_retry'))
      }

      this._storeToken(token, expirationTime)

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

  // Detecta la cabecera X-Reauth-Required que el backend añade a una respuesta 401
  // cuando la operación exige reintroducir la contraseña actual (exportar/eliminar cuenta).
  _isReauthRequired(response) {
    const header = response?.headers?.['x-reauth-required'] ?? response?.headers?.get?.('x-reauth-required')
    return header === 'true'
  }

  async deleteAccount(currentPassword) {
    try {
      const headers = currentPassword ? { 'X-Current-Password': currentPassword } : {}
      await this.apiClient.delete('/usuario/me', { headers })
      this._clearAuth()
      return true
    } catch (e) {
      if (e.response?.status === 401 && this._isReauthRequired(e.response)) {
        throw new Error(tService('password_required_delete_account'))
      }
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      throw new Error(e.response?.data || tService('error_deleting_account'))
    }
  }

  async exportMyData(currentPassword) {
    try {
      const headers = currentPassword ? { 'X-Current-Password': currentPassword } : {}
      const resp = await this.apiClient.get('/usuario/export', { headers })
      return resp?.data || null
    } catch (e) {
      if (e.response?.status === 401 && this._isReauthRequired(e.response)) {
        throw new Error(tService('password_required_export'))
      }
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      throw new Error(tService('error_exporting_data'))
    }
  }

  // Derecho de limitación del tratamiento (art. 18 RGPD): pausa el acceso de los
  // médicos al historial del usuario sin eliminar la cuenta.

  async limitarTratamiento() {
    try {
      await this.apiClient.post('/usuario/me/limitar-tratamiento')
      return true
    } catch (e) {
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      if (e.response?.status === 409) throw new Error(tService('error_processing_restriction_conflict'))
      throw new Error(tService('error_processing_restriction'))
    }
  }

  async reanudarTratamiento() {
    try {
      await this.apiClient.post('/usuario/me/reanudar-tratamiento')
      return true
    } catch (e) {
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      if (e.response?.status === 409) throw new Error(tService('error_processing_restriction_conflict'))
      throw new Error(tService('error_processing_resume'))
    }
  }

  // Segundo factor (TOTP): configuración desde la pantalla de datos del usuario.

  async getTotpStatus() {
    try {
      const resp = await this.apiClient.get('/usuario/2fa/status')
      return Boolean(resp?.data)
    } catch (e) {
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      throw new Error(tService('error_loading_data'))
    }
  }

  async setupTotp() {
    try {
      const resp = await this.apiClient.post('/usuario/2fa/setup')
      return resp?.data || null
    } catch (e) {
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      throw new Error(tService('two_factor_setup_error'))
    }
  }

  async confirmTotp(code) {
    try {
      await this.apiClient.post('/usuario/2fa/confirm', { code })
      return true
    } catch (e) {
      if (e.response?.status === 400) throw new Error(tService('two_factor_code_invalid'))
      if (e.response?.status === 409) throw new Error(tService('two_factor_state_error'))
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      throw new Error(tService('two_factor_setup_error'))
    }
  }

  async disableTotp(code) {
    try {
      await this.apiClient.post('/usuario/2fa/disable', { code })
      return true
    } catch (e) {
      if (e.response?.status === 400) throw new Error(tService('two_factor_code_invalid'))
      if (e.response?.status === 409) throw new Error(tService('two_factor_state_error'))
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      throw new Error(tService('two_factor_setup_error'))
    }
  }

  // Relación asistencial: médicos con acceso activo al historial del usuario.
  async listarMisMedicos() {
    try {
      const resp = await this.apiClient.get('/relaciones/mis-medicos')
      return Array.isArray(resp?.data) ? resp.data : []
    } catch (e) {
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      throw new Error(tService('error_loading_my_doctors'))
    }
  }

  // El paciente finaliza la relación asistencial con uno de sus médicos.
  async desasignarMedico(nifMedico) {
    try {
      await this.apiClient.delete(`/relaciones/mis-medicos/${encodeURIComponent(nifMedico)}`)
      return true
    } catch (e) {
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      if (e.response?.status === 404) throw new Error(tService('error_relationship_not_found'))
      throw new Error(tService('error_unassigning_doctor'))
    }
  }

  // Anotaciones médicas recibidas: observaciones que un médico asignado registra
  // sobre el usuario. Admite filtro opcional por médico y por rango de fechas.
  async listarMisAnotaciones({ medicoNif, desde, hasta } = {}) {
    try {
      const params = {}
      if (medicoNif) params.medicoNif = medicoNif
      if (desde) params.desde = desde
      if (hasta) params.hasta = hasta
      const resp = await this.apiClient.get('/usuario/anotaciones', { params })
      return Array.isArray(resp?.data) ? resp.data : []
    } catch (e) {
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      throw new Error(tService('error_loading_annotations'))
    }
  }

  // Registro de accesos a mis datos (trazabilidad, derecho de acceso RGPD). Admite
  // filtro opcional por rango de fechas.
  async misLogs({ desde, hasta } = {}) {
    try {
      const params = {}
      if (desde) params.desde = desde
      if (hasta) params.hasta = hasta
      const resp = await this.apiClient.get('/usuario/logs', { params })
      return Array.isArray(resp?.data) ? resp.data : []
    } catch (e) {
      if (e.response?.status === 401) throw new Error(tService('not_authenticated'))
      throw new Error(tService('error_loading_access_log'))
    }
  }

  // Restablecimiento de contraseña ("he olvidado mi contraseña"). Endpoints públicos.

  // Solicita el enlace de restablecimiento. La respuesta del backend es siempre
  // neutra (204), exista o no una cuenta con ese correo, así que este método
  // resuelve sin distinguir ambos casos y solo propaga fallos de red/servidor.
  async solicitarResetPassword(email) {
    try {
      await this.apiClient.post('/authentication/password-reset/request', { email })
      return true
    } catch (e) {
      if (e.response?.status >= 500 || e.code === 'ECONNABORTED') {
        throw new Error(tService('auth_generic_retry'))
      }
      // Un 4xx aquí (p. ej. cuerpo mal formado) tampoco debe revelar nada: se
      // trata como enviado.
      return true
    }
  }

  // Canjea el token del enlace de correo por el cambio de contraseña efectivo.
  async confirmarResetPassword(token, nuevaPassword) {
    try {
      await this.apiClient.post('/authentication/password-reset/confirm', { token, nuevaPassword })
      return true
    } catch (e) {
      if (e.response?.status === 400) throw new Error(tService('reset_password_link_invalid'))
      throw new Error(tService('auth_generic_retry'))
    }
  }
}

export default new AuthService()
