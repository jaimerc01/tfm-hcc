<template>
  <nav class="nav">
    <div class="nav__content">
      <router-link class="nav__brand" :to="{ name: 'Dashboard' }">HCC</router-link>

      <button class="nav__toggle" @click="open = !open" aria-label="Menú">
        ☰
      </button>

      <div class="nav__links" :class="{ 'nav__links--open': open }">
        <router-link class="nav__link" :to="{ name: 'Dashboard' }" @click="close">Inicio</router-link>
        <router-link class="nav__link" :to="{ name: 'HistoriaClinica' }" @click="close">Historia clínica</router-link>
        <router-link class="nav__link" :to="{ name: 'UserData' }" @click="close">Datos del usuario</router-link>
  <router-link class="nav__link" :to="{ name: 'PrivacyPolicy' }" @click="close">Privacidad</router-link>

        <button class="nav__logout" @click="handleLogout">Cerrar sesión</button>
      </div>
    </div>
  </nav>
  <div class="nav__spacer" />
  <div v-if="open" class="nav__backdrop" @click="close" />
  
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

export default {
  name: 'AppNavbar',
  setup() {
    const open = ref(false)
    const router = useRouter()
    const { logout } = useAuth()

    const close = () => { open.value = false }
    const handleLogout = async () => {
      await logout()
      close()
      router.push({ name: 'Login' })
    }

    return { open, close, handleLogout }
  }
}
</script>

<style scoped>
.nav { position: fixed; top: 0; left: 0; right: 0; background: #2c3e50; color: #fff; z-index: 1000; }
.nav__content { display: flex; align-items: center; justify-content: space-between; padding: 0.75rem 1rem; }
.nav__brand { color: #fff; text-decoration: none; font-weight: 600; font-size: 1.1rem; }
.nav__toggle { display: none; background: transparent; border: none; color: #fff; font-size: 1.25rem; cursor: pointer; }
.nav__links { display: flex; gap: 1rem; align-items: center; flex-wrap: wrap; }
.nav__link { color: #ecf0f1; text-decoration: none; }
.nav__link.router-link-exact-active { color: #42b983; }
.nav__logout { margin-left: 0.5rem; background: #e74c3c; color: #fff; border: none; padding: 0.5rem 0.75rem; border-radius: 4px; cursor: pointer; }
.nav__logout:hover { background: #c0392b; }
.nav__spacer { height: 56px; }

@media (max-width: 768px) {
  .nav__toggle { display: inline-block; }
  .nav__links { position: absolute; top: 56px; left: 0; right: 0; background: #2c3e50; flex-direction: column; padding: 0.75rem 1rem; display: none; }
  .nav__links--open { display: flex; }
  .nav__spacer { height: 56px; }
  .nav__backdrop { position: fixed; inset: 56px 0 0 0; background: rgba(0,0,0,0.25); }
}
</style>
