import axios from 'axios'

const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8080/api'

class AuthService {
  constructor() {
    this.apiClient = axios.create({
      baseURL: API_BASE_URL,
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
      const response = await this.apiClient.post('/auth/login', credentials)
      const { token, user } = response.data
      
      // Guardar token y usuario en localStorage
      localStorage.setItem('authToken', token)
      localStorage.setItem('user', JSON.stringify(user))
      
      return { token, user }
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Error de autenticación')
    }
  }

  async logout() {
    try {
      await this.apiClient.post('/auth/logout')
    } catch (error) {
      console.error('Error al cerrar sesión:', error)
    } finally {
      // Limpiar datos locales siempre
      localStorage.removeItem('authToken')
      localStorage.removeItem('user')
    }
  }

  async refreshToken() {
    try {
      const response = await this.apiClient.post('/auth/refresh')
      const { token } = response.data
      localStorage.setItem('authToken', token)
      return token
    } catch (error) {
      this.logout()
      throw error
    }
  }

  getCurrentUser() {
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : null
  }

  getToken() {
    return localStorage.getItem('authToken')
  }

  isAuthenticated() {
    return !!this.getToken()
  }
}

export default new AuthService()
