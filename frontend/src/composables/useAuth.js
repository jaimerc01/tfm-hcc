import { ref, computed } from 'vue'
import authService from '@/services/authService'

// Estado global de autenticación
const user = ref(authService.getCurrentUser())
const isAuthenticated = computed(() => !!user.value)

export function useAuth() {
  const login = async (credentials) => {
    try {
      const result = await authService.login(credentials)
      user.value = result.user
      return result
    } catch (error) {
      throw error
    }
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
    const currentUser = authService.getCurrentUser()
    user.value = currentUser
    return !!currentUser
  }

  return {
    user: computed(() => user.value),
    isAuthenticated,
    login,
    logout,
    checkAuth
  }
}
