<template>
  <div class="dashboard">
  <h1>Inicio</h1>
  <p v-if="welcomeName">Hola, {{ welcomeName }}</p>
  <p v-else>¡Bienvenido! Has iniciado sesión correctamente.</p>
    
    <div class="user-info">
      <p>Usuario autenticado</p>
      <button @click="handleLogout" class="logout-btn">
        Cerrar Sesión
      </button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { useRouter } from 'vue-router'
import authService from '@/services/authService'

export default {
  name: 'DashboardView',
  setup() {
    const { logout } = useAuth()
    const router = useRouter()
    const welcomeName = ref('')
    const loading = ref(false)
    const loadError = ref('')
    
    const handleLogout = async () => {
      await logout()
      router.push('/login')
    }

    const loadWelcome = async () => {
      try {
        loading.value = true
        // 1) Fallback inmediato desde el JWT (sub = nif, o nombre/name si existiera)
        const claims = authService.getCurrentUser()
        if (claims) {
          const maybeName = claims.nombre || claims.name || claims.sub || ''
          if (maybeName && !welcomeName.value) welcomeName.value = maybeName
        }
        // 2) Intentar cargar el nombre real desde el backend si existe el endpoint
        const name = await authService.fetchMyName()
        if (name) welcomeName.value = name
      } catch (e) {
        loadError.value = 'No se pudo cargar el nombre'
      } finally {
        loading.value = false
      }
    }

    onMounted(loadWelcome)
    
    return {
      handleLogout,
      welcomeName,
      loading,
      loadError
    }
  }
}
</script>

<style scoped>
.logout-btn { background-color:#e74c3c; color:white; padding:.75rem 1.5rem; border:none; border-radius:4px; cursor:pointer; font-size:1rem }
.logout-btn:hover { background-color:#c0392b }
</style>
