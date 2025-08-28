
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
      </form>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import authService from '@/services/authService'
import { validateNIF } from '@/utils/validateNIF'

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
      if (!validateNIF(form.value.nif)) {
        error.value = 'El NIF introducido no es válido.'
        return
      }
      if (form.value.password !== form.value.password2) {
        error.value = 'Las contraseñas no coinciden.'
        return
      }
      loading.value = true
      try {
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

<style>
  .error { margin-top:1rem }
</style>
