<template>
  <div class="login-container">
    <div class="login-card">
      <h2>Iniciar Sesión</h2>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="nif">Usuario:</label>
          <input
            id="nif"
            v-model="credentials.nif"
            type="text"
            required
            placeholder="Ingresa tu usuario"
          />
        </div>
        
        <div class="form-group">
          <label for="password">Contraseña:</label>
          <input
            id="password"
            v-model="credentials.password"
            type="password"
            required
            placeholder="Ingresa tu contraseña"
          />
        </div>
        
        <button type="submit" :disabled="isLoading" class="login-btn">
          {{ isLoading ? 'Iniciando...' : 'Iniciar Sesión' }}
        </button>
        
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
      </form>
      <div class="alt-actions">
        <router-link to="/register">¿No tienes cuenta? Crear usuario</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import { useAuth } from '@/composables/useAuth'
import { validateNIF } from '@/utils/validateNIF'


export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const { login } = useAuth()
    
    const credentials = ref({
      nif: '',
      password: ''
    })
    
    const isLoading = ref(false)
    const error = ref('')
    
    const handleLogin = async () => {
      try {
        isLoading.value = true
        error.value = ''
        if (!validateNIF(credentials.value.nif)) {
          error.value = 'El NIF introducido no es válido.'
          isLoading.value = false
          return
        }
        
        await login(credentials.value)
        router.push('/dashboard')
      } catch (err) {
        error.value = err.message || 'Error al iniciar sesión'
      } finally {
        isLoading.value = false
      }
    }
    
    return {
      credentials,
      isLoading,
      error,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #333;
}

.form-group input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.login-btn {
  background-color: #42b983;
  color: white;
  padding: 0.75rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-btn:hover:not(:disabled) {
  background-color: #369870;
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  color: #e74c3c;
  text-align: center;
  padding: 0.5rem;
  background-color: #fdf2f2;
  border-radius: 4px;
}
.alt-actions { margin-top:1rem; text-align:center; }
.alt-actions a { color:#42b983; text-decoration:none; }
.alt-actions a:hover { text-decoration:underline; }
</style>
