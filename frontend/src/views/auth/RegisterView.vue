<template>
  <div class="register-container">
    <div class="register-card">
      <h2>Crear Usuario</h2>
      <form @submit.prevent="handleSignup" class="register-form">
        <div class="grid">
          <div class="form-group">
            <label for="nombre">Nombre *</label>
            <input id="nombre" v-model="form.nombre" required />
          </div>
          <div class="form-group">
            <label for="apellido1">Primer Apellido *</label>
            <input id="apellido1" v-model="form.apellido1" required />
          </div>
          <div class="form-group">
            <label for="apellido2">Segundo Apellido</label>
            <input id="apellido2" v-model="form.apellido2" />
          </div>
          <div class="form-group">
            <label for="nif">NIF *</label>
            <input id="nif" v-model="form.nif" required />
          </div>
          <div class="form-group">
            <label for="email">Email *</label>
            <input id="email" type="email" v-model="form.email" required />
          </div>
          <div class="form-group">
            <label for="password">Contraseña *</label>
            <input id="password" type="password" v-model="form.password" required minlength="6" />
          </div>
          <div class="form-group">
            <label for="password2">Repetir Contraseña *</label>
            <input id="password2" type="password" v-model="form.password2" required minlength="6" />
          </div>
          <div class="form-group">
            <label for="fechaNacimiento">Fecha Nacimiento *</label>
            <input id="fechaNacimiento" type="date" v-model="form.fechaNacimiento" required />
          </div>
          <div class="form-group">
            <label for="telefono">Teléfono</label>
            <input id="telefono" v-model="form.telefono" />
          </div>
        </div>

        <div class="actions">
          <button type="submit" class="primary" :disabled="loading">{{ loading ? 'Creando...' : 'Crear Usuario' }}</button>
          <button type="button" class="secondary" @click="goLogin" :disabled="loading">Volver a Login</button>
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <p v-if="success" class="success">Usuario creado correctamente. Redirigiendo...</p>
      </form>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import authService from '@/services/authService'

export default {
  name: 'RegisterView',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const error = ref('')
    const success = ref(false)

    const form = ref({
      nombre: '',
      apellido1: '',
      apellido2: '',
      email: '',
      password: '',
  password2: '',
      fechaNacimiento: '',
      nif: '',
      telefono: ''
    })

    const handleSignup = async () => {
      error.value = ''
      success.value = false
      loading.value = true
      try {
        if (form.value.password !== form.value.password2) {
          throw new Error('Las contraseñas no coinciden')
        }
        // Preparar payload (convertir fecha a ISO si está rellena)
        const payload = { ...form.value }
        delete payload.password2
        if (!payload.fechaNacimiento) {
          throw new Error('La fecha de nacimiento es obligatoria')
        }
        payload.fechaNacimiento = new Date(payload.fechaNacimiento).toISOString()
        await authService.signup(payload)
        success.value = true
        setTimeout(() => router.push({ name: 'Login' }), 1200)
      } catch (e) {
        error.value = e.message || 'Error al crear usuario'
      } finally {
        loading.value = false
      }
    }

    const goLogin = () => router.push({ name: 'Login' })

    return { form, handleSignup, loading, error, success, goLogin }
  }
}
</script>

<style scoped>
.register-container { min-height: 100vh; display:flex; justify-content:center; align-items:center; background:#f5f5f5; }
.register-card { background:#fff; padding:2rem; border-radius:8px; width:100%; max-width:760px; box-shadow:0 2px 10px rgba(0,0,0,.1); }
.grid { display:grid; grid-template-columns: repeat(auto-fill,minmax(180px,1fr)); gap:1rem; }
.form-group { display:flex; flex-direction:column; }
.form-group label { font-weight:600; margin-bottom:4px; }
.form-group input { padding:.6rem .7rem; border:1px solid #ddd; border-radius:4px; }
.actions { display:flex; gap:.75rem; margin-top:1.5rem; }
button.primary { background:#42b983; color:#fff; border:none; padding:.75rem 1.1rem; border-radius:4px; cursor:pointer; }
button.primary:hover:not(:disabled) { background:#369870; }
button.secondary { background:#eee; border:none; padding:.75rem 1.1rem; border-radius:4px; cursor:pointer; }
button.secondary:hover { background:#ddd; }
.error { margin-top:1rem; color:#e74c3c; }
.success { margin-top:1rem; color:#2e7d32; }
</style>
