import { ref, computed } from 'vue'
import authService from '@/services/authService'

// Estado global de autenticación basado en token
const user = ref(authService.getCurrentUser())
const isAuthenticated = computed(() => authService.isAuthenticated())

export function useAuth() {
  const login = async (credentials) => {
    const result = await authService.login(credentials)
    // Si el usuario tiene segundo factor activo, todavía no hay token: el
    // llamador debe completar el login con loginTwoFactor().
    if (result.requiresTwoFactor) {
      return result
    }
    // Actualizar usuario a partir del token (claims) si existen
    user.value = authService.getCurrentUser()
    return result
  }

  const loginTwoFactor = async (challengeId, code) => {
    const result = await authService.loginTwoFactor(challengeId, code)
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
    loginTwoFactor,
    logout,
    checkAuth
  }
}
