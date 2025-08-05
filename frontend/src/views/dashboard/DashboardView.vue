<template>
  <div class="dashboard">
    <h1>Dashboard</h1>
    <p>¡Bienvenido! Has iniciado sesión correctamente.</p>
    
    <div class="user-info">
      <p>Usuario autenticado</p>
      <button @click="handleLogout" class="logout-btn">
        Cerrar Sesión
      </button>
    </div>
  </div>
</template>

<script>
import { useAuth } from '@/composables/useAuth'
import { useRouter } from 'vue-router'

export default {
  name: 'DashboardView',
  setup() {
    const { logout } = useAuth()
    const router = useRouter()
    
    const handleLogout = async () => {
      await logout()
      router.push('/login')
    }
    
    return {
      handleLogout
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 2rem;
  text-align: center;
}

.user-info {
  margin-top: 2rem;
}

.logout-btn {
  background-color: #e74c3c;
  color: white;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.logout-btn:hover {
  background-color: #c0392b;
}
</style>
