import { ref, computed } from 'vue'
import authService from '@/services/authService'

// Estado global de autenticación basado en token
const user = ref(authService.getCurrentUser())
const isAuthenticated = computed(() => authService.isAuthenticated())

export function useAuth() {
  const login = async (credentials) => {
    const result = await authService.login(credentials)
    // Actualizar usuario a partir del token (claims) si existen
    user.value = authService.getCurrentUser()
    return result
  }

  const logout = async () => {
    try {
      await authService.logout()
      user.value = null
    } catch (error) {
      console.error('Error al cerrar sesión:', error)
    }
  }

  const checkAuth = () => {
    user.value = authService.getCurrentUser()
    return authService.isAuthenticated()
  }

  return {
    user: computed(() => user.value),
    isAuthenticated,
    login,
    logout,
    checkAuth
  }
}
